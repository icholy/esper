/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.common.internal.epl.expression.ops;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.expression.core.*;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import java.io.StringWriter;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.constant;
import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.staticMethod;

/**
 * Represents the regexp-clause in an expression tree.
 */
public class ExprRegexpNode extends ExprNodeBase {
    private final boolean isNot;

    private transient ExprRegexpNodeForge forge;

    /**
     * Ctor.
     *
     * @param not is true if the it's a "not regexp" expression, of false for regular regexp
     */
    public ExprRegexpNode(boolean not) {
        this.isNot = not;
    }

    public ExprEvaluator getExprEvaluator() {
        checkValidated(forge);
        return forge.getExprEvaluator();
    }

    public ExprForge getForge() {
        checkValidated(forge);
        return forge;
    }

    public ExprNode validate(ExprValidationContext validationContext) throws ExprValidationException {
        if (this.getChildNodes().length != 2) {
            throw new ExprValidationException("The regexp operator requires 2 child expressions");
        }

        // check pattern child node
        EPTypeClass patternType = (EPTypeClass) getChildNodes()[1].getForge().getEvaluationType();
        if (patternType.getType() != String.class) {
            throw new ExprValidationException("The regexp operator requires a String-type pattern expression");
        }
        boolean constantPattern = this.getChildNodes()[1].getForge().getForgeConstantType().isCompileTimeConstant();

        // check eval child node - can be String or numeric
        EPTypeClass evalType = (EPTypeClass) getChildNodes()[0].getForge().getEvaluationType();
        boolean isNumericValue = JavaClassHelper.isNumeric(evalType);
        if ((evalType.getType() != String.class) && (!isNumericValue)) {
            throw new ExprValidationException("The regexp operator requires a String or numeric type left-hand expression");
        }

        if (constantPattern) {
            String patternText = (String) getChildNodes()[1].getForge().getExprEvaluator().evaluate(null, true, null);
            Pattern pattern;
            try {
                pattern = Pattern.compile(patternText);
            } catch (PatternSyntaxException ex) {
                throw new ExprValidationException("Failed to compile regex pattern '" + patternText + "': " + ex.getMessage(), ex);
            }
            CodegenExpression patternInit = staticMethod(Pattern.class, "compile", constant(patternText));
            forge = new ExprRegexpNodeForgeConst(this, isNumericValue, pattern, patternInit);
        } else {
            forge = new ExprRegexpNodeForgeNonconst(this, isNumericValue);
        }
        return null;
    }

    public Class getType() {
        return Boolean.class;
    }

    public boolean isConstantResult() {
        return false;
    }

    public boolean equalsNode(ExprNode node, boolean ignoreStreamPrefix) {
        if (!(node instanceof ExprRegexpNode)) {
            return false;
        }

        ExprRegexpNode other = (ExprRegexpNode) node;
        if (this.isNot != other.isNot) {
            return false;
        }
        return true;
    }

    public void toPrecedenceFreeEPL(StringWriter writer, ExprNodeRenderableFlags flags) {
        this.getChildNodes()[0].toEPL(writer, getPrecedence(), flags);
        if (isNot) {
            writer.append(" not");
        }
        writer.append(" regexp ");
        this.getChildNodes()[1].toEPL(writer, getPrecedence(), flags);
    }

    public ExprPrecedenceEnum getPrecedence() {
        return ExprPrecedenceEnum.RELATIONAL_BETWEEN_IN;
    }

    /**
     * Returns true if this is a "not regexp", or false if just a regexp
     *
     * @return indicator whether negated or not
     */
    public boolean isNot() {
        return isNot;
    }
}
