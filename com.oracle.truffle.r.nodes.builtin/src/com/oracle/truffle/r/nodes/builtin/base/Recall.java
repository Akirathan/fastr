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
package com.oracle.truffle.r.nodes.builtin.base;

import static com.oracle.truffle.r.runtime.RVisibility.CUSTOM;
import static com.oracle.truffle.r.runtime.builtins.RBehavior.COMPLEX;
import static com.oracle.truffle.r.runtime.builtins.RBuiltinKind.INTERNAL;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.r.nodes.access.variables.LocalReadVariableNode;
import com.oracle.truffle.r.nodes.builtin.RBuiltinNode;
import com.oracle.truffle.r.nodes.function.GetCallerFrameNode;
import com.oracle.truffle.r.nodes.function.call.RExplicitCallNode;
import com.oracle.truffle.r.runtime.ArgumentsSignature;
import com.oracle.truffle.r.runtime.RArguments;
import com.oracle.truffle.r.runtime.RError;
import com.oracle.truffle.r.runtime.builtins.RBuiltin;
import com.oracle.truffle.r.runtime.data.RArgsValuesAndNames;
import com.oracle.truffle.r.runtime.data.RFunction;

/**
 * The {@code Recall} {@code .Internal}.
 */
@RBuiltin(name = "Recall", visibility = CUSTOM, kind = INTERNAL, parameterNames = {"..."}, nonEvalArgs = {0}, behavior = COMPLEX)
public abstract class Recall extends RBuiltinNode.Arg1 {

    @Child private LocalReadVariableNode readArgs = LocalReadVariableNode.create(ArgumentsSignature.VARARG_NAME, false);

    @Child private GetCallerFrameNode callerFrame = new GetCallerFrameNode();

    @Child private RExplicitCallNode call = RExplicitCallNode.create();

    static {
        Casts.noCasts(Recall.class);
    }

    @Specialization
    protected Object recall(VirtualFrame frame, @SuppressWarnings("unused") RArgsValuesAndNames args) {
        Frame cframe = callerFrame.execute(frame);
        RFunction function = RArguments.getFunction(cframe);
        if (function == null) {
            throw error(RError.Message.RECALL_CALLED_OUTSIDE_CLOSURE);
        }
        /*
         * The args passed to the Recall internal are ignored, they are always "...". Instead, this
         * builtin looks at the arguments passed to the surrounding function.
         */
        RArgsValuesAndNames actualArgs = (RArgsValuesAndNames) readArgs.execute(frame);
        return call.call(frame, function, actualArgs);
    }
}
