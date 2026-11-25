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

import com.snugdoug.fsjdatabase.script.Script;
import com.snugdoug.fsjdatabase.util.clazz.SemiClass;

import java.util.*;

/**
 * The generic Interface for JDataManager
 */
public interface JDataManagerInter {
    boolean addToTable(SemiClass table, LinkedHashMap<Object, Object> dataToAdd);

    boolean addToTable(Class table, LinkedHashMap<Object, Object> dataToAdd);

    boolean deleteById(SemiClass table, int id);

    boolean deleteById(Class table, int id);

    boolean deleteByFromIdToId(SemiClass table, int fromId, int toId);

    boolean deleteByFromIdToId(Class table, int fromId, int toId);

    boolean deleteContaining(SemiClass table, Object key, Object value);

    boolean deleteContaining(Class table, Object key, Object value);

    boolean updateValueById(SemiClass table, int id, Object key, Object value);

    boolean updateValueById(Class table, int id, Object key, Object value);

    boolean updateValueContaining(SemiClass table, Object key, Object value, Object newValue);

    boolean updateValueContaining(Class table, Object key, Object value, Object newValue);

    boolean updateById(SemiClass table, LinkedHashMap<Object, Object> data) throws RuntimeException;

    boolean updateById(Class table, LinkedHashMap<Object, Object> data) throws RuntimeException;

    boolean updateContaining(SemiClass table, LinkedHashMap<Object, Object> searchData, LinkedHashMap<Object, Object> data);

    boolean updateContaining(Class table, LinkedHashMap<Object, Object> searchData, LinkedHashMap<Object, Object> data) throws RuntimeException;

    LinkedHashMap<Object, Object> findById(SemiClass table, int id);

    LinkedHashMap<Integer, LinkedHashMap<Object, Object>> findByFromIdToId(SemiClass table, int fromId, int toId);

    LinkedHashMap<Integer, LinkedHashMap<Object, Object>> findByFromIdToId(Class table, int fromId, int toId);

    LinkedHashMap<Object, Object> findById(Class table, int id);

    List<LinkedHashMap<Object, Object>> findByValueContaining(SemiClass table, Object key, Object value);

    List<LinkedHashMap<Object, Object>> findByValueContaining(Class table, Object key, Object value);

    LinkedList<Map.Entry<Object, Object>> findAllValuesOfKey(SemiClass table, Object key);

    LinkedList<Map.Entry<Object, Object>> findAllValuesOfKey(Class table, Object key);

    Set<String> getTableColumns(SemiClass table, boolean autoIncrement);

    String getTableIdName(SemiClass table) throws RuntimeException;

    boolean recordExists(int id, Class<?> table);

    boolean recordExists(int id, SemiClass table);

    List<SemiClass> getAutoIncrement();

    boolean tableExists(SemiClass table);

    @SuppressWarnings("all")
    abstract List<LinkedHashMap<Integer, LinkedHashMap<Object, Object>>> parse(Script scriptData);
}
