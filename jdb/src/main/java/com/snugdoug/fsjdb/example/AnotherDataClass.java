package com.snugdoug.fsjdb.example;

import com.snugdoug.fsjdb.database.DataSource;
import com.snugdoug.fsjdb.database.createStructure.Column;
import com.snugdoug.fsjdb.database.createStructure.Table;

@DataSource(rootPath = "jdb_data") // Same root path, another table
@Table(name = "products")
class AnotherDataClass {
    @Column(name = "product_id", primaryKey = true)
    private int productId;

    @Column(name = "product_name")
    private String productName;
}
