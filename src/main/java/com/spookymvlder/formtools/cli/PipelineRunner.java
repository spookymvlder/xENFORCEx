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
import org.w3c.dom.Document;

import java.util.List;

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
            FormDebugPrinter.printForms(forms);
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

    /** Stub pending Phase 4 (program output design). Does not clear the caller's pending form. */
    public static void commit(List<FormInstance> forms) {
        System.out.println("Commit is not implemented yet - program output has not been designed. "
                + "See ROADMAP.md Phase 4. The pending form is still held; nothing was lost.");
    }
}
