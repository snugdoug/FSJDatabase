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

package com.snugdoug.fsjdatabase;

import com.snugdoug.fsjdatabase.annotation.Column;
import com.snugdoug.fsjdatabase.annotation.Id;
import com.snugdoug.fsjdatabase.annotation.Table;
import com.snugdoug.fsjdatabase.data.DataManager;
import com.snugdoug.fsjdatabase.data.FSJDatabaseInitializer;
import com.snugdoug.fsjdatabase.data.FSJDatabaseStructure;
import com.snugdoug.fsjdatabase.examples.TableNumberTwo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Testing {
    public static void main(String[] args) {
        FSJDatabaseInitializer fsjdb = new FSJDatabaseInitializer(Testing.class);
        DataManager dataManager = new DataManager();

//        try {
//            dataManager.addToTable(Map.of("1", "2"), TableNumberTwo.class);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        dataManager.

    }

//    @Table(name = "TableNumberOne")
//    public class TableNumberOne {
//
//        @Column(name = "firstName")
//        String firstName;
//
//        @Column(name = "lastName")
//        String lastName;
//
//        @Column(name = "email")
//        String email;
//
//    }
//    @Table(name = "TableNumberOne")
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
