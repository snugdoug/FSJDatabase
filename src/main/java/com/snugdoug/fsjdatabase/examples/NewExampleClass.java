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

import com.snugdoug.fsjdatabase.data.*;
import com.snugdoug.fsjdatabase.data.json.FSJJsonReader;
import com.snugdoug.fsjdatabase.util.Logger;
import com.snugdoug.fsjdatabase.util.clazz.SemiClass;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Examples for FSJDB, these contain all CRUD examples
 * <br><br>
 * Important<br><br>
 * Make SURE that you have your init class in the base directory of your project!
 * <br><br>
 * Tables need to be separate classes, because FSJDB has some problems parsing tables in nested classes!
 **/
public class NewExampleClass {

    /**
     * Create a new instance
     */
    public NewExampleClass() {

    }

    /**
     * Main method for examples/testing
     * @param args the arguments
     */
    public static void main(String[] args) {
        FSJDatabaseConfiguration conf = new FSJDatabaseConfiguration();
        conf.build("FSJDB_TestingDB", true);

        FSJDatabaseInitializer init = new FSJDatabaseInitializer(NewExampleClass.class);

        JDataManager dm = new JDataManager();

        LinkedHashMap<Object, Object> dataTest1 = new LinkedHashMap<>();
        dataTest1.put("firstName", "Alice");
        dataTest1.put("lastName", "Smith");
        dataTest1.put("email", "alice@fakegmail.com");

        // Creating FREE TABLE data
        LinkedHashMap<Object, Object> freeTableTestData = new LinkedHashMap<>();
        freeTableTestData.put("firstName", "Alice");
        freeTableTestData.put("lastName", "Smith");
        freeTableTestData.put("email", "alice@fakegmail.com");
        freeTableTestData.put("phone", "phone_number");
        freeTableTestData.put("phone_model", "phone_model");
        freeTableTestData.put("phone_model2", "phone_model");
        freeTableTestData.put("phone_model3", "phone_model");



        // Testing without auto increment
        LinkedHashMap<Object, Object> noAutoIncrementData = new LinkedHashMap<>();
        noAutoIncrementData.put("firstName", "Alice");
        noAutoIncrementData.put("lastName", "Smith");
        noAutoIncrementData.put("email", "alice@fakegmail.com");
        noAutoIncrementData.put("id", "1"); // No 100% confirmation that it works with integers, feel free to give it a try though!

        // Updating data

        // value by id
        dm.updateValueById(TableNumberOne.class, 1, "firstName", "Jim");
        dm.updateValueById(TableNumberOne.class, 0, "firstName", "Jimmy");

        // value by containing
        dm.updateValueContaining(TableNumberOne.class, "firstName", "Jimmy", "NotAlice");

        // Updating by id
        LinkedHashMap<Object, Object> updateData = new LinkedHashMap<>();
        updateData.put("firstName", "alice#02");
        updateData.put("lastName", "ecila");
        updateData.put("emailName", "alice#02@fakegmail.com");
        updateData.put("id", "4");

        LinkedHashMap<Object, Object> updateData2 = new LinkedHashMap<>();
        updateData2.put("firstName", "alice#03");
        updateData2.put("lastName", "ecila");
        updateData2.put("emailName", "alice#03@fakegmail.com");
        updateData2.put("id", "5");

        dm.updateById(TableNumberOne.class, updateData);
        dm.updateById(TableNumberOne.class, updateData2);


        // updating by containing (kinda equals though)
        LinkedHashMap<Object, Object> newUpdateData = new LinkedHashMap<>();
        updateData.put("firstName", "Nah");
        updateData.put("lastName", "RatherNotProvideThis");
        updateData.put("emailName", "nottoday@fakegmail.com");
        updateData.put("id", "5");

        dm.updateContaining(TableNumberOne.class, updateData2, newUpdateData);

        // Deleting
        dm.deleteById(TableNumberOne.class, 10);
        dm.deleteByFromIdToId(TableNumberOne.class, 6, 8);
        dm.deleteContaining(TableNumberOne.class, "firstName", "Nah");

        // Finding
        LinkedHashMap<Object, Object> result = dm.findById(TableNumberOne.class, 0);
        List<LinkedHashMap<Object, Object>> result2 = dm.findByValueContaining(TableNumberOne.class, "firstName", "alice#02");
        List<Map.Entry<Object, Object>> result3 = dm.findAllValuesOfKey(TableNumberOne.class, "firstName");


        // adding to tables.
        dm.addToTable(TableNumberOne.class, dataTest1);
        dm.addToTable(TableNumberTwo.class, noAutoIncrementData);
        dm.addToTable(TableNumberThree.class, freeTableTestData);

        // Other potentially useful snippets
        // Getting last record
        int lastRecordId = JAutoIncrement.get(new SemiClass(TableNumberOne.class)) - 2;
        Logger.info("Last record ID: " + lastRecordId);
        Logger.info("Last record contents: " + FSJJsonReader.read(lastRecordId, new SemiClass(TableNumberOne.class)));
    }

}
