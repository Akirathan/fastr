/*
 * Copyright (c) 2015, 2019, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(InteropLibrary.class)
public abstract class RBaseObject extends RObject implements RTypedValue {

    private int typedValueInfo;

    @SuppressWarnings("static-method")
    @ExportMessage
    public boolean isPointer() {
        return true;
    }

    @ExportMessage
    public long asPointer() {
        return NativeDataAccess.asPointer(this);
    }

    @ExportMessage
    public void toNative() {
        NativeDataAccess.asPointer(this);
    }

    @Override
    public final int getTypedValueInfo() {
        return typedValueInfo;
    }

    @Override
    public final void setTypedValueInfo(int value) {
        typedValueInfo = value;
    }

    @Override
    public final int getGPBits() {
        return (getTypedValueInfo() & GP_BITS_MASK) >>> GP_BITS_MASK_SHIFT;
    }

    @Override
    public final void setGPBits(int gpbits) {
        setTypedValueInfo((getTypedValueInfo() & ~GP_BITS_MASK) | (gpbits << GP_BITS_MASK_SHIFT));
    }

    @Override
    public final boolean isS4() {
        return (getTypedValueInfo() & S4_MASK_SHIFTED) != 0;
    }

    @Override
    public final void setS4() {
        setTypedValueInfo(getTypedValueInfo() | S4_MASK_SHIFTED);
    }

    @Override
    public final void unsetS4() {
        setTypedValueInfo(getTypedValueInfo() & ~S4_MASK_SHIFTED);
    }
}
