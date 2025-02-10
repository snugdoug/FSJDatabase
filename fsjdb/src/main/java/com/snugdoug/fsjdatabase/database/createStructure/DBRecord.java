package com.snugdoug.fsjdatabase.database.createStructure;

import java.util.HashMap;
import java.util.Map;

public class DBRecord {
    private final Map<String, Object> fields = new HashMap<>();

    public DBRecord set(String key, Object value) {
        fields.put(key, value);
        return this;
    }

    public Object get(String key) {
        return fields.get(key);
    }

    public Map<String, Object> getFields() {
        return new HashMap<>(fields); // Return a copy for safety
    }

    @Override
    public String toString() {
        return fields.toString();
    }
}
