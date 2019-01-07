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
package com.oracle.truffle.r.runtime.data;

public interface RShareable extends RTypedValue {

    // SHARED_PERMANENT_VAL describes both overflow value and a value that can be set to prevent
    // further updates to ref count (for sharing between different threads) - can potentially be
    // made smaller
    // TODO: a better placement for this constant?
    int SHARED_PERMANENT_VAL = Integer.MAX_VALUE;

    boolean isTemporary();

    boolean isShared();

    RShareable copy();

    default RShareable deepCopy() {
        return copy();
    }

    /**
     * It is invalid to invoke this method on 'shared permanent' shareables.
     */
    void incRefCount();

    /**
     * It is invalid to invoke this method on 'temporary' shareables.
     */
    void decRefCount();

    boolean isSharedPermanent();

    /**
     * Turns off reference counting for this object.
     * 
     * @return {@code this}
     */
    RShareable makeSharedPermanent();

    /**
     * In order to support some hacks of some packages done via C API.
     * 
     * @return {@code this}
     */
    RShareable makeTemporary();

    RTypedValue getNonShared();

}
