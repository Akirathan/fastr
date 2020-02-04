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

import com.oracle.truffle.api.CompilerDirectives.ValueType;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.RType;
import com.oracle.truffle.r.runtime.data.model.RAbstractContainer;
import com.oracle.truffle.r.runtime.data.model.RIntVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;
import com.oracle.truffle.r.runtime.data.nodes.FastPathVectorAccess.FastPathFromIntAccess;
import com.oracle.truffle.r.runtime.data.nodes.SlowPathVectorAccess.SlowPathFromIntAccess;
import com.oracle.truffle.r.runtime.data.nodes.VectorAccess;

@ValueType
public final class RInteger extends RIntVector implements RScalarVector {

    protected final int value;

    private RInteger(int value) {
        super(!RRuntime.isNA(value));
        this.value = value;
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public RAbstractVector copy() {
        return this;
    }

    @Override
    public com.oracle.truffle.r.runtime.data.RIntVector materialize() {
        com.oracle.truffle.r.runtime.data.RIntVector result = RDataFactory.createIntVector(new int[]{getValue()}, isComplete());
        MemoryCopyTracer.reportCopying(this, result);
        return result;
    }

    @Override
    public int[] getDataCopy() {
        return new int[]{getValue()};
    }

    public static RInteger createNA() {
        return new RInteger(RRuntime.INT_NA);
    }

    public static RInteger valueOf(int value) {
        return new RInteger(value);
    }

    @Override
    public int getDataAt(int index) {
        assert index == 0;
        return value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public RAbstractVector castSafe(RType type, ConditionProfile isNAProfile, boolean keepAttributes) {
        switch (type) {
            case Integer:
                return this;
            case Double:
                return isNAProfile.profile(isNA()) ? RDouble.createNA() : RDouble.valueOf(value);
            case Complex:
                return isNAProfile.profile(isNA()) ? RComplex.createNA() : RComplex.valueOf(value, 0.0);
            case Character:
                return RString.valueOf(RRuntime.intToString(value));
            case List:
                return RScalarList.valueOf(this);
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return RRuntime.intToString(value);
    }

    @Override
    public boolean isNA() {
        return RRuntime.isNA(value);
    }

    private static final class FastPathAccess extends FastPathFromIntAccess {

        FastPathAccess(RAbstractContainer value) {
            super(value);
        }

        @Override
        public int getIntImpl(AccessIterator accessIter, int index) {
            assert index == 0;
            return ((RInteger) accessIter.getStore()).value;
        }
    }

    @Override
    public VectorAccess access() {
        return new FastPathAccess(this);
    }

    private static final SlowPathFromIntAccess SLOW_PATH_ACCESS = new SlowPathFromIntAccess() {
        @Override
        public int getIntImpl(AccessIterator accessIter, int index) {
            assert index == 0;
            return ((RInteger) accessIter.getStore()).value;
        }
    };

    @Override
    public VectorAccess slowPathAccess() {
        return SLOW_PATH_ACCESS;
    }
}
