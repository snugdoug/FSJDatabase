package com.snugdoug.fsjdatabase.database.query;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.snugdoug.fsjdatabase.database.FSJDatabase.metadataCache;
import com.snugdoug.fsjdatabase.database.createStructure.TableMetadata;

public class Query {
    public static List<Map<String, Object>> query(Class<?> cls, String queryString) throws IOException {
        TableMetadata metadata = metadataCache.get(cls);
        if (metadata == null) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is not registered as a table.");
        }

        List<Map<String, Object>> results = new ArrayList<>();

        String whereClause = null;

        if (queryString.contains("WHERE")) { // Check for WHERE clause correctly
            String[] parts = queryString.split("WHERE");
            whereClause = parts[1].trim();
        } else {
            // If no WHERE clause, queryString is just the table name
            if (!queryString.equals(cls.getSimpleName())) {
                throw new IllegalArgumentException("Invalid query: Table name does not match class name.");
            }
        }

        Path tablePath = metadata.getTablePath();

        if (whereClause != null) {
            List<String> conditions = splitWhereClause(whereClause);
            try (java.util.stream.Stream<Path> files = Files.list(tablePath)) {
                files.forEach(filePath -> {
                    try {
                        Map<String, Object> rowData = getRowData(filePath);
                        boolean match = true;
                        for (String condition : conditions) {
                            if (!evaluateCondition(rowData, condition)) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            results.add(rowData);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } else {
            try (java.util.stream.Stream<Path> files = Files.list(tablePath)) {
                files.forEach(filePath -> {
                    try {
                        Map<String, Object> rowData = getRowData(filePath);
                        results.add(rowData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        return results;
    }

    private static boolean evaluateJoinCondition(Map<String, Object> leftRow, Map<String, Object> rightRow, String joinCondition) {
        String[] parts = joinCondition.split("=");
        if (parts.length == 2) {
            String leftKey = parts[0].trim();
            String rightKey = parts[1].trim();

            String[] leftParts = leftKey.split("\\.");
            String[] rightParts = rightKey.split("\\.");

            if (leftParts.length == 2 && rightParts.length == 2) {
                String leftTable = leftParts[0];
                String leftCol = leftParts[1];

                String rightTableJoin = rightParts[0];
                String rightCol = rightParts[1];

                Object leftValue = leftRow.get(leftCol);
                Object rightValue = rightRow.get(rightCol);

                // ***THE FIX: Convert to integers for comparison***
                if (leftValue != null && rightValue != null) {
                    try {
                        int leftInt = Integer.parseInt(leftValue.toString());
                        int rightInt = Integer.parseInt(rightValue.toString());
                        return leftInt == rightInt;
                    } catch (NumberFormatException e) {
                        // Handle cases where values are not integers
                        return leftValue.toString().equals(rightValue.toString()); // String comparison as fallback
                    }
                } else {
                    return false;
                }
            }
        }
        return false; // Improve this for more complex conditions
    }


    private static List<Map<String, Object>> getAllRows(Class<?> cls) throws IOException {
        TableMetadata metadata = metadataCache.get(cls);
        if (metadata == null) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is not registered as a table.");
        }

        List<Map<String, Object>> allRows = new ArrayList<>();
        Path tablePath = metadata.getTablePath();

        try (java.util.stream.Stream<Path> files = Files.list(tablePath)) { // Correct try-with-resources for Files.list()
            files.forEach(filePath -> {
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                    Map<String, Object> rowData = new HashMap<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts.length == 2) {
                            String key = parts[0];
                            String value = parts[1];
                            rowData.put(key, value);
                        }
                    }
                    allRows.add(rowData); // Correctly add rowData to the list
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return allRows;
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





    public static int count(Class<?> cls, String whereClause) throws IOException {
        List<Map<String, Object>> rows = select(cls, whereClause);
        return rows.size();
    }
    public static List<Map<String, Object>> select(Class<?> cls, String whereClause) throws IOException {
        TableMetadata metadata = metadataCache.get(cls);
        if (metadata == null) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is not registered as a table.");
        }

        List<Map<String, Object>> results = new ArrayList<>();

        if (whereClause != null) {
            List<String> conditions = splitWhereClause(whereClause); // Split into individual conditions

            Files.list(metadata.getTablePath()).forEach(filePath -> {
                try {
                    Map<String, Object> rowData = Indexing.getRowData(filePath);
                    boolean match = true;
                    for (String condition : conditions) {
                        if (!evaluateCondition(rowData, condition)) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        results.add(rowData);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            // ... (No where clause, return all rows - same as before)
        }

        return results;
    }

    private static List<String> splitWhereClause(String whereClause) {
        List<String> conditions = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\w+[><=!]?\\w+)|(\\w+)\\s*(AND|OR)?\\s*"); // Improved regex
        Matcher matcher = pattern.matcher(whereClause);

        while (matcher.find()) {
            String condition = matcher.group(1);
            if (condition != null) {
                conditions.add(condition.trim());
            }
        }
        return conditions;
    }


    private static boolean evaluateCondition(Map<String, Object> rowData, String condition) {
        // Improved condition evaluation with operators
        Pattern pattern = Pattern.compile("(\\w+)\\s*([><=!]?=)\\s*(\\w+)"); // Capture column, operator, value
        Matcher matcher = pattern.matcher(condition);

        if (matcher.find()) {
            String column = matcher.group(1).trim();
            String operator = matcher.group(2).trim();
            String value = matcher.group(3).trim();

            Object rowValue = rowData.get(column);
            if(rowValue == null) return false;

            // Handle different data types and comparisons (improve this)
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
                // Handle String comparison or other data types here if needed
                 switch (operator) {
                    case "=": return rowValue.toString().equals(value);
                    case "!=": return !rowValue.toString().equals(value);
                }
            }
        }
        return false; // Default to false if condition is not in expected format
    }
}
