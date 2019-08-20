/*
 * Copyright (c) 2017, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.test.engine.interop;

import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.data.RInteger;
import org.junit.Test;

public class RIntegerInteropTest extends AbstractInteropTest {

    @Test
    @Override
    public void testIsNull() throws Exception {
        super.testIsNull(); // force inherited tests from AbstractInteropTest
    }

    @Override
    protected boolean isNull(TruffleObject obj) {
        assert obj instanceof RInteger;
        return ((RInteger) obj).isNA();
    }

    @Override
    protected int getSize(TruffleObject arg0) {
        return 1;
    }

    @Override
    protected boolean canRead(TruffleObject arg0) {
        return true;
    }

    @Override
    protected Class<? extends InteropException> readException(TruffleObject obj, int index) {
        return UnknownIdentifierException.class;
    }

    @Override
    protected TruffleObject[] createTruffleObjects() throws Exception {
        return new TruffleObject[]{RInteger.valueOf(123), RInteger.createNA()};
    }

    @Override
    protected boolean shouldTestToNative(TruffleObject obj) {
        return true;
    }

    @Override
    protected Object getUnboxed(TruffleObject obj) {
        int unboxed = ((RInteger) obj).getValue();
        return RRuntime.isNA(unboxed) ? null : unboxed;
    }

    @Override
    protected TruffleObject createEmptyTruffleObject() throws Exception {
        return null;
    }
}
