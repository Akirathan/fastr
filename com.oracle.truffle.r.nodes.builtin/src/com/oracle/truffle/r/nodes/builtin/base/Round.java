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
package com.oracle.truffle.r.nodes.builtin.base;

import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.complexValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.numericValue;
import static com.oracle.truffle.r.runtime.RDispatch.MATH_GROUP_GENERIC;
import static com.oracle.truffle.r.runtime.builtins.RBehavior.PURE_ARITHMETIC;
import static com.oracle.truffle.r.runtime.builtins.RBuiltinKind.PRIMITIVE;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.r.nodes.builtin.RBuiltinNode;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.builtins.RBuiltin;
import com.oracle.truffle.r.runtime.data.RComplex;
import com.oracle.truffle.r.runtime.data.RComplexVector;
import com.oracle.truffle.r.runtime.data.RDataFactory;
import com.oracle.truffle.r.runtime.data.RDoubleVector;
import com.oracle.truffle.r.runtime.data.RMissing;
import com.oracle.truffle.r.runtime.data.model.RAbstractComplexVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractDoubleVector;
import com.oracle.truffle.r.runtime.data.RIntVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractLogicalVector;
import com.oracle.truffle.r.runtime.ops.BinaryArithmetic;
import com.oracle.truffle.r.runtime.ops.na.NACheck;

@RBuiltin(name = "round", kind = PRIMITIVE, parameterNames = {"x", "digits"}, dispatch = MATH_GROUP_GENERIC, behavior = PURE_ARITHMETIC)
public abstract class Round extends RBuiltinNode.Arg2 {

    @Child private RoundArithmetic roundOp = new RoundArithmetic();

    private final ConditionProfile zeroDigitProfile = ConditionProfile.createBinaryProfile();

    private final NACheck check = NACheck.create();

    @Override
    public Object[] getDefaultParameterValues() {
        return new Object[]{RMissing.instance, 0};
    }

    static {
        Casts casts = new Casts(Round.class);
        casts.arg("x").defaultError(RError.Message.NON_NUMERIC_MATH).mustBe(numericValue().or(complexValue()));

        // TODO: this should also accept vectors
        casts.arg("digits").defaultError(RError.Message.NON_NUMERIC_MATH).mustBe(numericValue().or(complexValue())).asDoubleVector().findFirst();
    }

    protected static boolean isZero(double value) {
        return value == 0;
    }

    @Specialization
    protected double round(int x, @SuppressWarnings("unused") double digits) {
        check.enable(x);
        return check.check(x) ? RRuntime.DOUBLE_NA : x;
    }

    @Specialization
    protected double round(byte x, @SuppressWarnings("unused") double digits) {
        check.enable(x);
        return check.check(x) ? RRuntime.DOUBLE_NA : x;
    }

    @Specialization
    protected double roundDigits(double x, double digits) {
        check.enable(x);
        int digitsInt = (int) Math.round(digits);
        return check.check(x) ? RRuntime.DOUBLE_NA : zeroDigitProfile.profile(digitsInt == 0) ? roundOp.op(x) : roundOp.opd(x, digitsInt);
    }

    @Specialization
    protected RDoubleVector round(RAbstractLogicalVector x, @SuppressWarnings("unused") double digits) {
        double[] data = new double[x.getLength()];
        check.enable(x);
        for (int i = 0; i < data.length; i++) {
            byte val = x.getDataAt(i);
            data[i] = check.check(val) ? RRuntime.DOUBLE_NA : val;
        }
        RDoubleVector ret = RDataFactory.createDoubleVector(data, check.neverSeenNA());
        ret.copyAttributesFrom(x);
        return ret;
    }

    @Specialization
    protected RDoubleVector round(RIntVector x, @SuppressWarnings("unused") double digits) {
        double[] data = new double[x.getLength()];
        check.enable(x);
        for (int i = 0; i < data.length; i++) {
            int val = x.getDataAt(i);
            data[i] = check.check(val) ? RRuntime.DOUBLE_NA : val;
        }
        RDoubleVector ret = RDataFactory.createDoubleVector(data, check.neverSeenNA());
        ret.copyAttributesFrom(x);
        return ret;
    }

    protected double roundDigits(double x, int digits) {
        return check.check(x) ? RRuntime.DOUBLE_NA : zeroDigitProfile.profile(digits == 0) ? roundOp.op(x) : roundOp.opd(x, digits);
    }

    @Specialization
    protected RDoubleVector round(RAbstractDoubleVector x, double digits) {
        double[] result = new double[x.getLength()];
        check.enable(x);
        int digitsInt = (int) Math.round(digits);
        for (int i = 0; i < x.getLength(); i++) {
            double value = x.getDataAt(i);
            result[i] = check.check(value) ? RRuntime.DOUBLE_NA : zeroDigitProfile.profile(digitsInt == 0) ? roundOp.op(value) : roundOp.opd(value, digitsInt);
        }
        RDoubleVector ret = RDataFactory.createDoubleVector(result, check.neverSeenNA());
        ret.copyAttributesFrom(x);
        return ret;
    }

    @Specialization(guards = "isZero(digits)")
    protected RComplex round(RComplex x, @SuppressWarnings("unused") double digits) {
        check.enable(x);
        return check.check(x) ? RComplex.createNA() : RComplex.valueOf(roundOp.op(x.getRealPart()), roundOp.op(x.getImaginaryPart()));
    }

    protected RComplex roundDigits(RComplex x, int digits) {
        check.enable(x);
        return check.check(x) ? RComplex.createNA() : roundOp.opd(x.getRealPart(), x.getImaginaryPart(), digits);
    }

    @Specialization(guards = "!isZero(digits)")
    protected RComplex roundDigits(RComplex x, double digits) {
        return roundDigits(x, (int) Math.round(digits));
    }

    @Specialization(guards = "isZero(digits)")
    protected RComplexVector round(RAbstractComplexVector x, double digits) {
        double[] result = new double[x.getLength() << 1];
        check.enable(x);
        for (int i = 0; i < x.getLength(); i++) {
            RComplex z = x.getDataAt(i);
            RComplex r = check.check(z) ? RComplex.createNA() : round(z, digits);
            result[2 * i] = r.getRealPart();
            result[2 * i + 1] = r.getImaginaryPart();
            check.check(r);
        }
        RComplexVector ret = RDataFactory.createComplexVector(result, check.neverSeenNA());
        ret.copyAttributesFrom(x);
        return ret;
    }

    @Specialization(guards = "!isZero(dDigits)")
    protected RComplexVector roundDigits(RAbstractComplexVector x, double dDigits) {
        int digits = (int) Math.round(dDigits);
        double[] result = new double[x.getLength() << 1];
        check.enable(x);
        for (int i = 0; i < x.getLength(); i++) {
            RComplex z = x.getDataAt(i);
            RComplex r = check.check(z) ? RComplex.createNA() : roundDigits(z, digits);
            result[2 * i] = r.getRealPart();
            result[2 * i + 1] = r.getImaginaryPart();
            check.check(r);
        }
        RComplexVector ret = RDataFactory.createComplexVector(result, check.neverSeenNA());
        ret.copyAttributesFrom(x);
        return ret;
    }

    public static final class RoundArithmetic extends Node {

        @Child private BinaryArithmetic pow;

        @SuppressWarnings("static-method")
        public int op(int op) {
            return op;
        }

        @SuppressWarnings("static-method")
        public double op(double op) {
            return Math.rint(op);
        }

        @SuppressWarnings("static-method")
        public int op(byte op) {
            return op;
        }

        public double opd(double op, int digits) {
            return fround(op, digits);
        }

        public RComplex opd(double re, double im, int digits) {
            return zrround(re, im, digits);
        }

        // The logic for fround and zrround (z_rround) is derived from GNU R.

        private static final int F_MAX_DIGITS = 308; // IEEE constant

        private double fround(double x, double digits) {
            double pow10;
            double sgn;
            double intx;
            int dig;

            if (Double.isNaN(x) || Double.isNaN(digits)) {
                return x + digits;
            }
            if (!RRuntime.isFinite(x)) {
                return x;
            }
            if (digits == Double.POSITIVE_INFINITY) {
                return x;
            } else if (digits == Double.NEGATIVE_INFINITY) {
                return 0.0;
            }

            double dd = digits;
            double xx = x;

            if (dd > F_MAX_DIGITS) {
                dd = F_MAX_DIGITS;
            }

            dig = (int) Math.floor(dd + 0.5);
            if (xx < 0.0) {
                sgn = -1.0;
                xx = -xx;
            } else {
                sgn = 1.0;
            }

            if (dig == 0) {
                return sgn * op(xx);
            } else if (dig > 0) {
                pow10 = rpowdi(10.0, dig);
                intx = Math.floor(xx);
                // System.out.println(String.format("X %.22f RINT1 %.22f POW10 %.22f INTX %.22f",
                // new BigDecimal(x),
                // new BigDecimal(Math.rint((xx - intx) * pow10)), new BigDecimal(pow10),
                // new BigDecimal(intx)));
                return sgn * (intx + Math.rint((xx - intx) * pow10) / pow10);
            } else {
                pow10 = rpowdi(10.0, -dig);
                // System.out.println(String.format("RINT2 %.22f", new BigDecimal(Math.rint(xx /
                // pow10))));
                return sgn * Math.rint(xx / pow10) * pow10;
            }
        }

        private double rpowdi(double x, int n) {
            double result = 1.0;

            if (Double.isNaN(x)) {
                return x;
            }
            if (n != 0) {
                if (!RRuntime.isFinite(x)) {
                    return rpow(x, n);
                }
                int nn = n;
                double xx = x;
                boolean isNeg = (n < 0);
                if (isNeg) {
                    nn = -nn;
                }
                for (;;) {
                    if ((nn & 1) != 0) {
                        result *= xx;
                    }
                    if ((nn >>= 1) != 0) {
                        xx *= xx;
                    } else {
                        break;
                    }
                }
                if (isNeg) {
                    result = 1.0 / result;
                }
            }
            return result;
        }

        private static double myfmod(double x1, double x2) {
            double q = x1 / x2;
            return x1 - Math.floor(q) * x2;
        }

        private double rpow(double x, double y) {
            if (x == 1.0 || y == 0.0) {
                return 1.0;
            }
            if (x == 0.0) {
                if (y > 0.0) {
                    return 0.0;
                }
                return Double.POSITIVE_INFINITY;
            }
            if (RRuntime.isFinite(x) && RRuntime.isFinite(y)) {
                if (pow == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    pow = insert(BinaryArithmetic.POW.createOperation());
                }
                return pow.op(x, y);
            }
            if (Double.isNaN(x) || Double.isNaN(y)) {
                return x + y; // assuming IEEE 754; otherwise return NaN
            }
            if (!RRuntime.isFinite(x)) {
                if (x > 0) { /* Inf ^ y */
                    return (y < 0.0) ? 0.0 : Double.POSITIVE_INFINITY;
                } else { /* (-Inf) ^ y */
                    if (RRuntime.isFinite(y) && y == Math.floor(y)) { /* (-Inf) ^ n */
                        return (y < 0.0) ? 0.0 : (myfmod(y, 2.0) != 0.0 ? x : -x);
                    }
                }
            }
            if (!RRuntime.isFinite(y)) {
                if (x >= 0) {
                    if (y > 0) { /* y == +Inf */
                        return (x >= 1) ? Double.POSITIVE_INFINITY : 0.0;
                    } else {
                        /* y == -Inf */
                        return (x < 1) ? Double.POSITIVE_INFINITY : 0.0;
                    }
                }
            }
            // all other cases: (-Inf)^{+-Inf, non-int}; (neg)^{+-Inf}
            return Double.NaN;
        }

        private RComplex zrround(double re, double im, int digits) {
            return RDataFactory.createComplex(fround(re, digits), fround(im, digits));
        }
    }
}
