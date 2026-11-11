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

package com.snugdoug.fsjdatabase.script;

import com.snugdoug.fsjdatabase.data.FSJDatabaseStructure;
import com.snugdoug.fsjdatabase.exception.logError.RecordNotFoundLogError;
import com.snugdoug.fsjdatabase.exception.logError.ScriptNotFoundLogError;
import com.snugdoug.fsjdatabase.util.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * <h2>ScriptLoader</h2>
 * Loads scripts from .fsjs files
 */
public class ScriptLoader {

    private final boolean addExt;
    private final File file;

    public ScriptLoader(String scriptName) {
        this.addExt = !scriptName.endsWith(".fsjs");

        if(!addExt)
            this.file = new File(FSJDatabaseStructure.scriptDir() + "/" + scriptName);
        else
            this.file = new File(FSJDatabaseStructure.scriptDir() + "/" + scriptName + ".fsjs");
    }

    public Script load() {

        List<String> lines;

        try(BufferedReader reader = new BufferedReader(new FileReader(file.getPath()))) {
            lines = reader.lines().toList();
        } catch (Exception e) {
            Logger.error("Failed to create a buffered reader");
            return new Script("");
        }

        return new Script(lines.toArray(String[]::new));
    }
}
