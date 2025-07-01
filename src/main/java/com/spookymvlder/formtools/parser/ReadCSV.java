package com.spookymvlder.formtools.parser;

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
}
