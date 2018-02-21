/*
 * Copyright (c) 2014, 2018, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.ffi.impl.llvm;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.r.ffi.impl.interop.NativeDoubleArray;
import com.oracle.truffle.r.ffi.impl.interop.NativeIntegerArray;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.ffi.CRFFI;
import com.oracle.truffle.r.runtime.ffi.NativeCallInfo;
import com.oracle.truffle.r.runtime.ffi.interop.NativeRawArray;

class TruffleLLVM_C implements CRFFI {
    private static class TruffleLLVM_InvokeCNode extends InvokeCNode {

        @Child private Node messageNode;
        private int numArgs;

        @Override
        public void execute(NativeCallInfo nativeCallInfo, Object[] args) {
            Object[] wargs = wrap(args);
            try {
                if (messageNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    // TODO: we assume that the number of args doesn't change, is that correct?
                    messageNode = Message.createExecute(args.length).createNode();
                    numArgs = args.length;
                }
                assert numArgs == args.length;
                ForeignAccess.sendExecute(messageNode, nativeCallInfo.address.asTruffleObject(), wargs);
            } catch (InteropException ex) {
                throw RInternalError.shouldNotReachHere(ex);
            }
        }

        Object[] wrap(Object[] args) {
            Object[] nargs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                Object narg;
                if (arg instanceof int[]) {
                    narg = new NativeIntegerArray((int[]) arg);
                } else if (arg instanceof double[]) {
                    narg = new NativeDoubleArray((double[]) arg);
                } else if (arg instanceof byte[]) {
                    narg = new NativeRawArray((byte[]) arg);
                } else if (arg instanceof TruffleObject) {
                    narg = arg;
                } else {
                    throw RInternalError.unimplemented(".C type: " + arg.getClass().getSimpleName());
                }
                nargs[i] = narg;
            }
            return nargs;
        }
    }

    @Override
    public InvokeCNode createInvokeCNode() {
        return new TruffleLLVM_InvokeCNode();
    }
}
