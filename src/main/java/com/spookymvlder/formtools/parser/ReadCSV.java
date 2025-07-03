package com.spookymvlder.formtools.parser;

import com.opencsv.bean.CsvToBeanBuilder;
import com.spookymvlder.formtools.datatypes.FieldProperties;
import com.spookymvlder.formtools.settings.ImportSettings;
import com.spookymvlder.formtools.settings.ParserSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.datatypes.FieldPropertyValidationError;
import com.spookymvlder.formtools.utilities.FieldPropertyValidator;

import java.io.*;
import java.util.*;

public class ReadCSV {

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
    }

    public static List<FieldProperties> importBeanCSV(String filePath) throws FileNotFoundException {
        List<FieldProperties> csvFields = new ArrayList<>();
        List<FieldPropertyValidationError> fieldErrors = new ArrayList<>();

        ParserSettings.getInstance().setConverters();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            csvFields = new CsvToBeanBuilder<FieldProperties>(br)
                    .withType(FieldProperties.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator('\t')
                    .build()
                    .parse();

        } catch (FileNotFoundException fnfe) {
            System.out.println("Error! File not found: " + fnfe.getMessage());

        } catch (IOException ioe) {
            System.out.println("Error! I/O Error: " + ioe.getMessage());

        }

        for (FieldProperties field : csvFields) {
            fieldErrors.addAll(FieldPropertyValidator.validateWithErrors(field));
        }
        boolean successCheck = true;
        if (!fieldErrors.isEmpty()) {
            successCheck = fieldErrors.stream().allMatch(e -> e.getSeverity() == ErrorSeverity.ERROR);

            System.out.println("Validation errors found.");
            for (FieldPropertyValidationError error : fieldErrors) {
                System.out.println("   -" + error);
            }
        }

        ImportSettings.getInstance().setSpecImported(successCheck);


        return csvFields;
    }
}
