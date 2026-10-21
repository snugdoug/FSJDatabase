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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.snugdoug.fsjdatabase.data.annotation.TableMeta;
import com.snugdoug.fsjdatabase.data.json.FSJJsonWriter;
import com.snugdoug.fsjdatabase.util.Logger;
import com.snugdoug.fsjdatabase.util.clazz.SemiClass;
import com.snugdoug.fsjdatabase.util.field.SemiField;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * This creates the core structure of FSJDatabase.
 */
public class FSJDatabaseStructure {
    // DO NOT MODIFY
    // Until I release something that fixes it, when modified it will break FSJDatabase!
    // So if you want to break the library for fun, look here.

    /**
     * Create a new instance
     */
    public FSJDatabaseStructure() {

    }

    /**
     * Root directory
     */
    public static String root;

    /**
     * Data directory: root/data
     */
    public static String dataDir;

    /**
     * Tables directory root/tables
     */
    public static String tablesDir;

    /**
     * autoIncrement location (legacy): root/data/autoIncrement.fsj
     */
    public static String autoIncrementLoc;

    /**
     * If the structure exists, used in legacy class {@link AutoIncrement}<br>
     * This is false by default
     */
    protected static boolean doesStructureExist = false;

    /**
     * The current directory
     */
    private static final String currentDir = System.getProperty("user.dir");

    public static void create(Path rootFSJDatabaseDirectory) throws IOException {
        if(FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName)
            createStructure(rootFSJDatabaseDirectory, FSJDatabaseConfiguration.rootFSJDatabaseDirectoryName);

        if(!FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName)
            createStructure(rootFSJDatabaseDirectory, "FSJDatabase");
    }

    /**
     * Provides the arguments for {@link #createStructure(Path, String)} so you don't have to manually provide them
     */
    public static void create() {
        if(FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName)
            createStructure(Path.of(currentDir), FSJDatabaseConfiguration.rootFSJDatabaseDirectoryName);

        if(!FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName)
            createStructure(Path.of(currentDir), "FSJDatabase");
    }

    /**
     * Creates the structure for FSJDatabase<br>
     * Structure:<br>
     * - root<br>
     * ----- root/data | tables<br>
     * --------- root/data/autoIncrement.json | autoIncrement.fsj
     * @param rootDirectory the root
     * @param rootFSJDatabaseDirectoryName the directory name
     */
    public static void createStructure(Path rootDirectory, String rootFSJDatabaseDirectoryName) {
        root = rootDirectory + "/" + rootFSJDatabaseDirectoryName;
        dataDir = root + "/" + "data";
        tablesDir = root + "/" + "tables";
        autoIncrementLoc = dataDir + "/autoIncrement.fsj";

        FileManager.mkdir(root);
        FileManager.mkdir(dataDir);
        FileManager.createFile(dataDir + "/autoIncrement.fsj");
        FileManager.createFile(dataDir + "/autoIncrement.json");

        doesStructureExist = true;
    }

    /**
     * Adds a table to the structure<br>
     * - root<br>
     * ---- tables<br>
     * -------- table
     *
     * @param table the table to add
     * @param autoIncrement whether it auto increments
     */
    protected static void addTable(Class table, boolean autoIncrement) {
        create();

        Map<String, Integer> idMap = JAutoIncrement.getAll();

        if(idMap == null) {
            Logger.debug("idMap was checked as null, confirmation: " + idMap);
            idMap = new HashMap<>();
            Logger.warn("idMap was null! Possible reasons are: First run, or an actual bug.");
        }

        try {
            if (!idMap.containsKey(table.getSimpleName())) {

                idMap.put(table.getSimpleName(), 0);
                Writer jwriter = new FileWriter(dataDir + "/" + "autoIncrement.json", false);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Logger.debug(gson.toJson(idMap));
                jwriter.append(gson.toJson(idMap));
                jwriter.flush();
                jwriter.close();
            }
        } catch (Exception e) {
            Logger.error("Failed to write to data/autoIncrement.json\n" + e);
        }

        String tableName = table.getSimpleName();
        FileManager.mkdirs(tablesDir + "/" + tableName);
        FileManager.mkdirs(tablesDir + "/" + tableName + "/fields");

        String tableDir = tablesDir + "/" + tableName;

        TableMeta meta = TableMeta.of(table);

        String metaPath = tableDir + "/meta";
        FileManager.createFile(metaPath);


        try {

            Writer metaWriter = new FileWriter(tableDir + "/meta", false);

            meta.toMap().forEach((String k, Object v) -> {

                try {
                    metaWriter.append(k).append("=").append(v.toString()).append("\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            metaWriter.flush();
            metaWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create table metadata file\n" + e);
        }



        meta.getColumnNames().forEach((name) -> {
            try {
                FileManager.createFile(tableDir + "/fields/" + name + ".fsj");
                Writer annoWriter = new FileWriter(tableDir + "/fields/" + name + ".fsj", false);
                HashMap<String, List<String>> map = new HashMap<>(meta.getGetFieldAnnotationsByName());
                annoWriter.write(map.get(name).toString().replace("[","").replace("]",""));
                annoWriter.flush();
                annoWriter.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create table field meta files\n" + e);
            }
        });


        try {
            Writer writer = new FileWriter(dataDir + "/" + "autoIncrement.fsj", true);
            Properties props = new Properties();
            Reader read = new FileReader(dataDir + "/" + "autoIncrement.fsj");
            props.load(read);

            if(!props.containsKey(tableName)) {
                writer.append(tableName).append("=0\n");
                writer.flush();
                writer.close();
            }




        } catch (IOException e) {
            throw new RuntimeException("Failed to add table to auto incrementing file\n" + e);
        }
    }

    /**
     * Checks if a table is in the database structure
     *
     * @param clazz the table to check
     * @return gives true or false for if it was in the database or not
     */
    public static boolean isTableNotInDatabase(Class<?> clazz) {
        create();

        File hypotheticTableDir = new File(tablesDir + "/" + clazz.getName());
        return !hypotheticTableDir.exists();
    }

    /**
     * Update from legacy FSJdatabase
     * @return gives true or false for if it was successful in updating
     */
    public static boolean update() {

        if (!FSJDatabaseInitializer.init) {
            Logger.error("FSJDatabase not initialized.");
            return false;
        } else {
            create();
        }

        Logger.debug("root: " + root);
        LocalDateTime now = LocalDateTime.now();
        Path absRoot = Path.of(root).toAbsolutePath();
        Path targetBackupDir = absRoot.getParent().resolve(
                absRoot.getFileName().toString() + "_BACKUP_" + now.toLocalTime().toString()
        );
        if(!FileManager.copyDir(Path.of(root), targetBackupDir)) {
            Logger.error("Failed to copy database directory!");
            return false;
        }

        List<Path> tablesPaths = new ArrayList<>();
        HashMap<Path, SemiClass> tableMap = new HashMap();
        Logger.debug("Tables: " + FSJDatabaseInitializer.tables);
        FSJDatabaseInitializer.tables.forEach((table) -> {
            tablesPaths.add(Path.of(tablesDir + "/" + table.getSimpleName()));
            tableMap.put(Path.of(tablesDir + "/" + table.getSimpleName()), table);
        });

        List<Path> freeTablePaths = new ArrayList<>();
        HashMap<Path, SemiClass> freeTableMap = new HashMap();
        FSJDatabaseInitializer.freeTables.forEach((table) -> {
            freeTablePaths.add(Path.of(tablesDir + "/" + table.getSimpleName()));
            freeTableMap.put(Path.of(tablesDir + "/" + table.getSimpleName()), table);
        });
        AtomicReference<Float> updatedCount = new AtomicReference<>((float) 0);

        Logger.debug("Paths: " + tablesPaths);

        // Update autoIncrement.fsj
        try(BufferedReader reader = new BufferedReader(new FileReader(dataDir + "/autoIncrement.fsj"))) {
            HashMap<Object, Object> temp = new HashMap<>();
            HashMap<SemiClass, Integer> result = new HashMap<>();
            reader.lines().forEach((line) -> {
               temp.putAll(Parser.parse(line));
            });
            for (SemiClass table : FSJDatabaseInitializer.tables) {
                int id = Integer.parseInt(temp.get(table.getSimpleName()).toString());
                result.put(table, id);
            }
            for (SemiClass freetable : FSJDatabaseInitializer.freeTables) {
                int id = Integer.parseInt(temp.get(freetable.getSimpleName()).toString());
                result.put(freetable, id);
            }
            FSJJsonWriter.writeAutoIncrementFile(result);
            FileManager.deleteFsjdbFile(FSJDatabaseStructure.dataDir + "/autoIncrement.fsj");
        } catch (Exception e) {
            Logger.warn("Failed to convert autoIncrement.fsj!\n" + e);
        }

        // I have created eachception
        tablesPaths.forEach((path) -> {

                try(Stream<Path> stream = Files.walk(path)) {

                    stream.forEach((file) -> {

                        if(file.toFile().getName().endsWith(".txt")) {
                            updatedCount.getAndSet((float) (updatedCount.get() + 1));
                            TableMeta meta = TableMeta.of(Path.of(path + "/meta"));
                            List<SemiField> fields = meta.getColumnFields();
                            LinkedHashMap<Object, Object> result = new LinkedHashMap<>();
                            fields.forEach((field) -> {
                                try {
                                    if(file.toFile().exists()) {
                                        Properties props = new Properties();
                                        props.load(new FileReader(file.toFile()));

                                        Object toConvert = props.get(field.getName());

                                        if (!(toConvert == null)) {
                                            Logger.debug("Current field: " + field);
                                            Logger.debug("toConvert: " + toConvert);
                                            // Convert to basic types.
                                            Class type = field.getSemiFieldClass();
                                            Logger.debug("Type: " + field.getType());

                                            if (type.equals(String.class)) {
                                                toConvert = String.valueOf(toConvert);
                                            }
                                            if (type.equals(Integer.class)) {
                                                toConvert = Integer.parseInt(String.valueOf(toConvert));
                                            }
                                            if (type.equals(Double.class)) {
                                                toConvert = Double.parseDouble(String.valueOf(toConvert));
                                            }
                                            if (type.equals(Float.class)) {
                                                toConvert = Float.parseFloat(String.valueOf(toConvert));
                                            }
                                            if (type.equals(Byte.class)) {
                                                toConvert = Byte.parseByte(String.valueOf(toConvert));
                                            }
                                            if(!field.getName().equals(new JDataManager().getTableIdName(tableMap.get(path))))
                                                result.put(field.getName(), toConvert);
                                            Logger.debug(tableMap.get(path));
                                        }
                                    }
                                } catch (IOException e) {
                                    Logger.warn("Failed do the proper converting process for file: " + file + "\n" + e);
                                }
                            });
                            FileManager.deleteFsjdbFile(String.valueOf(file));
                            FSJJsonWriter.write(result, tableMap.get(path), Integer.parseInt(file.getFileName().toString().replace(".txt", "")), false);

                        }
                    });
                } catch (Exception e) {
                    Logger.warn("Failed to walk through directory: " + path + "\n" + e);
                }
        });
        // Free Tables
        freeTablePaths.forEach((path) -> {

            try(Stream<Path> stream = Files.walk(path)) {

                stream.forEach((file) -> {
                    if(file.toFile().getName().endsWith(".txt")) {
                        updatedCount.getAndSet((float) (updatedCount.get() + 1));

                        LinkedHashMap<Object, Object> result = new LinkedHashMap<>();

                        if(file.toFile().exists()) {
                            try (BufferedReader reader = new BufferedReader(new FileReader(String.valueOf(file)))) {
                                reader.lines().forEach((line) -> {
                                    result.putAll(Parser.parse(line));
                                });
                            } catch (Exception e) {
                                Logger.warn("Failed to convert Free Table file: " + file);
                            }
                        }
                        FileManager.deleteFsjdbFile(String.valueOf(file));
                        FSJJsonWriter.write(result, freeTableMap.get(path), Integer.parseInt(file.getFileName().toString().replace(".txt", "")), false);
                    }
                });
            } catch (Exception e) {
                Logger.warn("Failed to walk through directory: " + path + "\n" + e);
            }
        });
        Logger.info("Attempted to update " + updatedCount + " files.");
        return true;
    }
}
