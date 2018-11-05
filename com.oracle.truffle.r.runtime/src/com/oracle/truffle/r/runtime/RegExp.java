/*
 * Copyright (c) 2014, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.r.runtime;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

/**
 * Support methods for regular expressions.
 */
public class RegExp {

    private enum Predefined {
        alnum("\\p{Alnum}"),
        alpha("\\p{Alpha}"),
        blank("\\p{Blank}"),
        cntrl("\\p{Cntrl}"),
        digit("\\p{Digit}"),
        graph("\\p{Graph}"),
        lower("\\p{Lower}"),
        print("\\p{Print}"),
        punct("\\p{Punct}"),
        space("\\p{Space}"),
        upper("\\p{Upper}"),
        xdigit("\\p{XDigit}");

        private final String replacement;
        private final String syntax;
        private final int syntaxLength;

        Predefined(String replacement) {
            this.replacement = replacement;
            syntax = "[:" + name() + ":]";
            syntaxLength = syntax.length();
        }
    }

    /**
     * R defines some short forms of character classes. E.g. {@code [[:alnum:]]} means
     * {@code [0-9A-Za-z]} but independent of locale and character encoding. So we have to translate
     * these for use with Java regexp. TODO handle the complete set and do locale and character
     * encoding
     */
    @TruffleBoundary
    public static String checkPreDefinedClasses(String pattern) {
        String result = pattern;
        /*
         * this loop replaces "[[]" (illegal in Java regex) with "[\[]", "[\]" with "[\\]" and
         * predefined classes like "[:alpha:]" with "\p{Alpha}".
         */
        boolean withinCharClass = false;
        int parensNesting = 0;
        int i = 0;
        while (i < result.length()) {
            switch (result.charAt(i)) {
                case '(':
                    if (withinCharClass) {
                        result = result.substring(0, i) + '\\' + result.substring(i);
                        i++; // skip the newly inserted '\\'
                    } else {
                        parensNesting++;
                    }
                    break;
                case ')':
                    if (withinCharClass || parensNesting == 0) {
                        result = result.substring(0, i) + '\\' + result.substring(i);
                        i++; // skip the newly inserted '\\'
                    } else {
                        parensNesting--;
                    }
                    break;
                case '\\':
                    if (withinCharClass) {
                        result = result.substring(0, i) + '\\' + result.substring(i);
                        i++; // skip the newly inserted '\\'
                    } else {
                        i++; // skip the next character
                    }
                    break;
                case '[':
                    if (withinCharClass) {
                        boolean predefined = false;
                        if (i + 1 < result.length() && result.charAt(i + 1) == ':') {
                            for (Predefined pre : Predefined.values()) {
                                if (pre.syntax.regionMatches(0, result, i, pre.syntaxLength)) {
                                    result = result.substring(0, i) + pre.replacement + result.substring(i + pre.syntaxLength);
                                    i += pre.replacement.length() - 1;
                                    predefined = true;
                                    break;
                                }
                            }
                        }
                        if (!predefined) {
                            result = result.substring(0, i) + '\\' + result.substring(i);
                            i++;
                        }
                    } else {
                        withinCharClass = true;
                    }
                    break;
                case ']':
                    // Detecting that the current ']' follows the initial '[^' (i.e. excluding
                    // character class)
                    boolean followsCaret = (i == 2 && result.charAt(0) == '[' && result.charAt(1) == '^');
                    // Detecting that the current ']' closes "empty brackets '[]'
                    boolean closingEmptyBrackets = followsCaret || (i > 0 && result.charAt(i - 1) == '[' &&
                                    (i < 2 || result.charAt(i - 2) != '\\'));
                    // To leave a character class open we must already be within some and the
                    // current ']' must be closing empty brackets.
                    // Examples:
                    // ] - there is no character class, so the current ']' has no effect
                    // [\[] - the ']' closes the character class
                    // []\[] - the 1st ']' leaves the character class open, while the 2nd one closes
                    // it
                    // [^]] - the first ']' leaves the character class open
                    withinCharClass &= closingEmptyBrackets;
                    break;
            }
            i++;
        }
        return result;
    }
}
