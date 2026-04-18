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

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Handles all auto incrementing actions
 */
public class AutoIncrement {

    /**
     * Gets the current autoIncrement id from the autoIncrement.fsj file
     *
     * @param table the table to get the current autoIncrement id of
     * @return returns the id
     */
    public static Integer getId(Class table) {
        try {
            FSJDatabaseStructure.create();

            Reader read;
            try {
                read = new FileReader(FSJDatabaseStructure.dataDir + "/autoIncrement.fsj");
            } catch(FileNotFoundException e){
                throw new NullPointerException("Could not read file: autoIncrement.fsj" + e);
            }

            // get the id from the autoIncrement.fsj file
            Properties props = new Properties();
            props.load(read);

            int id = Integer.parseInt(props.getProperty(table.getSimpleName(),"0"));

            // ! TEMP
            return id;
        } catch (IOException e) {
            throw new RuntimeException("An IOException occurred while making the database structure" + e);
        }
    }

    /**
     * Increments a tables autoIncrement by 1 in the autoIncrement.fsj file
     *
     * @param table
     */

    public static void incrementTableId(Class table) {
        String removeData = table.getSimpleName();
        Path path = Path.of(FSJDatabaseStructure.autoIncrementLoc);

        try {
            if (!FSJDatabaseStructure.doesStructureExist) {
                throw new RuntimeException("FSJDatabase structure doesn't exist");
            }


            Reader read = new FileReader(String.valueOf(path));

            Properties props = new Properties();
            props.load(read);

            // get the previous id in the file and add one to it
            int previousId = Integer.parseInt(props.getProperty(table.getSimpleName(),"0").replace(table.getSimpleName() + "=", ""));
            int finalId = previousId + 1;

            Files.write(path,
                    Files.lines(path)
                            .filter(line -> !line.contains(removeData))
                            .collect(Collectors.toList()),
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            // create a writer to add the updated id
            Writer writer = new FileWriter(String.valueOf(path), true);

            writer.write(table.getSimpleName() + "=" + finalId);
            writer.flush();

            writer.close();

        } catch (IOException e) {
            throw new RuntimeException("Failed to auto increment table: '" + table.getSimpleName() + "'" + e);
        }
    }
}
