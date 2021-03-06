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
package com.espertech.esper.common.internal.epl.expression.dot.propertydot;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.*;
import com.espertech.esper.common.internal.event.core.EventPropertyGetterIndexedSPI;

import java.io.StringWriter;

public class PropertyDotNonLambdaIndexedForge implements ExprForge, ExprNodeRenderable {

    private final int streamId;
    private final EventPropertyGetterIndexedSPI indexedGetter;
    private final ExprForge paramForge;
    private final EPTypeClass returnType;

    public PropertyDotNonLambdaIndexedForge(int streamId, EventPropertyGetterIndexedSPI indexedGetter, ExprForge paramForge, EPTypeClass returnType) {
        this.streamId = streamId;
        this.indexedGetter = indexedGetter;
        this.paramForge = paramForge;
        this.returnType = returnType;
    }

    public ExprEvaluator getExprEvaluator() {
        return new PropertyDotNonLambdaIndexedForgeEval(this, paramForge.getExprEvaluator());
    }

    public CodegenExpression evaluateCodegen(EPTypeClass requiredType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        return PropertyDotNonLambdaIndexedForgeEval.codegen(this, codegenMethodScope, exprSymbol, codegenClassScope);
    }

    public ExprForgeConstantType getForgeConstantType() {
        return ExprForgeConstantType.NONCONST;
    }

    public EPTypeClass getEvaluationType() {
        return returnType;
    }

    public int getStreamId() {
        return streamId;
    }

    public EventPropertyGetterIndexedSPI getIndexedGetter() {
        return indexedGetter;
    }

    public ExprForge getParamForge() {
        return paramForge;
    }

    public ExprNodeRenderable getForgeRenderable() {
        return this;
    }

    public void toEPL(StringWriter writer, ExprPrecedenceEnum parentPrecedence, ExprNodeRenderableFlags flags) {
        writer.append(this.getClass().getSimpleName());
    }
}
