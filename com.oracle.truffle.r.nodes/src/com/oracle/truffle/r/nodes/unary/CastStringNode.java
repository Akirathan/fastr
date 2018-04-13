/*
 * Copyright (c) 2013, 2018, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.unary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.profiles.ValueProfile;
import com.oracle.truffle.r.runtime.RDeparse;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.data.RIntSequence;
import com.oracle.truffle.r.runtime.data.RPairList;
import com.oracle.truffle.r.runtime.data.RStringSequence;
import com.oracle.truffle.r.runtime.data.RStringVector;
import com.oracle.truffle.r.runtime.data.RSymbol;
import com.oracle.truffle.r.runtime.data.model.RAbstractContainer;
import com.oracle.truffle.r.runtime.data.model.RAbstractStringVector;
import com.oracle.truffle.r.runtime.interop.ForeignArray2R;

@ImportStatic(RRuntime.class)
public abstract class CastStringNode extends CastStringBaseNode {

    @Child private CastStringNode recursiveCastString;

    protected CastStringNode(boolean preserveNames, boolean preserveDimensions, boolean preserveAttributes) {
        this(preserveNames, preserveDimensions, preserveAttributes, false);
    }

    protected CastStringNode(boolean preserveNames, boolean preserveDimensions, boolean preserveAttributes, boolean forRFFI) {
        super(preserveNames, preserveDimensions, preserveAttributes, forRFFI);
    }

    public abstract Object executeString(int o);

    public abstract Object executeString(double o);

    public abstract Object executeString(byte o);

    public abstract Object executeString(Object o);

    private RStringVector vectorCopy(RAbstractContainer operand, String[] data) {
        RStringVector ret = factory().createStringVector(data, operand.isComplete(), getPreservedDimensions(operand), getPreservedNames(operand), getPreservedDimNames(operand));
        if (preserveRegAttributes()) {
            ret.copyRegAttributesFrom(operand);
        }
        return ret;
    }

    protected boolean isIntSequence(RAbstractContainer c) {
        return c instanceof RIntSequence;
    }

    @Specialization
    protected RAbstractStringVector doStringVector(RAbstractStringVector vector) {
        return vector;
    }

    @Specialization
    protected RStringSequence doIntSequence(RIntSequence vector) {
        return factory().createStringSequence("", "", vector.getStart(), vector.getStride(), vector.getLength());
    }

    @Specialization(guards = {"!isIntSequence(operandIn)", "!isRAbstractStringVector(operandIn)"})
    protected RStringVector doAbstractContainer(RAbstractContainer operandIn,
                    @Cached("createClassProfile()") ValueProfile operandProfile,
                    @Cached("createBinaryProfile()") ConditionProfile isLanguageProfile) {
        RAbstractContainer operand = operandProfile.profile(operandIn);
        String[] sdata = new String[operand.getLength()];
        // conversions to character will not introduce new NAs
        for (int i = 0; i < operand.getLength(); i++) {
            Object o = operand.getDataAtAsObject(i);
            if (isLanguageProfile.profile((o instanceof RPairList && ((RPairList) o).isLanguage()))) {
                sdata[i] = RDeparse.deparse(o);
            } else {
                sdata[i] = toString(o);
            }
        }
        return vectorCopy(operand, sdata);
    }

    @Specialization(guards = "isForeignObject(obj)")
    protected RAbstractStringVector doForeignObject(TruffleObject obj,
                    @Cached("create()") ForeignArray2R foreignArray2R) {
        Object o = foreignArray2R.convert(obj);
        if (!RRuntime.isForeignObject(o)) {
            if (o instanceof RAbstractStringVector) {
                return (RAbstractStringVector) o;
            }
            o = castStringRecursive(o);
            if (o instanceof RAbstractStringVector) {
                return (RAbstractStringVector) o;
            }
        }
        throw error(RError.Message.CANNOT_COERCE_EXTERNAL_OBJECT_TO_VECTOR, "vector");
    }

    @Specialization
    protected String doRSymbol(RSymbol s) {
        return s.getName();
    }

    public static CastStringNode create() {
        return CastStringNodeGen.create(true, true, true);
    }

    public static CastStringNode createForRFFI(boolean preserveNames, boolean preserveDimensions, boolean preserveAttributes) {
        return CastStringNodeGen.create(preserveNames, preserveDimensions, preserveAttributes, true);
    }

    public static CastStringNode createNonPreserving() {
        return CastStringNodeGen.create(false, false, false);
    }

    private Object castStringRecursive(Object o) {
        if (recursiveCastString == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            recursiveCastString = insert(CastStringNodeGen.create(preserveNames(), preserveDimensions(), preserveRegAttributes()));
        }
        return recursiveCastString.executeString(o);
    }
}
