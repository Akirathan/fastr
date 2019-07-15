/*
 * Copyright (c) 2013, 2019, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Arrays;

import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.r.runtime.RType;
import com.oracle.truffle.r.runtime.Utils;
import com.oracle.truffle.r.runtime.data.closures.RClosures;
import com.oracle.truffle.r.runtime.data.model.RAbstractContainer;
import com.oracle.truffle.r.runtime.data.model.RAbstractRawVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;
import com.oracle.truffle.r.runtime.data.nodes.FastPathVectorAccess.FastPathFromRawAccess;
import com.oracle.truffle.r.runtime.data.nodes.SlowPathVectorAccess.SlowPathFromRawAccess;
import com.oracle.truffle.r.runtime.data.nodes.VectorAccess;
import com.oracle.truffle.r.runtime.ops.na.NACheck;
import com.oracle.truffle.r.runtime.data.RSharingAttributeStorage.Shareable;

public final class RRawVector extends RAbstractRawVector implements RMaterializedVector, Shareable {

    private byte[] data;

    RRawVector(byte[] data) {
        super(true);
        this.data = data;
        assert RAbstractVector.verifyVector(this);
    }

    RRawVector(byte[] data, int[] dims, RStringVector names, RList dimNames) {
        this(data);
        initDimsNamesDimNames(dims, names, dimNames);
    }

    private RRawVector() {
        super(true);
    }

    @Override
    public boolean isMaterialized() {
        return true;
    }

    static RRawVector fromNative(long address, int length) {
        RRawVector result = new RRawVector();
        NativeDataAccess.asPointer(result);
        NativeDataAccess.setNativeContents(result, address, length);
        return result;
    }

    @Override
    public RAbstractVector castSafe(RType type, ConditionProfile isNAProfile, boolean keepAttributes) {
        switch (type) {
            case Raw:
                return this;
            case Integer:
                return RClosures.createToIntVector(this, keepAttributes);
            case Double:
                return RClosures.createToDoubleVector(this, keepAttributes);
            case Complex:
                return RClosures.createToComplexVector(this, keepAttributes);
            case Character:
                return RClosures.createToStringVector(this, keepAttributes);
            default:
                return null;
        }
    }

    @Override
    public byte[] getInternalManagedData() {
        return data;
    }

    @Override
    public byte[] getInternalStore() {
        return data;
    }

    @Override
    public byte getRawDataAt(int index) {
        return NativeDataAccess.getData(this, data, index);
    }

    @Override
    public void setRawDataAt(Object store, int index, byte value) {
        assert data == store;
        NativeDataAccess.setData(this, (byte[]) store, index, value);
    }

    @Override
    protected RRawVector internalCopy() {
        if (data != null) {
            return new RRawVector(Arrays.copyOf(data, data.length));
        } else {
            return new RRawVector(NativeDataAccess.copyByteNativeData(getNativeMirror()));
        }
    }

    @Override
    public int getLength() {
        return NativeDataAccess.getDataLength(this, data);
    }

    @Override
    public int getTrueLength() {
        return NativeDataAccess.getTrueDataLength(this);
    }

    @Override
    public void setLength(int l) {
        try {
            NativeDataAccess.setDataLength(this, data, l);
        } finally {
            data = null;
            complete = false;
        }
    }

    @Override
    public void setTrueLength(int l) {
        NativeDataAccess.setTrueDataLength(this, l);
    }

    @Override
    public byte[] getDataCopy() {
        if (data != null) {
            return Arrays.copyOf(data, data.length);
        } else {
            return NativeDataAccess.copyByteNativeData(getNativeMirror());
        }
    }

    @Override
    public byte[] getReadonlyData() {
        if (data != null) {
            return data;
        } else {
            return NativeDataAccess.copyByteNativeData(getNativeMirror());
        }
    }

    @Override
    public RRawVector materialize() {
        return this;
    }

    private RRawVector updateDataAt(int index, RRaw value) {
        assert !this.isShared();
        NativeDataAccess.setData(this, data, index, value.getValue());
        return this;
    }

    @Override
    public RRawVector updateDataAtAsObject(int i, Object o, NACheck naCheck) {
        return updateDataAt(i, (RRaw) o);
    }

    private byte[] copyResizedData(int size, boolean fillNA) {
        byte[] localData = getReadonlyData();
        byte[] newData = Arrays.copyOf(localData, size);
        if (!fillNA) {
            assert localData.length > 0 : "cannot call resize on empty vector if fillNA == false";
            // NA is 00 for raw
            for (int i = localData.length, j = 0; i < size; ++i, j = Utils.incMod(j, localData.length)) {
                newData[i] = data[j];
            }
        }
        return newData;
    }

    @Override
    protected RRawVector internalCopyResized(int size, boolean fillNA, int[] dimensions) {
        return RDataFactory.createRawVector(copyResizedData(size, fillNA), dimensions);
    }

    @Override
    public void transferElementSameType(int toIndex, RAbstractVector fromVector, int fromIndex) {
        NativeDataAccess.setData(this, data, toIndex, ((RAbstractRawVector) fromVector).getRawDataAt(fromIndex));
    }

    public long allocateNativeContents() {
        try {
            return NativeDataAccess.allocateNativeContents(this, data, getLength());
        } finally {
            data = null;
            complete = false;
        }
    }

    private static final class FastPathAccess extends FastPathFromRawAccess {

        FastPathAccess(RAbstractContainer value) {
            super(value);
        }

        @Override
        protected byte getRawImpl(AccessIterator accessIter, int index) {
            return hasStore ? ((byte[]) accessIter.getStore())[index] : NativeDataAccess.getRawNativeMirrorData(accessIter.getStore(), index);
        }

        @Override
        protected void setRawImpl(AccessIterator accessIter, int index, byte value) {
            if (hasStore) {
                ((byte[]) accessIter.getStore())[index] = value;
            } else {
                NativeDataAccess.setNativeMirrorRawData(accessIter.getStore(), index, value);
            }
        }
    }

    @Override
    public VectorAccess access() {
        return new FastPathAccess(this);
    }

    private static final SlowPathFromRawAccess SLOW_PATH_ACCESS = new SlowPathFromRawAccess() {
        @Override
        protected byte getRawImpl(AccessIterator accessIter, int index) {
            RRawVector vector = (RRawVector) accessIter.getStore();
            return NativeDataAccess.getData(vector, vector.data, index);
        }

        @Override
        protected void setRawImpl(AccessIterator accessIter, int index, byte value) {
            RRawVector vector = (RRawVector) accessIter.getStore();
            NativeDataAccess.setData(vector, vector.data, index, value);
        }
    };

    @Override
    public VectorAccess slowPathAccess() {
        return SLOW_PATH_ACCESS;
    }
}
