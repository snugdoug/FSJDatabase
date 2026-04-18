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

package com.snugdoug.fsjdatabase.examples;

import com.snugdoug.fsjdatabase.data.DataManager;
import com.snugdoug.fsjdatabase.data.FSJDatabaseConfiguration;
import com.snugdoug.fsjdatabase.data.FSJDatabaseInitializer;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * <h1>Examples for FSJDB, these contain all CRUD examples + making a table</h1>
 *
 * <h2>Important</h2>
 * Make SURE that you have your init class in the base directory of your project!
 * <br>
 * Tables need to be separate classes, because FSJDB has some problems parsing tables in nested classes!
 *
 * <h2>Side Notes</h2>
 * I am near 99% sure that you could TECHNICALLY pair FSJDB with Spring/SpringBoot, if you do decide to do this, I haven't tried it YET, so good luck!
 */
public class ExampleClass {
    public static void main(String[] args) {
        // Configuring, MUST be before the init call so it auto-detects it.
        // Characters such as /,\, etc., aren't allowed.
        FSJDatabaseConfiguration fsjdbConfig = new FSJDatabaseConfiguration().build("FSJDB_TestingDB");

        // initialize fsjdb
        FSJDatabaseInitializer fsjdb = new FSJDatabaseInitializer(ExampleClass.class);


        // to create a DataManager fsjdb MUST be initialized
        DataManager data = new DataManager();

        // Creating data to add
        LinkedHashMap<String, String> testData = new LinkedHashMap<>();
        testData.put("firstName", "Alice");
        testData.put("lastName", "Smith");
        testData.put("email", "alice@fakegmail.com");

        // Creating FREE TABLE data
        LinkedHashMap<String, String> freeTableTestData = new LinkedHashMap<>();
        freeTableTestData.put("firstName", "Alice");
        freeTableTestData.put("lastName", "Smith");
        freeTableTestData.put("email", "alice@fakegmail.com");
        freeTableTestData.put("phone", "phone_number");
        freeTableTestData.put("phone_model", "phone_model");
        freeTableTestData.put("phone_model2", "phone_model");
        freeTableTestData.put("phone_model3", "phone_model");

        // Testing without auto increment
        LinkedHashMap<String, String> noAutoIncrementData = new LinkedHashMap<>();
        noAutoIncrementData.put("firstName", "Alice");
        noAutoIncrementData.put("lastName", "Smith");
        noAutoIncrementData.put("email", "alice@fakegmail.com");
        noAutoIncrementData.put("id", "1");

        // Deleting data
        data.deleteById(TableNumberOne.class, 5);
        data.deleteByFromIdToId(TableNumberOne.class, 10, 11);
        data.deleteContaining(TableNumberOne.class, "firstName", "AliceE");

        // Updating Data
        data.updateValueById(TableNumberOne.class, 28, "firstName", "Jonny");
        data.updateValueContaining(TableNumberOne.class, "firstName", "AliceEE", "John");

        LinkedHashMap<String, String> updatingOneData = new LinkedHashMap<>();
        updatingOneData.put("firstName", "AliceEEEEE");
        updatingOneData.put("lastName", "AliceEEEEE");
        updatingOneData.put("emailName", "AliceEEEEE");
        updatingOneData.put("id", "27");

        data.updateById(TableNumberOne.class, updatingOneData);

        LinkedHashMap<String, String> updatingTwoData = new LinkedHashMap<>();
        updatingTwoData.put("firstName", "Jimmy");
        updatingTwoData.put("lastName", "Smith");
        updatingTwoData.put("emailName", "jimmy@fakegmail.com");

        LinkedHashMap<String, String> updatingTwoSearchTerm = new LinkedHashMap<>();
        updatingTwoSearchTerm.put("firstName", "AliceEEE");
        updatingTwoSearchTerm.put("lastName", "Smith");
        updatingTwoSearchTerm.put("email", "alice@fakegmail.com");

        data.updateContaining(TableNumberOne.class, updatingTwoSearchTerm, updatingTwoData);

        // finding data
        System.out.println(data.findById(TableNumberOne.class, 28));
        System.out.println(data.findByValueContaining(TableNumberOne.class, "firstName", "Jimmy"));


        try {
            // sometimes this can throw an IO exception (never have had a case where it HAS though), if it has invalid data it is VERY much likely to through a runtime exception
            data.addToTable(TableNumberOne.class, testData);
            data.addToTable(TableNumberThree.class, freeTableTestData);
            data.addToTable(TableNumberTwo.class, noAutoIncrementData);
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    // ! Not Recommended, from what I know it doesn't find this, this is a example for a separate class table !

//    @Table(name = "TableNumberTwo")
//    public class TableNumberTwo {
//
//        @Column(name = "product")
//        String product;
//
//        @Id(autoIncrement = true)
//        int id;
//
//    }
}
