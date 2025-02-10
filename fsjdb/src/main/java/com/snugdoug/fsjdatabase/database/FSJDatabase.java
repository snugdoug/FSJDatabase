package com.snugdoug.fsjdatabase.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snugdoug.fsjdatabase.database.createStructure.Column;
import com.snugdoug.fsjdatabase.database.createStructure.Table;
import com.snugdoug.fsjdatabase.database.createStructure.TableMetadata;
import com.snugdoug.fsjdatabase.database.query.Indexing;

public class FSJDatabase {

    public static final Map<Class<?>, TableMetadata> metadataCache = new HashMap<>();

    public static void start(Class<?>... annotatedClasses) throws IOException {
        for (Class<?> cls : annotatedClasses) {
            processClass(cls);
        }
    }

    private static void processClass(Class<?> cls) throws IOException {
        if (!cls.isAnnotationPresent(DataSource.class)) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is not annotated with @DataSource");
        }

        DataSource dataSource = cls.getAnnotation(DataSource.class);
        String rootPath = dataSource.rootPath();

        Path root = Paths.get(rootPath);
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }

        if (cls.isAnnotationPresent(Table.class)) {
            Table table = cls.getAnnotation(Table.class);
            String tableName = table.name();
            Path tablePath = root.resolve(tableName);
            if (!Files.exists(tablePath)) {
                Files.createDirectories(tablePath);
            }

            TableMetadata tableMetadata = new TableMetadata(tableName, tablePath);

            for (Field field : cls.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    String columnName = column.name();
                    boolean isPrimaryKey = column.primaryKey();
                    tableMetadata.addColumn(columnName, isPrimaryKey);
                }
            }

            metadataCache.put(cls, tableMetadata); // Cache AFTER adding columns

            Indexing.buildIndex(cls);
        }
    }

    public static List<Map<String, Object>> select(Class<?> cls, String whereClause) throws IOException {
        TableMetadata metadata = metadataCache.get(cls);
        if (metadata == null) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is not registered as a table.");
        }

        List<Map<String, Object>> results = new ArrayList<>();
        Path tablePath = metadata.getTablePath();

        // Iterate through files (representing rows) in the table directory
        Files.list(tablePath).forEach(filePath -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                Map<String, Object> rowData = new HashMap<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String key = parts[0];
                        String value = parts[1]; // You'll need to handle data types later
                        rowData.put(key, value);
                    }
                }

                // Basic where clause evaluation (improve this later)
                if (whereClause == null || evaluateWhereClause(rowData, whereClause)) {
                    results.add(rowData);
                }

            } catch (IOException e) {
                // Handle exceptions appropriately

            }
        });

        return results;
    }

    // Basic where clause evaluation (expand as needed)
    public static boolean evaluateWhereClause(Map<String, Object> rowData, String whereClause) {
        if (whereClause == null) {
            return true; // No where clause, include all rows
        }
        // Very basic example (improve this to handle more complex conditions)
        String[] parts = whereClause.split("=");
        if (parts.length == 2) {
            String columnName = parts[0].trim();
            String expectedValue = parts[1].trim();
            Object actualValue = rowData.get(columnName);
            return expectedValue.equals(actualValue); // Handle data types and operators
        }
        return false; // Default to false if where clause is not in simple "key=value" form
    }

    public static void insert(Class<?> cls, Map<String, Object> data) throws IOException {
        TableMetadata metadata = metadataCache.get(cls); // Now it should work!
        if (metadata == null) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is not registered as a table.");
        }

        Path tablePath = metadata.getTablePath();
        String fileName = generateFileName(data, metadata);

        Path filePath = tablePath.resolve(fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        }
    }

    private static String generateFileName(Map<String, Object> data, TableMetadata metadata) {
        String primaryKeyColumn = null;
        for (Map.Entry<String, Boolean> entry : metadata.getColumns().entrySet()) {
            if (entry.getValue()) {
                primaryKeyColumn = entry.getKey();
                break;
            }
        }

        if (primaryKeyColumn != null) {
            Object primaryKeyValue = data.get(primaryKeyColumn);
            if (primaryKeyValue != null) {
                return primaryKeyValue.toString() + ".txt";
            }
        }
        return UUID.randomUUID().toString() + ".txt";
    }

    public static void update(Class<?> cls, Map<String, Object> data, String whereClause) throws IOException {
        TableMetadata metadata = metadataCache.get(cls);
        if (metadata == null) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is not registered as a table.");
        }

        Path tablePath = metadata.getTablePath();

        Files.list(tablePath).forEach(filePath -> {
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

                if (evaluateWhereClause(rowData, whereClause)) {
                    // Update the data
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        rowData.put(entry.getKey(), entry.getValue());
                    }

                    // Write the updated data back to the file (overwrite)
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
                        for (Map.Entry<String, Object> entry : rowData.entrySet()) {
                            writer.write(entry.getKey() + ":" + entry.getValue());
                            writer.newLine();
                        }
                    }
                }

            } catch (IOException e) {
            }
        });
    }

    public static void delete(Class<?> cls, String whereClause) throws IOException {
        TableMetadata metadata = metadataCache.get(cls);
        if (metadata == null) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is not registered as a table.");
        }

        Path tablePath = metadata.getTablePath();

        Files.list(tablePath).forEach(filePath -> {
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

                if (evaluateWhereClause(rowData, whereClause)) {
                    // Secure Delete:  Make sure we're only deleting files within the table directory
                    if (!filePath.startsWith(tablePath)) {  // Double-check
                        throw new SecurityException("Unauthorized delete operation.");
                    }
                    Files.delete(filePath);
                }

            } catch (IOException e) {
            }
        });
    }

    public static void importFromCSV(Class<?> cls, String filePath) throws IOException {
        try (CSVParser parser = new CSVParser(new FileReader(filePath), CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) { // Correct way
            Map<String, Integer> headerMap = parser.getHeaderMap(); // Get header map

            for (CSVRecord record : parser) {
                Map<String, Object> data = new HashMap<>();
                for (Entry<String, Integer> entry : headerMap.entrySet()) {
                    String headerName = entry.getKey();
                    data.put(headerName, record.get(headerName)); // Use header names
                }
                insert(cls, data);
            }
        }
    }

    public static void exportToJson(List<Map<String, Object>> data, String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(filePath), data);
    }

}
