package com.spookymvlder.formtools.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.spookymvlder.formtools.datatypes.CodeSystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory registry of imported/entered CodeSystems, persisted as one JSON file per code system
 * under {@link #DIRECTORY} (a bare, CWD-relative directory, matching the convention
 * {@link ConfigFileManager#DEFAULT_CONFIG_PATH} already uses - this codebase has no app-data-directory
 * mechanism to extend instead). Reuses Gson, already a project dependency via JsonFormWriter, rather
 * than adding any new persistence dependency.
 * <p>
 * Deliberately shallow: no caching layer beyond this in-memory map, no migrations, no partial-write
 * safety beyond what Files/FileWriter already give. Changing a code system's values always means
 * re-saving the whole object - there is no merge/patch operation here, matching the CLI's own
 * "reimport, don't edit in place" rule for individual codes.
 */
public class CodeSystemRegistry {
    public static final String DIRECTORY = "codesystems";

    private static CodeSystemRegistry instance;

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private final Map<String, CodeSystem> codeSystems = new LinkedHashMap<>();
    private boolean loaded = false;

    private CodeSystemRegistry() {
    }

    public static CodeSystemRegistry getInstance() {
        if (instance == null) {
            instance = new CodeSystemRegistry();
        }
        return instance;
    }

    public Optional<CodeSystem> find(String codeSystemId) {
        ensureLoaded();
        return Optional.ofNullable(codeSystemId == null ? null : codeSystems.get(codeSystemId));
    }

    public List<CodeSystem> list() {
        ensureLoaded();
        return List.copyOf(codeSystems.values());
    }

    public void save(CodeSystem codeSystem) throws IOException {
        if (codeSystem.getCodeSystemId() == null || codeSystem.getCodeSystemId().isBlank()) {
            throw new IOException("A code system must have a codeSystemId before it can be saved.");
        }
        ensureLoaded();
        File dir = new File(DIRECTORY);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create directory " + DIRECTORY);
        }
        File file = fileFor(codeSystem.getCodeSystemId());
        try (var writer = Files.newBufferedWriter(file.toPath())) {
            GSON.toJson(codeSystem, writer);
        }
        codeSystems.put(codeSystem.getCodeSystemId(), codeSystem);
    }

    public void delete(String codeSystemId) throws IOException {
        ensureLoaded();
        File file = fileFor(codeSystemId);
        if (file.exists() && !file.delete()) {
            throw new IOException("Could not delete " + file.getPath());
        }
        codeSystems.remove(codeSystemId);
    }

    /** Reads a CodeSystem identity (same JSON shape as the persisted form) from an arbitrary file -
     * used by the CLI's "import identity from JSON" flow, distinct from the registry's own storage. */
    public CodeSystem readIdentityFile(String path) throws IOException {
        try (var reader = Files.newBufferedReader(new File(path).toPath())) {
            CodeSystem codeSystem = GSON.fromJson(reader, CodeSystem.class);
            if (codeSystem == null) {
                throw new IOException("File did not contain a recognizable code system.");
            }
            return codeSystem;
        }
    }

    public void reload() {
        codeSystems.clear();
        loaded = false;
        ensureLoaded();
    }

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        File dir = new File(DIRECTORY);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            try (var reader = Files.newBufferedReader(file.toPath())) {
                CodeSystem codeSystem = GSON.fromJson(reader, CodeSystem.class);
                if (codeSystem != null && codeSystem.getCodeSystemId() != null) {
                    codeSystems.put(codeSystem.getCodeSystemId(), codeSystem);
                }
            } catch (IOException e) {
                // A single unreadable file shouldn't block the rest of the registry from loading.
                System.out.println("Warning: could not load code system file " + file.getPath() + ": " + e.getMessage());
            }
        }
    }

    private static File fileFor(String codeSystemId) {
        return new File(DIRECTORY, codeSystemId + ".json");
    }

    /** ISO-8601 (yyyy-MM-dd), matching the rest of the codebase's date convention. */
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return LocalDate.parse(in.nextString());
        }
    }
}
