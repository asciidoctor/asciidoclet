/*
 * Copyright 2013-2024 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.asciidoclet;

/**
 * Path matcher implementation for Ant-style path patterns. This implementation matches URLs using the following rules:
 *
 * * `?`  matches one character
 * * `*`  matches zero or more characters
 * * `**` matches zero or more directories in a path
 *
 * The instances of this class can be configured via its {@link Builder} to:
 *
 * 1. Use a custom path separator. The default is `/` character
 * 2. Ignore character case during comparison. The default is `false`
 * 3. Match start. Determines whether the pattern at least matches as far as the given base path goes,
 * assuming that a full path may then match as well. The default is `false`.
 * 4. Specify whether to trim tokenized paths. The default is `false`
 *
 * The custom path separator & ignoring character case options were inspired by Spring's `AntPathMatcher`
 *
 * Class copied from https://github.com/azagniotov/ant-style-path-matcher
 */
@SuppressWarnings("WeakerAccess")
public class AntPathMatcher {

    private static final char ASTERISK = '*';
    private static final char QUESTION = '?';
    private static final char BLANK = ' ';
    private static final int ASCII_CASE_DIFFERENCE_VALUE = 32;

    private final char pathSeparator;
    private final boolean ignoreCase;
    private final boolean matchStart;
    private final boolean trimTokens;

    private AntPathMatcher(final char pathSeparator, boolean ignoreCase, boolean matchStart, boolean trimTokens) {
        this.pathSeparator = pathSeparator;
        this.ignoreCase = ignoreCase;
        this.matchStart = matchStart;
        this.trimTokens = trimTokens;
    }
    
    /**
     * Checks if a `path` matches with a given `pattern`.
     * Is this method really necessary?
     *
     * @param pattern A pattern to be checked with a `path`.
     * @param path A path to be checked
     * @return `true` if `path` matches `pattern`. `false`, otherwise.
     */
    
    public boolean isMatch(final String pattern, final String path) {
        if (pattern.isEmpty()) {
            return path.isEmpty();
        } else if (path.isEmpty() && pattern.charAt(0) == pathSeparator) {
            if (matchStart) {
                return true;
            } else if (pattern.length() == 2 && pattern.charAt(1) == ASTERISK) {
                return false;
            }
            return isMatch(pattern.substring(1), path);
        }

        final char patternStart = pattern.charAt(0);
        if (patternStart == ASTERISK) {

            if (pattern.length() == 1) {
                return path.isEmpty() || path.charAt(0) != pathSeparator && isMatch(pattern, path.substring(1));
            } else if (doubleAsteriskMatch(pattern, path)) {
                return true;
            }

            int start = 0;
            while (start < path.length()) {
                if (isMatch(pattern.substring(1), path.substring(start))) {
                    return true;
                }
                start++;
            }
            return isMatch(pattern.substring(1), path.substring(start));
        }

        int pointer = skipBlanks(path);

        return !path.isEmpty() && (equal(path.charAt(pointer), patternStart) || patternStart == QUESTION)
                && isMatch(pattern.substring(1), path.substring(pointer + 1));
    }

    private boolean doubleAsteriskMatch(final String pattern, final String path) {
        if (pattern.charAt(1) != ASTERISK) {
            return false;
        } else if (pattern.length() > 2) {
            return isMatch(pattern.substring(3), path);
        }

        return false;
    }

    private int skipBlanks(final String path) {
        int pointer = 0;
        if (trimTokens) {
            while (!path.isEmpty() && pointer < path.length() && path.charAt(pointer) == BLANK) {
                pointer++;
            }
        }
        return pointer;
    }

    private boolean equal(final char pathChar, final char patternChar) {
        if (ignoreCase) {
            return pathChar == patternChar ||
                    ((pathChar > patternChar) ?
                            pathChar == patternChar + ASCII_CASE_DIFFERENCE_VALUE :
                            pathChar == patternChar - ASCII_CASE_DIFFERENCE_VALUE);
        }
        return pathChar == patternChar;
    }
    
    /**
     * A builder class for `AndPathMatcher`.
     * Is this class really necessary?
     * // A comment to suppress warnings during JavaDoc generation by AsciiDoclet.
     */
    public static final class Builder {

        private char pathSeparator = '/';
        private boolean ignoreCase = false;
        private boolean matchStart = false;
        private boolean trimTokens = false;
        
        /**
         * Creates {@link Builder} object.
         */
        public Builder() {

        }
        
        /**
         * Sets `pathSeparator` to this object.
         *
         * @param pathSeparator `pathSeparator` to be set.
         * @return returns this object.
         */
        public Builder withPathSeparator(final char pathSeparator) {
            this.pathSeparator = pathSeparator;
            return this;
        }
        
        /**
         * Sets `ignoreCase` to this object to `true`.
         * @return returns this object.
         */
        public Builder withIgnoreCase() {
            this.ignoreCase = true;
            return this;
        }
        
        /**
         * Sets `matchStart` to this object to `true`.
         * @return returns this object.
         */
        public Builder withMatchStart() {
            this.matchStart = true;
            return this;
        }
        
        /**
         * Sets `trimTokens` to this object to `true`.
         * @return returns this object.
         */
        public Builder withTrimTokens() {
            this.trimTokens = true;
            return this;
        }
        
        /**
         * Creates and returns {@link AntPathMatcher} object.
         * @return A built {@link AntPathMatcher} object.
         */
        public AntPathMatcher build() {
            return new AntPathMatcher(pathSeparator, ignoreCase, matchStart, trimTokens);
        }
    }
}
