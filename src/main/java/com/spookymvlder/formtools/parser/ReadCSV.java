package com.spookymvlder.formtools.parser;

import com.opencsv.bean.CsvToBeanBuilder;
import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FieldProperties;
import com.spookymvlder.formtools.settings.ImportSettings;
import com.spookymvlder.formtools.settings.ParserSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;
import com.spookymvlder.formtools.utilities.FieldHierarchyValidator;
import com.spookymvlder.formtools.utilities.FieldPropertyValidator;

import java.io.*;
import java.util.*;

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

    public static List<FieldProperties> importBeanCSV(String filePath) {
        List<FieldProperties> csvFields = new ArrayList<>();
        //TODO send error log somewhere
        List<ErrorEntry> errors = new ArrayList<>();

        ParserSettings.getInstance().setConverters();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            csvFields = new CsvToBeanBuilder<FieldProperties>(br)
                    .withType(FieldProperties.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator('\t')
                    .build()
                    .parse();

        } catch (FileNotFoundException fnfe) {
            errors.add(new ErrorEntry(ErrorSource.IMPORT_SCHEME,"Error! File not found: " + fnfe.getMessage(), ErrorSeverity.FATAL));
        } catch (IOException ioe) {
            errors.add(new ErrorEntry(ErrorSource.IMPORT_SCHEME,"Error! I/O Error: " + ioe.getMessage(), ErrorSeverity.FATAL));
        }
        boolean successCheck = errors.isEmpty();

        if(successCheck) {
            for (FieldProperties field : csvFields) {
                errors.addAll(FieldPropertyValidator.validateWithErrors(field));
            }
            errors.addAll(FieldHierarchyValidator.validateHierarchy(csvFields));
        }
        //Only if we added something during validation.
        //TODO remove this code and make it accessible via CLI log or error log
        if (successCheck && !errors.isEmpty()) {
            //TODO add check for fatal as well.
            successCheck = errors.stream().allMatch(e -> e.getSeverity() == ErrorSeverity.ERROR);

            System.out.println("Validation errors found.");
            for (ErrorEntry error : errors) {
                System.out.println("   -" + error);
            }
        }

        //TODO move someplace else
        ParserSettings settings = ParserSettings.getInstance();
        for (FieldProperties field : csvFields) {
            settings.addToFieldIdToName(field.getFieldId(), field.getFieldName());
            settings.addToFieldFromId(field.getFieldId(), field);
        }

        ImportSettings.getInstance().setSpecImported(successCheck);


        return csvFields;
    }
}
