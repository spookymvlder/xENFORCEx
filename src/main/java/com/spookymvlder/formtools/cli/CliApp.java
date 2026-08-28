package com.spookymvlder.formtools.cli;

import com.spookymvlder.formtools.settings.ErrorSettings;

import java.io.IOException;
import java.util.Scanner;

/**
 * The program's entry point and only long-running loop. Everything here exists because review mode
 * has to hold an in-memory FormInstance list until it's committed or discarded, and that's only
 * possible if the process stays alive between those two actions - there's nothing durable to reload
 * from a second invocation.
 */
public class CliApp {

    private final ReviewSession reviewSession = new ReviewSession();

    public static void main(String[] args) {
        new CliApp().run();
    }

    public void run() {
        loadStartupConfig();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("> ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> SettingsMenu.run(scanner);
                case "2" -> runSchemaOnly();
                case "3" -> runReview(scanner);
                case "4" -> runCommit();
                case "5" -> discardPending();
                case "6" -> browseErrorLogs(scanner);
                case "7" -> running = false;
                default -> System.out.println("Unrecognized option: " + choice);
            }
        }
        System.out.println("Exiting.");
    }

    private void loadStartupConfig() {
        try {
            CliConfig config = ConfigFileManager.load(ConfigFileManager.DEFAULT_CONFIG_PATH);
            ConfigFileManager.applyToRuntime(config);
            System.out.println("Loaded settings from " + ConfigFileManager.DEFAULT_CONFIG_PATH + ".");
        } catch (IOException e) {
            System.out.println("No config file found at " + ConfigFileManager.DEFAULT_CONFIG_PATH
                    + " - starting with defaults. Use Edit Settings to configure and save one.");
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Edit settings");
        System.out.println("2. Validate schema only");
        System.out.println("3. Review" + (reviewSession.hasPending() ? " (a form is already pending)" : ""));
        System.out.println("4. Commit pending form" + (reviewSession.hasPending() ? "" : " (nothing pending)"));
        System.out.println("5. Discard pending form" + (reviewSession.hasPending() ? "" : " (nothing pending)"));
        System.out.println("6. Review error logs");
        System.out.println("7. Exit");
    }

    private void runSchemaOnly() {
        PipelineRunner.PipelineResult result = PipelineRunner.validateSchemaOnly();
        System.out.println(result.success() ? "Schema imported successfully." : "Schema import failed.");
        ErrorSettings.getInstance().printErrorLog(result.logId());
    }

    private void runReview(Scanner scanner) {
        if (reviewSession.hasPending()) {
            System.out.print("A form is already pending. [C]ommit, [D]iscard, or [A]bort this review? ");
            String choice = scanner.nextLine().trim().toUpperCase();
            if (choice.equals("C")) {
                runCommit();
            } else if (choice.equals("D")) {
                discardPending();
            } else {
                System.out.println("Review canceled.");
                return;
            }
            if (reviewSession.hasPending()) {
                // Commit is a stub and never actually clears the pending form - don't start a
                // second review on top of one that's still sitting there.
                return;
            }
        }

        PipelineRunner.PipelineResult result = PipelineRunner.runFullPipeline();
        ErrorSettings.getInstance().printErrorLog(result.logId());
        if (!result.forms().isEmpty()) {
            reviewSession.hold(result.forms(), result.logId());
            System.out.println("Form held for review. Use Commit or Discard when ready.");
        } else {
            System.out.println("No form was produced to review.");
        }
    }

    private void runCommit() {
        if (!reviewSession.hasPending()) {
            System.out.println("Nothing pending to commit.");
            return;
        }
        PipelineRunner.commit(reviewSession.getForms());
    }

    private void discardPending() {
        if (!reviewSession.hasPending()) {
            System.out.println("Nothing pending to discard.");
            return;
        }
        reviewSession.clear();
        System.out.println("Pending form discarded.");
    }

    private void browseErrorLogs(Scanner scanner) {
        int count = ErrorSettings.getInstance().getLogCount();
        if (count == 0) {
            System.out.println("No error logs yet this session.");
            return;
        }
        System.out.println("Logs 0.." + (count - 1) + " available this session.");
        System.out.print("Enter a log number to view, or blank to go back: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return;
        }
        try {
            int logId = Integer.parseInt(input);
            if (!ErrorSettings.getInstance().logExists(logId)) {
                System.out.println("No log with that number.");
                return;
            }
            ErrorSettings.getInstance().printErrorLog(logId);
        } catch (NumberFormatException e) {
            System.out.println("Not a number.");
        }
    }
}
