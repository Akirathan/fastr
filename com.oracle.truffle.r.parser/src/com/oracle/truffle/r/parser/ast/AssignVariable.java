/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * Copyright (c) 2012-2014, Purdue University
 * Copyright (c) 2013, 2016, Oracle and/or its affiliates
 *
 * All rights reserved.
 */
package com.oracle.truffle.r.parser.ast;

import java.util.*;

import com.oracle.truffle.api.source.*;
import com.oracle.truffle.r.runtime.*;

public final class AssignVariable extends ASTNode {

    private final boolean isSuper;
    private final String variable;
    private final ASTNode rhs;

    private AssignVariable(SourceSection source, boolean isSuper, String variable, ASTNode rhs) {
        super(source);
        this.isSuper = isSuper;
        this.variable = variable;
        this.rhs = rhs;
    }

    public boolean isSuper() {
        return isSuper;
    }

    public String getVariable() {
        return variable;
    }

    public ASTNode getExpr() {
        return rhs;
    }

    @Override
    public <R> R accept(Visitor<R> v) {
        return v.visit(this);
    }

    @Override
    public <R> List<R> visitAll(Visitor<R> v) {
        return Arrays.asList(getExpr().accept(v));
    }

    public static ASTNode create(boolean isSuper, SourceSection operatorSource, ASTNode lhs, ASTNode rhs) {
        if (lhs instanceof Call) {
            return Replacement.create(operatorSource, isSuper, lhs, rhs);
        } else {
            String name;
            if (lhs instanceof AccessVariable) {
                name = ((AccessVariable) lhs).getVariable();
            } else if (lhs instanceof AccessVariadicComponent) {
                // assigning to ..N indeed creates a local variable of that name
                name = ((AccessVariadicComponent) lhs).getName();
            } else if (lhs instanceof Constant) {
                Constant c = (Constant) lhs;
                assert c.getValue() instanceof String;
                name = (String) c.getValue();
            } else {
                throw RInternalError.unimplemented("unexpected lhs type: " + lhs.getClass());
            }
            return new AssignVariable(null, isSuper, name, rhs);
        }
    }
}
