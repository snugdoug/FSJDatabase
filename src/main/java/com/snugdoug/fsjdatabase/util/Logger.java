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

package com.snugdoug.fsjdatabase.util;

import com.snugdoug.fsjdatabase.data.FSJDatabaseConfiguration;
import com.snugdoug.fsjdatabase.exception.logError.AdvancedLogError;
import com.snugdoug.fsjdatabase.exception.logError.LogError;

import java.util.List;

/**
 * Log's messages
 */
public class Logger {

    /**
     * Create a new instance
     */
    public Logger() {

    }

    /**
     * Reset escape code
     */
    public static final String RESET = "\u001B[0m";

    /**
     * Red escape code
     */
    public static final String RED = "\u001B[31m";

    /**
     * Green escape code
     */
    public static final String GREEN = "\u001B[32m";

    /// yellow escape code
    public static final String YELLOW = "\033[93m";

    /// cyan escape code
    public static final String CYAN = "\u001B[36m";

    /**
     * Gets the caller name
     *
     * @param callStackDepth the depth
     * @return the name of the caller
     */
    private static String getCallerClassName(int callStackDepth) {
        return Thread.currentThread().getStackTrace()[callStackDepth].getClassName();
    }

    /**
     * Displays an info message
     * @param text the text
     * @param <T> type param for allowing all types
     */
    public static <T> void info(T text) {
        System.out.println(GREEN + translateText(text) + RESET);
    }

    /**
     * Displays a warn message
     * @param text the text
     * @param <T> type param for allowing all types
     */
    public static <T> void warn(T text) {
        System.out.println(YELLOW + translateText(text) + RESET);
    }

    /**
     * Displays an error message
     * @param text the text
     * @param <T> type param for allowing all types
     */
    public static <T> void error(T text) {
        System.out.println(RED + translateText(text) + RESET);
    }

    /**
     * Displays a {@link LogError} API Message
     * @param error The error
     */
    public static void error(LogError error) {
        System.out.println(RED + translateLogErrorText(error.getError(), error) + RESET);
    }

    /**
     * Displays a {@link LogError} API Message using the advanced version ({@link AdvancedLogError})
     * @param error the error
     */
    public static void error(AdvancedLogError error) {
        System.out.println(RED + translateAdvancedLogErrorText(error) + RESET);
    }



    /**
     * Displays a debug message
     * @param text the text
     * @param <T> type param for allowing all types
     */
    public static <T> void debug(T text) {
        if(FSJDatabaseConfiguration.isDebugEnabled())
            System.out.println(CYAN + translateText(text) + RESET);
    }

    /**
     * Translates text from "Text" to "(caller.method) Text"
     *
     * @param text the text
     * @return the translated String
     * @param <T> the type param for allowing all types
     */
    private static <T> String translateText(T text) {
        String callerName = getCallerClassName(4);
        callerName = callerName.replace(".", " ");
        List<String> caller = List.of(callerName.split(" "));
        return "(" + caller.get(caller.size() - 1) + "." +
                new Throwable().fillInStackTrace().getStackTrace()[2].getMethodName() + ") " + text;
    }

    /**
     * Translates text from "Text" to "(caller.method) {@literal [LOG ERROR]}Text"
     *
     * @param text the text
     * @return the translated String
     * @param <T> the type param for allowing all types
     */
    private static <T> String translateLogErrorText(T text, LogError err) {
        String callerName = getCallerClassName(4);
        callerName = callerName.replace(".", " ");
        List<String> caller = List.of(callerName.split(" "));
        return "(" + caller.get(caller.size() - 1) + "." +
                new Throwable().fillInStackTrace().getStackTrace()[2].getMethodName() + ") " + err.getClass().getSimpleName() + ": " + text;
    }

    /**
     * Translates text from "Text" to "(caller.method) {@literal [LOG ERROR]: }Text\nStacktrace: [STACK TRACE]"
     *
     * @return the translated String
     */
    private static String translateAdvancedLogErrorText(AdvancedLogError err) {
        String callerName = getCallerClassName(4);
        callerName = callerName.replace(".", " ");
        List<String> caller = List.of(callerName.split(" "));
        return "(" + caller.get(caller.size() - 1) + "." +
                new Throwable().fillInStackTrace().getStackTrace()[2].getMethodName() + ") " + err.getClass().getSimpleName() + ": " + err.getMsg() + "" +
                "\nStacktrace: " + err.getStacktrace();
    }

    /**
     * The method for testing the logger
     * @param args arguments
     */
    public static void main(String[] args) {
        Logger.info("Test");
        Logger.warn("Test");
        Logger.error("Test");
        Logger.debug("Test");
    }
}
