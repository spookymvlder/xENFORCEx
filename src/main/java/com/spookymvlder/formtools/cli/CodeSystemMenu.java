package com.spookymvlder.formtools.cli;

import com.spookymvlder.formtools.datatypes.CodeEntry;
import com.spookymvlder.formtools.datatypes.CodeSystem;
import com.spookymvlder.formtools.parser.ReadCSV;
import com.spookymvlder.formtools.settings.CodeSystemRegistry;
import com.spookymvlder.formtools.settings.ErrorSettings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Manages imported/entered code systems: list/view/edit/delete, and adding a new one (identity via
 * JSON import or manual entry, code values via CSV import or manual entry). Deliberately does not
 * support editing individual codes in place - changing a code system's values always means
 * re-importing the whole CSV, matching the explicit "not a code management system" scope decision.
 */
public class CodeSystemMenu {
    private static final String IDENTITY_TEMPLATE_RESOURCE = "Documentation/codeSystemIdentityTemplate.json";
    private static final String VALUES_TEMPLATE_RESOURCE = "Documentation/codeSystemValuesTemplate.csv";

    private final CodeSystemRegistry registry = CodeSystemRegistry.getInstance();

    public static void run(Scanner scanner) {
        new CodeSystemMenu().loop(scanner);
    }

    private void loop(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("1. View Code Systems");
            System.out.println("2. Add New Code System");
            System.out.println("3. Back");
            System.out.print("> ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewCodeSystems(scanner);
                case "2" -> addNewCodeSystem(scanner);
                case "3" -> {
                    return;
                }
                default -> System.out.println("Unrecognized option: " + choice);
            }
        }
    }

    // ---- View / edit / delete ----

    private void viewCodeSystems(Scanner scanner) {
        List<CodeSystem> systems = registry.list();
        if (systems.isEmpty()) {
            System.out.println("No code systems yet.");
            return;
        }
        for (int i = 0; i < systems.size(); i++) {
            CodeSystem cs = systems.get(i);
            System.out.printf("%d. %s - %s%s (%d codes%s)%n", i + 1, cs.getCodeSystemId(),
                    cs.getCodeSystemName() == null ? "(no name)" : cs.getCodeSystemName(),
                    cs.getCodeSystemVersion() == null ? "" : " v" + cs.getCodeSystemVersion(),
                    cs.getCodeValues().size(), cs.isStub() ? ", stub" : "");
        }
        System.out.print("Enter a number to select, or blank to go back: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return;
        }
        int index;
        try {
            index = Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Not a number.");
            return;
        }
        if (index < 0 || index >= systems.size()) {
            System.out.println("No code system with that number.");
            return;
        }
        manageCodeSystem(scanner, systems.get(index));
    }

    private void manageCodeSystem(Scanner scanner, CodeSystem codeSystem) {
        while (true) {
            System.out.println();
            System.out.println("Code system: " + codeSystem.getCodeSystemId());
            System.out.println("1. Edit identity (name/version)");
            System.out.println("2. View code values");
            System.out.println("3. Delete");
            System.out.println("4. Back");
            System.out.print("> ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> editIdentity(scanner, codeSystem);
                case "2" -> viewCodeValues(codeSystem);
                case "3" -> {
                    if (deleteCodeSystem(scanner, codeSystem)) {
                        return;
                    }
                }
                case "4" -> {
                    return;
                }
                default -> System.out.println("Unrecognized option: " + choice);
            }
        }
    }

    private void editIdentity(Scanner scanner, CodeSystem codeSystem) {
        System.out.print("New codeSystemName [" + display(codeSystem.getCodeSystemName()) + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) {
            codeSystem.setCodeSystemName(name);
        }
        System.out.print("New codeSystemVersion [" + display(codeSystem.getCodeSystemVersion()) + "]: ");
        String version = scanner.nextLine().trim();
        if (!version.isEmpty()) {
            codeSystem.setCodeSystemVersion(version);
        }
        try {
            registry.save(codeSystem);
            System.out.println("Saved.");
        } catch (IOException e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    private void viewCodeValues(CodeSystem codeSystem) {
        List<CodeEntry> values = codeSystem.getCodeValues();
        if (values.isEmpty()) {
            System.out.println("This code system has no code values loaded (it's a stub).");
            return;
        }
        int shown = Math.min(values.size(), 50);
        for (int i = 0; i < shown; i++) {
            CodeEntry entry = values.get(i);
            System.out.println("  " + entry.getCode() + " - " + entry.getDisplayName()
                    + (entry.isActive() ? "" : " (inactive)")
                    + (entry.getExpireDate() != null ? " (expires " + entry.getExpireDate() + ")" : ""));
        }
        if (values.size() > shown) {
            System.out.println("  ... and " + (values.size() - shown) + " more.");
        }
    }

    private boolean deleteCodeSystem(Scanner scanner, CodeSystem codeSystem) {
        System.out.print("Delete code system " + codeSystem.getCodeSystemId() + "? This cannot be undone. [y/N]: ");
        String confirm = scanner.nextLine().trim();
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Canceled.");
            return false;
        }
        try {
            registry.delete(codeSystem.getCodeSystemId());
            System.out.println("Deleted.");
            return true;
        } catch (IOException e) {
            System.out.println("Failed to delete: " + e.getMessage());
            return false;
        }
    }

    // ---- Add new ----

    private void addNewCodeSystem(Scanner scanner) {
        CodeSystem codeSystem = resolveIdentity(scanner);
        if (codeSystem == null) {
            return;
        }
        resolveCodeValues(scanner, codeSystem);

        try {
            registry.save(codeSystem);
            System.out.println("Saved code system " + codeSystem.getCodeSystemId() + " to "
                    + CodeSystemRegistry.DIRECTORY + "/" + codeSystem.getCodeSystemId() + ".json");
        } catch (IOException e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    private CodeSystem resolveIdentity(Scanner scanner) {
        System.out.print("Import identity from JSON, or enter manually? [I/M]: ");
        String choice = scanner.nextLine().trim().toUpperCase();

        if (choice.equals("I")) {
            offerTemplate(scanner, IDENTITY_TEMPLATE_RESOURCE, "identity JSON");
            System.out.print("Path to the identity JSON file (blank to enter manually instead): ");
            String path = scanner.nextLine().trim();
            if (!path.isEmpty()) {
                try {
                    CodeSystem imported = registry.readIdentityFile(path);
                    if (imported.getCodeSystemId() == null || imported.getCodeSystemId().isBlank()) {
                        System.out.println("That file has no codeSystemId set - falling back to manual entry.");
                    } else {
                        return imported;
                    }
                } catch (IOException e) {
                    System.out.println("Could not read that file (" + e.getMessage() + ") - falling back to manual entry.");
                }
            }
        }

        return manualIdentity(scanner);
    }

    private CodeSystem manualIdentity(Scanner scanner) {
        String id;
        do {
            System.out.print("codeSystemId (required): ");
            id = scanner.nextLine().trim();
        } while (id.isEmpty());

        System.out.print("codeSystemName (optional): ");
        String name = scanner.nextLine().trim();
        System.out.print("codeSystemVersion (optional): ");
        String version = scanner.nextLine().trim();

        return new CodeSystem(id, name.isEmpty() ? null : name, version.isEmpty() ? null : version);
    }

    private void resolveCodeValues(Scanner scanner, CodeSystem codeSystem) {
        System.out.print("Import code values from CSV, enter manually, or skip (leave as a stub)? [I/M/S]: ");
        String choice = scanner.nextLine().trim().toUpperCase();

        if (choice.equals("I")) {
            offerTemplate(scanner, VALUES_TEMPLATE_RESOURCE, "code values CSV");
            System.out.print("Path to the code values CSV file: ");
            String path = scanner.nextLine().trim();
            if (!path.isEmpty()) {
                List<CodeEntry> entries = ReadCSV.importCodeValuesCsv(path);
                ErrorSettings errors = ErrorSettings.getInstance();
                errors.printErrorLog(errors.getLogCount() - 1);
                if (!entries.isEmpty()) {
                    codeSystem.setCodeValues(entries);
                    System.out.println("Loaded " + entries.size() + " codes.");
                } else {
                    System.out.println("No codes were loaded - leaving this code system as a stub.");
                }
            }
            return;
        }

        if (choice.equals("M")) {
            List<CodeEntry> entries = new ArrayList<>();
            System.out.println("Enter codes one at a time. Blank code to stop.");
            while (true) {
                System.out.print("  code: ");
                String code = scanner.nextLine().trim();
                if (code.isEmpty()) {
                    break;
                }
                System.out.print("  displayName: ");
                String displayName = scanner.nextLine().trim();
                System.out.print("  active [T]: ");
                String activeInput = scanner.nextLine().trim();
                Boolean active = activeInput.isEmpty() ? null
                        : activeInput.equalsIgnoreCase("f") || activeInput.equalsIgnoreCase("false") ? false : true;
                System.out.print("  expireDate (yyyy-MM-dd, optional): ");
                String expireInput = scanner.nextLine().trim();
                LocalDate expireDate = null;
                if (!expireInput.isEmpty()) {
                    try {
                        expireDate = LocalDate.parse(expireInput);
                    } catch (DateTimeParseException e) {
                        System.out.println("  Could not parse that date - leaving expireDate blank.");
                    }
                }
                entries.add(new CodeEntry(code, displayName, active, expireDate));
            }
            if (!entries.isEmpty()) {
                codeSystem.setCodeValues(entries);
            }
            return;
        }

        // Anything else (including S): stays a stub.
    }

    // ---- Shared helpers ----

    private void offerTemplate(Scanner scanner, String resourceName, String label) {
        System.out.print("Save a blank " + label + " template first? [y/N]: ");
        String offer = scanner.nextLine().trim();
        if (!offer.equalsIgnoreCase("y")) {
            return;
        }
        System.out.print("Save template to path: ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            return;
        }
        try {
            copyTemplate(resourceName, path);
            System.out.println("Template saved to " + path);
        } catch (IOException e) {
            System.out.println("Failed to save template: " + e.getMessage());
        }
    }

    private void copyTemplate(String resourceName, String destPath) throws IOException {
        try (InputStream in = CodeSystemMenu.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IOException("Bundled template resource not found: " + resourceName);
            }
            File dest = new File(destPath);
            File parent = dest.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String display(String value) {
        return (value == null || value.isBlank()) ? "(not set)" : value;
    }
}
