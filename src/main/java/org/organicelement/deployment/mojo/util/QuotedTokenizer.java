/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.organicelement.deployment.mojo.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Parse on OSGi Manifest clause.
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class QuotedTokenizer {
    /**
     * String to parse.
     */
    String string;

    /**
     * Index.
     */
    int index = 0;

    /**
     * Default separator to use.
     */
    String separators;

    /**
     * Does the tokenizer returns token.
     */
    boolean returnTokens;

    /**
     * Peek.
     */
    String peek;

    /**
     * Separator.
     */
    char separator;

    /**
     * Constructors.
     * @param string : input String.
     * @param separators : separators.
     * @param returnTokens : should the tokenizer return tokens ?
     */
    public QuotedTokenizer(String string, String separators, boolean returnTokens) {
        if (string == null) {
            throw new IllegalArgumentException("string argument must be not null");
        }
        this.string = string;
        this.separators = separators;
        this.returnTokens = returnTokens;
    }

    /**
     * Constructors.
     * Set returnTokens to false.
     * @param string : input String.
     * @param separators : separators
     */
    public QuotedTokenizer(String string, String separators) {
        this(string, separators, false);
    }

    /**
     * Get the next token.
     * @param separators : separators to used.
     * @return : the next token.
     */
    public String nextToken(String separators) {
        separator = 0;
        if (peek != null) {
            String tmp = peek;
            peek = null;
            return tmp;
        }

        if (index == string.length()) { return null; }

        StringBuffer sb = new StringBuffer();

        while (index < string.length()) {
            char c = string.charAt(index++);

            if (Character.isWhitespace(c)) {
                if (index == string.length()) {
                    break;
                } else {
                    continue;
                }
            }

            if (separators.indexOf(c) >= 0) {
                if (returnTokens) {
                    peek = Character.toString(c);
                } else {
                    separator = c;
                }
                break;
            }

            switch (c) {
                case '"':
                case '\'':
                    quotedString(sb, c);
                    break;

                default:
                    sb.append(c);
            }
        }
        String result = sb.toString().trim();
        if (result.length() == 0 && index == string.length()) { return null; }
        return result;
    }

    /**
     * Get the next token.
     * Used the defined separators.
     * @return the next token.
     */
    public String nextToken() {
        return nextToken(separators);
    }

    /**
     * Append the next quote to the given String Buffer.
     * @param sb : accumulator.
     * @param c : quote.
     */
    private void quotedString(StringBuffer sb, char c) {
        char quote = c;
        while (index < string.length()) {
            c = string.charAt(index++);
            if (c == quote) { break; }
            if (c == '\\' && index < string.length() && string.charAt(index + 1) == quote) {
                c = string.charAt(index++);
            }
            sb.append(c);
        }
    }

    public String[] getTokens() {
        return getTokens(0);
    }

    /**
     * Get the list of tokens.
     * @param cnt : array length.
     * @return : the array of token.
     */
    private String[] getTokens(int cnt) {
        String token = nextToken();
        if (token == null) {
            return new String[cnt];
        }

        String[] result = getTokens(cnt + 1);
        result[cnt] = token;
        return result;
    }

    public char getSeparator() {
        return separator;
    }

    /**
     * Get token list.
     * @return the list of token.
     */
    public List<String> getTokenSet() {
        List<String> list = new ArrayList<String>();
        String token = nextToken();
        while (token != null) {
            list.add(token);
            token = nextToken();
        }
        return list;
    }
}
