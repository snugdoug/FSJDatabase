package com.snugdoug.fsjdatabase.database.query;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.snugdoug.fsjdatabase.database.FSJDatabase.metadataCache;
import com.snugdoug.fsjdatabase.database.createStructure.TableMetadata;

public class Query {
    private final Class<?> tableClass;
    private final List<String> selectedFields = new ArrayList<>();
    private String whereClause;
    private String orderByField;
    private boolean orderDescending = false;

    public Query(Class<?> tableClass) {
        this.tableClass = tableClass;
    }

    public Query select(String... fields) {
        selectedFields.addAll(Arrays.asList(fields));
        return this;
    }

    public Query where(String whereClause) {
        this.whereClause = whereClause;
        return this;
    }

    public Query orderBy(String field, boolean descending) {
        this.orderByField = field;
        this.orderDescending = descending;
        return this;
    }

    public List<Map<String, Object>> execute() throws IOException {
        TableMetadata metadata = metadataCache.get(tableClass);
        if (metadata == null) {
            throw new IllegalArgumentException("Class " + tableClass.getName() + " is not registered as a table.");
        }

        List<Map<String, Object>> results = new ArrayList<>();
        Path tablePath = metadata.getTablePath();

        Files.list(tablePath).forEach(filePath -> {
            try {
                Map<String, Object> rowData = getRowData(filePath);
                if (whereClause == null || evaluateCondition(rowData, whereClause)) {
                    if (!selectedFields.isEmpty()) {
                        Map<String, Object> filteredData = new HashMap<>();
                        for (String field : selectedFields) {
                            filteredData.put(field, rowData.get(field));
                        }
                        results.add(filteredData);
                    } else {
                        results.add(rowData);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        if (orderByField != null) {
            results.sort((row1, row2) -> {
                Comparable<Object> val1 = (Comparable<Object>) row1.get(orderByField);
                Comparable<Object> val2 = (Comparable<Object>) row2.get(orderByField);

                return orderDescending ? val2.compareTo(val1) : val1.compareTo(val2);
            });
        }

        return results;
    }

    private static Map<String, Object> getRowData(Path filePath) throws IOException {
        Map<String, Object> rowData = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String key = parts[0];
                    String value = parts[1];
                    rowData.put(key, value);
                }
            }
        }
        return rowData;
    }

    private static boolean evaluateCondition(Map<String, Object> rowData, String condition) {
        Pattern pattern = Pattern.compile("(\\w+)\\s*([><=!]=?)\\s*(\\w+)");
        Matcher matcher = pattern.matcher(condition);

        if (matcher.find()) {
            String column = matcher.group(1).trim();
            String operator = matcher.group(2).trim();
            String value = matcher.group(3).trim();

            Object rowValue = rowData.get(column);
            if (rowValue == null) return false;

            try {
                int intRowValue = Integer.parseInt(rowValue.toString());
                int intValue = Integer.parseInt(value);

                switch (operator) {
                    case ">": return intRowValue > intValue;
                    case "<": return intRowValue < intValue;
                    case ">=": return intRowValue >= intValue;
                    case "<=": return intRowValue <= intValue;
                    case "=": return intRowValue == intValue;
                    case "!=": return intRowValue != intValue;
                }
            } catch (NumberFormatException ex) {
                switch (operator) {
                    case "=": return rowValue.toString().equals(value);
                    case "!=": return !rowValue.toString().equals(value);
                }
            }
        }
        return false;
    }
}
