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

package com.snugdoug.fsjdatabase.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles making files and directories
 */
public class FileManager {
    private static final Logger logger = LoggerFactory.getLogger(FileManager.class);

    /**
     * Creates a {@code file}.
     *
     * @param path the path it uses to create the file
     */
    public static void createFile(String path) {
        Path finalPath = Paths.get(path).toAbsolutePath();
        File file = new File(finalPath.toString());

        try {
            if(!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create new file, stacktrace:\n " + e);
        }
    }

    /**
     * Create a {@code single} directory.
     *
     * @param path the path it uses to create the directory
     */
    public static void mkdir(String path) {
        File file = new File(path);
        if(file.mkdir()) {
            // possible future logic
        }
    }

    /**
     * Creates {@code multiple} directories from a single path.
     *
     * @param path the path to create the directory or directories leading up the path
     */
    public static void mkdirs(String path) {
        File file = new File(path);
        if(file.mkdirs()) {
            // possible future logic
        }
    }

    /**
     * Deletes a file, has {@code safeguards} that make sure it's in the fsjdatabase {@code root} and not outside the root directory.<br>
     * The reason the safeguards exist is so with accidental usage it doesn't delete something {@code sensitive} or {@code confidental}.
     *
     * @param path the path to the file to delete
     * @return returns whether it deleted it successfully or not
     */
    public static boolean deleteFsjdbFile(String path) {
        File file = new File(path);

        if(!path.contains(FSJDatabaseConfiguration.rootFSJDatabaseDirectoryName) && FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName) {
            throw new SecurityException("For safety, deleting the file with the path: '" + path + "' won't be deleted as it is not in the FSJDatabase root folder.");
        } else {
            if (!path.contains("FSJDatabase") && !FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName)
                throw new SecurityException("For safety, deleting the file with the path: '" + path + "' won't be deleted as it is not in the FSJDatabase root folder.");
        }

        return file.delete();// possible future logic
    }
}
