package com.snugdoug.fsjdb.database.createStructure;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TableMetadata {
    private String tableName;
    private Path tablePath;
    private Map<String, Boolean> columns = new HashMap<>();
    private Map<String, Map<String, Path>> index = new HashMap<>(); // Add index field here

    public TableMetadata(String tableName, Path tablePath) {
        this.tableName = tableName;
        this.tablePath = tablePath;
    }

    public String getTableName() {
        return tableName;
    }

    public Path getTablePath() {
        return tablePath;
    }

    public void addColumn(String columnName, boolean isPrimaryKey) {
        columns.put(columnName, isPrimaryKey);
    }

    public Map<String, Boolean> getColumns() {
        return columns;
    }

    public void setIndex(Map<String, Map<String, Path>> index) { // Add setter
        this.index = index;
    }

    public Map<String, Map<String, Path>> getIndex() { // Add getter
        return index;
    }
}