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

package com.snugdoug.fsjdatabase.data.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.snugdoug.fsjdatabase.data.FSJDatabaseInitializer;
import com.snugdoug.fsjdatabase.data.FSJDatabaseStructure;
import com.snugdoug.fsjdatabase.data.FileManager;
import com.snugdoug.fsjdatabase.data.JAutoIncrement;
import com.snugdoug.fsjdatabase.util.Logger;
import com.snugdoug.fsjdatabase.util.clazz.SemiClass;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Writes data to the database, reducing boiler plate code
 */
public class FSJJsonWriter {

    /**
     * Create a new instance
     */
    public FSJJsonWriter() {

    }

    /**
     * Writes data to the database
     *
     * @param map the data to write
     * @param table the table to write to
     * @param id the id
     * @param autoIncrement if it auto increments or not
     */
    public static void write(LinkedHashMap map, SemiClass table, int id, boolean autoIncrement) {
        String path = FSJDatabaseStructure.tablesDir + "/" + table.getSimpleName() + "/" + id + ".json";
        try {

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(map);
            FileWriter writer = new FileWriter(path);

            FileManager.createFile(path);

            Logger.debug("Writing data, id: " + id + ", autoIncrement: " + autoIncrement + ", table: " + table + ", data: " + map);
            writer.write(jsonString);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (autoIncrement)
            JAutoIncrement.increment(table);
    }

    /**
     * Writes directly to the autoIncrement.json file (root/data/autoIncrement.json)
     * @param map the data
     */
    public static void writeAutoIncrementFile(HashMap<SemiClass, Integer> map) {
        String path = FSJDatabaseStructure.dataDir + "/" + "autoIncrement" + ".json";
        try {

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gson.toJson(map);
            FileWriter writer = new FileWriter(path);

            writer.write(jsonString);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
