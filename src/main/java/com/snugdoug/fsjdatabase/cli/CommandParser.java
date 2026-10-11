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

package com.snugdoug.fsjdatabase.cli;

import com.google.common.reflect.TypeToken;
import com.snugdoug.fsjdatabase.util.GetTypeOf;
import com.snugdoug.fsjdatabase.util.ParserConverter;
import com.snugdoug.fsjdatabase.util.clazz.SemiClass;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandParser {
    public static String[] parse(String cmd) {
        String[] cmdSplit = cmd.split("\\s");
        List<String> output = new ArrayList<>();
        for (int i = 0; i < cmdSplit.length; i++) {
            String tempStr = cmdSplit[i];
            output.add(i, tempStr);
        }

        return output.toArray(String[]::new);
    }

    public static <T> boolean isValid(String cmd, Class<?> classWithMethod, String methodName, ParserConverter converter) {
        Method finalMethod = null;

        for (Method method : classWithMethod.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                finalMethod = method;
            }
        }

        if (cmd == null || cmd.isEmpty()) {
            return false;
        }

        if (finalMethod == null) {
            throw new RuntimeException("Invalid class method");
        }

        String[] cmdSplit = parse(cmd);

        List<String> cmdSplitRemoval = new ArrayList();
        cmdSplitRemoval = Arrays.stream(cmdSplit).collect(Collectors.toList());

        cmdSplitRemoval.removeFirst();

        cmdSplit = cmdSplitRemoval.toArray(new String[0]);

        Type[] methodTypes = finalMethod.getGenericParameterTypes();

        if (cmdSplit.length != methodTypes.length) {
            return false;
        }

        for (int i = 0; i < cmdSplit.length; i++) {
            try {
                // Looking back at this code I have no idea of WHAT in the world I did here, but it probably work. No touchy
                if (!methodTypes[i].getTypeName().equals(SemiClass.class.getName()) && !methodTypes[i].getTypeName().equals(Class.class.getName())){
                    if (!methodTypes[i].getTypeName().equals(GetTypeOf.get(converter.convert(cmdSplit[i], TypeToken.of(methodTypes[i]))).getTypeName())) {
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
