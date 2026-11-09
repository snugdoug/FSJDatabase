package com.snugdoug.fsjdatabase.data;

import com.snugdoug.fsjdatabase.annotation.Column;
import com.snugdoug.fsjdatabase.annotation.FreeTable;
import com.snugdoug.fsjdatabase.annotation.Id;
import com.snugdoug.fsjdatabase.annotation.Table;
import com.snugdoug.fsjdatabase.data.annotation.TableMeta;
import com.snugdoug.fsjdatabase.exception.IdNotFoundException;
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

/**
 * The initializer for FSJDatabase<br>
 * If this is not called before things like {@link JDataManager}, it won't be able to work
 */
public class FSJDatabaseInitializer {

    /**
     * If FSJDatabase is initialized
     */
    protected static boolean init;

    /**
     * The regular tables
     */
    protected static List<SemiClass> tables = new ArrayList<>();

    /**
     * The free tables
     */
    protected static List<SemiClass> freeTables = new ArrayList<>();

    /**
     * This is called to initialize FSJDatabase
     *
     * @param mainClass this gives the base package to {@link #scanForAnnotatedClassesAndPackages(Class)}
     */
    @SuppressWarnings("deprecation")
    public FSJDatabaseInitializer(Class<?> mainClass) {
        if(init) {
            Logger.warn("Calling initializer twice may allow for hot switching databases, though unknown bugs might appear.\nUse at your own risk.");
            init = false;
            tables = new ArrayList<>();
            freeTables = new ArrayList<>();
        }


        List<SemiClass> results = scanForAnnotatedClassesAndPackages(mainClass);

        init = true;
        DataManager.dataManagerInit(results);
        JDataManager.jDataManagerInit(results);
    }

    /**
     * The method to load by directory. Please only use true when using this.<br>
     * If you intend to load by class use {@link #FSJDatabaseInitializer(Class mainclass)}
     * @param useDirectory tells fsjdatabase whether to use directory loading or not.
     */
    @SuppressWarnings("deprecation")
    public FSJDatabaseInitializer(boolean useDirectory) {
        List<SemiClass> results;
        if(useDirectory) {
            if(init) {
                Logger.warn("Calling initializer twice may allow for hot switching databases, though unknown bugs might appear.\nUse at your own risk.");
                init = false;
                tables = new ArrayList<>();
                freeTables = new ArrayList<>();
            }

            if (FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName && !FSJDatabaseConfiguration.didChangeFSJDatabaseRootPath)
                results = scanForAnnotatedClassesAndPackages();
            else if (FSJDatabaseConfiguration.didChangeFSJDatabaseRootPath && FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName)
                results = scanForAnnotatedClassesAndPackages();
            else if (FSJDatabaseConfiguration.didChangeFSJDatabaseRootPath)
                results = scanForAnnotatedClassesAndPackages();
            else {
                results = scanForAnnotatedClassesAndPackages();
            }
            init = true;
            DataManager.dataManagerInit(results);
            JDataManager.jDataManagerInit(results);
        } else {
            Logger.error("If you are not using directory loading, please use the regular method (FSJDatabaseInitializer(Class<?> mainClass))");
            Runtime.getRuntime().exit(1);
        }
    }

    /**
     * Using Java Reflection Library to get the Annotated Classes
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
            boolean tableHasIdField = false;
            if(Arrays.stream(clazz.getDeclaredFields()).toList().isEmpty()) {
                throw new RuntimeException("Table '" + clazz + "' doesn't have any data, the id column is the only needed column for it to be valid");
            }

            boolean hasAutoIncrement = true;

            // iterate through fields
            for (Field field : clazz.getDeclaredFields()) {

                if (field.isAnnotationPresent(Id.class)) {
                    tableHasIdField = true;
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

            if(!tableHasIdField) {
                throw new IdNotFoundException(clazz);
            }

            if(hasAutoIncrement) {
                autoIncrement.add(SemiClass.nameOf(clazz.getSimpleName()));
            }

            if (FSJDatabaseStructure.isTableNotInDatabase(clazz) | FSJDatabaseStructure.isTableNotFoundInAutoIncrement(clazz)) {
                FSJDatabaseStructure.addTable(clazz, autoIncrement.contains(new SemiClass(clazz)));
            }
        }

        // initialize freeTable classes
        for (Class clazz : freeTableClassesUnparsed) {
           boolean tableHasIdField = false;

           if(Arrays.stream(clazz.getDeclaredFields()).toList().isEmpty()) {
               throw new RuntimeException("Table '" + clazz + "' doesn't have any data, the id column is the only needed column for it to be valid");
           }

           boolean hasAutoIncrement = true;

            // iterate through fields
            for (Field field : clazz.getDeclaredFields()) {


                if (field.isAnnotationPresent(Id.class)) {
                    tableHasIdField = true;
                    Logger.debug("Found table with autoIncrement: " + field.getDeclaringClass().getSimpleName());
                    Id idDetail = field.getAnnotation(Id.class);
                    hasAutoIncrement = idDetail.autoIncrement();
                }

                if (field.isAnnotationPresent(Column.class)) {
                    Column columnDetail = field.getAnnotation(Column.class);
                    String colName = columnDetail.name().isEmpty() ? field.getName() : columnDetail.name();
                }
            }
            if(!tableHasIdField) {
                throw new IdNotFoundException(clazz);
            }
            if(hasAutoIncrement) {
                autoIncrement.add(SemiClass.nameOf(clazz.getSimpleName()));
            }
            if(FSJDatabaseStructure.isTableNotInDatabase(clazz) | FSJDatabaseStructure.isTableNotFoundInAutoIncrement(clazz)) {
                FSJDatabaseStructure.addTable(clazz, autoIncrement.contains(new SemiClass(clazz)));

            }
        }

        FSJDatabaseStructure.create();

        return autoIncrement;
    }

    /**
     * Much more complex scanning than other method, as the library doesn't have access to the classes.<br>
     * This will not work on a brand new database, as it requires the meta to be written by the main method of initializing.<br>
     * I physically cannot get it to run with just the directory as it needs the classes to write the meta and other fields.
     *
     * @return returns auto incremented tables
     */
    protected List<SemiClass> scanForAnnotatedClassesAndPackages() {
        FSJDatabaseStructure.create();


        if(!(new File(FSJDatabaseStructure.dataDir + "/autoIncrement.json").exists())) {
            Logger.error("Please run the Class initializing method first, as it will break without the proper meta and other files being written by it.");
            Runtime.getRuntime().exit(1);
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

                                    if(!meta.isFreeTable())
                                        tables.add(SemiClass.nameOf(meta.getTableName()));
                                    else
                                        freeTables.add(SemiClass.nameOf(meta.getTableName()));

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

        FSJDatabaseStructure.create();

        return autoIncrement;
    }

    /**
     * Gets the regular tables
     * @return {@link #tables}
     */
    public static List<SemiClass> getTables() {
        return tables;
    }

    /**
     * Gets the free tables
     * @return {@link #freeTables}
     */
    public static List<SemiClass> getFreeTables() {
        return freeTables;
    }

    /**
     * Gets if FSJDatabase is initialized
     * @return {@link #init}
     */
    public static boolean isInit() {
        return init;
    }
}