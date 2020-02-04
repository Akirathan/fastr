/*
 * Copyright (C) 2001-3 Paul Murrell
 * Copyright (c) 1998-2013, The R Core Team
 * Copyright (c) 2017, 2018, Oracle and/or its affiliates
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.R-project.org/Licenses/
 */
package com.oracle.truffle.r.library.fastrGrid;

import static com.oracle.truffle.r.library.fastrGrid.Unit.NATIVE;
import static com.oracle.truffle.r.library.fastrGrid.Unit.NPC;
import static com.oracle.truffle.r.library.fastrGrid.Unit.isArithmeticUnit;
import static com.oracle.truffle.r.library.fastrGrid.Unit.isListUnit;
import static com.oracle.truffle.r.library.fastrGrid.Unit.isSimpleUnit;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.abstractVectorValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.numericValue;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.r.library.fastrGrid.Unit.AxisOrDimension;
import com.oracle.truffle.r.library.fastrGrid.Unit.UnitConversionContext;
import com.oracle.truffle.r.library.fastrGrid.device.GridDevice;
import com.oracle.truffle.r.nodes.builtin.RExternalBuiltinNode;
import com.oracle.truffle.r.runtime.data.RDataFactory;
import com.oracle.truffle.r.runtime.data.RList;
import com.oracle.truffle.r.runtime.data.model.RAbstractDoubleVector;
import com.oracle.truffle.r.runtime.data.RIntVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;

public abstract class LConvert extends RExternalBuiltinNode.Arg4 {

    static {
        Casts casts = new Casts(LConvert.class);
        casts.arg(0).mustBe(abstractVectorValue());
        casts.arg(1).mustBe(numericValue()).asIntegerVector();
        casts.arg(2).mustBe(numericValue()).asIntegerVector();
        casts.arg(3).mustBe(numericValue()).asIntegerVector();
    }

    public static LConvert create() {
        return LConvertNodeGen.create();
    }

    @Specialization
    @TruffleBoundary
    Object doConvert(RAbstractVector units, RIntVector axisFromVec, RIntVector axisToVec, RIntVector unitToVec) {

        GridContext ctx = GridContext.getContext();
        GridDevice dev = ctx.getCurrentDevice();

        RList currentVP = ctx.getGridState().getViewPort();
        GPar gpar = GPar.create(ctx.getGridState().getGpar());
        ViewPortTransform vpTransform = ViewPortTransform.get(currentVP, dev);
        ViewPortContext vpContext = ViewPortContext.fromViewPort(currentVP);
        UnitConversionContext conversionCtx = new UnitConversionContext(vpTransform.size, vpContext, dev, gpar);

        int length = Unit.getLength(units);
        double[] result = new double[length];

        RIntVector unitIds = null;
        if (isSimpleUnit(units)) {
            unitIds = GridUtils.asIntVector(units.getAttr(Unit.VALID_UNIT_ATTR));
        }
        boolean fromUnitIsSimple = !isArithmeticUnit(units) && !isListUnit(units);

        for (int i = 0; i < length; i++) {
            // scalar values used in current iteration
            AxisOrDimension axisFrom = AxisOrDimension.fromInt(axisFromVec.getDataAt(i % axisFromVec.getLength()));
            AxisOrDimension axisTo = AxisOrDimension.fromInt(axisToVec.getDataAt(i % axisToVec.getLength()));
            boolean compatibleAxes = axisFrom.isHorizontal() == axisTo.isHorizontal();
            double vpToSize = axisTo.isHorizontal() ? vpTransform.size.getWidth() : vpTransform.size.getHeight();
            double vpFromSize = axisFrom.isHorizontal() ? vpTransform.size.getWidth() : vpTransform.size.getHeight();
            int unitTo = unitToVec.getDataAt(i % unitToVec.getLength());
            int fromUnitId = -1;    // invalid, and definitely not relative unit
            if (unitIds != null) {
                fromUnitId = unitIds.getDataAt(i % unitIds.getLength());
            }

            // actual conversion:
            // if the units are both relative, we are converting compatible axes and the vpSize for
            // 'from' axis is small, we will not convert through inches, but directly to avoid
            // divide by zero, but still do something useful
            boolean bothRelative = isRelative(unitTo) && isRelative(fromUnitId);
            boolean realativeConversion = bothRelative && fromUnitIsSimple && compatibleAxes && vpFromSize < 1e-6;
            if (realativeConversion) {
                // if the unit is not "unit.arithmetic" or "unit.list", it must be double vector
                RAbstractDoubleVector simpleUnits = (RAbstractDoubleVector) units;
                double fromValue = simpleUnits.getDataAt(i % simpleUnits.getLength());
                result[i] = transformFromNPC(tranfromToNPC(fromValue, fromUnitId, axisFrom, vpContext), unitTo, axisTo, vpContext);
            } else {
                double inches = toInches(units, i, axisFrom, conversionCtx);
                boolean isX = axisTo.isHorizontal();
                double scalemin = isX ? vpContext.xscalemin : vpContext.yscalemin;
                double scalemax = isX ? vpContext.xscalemax : vpContext.yscalemax;
                result[i] = Unit.convertFromInches(inches, unitTo, vpToSize, scalemin, scalemax, axisTo.isDimension(), gpar.getDrawingContext(i));
            }
        }

        return RDataFactory.createDoubleVector(result, RDataFactory.COMPLETE_VECTOR);
    }

    private static double toInches(RAbstractVector units, int index, AxisOrDimension axisFrom, UnitConversionContext conversionCtx) {
        double inches;
        if (axisFrom.isHorizontal()) {
            if (axisFrom.isDimension()) {
                inches = Unit.convertWidth(units, index, conversionCtx);
            } else {
                inches = Unit.convertX(units, index, conversionCtx);
            }
        } else {
            if (axisFrom.isDimension()) {
                inches = Unit.convertHeight(units, index, conversionCtx);
            } else {
                inches = Unit.convertY(units, index, conversionCtx);
            }
        }
        return inches;
    }

    private static double tranfromToNPC(double value, int fromUnitId, AxisOrDimension axisFrom, ViewPortContext vpContext) {
        if (fromUnitId == Unit.NPC) {
            return value;
        }
        assert fromUnitId == Unit.NATIVE : "relative conversion should only happen when units are NPC or NATIVE";
        boolean isX = axisFrom.isHorizontal();
        double min = isX ? vpContext.xscalemin : vpContext.yscalemin;
        double max = isX ? vpContext.xscalemax : vpContext.yscalemax;
        if (axisFrom.isDimension()) {
            return value / (max - min);
        } else {
            return (value - min) / (max - min);
        }
    }

    private static double transformFromNPC(double value, int unitTo, AxisOrDimension axisTo, ViewPortContext vpContext) {
        if (unitTo == Unit.NPC) {
            return value;
        }
        assert unitTo == Unit.NATIVE : "relative conversion should only happen when units are NPC or NATIVE";
        boolean isX = axisTo.isHorizontal();
        double min = isX ? vpContext.xscalemin : vpContext.yscalemin;
        double max = isX ? vpContext.xscalemax : vpContext.yscalemax;
        if (axisTo.isDimension()) {
            return value * (max - min);
        } else {
            return min + value * (max - min);
        }
    }

    // Note: this is not relative in the same sense as IsRelativeUnitNode. The later checks for
    // special NULL unit used only in layouting.
    private static boolean isRelative(int unitId) {
        return unitId == NPC || unitId == NATIVE;
    }
}
