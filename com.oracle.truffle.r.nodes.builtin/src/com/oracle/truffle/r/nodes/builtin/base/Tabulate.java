/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Copyright (c) 2014, Purdue University
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates
 *
 * All rights reserved.
 */
package com.oracle.truffle.r.nodes.builtin.base;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.utilities.BranchProfile;
import com.oracle.truffle.r.nodes.builtin.RBuiltinNode;
import com.oracle.truffle.r.nodes.profile.CountedLoopConditionProfile;
import com.oracle.truffle.r.runtime.RBuiltin;
import com.oracle.truffle.r.runtime.RBuiltinKind;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.data.RDataFactory;
import com.oracle.truffle.r.runtime.data.RFactor;
import com.oracle.truffle.r.runtime.data.RIntVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractIntVector;

@RBuiltin(name = "tabulate", kind = RBuiltinKind.INTERNAL, parameterNames = {"bin", "nbins"})
public abstract class Tabulate extends RBuiltinNode {

    private final BranchProfile errorProfile = BranchProfile.create();
    private final CountedLoopConditionProfile loopProfile = CountedLoopConditionProfile.create();

    @Override
    protected void createCasts(CastBuilder casts) {
        casts.toInteger(1);
    }

    @Specialization
    public RIntVector tabulate(RAbstractIntVector bin, int nBins) {
        controlVisibility();
        if (RRuntime.isNA(nBins) || nBins < 0) {
            errorProfile.enter();
            throw RError.error(this, RError.Message.INVALID_ARGUMENT, "nbin");
        }
        int[] ans = new int[nBins];
        loopProfile.profileLength(bin.getLength());
        for (int i = 0; loopProfile.inject(i < bin.getLength()); i++) {
            int currentEl = bin.getDataAt(i);
            if (!RRuntime.isNA(currentEl) && currentEl > 0 && currentEl <= nBins) {
                ans[currentEl - 1]++;
            }
        }
        return RDataFactory.createIntVector(ans, RDataFactory.COMPLETE_VECTOR);
    }

    @Specialization
    public RIntVector tabulate(RFactor bin, int nBins) {
        return tabulate(bin.getVector(), nBins);
    }

    @SuppressWarnings("unused")
    @Fallback
    @TruffleBoundary
    public RIntVector tabulate(Object bin, int nBins) {
        throw RError.error(this, RError.Message.INVALID_INPUT);
    }
}
