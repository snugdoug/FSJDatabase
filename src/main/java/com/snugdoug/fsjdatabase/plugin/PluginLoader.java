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

import com.snugdoug.fsjdatabase.data.FSJDatabaseStructure;
import com.snugdoug.fsjdatabase.exception.InvalidPluginEndpoint;
import com.snugdoug.fsjdatabase.exception.PluginInitializationException;
import com.snugdoug.fsjdatabase.plugin.anno.Plugin;
import com.snugdoug.fsjdatabase.plugin.anno.PluginEndpoint;
import com.snugdoug.fsjdatabase.plugin.anno.StructureExtension;
import com.snugdoug.fsjdatabase.plugin.anno.StructureFileExtension;
import com.snugdoug.fsjdatabase.util.Logger;
import com.snugdoug.fsjdatabase.util.clazz.SemiClass;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Loads plugins
 */
public class PluginLoader {
    private static HashMap<Class<?>, Set<Field>> classStructFileExtHashMap = new HashMap<>();
    /// A map with the class and structure extensions
    private static HashMap<Class<?>, Set<Field>> classStructExtHashMap = new HashMap<>();

    /// the plugin base classes
    private static Class<?>[] pluginBaseClasses = new Class<?>[] {};
    /// The loaded plugins information
    private static final List<PluginInformation> pluginInformationList = new ArrayList<>();

    /**
     * Loads plugins
     * @param pluginBaseClasses the plugins to load
     */
    public PluginLoader(Class<?>... pluginBaseClasses) {
        AtomicBoolean hadStructureExt = new AtomicBoolean(false);
        PluginLoader.pluginBaseClasses = pluginBaseClasses;
        HashMap<Class<?>, Set<Field>> classStructExtHashMap = new HashMap<>();
        HashMap<Class<?>, Set<Field>> classStructFileExtHashMap = new HashMap<>();
        Arrays.stream(pluginBaseClasses).forEach((clazz) -> {
            Reflections reflect = new Reflections(clazz.getPackageName());

            Set<Class<?>> plugins = reflect.get(Scanners.TypesAnnotated.with(Plugin.class).asClass());


            plugins.forEach((plugin) -> {
                Plugin pluginInfo = plugin.getAnnotation(Plugin.class);
                PluginInformation pluginInformation = new PluginInformation(pluginInfo.pluginName(), pluginInfo.pluginGroup());

                if(pluginInformationList.contains(pluginInformation))
                    throw new PluginInitializationException("Couldn't initialize plugin " + pluginInformation + " as another plugin uses the same group & name!");
                pluginInformationList.add(pluginInformation);
                Logger.debug("Found plugin: " + plugin);


                Field[] fields = plugin.getDeclaredFields();

                Set<Field> structExtFields = parseStructureFields(fields);
                if(!structExtFields.isEmpty())
                    hadStructureExt.set(true);
                classStructExtHashMap.put(plugin, structExtFields);

                Set<Field> structFileExtFields = parseStructureFileFields(fields);
                if(!structFileExtFields.isEmpty())
                    hadStructureExt.set(true);
                classStructFileExtHashMap.put(plugin, structFileExtFields);
            });

            Set<Class<?>> endpoints = reflect.get(Scanners.TypesAnnotated.with(PluginEndpoint.class).asClass());
            endpoints.forEach(endpoint -> {
                PluginEndpoint pluginEndpointDetail = endpoint.getAnnotation(PluginEndpoint.class);
                if(!pluginInformationList.contains(new PluginInformation(pluginEndpointDetail.pluginName(), pluginEndpointDetail.pluginGroup())))
                    throw new InvalidPluginEndpoint("The Plugin that the plugin endpoint references does not exist, endpoint: " + endpoint);

                Field[] fields = endpoint.getDeclaredFields();

                Set<Field> structExtFields = parseStructureFields(fields);
                if(!structExtFields.isEmpty())
                    hadStructureExt.set(true);
                classStructExtHashMap.put(endpoint, structExtFields);

                Set<Field> structFileExtFields = parseStructureFileFields(fields);
                if(!structFileExtFields.isEmpty())
                    hadStructureExt.set(true);
                classStructFileExtHashMap.put(endpoint, structFileExtFields);
            });
        });



        PluginLoader.classStructExtHashMap = classStructExtHashMap;
        PluginLoader.classStructFileExtHashMap = classStructFileExtHashMap;
        if(hadStructureExt.get()) {
            Logger.debug("Regenerating database structure");
            FSJDatabaseStructure.create();
        }
    }

    /**
     * Get the class structure extension hash map
     * @return {@link #classStructExtHashMap}
     */
    public static HashMap<Class<?>, Set<Field>> getClassStructExtHashMap() {
        return classStructExtHashMap;
    }

    /**
     * Gets the plugin base classes
     * @return {@link #pluginBaseClasses}
     */
    public static Class<?>[] getPluginBaseClasses() {
        return pluginBaseClasses;
    }

    /**
     * Get the all plugins information
     * @return {@link #pluginInformationList}
     */
    public static List<PluginInformation> getPluginInformationList() {
        return pluginInformationList;
    }

    /**
     * Get {@link #classStructExtHashMap} as a SemiClass structure extension hashmap
     * @return class structure extension hash map with SemiClass instead of class
     */
    public static HashMap<SemiClass, Set<Field>> getClassStructExtHashMapAsSemi() {
        HashMap<SemiClass, Set<Field>> result = new HashMap<>();
        classStructExtHashMap.forEach((clazz, fields) -> {
            result.put(new SemiClass(clazz), fields);
        });

        return result;
    }

    /**
     * Get the class structure file extension hash map
     * @return {@link #classStructFileExtHashMap}
     */
    public static HashMap<Class<?>, Set<Field>> getClassStructFileExtHashMap() {
        return classStructFileExtHashMap;
    }

    /**
     * Get the structure extension fields from all the fields
     * @param fields the fields
     * @return the structure extension fields
     */
    protected static Set<Field> parseStructureFields(Field[] fields) {
        Set<Field> result = new HashSet<>();
        Arrays.stream(fields).forEach(field -> {
            if(field.isAnnotationPresent(StructureExtension.class)) {
                Logger.debug("Found structure extension: " + field);
                if (!field.getType().equals(String.class)) {
                    throw new PluginInitializationException("StructureExtension's must be a String, not a " + field.getType());
                }
                result.add(field);
            }
            if(field.isAnnotationPresent(StructureFileExtension.class)) {
                Logger.debug("Found structure file extension: " + field);

            }
        });

        return result;
    }

    protected static Set<Field> parseStructureFileFields(Field[] fields) {
        Set<Field> result = new HashSet<>();
        Arrays.stream(fields).forEach(field -> {
            if(field.isAnnotationPresent(StructureFileExtension.class)) {
                Logger.debug("Found structure extension: " + field);
                if (!field.getType().equals(String.class)) {
                    throw new PluginInitializationException("StructureFileExtension's must be a String, not a " + field.getType());
                }
                result.add(field);
            }
        });
        return result;
    }
}
