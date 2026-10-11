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

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

public class FSJDatabaseStructure {
    // DO NOT MODIFY
    // Until I release something that fixes it, when modified it will break FSJDatabase!
    // So if you want to break the library for fun, look here.
    public static String root;
    public static String dataDir;
    public static String tablesDir;
    public static String autoIncrementLoc;

    protected static boolean doesStructureExist = false; // false by default
    private static String currentDir = System.getProperty("user.dir");

    public static void create(Path rootFSJDatabaseDirectory) throws IOException {
        if(FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName)
            createStructure(rootFSJDatabaseDirectory, FSJDatabaseConfiguration.rootFSJDatabaseDirectoryName);

        if(!FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName)
            createStructure(rootFSJDatabaseDirectory, "FSJDatabase");
    }

    public static void create() throws IOException {
        if(FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName)
            createStructure(Path.of(currentDir), FSJDatabaseConfiguration.rootFSJDatabaseDirectoryName);

        if(!FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName)
            createStructure(Path.of(currentDir), "FSJDatabase");
    }

    public static void createStructure(Path rootDirectory, String rootFSJDatabaseDirectoryName) throws IOException {
        root = rootDirectory + "/" + rootFSJDatabaseDirectoryName;
        dataDir = rootFSJDatabaseDirectoryName + "/" + "data";
        tablesDir = rootFSJDatabaseDirectoryName + "/" + "tables";
        autoIncrementLoc = dataDir + "/autoIncrement.fsj";

        FileManager.mkdir(root);
        FileManager.mkdir(dataDir);
        FileManager.createFile(dataDir + "/autoIncrement.fsj");

        doesStructureExist = true;
    }

    protected static void addTable(Class table, boolean autoIncrement) {
        try {
            create();
        } catch (IOException e) {
            throw new RuntimeException("Could not create structure!\n" + e);
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

    public static boolean isTableNotInDatabase(Class<?> clazz) {
        try {
            create();
        } catch (Exception e) {
            throw new RuntimeException("Could not create FSJDatabase structure! Stacktrace: \n" + e);
        }

        File hypotheticTableDir = new File(tablesDir + "/" + clazz.getName());
        return !hypotheticTableDir.exists();
    }
}
