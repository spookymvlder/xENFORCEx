package com.spookymvlder.formtools.parser;

import com.opencsv.bean.CsvToBeanBuilder;
import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FieldProperty;
import com.spookymvlder.formtools.processsor.ProcessFieldProperties;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.settings.ImportSettings;
import com.spookymvlder.formtools.settings.ParserSettings;
import com.spookymvlder.formtools.utilities.*;

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

    public static List<FieldProperty> importBeanCSV(String filePath) {
        //TODO review best practice for reassigning local variable here.
        List<FieldProperty> csvFields = new ArrayList<>();
        ErrorSettings errors = ErrorSettings.getInstance();
        errors.startNewLog();
        ParserSettings.getInstance().setConverters();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
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
        }
        //TODO check for specific error level.
        boolean successCheck = errors.getSeverityOfCurrentLog() < 3;
        if (successCheck) {
            ProcessFieldProperties.processFieldProperties(csvFields);
        }
        successCheck = errors.getSeverityOfCurrentLog() < 3;

        ImportSettings.getInstance().setSpecImported(successCheck);


        return csvFields;
    }
}
