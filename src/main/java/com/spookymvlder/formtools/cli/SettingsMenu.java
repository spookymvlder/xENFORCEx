package com.spookymvlder.formtools.cli;

import com.spookymvlder.formtools.utilities.ErrorSeverity;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Edits a working copy of {@link CliConfig} against three states: on-disk (the config file),
 * active (the live runtime singletons), and working (in-progress edits, not yet applied).
 * Cancel discards working only. Apply pushes working -> active. Apply-and-save also writes
 * active -> file. Reload re-reads the file and replaces both active and working, since the whole
 * point of "only reload when manually triggered" is that active and the file are allowed to diverge
 * until this is explicitly asked for.
 */
public class SettingsMenu {

    public static void run(Scanner scanner) {
        CliConfig working = ConfigFileManager.snapshotFromRuntime();

        while (true) {
            printSettings(working);
            System.out.println();
            System.out.print("Enter a number to edit, or [C]ancel [A]pply [S]ave and apply [R]eload from file: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            switch (input.toUpperCase()) {
                case "C":
                    System.out.println("Discarding changes.");
                    return;
                case "A":
                    ConfigFileManager.applyToRuntime(working);
                    System.out.println("Applied for this session (not saved to config file).");
                    return;
                case "S":
                    ConfigFileManager.applyToRuntime(working);
                    try {
                        ConfigFileManager.save(working, ConfigFileManager.DEFAULT_CONFIG_PATH);
                        System.out.println("Applied and saved to " + ConfigFileManager.DEFAULT_CONFIG_PATH + ".");
                    } catch (IOException e) {
                        System.out.println("Applied for this session, but failed to save: " + e.getMessage());
                    }
                    return;
                case "R":
                    try {
                        CliConfig reloaded = ConfigFileManager.load(ConfigFileManager.DEFAULT_CONFIG_PATH);
                        ConfigFileManager.applyToRuntime(reloaded);
                        working = reloaded.copy();
                        System.out.println("Reloaded from " + ConfigFileManager.DEFAULT_CONFIG_PATH + ".");
                    } catch (IOException e) {
                        System.out.println("Failed to reload: " + e.getMessage() + " (keeping current edits).");
                    }
                    break;
                default:
                    editByNumber(input, working, scanner);
            }
        }
    }

    private static void printSettings(CliConfig c) {
        System.out.println();
        System.out.println("1. xmlFilePath        = " + display(c.getXmlFilePath()));
        System.out.println("2. xsdFilePath        = " + display(c.getXsdFilePath()));
        System.out.println("3. specFilePath       = " + display(c.getSpecFilePath()));
        System.out.println("4. rootId             = " + display(c.getRootId()));
        System.out.println("5. skipXsdValidation  = " + c.isSkipXsdValidation());
        System.out.println("6. dateFormat         = " + c.getDateFormat());
        System.out.println("7. errorThreshold     = " + c.getErrorThreshold());
    }

    private static String display(String value) {
        return (value == null || value.isBlank()) ? "(not set)" : value;
    }

    private static void editByNumber(String input, CliConfig working, Scanner scanner) {
        switch (input) {
            case "1" -> working.setXmlFilePath(promptPath(scanner, "xmlFilePath", working.getXmlFilePath()));
            case "2" -> working.setXsdFilePath(promptPath(scanner, "xsdFilePath", working.getXsdFilePath()));
            case "3" -> working.setSpecFilePath(promptPath(scanner, "specFilePath", working.getSpecFilePath()));
            case "4" -> working.setRootId(promptString(scanner, "rootId", working.getRootId()));
            case "5" -> working.setSkipXsdValidation(
                    promptBoolean(scanner, "skipXsdValidation", working.isSkipXsdValidation()));
            case "6" -> working.setDateFormat(promptString(scanner, "dateFormat", working.getDateFormat()));
            case "7" -> working.setErrorThreshold(promptSeverity(scanner, working.getErrorThreshold()));
            default -> System.out.println("Unrecognized option: " + input);
        }
    }

    /** Blank input keeps the current value rather than clearing it - there's no sensible reason a
     * form author would want any of these settings to become blank as a normal edit. */
    private static String promptString(Scanner scanner, String label, String current) {
        System.out.print("New " + label + " [" + display(current) + "]: ");
        String value = scanner.nextLine().trim();
        return value.isEmpty() ? current : value;
    }

    private static String promptPath(Scanner scanner, String label, String current) {
        String value = promptString(scanner, label, current);
        if (value != null && !new File(value).exists()) {
            System.out.println("Warning: no file currently exists at that path.");
        }
        return value;
    }

    private static boolean promptBoolean(Scanner scanner, String label, boolean current) {
        while (true) {
            System.out.print("New " + label + " (true/false) [" + current + "]: ");
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                return current;
            }
            if (value.equalsIgnoreCase("true")) {
                return true;
            }
            if (value.equalsIgnoreCase("false")) {
                return false;
            }
            System.out.println("Please enter true or false.");
        }
    }

    private static ErrorSeverity promptSeverity(Scanner scanner, ErrorSeverity current) {
        while (true) {
            System.out.print("New errorThreshold (NONE, NOTIFICATION, WARNING, ERROR, FATAL) [" + current + "]: ");
            String value = scanner.nextLine().trim();
            if (value.isEmpty()) {
                return current;
            }
            try {
                return ErrorSeverity.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Not a recognized severity.");
            }
        }
    }
}
