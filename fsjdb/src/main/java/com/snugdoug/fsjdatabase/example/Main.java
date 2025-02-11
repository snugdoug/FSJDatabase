package com.snugdoug.fsjdatabase.example;

import java.io.IOException; // Import JDatabase
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.snugdoug.fsjdatabase.database.FSJDatabase;
import com.snugdoug.fsjdatabase.database.createStructure.DBRecord;
import com.snugdoug.fsjdatabase.database.query.Query;
import com.snugdoug.fsjdatabase.database.schemas.Schema;

public class Main {

    public static void main(String[] args) throws IOException {
        FSJDatabase.start(MyDataClass.class, AnotherDataClass.class);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", 1);
        userData.put("name", "John Doe");
        userData.put("age", 30);
        userData.put("email", "johndoe@fsjdatabase");
        FSJDatabase.insert(MyDataClass.class, userData);

        Map<String, Object> productData = new HashMap<>();
        productData.put("product_id", 1);
        productData.put("product_name", "Laptop");
        FSJDatabase.insert(AnotherDataClass.class, productData);
        Schema userSchema = new Schema()
                .addField("id", Integer.class)
                .addField("name", String.class)
                .addField("email", String.class)
                .addField("age", String.class);

        DBRecord record = new DBRecord()
                .set("id", 2)
                .set("name", "Alice")
                .set("age", "30")
                .set("email", "alice@fsjdatabase.com");

        if(userSchema.validate(record.getFields())) {
            FSJDatabase.insert(MyDataClass.class, record.getFields());
            System.out.println("Schema was validated!");
        }


        Scanner scanner = new Scanner(System.in);

        while (true) {  // Loop for multiple queries
            System.out.print("Enter your query (like MyDataClass WHERE name=John Doe AND age>25, just MyDataClass for all rows, or 'exit' to quit): ");
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
