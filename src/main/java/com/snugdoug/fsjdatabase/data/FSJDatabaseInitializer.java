package com.snugdoug.fsjdatabase.data;

import com.snugdoug.fsjdatabase.annotation.Column;
import com.snugdoug.fsjdatabase.annotation.FreeTable;
import com.snugdoug.fsjdatabase.annotation.Id;
import com.snugdoug.fsjdatabase.annotation.Table;
import com.snugdoug.fsjdatabase.data.annotation.TableMeta;
import com.snugdoug.fsjdatabase.util.Logger;
import com.snugdoug.fsjdatabase.util.clazz.SemiClass;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class FSJDatabaseInitializer {

    protected static boolean init;
    protected static List<SemiClass> tables = new ArrayList<>();
    protected static List<SemiClass> freeTables = new ArrayList<>();

    /**
     * This is called to initialize FSJDatabase
     *
     * @param mainClass this gives the base package to {@link #scanForAnnotatedClassesAndPackages(Class)}
     */
    @SuppressWarnings("deprecation")
    public FSJDatabaseInitializer(Class<?> mainClass) {
        List<SemiClass> results = scanForAnnotatedClassesAndPackages(mainClass);

        init = true;
        DataManager.dataManagerInit(results);
        JDataManager.jDataManagerInit(results);
    }

    public FSJDatabaseInitializer(boolean useDirectory) {
        if(FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName)
            scanForAnnotatedClassesAndPackages(FSJDatabaseConfiguration.getDirectoryForDatabaseRoot());
        else {
            scanForAnnotatedClassesAndPackages(Path.of("FSJDatabase"));
        }
    }

    /**
     * Using Java Reflection Library to get the Annotated Classes
     * NOTE: In the future it is planned to have auto increment for only some tables!
     *
     * @param mainClass this gives the base package to FSJDatabase
     * @return whether to use auto increment or not
     */
    protected List<SemiClass> scanForAnnotatedClassesAndPackages(Class<?> mainClass) {
        Reflections reflections = new Reflections(mainClass.getPackageName());

        // find all classes annotated with @Table
        Set<Class<?>> tableClassesUnparsed = reflections.get(Scanners.TypesAnnotated.with(Table.class).asClass());
        Set<Class<?>> freeTableClassesUnparsed = reflections.get(Scanners.TypesAnnotated.with(FreeTable.class).asClass());

        List<SemiClass> autoIncrement = new ArrayList<>();

        Set<SemiClass> tableClasses = new HashSet<>();
        Set<SemiClass> freeTableClasses = new HashSet<>();

        for(Class clazz : tableClassesUnparsed) {
            Logger.debug("Found regular table class: " + new SemiClass(clazz));
            tableClasses.add(new SemiClass(clazz));
        }
        for(Class clazz : freeTableClassesUnparsed) {
            Logger.debug("Found free table class: " + new SemiClass(clazz));
            freeTableClasses.add(new SemiClass(clazz));
        }


        tables = tableClasses.stream().toList();
        freeTables = freeTableClasses.stream().toList();

        for (Class<?> clazz : freeTableClassesUnparsed) {
            if(tables.contains(clazz)) {
                throw new RuntimeException("You can not have the '@table' and '@freeTable' annotated on the same class.");
            }
        }

        // initialize table classes
        for (Class<?> clazz : tableClassesUnparsed) {
            if(Arrays.stream(clazz.getDeclaredFields()).toList().isEmpty()) {
                throw new RuntimeException("Table '" + clazz + "' doesn't have any data, the id column is the only needed column for it to be valid");
            }

            boolean hasAutoIncrement = true;

            // iterate through fields
            for (Field field : clazz.getDeclaredFields()) {

                if (field.isAnnotationPresent(Id.class)) {
                    Id idDetail = field.getAnnotation(Id.class);
                    hasAutoIncrement = idDetail.autoIncrement();

                    if (hasAutoIncrement) {
                        Logger.debug("Found table with autoIncrement: " + field.getDeclaringClass().getSimpleName());
                        autoIncrement.add(SemiClass.nameOf(clazz.getSimpleName()));
                    }

                    if (field.isAnnotationPresent(Column.class)) {
                        Logger.debug("Found column " + field.getName() + " in table class " + field.getDeclaringClass().getSimpleName());
                        Column columnDetail = field.getAnnotation(Column.class);
                        String colName = columnDetail.name().isEmpty() ? field.getName() : columnDetail.name();
                    }
                }
            }
            if(hasAutoIncrement) {
                autoIncrement.add(SemiClass.nameOf(clazz.getSimpleName()));
            }

            if (FSJDatabaseStructure.isTableNotInDatabase(clazz)) {
                FSJDatabaseStructure.addTable(clazz, autoIncrement.contains(clazz));
            }
        }

        // initialize freeTable classes
        for (Class clazz : freeTableClassesUnparsed) {

           if(Arrays.stream(clazz.getDeclaredFields()).toList().isEmpty()) {
               throw new RuntimeException("Table '" + clazz + "' doesn't have any data, the id column is the only needed column for it to be valid");
           }

           boolean hasAutoIncrement = true;

            // iterate through fields
            for (Field field : clazz.getDeclaredFields()) {


                if (field.isAnnotationPresent(Id.class)) {
                    Logger.debug("Found table with autoIncrement: " + field.getDeclaringClass().getSimpleName());
                    Id idDetail = field.getAnnotation(Id.class);
                    hasAutoIncrement = idDetail.autoIncrement();
                }

                if (field.isAnnotationPresent(Column.class)) {
                    Column columnDetail = field.getAnnotation(Column.class);
                    String colName = columnDetail.name().isEmpty() ? field.getName() : columnDetail.name();
                }
            }
            if(hasAutoIncrement) {
                autoIncrement.add(SemiClass.nameOf(clazz.getSimpleName()));
            }
            if(FSJDatabaseStructure.isTableNotInDatabase(clazz)) {
                FSJDatabaseStructure.addTable(clazz, autoIncrement.contains(clazz));
            }
        }
        return autoIncrement;
    }

    /**
     * Much more complex scanning than other method, as the library doesn't have access to the classes.
     *
     * @param directory the directory to scan
     * @return returns auto incremented tables
     */
    protected List<SemiClass> scanForAnnotatedClassesAndPackages(Path directory) {
        try {
            FSJDatabaseStructure.create();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<SemiClass> autoIncrement = new ArrayList<>();
        try {
            Files.newDirectoryStream(Path.of(FSJDatabaseStructure.tablesDir)).forEach(dir -> {

                try {

                    Stream<Path> paths = Files.walk(Paths.get(FSJDatabaseStructure.tablesDir));
                    paths
                            .filter(Files::isRegularFile).
                            forEach(file -> {
                                if(file.toFile().getName().equals("meta")) {
                                    TableMeta meta = TableMeta.of(file);

                                    tables.add(new SemiClass(meta.getTableName()));

                                    if(meta.isAutoIncrement()) {
                                        autoIncrement.add(SemiClass.nameOf(meta.getTableName()));
                                    }
                                }
                            });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(tables.get(0));
        return autoIncrement;
    }
}