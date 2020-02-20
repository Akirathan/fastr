package com.oracle.truffle.r.runtime.data.altrep;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.data.RAltIntVectorData;
import com.oracle.truffle.r.runtime.data.RBaseObject;
import com.oracle.truffle.r.runtime.data.RIntVector;
import com.oracle.truffle.r.runtime.ffi.util.NativeMemory;

public class AltrepUtilities {
    public static boolean isAltrep(Object object) {
        return object instanceof RBaseObject && ((RBaseObject) object).isAltRep();
    }

    private static RAltIntVectorData getAltIntVectorData(RIntVector altIntVector) {
        assert altIntVector.isAltRep();
        return (RAltIntVectorData) altIntVector.getData();
    }

    public static boolean hasCoerceMethodRegistered(Object object) {
        if (!isAltrep(object)) {
            return false;
        }

        if (object instanceof RIntVector) {
            return getAltIntVectorData((RIntVector) object).getDescriptor().isCoerceMethodRegistered();
        } else {
            throw RInternalError.shouldNotReachHere("Unexpected altrep type");
        }
    }

    public static boolean hasDuplicateMethodRegistered(Object object) {
        if (!isAltrep(object)) {
            return false;
        }

        if (object instanceof RIntVector) {
            return getAltIntVectorData((RIntVector) object).getDescriptor().isDuplicateMethodRegistered();
        } else {
            throw RInternalError.shouldNotReachHere("Unexpected altrep type");
        }
    }

    public static boolean hasEltMethodRegistered(Object object) {
        if (!isAltrep(object)) {
            return false;
        }
        if (object instanceof RIntVector) {
            return getAltIntVectorData((RIntVector) object).getDescriptor().isEltMethodRegistered();
        } else {
            throw RInternalError.shouldNotReachHere("Unexpected altrep type");
        }
    }

    public static boolean hasMaxMethodRegistered(Object object) {
        if (!isAltrep(object)) {
            return false;
        }

        if (object instanceof RIntVector) {
            return getAltIntVectorData((RIntVector) object).getDescriptor().isMaxMethodRegistered();
        } else {
            throw RInternalError.shouldNotReachHere("Unexpected altrep type");
        }
    }

    public static boolean hasMinMethodRegistered(Object object) {
        if (!isAltrep(object)) {
            return false;
        }

        if (object instanceof RIntVector) {
            return getAltIntVectorData((RIntVector) object).getDescriptor().isMinMethodRegistered();
        } else {
            throw RInternalError.shouldNotReachHere("Unexpected altrep type");
        }
    }

    public static boolean hasSumMethodRegistered(Object object) {
        if (!isAltrep(object)) {
            return false;
        }

        if (object instanceof RIntVector) {
            return getAltIntVectorData((RIntVector) object).getDescriptor().isSumMethodRegistered();
        } else {
            throw RInternalError.shouldNotReachHere("Unexpected altrep type");
        }
    }

    public abstract static class AltrepDuplicateMethodInvoker extends Node {
        public abstract Object execute(Object altrepVector, boolean deep);

        public static AltrepDuplicateMethodInvoker create() {
            return AltrepUtilitiesFactory.AltrepDuplicateMethodInvokerNodeGen.create();
        }

        @Specialization(limit = "3")
        Object doAltInt(RAltIntVectorData altIntVec, boolean deep,
                        @CachedLibrary("altIntVec.getDescriptor().getDuplicateMethod()") InteropLibrary duplicateMethodInterop,
                        @Cached("createBinaryProfile()") ConditionProfile hasMirrorProfile) {
            assert altIntVec.getDescriptor().isDuplicateMethodRegistered();
            return altIntVec.getDescriptor().invokeDuplicateMethodCached(altIntVec, deep, duplicateMethodInterop, hasMirrorProfile);
        }
    }

    public abstract static class AltrepSumMethodInvoker extends Node {
        public abstract Object execute(Object altrepVector, boolean naRm);

        public static AltrepSumMethodInvoker create() {
            return AltrepUtilitiesFactory.AltrepSumMethodInvokerNodeGen.create();
        }

        @Specialization(limit = "3")
        Object doAltInt(RAltIntVectorData altIntVec, boolean naRm,
                        @CachedLibrary("altIntVec.getDescriptor().getSumMethod()") InteropLibrary methodInterop,
                        @Cached("createBinaryProfile()") ConditionProfile hasMirrorProfile) {
            assert altIntVec.getDescriptor().isSumMethodRegistered();
            return altIntVec.getDescriptor().invokeSumMethodCached(altIntVec, naRm, methodInterop, hasMirrorProfile);
        }
    }

    public abstract static class AltrepMaxMethodInvoker extends Node {
        public abstract Object execute(Object altrepVector, boolean naRm);

        public static AltrepMaxMethodInvoker create() {
            return AltrepUtilitiesFactory.AltrepMaxMethodInvokerNodeGen.create();
        }

        @Specialization(limit = "3")
        Object doAltInt(RAltIntVectorData altIntVec, boolean naRm,
                        @CachedLibrary("altIntVec.getDescriptor().getMaxMethod()") InteropLibrary methodInterop,
                        @Cached("createBinaryProfile()") ConditionProfile hasMirrorProfile) {
            assert altIntVec.getDescriptor().isMaxMethodRegistered();
            return altIntVec.getDescriptor().invokeMaxMethodCached(altIntVec, naRm, methodInterop, hasMirrorProfile);
        }
    }

    public abstract static class AltrepMinMethodInvoker extends Node {
        public abstract Object execute(Object altrepVector, boolean naRm);

        public static AltrepMinMethodInvoker create() {
            return AltrepUtilitiesFactory.AltrepMinMethodInvokerNodeGen.create();
        }

        @Specialization(limit = "3")
        Object doAltInt(RAltIntVectorData altIntVec, boolean naRm,
                        @CachedLibrary("altIntVec.getDescriptor().getMinMethod()") InteropLibrary methodInterop,
                        @Cached("createBinaryProfile()") ConditionProfile hasMirrorProfile) {
            assert altIntVec.getDescriptor().isMinMethodRegistered();
            return altIntVec.getDescriptor().invokeMinMethodCached(altIntVec, naRm, methodInterop, hasMirrorProfile);
        }
    }

    /**
     *
     */
    public abstract static class AltrepReadArrayElement extends Node {
        public abstract Object execute(Object altrepVector, int index);

        public static AltrepReadArrayElement create() {
            return AltrepUtilitiesFactory.AltrepReadArrayElementNodeGen.create();
        }

        protected static boolean hasEltMethodRegistered(Object object) {
            return AltrepUtilities.hasEltMethodRegistered(object);
        }

        @Specialization(guards = "hasEltMethodRegistered(altIntVec)", limit = "1")
        Object doAltIntElt(RAltIntVectorData altIntVec, int index,
                           @CachedLibrary("altIntVec.getDescriptor().getEltMethod()") InteropLibrary eltMethodInterop,
                           @Cached("createBinaryProfile()") ConditionProfile hasMirrorProfile) {
            return altIntVec.getDescriptor().invokeEltMethodCached(altIntVec, index, eltMethodInterop, hasMirrorProfile);
        }

        @Specialization(replaces = "doAltIntElt", limit = "1")
        Object doAltIntWithoutElt(RAltIntVectorData altIntVec, int index,
                                  @CachedLibrary("altIntVec.getDescriptor().getDataptrMethod()") InteropLibrary dataptrMethodInterop,
                                  @CachedLibrary(limit = "1") InteropLibrary dataptrInterop,
                                  @Cached("createBinaryProfile()") ConditionProfile hasMirrorProfile) {
            long address = altIntVec.getDescriptor().invokeDataptrMethodCached(altIntVec, true, dataptrMethodInterop, dataptrInterop, hasMirrorProfile);
            return NativeMemory.getInt(address, index);
        }

        @Specialization(limit = "3")
        Object doAltString(RAltStringVector altStringVector, int index,
                           @CachedLibrary("altStringVector.getDescriptor().getEltMethod()") InteropLibrary eltMethodInterop,
                           @Cached("createBinaryProfile()") ConditionProfile hasMirrorProfile) {
            return altStringVector.getDescriptor().invokeEltMethodCached(altStringVector, index, eltMethodInterop, hasMirrorProfile);
        }
    }
}
