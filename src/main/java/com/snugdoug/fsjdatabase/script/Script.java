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

import com.snugdoug.fsjdatabase.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <h2>Script</h2>
 * Creates FSJDBS scripts that can be passed to JDataManager
 * FSJDBS: File Structure Java Database Script
 * <h2>General Format</h2>
 * Main information will be on the wiki once all the basic methods have been added, but here are some examples:
 * SELECT TableNumberOne WHERE [id_field] EQUAL 1
 * SELECT TableNumberOne WHERE [id_field] GREATER 1
 * SELECT TableNumberOne WHERE [id_field] LESS 1
 */
public class Script {
    /**
     * The script lines/data
     */
    private final List<String[]> script = new ArrayList<>();

    /**
     * Create a new script instance
     * @param scriptData the script data
     */
    public Script(String ...scriptData) {
        for (String data : scriptData) {
            Pattern pattern = Pattern.compile("\\$lit\\[\\s*(?<text>.*?)\\s*]");
            Matcher matcher = pattern.matcher(data);

            List<String> finalResult = new ArrayList<>();
            int lastEnd = 0;
            while (matcher.find()) {
                // text before the match (non-literal part)
                String between = data.substring(lastEnd, matcher.start()).trim();
                if (!between.isEmpty()) {
                    finalResult.addAll(Arrays.asList(between.split("\\s+")));
                }
                finalResult.add(matcher.group("text"));
                lastEnd = matcher.end();
            }

            String tail = data.substring(lastEnd).trim();
            if (!tail.isEmpty()) {
                finalResult.addAll(Arrays.asList(tail.split("\\s+")));
            }

            script.add(finalResult.toArray(String[]::new));
            Logger.debug("Script: " + finalResult);
        }
    }

    /**
     * Gets the script
     * @return {@link #script}
     */
    public List<String[]> getScript() {
        return script;
    }
}
