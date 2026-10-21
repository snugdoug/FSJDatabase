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

package com.snugdoug.fsjdatabaseTesting;import com.snugdoug.fsjdatabase.data.FSJDatabaseConfiguration;
import com.snugdoug.fsjdatabase.data.FSJDatabaseInitializer;
import com.snugdoug.fsjdatabase.data.FSJDatabaseStructure;
import com.snugdoug.fsjdatabase.data.JDataManager;
import com.snugdoug.fsjdatabase.util.Logger;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonFSJTest {
    private static JDataManager dm;
    private static FSJDatabaseInitializer init;
    private static FSJDatabaseConfiguration conf;
    @BeforeAll
    static void init() {
        conf =  new FSJDatabaseConfiguration().build("FSJDB_TestingDB", true);
        init = new FSJDatabaseInitializer(JsonFSJTest.class);
        dm = new JDataManager();
    }
    @AfterEach
    void tearDown() {

    }



    @Test
    @DisplayName("Test adding data")
    void addingData() {
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

        assertTrue(dm.addToTable(TableNumberOne.class, dataTest1));
        assertTrue(dm.addToTable(TableNumberThree.class, noAutoIncrementData));
        assertTrue(dm.addToTable(TableNumberTwo.class, freeTableTestData));
    }
    @Test
    @DisplayName("Test updating data")
    void testUpdatingData() {
        // value by id
        assertTrue(dm.updateValueById(TableNumberOne.class, 1, "firstName", "Jim"));
        assertTrue(dm.updateValueById(TableNumberThree.class, 0, "firstName", "Jimmy"));

        // value by containing
        assertTrue(dm.updateValueContaining(TableNumberOne.class, "firstName", "Jimmy", "NotAlice"));

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

        assertTrue(dm.updateById(TableNumberOne.class, updateData));
        assertTrue(dm.updateById(TableNumberOne.class, updateData2));


        // updating by containing (kinda equals though)
        LinkedHashMap<Object, Object> newUpdateData = new LinkedHashMap<>();
        updateData.put("firstName", "Nah");
        updateData.put("lastName", "RatherNotProvideThis");
        updateData.put("emailName", "nottoday@fakegmail.com");
        updateData.put("id", "5");

        assertTrue(dm.updateContaining(TableNumberOne.class, updateData2, newUpdateData));
    }
    @Test
    @DisplayName("Test deleting data")
    void testDeletingData() {
        // Deleting
        assertTrue(dm.deleteById(TableNumberOne.class, 10));
        assertTrue(dm.deleteByFromIdToId(TableNumberOne.class, 6, 8));
        assertTrue(dm.deleteContaining(TableNumberOne.class, "firstName", "Nah"));

    }
    @Test
    @DisplayName("Migration Test")
    @Disabled(value = "migrationTest ignored. Reason: Not testing migration during main cycle.")
    void migrationTest() {
        assertTrue(FSJDatabaseStructure.update());
    }


    @Test
    @DisplayName("Test finding data")
    @Disabled(value = "testFindingData ignored. Reason: Not finished.")
    void testFindingData() {
        // Finding
        LinkedHashMap<Object, Object> result = dm.findById(TableNumberOne.class, 0);
        List<LinkedHashMap<Object, Object>> result2 = dm.findByValueContaining(TableNumberOne.class, "firstName", "alice#02");
        List<Map.Entry<Object, Object>> result3 = dm.findAllValuesOfKey(TableNumberOne.class, "firstName");
    }
}
