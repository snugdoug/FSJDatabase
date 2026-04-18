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

import java.nio.file.Path;

/**
 * Contains the logic for configuring stuff for fsjdatabase
 */
public class FSJDatabaseConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(FSJDatabaseConfiguration.class);

    public static Path directoryForDatabaseRoot;
    public static String rootFSJDatabaseDirectoryName;
    protected static boolean didChangeFSJDatabaseRootDirectoryName = false;
    protected static boolean didChangeFSJDatabaseRootPath = false;

    /**
     * Creates the fsjdatabase configuration that the rest of the application uses.
     * <br><br>
     * This method allows for changing the root directory name, doesn't allow for special characters
     *
     * @param rootFSJDatabaseDirectoryName allows for changing the root directory {@code name}
     * @return returns this with its current properties
     */
    public FSJDatabaseConfiguration build(String rootFSJDatabaseDirectoryName) {

        rootFSJDatabaseDirectoryName = rootFSJDatabaseDirectoryName.replaceAll("[-+.^:,]","");

        FSJDatabaseConfiguration.rootFSJDatabaseDirectoryName = rootFSJDatabaseDirectoryName;
        didChangeFSJDatabaseRootDirectoryName = true;

        return this;
    }

    /**
     * Creates the fsjdatabase configuration that the rest of the application uses.
     * <br><br>
     *
     * Near same method as {@link #build(String)}, but also allows for changing where the {@code root directory} is made
     *
     * @param rootFSJDatabaseDirectoryName allows for changing the root directory {@code name}
     * @param directoryForDatabaseRoot allows for changing where the root directory {@code is}
     * @return returns this with its current properties
     */
    public FSJDatabaseConfiguration build(String rootFSJDatabaseDirectoryName, Path directoryForDatabaseRoot) {

        if(!directoryForDatabaseRoot.isAbsolute())
            throw new RuntimeException("Root path must be absolute!");

        rootFSJDatabaseDirectoryName = rootFSJDatabaseDirectoryName.replaceAll("[-+.^:,]","");

        FSJDatabaseConfiguration.directoryForDatabaseRoot = directoryForDatabaseRoot;
        FSJDatabaseConfiguration.rootFSJDatabaseDirectoryName = rootFSJDatabaseDirectoryName;
        FSJDatabaseConfiguration.didChangeFSJDatabaseRootDirectoryName = true;
        didChangeFSJDatabaseRootPath = true;

        return this;
    }

    // getters
    public static Path getDirectoryForDatabaseRoot() {
        return directoryForDatabaseRoot;
    }

    public static String getRootFSJDatabaseDirectoryName() {
        return rootFSJDatabaseDirectoryName;
    }

    public static boolean isDidChangeFSJDatabaseRootDirectoryName() {
        return didChangeFSJDatabaseRootDirectoryName;
    }

    public static boolean isDidChangeFSJDatabaseRootPath() {
        return didChangeFSJDatabaseRootPath;
    }
}
