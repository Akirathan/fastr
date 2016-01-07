/*
 * Copyright (c) 2013, 2016, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.nodes.function;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.r.nodes.RRootNode;
import com.oracle.truffle.r.nodes.function.PromiseHelperNode.PromiseDeoptimizeFrameNode;
import com.oracle.truffle.r.nodes.function.opt.EagerEvalHelper;
import com.oracle.truffle.r.nodes.instrument.RInstrument;
import com.oracle.truffle.r.runtime.RDeparse;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.RSerialize;
import com.oracle.truffle.r.runtime.data.FastPathFactory;
import com.oracle.truffle.r.runtime.data.RDataFactory;
import com.oracle.truffle.r.runtime.data.RFunction;
import com.oracle.truffle.r.runtime.env.REnvironment;
import com.oracle.truffle.r.runtime.env.frame.FrameSlotChangeMonitor;
import com.oracle.truffle.r.runtime.gnur.SEXPTYPE;
import com.oracle.truffle.r.runtime.nodes.RNode;
import com.oracle.truffle.r.runtime.nodes.RSyntaxNode;

public final class FunctionExpressionNode extends RNode implements RSyntaxNode {

    public static FunctionExpressionNode create(SourceSection src, RootCallTarget callTarget, FastPathFactory fastPath) {
        return new FunctionExpressionNode(src, callTarget, fastPath);
    }

    @CompilationFinal private RootCallTarget callTarget;
    private final PromiseDeoptimizeFrameNode deoptFrameNode;
    private final FastPathFactory fastPath;

    @CompilationFinal private boolean initialized = false;

    private FunctionExpressionNode(SourceSection src, RootCallTarget callTarget, FastPathFactory fastPath) {
        this.fastPath = fastPath;
        assignSourceSection(src);
        this.callTarget = callTarget;
        this.deoptFrameNode = EagerEvalHelper.optExprs() || EagerEvalHelper.optVars() || EagerEvalHelper.optDefault() ? new PromiseDeoptimizeFrameNode() : null;
    }

    @Override
    public RFunction execute(VirtualFrame frame) {
        return executeFunction(frame);
    }

    @Override
    public RFunction executeFunction(VirtualFrame frame) {
        MaterializedFrame matFrame = frame.materialize();
        if (deoptFrameNode != null) {
            // Deoptimize every promise which is now in this frame, as it might leave it's stack
            deoptFrameNode.deoptimizeFrame(matFrame);
        }
        if (!initialized) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            if (!FrameSlotChangeMonitor.hasEnclosingFrameDescriptor(callTarget.getRootNode().getFrameDescriptor(), frame)) {
                RRootNode root = (RRootNode) callTarget.getRootNode();
                root = root.duplicateWithNewFrameDescriptor();
                RootCallTarget newTarget = Truffle.getRuntime().createCallTarget(root);
                callTarget = newTarget;
            }
            FrameSlotChangeMonitor.initializeEnclosingFrame(callTarget.getRootNode().getFrameDescriptor(), frame);
            initialized = true;
        }
        boolean containsDispatch = ((FunctionDefinitionNode) callTarget.getRootNode()).containsDispatch();
        RFunction func = RDataFactory.createFunction(RFunction.NO_NAME, callTarget, null, matFrame, fastPath, containsDispatch);
        if (RInstrument.instrumentingEnabled()) {
            RInstrument.checkDebugRequested(callTarget.toString(), func);
        }
        return func;
    }

    public RootCallTarget getCallTarget() {
        return callTarget;
    }

    @Override
    public void deparseImpl(RDeparse.State state) {
        state.startNodeDeparse(this);
        ((FunctionDefinitionNode) callTarget.getRootNode()).deparseImpl(state);
        state.endNodeDeparse(this);
    }

    @Override
    public void serializeImpl(RSerialize.State state) {
        state.setAsBuiltin("function");
        state.openPairList(SEXPTYPE.LISTSXP);
        FunctionDefinitionNode fdn = (FunctionDefinitionNode) callTarget.getRootNode();
        /*
         * Cannot just serialize fdn, as this needs to generate slightly different output. In
         * particular the body is always a LISTSXP and never shortened.
         */
        fdn.serializeFormals(state);
        state.openPairList(SEXPTYPE.LISTSXP);
        boolean hasBraces = fdn.checkOpenBrace(state);
        fdn.serializeBody(state);
        if (hasBraces) {
            FunctionDefinitionNode.checkCloseBrace(state, hasBraces);
        }
        state.switchCdrToCar();
        state.setCdr(state.closePairList());
        state.setCdr(state.closePairList());
    }

    @Override
    public RSyntaxNode substituteImpl(REnvironment env) {
        FunctionDefinitionNode thisFdn = (FunctionDefinitionNode) callTarget.getRootNode();
        FunctionDefinitionNode fdn = (FunctionDefinitionNode) thisFdn.substituteImpl(env);
        return new FunctionExpressionNode(null, Truffle.getRuntime().createCallTarget(fdn), fastPath);
    }

    public int getRlengthImpl() {
        throw RInternalError.unimplemented();
    }

    @Override
    public Object getRelementImpl(int index) {
        throw RInternalError.unimplemented();
    }

    @Override
    public boolean getRequalsImpl(RSyntaxNode other) {
        throw RInternalError.unimplemented();
    }
}
