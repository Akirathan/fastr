/*
 * Copyright (c) 2017, 2018, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.ffi.impl.upcalls;

import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.r.ffi.processor.RFFICpointer;
import com.oracle.truffle.r.ffi.processor.RFFICstring;

public interface DLLUpCallsRFFI {

    /**
     * This is the start, called from {@code R_RegisterRoutines}.
     *
     * @param dllInfo library the symbols are defined in
     * @param nstOrd the ordinal value corresponding to
     *            {@link com.oracle.truffle.r.runtime.ffi.DLL.NativeSymbolType}.
     * @param num the number of functions being registered
     * @param routines the C address of the function table (not interpreted).
     */
    int registerRoutines(Object dllInfo, int nstOrd, int num, @RFFICpointer Object routines);

    /**
     * Internal upcall used by {@code Rdynload_setSymbol}. The {@code fun} value must be converted
     * to a {@link TruffleObject} representing the symbol}.
     *
     * @param dllInfo library the symbol is defined in
     * @param name name of function
     * @param fun a representation of the the C address of the function (in the table)
     * @param numArgs the number of arguments the function takes.
     */
    Object setDotSymbolValues(Object dllInfo, @RFFICstring String name, @RFFICpointer Object fun, int numArgs);

    /**
     * Directly implements {@code R_useDynamicSymbols}.
     */
    int useDynamicSymbols(Object dllInfo, int value);

    /**
     * Directly implements {@code R_forceSymbols}.
     */
    int forceSymbols(Object dllInfo, int value);

    /**
     * Directly implements {@code R_RegisterCCallable}.
     */
    int registerCCallable(@RFFICstring String pkgName, @RFFICstring String functionName, @RFFICpointer Object fun);

    /**
     * Directly implements {@code R_GetCCallable}.
     */
    @RFFICpointer
    Object getCCallable(@RFFICstring String pkgName, @RFFICstring String functionName);

    /**
     * Returns special {@link com.oracle.truffle.r.runtime.ffi.DLL.DLLInfo} instance that is
     * supposed to be used when registering symbols from within code embedding R, i.e. code that
     * cannot have its init method called by R runtime and must call {@code R_registerRoutines}
     * itself.
     */
    Object getEmbeddingDLLInfo();
}
