/*
 * Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.r.runtime.data.VectorDataLibrary.RandomAccessIterator;
import com.oracle.truffle.r.runtime.data.VectorDataLibrary.SeqIterator;
import com.oracle.truffle.r.runtime.ops.na.NACheck;

@ExportLibrary(VectorDataLibrary.class)
public class RIntNativeVectorData implements TruffleObject {
    // We need the vector, so that we can easily use the existing NativeDataAccess methods
    // TODO: this field should be replaced with address/length fields and
    // the address/length fields and logic should be removed from NativeMirror
    // including the releasing of the native memory
    private final RIntVector vec;

    public RIntNativeVectorData(RIntVector vec) {
        this.vec = vec;
    }

    @ExportMessage
    public int getLength() {
        return NativeDataAccess.getDataLength(vec, null);
    }

    @ExportMessage
    public RIntNativeVectorData materialize() {
        return this;
    }

    @ExportMessage
    public boolean isWriteable() {
        return true;
    }

    @ExportMessage
    public RIntArrayVectorData copy(@SuppressWarnings("unused") boolean deep) {
        int[] data = NativeDataAccess.copyIntNativeData(vec.getNativeMirror());
        return new RIntArrayVectorData(data, RDataFactory.INCOMPLETE_VECTOR);
    }

    @ExportMessage
    public RIntArrayVectorData copyResized(int newSize, boolean deep, boolean fillNA) {
        return copy(deep).copyResized(newSize, deep, fillNA);
    }

    @ExportMessage
    public int[] getIntDataCopy() {
        return NativeDataAccess.copyIntNativeData(vec.getNativeMirror());
    }

    // TODO: actually use the store in the iterator, which should be just the "address" (Long)

    @ExportMessage
    public SeqIterator iterator() {
        return new SeqIterator(vec, NativeDataAccess.getDataLength(vec, null));
    }

    @ExportMessage
    public RandomAccessIterator randomAccessIterator() {
        return new RandomAccessIterator(vec, NativeDataAccess.getDataLength(vec, null));
    }

    @ExportMessage
    public Object getDataAtAsObject(int index) {
        return getIntAt(index);
    }

    @ExportMessage
    public int getIntAt(int index) {
        return NativeDataAccess.getData(vec, null, index);
    }

    @ExportMessage
    public int getNextInt(SeqIterator it) {
        return NativeDataAccess.getData(vec, null, it.getIndex());
    }

    @ExportMessage
    public int getInt(@SuppressWarnings("unused") RandomAccessIterator it, int index) {
        return NativeDataAccess.getData(vec, null, index);
    }

    @ExportMessage
    public void setIntAt(int index, int value, @SuppressWarnings("unused") NACheck naCheck) {
        NativeDataAccess.setData(vec, null, index, value);
    }

    @ExportMessage
    public void setDataAtAsObject(int index, Object value, @SuppressWarnings("unused") NACheck naCheck) {
        NativeDataAccess.setData(vec, null, index, (int) value);
    }

    @ExportMessage
    public void setNextInt(SeqIterator it, int value, @SuppressWarnings("unused") NACheck naCheck) {
        NativeDataAccess.setData(vec, null, it.getIndex(), value);
    }

    @ExportMessage
    public void setInt(@SuppressWarnings("unused") RandomAccessIterator it, int index, int value, @SuppressWarnings("unused") NACheck naCheck) {
        NativeDataAccess.setData(vec, null, index, value);
    }
}
