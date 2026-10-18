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
import com.snugdoug.fsjdatabase.util.DefaultParserConverter;
import com.snugdoug.fsjdatabase.util.Logger;
import com.snugdoug.fsjdatabase.util.clazz.SemiClass;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

// TODO: Make this a separate project at some point
public class FSJDatabaseShell {

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

        Path pathToDBFile = Path.of(pathToDB);
        if (!pathToDBFile.toFile().isDirectory() || !pathToDBFile.toFile().exists()) {
            System.out.println("The path " + pathToDB + " is not a directory or doesn't exist");
            Runtime.getRuntime().exit(1);
        }

        pathToDBFile = pathToDBFile.toAbsolutePath();

        String pathToAutoIncrement = pathToDB + "/data/autoIncrement.fsj";

        if (!Path.of(pathToAutoIncrement).toFile().exists()) {
            System.out.println("The given directory is not a FSJDatabase directory");
            Runtime.getRuntime().exit(1);
        }
        FSJDatabaseConfiguration conf = new FSJDatabaseConfiguration().build(pathToDBFile.toString(), pathToDBFile.getFileName());
        FSJDatabaseInitializer init = new FSJDatabaseInitializer(true);

        JDataManager data = new JDataManager();

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

        while (true) {
            String line = "it didn't work if you see this";
            try {
                line = reader.readLine("fsjdb > ");
            } catch (UserInterruptException e) {
                term.writer().println("Goodbye.");
                history.save();
                Runtime.getRuntime().exit(0);
            }
            String[] cmdArray = CommandParser.parse(line);

            if (line.equals("quit") || line.equals("exit")) {
                term.writer().println("Goodbye.");
                term.writer().flush();
                break;
            }

            else if (line.equals("help")) {
                term.writer().println("FSJDatabase Shell Command List");
                term.writer().println("help - shows this");
                term.writer().println("quit|exit - exits shell");
                term.writer().println("findById <table> <id> - gets data by id");
                term.writer().println("findByValueContaining <table> <key> <value> - gets data by id");
            }

            else if (cmdArray[0].equals("findById")) {
                if (cmdArray.length < 3) {
                    term.writer().println("Invalid arguments for getById, correct arguments are: findById <table> <id>");
                } else {
                    try {

                        pathToMeta = Path.of(FSJDatabaseStructure.tablesDir + "/" + cmdArray[1] + "/meta");
                        try {

                        int id = Integer.parseInt(cmdArray[2]);
                        term.writer().println("Table: " + cmdArray[1]);
                        term.writer().println("ID: " + cmdArray[2]);

                        LinkedHashMap<Object, Object> getByIdResult = data.findById(SemiClass.nameOf(cmdArray[1]), id);
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
            else if(cmdArray[0].equals("findByValueContaining")) {
                if(!CommandParser.isValid(line, DataManager.class, "findByValueContaining", new DefaultParserConverter())) {
                    term.writer().println("Invalid parameters, valid parameters are: findByValueContaining <table> <key> <value>");
                }
                else {
                    Path tableMetaPath = Path.of(FSJDatabaseStructure.tablesDir + "/" + cmdArray[1] + "/meta");

                    if(validTableMetaPath(tableMetaPath)) {

                    TableMeta meta = TableMeta.of(tableMetaPath);

                    List<LinkedHashMap<Object, Object>> dataByValueContaining = data.findByValueContaining(SemiClass.nameOf(cmdArray[1]), cmdArray[2], cmdArray[3]);
                    // You know I don't think we need that after I just examined my code...
//                    dataByValueContaining.sort(new IdSortedComparator());
                    dataByValueContaining.forEach((hashmap) -> {




                        String formatted = hashmap.get(data.getTableIdName(SemiClass.nameOf(meta.getTableName()))) + ": " + hashmap.toString()
                                .replace("{", "")
                                .replace("}", "");

                        term.writer().println(formatted);

                    });
                    } else {
                        term.writer().println("Failed to find Table Directory, Meta File, or Id File.");
                    }
                }
            } else {
                term.writer().println("Invalid Command '" + line + "', use the command 'help' to see available commands");
            }

            // TODO: add the rest of the methods

        }
    }

    public static boolean validTableMetaPath(Path path) {
        return path.toFile().exists();
    }

    public static boolean fullValidityCheck(String line, String methodName, String pathParam) {
        return true;
    }
}
