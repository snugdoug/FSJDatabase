package com.snugdoug.fsjdatabase.example;

import com.snugdoug.fsjdatabase.database.DataSource;
import com.snugdoug.fsjdatabase.database.createStructure.Column;
import com.snugdoug.fsjdatabase.database.createStructure.Table;

@DataSource(rootPath = "fsjdb_data") // Same root path, another table
@Table(name = "products")
class AnotherDataClass {
    @Column(name = "product_id", primaryKey = true)
    private int productId;

    @Column(name = "product_name")
    private String productName;
}
