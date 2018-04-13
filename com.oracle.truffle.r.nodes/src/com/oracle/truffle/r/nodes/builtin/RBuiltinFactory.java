/*
 * Copyright (c) 2013, 2018, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.builtin;

import java.util.function.Supplier;

import com.oracle.truffle.r.runtime.ArgumentsSignature;
import com.oracle.truffle.r.runtime.RDispatch;
import com.oracle.truffle.r.runtime.RVisibility;
import com.oracle.truffle.r.runtime.builtins.RBehavior;
import com.oracle.truffle.r.runtime.builtins.RBuiltinDescriptor;
import com.oracle.truffle.r.runtime.builtins.RBuiltinKind;
import com.oracle.truffle.r.runtime.builtins.RSpecialFactory;

public final class RBuiltinFactory extends RBuiltinDescriptor {

    private final Supplier<RBuiltinNode> constructor;

    RBuiltinFactory(String name, Class<?> builtinMetaClass, Class<?> builtinNodeClass, RVisibility visibility, String[] aliases, RBuiltinKind kind, ArgumentsSignature signature, int[] nonEvalArgs,
                    boolean splitCaller,
                    boolean alwaysSplit, RDispatch dispatch, String genericName, Supplier<RBuiltinNode> constructor, RBehavior behavior, RSpecialFactory specialCall) {
        super(name, builtinMetaClass, builtinNodeClass, visibility, aliases, kind, signature, nonEvalArgs, splitCaller, alwaysSplit, dispatch, genericName, behavior, specialCall);
        this.constructor = constructor;
    }

    public Supplier<RBuiltinNode> getConstructor() {
        return constructor;
    }
}
