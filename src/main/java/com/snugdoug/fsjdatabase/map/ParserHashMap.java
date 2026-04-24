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

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.BiFunction;

public class ParserHashMap<K, V> implements Map<K, V> {
    private int size = 0;
    HashMap<K, V> data = new HashMap<>();

    private final TypeToken<K> keyToken = new TypeToken<K>(getClass()) {};
    private final TypeToken<V> valueToken = new TypeToken<V>(getClass()) {};

    private final ParserConverter converter;

    public ParserHashMap(ParserConverter converter) {
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

    @Override
    public boolean containsKey(Object key) { return data.containsKey(key); }

    @Override
    public boolean containsValue(Object value) { return data.containsValue(value); }

    @Override
    public V get(Object key) {  return data.get(key); }

    @Override
    public V put(K key, V value) { size ++; return data.put(key, value); }

    public V put(String unparsed) {
        if(unparsed == null) {
            throw new NullPointerException("The unparsed string is null");
        }

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

    public void putAll(List unparsedList) {
        for(Object unparsed : unparsedList) {
            this.put(unparsed.toString());
        }
    }

    @Override
    public void clear() { size = 0; data.clear(); }

    @Override
    public Set<K> keySet() { return data.keySet(); }

    
    @Override
    public Collection<V> values() { return data.values(); }

    
    @Override
    public Set<Entry<K, V>> entrySet() { return data.entrySet(); }

    
    @Override
    public boolean equals(Object o) { return data.equals(o); }

    
    @Override
    public int hashCode() { return data.hashCode(); }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return Map.super.replace(key, oldValue, newValue);
    }

    public boolean replaceParsed(String newValueUnparsed, K key, V oldValue) {
        Map<K, V> newValue = Parser.parse(newValueUnparsed);

        V finalValue = newValue.get(key);
        return data.replace(key, oldValue, finalValue);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Map.super.replaceAll(function);
    }

    @Override
    public V replace(K key, V value) {
        return Map.super.replace(key, value);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
