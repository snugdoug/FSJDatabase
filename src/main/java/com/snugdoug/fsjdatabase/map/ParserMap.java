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

package com.snugdoug.fsjdatabase.map;

import com.google.common.reflect.TypeToken;
import com.snugdoug.fsjdatabase.data.Parser;
import com.snugdoug.fsjdatabase.util.ParserConverter;

import java.util.*;
import java.util.function.BiFunction;

/**
 * @deprecated Deprecated since FSJDatabase version 1.5.2.<br> In the future, this may be updated to a new class called {@code JsonParserMap}
 * @param <K> The key value
 * @param <V> THe value value
 */
@Deprecated(since="1.5.2")
public class ParserMap<K, V> implements Map<K, V> {

    /**
     * The size
     */
    private int size = 0;

    /// The internal map to manage things
    LinkedHashMap<K, V> data = new LinkedHashMap<>();

    /// the key token
    private final TypeToken<K> keyToken = new TypeToken<K>(getClass()) {};

    /// The value token
    private final TypeToken<V> valueToken = new TypeToken<V>(getClass()) {};

    /// The {@link ParserConverter}
    private final ParserConverter converter;

    /**
     * Create a new instance
     * @param converter the converter to use
     */
    public ParserMap(ParserConverter converter) {
        this.converter = converter;
    }

    /**
     * Gets the size of the map.
     *
     * @return returns {@link #size}
     */
    @Override
    public int size() {
        return size;
    }


    /**
     * Gets if the map is empty or not
     *
     * @return returns true or false if {@link #size} is equal to zero or not
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Checks if the map contains a key
     * @param key key whose presence in this map is to be tested
     * @return whether it succeeded or not
     */
    @Override
    public boolean containsKey(Object key) { return data.containsKey(key); }

    /**
     * Checks if the map contains a value
     * @param value value whose presence in this map is to be tested
     * @return whether it succeeded or not
     */
    @Override
    public boolean containsValue(Object value) { return data.containsValue(value); }

    /**
     * Gets a value of a key
     * @param key the key whose associated value is to be returned
     * @return the value
     */
    @Override
    public V get(Object key) {  return data.get(key); }

    /**
     * Adds something to the map
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the value
     */
    @Override
    public V put(K key, V value) { size ++; return data.put(key, value); }

    /**
     * Puts unparsed data into the map
     * @param unparsed the unparsed data
     * @return the value
     */
    public V put(String unparsed) {
        if(unparsed == null) {
            throw new NullPointerException("The unparsed string is null");
        }

        Map<String, String> rawData = Parser.parse(unparsed);
        Map.Entry<String, String> entry = rawData.entrySet().iterator().next();

        K key = converter.convert(entry.getKey().toString(), keyToken);
        V value = converter.convert(entry.getValue().toString(), valueToken);

        size++;
        return data.put(key, value);
    }
    
    @Override
    public V remove(Object key) { size--; return data.remove(key); }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach((K key, V value) -> this.put(key, value));
    }

    /**
     * Puts all the data from a list with unparsed data into the map
     * @param unparsedList the unparsed list
     */
    public void putAll(List unparsedList) {
        for(Object unparsed : unparsedList) {
                this.put(unparsed.toString());
        }
    }

    /// clear the map
    @Override
    public void clear() { size = 0; data.clear(); }

    /**
     * Gets the keyset
     * @return the keyset
     */
    @Override
    public Set<K> keySet() { return data.keySet(); }


    /**
     * Gets the values
     * @return the values
     */
    @Override
    public Collection<V> values() { return data.values(); }

    /**
     * Gets the entry set
     * @return the entry set
     */
    @Override
    public Set<Entry<K, V>> entrySet() { return data.entrySet(); }


    /**
     * Compares two objects
     * @param o object to be compared for equality with this map
     * @return whether they are equal
     */
    @Override
    public boolean equals(Object o) { return data.equals(o); }

    /**
     * Gets the hashcode
     * @return the hashcode
     */
    @Override
    public int hashCode() { return data.hashCode(); }

    /**
     * Replaces something in the map
     * @param key key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @return if it succeeded
     */
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return Map.super.replace(key, oldValue, newValue);
    }

    /**
     * Replaces something in the map with unparsed data
     * @param key the key
     * @param oldValue the old value
     * @param newValueUnparsed the new unparsed value
     * @return if it succeeded
     */
    public boolean replaceWithParser(K key, V oldValue, String newValueUnparsed) {
        Map<K, V> newValue = Parser.parse(newValueUnparsed);

        V finalValue = newValue.get(key);
        return data.replace(key, oldValue, finalValue);
    }

    /**
     * Replace all using a function
     * @param function the function to apply to each entry
     */
    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Map.super.replaceAll(function);
    }

    /**
     * Replaces something in the map
     * @param key key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @return the value
     */
    @Override
    public V replace(K key, V value) {
        return Map.super.replace(key, value);
    }

    /**
     * Replaces data in a map with unparsed data
     * @param newValueUnparsed the new unparsed value
     * @param key the key
     * @param oldValue the old value
     * @return if it succeeded
     */
    public boolean replaceParsed(String newValueUnparsed, K key, V oldValue) {
        Map<K, V> newValue = Parser.parse(newValueUnparsed);

        V finalValue = newValue.get(key);
        return data.replace(key, oldValue, finalValue);
    }

    /**
     * Converts the map to a list
     * @return the stringified map
     */
    @Override
    public String toString() {
        return data.toString();
    }
}
