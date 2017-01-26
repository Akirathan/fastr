/*
 * Copyright (c) 2015, 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.runtime.ffi.generic;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.conn.RConnection;
import com.oracle.truffle.r.runtime.data.RLogicalVector;
import com.oracle.truffle.r.runtime.data.RStringVector;
import com.oracle.truffle.r.runtime.env.REnvironment;
import com.oracle.truffle.r.runtime.ffi.CallRFFI;
import com.oracle.truffle.r.runtime.ffi.DLL;
import com.oracle.truffle.r.runtime.ffi.DLL.DLLInfo;
import com.oracle.truffle.r.runtime.ffi.DLL.SymbolHandle;
import com.oracle.truffle.r.runtime.ffi.NativeCallInfo;
import com.oracle.truffle.r.runtime.ffi.RFFIFactory;
import com.oracle.truffle.r.runtime.ffi.ToolsRFFI;

public class Generic_Tools implements ToolsRFFI {
    private static class Generic_ToolsRFFINode extends ToolsRFFINode {
        private static final String C_PARSE_RD = "C_parseRd";
        private static final String TOOLS = "tools";

        @Child private CallRFFI.CallRFFINode callRFFINode = RFFIFactory.getRFFI().getCallRFFI().createCallRFFINode();
        @Child DLL.FindSymbolNode findSymbolNode = DLL.FindSymbolNode.create();

        @CompilationFinal private NativeCallInfo nativeCallInfo;

        /**
         * Invoke C implementation, N.B., code is not thread safe.
         */
        @Override
        public synchronized Object parseRd(RConnection con, REnvironment srcfile, RLogicalVector verbose, RLogicalVector fragment, RStringVector basename, RLogicalVector warningCalls, Object macros,
                        RLogicalVector warndups) {
            try {
                if (nativeCallInfo == null) {
                    // lookup entry point (assert library is loaded)
                    DLLInfo toolsDLLInfo = DLL.findLibrary(TOOLS);
                    assert toolsDLLInfo != null;
                    SymbolHandle symbolHandle = findSymbolNode.execute(C_PARSE_RD, TOOLS, DLL.RegisteredNativeSymbol.any());
                    assert symbolHandle != DLL.SYMBOL_NOT_FOUND;
                    nativeCallInfo = new NativeCallInfo(C_PARSE_RD, symbolHandle, toolsDLLInfo);
                }
                return callRFFINode.invokeCall(nativeCallInfo,
                                new Object[]{con, srcfile, verbose, fragment, basename, warningCalls, macros, warndups});
            } catch (Throwable ex) {
                throw RInternalError.shouldNotReachHere(ex, "error during Rd parsing");
            }
        }
    }

    @Override
    public ToolsRFFINode createToolsRFFINode() {
        return new Generic_ToolsRFFINode();
    }
}
