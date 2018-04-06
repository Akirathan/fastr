/*
 * Copyright (c) 2017, 2018, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.builtin.casts;

import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.r.nodes.unary.CastNode;
import com.oracle.truffle.r.runtime.data.RInteropScalar;
import com.oracle.truffle.r.runtime.interop.ForeignArray2R;

public final class CastForeignNode extends CastNode {

    @Child private ForeignArray2R foreignArray2R = ForeignArray2R.create();

    private final ConditionProfile isForeign = ConditionProfile.createBinaryProfile();
    private final ConditionProfile isInteropScalar = ConditionProfile.createBinaryProfile();

    @Override
    protected Object execute(Object obj) {
        if (isForeign.profile(foreignArray2R.isForeignVector(obj))) {
            return foreignArray2R.convert(obj);
        } else if (isInteropScalar.profile(isInteropScalar(obj))) {
            return ((RInteropScalar) obj).getRValue();
        } else {
            return obj;
        }
    }

    protected static boolean isInteropScalar(Object obj) {
        return obj instanceof RInteropScalar;
    }
}
