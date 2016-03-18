/*
 * Copyright (c) 2013, 2016, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.r.nodes.builtin.base;

import static com.oracle.truffle.r.runtime.RBuiltinKind.PRIMITIVE;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.r.nodes.binary.BoxPrimitiveNode;
import com.oracle.truffle.r.nodes.binary.BoxPrimitiveNodeGen;
import com.oracle.truffle.r.nodes.builtin.RBuiltinNode;
import com.oracle.truffle.r.nodes.unary.UnaryArithmeticNode;
import com.oracle.truffle.r.nodes.unary.UnaryArithmeticNodeGen;
import com.oracle.truffle.r.runtime.RBuiltin;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.RType;
import com.oracle.truffle.r.runtime.data.RComplex;
import com.oracle.truffle.r.runtime.ops.UnaryArithmetic;

@RBuiltin(name = "abs", kind = PRIMITIVE, parameterNames = {"x"})
public abstract class Abs extends RBuiltinNode {

    @Child private BoxPrimitiveNode boxPrimitive = BoxPrimitiveNodeGen.create();
    @Child private UnaryArithmeticNode absNode = UnaryArithmeticNodeGen.create(AbsArithmetic::new,
                    RError.Message.NON_NUMERIC_MATH, RType.Integer);

    @Specialization
    protected Object abs(Object value) {
        return absNode.execute(boxPrimitive.execute(value));
    }

    public static class AbsArithmetic extends UnaryArithmetic {

        @Override
        public RType calculateResultType(RType argumentType) {
            switch (argumentType) {
                case Complex:
                    return RType.Double;
                default:
                    return super.calculateResultType(argumentType);
            }
        }

        @Override
        public int op(byte op) {
            return Math.abs(op);
        }

        @Override
        public int op(int op) {
            return Math.abs(op);
        }

        @Override
        public double op(double op) {
            return Math.abs(op);
        }

        @Override
        public RComplex op(double re, double im) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double opd(double re, double im) {
            return RComplex.abs(re, im);
        }
    }
}
