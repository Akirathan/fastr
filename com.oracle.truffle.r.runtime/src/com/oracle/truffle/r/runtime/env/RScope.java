/*
 * Copyright (c) 2014, 2018, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.runtime.env;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Scope;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.KeyInfo;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.r.runtime.ArgumentsSignature;
import com.oracle.truffle.r.runtime.RArguments;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.context.RContext;
import com.oracle.truffle.r.runtime.data.RFunction;
import com.oracle.truffle.r.runtime.data.RStringVector;
import com.oracle.truffle.r.runtime.env.REnvironment.PutException;
import com.oracle.truffle.r.runtime.env.frame.REnvFrameAccess;
import com.oracle.truffle.r.runtime.env.frame.REnvTruffleFrameAccess;
import com.oracle.truffle.r.runtime.interop.Foreign2R;
import com.oracle.truffle.r.runtime.interop.R2Foreign;

/**
 * Represents a variable scope for external tools like a debugger.<br>
 * This is basically a view on R environments.
 */
public final class RScope {

    private final Node current;
    private REnvironment env;

    /**
     * Intended to be used when creating a parent scope where we do not know any associated node.
     */
    private RScope(REnvironment env) {
        this.env = env;
        this.current = null;
    }

    private RScope(Node current, REnvironment env) {
        this.current = current;
        this.env = env;
    }

    protected String getName() {
        // just to be sure
        if (env == REnvironment.emptyEnv()) {
            return "empty environment";
        }

        assert env.getFrame() != null;
        RFunction function = RArguments.getFunction(env.getFrame());
        if (function != null) {
            String name = function.getName();
            return "function environment" + (name != null ? " for function " + name : "");
        } else {
            String name = env.getName();
            return "explicit environment" + (name != null ? ": " + name : "");
        }
    }

    protected Node getNode() {
        return current;
    }

    protected Object getVariables() {
        return new EnvVariablesObject(env, false);
    }

    private static REnvironment getEnv(Frame frame) {
        if (RArguments.isRFrame(frame)) {
            return REnvironment.frameToEnvironment(frame.materialize());
        }
        return null;
    }

    protected Object getArguments() {
        return new EnvVariablesObject(env, true);
    }

    protected RScope findParent() {
        if (env == REnvironment.emptyEnv() || env.getParent() == REnvironment.emptyEnv()) {
            return null;
        }
        return new RScope(env.getParent());
    }

    public static Iterable<Scope> createLocalScopes(RContext context, Node node, Frame frame) {
        REnvironment env = getEnv(frame);
        if (env == context.stateREnvironment.getGlobalEnv()) {
            return Collections.emptySet();
        }
        if (env != null && env != REnvironment.emptyEnv()) {
            RScope scope = new RScope(node.getRootNode(), env);
            return createScopes(scope, context.stateREnvironment.getGlobalEnv());
        }
        MaterializedFrame mFrame = frame.materialize();
        String name = node.getRootNode().getName();
        if (name == null) {
            name = "local";
        }
        return Collections.singleton(Scope.newBuilder(name, new GenericVariablesObject(mFrame, false)).node(node).arguments(new GenericVariablesObject(mFrame, true)).build());
    }

    public static Iterable<Scope> createTopScopes(RContext context) {
        REnvironment env = context.stateREnvironment.getGlobalEnv();
        RScope scope = new RScope(env);
        return createScopes(scope, null);
    }

    private static Iterable<Scope> createScopes(RScope scope, REnvironment toEnv) {
        return new Iterable<Scope>() {
            @Override
            public Iterator<Scope> iterator() {
                return new Iterator<Scope>() {
                    private RScope previousScope;
                    private RScope nextScope = scope;

                    @Override
                    public boolean hasNext() {
                        if (nextScope == null) {
                            nextScope = previousScope.findParent();
                            if (nextScope != null && nextScope.env == toEnv) {
                                nextScope = null;
                            }
                        }
                        return nextScope != null;
                    }

                    @Override
                    public Scope next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        Scope vscope = Scope.newBuilder(nextScope.getName(), nextScope.getVariables()).node(nextScope.getNode()).arguments(nextScope.getArguments()).build();
                        previousScope = nextScope;
                        nextScope = null;
                        return vscope;
                    }
                };
            }
        };
    }

    /**
     * Explicitly convert some known types to interop types.
     */
    private static Object getInteropValue(Object obj) {
        if (obj instanceof Frame) {
            MaterializedFrame materialized = ((Frame) obj).materialize();
            assert RArguments.isRFrame(materialized);
            return REnvironment.frameToEnvironment(materialized);
        }
        return obj;
    }

    abstract static class VariablesObject implements TruffleObject {

        private final REnvFrameAccess frameAccess;
        private final boolean argumentsOnly;

        private VariablesObject(REnvFrameAccess frameAccess, boolean argumentsOnly) {
            this.frameAccess = frameAccess;
            this.argumentsOnly = argumentsOnly;
        }

        @Override
        public ForeignAccess getForeignAccess() {
            return VariablesMessageResolutionForeign.ACCESS;
        }

        private String[] ls() {
            RStringVector ls = frameAccess.ls(true, null, false);
            // we make a defensive copy, another option would be to make the vector shared and reuse
            // the underlying array
            return ls.getDataCopy();
        }

        public static boolean isInstance(TruffleObject obj) {
            return obj instanceof VariablesObject;
        }

        protected abstract String[] collectArgs();

        protected abstract Object getArgument(String name);

        @MessageResolution(receiverType = VariablesObject.class)
        static final class VariablesMessageResolution {

            @Resolve(message = "KEYS")
            abstract static class VarsMapKeysNode extends Node {

                @TruffleBoundary
                public Object access(VariablesObject varMap) {
                    String[] names = null;
                    if (varMap.argumentsOnly) {
                        names = varMap.collectArgs();
                    } else {
                        names = varMap.ls();
                    }
                    return new ArgumentNamesObject(names);
                }
            }

            @Resolve(message = "KEY_INFO")
            public abstract static class VarMapsKeyInfoNode extends Node {

                @TruffleBoundary
                protected Object access(VariablesObject receiver, String identifier) {
                    int result = KeyInfo.READABLE;
                    if (!receiver.frameAccess.bindingIsLocked(identifier)) {
                        result |= KeyInfo.MODIFIABLE;
                    }
                    if (receiver.frameAccess.get(identifier) instanceof RFunction) {
                        result |= KeyInfo.INVOCABLE;
                    }
                    return result;
                }
            }

            @Resolve(message = "READ")
            abstract static class VarsMapReadNode extends Node {
                @Child private R2Foreign r2Foreign = R2Foreign.create();

                @TruffleBoundary
                public Object access(VariablesObject varMap, String name) {
                    if (varMap.frameAccess == null) {
                        throw UnsupportedMessageException.raise(Message.READ);
                    }
                    Object value = varMap.frameAccess.get(name);
                    if (value == null) {
                        // internal builtin argument?
                        value = ((EnvVariablesObject) varMap).getArgument(name);
                    }

                    // If Java-null is returned, the identifier does not exist !
                    if (value == null) {
                        throw UnknownIdentifierException.raise(name);
                    } else {
                        return r2Foreign.execute(getInteropValue(value));
                    }
                }
            }

            @Resolve(message = "WRITE")
            abstract static class VarsMapWriteNode extends Node {
                @Child private Foreign2R foreign2R = Foreign2R.create();

                @TruffleBoundary
                public Object access(VariablesObject varMap, String name, Object value) {
                    if (varMap.frameAccess == null) {
                        throw UnsupportedMessageException.raise(Message.WRITE);
                    }
                    try {
                        varMap.frameAccess.put(name, foreign2R.execute(value));
                        return value;
                    } catch (PutException e) {
                        throw RInternalError.shouldNotReachHere(e);
                    }
                }
            }
        }
    }

    static final class EnvVariablesObject extends VariablesObject {

        private final REnvironment env;

        private EnvVariablesObject(REnvironment env, boolean argumentsOnly) {
            super(new REnvTruffleFrameAccess(env.getFrame()), argumentsOnly);
            this.env = env;
        }

        @Override
        protected String[] collectArgs() {
            if (env != REnvironment.emptyEnv()) {
                assert RArguments.isRFrame(env.getFrame());
                RFunction f = RArguments.getFunction(env.getFrame());
                if (f != null) {
                    ArgumentsSignature signature = RContext.getRRuntimeASTAccess().getArgumentsSignature(f);
                    String[] names = signature.getNames();
                    return names == null ? new String[signature.getLength()] : names;
                } else {
                    ArgumentsSignature suppliedSignature = RArguments.getSuppliedSignature(env.getFrame());
                    if (suppliedSignature != null) {
                        String[] names = suppliedSignature.getNames();
                        return names == null ? new String[suppliedSignature.getLength()] : names;
                    }
                }
            }
            return new String[0];
        }

        @Override
        public Object getArgument(String name) {
            if (env != REnvironment.emptyEnv()) {
                assert RArguments.isRFrame(env.getFrame());
                RFunction f = RArguments.getFunction(env.getFrame());
                ArgumentsSignature signature;
                if (f != null) {
                    signature = RContext.getRRuntimeASTAccess().getArgumentsSignature(f);
                } else {
                    signature = RArguments.getSuppliedSignature(env.getFrame());
                }
                for (int i = 0; i < signature.getLength(); i++) {
                    if (name.equals(signature.getName(i))) {
                        return RArguments.getArgument(env.getFrame(), i);
                    }
                }
            }
            return null;
        }

    }

    static final class GenericVariablesObject extends VariablesObject {

        private GenericVariablesObject(MaterializedFrame frame, boolean argumentsOnly) {
            super(new REnvTruffleFrameAccess(frame), argumentsOnly);
        }

        @Override
        protected String[] collectArgs() {
            return new String[0];
        }

        @Override
        protected Object getArgument(String name) {
            return null;
        }

    }

    static final class ArgumentNamesObject implements TruffleObject {

        private final String[] names;

        private ArgumentNamesObject(String[] names) {
            this.names = names;
        }

        @Override
        public ForeignAccess getForeignAccess() {
            return ArgumentNamesMessageResolutionForeign.ACCESS;
        }

        public static boolean isInstance(TruffleObject obj) {
            return obj instanceof ArgumentNamesObject;
        }

        @MessageResolution(receiverType = ArgumentNamesObject.class)
        static final class ArgumentNamesMessageResolution {

            @Resolve(message = "HAS_SIZE")
            abstract static class ArgNamesHasSizeNode extends Node {

                public Object access(@SuppressWarnings("unused") ArgumentNamesObject varNames) {
                    return true;
                }
            }

            @Resolve(message = "GET_SIZE")
            abstract static class ArgNamesGetSizeNode extends Node {

                public Object access(ArgumentNamesObject varNames) {
                    return varNames.names.length;
                }
            }

            @Resolve(message = "READ")
            abstract static class ArgNamesReadNode extends Node {

                @TruffleBoundary
                public Object access(ArgumentNamesObject varNames, int index) {
                    String[] names = varNames.names;
                    if (index >= 0 && index < names.length) {
                        return names[index];
                    } else {
                        throw UnknownIdentifierException.raise(Integer.toString(index));
                    }
                }
            }
        }
    }
}
