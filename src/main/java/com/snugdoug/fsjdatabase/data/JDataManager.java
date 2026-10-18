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

package com.snugdoug.fsjdatabase.data;

import com.snugdoug.fsjdatabase.data.annotation.TableMeta;
import com.snugdoug.fsjdatabase.data.json.FSJJsonReader;
import com.snugdoug.fsjdatabase.data.json.FSJJsonWriter;
import com.snugdoug.fsjdatabase.exception.TableNotFoundException;
import com.snugdoug.fsjdatabase.util.Logger;
import com.snugdoug.fsjdatabase.util.clazz.SemiClass;
import com.snugdoug.fsjdatabase.util.field.SemiField;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;


/**
 * Handles everything related to data managing
 */
@SuppressWarnings("rawtypes")
public class JDataManager {

    private static boolean isFSJDatabaseInitialized;
    private static List<SemiClass> autoIncrement;


    /**
     *<p>This method is called to tell {@link #addToTable(SemiClass, LinkedHashMap)} and other methods whether {@code FSJDatabase} is initialized.
     *<p>This is needed because {@link FSJDatabaseInitializer} initializes the tables and most methods (planned or implemented)
     *will need table access
     *@param autoIncrement is changed by {@link FSJDatabaseInitializer} to tell whether the user wants auto increment or not.
     *
     */
    protected static void jDataManagerInit(List<SemiClass> autoIncrement) {
        isFSJDatabaseInitialized = true;
        JDataManager.autoIncrement = autoIncrement;
    }

    /**
     * Adds data to a given table using {@link LinkedHashMap} to preserve insertion order.
     *
     *
     * @param dataToAdd the data to be added to the table
     * @param table the table used to add the data
     * @throws IOException from {@code BufferedWriter/BufferedReader} for {@code auto incrementing}, the reason instead of using a {@code try-catch} is so users can make their own way of handling it
     */

    public boolean addToTable(SemiClass table, LinkedHashMap<Object, Object> dataToAdd) throws IOException, RuntimeException {


        TableMeta meta = TableMeta.of(Path.of(FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/meta"));

        // check for flags of invalid data/inproper initialization
        if(!isFSJDatabaseInitialized)
            throw new RuntimeException("FSJDatabase is not initialized");

        if(!FSJDatabaseInitializer.tables.contains(table) && !FSJDatabaseInitializer.freeTables.contains(table))
            throw new TableNotFoundException(table);

        if(meta.isAutoIncrement() && dataToAdd.containsKey(getTableIdName(table))) {
            Logger.error("Failed to add data, extra stacktrace:");
            Logger.error("AutoIncrement List: " + autoIncrement);
            Logger.error("DataToAdd: " + dataToAdd);
            Logger.error("Table: " + table);
//            Logger.error("Manual auto increment extraction with TableMeta: " + TableMeta.of(Path.of(FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/meta")).isAutoIncrement());
            Logger.error("Id field cannot be in data to add when using auto incrementing");
        }

        LinkedHashSet<String> tableColumns = new LinkedHashSet<>(getTableColumns(table, autoIncrement.contains(table)));

        if(!dataToAdd.keySet().equals(tableColumns)) {
            if(!FSJDatabaseInitializer.freeTables.contains(table)) {
                Logger.error("dataToAdd's keys does not match the tables columns with data set: \n" + dataToAdd);
                return false;
            }
        }

        return writeToDatabase(dataToAdd, table, meta.isAutoIncrement());
    }
    public boolean addToTable(Class table, LinkedHashMap<Object, Object> dataToAdd) throws IOException, RuntimeException {
        Logger.debug("--- Attempting to write data ---");
        Logger.debug("Table: " + table.getSimpleName());
        Logger.debug("Uses autoIncrementing: " + autoIncrement.contains(SemiClass.nameOf(table.getSimpleName())));
        Logger.debug("DataToAdd:" + dataToAdd);

        Logger.debug("AutoIncrementing list: " + autoIncrement);
        return addToTable(new SemiClass(table), dataToAdd);

    }

        /**
         * Deletes data from a {@code table} by id
         *
         * @param table the table to delete from
         * @param id the id to search for
         */
    public boolean deleteById(SemiClass table, int id) {
        Path targetFileLocation = Path.of(FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/" + id + ".json");
        targetFileLocation = targetFileLocation.toAbsolutePath();

        File targetFile = new File(targetFileLocation.toUri());

        if(targetFile.exists()) {
            FileManager.deleteFsjdbFile(targetFileLocation.toString());
        }
        return true;
    }

    public boolean deleteById(Class table, int id) {
        return deleteById(new SemiClass(table), id);
    }

    /**
     * Similar to {@link #deleteById(SemiClass, int)}, but instead of deleting a {@code single file} it deletes from an {@code id}
     * to another {@code id} in a table
     *
     * @param table the table to delete from
     * @param fromId where to start deleting data
     * @param toId where to end deleting data
     */
    public boolean deleteByFromIdToId(SemiClass table, int fromId, int toId) {

        for(int id = fromId; id <= toId; id++) {
            Path targetFileLocation = Path.of(FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/" + id + ".json");
            targetFileLocation = targetFileLocation.toAbsolutePath();

            File targetFile = new File(targetFileLocation.toUri());

            if (targetFile.exists()) {
                FileManager.deleteFsjdbFile(targetFileLocation.toString());
            }
        }
        return true;
    }

    public boolean deleteByFromIdToId(Class table, int fromId, int toId) {
        return deleteByFromIdToId(new SemiClass(table), fromId, toId);
    }

    /**
     * This method is not similar to the other delete methods<br>
     * What this does is it first finds all files in the tables directory, then it searches through the<br>
     * contents of each.
     * <br><br>
     *If it finds something that matches the value to the key given, it deletes it.
     *
     *
     * @param table the table to delete from
     * @param key the key to check
     * @param value the value to check
     */
    public boolean deleteContaining(SemiClass table, Object key, Object value) {
        String tableName = table.getSimpleName();

        String targetDir = FSJDatabaseStructure.tablesDir + "/" + tableName;

        try {
            try(Stream<Path> paths = Files.walk(Paths.get(targetDir));) {
                paths
                        .filter(Files::isRegularFile).
                        forEach(file -> {
                            if (file.getFileName().toString().contains(".json")) {
                                int id = Integer.parseInt(file.getFileName().toString().replace(".json", ""));
                                LinkedHashMap<Object, Object> map = FSJJsonReader.read(id, table);

                                if (map.entrySet().contains(Map.entry(key, value))) {
                                    FileManager.deleteFsjdbFile(String.valueOf(file));
                                }
                            }

                        });
            }
        } catch (IOException e) {
            Logger.error("Failed to walk through files in '" + targetDir + "'\n" + e);
            return false;
        }
        return true;
    }

    public boolean deleteContaining(Class table, Object key, Object value) {
        return deleteContaining(new SemiClass(table), key, value);
    }


    /**
     * Updates a {@code specific keys} value in a data file found by {@code id}.
     *
     * @param table the table to find the data with
     * @param key the key to change the value of in the file
     * @param value the updated value
     * @param id the id to find the data
     *
     */
    public boolean updateValueById(SemiClass table, int id, Object key, Object value) {
        String tableName = table.getSimpleName();
        Path targetFileLocation = Path.of(FSJDatabaseStructure.tablesDir + "/" + tableName + "/" + id + ".json");

        List<String> fileContent = new ArrayList<>();
        targetFileLocation = targetFileLocation.toAbsolutePath();

        try {
            try(Reader fileReader = new FileReader(targetFileLocation.toFile());) {
                LinkedHashMap<Object, Object> map = FSJJsonReader.read(id, table);
                map.remove(key.toString());
                map.put(key.toString(), value.toString());
                FSJJsonWriter.write(map, table, id, false);
            }
        } catch (IOException e) {
            Logger.error("Failed to read/write to file: " + targetFileLocation + "\n" + e);
            return false;
        }
        return true;
    }

    public boolean updateValueById(Class table, int id, String key, String value) {
        return updateValueById(new SemiClass(table), id, key, value);
    }

    /**
     * Replaces a value ({@code firstName=*value*}) in a data file found by if it contains something with a {@code new value}.
     *
     * @param table the table to search the data file
     * @param key the key to change the value of in the file
     * @param value the value to search for after the key
     * @param newValue the values to replace the previous value with
     */
    public boolean updateValueContaining(SemiClass table, Object key, Object value, Object newValue) {
        String tableName = table.getSimpleName();
        String targetDir =  FSJDatabaseStructure.tablesDir + "/" + tableName;

        try {
            try(Stream<Path> paths = Files.walk(Paths.get(targetDir))) {
                paths
                        .filter(Files::isRegularFile).
                        forEach(file -> {
                            if (file.getFileName().toString().contains(".json")) {
                                try (FileReader fileReader = new FileReader(file.toFile().getPath())) {

                                    int id = Integer.parseInt(file.getFileName().toString().replace(".json", ""));
                                    LinkedHashMap<Object, Object> map = FSJJsonReader.read(id, table);

                                    if (map.entrySet().contains(Map.entry(key, value))) {
                                        map.remove(key);
                                        map.put(key, newValue);

                                        FSJJsonWriter.write(map, table, id, false);
                                    }

                                } catch (IOException e) {
                                    Logger.error("Failed to successfully check file: " + file);
                                }
                            }

                        });
            }
        } catch (IOException e) {
            Logger.error("Failed to walk through the files in the folder\n" + e);
            return false;
        }
        return true;
    }

    public boolean updateValueContaining(Class table, Object key, Object value, Object newValue) {
        return updateValueContaining(new SemiClass(table), key, value, newValue);
    }


        /**
         * Updates a data file by id (id should be in data (2nd argument))
         *
         * @param table the table to search for the data file
         * @param data data to add
         * @throws RuntimeException throws from invalid data
         */
    public boolean updateById(SemiClass table, LinkedHashMap<Object, Object> data) throws RuntimeException {

        String idName = getTableIdName(table);

        if(!data.containsKey(getTableIdName(table))) {
            Logger.error("Data does not contain id field! Dataset: \n" + data);
            return false;
        }

        int id = Integer.parseInt(String.valueOf(data.get(getTableIdName(table))));

        FSJJsonWriter.write(data, table, id, false);
        return true;
    }

    public boolean updateById(Class table, LinkedHashMap<Object, Object> data) throws RuntimeException {
        return updateById(new SemiClass(table), data);
    }

    /**
     * Updates a specific data file by looking for a file containing the searchData, it is sorted so no matter what you put it in it will properly sort it.
     *
     * @param table the table to change data with
     * @param searchData the search term for finding what to replace
     * @param data the replacement data after the data matching searchData is found
     * @throws RuntimeException throws RuntimeException mostly from invalid data
     */
    public boolean updateContaining(SemiClass table, LinkedHashMap<Object, Object> searchData,  LinkedHashMap<Object, Object> data) throws RuntimeException {
        String tableName = table.getSimpleName();
        String targetDir =  FSJDatabaseStructure.tablesDir + "/" + tableName;

        if(data.containsKey(getTableIdName(table)) &&
                TableMeta.of(Path.of(FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/" + "meta")).isAutoIncrement()) {
            Logger.error("Data needs to not contain id field for proper identification!");
            return false;
        }

        try {
            try(Stream<Path> paths = Files.walk(Paths.get(targetDir))) {
                paths
                        .filter(Files::isRegularFile).
                        forEach(file -> {
                            if (file.getFileName().toString().contains(".json")) {
                                int id = Integer.parseInt(file.getFileName().toString().replace(".json", ""));
                                LinkedHashMap<Object, Object> map = FSJJsonReader.read(id, table);

                                if (searchData.equals(map)) {
                                    FSJJsonWriter.write(data, table, id, false);
                                }
                            }

                        });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to walk through the files in '" + targetDir + "'\n" + e);
        }
        return true;
    }

    public boolean updateContaining(Class table, LinkedHashMap<Object, Object> searchData,  LinkedHashMap<Object, Object> data) throws RuntimeException {
        return updateContaining(new SemiClass(table), searchData, data);
    }

    /**
     * Gets data from a data file by id
     *
     * @param table the table to find the data file of
     * @param id the id of the data file to find
     * @return returns a {@link LinkedHashMap} of the data files data
     */
    public LinkedHashMap<Object, Object> findById(SemiClass table, int id) {
        return FSJJsonReader.read(id, table);
    }

    public LinkedHashMap<Object, Object> findById(Class table, int id) {
        return findById(new SemiClass(table), id);
    }

    /**
     * Looks everywhere in a table matching the data provided<br>
     * @param table The table to be used
     * @param key the key of the value to check
     * @param value the value to look for
     * @return returns found matches
     */
    public List<LinkedHashMap<Object, Object>> findByValueContaining(SemiClass table, String key, String value) {
        String tableName = table.getSimpleName();
        List<LinkedHashMap<Object, Object>> finalContent = new ArrayList<>();
        String targetDir = FSJDatabaseStructure.tablesDir + "/" + tableName;

        try {
            try(Stream<Path> paths = Files.walk(Paths.get(targetDir))) {
                paths
                        .filter(Files::isRegularFile).
                        forEach(file -> {
                            if (file.getFileName().toString().contains(".json")) {
                                int id = Integer.parseInt(file.getFileName().toString().replace(".json", ""));

                                LinkedHashMap<Object, Object> map = FSJJsonReader.read(id, table);

                                if (map.entrySet().contains(Map.entry(key, value))) {
                                    finalContent.add(map);
                                }
                            }

                        });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to walk through files in '" + targetDir + "'\n" + e);
        }
        return finalContent;
    }

    public List<LinkedHashMap<Object, Object>> findByValueContaining(Class table, String key, String value) {
        return findByValueContaining(new SemiClass(table), key, value);
    }

    public LinkedList<Map.Entry<Object, Object>> findAllValuesOfKey(SemiClass table, String key) {
        String tableName = table.getSimpleName();
        LinkedList<Map.Entry<Object, Object>> finalContent = new LinkedList<>();
        String targetDir = FSJDatabaseStructure.tablesDir + "/" + tableName;

        try {
            try(Stream<Path> paths = Files.walk(Paths.get(targetDir))) {
                paths
                        .filter(Files::isRegularFile).
                        forEach(file -> {
                            if (file.getFileName().toString().contains(".json")) {
                                int id = Integer.parseInt(file.getFileName().toString().replace(".json", ""));
                                LinkedHashMap<Object, Object> map = FSJJsonReader.read(id, table);

                                if (map.containsKey(key)) {
                                    finalContent.add(Map.entry(id, map.get(key)));
                                }
                            }

                        });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to walk through files in '" + targetDir + "'\n" + e);
        }
        return finalContent;
    }

    public LinkedList<Map.Entry<Object, Object>> findAllValuesOfKey(Class table, String key) {
        return findAllValuesOfKey(new SemiClass(table), key);
    }

    /**
     * This method handles writing things to the database, the reason this method exists is so the<br>
     * {@link #addToTable(SemiClass, LinkedHashMap)} isn't way longer than it should be.
     *
     * @param dataToAdd the data to add
     * @param table the table to add the data to
     * @param autoIncrement if the table uses auto incrementing
     * @throws IOException gives IOException from BufferedWriter, why its thrown is explained in the method {@link #addToTable(SemiClass, LinkedHashMap)}
     */
    private boolean writeToDatabase(LinkedHashMap<?, ?> dataToAdd, SemiClass table, boolean autoIncrement) throws IOException {
        if(autoIncrement) {
            int id = JAutoIncrement.get(table);

            String targetFileLocation = FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/" + id + ".json";
            FileManager.createFile(targetFileLocation);
            FSJJsonWriter.write(dataToAdd, table, id, true);

        }

        // logic for adding data to table without auto increment
        else {
            int id;
            try {
                id = Integer.parseInt(dataToAdd.get(getTableIdName(table)).toString());
            } catch (NullPointerException e) {
                Logger.error("Failed to find " + dataToAdd.get(getTableIdName(table) + " in dataToAdd, or failed to parse id."));
                return false;
            }

            String targetFileLocation = FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/" + id + ".json";
            FileManager.createFile(targetFileLocation);
            FSJJsonWriter.write(dataToAdd, table, id, false);

        }
        return true;
    }

    /**
     * Gets the table columns of a class, this is needed to detect if a data map given is invalid to the columns in {@link #addToTable(SemiClass, LinkedHashMap)}.
     *
     * @param table the table to get the columns of
     * @param autoIncrement when not using auto incrementing it will add the @id annotation name to match the given map data if it's not already invalid
     * @return returns a set of columns
     */
    public Set<String> getTableColumns(SemiClass table, boolean autoIncrement) {
        Set<String> columns;
        TableMeta tableMeta = TableMeta.of(Path.of(FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/meta"));

        columns = new LinkedHashSet<>(tableMeta.getColumnNames());
        if(autoIncrement)
            columns.remove(getTableIdName(table));
        return columns;
    }

    /**
     * Gets a tables id field name
     *
     * @param table the table to get the id field of
     * @return returns the id field name
     * @throws RuntimeException throws from invalid tables (table without id field)
     *
     */
    public String getTableIdName(SemiClass table) throws RuntimeException {
        String id = "";

        Path pathToMeta = Path.of(FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/meta");
        TableMeta meta = TableMeta.of(pathToMeta);

        for(SemiField field : meta.getColumnFields()) {
            Logger.debug("In table '" + table.getSimpleName() + "' found field present: " + field);
            if(field.getName().equals(meta.getIdField().getName())) {
                Logger.debug("--- Found ID Name: " + field.getName() + " ---");
                id = field.getName();
            }
        }
        return id;
    }

    public List<SemiClass> getAutoIncrement() {
        return autoIncrement;
    }
}
