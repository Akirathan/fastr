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
package com.oracle.truffle.r.nodes.binary;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.r.nodes.binary.BinaryBooleanScalarNodeGen.LogicalScalarCastNodeGen;
import com.oracle.truffle.r.nodes.builtin.RBuiltinNode;
import com.oracle.truffle.r.nodes.function.PromiseHelperNode.PromiseCheckHelperNode;
import com.oracle.truffle.r.runtime.DSLConfig;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.RType;
import com.oracle.truffle.r.runtime.data.RComplex;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;
import com.oracle.truffle.r.runtime.interop.ConvertForeignObjectNode;
import com.oracle.truffle.r.runtime.nodes.RBaseNode;
import com.oracle.truffle.r.runtime.ops.BooleanOperation;
import com.oracle.truffle.r.runtime.ops.BooleanOperationFactory;
import com.oracle.truffle.r.runtime.ops.na.NACheck;

public abstract class BinaryBooleanScalarNode extends RBuiltinNode.Arg2 {

    /*
     * As the execution of right depends on the left value and the right node might be arbitrarily
     * expensive we use a more expensive counting condition profile to inject more accurate profile
     * here.
     */
    private final ConditionProfile profile = ConditionProfile.createCountingProfile();

    @Child private BinaryMapBooleanFunctionNode logic;
    @Child private LogicalScalarCastNode leftCast;
    @Child private BoxPrimitiveNode leftBox;
    @Child private LogicalScalarCastNode rightCast;
    @Child private BoxPrimitiveNode rightBox;
    @Child private PromiseCheckHelperNode promiseHelper;

    static {
        Casts.noCasts(BinaryBooleanScalarNode.class);
    }

    BinaryBooleanScalarNode(BooleanOperationFactory factory) {
        BooleanOperation booleanLogic = factory.createOperation();
        logic = new BinaryMapBooleanFunctionNode(booleanLogic);
        leftCast = LogicalScalarCastNodeGen.create(booleanLogic.opName(), "x", logic.getLeftNACheck());
        leftBox = BoxPrimitiveNodeGen.create();
        rightCast = LogicalScalarCastNodeGen.create(booleanLogic.opName(), "y", logic.getRightNACheck());
        rightBox = BoxPrimitiveNodeGen.create();
        promiseHelper = new PromiseCheckHelperNode();
    }

    @Specialization
    protected byte binary(VirtualFrame frame, Object leftValue, Object rightValue) {
        byte left = leftCast.executeCast(leftBox.execute(leftValue));
        if (profile.profile(logic.requiresRightOperand(left))) {
            return logic.applyLogical(left, rightCast.executeCast(rightBox.execute(promiseHelper.checkVisibleEvaluate(frame, rightValue))));
        }
        return left;
    }

    @ImportStatic({RRuntime.class, DSLConfig.class, ConvertForeignObjectNode.class})
    protected abstract static class LogicalScalarCastNode extends RBaseNode {

        protected static final int CACHE_LIMIT = 3;

        public abstract byte executeCast(Object o);

        private final String opName;
        private final String argumentName;

        private final NACheck check;
        private final BranchProfile seenEmpty = BranchProfile.create();

        LogicalScalarCastNode(String opName, String argumentName, NACheck check) {
            this.opName = opName;
            this.argumentName = argumentName;
            this.check = check;
        }

        @Specialization(limit = "getCacheSize(CACHE_LIMIT)", guards = {"cachedClass != null", "operand.getClass() == cachedClass"})
        protected byte doCached(Object operand,
                        @Cached("getNumericVectorClass(operand)") Class<? extends RAbstractVector> cachedClass) {
            return castImpl(cachedClass.cast(operand));
        }

        @Specialization(replaces = "doCached", guards = {"operand.getRType().isNumeric()"})
        @TruffleBoundary
        protected byte doGeneric(RAbstractVector operand) {
            return castImpl(operand);
        }

        private byte castImpl(RAbstractVector vector) {
            if (vector.getLength() == 0) {
                seenEmpty.enter();
                this.check.enable(true);
                return RRuntime.LOGICAL_NA;
            }
            this.check.enable(!vector.isComplete());
            RType type = vector.getRType();
            CompilerAsserts.compilationConstant(type);
            switch (type) {
                case Logical:
                    return (byte) vector.getDataAtAsObject(0);
                case Integer:
                    return check.convertIntToLogical((int) vector.getDataAtAsObject(0));
                case Double:
                    return check.convertDoubleToLogical((double) vector.getDataAtAsObject(0));
                case Complex:
                    return check.convertComplexToLogical((RComplex) vector.getDataAtAsObject(0));
                default:
                    throw RInternalError.shouldNotReachHere();
            }
        }

        protected static Class<? extends RAbstractVector> getNumericVectorClass(Object value) {
            if (value instanceof RAbstractVector) {
                RAbstractVector castVector = (RAbstractVector) value;
                if (castVector.getRType().isNumeric()) {
                    return castVector.getClass();
                }
            }
            return null;
        }

        @Specialization(guards = {"isForeignArray(operand, interop)"}, limit = "getInteropLibraryCacheSize()")
        protected byte doForeignVector(TruffleObject operand,
                        @Cached("create()") ConvertForeignObjectNode convertForeign,
                        @Cached("createRecursive()") LogicalScalarCastNode recursive,
                        @SuppressWarnings("unused") @CachedLibrary("operand") InteropLibrary interop) {
            Object o = convertForeign.convert(operand);
            return recursive.executeCast(o);
        }

        @Specialization(guards = {"isForeignObject(operand)", "!isForeignArray(operand, interop)"}, limit = "getInteropLibraryCacheSize()")
        protected byte fallbackForeignVector(TruffleObject operand,
                        @SuppressWarnings("unused") @CachedLibrary("operand") InteropLibrary interop) {
            return doFallback(operand);
        }

        protected LogicalScalarCastNode createRecursive() {
            return LogicalScalarCastNodeGen.create(opName, argumentName, check);
        }

        @Fallback
        protected byte doFallback(@SuppressWarnings("unused") Object operand) {
            throw RError.error(this, RError.Message.INVALID_TYPE_IN, argumentName, opName);
        }
    }
}
