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

package com.snugdoug.fsjdatabase.plugin;

/**
 * @param name  The plugin name
 * @param group The plugin group (example: com.snugdoug.fsjplugin)
 */
public record PluginInformation(String name, String group) {

    public static final PluginInformation defaultInfo = new PluginInformation("FSJDatabasePlugin", "com.company.defaultplugin");

    /**
     * Creates a new instance
     *
     * @param name  the plugin name
     * @param group the plugin group (example: com.snugdoug.fsjplugin)
     */
    public PluginInformation {
    }

    /**
     * Gets the name of the plugin
     *
     * @return {@link #name}
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Gets the plugin group
     *
     * @return {@link #group}
     */
    @Override
    public String group() {
        return group;
    }

    /**
     * Convert this to a string
     *
     * @return this as a string
     */
    @Override
    public String toString() {
        return name + " " + group;
    }
}
