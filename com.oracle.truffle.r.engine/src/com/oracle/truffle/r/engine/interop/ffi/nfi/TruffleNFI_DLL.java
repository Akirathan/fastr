/*
 * Copyright (c) 2017, 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.engine.interop.ffi.nfi;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.context.RContext;
import com.oracle.truffle.r.runtime.ffi.DLL;
import com.oracle.truffle.r.runtime.ffi.DLL.SymbolHandle;
import com.oracle.truffle.r.runtime.ffi.DLLRFFI;

public class TruffleNFI_DLL implements DLLRFFI {

    static class NFIHandle {
        @SuppressWarnings("unused") private final String libName;
        final TruffleObject libHandle;

        NFIHandle(String libName, TruffleObject libHandle) {
            this.libName = libName;
            this.libHandle = libHandle;
        }
    }

    private static class TruffleNFI_DLOpenNode extends DLLRFFI.DLOpenNode {

        @Override
        public Object execute(String path, boolean local, boolean now) {
            String libName = DLL.libName(path);
            PolyglotEngine engine = RContext.getInstance().getVM();
            TruffleObject libHandle = engine.eval(Source.newBuilder(prepareLibraryOpen(path, local, now)).name(path).mimeType("application/x-native").build()).as(TruffleObject.class);
            return new NFIHandle(libName, libHandle);
        }
    }

    @TruffleBoundary
    private static String prepareLibraryOpen(String path, boolean local, boolean now) {
        StringBuilder sb = new StringBuilder("load");
        sb.append("(");
        sb.append(local ? "RTLD_LOCAL" : "RTLD_GLOBAL");
        sb.append('|');
        sb.append(now ? "RTLD_NOW" : "RTLD_LAZY");
        sb.append(")");
        sb.append(' ');
        sb.append(path);
        return sb.toString();
    }

    private static class TruffleNFI_DLSymNode extends DLLRFFI.DLSymNode {

        @Override
        public SymbolHandle execute(Object handle, String symbol) {
            assert handle instanceof NFIHandle;
            NFIHandle nfiHandle = (NFIHandle) handle;
            Node lookupSymbol = Message.READ.createNode();
            try {
                TruffleObject result = (TruffleObject) ForeignAccess.sendRead(lookupSymbol, nfiHandle.libHandle, symbol);
                return new SymbolHandle(result);
            } catch (UnknownIdentifierException e) {
                return null;
            } catch (InteropException e) {
                throw RInternalError.shouldNotReachHere();
            }
        }
    }

    private static class TruffleNFI_DLCloseNode extends DLLRFFI.DLCloseNode {

        @Override
        public int execute(Object handle) {
            assert handle instanceof NFIHandle;
            // TODO
            return 0;
        }

    }

    @Override
    public DLOpenNode createDLOpenNode() {
        return new TruffleNFI_DLOpenNode();
    }

    @Override
    public DLSymNode createDLSymNode() {
        return new TruffleNFI_DLSymNode();
    }

    @Override
    public DLCloseNode createDLCloseNode() {
        return new TruffleNFI_DLCloseNode();
    }
}
