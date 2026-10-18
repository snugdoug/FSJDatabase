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

import java.util.List;

public class Logger {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\033[93m";
    public static final String CYAN = "\u001B[36m";

    private static String getCallerClassName(int callStackDepth) {
        return Thread.currentThread().getStackTrace()[callStackDepth].getClassName();
    }

    public static <T> void info(T text) {
        System.out.println(GREEN + translateText(text) + RESET);
    }

    public static <T> void warn(T text) {
        System.out.println(YELLOW + translateText(text) + RESET);
    }

    public static <T> void error(T text) {
        System.out.println(RED + translateText(text) + RESET);
    }

    public static <T> void debug(T text) {
        if(FSJDatabaseConfiguration.debugEnabled)
            System.out.println(CYAN + translateText(text) + RESET);
    }

    private static <T> String translateText(T text) {
        String callerName = getCallerClassName(4);
        callerName = callerName.replace(".", " ");
        List<String> caller = List.of(callerName.split(" "));
        return "(" + caller.get(caller.size() - 1) + "." +
                new Throwable().fillInStackTrace().getStackTrace()[2].getMethodName() + ") " + text;
    }

    // Test the logger
    public static void main(String[] args) {
        Logger.info("Test");
        Logger.warn("Test");
        Logger.error("Test");
        Logger.debug("Test");
    }
}
