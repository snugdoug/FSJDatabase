package com.snugdoug.fsjdatabase.data;

import com.snugdoug.fsjdatabase.annotation.Column;
import com.snugdoug.fsjdatabase.annotation.FreeTable;
import com.snugdoug.fsjdatabase.annotation.Id;
import com.snugdoug.fsjdatabase.annotation.Table;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FSJDatabaseInitializer {

    protected static List<Class<?>> tables = List.of();
    protected static List<Class<?>> freeTables = List.of();

    private static final Logger logger = LoggerFactory.getLogger(FSJDatabaseInitializer.class);


    /**
     * This is called to initialize FSJDatabase
     *
     * @param mainClass this gives the base package to {@link #scanForAnnotatedClassesAndPackages(Class)}
     */
    public FSJDatabaseInitializer(Class<?> mainClass) {
        List<Class> results = scanForAnnotatedClassesAndPackages(mainClass);

        DataManager.dataManagerInit(results);
    }

    /**
     * Using Java Reflection Library to get the Annotated Classes
     * NOTE: In the future it is planned to have auto increment for only some tables!
     *
     * @param mainClass this gives the base package to FSJDatabase
     * @return whether to use auto increment or not
     */
    protected List<Class> scanForAnnotatedClassesAndPackages(Class<?> mainClass) {
        Reflections reflections = new Reflections(mainClass.getPackageName());

        List<Class> autoIncrement = new ArrayList<>();
        // find all classes annotated with @Table
        Set<Class<?>> tableClasses = reflections.get(Scanners.TypesAnnotated.with(Table.class).asClass());
        Set<Class<?>> freeTableClasses = reflections.get(Scanners.TypesAnnotated.with(FreeTable.class).asClass());

        tables = tableClasses.stream().toList();
        freeTables = freeTableClasses.stream().toList();

        for (Class<?> clazz : freeTables) {



            if(tables.contains(clazz)) {
                throw new RuntimeException("You can not have the '@table' and '@freeTable' annotated on the same class.");
            }

            if (FSJDatabaseStructure.isTableNotInDatabase(clazz)) {
                FSJDatabaseStructure.addTable(clazz);
            }
        }

        // initialize table classes
        for (Class<?> clazz : tableClasses) {
            if(Arrays.stream(clazz.getDeclaredFields()).toList().isEmpty()) {
                throw new RuntimeException("Table '" + clazz + "' doesn't have any data, the id column is the only needed column for it to be valid");
            }

            if (FSJDatabaseStructure.isTableNotInDatabase(clazz)) {
                FSJDatabaseStructure.addTable(clazz);
            }

            boolean hasAutoIncrement = true;

            // iterate through fields
            for (Field field : clazz.getDeclaredFields()) {

                if (field.isAnnotationPresent(Id.class)) {
                    Id idDetail = field.getAnnotation(Id.class);
                    hasAutoIncrement = idDetail.autoIncrement();

                    if (hasAutoIncrement) {
                        autoIncrement.add(clazz);
                    }

                    if (field.isAnnotationPresent(Column.class)) {
                        Column columnDetail = field.getAnnotation(Column.class);
                        String colName = columnDetail.name().isEmpty() ? field.getName() : columnDetail.name();
                    }
                }
            }
            if(hasAutoIncrement) {
                autoIncrement.add(clazz);
            }
        }

        // initialize freeTable classes
        for (Class<?> clazz : freeTables) {
            if(FSJDatabaseStructure.isTableNotInDatabase(clazz)) {
                FSJDatabaseStructure.addTable(clazz);
            }

           if(Arrays.stream(clazz.getDeclaredFields()).toList().isEmpty()) {
               throw new RuntimeException("Table '" + clazz + "' doesn't have any data, the id column is the only needed column for it to be valid");
           }

           boolean hasAutoIncrement = true;

            // iterate through fields
            for (Field field : clazz.getDeclaredFields()) {


                if (field.isAnnotationPresent(Id.class)) {
                    Id idDetail = field.getAnnotation(Id.class);
                    hasAutoIncrement = idDetail.autoIncrement();
                }

                if (field.isAnnotationPresent(Column.class)) {
                    Column columnDetail = field.getAnnotation(Column.class);
                    String colName = columnDetail.name().isEmpty() ? field.getName() : columnDetail.name();
                }
            }
            if(hasAutoIncrement) {
               autoIncrement.add(clazz);
            }
        }
        
        return autoIncrement;
    }
}