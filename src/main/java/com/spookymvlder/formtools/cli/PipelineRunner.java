package com.spookymvlder.formtools.cli;

import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FormInstance;
import com.spookymvlder.formtools.parser.FormParser;
import com.spookymvlder.formtools.parser.ImportSpecification;
import com.spookymvlder.formtools.parser.ImportXml;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.settings.ImportSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;
import com.spookymvlder.formtools.utilities.FormDebugPrinter;
import com.spookymvlder.formtools.output.JsonFormWriter;
import org.w3c.dom.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import static com.spookymvlder.formtools.processsor.ProcessFormExtract.processForms;
import static com.spookymvlder.formtools.validator.ValidateFormExtract.validateForm;

/**
 * Callable pipeline stages extracted from what used to be one inline block in
 * {@code ImportXml.main()}. Split so the CLI can offer schema-only validation and a review mode that
 * holds its result, instead of one method that always runs everything and prints-and-exits.
 */
public class PipelineRunner {

    public record PipelineResult(boolean success, List<FormInstance> forms, int logId) {
    }

    /** CSV import only — never touches the XML/XSD side at all. */
    public static PipelineResult validateSchemaOnly() {
        ImportSettings settings = ImportSettings.getInstance();
        ImportSpecification.importSpec(settings.getSpecFilePath());
        boolean success = Boolean.TRUE.equals(settings.getSpecImported());
        return new PipelineResult(success, List.of(), ErrorSettings.getInstance().getCurrentLogId());
    }

    /**
     * Full pipeline: spec import, XSD validation (unless disabled), XML parse, process, validate.
     * "success" means forms were produced to review, not that validation found zero problems —
     * review mode's whole point is to hold a form that still has errors so a human can look at them.
     */
    public static PipelineResult runFullPipeline() {
        ImportSettings settings = ImportSettings.getInstance();
        ImportSpecification.importSpec(settings.getSpecFilePath());
        if (!Boolean.TRUE.equals(settings.getSpecImported())) {
            return new PipelineResult(false, List.of(), ErrorSettings.getInstance().getCurrentLogId());
        }
        try {
            Document doc = ImportXml.importXML();
            FormParser parser = new FormParser(doc, settings.getRootField());
            List<FormInstance> forms = parser.parseForm();
            if (settings.isVerbose()) {
                FormDebugPrinter.printForms(forms);
            }
            processForms(forms, settings.getRootField());
            validateForm(forms, settings.getRootField());
            return new PipelineResult(true, forms, ErrorSettings.getInstance().getCurrentLogId());
        } catch (Exception e) {
            // Report cleanly instead of letting it surface as a raw stack trace to the menu loop -
            // e.g. a missing rootId fails here today (see ROADMAP.md: find a rootId default).
            ErrorSettings.getInstance().addToCurrentLog(new ErrorEntry(ErrorSource.PARSE_ERROR,
                    "Unexpected error while running the pipeline: " + e, ErrorSeverity.FATAL));
            return new PipelineResult(false, List.of(), ErrorSettings.getInstance().getCurrentLogId());
        }
    }

    /**
     * Prompts for a file name per form (defaulting to the source XML's own name with .json in place
     * of .xml) and writes it into the configured outputDirectory, or next to the source XML if that
     * setting is blank. Warns and asks for confirmation before overwriting an existing file - a
     * declined overwrite counts as failure for that form (nothing was written), not as success,
     * which is what keeps the caller's pending review held for a retry instead of clearing it.
     */
    public static boolean commit(List<FormInstance> forms, Scanner scanner) {
        String xmlPath = ImportSettings.getInstance().getXmlFilePath();
        String defaultFileName = defaultFileName(xmlPath);

        boolean allSucceeded = true;
        for (int i = 0; i < forms.size(); i++) {
            String suggested = forms.size() == 1 ? defaultFileName : withIndex(defaultFileName, i);
            System.out.print("Output file name for \"" + forms.get(i).getFormName() + "\" [" + suggested + "]: ");
            String fileName = scanner.nextLine().trim();
            if (fileName.isEmpty()) {
                fileName = suggested;
            }

            Path outputPath = resolveOutputPath(xmlPath, fileName);
            if (Files.exists(outputPath)) {
                System.out.print("Warning: " + outputPath + " already exists. Overwrite? (y/n): ");
                String confirm = scanner.nextLine().trim();
                if (!confirm.equalsIgnoreCase("y") && !confirm.equalsIgnoreCase("yes")) {
                    System.out.println("Skipped - not overwritten: " + outputPath);
                    allSucceeded = false;
                    continue;
                }
            }

            try {
                Path parent = outputPath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                JsonFormWriter.writeToFile(forms.get(i), outputPath.toString());
                System.out.println("Committed \"" + forms.get(i).getFormName() + "\" to " + outputPath);
            } catch (IOException e) {
                System.out.println("Failed to write output for form " + i + ": " + e.getMessage());
                allSucceeded = false;
            }
        }
        return allSucceeded;
    }

    private static String defaultFileName(String xmlPath) {
        String name = xmlPath != null ? Paths.get(xmlPath).getFileName().toString() : "form.xml";
        if (name.toLowerCase().endsWith(".xml")) {
            name = name.substring(0, name.length() - 4);
        }
        return name + ".json";
    }

    private static String withIndex(String fileName, int index) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(0, dot) + "." + index + fileName.substring(dot) : fileName + "." + index;
    }

    private static Path resolveOutputPath(String xmlPath, String fileName) {
        String outputDirectory = ImportSettings.getInstance().getOutputDirectory();
        Path dir;
        if (outputDirectory != null && !outputDirectory.isBlank()) {
            dir = Paths.get(outputDirectory);
        } else if (xmlPath != null) {
            Path xmlParent = Paths.get(xmlPath).toAbsolutePath().getParent();
            dir = xmlParent != null ? xmlParent : Paths.get(".");
        } else {
            dir = Paths.get(".");
        }
        return dir.resolve(fileName);
    }
}
