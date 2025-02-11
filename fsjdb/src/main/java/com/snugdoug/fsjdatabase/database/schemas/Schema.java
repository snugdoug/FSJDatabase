package com.snugdoug.fsjdatabase.database.schemas;

import java.util.LinkedHashMap;
import java.util.Map;

public class Schema {
    private final Map<String, Class<?>> fields = new LinkedHashMap<>();

    public Schema addField(String fieldName, Class<?> type) {
        fields.put(fieldName, type);
        return this;
    }

    public boolean validate(Map<String, Object> record) {
        for (Map.Entry<String, Class<?>> entry : fields.entrySet()) {
            String key = entry.getKey();
            Class<?> expectedType = entry.getValue();

            if (!record.containsKey(key)) {
                throw new IllegalArgumentException("Missing field: " + key);
            }
            Object value = record.get(key);
            if (!expectedType.isInstance(value)) {
                throw new IllegalArgumentException("Invalid type for " + key + ": Expected " + expectedType.getSimpleName());
            }
        }
        return true;
    }

    public Map<String, Class<?>> getFields() {
        return fields;
    }
}
