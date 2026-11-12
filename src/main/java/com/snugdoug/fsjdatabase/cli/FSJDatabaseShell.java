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

package com.snugdoug.fsjdatabase.cli;

import com.snugdoug.fsjdatabase.data.*;
import com.snugdoug.fsjdatabase.data.annotation.TableMeta;
import com.snugdoug.fsjdatabase.script.Script;
import com.snugdoug.fsjdatabase.script.ScriptLoader;
import com.snugdoug.fsjdatabase.util.DefaultParserConverter;
import com.snugdoug.fsjdatabase.util.Logger;
import com.snugdoug.fsjdatabase.util.clazz.SemiClass;
import org.jline.reader.*;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

// TODO: Make this a separate project at some point and also fix this to work with the new JSON system

/**
 * The shell for FSJDatabase
 */
public class FSJDatabaseShell {

    /**
     * Create a new instance
     */
    public FSJDatabaseShell() {

    }

    /**
     * The main method for the CLI
     *
     * @param args the arguments
     * @throws IOException due to TerminalBuilder.builder().build(); and history.save(); throwing IOExceptions
     */
    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws IOException {
        Path pathToMeta;

        Terminal term = TerminalBuilder.builder()
                .system(true)
                .type("xterm-256color")
                .build();

        if (Arrays.stream(args).toList().isEmpty()) {
            System.out.println("Please specify a path to a FSJDatabase database as the first argument");
            Runtime.getRuntime().exit(1);
        }

        String pathToDB = args[0];

        String dbType;

        if(!(args.length < 2))
            dbType = args[1];
        else {
            dbType = "--new";
        }

        Path pathToDBFile = Path.of(pathToDB);
        if (!pathToDBFile.toFile().isDirectory() || !pathToDBFile.toFile().exists()) {
            System.out.println("The path " + pathToDB + " is not a directory or doesn't exist");
            Runtime.getRuntime().exit(1);
        }

        boolean newDb = true;

        if (dbType.equals("--old")) {
            newDb = false;
        } else {
            if(!dbType.equals("--new")) {
                System.out.println("Unknown database type param: " + dbType);
                Runtime.getRuntime().exit(1);
            }
        }
        pathToDBFile = pathToDBFile.toAbsolutePath();

        String pathToAutoIncrement = pathToDB + "/data/autoIncrement.fsj";
        String pathToAutoIncrementJ = pathToDB + "/data/autoIncrement.json";

        if (!Path.of(pathToAutoIncrement).toFile().exists() && !Path.of(pathToAutoIncrementJ).toFile().exists()) {
            System.out.println("The given directory is likely not a FSJDatabase directory");
            Runtime.getRuntime().exit(1);
        }

        FSJDatabaseConfiguration conf = new FSJDatabaseConfiguration().setRootFSJDatabaseDirectoryName(pathToDBFile.toFile().getName()).setDirectoryForDatabaseRoot(pathToDBFile.getParent());

        FSJDatabaseInitializer init = new FSJDatabaseInitializer(true);

        DataManager dm = new DataManager();
        JDataManager jdm = new JDataManager();

        Scanner scan = new Scanner(System.in);

        term.writer().println("FSJDatabaseShell - Version 1.0.0");
        term.writer().println("Run 'help' to get a list of commands");

        boolean quit = false;




        History history = new DefaultHistory();
        LineReader reader = LineReaderBuilder.builder().history(history).variable(LineReader.HISTORY_FILE,
                Path.of(System.getProperty("user.home") + "/.fsjdatabaseCLI/history")).variable(LineReader.HISTORY_SIZE, 1000).terminal(term).build();



        reader.getHistory().attach(reader);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                history.save();
            } catch (IOException e) {
                throw new RuntimeException("Failed to save history.");
            }
        }));

        Scanner scanner = new Scanner(System.in);

        if(!newDb) {
        while (true) {
            String line = "it didn't work if you see this";
            try {
                line = reader.readLine("fsjdb > ");
            } catch (UserInterruptException | EndOfFileException e) {
                term.writer().println("Goodbye.");
                history.save();
                Runtime.getRuntime().exit(0);
            }
            Object[] cmdArray = CommandParser.parse(line);
            List<Object> cmdList = new ArrayList<>();
            for (int i = 0; i < cmdArray.length; i++) {
                String cmd = cmdArray[i].toString();

                if (CommandParser.verifyIsNum(cmdArray[i].toString())) {
                    ArrayList<Double> results = CommandParser.parseToDouble(cmd);
                    if (results.getLast() == 0.0d)
                        term.writer().println("Invalid syntax.");
                    else
                        cmdList.add(results.getFirst());
                } else {
                    cmdList.add(cmd);
                }
            }
            cmdArray = cmdList.toArray();

            if (line.equals("quit") || line.equals("exit")) {
                term.writer().println("Goodbye.");
                term.writer().flush();
                break;
            } else if (line.equals("help")) {
                term.writer().println("FSJDatabase Shell Command List");
                term.writer().println("To represent a number (double) value wrap the number in $num[<NUM>]");
                term.writer().println("help - shows this");
                term.writer().println("test-num <num-syntax>");
                term.writer().println("quit|exit - exits shell");
                term.writer().println("findById <table> <id> - gets data by id");
                term.writer().println("findByValueContaining | fvc <table> <key> <value> - gets data by id");
            } else if (cmdArray[0].equals("findById")) {
                if (cmdArray.length < 3) {
                    term.writer().println("Invalid arguments for getById, correct arguments are: findById <table> <id>");
                } else {
                    try {

                        pathToMeta = Path.of(FSJDatabaseStructure.tablesDir() + "/" + cmdArray[1] + "/meta");
                        try {

                            int id = Integer.parseInt(cmdArray[2].toString());
                            term.writer().println("Table: " + cmdArray[1]);
                            term.writer().println("ID: " + cmdArray[2]);

                            LinkedHashMap<Object, Object> getByIdResult = dm.findById(SemiClass.nameOf(cmdArray[1].toString()), id);
                            term.writer().println(getByIdResult);
                            TableMeta getByIdTableMeta = TableMeta.of(pathToMeta);
                        } catch (Exception e) {
                            term.writer().println("Failed to find Table Directory, Meta File, or Id File.");
                            term.writer().flush();
                        }
                    } catch (NumberFormatException e) {
                        term.writer().println("Id argument isn't an integer");
                    }

                }
            }
//            if (cmdArray[0].equals("getByValueContaining")) {
//                if (!(cmdArray.length == 4)) {
//                    term.writer().println("Invalid arguments for getByValueContaining, correct arguments are: getById <table> <key> <value>");
//                } else {
//                        pathToMeta = Path.of(FSJDatabaseStructure.tablesDir + "/" + cmdArray[1] + "/meta");
//                        term.writer().println("Table: " + cmdArray[1]);
//                        term.writer().println("Key: " + cmdArray[2]);
//                        term.writer().println("Value: " + cmdArray[3]);
//
//                        LinkedHashMap<String, String> getByValueContainingResult = data.findByValueContaining(new SemiClass(cmdArray[1]), cmdArray[2], cmdArray[3]);
//                        term.writer().println(getByValueContainingResult);
//                }
//
//            }
            else if (cmdArray[0].equals("findByValueContaining") || cmdArray[0].equals("fvc")) {
                if (!CommandParser.isValid(line, DataManager.class, "findByValueContaining", new DefaultParserConverter())) {
                    term.writer().println("Invalid parameters, valid parameters are: findByValueContaining <table> <key> <value>");
                } else {
                    Path tableMetaPath = Path.of(FSJDatabaseStructure.tablesDir() + "/" + cmdArray[1] + "/meta");

                    if (validTableMetaPath(tableMetaPath)) {

                        Logger.debug("Tests");

                        TableMeta meta = TableMeta.of(tableMetaPath);
                        List<LinkedHashMap<?, ?>> dataByValueContaining = new ArrayList<>();

                        dataByValueContaining = dm.findByValueContaining(SemiClass.nameOf(cmdArray[1].toString()), cmdArray[2].toString(), cmdArray[3].toString());


                        // You know I don't think we need that after I just examined my code...
                        //                    dataByValueContaining.sort(new IdSortedComparator());
                        dataByValueContaining.forEach((hashmap) -> {


                            String formatted = hashmap.get(dm.getTableIdName(SemiClass.nameOf(meta.getTableName()))) + ": " + hashmap.toString()
                                    .replace("{", "")
                                    .replace("}", "");

                            term.writer().println(formatted);

                        });
                    } else {
                        term.writer().println("Failed to find Table Directory, Meta File, or Id File.");
                    }
                }

            } else if (cmdArray[0].equals("test-num")) {
                if (CommandParser.verifyIsNum(cmdArray[1].toString())) {
                    ArrayList<Double> results = CommandParser.parseToDouble(cmdArray[1].toString());
                    if (results.getLast() == 0.0d)
                        term.writer().println("Invalid syntax.");
                    else
                        term.writer().println(results.getFirst());
                } else {
                    term.writer().println("Invalid syntax.");
                }
            } else {
                term.writer().println("Invalid Command '" + line + "', use the command 'help' to see available commands");
            }
        }

        } else {
            while(true) {
                String line = "it didn't work if you see this";
                try {
                    line = reader.readLine("fsjdb > ");
                } catch (UserInterruptException | EndOfFileException e) {
                    term.writer().println("Goodbye.");
                    history.save();
                    Runtime.getRuntime().exit(0);
                }
                Object[] cmdArray = CommandParser.parse(line);
                List<LinkedHashMap<Integer, LinkedHashMap<Object, Object>>> result = new ArrayList<>();
                boolean noOperation = false;
                if(cmdArray[0].toString().equals("load")) {
                    if(cmdArray[1].toString().isBlank()) {
                        System.out.println("Invalid syntax for load, please use load <script_name>");
                    } else {
                        result = jdm.parse(new ScriptLoader(cmdArray[1].toString()).load());
                    }
                } else if(cmdArray[0].toString().equals("help")) {
                    term.writer().println("--- FSJDatabaseShell: Commands ---");
                    term.writer().println("help       | shows this");
                    term.writer().println("load       | loads a script file");
                    term.writer().println("(anything) | parses FSJS directly from shell");
                    noOperation = true;
                } else if(cmdArray[0].toString().equals("exit")) {
                    term.writer().println("Goodbye.");
                    term.writer().flush();
                    break;
                } else {
                    result = jdm.parse(new Script(line));
                }

                if(!noOperation) {
                    term.writer().println("--- RESULTS ---");
                    AtomicReference<Integer> operationNum = new AtomicReference<Integer>(1);
                    result.forEach((o) -> {
                        term.writer().println("\nFind Operation " + operationNum + ":");
                        operationNum.getAndSet(operationNum.get() + 1);
                        o.forEach(((integer, map) -> {
                            term.writer().println(integer + ": " + map);
                        }));
                    });
                }


            }
        }
    }

    /**
     * Checks if a path is valid for the {@link TableMeta} to use
     * @param path the path
     * @return gives true or false for if valid or invalid
     */
    public static boolean validTableMetaPath(Path path) {
        return path.toFile().exists();
    }



}
