package com.snugdoug.fsjdatabase.example;

import java.io.IOException; // Import JDatabase
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.snugdoug.fsjdatabase.database.FSJDatabase;
import com.snugdoug.fsjdatabase.database.createStructure.DBRecord;
import com.snugdoug.fsjdatabase.database.query.Query;

public class Main {

    public static void main(String[] args) throws IOException {
        FSJDatabase.start(MyDataClass.class, AnotherDataClass.class);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", 1);
        userData.put("name", "John Doe");
        userData.put("age", 30);
        FSJDatabase.insert(MyDataClass.class, userData);

        Map<String, Object> productData = new HashMap<>();
        productData.put("product_id", 1);
        productData.put("product_name", "Laptop");
        FSJDatabase.insert(AnotherDataClass.class, productData);

        DBRecord record = new DBRecord()
                .set("id", 1)
                .set("name", "Alice")
                .set("age", 30);

        Map<String, Object> rowData = record.getFields();
        
            FSJDatabase.insert(MyDataClass.class, rowData);
        
        System.out.println(rowData); // {id=1, name=Alice, age=30}

        Scanner scanner = new Scanner(System.in);

        while (true) {  // Loop for multiple queries
            System.out.print("Enter your query (e.g., MyDataClass WHERE name=John Doe AND age>25, or just MyDataClass for all rows, or 'exit' to quit): ");
            String queryString = scanner.nextLine();

            if (queryString.equalsIgnoreCase("exit")) {
                break; // Exit the loop if the user types 'exit'
            }

                Query query = new Query(MyDataClass.class)
                        .select("id", "name")
                        .where("age > 25")
                        .orderBy("name", false);

                List<Map<String, Object>> results = query.execute();
                System.out.println(results);

        }

        scanner.close(); // Close the scanner to prevent resource leaks.
    }
}
