/*
 * Copyright (c) 2014, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.runtime.data;

import java.util.Iterator;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.env.REnvironment;

/**
 * Denotes an R type that can have associated attributes, e.g. {@link RVector}, {@link REnvironment}
 *
 * An attribute is a {@code String, Object} pair. The set of attributes associated with an
 * {@link RAttributable} is implemented by the {@link DynamicObject} class.
 */
public interface RAttributable extends RTypedValue {

    /**
     * If the attribute set is not initialized, then initialize it.
     *
     * @return the pre-existing or new value
     */
    DynamicObject initAttributes();

    void initAttributes(DynamicObject newAttributes);

    /**
     * Access all the attributes. Use {@code for (RAttribute a : getAttributes) ... }. Returns
     * {@code null} if not initialized.
     */
    DynamicObject getAttributes();

    /**
     * Get the value of an attribute. Returns {@code null} if not set.
     */
    default Object getAttr(String name) {
        CompilerAsserts.neverPartOfCompilation();
        DynamicObject attr = getAttributes();
        return attr == null ? null : attr.get(name);
    }

    /**
     * Set the attribute {@code name} to {@code value}, overwriting any existing value. This is
     * generic; a class may need to override this to handle certain attributes specially.
     */
    default void setAttr(String name, Object value) {
        CompilerAsserts.neverPartOfCompilation();
        DynamicObject attributes = getAttributes();
        if (attributes == null) {
            attributes = initAttributes();
        }
        attributes.define(name, value);
    }

    default void removeAttr(String name) {
        CompilerAsserts.neverPartOfCompilation();
        DynamicObject attributes = getAttributes();
        if (attributes != null) {
            attributes.delete(name);
            if (attributes.isEmpty()) {
                initAttributes(null);
            }
        }
    }

    default void removeAllAttributes() {
        CompilerAsserts.neverPartOfCompilation();
        DynamicObject attributes = getAttributes();
        if (attributes != null) {
            RAttributesLayout.clear(attributes);
        }
    }

    /**
     * Removes all attributes. If the attributes instance was not initialized, it will stay
     * uninitialized (i.e. {@code null}). If the attributes instance was initialized, it will stay
     * initialized and will be just cleared, unless nullify is {@code true}.
     *
     * @param nullify Some implementations can force nullifying attributes instance if this flag is
     *            set to {@code true}. Nullifying is not guaranteed for all implementations.
     */
    default void resetAllAttributes(boolean nullify) {
        DynamicObject attributes = getAttributes();
        if (attributes != null) {
            RAttributesLayout.clear(attributes);
        }
    }

    default RAttributable setClassAttr(RStringVector classAttr) {
        CompilerAsserts.neverPartOfCompilation();
        if (classAttr == null && getAttributes() != null) {
            getAttributes().delete(RRuntime.CLASS_ATTR_KEY);
        } else {
            setAttr(RRuntime.CLASS_ATTR_KEY, classAttr);
        }
        return this;
    }

    default RStringVector getClassAttr() {
        return (RStringVector) getAttr(RRuntime.CLASS_ATTR_KEY);
    }

    /**
     * Returns {@code true} if and only if the value has a {@code class} attribute added explicitly.
     */
    default boolean isObject() {
        return getClassAttr() != null;
    }

    static void copyAttributes(RAttributable obj, DynamicObject attrs) {
        if (attrs == null) {
            return;
        }
        Iterator<RAttributesLayout.RAttribute> iter = RAttributesLayout.asIterable(attrs).iterator();
        while (iter.hasNext()) {
            RAttributesLayout.RAttribute attr = iter.next();
            obj.setAttr(attr.getName(), attr.getValue());
        }
    }
}
