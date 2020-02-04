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

import static com.oracle.truffle.r.library.fastrGrid.GridUtils.getDataAtMod;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.abstractVectorValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.logicalValue;
import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.numericValue;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.r.library.fastrGrid.Unit.UnitConversionContext;
import com.oracle.truffle.r.library.fastrGrid.device.GridDevice;
import com.oracle.truffle.r.library.fastrGrid.device.GridDevice.ImageInterpolation;
import com.oracle.truffle.r.nodes.builtin.RExternalBuiltinNode;
import com.oracle.truffle.r.runtime.RError.Message;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.data.RList;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.data.RStringVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractDoubleVector;
import com.oracle.truffle.r.runtime.data.model.RIntVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractLogicalVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;

/**
 * Draws a raster image at specified position. The image may be matrix of integers, in which case it
 * is used directly, or matrix of any valid color representation, e.g. strings with color codes.
 */
public abstract class LRaster extends RExternalBuiltinNode.Arg8 {
    private static final String NATIVE_RASTER_CLASS = "nativeRaster";

    static {
        Casts casts = new Casts(LRaster.class);
        casts.arg(0).mustBe(abstractVectorValue());
        casts.arg(1).mustBe(abstractVectorValue());
        casts.arg(2).mustBe(abstractVectorValue());
        casts.arg(3).mustBe(abstractVectorValue());
        casts.arg(4).mustBe(abstractVectorValue());
        casts.arg(5).mustBe(numericValue()).asDoubleVector();
        casts.arg(6).mustBe(numericValue()).asDoubleVector();
        casts.arg(7).mustBe(logicalValue()).asLogicalVector();
    }

    public static LRaster create() {
        return LRasterNodeGen.create();
    }

    @Specialization
    @TruffleBoundary
    Object doRaster(RAbstractVector raster, RAbstractVector xVec, RAbstractVector yVec, RAbstractVector widthVec, RAbstractVector heightVec, RAbstractDoubleVector hjust, RAbstractDoubleVector vjust,
                    RAbstractLogicalVector interpolate) {
        GridContext ctx = GridContext.getContext();
        GridDevice dev = ctx.getCurrentDevice();

        RList currentVP = ctx.getGridState().getViewPort();
        GPar gpar = GPar.create(ctx.getGridState().getGpar());
        ViewPortTransform vpTransform = ViewPortTransform.get(currentVP, dev);
        ViewPortContext vpContext = ViewPortContext.fromViewPort(currentVP);
        UnitConversionContext conversionCtx = new UnitConversionContext(vpTransform.size, vpContext, dev, gpar);

        if (vpTransform.rotationAngle != 0) {
            throw RInternalError.unimplemented("L_raster with view-port rotation.");
        }

        int[] pixels;
        if (raster instanceof RIntVector && isNativeRaster(raster)) {
            com.oracle.truffle.r.runtime.data.RIntVector rasterVec = ((RIntVector) raster).materialize();
            pixels = rasterVec.getDataTemp();
        } else {
            int rasterLen = raster.getLength();
            pixels = new int[rasterLen];
            for (int i = 0; i < rasterLen; i++) {
                pixels[i] = GridColorUtils.getColor(raster, i).getRawValue();
            }
        }

        Object dimsObj = raster.getAttr(RRuntime.DIM_ATTR_KEY);
        if (!(dimsObj instanceof RIntVector)) {
            throw RInternalError.shouldNotReachHere("Dims attribute should always be integer vector.");
        }
        RIntVector dims = (RIntVector) dimsObj;
        if (dims.getLength() != 2) {
            throw error(Message.GENERIC, "L_raster dims attribute is not of size 2");
        }

        int length = GridUtils.maxLength(xVec, yVec, widthVec, heightVec);
        for (int i = 0; i < length; i++) {
            Size size = Size.fromUnits(widthVec, heightVec, i, conversionCtx);
            Point origLoc = Point.fromUnits(xVec, yVec, i, conversionCtx);
            Point transLoc = TransformMatrix.transLocation(origLoc, vpTransform.transform);
            Point loc = transLoc.justify(size, getDataAtMod(hjust, i), getDataAtMod(vjust, i));
            if (!loc.isFinite() || !size.isFinite()) {
                continue;
            }
            ImageInterpolation interpolation = getInterpolation(interpolate, i);
            dev.drawRaster(loc.x, loc.y, size.getWidth(), size.getHeight(), pixels, dims.getDataAt(1), interpolation);
        }
        return RNull.instance;
    }

    private static ImageInterpolation getInterpolation(RAbstractLogicalVector interpolation, int idx) {
        if (RRuntime.fromLogical(interpolation.getDataAt(idx % interpolation.getLength()))) {
            return ImageInterpolation.LINEAR_INTERPOLATION;
        }
        return ImageInterpolation.NEAREST_NEIGHBOR;
    }

    private static boolean isNativeRaster(RAbstractVector vec) {
        RStringVector clazz = vec.getClassAttr();
        if (clazz == null) {
            return false;
        }
        for (int i = 0; i < clazz.getLength(); i++) {
            if (clazz.getDataAt(i).equals(NATIVE_RASTER_CLASS)) {
                return true;
            }
        }
        return false;
    }
}
