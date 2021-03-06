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
package com.espertech.esper.common.internal.epl.resultset.select.eval;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.util.CodegenRepetitiveLengthBuilder;
import com.espertech.esper.common.internal.epl.expression.codegen.CodegenLegoMayVoid;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.resultset.select.core.SelectExprForgeContext;
import com.espertech.esper.common.internal.epl.resultset.select.core.SelectExprProcessorCodegenSymbol;
import com.espertech.esper.common.internal.epl.resultset.select.core.SelectExprProcessorForge;
import com.espertech.esper.common.internal.util.CollectionUtil;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

public class SelectEvalNoWildcardMap implements SelectExprProcessorForge {

    private final SelectExprForgeContext selectContext;
    private final EventType resultEventType;

    public SelectEvalNoWildcardMap(SelectExprForgeContext selectContext, EventType resultEventType) {
        this.selectContext = selectContext;
        this.resultEventType = resultEventType;
    }

    public CodegenMethod processCodegen(CodegenExpression resultEventType, CodegenExpression eventBeanFactory, CodegenMethodScope codegenMethodScope, SelectExprProcessorCodegenSymbol selectSymbol, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        CodegenMethod methodNode = codegenMethodScope.makeChild(EventBean.EPTYPE, this.getClass(), codegenClassScope);
        methodNode.getBlock().declareVar(EPTypePremade.MAP.getEPType(), "props", newInstance(EPTypePremade.HASHMAP.getEPType(), constant(CollectionUtil.capacityHashMap(selectContext.getColumnNames().length))));

        new CodegenRepetitiveLengthBuilder(selectContext.getColumnNames().length, methodNode, codegenClassScope, this.getClass())
                .addParam(EPTypePremade.MAP.getEPType(), "props")
                .setConsumer((index, leafMethod) -> {
                    CodegenExpression expression = CodegenLegoMayVoid.expressionMayVoid(EPTypePremade.OBJECT.getEPType(), selectContext.getExprForges()[index], leafMethod, exprSymbol, codegenClassScope);
                    leafMethod.getBlock().expression(exprDotMethod(ref("props"), "put", constant(selectContext.getColumnNames()[index]), expression));
                }).build();

        methodNode.getBlock().methodReturn(exprDotMethod(eventBeanFactory, "adapterForTypedMap", ref("props"), resultEventType));
        return methodNode;
    }

    public EventType getResultEventType() {
        return resultEventType;
    }
}