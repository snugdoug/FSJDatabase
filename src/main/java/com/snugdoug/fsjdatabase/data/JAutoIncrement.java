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
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.snugdoug.fsjdatabase.data.json.FSJJsonReader;
import com.snugdoug.fsjdatabase.util.Logger;
import com.snugdoug.fsjdatabase.util.clazz.SemiClass;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages auto incrementing
 */
public class JAutoIncrement {

    /**
     * Create a new instance
     */
    public JAutoIncrement() {

    }

    /**
     * Gets the id for the table
      * @param table the table to get the id of
     * @return the id
     */
    public static int get(SemiClass table) {
        return getAll().getOrDefault(table.getSimpleName(), -1);

    }

    /**
     * Increments the id of a table
     * @param table the table to increment
     */
    public static void increment(SemiClass table) {
        String path = FSJDatabaseStructure.dataDir + "/autoIncrement.json";
        int id = get(table);
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            FileReader fileReader = new FileReader(path);
            BufferedReader reader = new BufferedReader(fileReader);


            Map<String, Integer> resultMap = getAll();

            resultMap.remove(table.getSimpleName());
            resultMap.put(table.getSimpleName(), id + 1);
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));

            Logger.debug("Rewriting to data/autoincrement.fsj: " + resultMap);

            writer.append(gson.toJson(resultMap));
            writer.flush();
            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Gives all the id values
     * @return {@code Map<String, Integer>} of the table -> id values
     */
    public static Map<String, Integer> getAll() {
        String path = FSJDatabaseStructure.dataDir + "/autoIncrement.json";
        FileReader reader = null;
        try {
            reader = new FileReader(path);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            TypeToken<Map<String, Integer>> mapType = new TypeToken<>() {
            };
            Map<String, Integer> result = gson.fromJson(reader, mapType);
            Logger.debug("Gson result: " + result);

            if(result == null) {
                Logger.warn("Returning empty LinkedHashMap, as Gson result returned null!");
                return new LinkedHashMap<>();
            }

            return result;
        } catch (NullPointerException | FileNotFoundException e) {
            Logger.error("Failed to read autoIncrement.json\n" + e);
        }
        Logger.warn("Returning empty LinkedHashMap, as autoIncrement.json could not be read!");
        return new LinkedHashMap<>();
    }
}
