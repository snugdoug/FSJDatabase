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

import com.snugdoug.fsjdatabase.database.FSJDatabase;
import static com.snugdoug.fsjdatabase.database.FSJDatabase.metadataCache;
import com.snugdoug.fsjdatabase.database.createStructure.TableMetadata;

public class Indexing {
        private Map<String, Map<String, Path>> index; // Add index field
       public static void buildIndex(Class<?> cls) throws IOException {
        TableMetadata metadata = metadataCache.get(cls);
        if (metadata == null) {
            return; // No metadata, no index
        }

        Map<String, Map<String, Path>> index = new HashMap<>(); // Column -> Value -> File Path
        metadata.setIndex(index); // Store the index in TableMetadata

        Files.list(metadata.getTablePath()).forEach(filePath -> {
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

                for (Map.Entry<String, Object> entry : rowData.entrySet()) {
                    String columnName = entry.getKey();
                    String value = entry.getValue().toString(); // Convert to String for indexing

                    if (!index.containsKey(columnName)) {
                        index.put(columnName, new HashMap<>());
                    }
                    index.get(columnName).put(value, filePath);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public static List<Map<String, Object>> select(Class<?> cls, String whereClause) throws IOException {
        TableMetadata metadata = metadataCache.get(cls);
        if (metadata == null) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is not registered as a table.");
        }

        List<Map<String, Object>> results = new ArrayList<>();

        if (whereClause != null) {
            String[] parts = whereClause.split("=");
            if (parts.length == 2) {
                String columnName = parts[0].trim();
                String value = parts[1].trim();

                Map<String, Path> columnindex = metadata.getIndex().get(columnName);

                if (columnindex != null) {
                    Path filePath = columnindex.get(value);
                    if (filePath != null) {
                        results.add(getRowData(filePath));
                    }
                } else {
                    //If there is no index for the given column, then perform a linear search
                    Files.list(metadata.getTablePath()).forEach(filePath -> {
                        try {
                            Map<String, Object> rowData = getRowData(filePath);
                            if (FSJDatabase.evaluateWhereClause(rowData, whereClause)) {
                                results.add(rowData);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        } else {
            Files.list(metadata.getTablePath()).forEach(filePath -> {
                try {
                    Map<String, Object> rowData = getRowData(filePath);
                    results.add(rowData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        return results;
    }

    public static Map<String, Object> getRowData(Path filePath) throws IOException {
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
            return rowData;
        }
    }
    public void setIndex(Map<String, Map<String, Path>> index) {
        this.index = index;
    }
}
