package com.snugdoug.fsjdb.example;

import java.io.IOException; // Import JDatabase
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.snugdoug.fsjdb.database.JDatabase;
import com.snugdoug.fsjdb.database.query.Query;

public class Main {

public static void main(String[] args) throws IOException {
        JDatabase.start(MyDataClass.class, AnotherDataClass.class);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", 1);
        userData.put("name", "John Doe");
        userData.put("age", 30);
        JDatabase.insert(MyDataClass.class, userData);

        Map<String, Object> productData = new HashMap<>();
        productData.put("product_id", 1);
        productData.put("product_name", "Laptop");
        JDatabase.insert(AnotherDataClass.class, productData);

        Scanner scanner = new Scanner(System.in);

        while (true) {  // Loop for multiple queries
            System.out.print("Enter your query (e.g., MyDataClass WHERE name=John Doe AND age>25, or just MyDataClass for all rows, or 'exit' to quit): ");
            String queryString = scanner.nextLine();

            if (queryString.equalsIgnoreCase("exit")) {
                break; // Exit the loop if the user types 'exit'
            }

            try {
                Class<?> tableClass = null;
                String tableName = "";

                if (queryString.contains("WHERE")) {
                    tableName = queryString.substring(0, queryString.indexOf("WHERE")).trim();
                } else {
                    tableName = queryString.trim();
                }


                if (tableName.equals("MyDataClass")) {
                    tableClass = MyDataClass.class;
                } else if (tableName.equals("AnotherDataClass")) {
                    tableClass = AnotherDataClass.class;
                } else {
                    System.out.println("Invalid Table Name");
                    continue; // Go to the next iteration of the loop
                }

                List<Map<String, Object>> queryResults = Query.query(tableClass, queryString);
                System.out.println("Query Results: " + queryResults);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                e.printStackTrace(); // Print the full stack trace for debugging
            }
        }

        scanner.close(); // Close the scanner to prevent resource leaks.
    }
}
