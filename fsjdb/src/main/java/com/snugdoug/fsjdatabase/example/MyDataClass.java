package com.snugdoug.fsjdatabase.example;

import com.snugdoug.fsjdatabase.database.DataSource;
import com.snugdoug.fsjdatabase.database.createStructure.Column;
import com.snugdoug.fsjdatabase.database.createStructure.Table;

@DataSource(rootPath = "jdb_data") // Root path for the database
@Table(name = "users")
class MyDataClass {
    @Column(name = "id", primaryKey = true)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "age")
    private int age;
}
