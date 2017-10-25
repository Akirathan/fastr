package com.oracle.truffle.r.runtime.data;

import java.util.WeakHashMap;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.r.runtime.RCaller;
import com.oracle.truffle.r.runtime.Utils;
import com.oracle.truffle.r.runtime.VirtualEvalFrame;
import com.oracle.truffle.r.runtime.context.RContext;
import com.oracle.truffle.r.runtime.data.model.RAbstractStringVector;
import com.oracle.truffle.r.runtime.env.REnvironment;
import com.oracle.truffle.r.runtime.nodes.RBaseNode;
import com.oracle.truffle.r.runtime.nodes.RNode;
import com.oracle.truffle.r.runtime.nodes.RSyntaxConstant;
import com.oracle.truffle.r.runtime.nodes.RSyntaxLookup;

/**
 * A closure for creating promises and languages.
 */
public final class Closure {
    private WeakHashMap<FrameDescriptor, RootCallTarget> callTargets;

    public static final String PROMISE_CLOSURE_WRAPPER_NAME = new String("<promise>");
    public static final String LANGUAGE_CLOSURE_WRAPPER_NAME = new String("<language>");

    private final RBaseNode expr;
    private final String symbol;
    private final String stringConstant;
    private final String closureName;

    private Closure(String closureName, RBaseNode expr) {
        this.closureName = closureName;
        this.expr = expr;
        if (expr.asRSyntaxNode() instanceof RSyntaxLookup) {
            this.symbol = Utils.intern(((RSyntaxLookup) expr.asRSyntaxNode()).getIdentifier());
        } else {
            this.symbol = null;
        }
        if (expr.asRSyntaxNode() instanceof RSyntaxConstant) {
            Object constant = ((RSyntaxConstant) expr.asRSyntaxNode()).getValue();
            if (constant instanceof String) {
                this.stringConstant = (String) constant;
            } else if (constant instanceof RAbstractStringVector && ((RAbstractStringVector) constant).getLength() == 1) {
                this.stringConstant = ((RAbstractStringVector) constant).getDataAt(0);
            } else {
                this.stringConstant = null;
            }
        } else {
            this.stringConstant = null;
        }
    }

    public static Closure createPromiseClosure(RBaseNode expr) {
        return new Closure(PROMISE_CLOSURE_WRAPPER_NAME, expr);
    }

    public static Closure createLanguageClosure(RBaseNode expr) {
        return new Closure(LANGUAGE_CLOSURE_WRAPPER_NAME, expr);
    }

    public static Closure create(String name, RBaseNode expr) {
        return new Closure(name, expr);
    }

    private RootCallTarget getCallTarget(FrameDescriptor desc) {

        // Create lazily, as it is not needed at all for INLINED promises!
        if (callTargets == null) {
            callTargets = new WeakHashMap<>();
        }
        return callTargets.get(desc);
    }

    /**
     * Evaluates a {@link com.oracle.truffle.r.runtime.data.Closure} in {@code frame}.
     */
    public Object eval(MaterializedFrame frame) {
        CompilerAsserts.neverPartOfCompilation();

        FrameDescriptor desc = frame.getFrameDescriptor();
        RootCallTarget callTarget = getCallTarget(desc);
        if (callTarget == null) {
            // clone for additional call targets
            callTarget = generateCallTarget((RNode) (callTargets.isEmpty() ? expr : RContext.getASTBuilder().process(expr.asRSyntaxNode())));
            callTargets.put(desc, callTarget);
        }
        return callTarget.call(frame);
    }

    /**
     * Evaluates this clousure in {@code envir} using caller {@code caller}.
     */
    public Object eval(REnvironment envir, RCaller caller) {
        CompilerAsserts.neverPartOfCompilation();

        FrameDescriptor desc = envir.getFrame().getFrameDescriptor();
        RootCallTarget callTarget = getCallTarget(desc);
        if (callTarget == null) {
            // clone for additional call targets
            callTarget = generateCallTarget((RNode) RContext.getASTBuilder().process(expr.asRSyntaxNode()));
            callTargets.put(desc, callTarget);
        }
        MaterializedFrame vFrame = VirtualEvalFrame.create(envir.getFrame(), (RFunction) null, caller);
        return callTarget.call(vFrame);
    }

    private RootCallTarget generateCallTarget(RNode n) {
        return RContext.getEngine().makePromiseCallTarget(n, closureName + System.identityHashCode(n));
    }

    public RBaseNode getExpr() {
        return expr;
    }

    public String asSymbol() {
        return symbol;
    }

    public String asStringConstant() {
        return stringConstant;
    }
}
