package com.spookymvlder.formtools.parser;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldPropertyRow;
import com.spookymvlder.formtools.processsor.ProcessFieldProperties;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.settings.ImportSettings;
import com.spookymvlder.formtools.settings.ParserSettings;
import com.spookymvlder.formtools.utilities.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReadCSV {

    /*
    public static Map<Integer, String[]> importCSV(String filePath) {
        List<String[]> rawLines = new ArrayList<>();
        String delimiter = ",";
        //TODO better line splitting
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((br.readLine()) != null) {
                rawLines.add(br.readLine().split(delimiter));
            }

        } catch (FileNotFoundException fnfe) {
            System.out.println("Error! File not found: " + fnfe.getMessage());
        } catch (IOException ioe) {
            System.out.println("Error! I/O Error: " + ioe.getMessage());
        }
        return splitLines(rawLines);
    }

    public static Map<Integer, String[]> splitLines(List<String[]> rawLines) {
        Map<Integer, String[]> processedSpec = new LinkedHashMap<>();
        String[] remainder = new String[rawLines.getFirst().length - 1];
        for (String[] line : rawLines) {
            if (line.length - 1 >= 0) System.arraycopy(line, 1, remainder, 0, line.length - 1);
            processedSpec.put(Integer.parseInt(line[0]), remainder);
        }

        return processedSpec;
    }*/

    @FunctionalInterface
    private interface ReaderAction<T> {
        T apply(BufferedReader reader) throws Exception;
    }

    private static <T> T withBufferedReader(String filePath, ReaderAction<T> action, T failureValue) {
        ErrorSettings errors = ErrorSettings.getInstance();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            return action.apply(br);

        } catch (FileNotFoundException fnfe) {
            errors.addToCurrentLog(new ErrorEntry(
                    ErrorSource.IMPORT_SCHEME,
                    "Error! File not found: " + fnfe.getMessage(),
                    ErrorSeverity.FATAL
            ));
        } catch (IOException ioe) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.IMPORT_SCHEME, "Error! I/O Error: " + ioe.getMessage(), ErrorSeverity.FATAL));
        }
        catch (Exception e) {
            errors.addToCurrentLog(new ErrorEntry(
                    ErrorSource.IMPORT_SCHEME,
                    "Error! CSV processing error: " + e.getMessage(),
                    ErrorSeverity.FATAL
            ));
        }

        return failureValue;
    }


    public static List<FieldProperty> importBeanCSV(String filePath) {
        /*
        //TODO review best practice for reassigning local variable here.
        List<FieldProperty> csvFields = new ArrayList<>();
        ErrorSettings errors = ErrorSettings.getInstance();
        //TODO move this elsewhere
        errors.startNewLog();
        ParserSettings.getInstance().setConverters();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            CSVReader reader = new CSVReader(br);
            String[] headerRow = reader.readNext();


            csvFields = new CsvToBeanBuilder<FieldProperty>(br)
                    .withType(FieldProperty.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(',')
                    .build()
                    .parse();

        } catch (FileNotFoundException fnfe) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.IMPORT_SCHEME,"Error! File not found: " + fnfe.getMessage(), ErrorSeverity.FATAL));
        } catch (IOException ioe) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.IMPORT_SCHEME,"Error! I/O Error: " + ioe.getMessage(), ErrorSeverity.FATAL));
        } catch (CsvValidationException e) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.IMPORT_SCHEME,"Error! CSV Validation Error: " + e.getMessage(), ErrorSeverity.FATAL));
        }
        //TODO check for specific error level.
        boolean successCheck = errors.getSeverityOfCurrentLog() < 3;
        if (successCheck) {
            ProcessFieldProperties.processFieldProperties(csvFields);
        }
        successCheck = errors.getSeverityOfCurrentLog() < 3;

        ImportSettings.getInstance().setSpecImported(successCheck);


        return csvFields;
        */
        ErrorSettings errors = ErrorSettings.getInstance();
        errors.startNewLog();
        ParserSettings.getInstance().setConverters();

        // Step 1: CSV schema validation
        validateCsvHeaders(filePath);

        // Abort early if fatal errors
        if (errors.getSeverityOfCurrentLog() >= 3) {
            ImportSettings.getInstance().setSpecImported(false);
            return List.of();
        }

        // Step 2: Parse CSV
        List<FieldPropertyRow> csvFields = parseCsv(filePath);

        // Step 3: Process fields
        if (errors.getSeverityOfCurrentLog() < 3) {
            List<FieldProperty> = ProcessFieldProperties.processFieldProperties(csvFields);
        }

        boolean success = errors.getSeverityOfCurrentLog() < 3;
        ImportSettings.getInstance().setSpecImported(success);

        return csvFields;
    }

    private static void validateCsvHeaders(String filePath) {
        withBufferedReader(filePath, reader -> {

            CSVReader csvReader = new CSVReader(reader);
            String[] headers = csvReader.readNext();

            if (headers == null) {
                ErrorSettings.getInstance().addToCurrentLog(
                        new ErrorEntry(
                                ErrorSource.IMPORT_SCHEME,
                                "CSV file is empty",
                                ErrorSeverity.FATAL
                        )
                );
                return null;
            }

            validateRequiredHeaders(headers);
            return null;

        }, null);
    }

    /**
     * For required columns that may optionally have empty values.
     * @param headers
     */
    private static void validateRequiredHeaders(String[] headers) {
        Set<String> headerSet = Arrays.stream(headers)
                .map(String::trim)
                .collect(Collectors.toSet());

        List<String> requiredHeaders = List.of(
                "parentId",
                "dataType"
                // add others that must exist structurally
        );

        List<String> missing = requiredHeaders.stream()
                .filter(h -> !headerSet.contains(h))
                .toList();

        if (!missing.isEmpty()) {
            ErrorSettings.getInstance().addToCurrentLog(
                    new ErrorEntry(
                            ErrorSource.IMPORT_SCHEME,
                            "CSV is missing required columns: " + String.join(", ", missing),
                            ErrorSeverity.FATAL
                    )
            );
        }
    }

    private static List<FieldPropertyRow> parseCsv(String filePath) {
        return withBufferedReader(filePath, reader ->
                        new CsvToBeanBuilder<FieldPropertyRow>(reader)
                                .withType(FieldPropertyRow.class)
                                .withIgnoreLeadingWhiteSpace(true)
                                .withSeparator(',')
                                .build()
                                .parse(),
                List.of()
        );
    }



}
