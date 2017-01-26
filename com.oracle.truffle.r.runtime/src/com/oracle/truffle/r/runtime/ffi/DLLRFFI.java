/*
 * Copyright (c) 2014, 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.runtime.ffi;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.r.runtime.ffi.DLL.SymbolHandle;

/**
 * Caller should not assume that this interface is implemented in a thread-safe manner. In
 * particular, pairs of {@link DLLRFFINode#dlopen}/{@link DLLRFFINode#dlerror} and
 * {@link DLLRFFINode#dlsym}/{@link DLLRFFINode#dlerror} should be atomic in the caller.
 *
 */
public interface DLLRFFI {
    abstract class DLLRFFINode extends Node {
        /**
         * Open a DLL.
         *
         * @return {@code null} on error, opaque handle for following calls otherwise.
         */
        public abstract Object dlopen(String path, boolean local, boolean now);

        /**
         * Search for {@code symbol} in DLL specified by {@code handle}. To accommodate differing
         * implementations of this interface the result is {@link SymbolHandle}. For the standard OS
         * implementation this will encapsulate a {@link Long} or {@code null} if an error occurred.
         *
         */
        public abstract SymbolHandle dlsym(Object handle, String symbol);

        /**
         * Close DLL specified by {@code handle}.
         */
        public abstract int dlclose(Object handle);

        /**
         * Get any error message.
         *
         * @return {@code null} if no error, message otherwise.
         */
        public abstract String dlerror();
    }

    DLLRFFINode createDLLRFFINode();
}
