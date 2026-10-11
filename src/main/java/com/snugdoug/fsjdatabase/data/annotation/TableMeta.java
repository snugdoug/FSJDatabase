/*
 * Copyright (c) 2026 snugdoug
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.snugdoug.fsjdatabase.data.annotation;

import com.snugdoug.fsjdatabase.annotation.FreeTable;
import com.snugdoug.fsjdatabase.annotation.Id;
import com.snugdoug.fsjdatabase.annotation.Table;
import com.snugdoug.fsjdatabase.data.FSJDatabaseStructure;
import com.snugdoug.fsjdatabase.util.Logger;
import com.snugdoug.fsjdatabase.util.clazz.SemiClass;
import com.snugdoug.fsjdatabase.util.field.SemiField;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;

public class TableMeta {
    private final SemiClass tableClass;
    private final String tableName;
    private final boolean freeTable;
    private final boolean autoIncrement;
    private final List<SemiField> columnFields;
    private final List<String> columnNames;
    private final SemiField idField;
    private final Map<String, SemiField> getByColumnNames;
    private final Map<String, List<String>> getFieldAnnotationsByName;

    private TableMeta(SemiClass tableClass,
                      String tableName,
                      boolean freeTable,
                      boolean autoIncrement,
                      List<SemiField> columnFields,
                      List<String> columnNames,
                      SemiField idField,
                      Map<String, SemiField> getByColumnNames,
                      Map<String, List<String>> getFieldAnnotationsByName) {
        this.tableClass = tableClass;
        this.tableName = tableName;
        this.freeTable = freeTable;
        this.autoIncrement = autoIncrement;
        this.columnFields = columnFields;
        this.columnNames = columnNames;
        this.idField = idField;
        this.getByColumnNames = getByColumnNames;
        this.getFieldAnnotationsByName = getFieldAnnotationsByName;
    }

    public static TableMeta of(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Table.class) && !clazz.isAnnotationPresent(FreeTable.class))
            throw new RuntimeException(String.format("%s is not annotated with @Table", clazz.getSimpleName()));

        if (clazz.isAnnotationPresent(Table.class) && clazz.isAnnotationPresent(FreeTable.class))
            throw new RuntimeException(String.format("%s is annotated with both @Table and @FreeTable", clazz.getSimpleName()));

        List<SemiField> columnFields = new ArrayList<>(List.of());

        List<String> columnNames = new ArrayList<>();
        Arrays.stream(clazz.getDeclaredFields()).forEach(((field) -> {
            columnNames.add(field.getName());
        }));

        boolean autoIncrement = true;
        SemiField idField = null;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                Id id = field.getAnnotation(Id.class);
                autoIncrement = id.autoIncrement();
                idField = SemiField.of(field.getType(), field.getName(), convertFieldAnnotationsToList(field.getAnnotations()));
            }
        }



        boolean freeTable = clazz.isAnnotationPresent(FreeTable.class);

        Map<String, SemiField> getByColumnNames = new HashMap<>();
        Arrays.stream(clazz.getDeclaredFields()).forEach((field -> {
            getByColumnNames.put(field.getName(), SemiField.of(field.getType(), field.getName(), convertFieldAnnotationsToList(field.getAnnotations())));
        }));

        Map<String, List<String>> getFieldAnnotationsByName = new HashMap<>();
        for (int i = 0; i < clazz.getDeclaredFields().length; i++) {
            columnFields.add(SemiField.of(clazz.getDeclaredFields()[i].getType(), columnNames.get(i), convertFieldAnnotationsToList(clazz.getDeclaredFields()[i].getAnnotations())));
            getFieldAnnotationsByName.put(columnNames.get(i), convertFieldAnnotationsToList(clazz.getDeclaredFields()[i].getAnnotations()));
        }

        Logger.debug("--- Table Meta (By Class): '" + clazz.getName() + "' Results ---");
        Logger.debug("tableClass: " + new SemiClass(clazz.getSimpleName()));
        Logger.debug("className: " + clazz.getSimpleName());
        Logger.debug("freeTable: " + freeTable);
        Logger.debug("autoIncrement: " + autoIncrement);
        Logger.debug("columnFields: " + columnFields);
        Logger.debug("columnNames: " + columnNames);
        Logger.debug("idField: " + idField);
        Logger.debug("getByColumnNames: " + getByColumnNames);
        Logger.debug("getFieldAnnotationsByName: " + getFieldAnnotationsByName);
        Logger.debug("--- End of Table Meta (By Class): '" + clazz.getName() + "' Results ---");

        return new TableMeta(new SemiClass(clazz.getSimpleName()), clazz.getSimpleName(), freeTable, autoIncrement, columnFields, columnNames, idField, getByColumnNames, getFieldAnnotationsByName);
    }

    public static TableMeta of(Path pathToMeta) {
        Properties props = new Properties();
        try {
            props.load(new FileReader(pathToMeta.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        boolean freeTable = Boolean.parseBoolean(props.get("freeTable").toString());
        boolean autoIncrement = Boolean.parseBoolean(props.get("autoIncrement").toString());

        String columnFieldsStr = props.get("columnFields").toString();
        List<SemiField> columnFields = new ArrayList<>();

        columnFieldsStr = columnFieldsStr.replace("[", "").replace("]", "");
        String[] descriptors = columnFieldsStr.split(", ");

        String className = "Crap the code didn't work if you see this as a table or in an exception";

        for (String descriptor :  descriptors) {
            descriptor.trim();

            String[] parts = descriptor.split(" ");

            String fullPath = parts[parts.length - 1];

            int lastDotIndex = fullPath.lastIndexOf('.');
            String fullClassName = props.getProperty("tableClass").replace("class ","");
            String fieldName = parts[1];
            className = props.getProperty("tableName");
            String classType = parts[0];


            List<String> fieldAnnotations;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(FSJDatabaseStructure.tablesDir + "/" + className + "/fields/" + fieldName + ".fsj"));
                String rawAnnotationLine = reader.readLine();
                fieldAnnotations = List.of(rawAnnotationLine.split(", "));
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to find the field directory\n" + e);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read the Annotation data file\n" + e);
            }
            columnFields.add(new SemiField(classType, fieldName, fieldAnnotations));
            Logger.debug("Finished reading fields annotations: " + fieldAnnotations);


        }
        List<String> columnName = Arrays.stream(props.get("columnNames").toString().replace("[", "").replace("]", "").split(", ")).toList();
        Map<String, SemiField> getByColumnNames = new HashMap<>();

        for (int i = 0; i < columnName.toArray().length; i++) {
            getByColumnNames.put(columnName.get(i), columnFields.get(i));
        }

        Map<String, List<String>> getFieldAnnotationsByName = new HashMap<>();

        for (int i = 0; i < columnName.size(); i++) {
            SemiField field = columnFields.get(i);
            try {
                BufferedReader reader = new BufferedReader(new FileReader(FSJDatabaseStructure.tablesDir + "/" + className + "/fields/" + field.getName() + ".fsj"));
                getFieldAnnotationsByName.put(columnName.get(i), Arrays.stream(reader.readLine().split(", ")).toList());
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to create buffered reader for reading the annotation file\n" + e);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read annotation file\n" + e);
            }
        }

        SemiField idField = null;
        String[] idFieldRaw = props.getProperty("idField").split(" ");


        try {
            BufferedReader reader = new BufferedReader(new FileReader(FSJDatabaseStructure.tablesDir + "/" + className + "/fields/" + idFieldRaw[1] + ".fsj"));
            String[] rawAnnotations = reader.readLine().split(", ");
            idField = new SemiField(idFieldRaw[0], idFieldRaw[1], Arrays.stream(rawAnnotations).toList());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed to create buffered reader for annotations\n" + e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read annotations file\n" + e);
        }

        Logger.debug("--- Table Meta (By Path): '" + className + "' Results ---");
        Logger.debug("tableClass: " + new SemiClass(className));
        Logger.debug("className: " + className);
        Logger.debug("freeTable: " + freeTable);
        Logger.debug("autoIncrement: " + autoIncrement);
        Logger.debug("columnFields: " + columnFields);
        Logger.debug("columnNames: " + columnName);
        Logger.debug("idField: " + idField);
        Logger.debug("getByColumnNames: " + getByColumnNames);
        Logger.debug("getFieldAnnotationsByName: " + getFieldAnnotationsByName);
        Logger.debug("--- End of Table Meta (By Path): '" + className + "' Results ---");



        return new TableMeta(new SemiClass(className), className, freeTable, autoIncrement, columnFields, columnName, idField, getByColumnNames, getFieldAnnotationsByName);
    }

    public SemiClass getTableClass() {
        return tableClass;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isFreeTable() {
        return freeTable;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public List<SemiField> getColumnFields() {
        return columnFields;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public SemiField getIdField() {
        return idField;
    }

    public Map<String, SemiField> getGetByColumnNames() {
        return getByColumnNames;
    }

    public Map<String, List<String>> getGetFieldAnnotationsByName() {
        return getFieldAnnotationsByName;
    }



    @Override
    public String toString() {
        return "TableMeta{" +
                "tableClass=" + tableClass +
                ", tableName='" + tableName + '\'' +
                ", freeTable=" + freeTable +
                ", autoIncrement=" + autoIncrement +
                ", columnFields=" + columnFields +
                ", columnNames=" + columnNames +
                ", idField=" + idField +
                ", getByColumnNames=" + getByColumnNames +
                ", getFieldAnnotationsByName=" + getFieldAnnotationsByName +
                '}';
    }

    /**
     * Converts all the values to a usable list
     *
     * @return returns a list of all values
     */
    public List<Object> toList() {
        return List.of(tableClass, tableName, freeTable, autoIncrement, columnFields, columnNames, idField, getByColumnNames, getFieldAnnotationsByName);
    }


    /**
     *
     * Converts all the values to a usable map
     *
     * @return returns map with all values
     */
    public HashMap<String, Object> toMap() {
        HashMap<String, Object> m = new LinkedHashMap<>();

        m.put("tableClass", tableClass);
        m.put("tableName", tableName);
        m.put("freeTable", freeTable);
        m.put("autoIncrement", autoIncrement);
        m.put("columnFields", columnFields);
        m.put("columnNames", columnNames);
        m.put("idField", idField);
        m.put("getByColumnNames", getByColumnNames);
        // getFieldAnnotations would be here but for my formatting I am putting it separate
        // If you need it use the getGetFieldAnnotationsByName method

        return m;
    }

    private static List<String> convertFieldAnnotationsToList(Annotation[] annotations) {
        List<String> result = new ArrayList<>(List.of());

        Arrays.stream(annotations).forEach((annotation -> {
            result.add(annotation.toString());
        }));

        return result;
    }


}
