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
package com.oracle.truffle.r.ffi.impl.nfi;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.context.RContext;
import com.oracle.truffle.r.runtime.ffi.NativeFunction;
import com.oracle.truffle.r.runtime.ffi.RFFIContext;
import com.oracle.truffle.r.runtime.ffi.RFFIFactory;
import com.oracle.truffle.r.runtime.ffi.UserRngRFFI;

public class TruffleNFI_UserRng implements UserRngRFFI {

    private abstract static class RNGNode extends Node {

        @CompilationFinal protected Node userFunctionNode;
        @CompilationFinal protected Node readPointerNode;
        @CompilationFinal protected TruffleObject userFunctionTarget;
        @CompilationFinal protected TruffleObject readPointerTarget;

        protected void init(NativeFunction userFunction, NativeFunction readFunction) {
            if (userFunctionNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                userFunctionNode = insert(Message.EXECUTE.createNode());
            }
            if (userFunctionTarget == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                userFunctionTarget = TruffleNFI_Context.getInstance().lookupNativeFunction(userFunction);
            }
            if (readFunction != null) {
                if (readPointerNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    readPointerNode = insert(Message.EXECUTE.createNode());
                }
                if (readPointerTarget == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    readPointerTarget = TruffleNFI_Context.getInstance().lookupNativeFunction(readFunction);
                }
            }
        }
    }

    private static final class TruffleNFI_InitNode extends RNGNode implements InitNode {

        @Override
        public void execute(VirtualFrame frame, int seed) {
            init(NativeFunction.unif_init, null);
            RFFIContext stateRFFI = RContext.getInstance().getStateRFFI();
            Object before = stateRFFI.beforeDowncall(frame, RFFIFactory.Type.NFI);
            try {
                ForeignAccess.sendExecute(userFunctionNode, userFunctionTarget, seed);
            } catch (InteropException ex) {
                throw RInternalError.shouldNotReachHere(ex);
            } finally {
                stateRFFI.afterDowncall(before, RFFIFactory.Type.NFI);
            }
        }
    }

    private static final class TruffleNFI_RandNode extends RNGNode implements RandNode {

        @Override
        public double execute(VirtualFrame frame) {
            init(NativeFunction.unif_rand, NativeFunction.read_pointer_double);
            RFFIContext stateRFFI = RContext.getInstance().getStateRFFI();
            Object before = stateRFFI.beforeDowncall(frame, RFFIFactory.Type.NFI);
            try {
                Object address = ForeignAccess.sendExecute(userFunctionNode, userFunctionTarget);
                return (double) ForeignAccess.sendExecute(readPointerNode, readPointerTarget, address);
            } catch (InteropException ex) {
                throw RInternalError.shouldNotReachHere(ex);
            } finally {
                stateRFFI.afterDowncall(before, RFFIFactory.Type.NFI);
            }
        }
    }

    private static final class TruffleNFI_NSeedNode extends RNGNode implements NSeedNode {

        @Override
        public int execute(VirtualFrame frame) {
            init(NativeFunction.unif_nseed, NativeFunction.read_pointer_int);
            RFFIContext stateRFFI = RContext.getInstance().getStateRFFI();
            Object before = stateRFFI.beforeDowncall(frame, RFFIFactory.Type.NFI);
            try {
                Object address = ForeignAccess.sendExecute(userFunctionNode, userFunctionTarget);
                return (int) ForeignAccess.sendExecute(readPointerNode, readPointerTarget, address);
            } catch (InteropException ex) {
                throw RInternalError.shouldNotReachHere(ex);
            } finally {
                stateRFFI.afterDowncall(before, RFFIFactory.Type.NFI);
            }
        }
    }

    private static final class TruffleNFI_SeedsNode extends RNGNode implements SeedsNode {

        @Override
        public void execute(VirtualFrame frame, int[] n) {
            init(NativeFunction.unif_seedloc, NativeFunction.read_array_int);
            RFFIContext stateRFFI = RContext.getInstance().getStateRFFI();
            Object before = stateRFFI.beforeDowncall(frame, RFFIFactory.Type.NFI);
            try {
                Object address = ForeignAccess.sendExecute(userFunctionNode, userFunctionTarget);
                for (int i = 0; i < n.length; i++) {
                    n[i] = (int) ForeignAccess.sendExecute(readPointerNode, readPointerTarget, address, i);
                }
            } catch (InteropException ex) {
                throw RInternalError.shouldNotReachHere(ex);
            } finally {
                stateRFFI.afterDowncall(before, RFFIFactory.Type.NFI);
            }
        }
    }

    @Override
    public InitNode createInitNode() {
        return new TruffleNFI_InitNode();
    }

    @Override
    public RandNode createRandNode() {
        return new TruffleNFI_RandNode();
    }

    @Override
    public NSeedNode createNSeedNode() {
        return new TruffleNFI_NSeedNode();
    }

    @Override
    public SeedsNode createSeedsNode() {
        return new TruffleNFI_SeedsNode();
    }
}
