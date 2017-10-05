/*
 * Copyright (c) 2014, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.r.runtime.data.model;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.data.RAttributable;
import com.oracle.truffle.r.runtime.data.RList;
import com.oracle.truffle.r.runtime.data.RStringVector;
import com.oracle.truffle.r.runtime.data.RTypedValue;

public interface RAbstractContainer extends RAttributable, RTypedValue {

    boolean isComplete();

    int getLength();

    RAbstractContainer resize(int size);

    boolean hasDimensions();

    int[] getDimensions();

    void setDimensions(int[] newDimensions);

    RTypedValue getNonShared();

    RAbstractContainer materialize();

    Object getDataAtAsObject(int index);

    /**
     * Note: elements inside lists may be in inconsistent state reference counting wise. You may
     * need to put them into consistent state depending on what you use them for, consult the
     * documentation of {@code ExtractListElement}.
     */
    default Object getDataAtAsObject(@SuppressWarnings("unused") Object store, int index) {
        return getDataAtAsObject(index);
    }

    /**
     * Returns an object that could be passed to {@link #getDataAtAsObject(Object, int)} or type
     * specialized versions in concrete vector types. The {@code store} object should contain data
     * necessary for the vector to perform {@link #getDataAtAsObject(Object, int)} and similar
     * methods without any field loads. If {@code store} is saved into a local variable, then the
     * {@code getDataAsObject} overloads with {@code store} parameter do not have to load the
     * vector's fields, but instead read the necessary data from a local variable, which could be
     * beneficial when in loop.
     */
    default Object getInternalStore() {
        return EmptyInternalStore.INSTANCE;
    }

    default RStringVector getNames() {
        CompilerAsserts.neverPartOfCompilation();
        return (RStringVector) getAttr(RRuntime.NAMES_ATTR_KEY);
    }

    default void setNames(RStringVector newNames) {
        CompilerAsserts.neverPartOfCompilation();
        setAttr(RRuntime.NAMES_ATTR_KEY, newNames);
    }

    default RList getDimNames() {
        CompilerAsserts.neverPartOfCompilation();
        return (RList) getAttr(RRuntime.DIMNAMES_ATTR_KEY);
    }

    default void setDimNames(RList newDimNames) {
        CompilerAsserts.neverPartOfCompilation();
        setAttr(RRuntime.DIMNAMES_ATTR_KEY, newDimNames);
    }

    default Object getRowNames() {
        CompilerAsserts.neverPartOfCompilation();
        return getAttr(RRuntime.ROWNAMES_ATTR_KEY);
    }

    default void setRowNames(RAbstractVector rowNames) {
        CompilerAsserts.neverPartOfCompilation();
        setAttr(RRuntime.ROWNAMES_ATTR_KEY, rowNames);
    }

    final class EmptyInternalStore {
        private EmptyInternalStore() {
        }

        public static final EmptyInternalStore INSTANCE = new EmptyInternalStore();
    }
}
