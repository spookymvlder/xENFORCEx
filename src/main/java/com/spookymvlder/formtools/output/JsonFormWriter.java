package com.spookymvlder.formtools.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spookymvlder.formtools.datatypes.FormInstance;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serializes the {@link FormValueBuilder} value tree to JSON, wrapped in a small envelope
 * (formName + generatedAt) for traceability - a consumer reading a file later shouldn't have to
 * infer which form or when solely from a filename.
 */
public final class JsonFormWriter {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    private JsonFormWriter() {
    }

    public static String toJson(FormInstance instance) {
        return GSON.toJson(toEnvelope(instance));
    }

    public static void writeToFile(FormInstance instance, String path) throws IOException {
        try (Writer writer = new FileWriter(path)) {
            GSON.toJson(toEnvelope(instance), writer);
        }
    }

    private static Map<String, Object> toEnvelope(FormInstance instance) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("formName", instance.getFormName());
        envelope.put("generatedAt", LocalDateTime.now().toString());
        envelope.put("data", FormValueBuilder.build(instance));
        return envelope;
    }
}
