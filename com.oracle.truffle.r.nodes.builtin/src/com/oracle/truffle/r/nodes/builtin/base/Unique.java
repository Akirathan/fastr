/*
 * Copyright (c) 2014, 2020, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.builtin.base;

import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.abstractVectorValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.logicalFalse;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.logicalValue;
import static com.oracle.truffle.r.runtime.builtins.RBehavior.PURE;
import static com.oracle.truffle.r.runtime.builtins.RBuiltinKind.INTERNAL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.r.nodes.builtin.RBuiltinNode;
import com.oracle.truffle.r.runtime.Collections.NonRecursiveHashSet;
import com.oracle.truffle.r.runtime.Collections.NonRecursiveHashSetDouble;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.RError.Message;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.builtins.RBuiltin;
import com.oracle.truffle.r.runtime.data.RComplex;
import com.oracle.truffle.r.runtime.data.RComplexVector;
import com.oracle.truffle.r.runtime.data.RDataFactory;
import com.oracle.truffle.r.runtime.data.RDoubleVector;
import com.oracle.truffle.r.runtime.data.RList;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.data.RStringVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractComplexVector;
import com.oracle.truffle.r.runtime.data.RIntVector;
import com.oracle.truffle.r.runtime.data.RLogicalVector;
import com.oracle.truffle.r.runtime.data.RRawVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractStringVector;

@RBuiltin(name = "unique", kind = INTERNAL, parameterNames = {"x", "incomparables", "fromLast", "nmax"}, behavior = PURE)
// TODO A more efficient implementation is in order; GNU R uses hash tables so perhaps we should
// consider using one of the existing libraries that offer hash table implementations for primitive
// types
public abstract class Unique extends RBuiltinNode.Arg4 {

    private static final long BIG_THRESHOLD = 100;

    private final ConditionProfile bigProfile = ConditionProfile.createBinaryProfile();

    static {
        Casts casts = new Casts(Unique.class);
        // these are similar to those in DuplicatedFunctions.java
        casts.arg("x").defaultError(RError.Message.APPLIES_TO_VECTORS, "unique()").allowNull().mustBe(abstractVectorValue()).asVector();
        // not much more can be done for incomparables as it is either a vector of incomparable
        // values or a (single) logical value
        // TODO: coercion error must be handled by specialization as it depends on type of x (much
        // like in duplicated)
        casts.arg("incomparables").mustBe(logicalValue()).asLogicalVector().findFirst().mustBe(logicalFalse(), Message.GENERIC, "Handling of 'incomparables' parameter in unique is not implemented.");
        casts.arg("fromLast").asLogicalVector().findFirst(RRuntime.LOGICAL_FALSE);
        // currently not supported and not tested, but NA is a correct value (the same for empty
        // vectors) whereas 0 is not (throws an error)
        casts.arg("nmax").asIntegerVector().findFirst(RRuntime.INT_NA);
    }

    @SuppressWarnings("unused")
    @Specialization
    protected RNull doUnique(RNull vec, byte incomparables, byte fromLast, int nmax) {
        return vec;
    }

    @SuppressWarnings("unused")
    @Specialization(guards = "vecIn.getClass() == vecClass")
    protected RStringVector doUniqueCachedString(RAbstractStringVector vecIn, byte incomparables, byte fromLast, int nmax,
                    @Cached("vecIn.getClass()") Class<? extends RAbstractStringVector> vecClass) {
        RAbstractStringVector vec = vecClass.cast(vecIn);
        reportWork(vec.getLength());
        if (bigProfile.profile(vec.getLength() * (long) vec.getLength() > BIG_THRESHOLD)) {
            NonRecursiveHashSet<String> set = new NonRecursiveHashSet<>(vec.getLength());
            String[] data = new String[vec.getLength()];
            int ind = 0;
            for (int i = 0; i < vec.getLength(); i++) {
                String val = vec.getDataAt(i);
                if (!set.add(val)) {
                    data[ind++] = val;
                }
            }
            return RDataFactory.createStringVector(Arrays.copyOf(data, ind), vec.isComplete());
        } else {
            ArrayList<String> dataList = new ArrayList<>(vec.getLength());
            for (int i = 0; i < vec.getLength(); i++) {
                String s = vec.getDataAt(i);
                if (!dataList.contains(s)) {
                    dataList.add(s);
                }
            }
            String[] data = new String[dataList.size()];
            dataList.toArray(data);
            return RDataFactory.createStringVector(data, vec.isComplete());
        }
    }

    @Specialization(replaces = "doUniqueCachedString")
    protected RStringVector doUnique(RAbstractStringVector vec, byte incomparables, byte fromLast, int nmax) {
        return doUniqueCachedString(vec, incomparables, fromLast, nmax, RAbstractStringVector.class);
    }

    // these are intended to stay private as they will go away once we figure out which external
    // library to use

    private static class IntArray {
        int[] backingArray;
        int index;

        IntArray(int len) {
            this.backingArray = new int[len];
            index = 0;
        }

        public boolean contains(int val) {
            for (int i = 0; i < index; i++) {
                if (backingArray[i] == val) {
                    return true;
                }
            }
            return false;
        }

        public void add(int val) {
            backingArray[index++] = val;
        }

        public int[] toArray() {
            int[] newArray = new int[index];
            System.arraycopy(backingArray, 0, newArray, 0, index);
            return newArray;
        }
    }

    private static class DoubleArray {
        double[] backingArray;
        int index;

        DoubleArray(int len) {
            this.backingArray = new double[len];
            index = 0;
        }

        public boolean contains(double val) {
            for (int i = 0; i < index; i++) {
                if (areEqual(backingArray[i], val)) {
                    return true;
                }
            }
            return false;
        }

        public void add(double val) {
            backingArray[index++] = val;
        }

        public double[] toArray() {
            double[] newArray = new double[index];
            System.arraycopy(backingArray, 0, newArray, 0, index);
            return newArray;
        }

        private static boolean areEqual(double a, double b) {
            return a == b || (RRuntime.isNA(a) && RRuntime.isNA(b));
        }
    }

    private static class DoubleArrayForComplex {
        double[] backingArray;
        int index;

        DoubleArrayForComplex(int len) {
            this.backingArray = new double[len << 1];
            index = 0;
        }

        public boolean contains(RComplex val) {
            for (int i = 0; i < index; i++) {
                if (areEqual(backingArray[i << 1], backingArray[(i << 1) + 1], val.getRealPart(), val.getImaginaryPart())) {
                    return true;
                }
            }
            return false;
        }

        public void add(RComplex val) {
            backingArray[index << 1] = val.getRealPart();
            backingArray[(index << 1) + 1] = val.getImaginaryPart();
            index++;
        }

        public double[] toArray() {
            int size = index << 1;
            double[] newArray = new double[size];
            System.arraycopy(backingArray, 0, newArray, 0, size);
            return newArray;
        }

        private static boolean areEqual(double ar, double ai, double br, double bi) {
            return (ar == br && ai == bi) || (RRuntime.isNA(ar, ai) && RRuntime.isNA(br, bi));
        }
    }

    private static class ByteArray {
        byte[] backingArray;
        int index;

        ByteArray(int len) {
            this.backingArray = new byte[len];
            index = 0;
        }

        public boolean contains(byte val) {
            for (int i = 0; i < index; i++) {
                if (backingArray[i] == val) {
                    return true;
                }
            }
            return false;
        }

        public void add(byte val) {
            backingArray[index++] = val;
        }

        public byte[] toArray() {
            byte[] newArray = new byte[index];
            System.arraycopy(backingArray, 0, newArray, 0, index);
            return newArray;
        }
    }

    @SuppressWarnings("unused")
    @Specialization(guards = "vecIn.getClass() == vecClass")
    protected RIntVector doUniqueCached(RIntVector vecIn, byte incomparables, byte fromLast, int nmax,
                    @Cached("vecIn.getClass()") Class<? extends RIntVector> vecClass) {
        RIntVector vec = vecClass.cast(vecIn);
        reportWork(vec.getLength());
        if (bigProfile.profile(vec.getLength() * (long) vec.getLength() > BIG_THRESHOLD)) {
            NonRecursiveHashSetInt set = new NonRecursiveHashSetInt();
            int[] data = new int[16];
            int ind = 0;
            for (int i = 0; i < vec.getLength(); i++) {
                int val = vec.getDataAt(i);
                if (!set.add(val)) {
                    if (ind == data.length) {
                        data = Arrays.copyOf(data, data.length << 1);
                    }
                    data[ind++] = val;
                }
            }
            return RDataFactory.createIntVector(Arrays.copyOf(data, ind), vec.isComplete());
        } else {
            IntArray dataList = new IntArray(vec.getLength());
            for (int i = 0; i < vec.getLength(); i++) {
                int val = vec.getDataAt(i);
                if (!dataList.contains(val)) {
                    dataList.add(val);
                }
            }
            return RDataFactory.createIntVector(dataList.toArray(), vec.isComplete());
        }
    }

    @Specialization(replaces = "doUniqueCached")
    protected RIntVector doUnique(RIntVector vec, byte incomparables, byte fromLast, int nmax) {
        return doUniqueCached(vec, incomparables, fromLast, nmax, RIntVector.class);
    }

    @SuppressWarnings("unused")
    @Specialization(guards = "lengthOne(list)")
    protected RList doUniqueL1(RList list, byte incomparables, byte fromLast, int nmax) {
        return (RList) list.copyDropAttributes();
    }

    @Child private Identical identical;

    private boolean identical(Object x, Object y) {
        if (identical == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            identical = insert(Identical.create());
        }
        return RRuntime.fromLogical(identical.executeByte(x, y, true, true, true, true, false, true));
    }

    @SuppressWarnings("unused")
    @Specialization(guards = "!lengthOne(list)")
    @TruffleBoundary
    protected RList doUnique(RList list, byte incomparables, byte fromLast, int nmax) {
        reportWork(list.getLength());
        /*
         * Brute force, as manual says: Using this for lists is potentially slow, especially if the
         * elements are not atomic vectors (see vector) or differ only in their attributes. In the
         * worst case it is O(n^2).
         */
        ArrayList<Object> data = new ArrayList<>(list.getLength());
        for (int i = 0; i < list.getLength(); i++) {
            Object elem = list.getDataAt(i);
            boolean same = false;
            for (int j = 0; j < data.size(); j++) {
                Object dataElem = data.get(j);
                if (identical(elem, dataElem)) {
                    same = true;
                    break;
                }
            }
            if (!same) {
                data.add(elem);
            }
        }
        return RDataFactory.createList(data.toArray());
    }

    protected static boolean lengthOne(RList list) {
        return list.getLength() == 1;
    }

    private static final class NonRecursiveHashSetInt {

        private int[] keys;
        private int size;
        private boolean containsZero;

        NonRecursiveHashSetInt() {
            keys = new int[64];
        }

        public boolean add(int key) {
            if (key == 0) {
                if (containsZero) {
                    return true;
                } else {
                    containsZero = true;
                    return false;
                }
            }
            int ind = Math.abs(Integer.hashCode(key)) & (keys.length - 1);
            while (true) {
                if (keys[ind] == 0) {
                    keys[ind] = key;
                    size++;
                    if (size << 1 == keys.length) {
                        int[] newKeys = new int[keys.length << 1];
                        for (int rehashKey : keys) {
                            if (rehashKey != 0) {
                                int tmpInd = Math.abs(Integer.hashCode(rehashKey)) & (newKeys.length - 1);
                                while (newKeys[tmpInd] != 0) {
                                    assert newKeys[tmpInd] != rehashKey;
                                    tmpInd = (tmpInd + 1) & (newKeys.length - 1);
                                }
                                newKeys[tmpInd] = rehashKey;
                            }
                        }

                        keys = newKeys;
                    }
                    return false;
                } else if (key == keys[ind]) {
                    return true;
                } else {
                    ind = (ind + 1) & (keys.length - 1);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @Specialization
    protected RDoubleVector doUnique(RDoubleVector vec, byte incomparables, byte fromLast, int nmax) {
        reportWork(vec.getLength());
        if (bigProfile.profile(vec.getLength() * (long) vec.getLength() > BIG_THRESHOLD)) {
            NonRecursiveHashSetDouble set = new NonRecursiveHashSetDouble(vec.getLength());
            double[] data = new double[vec.getLength()];
            int ind = 0;
            for (int i = 0; i < vec.getLength(); i++) {
                double val = vec.getDataAt(i);
                if (!set.add(val)) {
                    data[ind++] = val;
                }
            }
            return RDataFactory.createDoubleVector(Arrays.copyOf(data, ind), vec.isComplete());
        } else {
            DoubleArray dataList = new DoubleArray(vec.getLength());
            for (int i = 0; i < vec.getLength(); i++) {
                double val = vec.getDataAt(i);
                if (!dataList.contains(val)) {
                    dataList.add(val);
                }
            }
            return RDataFactory.createDoubleVector(dataList.toArray(), vec.isComplete());
        }
    }

    @SuppressWarnings("unused")
    @Specialization
    protected RLogicalVector doUnique(RLogicalVector vec, byte incomparables, byte fromLast, int nmax) {
        reportWork(vec.getLength());
        ByteArray dataList = new ByteArray(vec.getLength());
        for (int i = 0; i < vec.getLength(); i++) {
            byte val = vec.getDataAt(i);
            if (!dataList.contains(val)) {
                dataList.add(val);
            }
        }
        return RDataFactory.createLogicalVector(dataList.toArray(), vec.isComplete());
    }

    @SuppressWarnings("unused")
    @Specialization
    protected RComplexVector doUnique(RAbstractComplexVector vec, byte incomparables, byte fromLast, int nmax) {
        reportWork(vec.getLength());
        if (bigProfile.profile(vec.getLength() * (long) vec.getLength() > BIG_THRESHOLD)) {
            NonRecursiveHashSet<RComplex> set = new NonRecursiveHashSet<>(vec.getLength());
            double[] data = new double[vec.getLength() * 2];
            int ind = 0;
            for (int i = 0; i < vec.getLength(); i++) {
                RComplex val = vec.getDataAt(i);
                if (!set.add(val)) {
                    data[ind++] = val.getRealPart();
                    data[ind++] = val.getImaginaryPart();
                }
            }
            return RDataFactory.createComplexVector(Arrays.copyOf(data, ind), vec.isComplete());
        } else {
            DoubleArrayForComplex dataList = new DoubleArrayForComplex(vec.getLength());
            for (int i = 0; i < vec.getLength(); i++) {
                RComplex s = vec.getDataAt(i);
                if (!dataList.contains(s)) {
                    dataList.add(s);
                }
            }
            return RDataFactory.createComplexVector(dataList.toArray(), vec.isComplete());
        }
    }

    @SuppressWarnings("unused")
    @Specialization
    protected RRawVector doUnique(RRawVector vec, byte incomparables, byte fromLast, int nmax,
                    @Cached("createBinaryProfile()") ConditionProfile needsCopyProfile) {
        reportWork(vec.getLength());
        BitSet bitset = new BitSet(256);
        byte[] data = new byte[vec.getLength()];
        int ind = 0;
        for (int i = 0; i < vec.getLength(); i++) {
            byte val = vec.getRawDataAt(i);
            if (!bitset.get(val)) {
                bitset.set(val);
                data[ind++] = val;
            }
        }
        return RDataFactory.createRawVector(needsCopyProfile.profile(ind == vec.getLength()) ? data : Arrays.copyOf(data, ind));
    }
}
