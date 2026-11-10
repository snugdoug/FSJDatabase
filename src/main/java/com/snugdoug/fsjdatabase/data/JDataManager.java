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
import com.snugdoug.fsjdatabase.exception.logError.InvalidDataKeySetLogError;
import com.snugdoug.fsjdatabase.exception.logError.NumberFormatLogError;
import com.snugdoug.fsjdatabase.exception.logError.RecordNotFoundLogError;
import com.snugdoug.fsjdatabase.util.GetTypeOf;
import com.snugdoug.fsjdatabase.util.Logger;
import com.snugdoug.fsjdatabase.util.clazz.SemiClass;
import com.snugdoug.fsjdatabase.util.field.SemiField;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;


/**
 * <h2>JDataManager (JDM)</h2>
 * Manages the database, with many CRUD (Create Read Update Delete) methods.<br>
 * Near all methods use {@link SemiClass}, but the methods are overloaded to accept {@link Class}'s, keep that in mind!
 * <h2>Methods</h2>
 * <h3>Create</h3>
 * {@link #addToTable(SemiClass, LinkedHashMap)} - Adds data to a specified table
 * <h3>Read</h3>
 * {@link #findById(SemiClass, int)} - Finds data in a table by id<br>
 * {@link #findByValueContaining(SemiClass, Object, Object)} - Finds data in a table by a value containing<br>
 * {@link #findAllValuesOfKey(SemiClass, String)} - Gets all values of a key from a table<br>
 * <h3>Update</h3>
 * {@link #updateById(SemiClass, LinkedHashMap)} - Updates an entire record in the specified table<br>
 * {@link #updateValueById(SemiClass, int, Object, Object)} - Updates a value in a record by id inside the specified table<br>
 * {@link #updateContaining(SemiClass, LinkedHashMap, LinkedHashMap)} - Updates a record inside a table if a record matches the search data<br>
 * {@link #updateValueContaining(SemiClass, Object, Object, Object)} - Searches a table for any occurrences of the key value pair if a match, it replaces the value with the new one
 * <h3>Delete</h3>
 * {@link #deleteById(SemiClass, int)} - Deletes a record inside a table by id<br>
 * {@link #deleteByFromIdToId(SemiClass, int, int)} - Deletes all records in a table from an id to another id<br>
 * {@link #deleteContaining(SemiClass, Object, Object)} - Searches all the tables records for a record that matches the search term, if found it found a match, it deletes it
 *
 */
@SuppressWarnings("rawtypes")
public class JDataManager {

    /**
     * Create a new instance<br>
     * Before using anything in here, make sure FSJDatabase is initialized.
     */
    public JDataManager() {

    }

    /**
     * The autoIncrement tables
     */
    private static List<SemiClass> autoIncrement;


    /**
     *<p>This method is called to tell {@link #addToTable(SemiClass, LinkedHashMap)} and other methods whether {@code FSJDatabase} is initialized.
     *<p>This is needed because {@link FSJDatabaseInitializer} initializes the tables and most methods (planned or implemented)
     *will need table access
     *@param autoIncrement is changed by {@link FSJDatabaseInitializer} to tell whether the user wants auto increment or not.
     *
     */
    protected static void jDataManagerInit(List<SemiClass> autoIncrement) {
        JDataManager.autoIncrement = autoIncrement;
    }

    /**
     * Adds data to a given table using {@link LinkedHashMap} to preserve insertion order.<br><br>
     *
     * Throws<br>
     * {@link InvalidDataKeySetLogError}<br>
     * {@link TableNotFoundException}<br>
     * When the id was not in data {@code (No Auto Incrementing)}<br>
     * The id field is in data {@code (Auto Incrementing)}
     *
     * @param dataToAdd the data to be added to the table
     * @param table the table used to add the data
     * @return if it was successful or not
     */

    public boolean addToTable(SemiClass table, LinkedHashMap<Object, Object> dataToAdd) {

        // check for flags of invalid data/inproper initialization

        Logger.debug("Adding data, tables: " + FSJDatabaseInitializer.tables);
        Logger.debug("Adding data, free tables: " + FSJDatabaseInitializer.freeTables);

        Logger.debug(FSJDatabaseInitializer.getTables().contains(table));
        Logger.debug("Table: " + table);

        if(!FSJDatabaseInitializer.tables.contains(table) && !FSJDatabaseInitializer.freeTables.contains(table))
            throw new TableNotFoundException(table);

        TableMeta meta = TableMeta.of(Path.of(FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/meta"));



        if(meta.isAutoIncrement() && dataToAdd.containsKey(getTableIdName(table))) {
            Logger.error("Failed to add data, extra stacktrace:");
            Logger.error("AutoIncrement List: " + autoIncrement);
            Logger.error("DataToAdd: " + dataToAdd);
            Logger.error("Table: " + table);
//            Logger.error("Manual auto increment extraction with TableMeta: " + TableMeta.of(Path.of(FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/meta")).isAutoIncrement());
            Logger.error("Id field cannot be in data to add when using auto incrementing");
            return false;
        }

        LinkedHashSet<String> tableColumns = new LinkedHashSet<>(getTableColumns(table, autoIncrement.contains(table)));

        if(!dataToAdd.keySet().equals(tableColumns)) {
            if(!FSJDatabaseInitializer.freeTables.contains(table)) {
                Logger.error(new InvalidDataKeySetLogError(dataToAdd, getTableColumns(table, TableMeta.of(table).isAutoIncrement())));
                return false;
            }
        }
        return writeToDatabase(dataToAdd, table, meta.isAutoIncrement());
    }

    /**
     * Adds data to a given table using {@link LinkedHashMap} to preserve insertion order.<br><br>
     * Throws<br>
     * {@link InvalidDataKeySetLogError}<br>
     * {@link TableNotFoundException}<br>
     * When the id was not in data {@code (No Auto Incrementing)}<br>
     * The id field is in data {@code (Auto Incrementing)}
     *
     * @param dataToAdd the data to be added to the table
     * @param table the table used to add the data
     * @return if it was successful or not
     */
    public boolean addToTable(Class table, LinkedHashMap<Object, Object> dataToAdd) {
        Logger.debug("--- Attempting to write data ---");
        Logger.debug("Table: " + table.getSimpleName());
        Logger.debug("Uses autoIncrementing: " + autoIncrement.contains(SemiClass.nameOf(table.getSimpleName())));
        Logger.debug("DataToAdd:" + dataToAdd);

        Logger.debug("AutoIncrementing list: " + autoIncrement);
        return addToTable(new SemiClass(table), dataToAdd);

    }

        /**
         * Deletes data from a {@code table} by id<br><br>
         *
         * Throws:<br>
         * {@link TableNotFoundException}<br>
         * {@link RecordNotFoundLogError}
         *
         * @param table the table to delete from
         * @param id the id to search for
         * @return if it was successful or not
         */
    public boolean deleteById(SemiClass table, int id) {

        if(!tableExists(table)) {
            throw new TableNotFoundException(table);
        }

        if(!FSJJsonWriter.tableContainsIdFile(id, table)) {
            Logger.error(new RecordNotFoundLogError(table, id));
            return false;
        }



        Path targetFileLocation = Path.of(FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/" + id + ".json");
        targetFileLocation = targetFileLocation.toAbsolutePath();

        File targetFile = new File(targetFileLocation.toUri());

        if(targetFile.exists()) {
            FileManager.deleteFsjdbFile(targetFileLocation.toString());
        }
        return true;
    }

    /**
     * Deletes data from a {@code table} by id<br><br>
     *
     * Throws:<br>
     * {@link TableNotFoundException}<br>
     * {@link RecordNotFoundLogError}
     *
     * @param table the table to delete from
     * @param id the id to search for
     * @return if it was successful or not
     */
    public boolean deleteById(Class table, int id) {
        return deleteById(new SemiClass(table), id);
    }

    /**
     * Similar to {@link #deleteById(SemiClass, int)}, but instead of deleting a {@code single file} it deletes from an {@code id}
     * to another {@code id} in a table<br><br>
     *
     * Throws:<br>
     * {@link TableNotFoundException}
     * {@link RecordNotFoundLogError}
     *
     * @param table the table to delete from
     * @param fromId where to start deleting data
     * @param toId where to end deleting data
     * @return if it was successful or not
     */
    public boolean deleteByFromIdToId(SemiClass table, int fromId, int toId) {
        if(!tableExists(table)) {
            throw new TableNotFoundException(table);
        }

        boolean hadFailed = false;

        for(int id = fromId; id <= toId; id++) {
            if(!FSJJsonWriter.tableContainsIdFile(id, table)) {
                Logger.error(new RecordNotFoundLogError(table, id));
                hadFailed = true;
            }
            Path targetFileLocation = Path.of(FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/" + id + ".json");
            targetFileLocation = targetFileLocation.toAbsolutePath();

            File targetFile = new File(targetFileLocation.toUri());

            if (targetFile.exists()) {
                FileManager.deleteFsjdbFile(targetFileLocation.toString());
            }
        }

        // inverts so if its false it will be true (success) or if it's true it will be false (failure)
        return !hadFailed;
    }

    /**
     * Similar to {@link #deleteById(SemiClass, int)}, but instead of deleting a {@code single file} it deletes from an {@code id}
     * to another {@code id} in a table
     *
     * @param table the table to delete from
     * @param fromId where to start deleting data
     * @param toId where to end deleting data
     * @return if it was successful or not
     */
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
     * @return if it was successful or not
     */
    public boolean deleteContaining(SemiClass table, Object key, Object value) {
        String tableName = table.getSimpleName();

        if(value == null) {
            Logger.error("Null value, make sure you aren't passing null values into JDM!");
            return false;
        }

        if(GetTypeOf.get(value).getTypeName().equals("java.lang.Integer")) {
            value = Double.parseDouble(value.toString());
        }

        if(!tableExists(table)) {
            throw new TableNotFoundException(table);
        }


        String targetDir = FSJDatabaseStructure.tablesDir + "/" + tableName;

        try {
            try(Stream<Path> paths = Files.walk(Paths.get(targetDir))) {
                Object finalValue = value;
                paths
                        .filter(Files::isRegularFile).
                        forEach(file -> {
                            if (file.getFileName().toString().contains(".json")) {
                                int id = Integer.parseInt(file.getFileName().toString().replace(".json", ""));
                                LinkedHashMap<Object, Object> map = FSJJsonReader.read(id, table);

                                if (map.entrySet().contains(Map.entry(key, finalValue))) {
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
     * @return if it was successful or not
     */
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
     * @return if it was successful or not
     */
    public boolean updateValueById(SemiClass table, int id, Object key, Object value) {
        if(tableExists(table)) {
            if(!FSJJsonWriter.tableContainsIdFile(id, table)) {
                Logger.error(new RecordNotFoundLogError(table, id));
                return false;
            }

            LinkedHashMap<Object, Object> map = FSJJsonReader.read(id, table);
            map.remove(key.toString());
            map.put(key, value);
            FSJJsonWriter.write(map, table, id, false);
            return true;
        } else {
            Logger.error("Table " + table + " does not exist");
            return false;
        }
    }


    /**
     * Updates a {@code specific keys} value in a data file found by {@code id}.
     *
     * @param table the table to find the data with
     * @param key the key to change the value of in the file
     * @param value the updated value
     * @param id the id to find the data
     * @return if it was successful or not
     */
    public boolean updateValueById(Class table, int id, Object key, Object value) {
        return updateValueById(new SemiClass(table), id, key, value);
    }

    /**
     * Replaces a value ({@code firstName=*value*}) in a data file found by if it contains something with a {@code new value}.
     *
     * @param table the table to search the data file
     * @param key the key to change the value of in the file
     * @param value the value to search for after the key
     * @param newValue the values to replace the previous value with
     * @return if it was successful or not
     */
    public boolean updateValueContaining(SemiClass table, Object key, Object value, Object newValue) {
        String tableName = table.getSimpleName();
        String targetDir =  FSJDatabaseStructure.tablesDir + "/" + tableName;

        if(!tableExists(table)) {
            throw new TableNotFoundException(table);
        }

        if(value == null) {
            Logger.error("Null value, make sure you aren't passing null values into JDM!");
            return false;
        }

        AtomicReference<Boolean> writeFailed = new AtomicReference<>(false);

        if(GetTypeOf.get(value).getTypeName().equals("java.lang.Integer")) {
            value = Double.parseDouble(value.toString());
        }

        try {
            try(Stream<Path> paths = Files.walk(Paths.get(targetDir))) {
                Object finalValue = value;
                paths
                        .filter(Files::isRegularFile).
                        forEach(file -> {
                            if (file.getFileName().toString().contains(".json")) {

                                int id = Integer.parseInt(file.getFileName().toString().replace(".json", ""));
                                LinkedHashMap<Object, Object> map = FSJJsonReader.read(id, table);

                                if (map.entrySet().contains(Map.entry(key, finalValue))) {
                                    map.remove(key);
                                    map.put(key, newValue);

                                    if (!FSJJsonWriter.write(map, table, id, false))
                                        writeFailed.set(true);
                                }

                            }

                        });
            }
        } catch (IOException e) {
            Logger.error("Failed to walk through the files in the folder\n" + e);
            return false;
        }
        return !writeFailed.get();
    }

    /**
     * Replaces a value ({@code firstName=*value*}) in a data file found by if it contains something with a {@code new value}.
     *
     * @param table the table to search the data file
     * @param key the key to change the value of in the file
     * @param value the value to search for after the key
     * @param newValue the values to replace the previous value with
     * @return if it was successful or not
     */
    public boolean updateValueContaining(Class table, Object key, Object value, Object newValue) {
        return updateValueContaining(new SemiClass(table), key, value, newValue);
    }


        /**
         * Updates a data file by id (id should be in data (2nd argument))
         *
         * @param table the table to search for the data file
         * @param data data to add
         * @throws RuntimeException throws from invalid data
         * @return if it was successful or not
         */
    public boolean updateById(SemiClass table, LinkedHashMap<Object, Object> data) throws RuntimeException {

        if(!FSJDatabaseInitializer.tables.contains(table) && !FSJDatabaseInitializer.freeTables.contains(table)) {
            throw new TableNotFoundException(table);
        }

        TableMeta meta = TableMeta.of(table);

        Set<String> requiredKeySet = getTableColumns(table, meta.isAutoIncrement());

        boolean added = false;
        if(meta.isAutoIncrement())
            added = requiredKeySet.add(getTableIdName(table));

        Logger.debug("Added id field: " + added);
        Logger.debug("Required set: " + requiredKeySet);

        if(!data.keySet().equals(requiredKeySet) && !FSJDatabaseInitializer.getFreeTables().contains(table)) {
            Logger.error(new InvalidDataKeySetLogError(data, requiredKeySet));
            return false;
        }


        if(!data.containsKey(getTableIdName(table))) {
            Logger.error("Data does not contain id field! Dataset: \n" + data);
            return false;
        }

        if(!(Double.parseDouble(data.get(getTableIdName(table)).toString())%1 == 0)) {
            Logger.error(new NumberFormatLogError(Double.parseDouble(data.get(getTableIdName(table)).toString()), "The id in updateById data must be a whole number!"));
            return false;
        }
        Object rawId = data.get(getTableIdName(table));
        Integer id = parseWholeId(rawId);
        if (id == null) {
            Logger.error(new NumberFormatLogError("The id in updateById data must be a whole number, got: " + rawId));
            return false;
        }

              if (!(FSJJsonWriter.tableContainsIdFile(id, table))) {
                  Logger.error(new RecordNotFoundLogError(table, id));
                  return false;
        }

              return FSJJsonWriter.write(data, table, id, false);
    }

    /**
     * Updates a data file by id (id should be in data (2nd argument))
     *
     * @param table the table to search for the data file
     * @param data data to add
     * @throws RuntimeException throws from invalid data
     * @return if it was successful or not
     */
    public boolean updateById(Class table, LinkedHashMap<Object, Object> data) throws RuntimeException {
        return updateById(new SemiClass(table), data);
    }

    /**
     * Updates a specific data file by looking for a file containing the searchData, it is sorted so no matter what you put it in it will properly sort it.
     *
     * @param table the table to change data with
     * @param searchData the search term for finding what to replace
     * @param data the replacement data after the data matching searchData is found
     * @return if it was successful or not
     */
    public boolean updateContaining(SemiClass table, LinkedHashMap<Object, Object> searchData,  LinkedHashMap<Object, Object> data) {

        String targetDir = FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName();

        Set<String> requiredKeySet = getTableColumns(table, TableMeta.of(table).isAutoIncrement());

        if(TableMeta.of(table).isAutoIncrement())
            requiredKeySet.add(getTableIdName(table));

        if(!data.keySet().equals(requiredKeySet)) {
            Logger.error(new InvalidDataKeySetLogError(data, requiredKeySet));
            return false;
        }

        AtomicReference<Boolean> writeFailed = new AtomicReference<>(false);

        Logger.debug("Updating containing");

            try(Stream<Path> paths = Files.walk(Path.of(targetDir))) {
                paths
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            if (file.getFileName().toString().endsWith(".json")) {
                                int id = Integer.parseInt(file.getFileName().toString().replace(".json", ""));
                                LinkedHashMap<Object, Object> map = FSJJsonReader.read(id, table);
                                Logger.debug("Attempted to find matching data in " + table + " with id " + id);
                                Logger.debug("Data: " + map);

                                HashMap<Object, Object> dataFound = new HashMap<>(map);
                                HashMap<Object, Object> searchTerm = new HashMap<>(searchData);

                                dataFound.remove(getTableIdName(table));
                                searchTerm.remove(getTableIdName(table));

                                if (dataFound.equals(searchTerm)) {
                                    LinkedHashMap<Object, Object> newData = new LinkedHashMap<>(data);
                                    newData.remove(getTableIdName(table));
                                    newData.put(getTableIdName(table), id);

                                    Logger.debug("Found match, update data: " + newData + "\n" + "search data: " + searchTerm + "\n" + "dataFound: " + dataFound);
                                    Logger.debug("Id: " + id);
                                    Logger.debug("Table: " + table);
                                    if (!FSJJsonWriter.write(newData, table, id, false))
                                        writeFailed.set(true);
                                }
                            }

                        });
            } catch (IOException e) {
                Logger.error("Failed to walk through directory\n" + e);
                return false;
            }
        return !writeFailed.get();
    }

    /**
     * Updates a specific data file by looking for a file containing the searchData, it is sorted so no matter what you put it in it will properly sort it.
     *
     * @param table the table to change data with
     * @param searchData the search term for finding what to replace
     * @param data the replacement data after the data matching searchData is found
     * @throws RuntimeException throws RuntimeException mostly from invalid data
     * @return if it was successful or not
     */
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
        if(!FSJDatabaseInitializer.tables.contains(table) && !FSJDatabaseInitializer.freeTables.contains(table))
            throw new TableNotFoundException(table);
        if(new File(FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/" + id + ".json").exists()) {
            return FSJJsonReader.read(id, table);
        } else {
            Logger.error("Data not found in table " + table.getSimpleName() + " with id " + id);
            return new LinkedHashMap<>();
        }
    }

    /**
     * Gets data from a data file by id
     *
     * @param table the table to find the data file of
     * @param id the id of the data file to find
     * @return returns a {@link LinkedHashMap} of the data files data
     */
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
    public List<LinkedHashMap<Object, Object>> findByValueContaining(SemiClass table, Object key, Object value) {
        if(!tableExists(table)) {
            throw new TableNotFoundException(table);
        }

        if(value == null) {
            Logger.error("Null value, make sure you aren't passing null values into JDM!");
            return new ArrayList<>();
        }

        String tableName = table.getSimpleName();
        List<LinkedHashMap<Object, Object>> finalContent = new ArrayList<>();
        String targetDir = FSJDatabaseStructure.tablesDir + "/" + tableName;

        if(GetTypeOf.get(value).getTypeName().equals("java.lang.Integer")) {
            value = Double.parseDouble(value.toString());
        }

        try {
            try(Stream<Path> paths = Files.walk(Paths.get(targetDir))) {
                Object finalValue = value;
                paths
                        .filter(Files::isRegularFile).
                        forEach(file -> {
                            if (file.getFileName().toString().contains(".json")) {
                                int id = Integer.parseInt(file.getFileName().toString().replace(".json", ""));

                                LinkedHashMap<Object, Object> map = FSJJsonReader.read(id, table);

                                if (map.entrySet().contains(Map.entry(key, finalValue))) {
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

    /**
     * Looks everywhere in a table matching the data provided<br>
     * @param table The table to be used
     * @param key the key of the value to check
     * @param value the value to look for
     * @return returns found matches
     */
    public List<LinkedHashMap<Object, Object>> findByValueContaining(Class table, Object key, Object value) {
        return findByValueContaining(new SemiClass(table), key, value);
    }

    /**
     * Find all values of a key
     *
     * @param table the table to scan
     * @param key the key value
     * @return returns a {@code LinkedList<Map.Entry<Object, Object>>}.<br>
     * The key value in the Map.Entry is the id.<br>
     * The value of Map.Entry is the value
     */
    public LinkedList<Map.Entry<Object, Object>> findAllValuesOfKey(SemiClass table, String key) {
        if(!tableExists(table)) {
            throw new TableNotFoundException(table);
        }
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

    /**
     * Find all values of a key
     * @param table the table to scan
     * @param key the key value
     * @return returns a {@code LinkedList<Map.Entry<Object, Object>>}.<br>
     * The key value in the Map.Entry is the id.<br>
     * The value of Map.Entry is the value
     */
    public LinkedList<Map.Entry<Object, Object>> findAllValuesOfKey(Class table, String key) {
        return findAllValuesOfKey(new SemiClass(table), key);
    }

    /**
     * Gets the table columns of a class, this is needed to detect if a data map given is invalid to the columns in {@link #addToTable(SemiClass, LinkedHashMap)} and other methods.
     *
     * @param table the table to get the columns of
     * @param autoIncrement when not using auto incrementing it will add the @id annotation name to match the given map data if it's not already invalid
     * @return returns a set of columns
     */
    public Set<String> getTableColumns(SemiClass table, boolean autoIncrement) {
        if(!tableExists(table)) {
            throw new TableNotFoundException(table);
        }
        Set<String> columns;
        TableMeta tableMeta = TableMeta.of(Path.of(FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/meta"));

        columns = new LinkedHashSet<>(tableMeta.getColumnNames());
        if(autoIncrement)
            columns.remove(getTableIdName(table));
        return columns;
    }

    /**
     * This method handles writing things to the database, the reason this method exists is so the<br>
     * {@link #addToTable(SemiClass, LinkedHashMap)} isn't longer than it should be.
     *
     * @param dataToAdd the data to add
     * @param table the table to add the data to
     * @param autoIncrement if the table uses auto incrementing*/
    private boolean writeToDatabase(LinkedHashMap<Object, Object> dataToAdd, SemiClass table, boolean autoIncrement) {
        if(autoIncrement) {
            int id = JAutoIncrement.get(table);

            if(id == -314) {
                Logger.error("A bug occurred as id increment is -314.");
                return false;
            }



            while (FSJJsonWriter.tableContainsIdFile(id, table)) {
                Logger.warn("Auto-increment counter for " + table + " was behind: id " + id + " is already used, trying " + (id + 1));
                id++;
            }
            LinkedHashMap<Object, Object> finalData = new LinkedHashMap<>(dataToAdd);
            finalData.put(getTableIdName(table), id);

            FSJJsonWriter.write(finalData, table, id, false);
            if (!FSJJsonWriter.write(finalData, table, id, false))
                return false;
            JAutoIncrement.set(table, id + 1);


        }

        // logic for adding data to table without auto increment
        else {
            Object rawId = dataToAdd.get(getTableIdName(table));
            Integer id = parseWholeId(rawId);
            if (id == null) {
                Logger.error(new NumberFormatLogError("The id '" + getTableIdName(table) + "' in dataToAdd must be a whole number, got: " + rawId));
                return false;
            }

            if(!FSJJsonWriter.tableContainsIdFile(id, table))
                return FSJJsonWriter.write(dataToAdd, table, id, false);
            else {
                Logger.error("Data already exists in the database with the id of " + id + "!");
                return false;
            }

        }
        return true;
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
        if(!tableExists(table)) {
            throw new TableNotFoundException(table);
        }

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

    /**
     * Helper method that just adds {@link FSJJsonWriter}'s method for seeing if a record exists into JDM
     * @param id The id
     * @param table The table
     * @return if the record exists
     */
    public boolean recordExists(int id, Class<?> table) {
        return recordExists(id, new SemiClass(table));
    }

    /**
     * Helper method that just adds {@link FSJJsonWriter}'s method for seeing if a record exists into JDM
     * @param id The id
     * @param table The table
     * @return if the record exists
     */
    public boolean recordExists(int id, SemiClass table) {
        if(!tableExists(table)) {
            throw new TableNotFoundException(table);
        }
        return FSJJsonWriter.tableContainsIdFile(id, table);
    }

    /**
     * Gets the autoIncrement list
     * @return {@link #autoIncrement}
     */
    public List<SemiClass> getAutoIncrement() {
        return autoIncrement;
    }

    /**
     * Helper method to see if a table exists
     * @param table the table
     * @return whether it exists or not
     */
    protected boolean tableExists(SemiClass table) {
        return FSJDatabaseInitializer.tables.contains(table) | FSJDatabaseInitializer.freeTables.contains(table);
    }

    /**
     * Reads an id given as 3, 3L, 3.0 (how Gson reads numbers back), "3" or "3.0".
     *
     * @param raw the id value from a record or a data map
     * @return the id, or null if it is not a whole number
     */
    private static Integer parseWholeId(Object raw) {
        if (raw == null)
            return null;
        try {
            double d = Double.parseDouble(raw.toString().trim());
            if (d % 1 != 0 || d < Integer.MIN_VALUE || d > Integer.MAX_VALUE)
                return null;
            return (int) d;
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
