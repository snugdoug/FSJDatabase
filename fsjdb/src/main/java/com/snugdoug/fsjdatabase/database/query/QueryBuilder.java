package com.snugdoug.fsjdatabase.database.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.snugdoug.fsjdatabase.database.FSJDatabase;

public class QueryBuilder {
    private final FSJDatabase database;
    private final List<Predicate<Map<String, Object>>> filters = new ArrayList<>();

    public QueryBuilder(FSJDatabase database) {
        this.database = database;
    }

    public QueryBuilder where(String field, String operator, Object value) {
        filters.add(record -> {
            Object fieldValue = record.get(field);
            if (fieldValue == null) return false;

            switch (operator) {
                case "=": return fieldValue.equals(value);
                case "!=": return !fieldValue.equals(value);
                case ">": return fieldValue instanceof Comparable && ((Comparable) fieldValue).compareTo(value) > 0;
                case "<": return fieldValue instanceof Comparable && ((Comparable) fieldValue).compareTo(value) < 0;
                case "LIKE": return fieldValue.toString().matches(value.toString().replace("%", ".*"));
                default: throw new IllegalArgumentException("Unsupported operator: " + operator);
            }
        });
        return this;
    }

    public List<Map<String, Object>> execute() {
        return database.findAll().stream()
            .filter(record -> filters.stream().allMatch(filter -> filter.test(record)))
            .collect(Collectors.toList());
    }
}
