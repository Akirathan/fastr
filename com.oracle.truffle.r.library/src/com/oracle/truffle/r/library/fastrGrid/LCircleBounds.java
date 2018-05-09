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

import static com.oracle.truffle.r.nodes.builtin.CastBuilder.Predef.numericValue;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.r.library.fastrGrid.EdgeDetection.Bounds;
import com.oracle.truffle.r.library.fastrGrid.Unit.UnitConversionContext;
import com.oracle.truffle.r.library.fastrGrid.device.GridDevice;
import com.oracle.truffle.r.nodes.builtin.RExternalBuiltinNode;
import com.oracle.truffle.r.runtime.data.RList;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;
import com.oracle.truffle.r.runtime.nmath.RMath;

public abstract class LCircleBounds extends RExternalBuiltinNode.Arg4 {

    static {
        Casts casts = new Casts(LCircleBounds.class);
        LCircle.addCircleCasts(casts);
        casts.arg(3).mustBe(numericValue()).asDoubleVector().findFirst();
    }

    public static LCircleBounds create() {
        return LCircleBoundsNodeGen.create();
    }

    @Specialization
    @TruffleBoundary
    Object doCircle(RAbstractVector xVec, RAbstractVector yVec, RAbstractVector radiusVec, double theta) {
        GridContext ctx = GridContext.getContext();
        GridDevice dev = ctx.getCurrentDevice();

        RList currentVP = ctx.getGridState().getViewPort();
        GPar gpar = GPar.create(ctx.getGridState().getGpar());
        ViewPortTransform vpTransform = ViewPortTransform.get(currentVP, dev);
        ViewPortContext vpContext = ViewPortContext.fromViewPort(currentVP);
        UnitConversionContext conversionCtx = new UnitConversionContext(vpTransform.size, vpContext, dev, gpar);

        int length = GridUtils.maxLength(xVec, yVec, radiusVec);
        Bounds bounds = new Bounds();
        int count = 0;
        Point loc = null;  // we remember the last position and radius
        double radius = -1;
        for (int i = 0; i < length; i++) {
            Size radiusSizes = Size.fromUnits(radiusVec, radiusVec, i, conversionCtx);
            radius = RMath.fmin2(radiusSizes.getWidth(), radiusSizes.getHeight());
            loc = Point.fromUnits(xVec, yVec, i, conversionCtx);
            if (loc.isFinite() && Double.isFinite(radius)) {
                bounds.updateX(loc.x - radius, loc.x + radius);
                bounds.updateY(loc.y - radius, loc.y + radius);
                count++;
            }
        }

        if (count == 0) {
            return RNull.instance;
        }
        Point result;
        assert loc != null;
        if (count == 1) {
            result = EdgeDetection.circleEdge(loc, radius, theta);
        } else {
            result = EdgeDetection.rectEdge(bounds.minX, bounds.minY, bounds.maxX, bounds.maxY, theta);
        }

        double scale = ctx.getGridState().getScale();
        return GridUtils.createDoubleVector(result.x / scale, result.y / scale, bounds.getWidth() / scale, bounds.getHeight() / scale);
    }
}
