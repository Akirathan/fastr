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
package com.oracle.truffle.r.ffi.impl.upcalls;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.data.NativeDataAccess;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.data.RTruffleObject;

/**
 * Unwraps a value that is arriving from the native side. This unwrapping should only happen for
 * arguments and return values that represent R data structures, not for primitive values.
 */
public final class FFIUnwrapNode extends Node {

    @Child private Node isPointer;
    @Child private Node asPointer;

    @Child private Node isBoxed;
    @Child private Node unbox;

    private final BranchProfile isRTruffleObject = BranchProfile.create();
    private final BranchProfile isNonBoxed = BranchProfile.create();
    private final BranchProfile isString = BranchProfile.create();

    public Object execute(Object x) {
        if (x instanceof RTruffleObject) {
            isRTruffleObject.enter();
            return x;
        } else if (x instanceof TruffleObject) {
            TruffleObject xTo = (TruffleObject) x;
            Node isPointerNode = isPointer;
            if (isPointerNode == null || ForeignAccess.sendIsPointer(isPointerNode, xTo)) {
                if (asPointer == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    asPointer = insert(Message.AS_POINTER.createNode());
                }
                try {
                    long address = ForeignAccess.sendAsPointer(asPointer, xTo);
                    if (address == 0) {
                        // Users are expected to use R_NULL, but at least when embedding, GNU R
                        // seems to be tolerant to NULLs.
                        return RNull.instance;
                    }
                    return NativeDataAccess.lookup(address);
                } catch (UnsupportedMessageException e) {
                    if (isPointerNode == null) {
                        // only create IS_POINTER if we've seen AS_POINTER failing
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        isPointer = insert(Message.IS_POINTER.createNode());
                    } else {
                        throw RInternalError.shouldNotReachHere(e);
                    }
                }
            }
            Node isBoxedNode = isBoxed;
            if (isBoxedNode == null || ForeignAccess.sendIsBoxed(isBoxedNode, xTo)) {
                if (unbox == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    unbox = insert(Message.UNBOX.createNode());
                }
                try {
                    return ForeignAccess.sendUnbox(unbox, xTo);
                } catch (UnsupportedMessageException e) {
                    if (isBoxedNode == null) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        isBoxed = insert(Message.IS_BOXED.createNode());
                    } else {
                        throw RInternalError.shouldNotReachHere(e);
                    }
                }
            }
            isNonBoxed.enter();
            return x;
        } else if (x instanceof String) {
            isString.enter();
            return x;
        } else {
            CompilerDirectives.transferToInterpreter();
            throw RInternalError.shouldNotReachHere("unexpected primitive value of class " + x.getClass().getSimpleName());
        }

    }

    public static FFIUnwrapNode create() {
        return new FFIUnwrapNode();
    }

    private static final FFIUnwrapNode unwrap = new FFIUnwrapNode();

    public static Object unwrap(Object value) {
        CompilerAsserts.neverPartOfCompilation();
        return unwrap.execute(value);
    }
}
