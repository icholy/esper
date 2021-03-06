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
package com.espertech.esper.common.internal.epl.agg.method.minmax;

import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenBlock;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMemberCol;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.core.CodegenCtor;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionField;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionMember;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRef;
import com.espertech.esper.common.internal.epl.agg.method.core.AggregatorMethodWDistinctWFilterWValueBase;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprForge;
import com.espertech.esper.common.internal.epl.expression.core.ExprNode;
import com.espertech.esper.common.internal.serde.compiletime.resolve.DataInputOutputSerdeForge;
import com.espertech.esper.common.internal.serde.compiletime.sharable.CodegenSharableSerdeClassTyped;

import java.util.function.Consumer;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;
import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRelational.CodegenRelational.GT;
import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRelational.CodegenRelational.LT;
import static com.espertech.esper.common.internal.epl.agg.method.core.AggregatorCodegenUtil.*;
import static com.espertech.esper.common.internal.epl.expression.core.MinMaxTypeEnum.MAX;

/**
 * Min/max aggregator for all values, not considering events leaving the aggregation (i.e. ever).
 */
public class AggregatorMinMaxEver extends AggregatorMethodWDistinctWFilterWValueBase {
    private final AggregationForgeFactoryMinMax factory;
    private CodegenExpressionMember currentMinMax;
    private CodegenExpressionField serdeField;

    public AggregatorMinMaxEver(AggregationForgeFactoryMinMax factory, EPTypeClass optionalDistinctValueType, DataInputOutputSerdeForge optionalDistinctSerde, boolean hasFilter, ExprNode optionalFilter, DataInputOutputSerdeForge serde) {
        super(optionalDistinctValueType, optionalDistinctSerde, hasFilter, optionalFilter);
        this.factory = factory;
    }

    public void initForgeFiltered(int col, CodegenCtor rowCtor, CodegenMemberCol membersColumnized, CodegenClassScope classScope) {
        currentMinMax = membersColumnized.addMember(col, EPTypePremade.COMPARABLE.getEPType(), "currentMinMax");
        this.serdeField = classScope.addOrGetFieldSharable(new CodegenSharableSerdeClassTyped(CodegenSharableSerdeClassTyped.CodegenSharableSerdeName.VALUE_NULLABLE, factory.type, factory.serde, classScope));
    }

    protected void applyEvalEnterNonNull(CodegenExpressionRef value, EPType valueType, CodegenMethod method, ExprForgeCodegenSymbol symbols, ExprForge[] forges, CodegenClassScope classScope) {
        method.getBlock().apply(enterConsumer(value));
    }

    protected void applyTableEnterNonNull(CodegenExpressionRef value, EPType[] evaluationTypes, CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().apply(enterConsumer(cast(EPTypePremade.COMPARABLE.getEPType(), value)));
    }

    @Override
    public void applyEvalLeaveCodegen(CodegenMethod method, ExprForgeCodegenSymbol symbols, ExprForge[] forges, CodegenClassScope classScope) {
        // no-op, this is designed to handle min-max ever
    }

    @Override
    public void applyTableLeaveCodegen(CodegenExpressionRef value, EPType[] evaluationTypes, CodegenMethod method, CodegenClassScope classScope) {
        // no-op
    }

    protected void applyEvalLeaveNonNull(CodegenExpressionRef value, EPType valueType, CodegenMethod method, ExprForgeCodegenSymbol symbols, ExprForge[] forges, CodegenClassScope classScope) {
        // no-op, this is designed to handle min-max ever
    }

    protected void applyTableLeaveNonNull(CodegenExpressionRef value, EPType[] evaluationTypes, CodegenMethod method, CodegenClassScope classScope) {
        // no-op
    }

    protected void clearWODistinct(CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().assignRef(currentMinMax, constantNull());
    }

    public void getValueCodegen(CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().methodReturn(currentMinMax);
    }

    protected void writeWODistinct(CodegenExpressionRef row, int col, CodegenExpressionRef output, CodegenExpressionRef unitKey, CodegenExpressionRef writer, CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().expression(writeNullable(rowDotMember(row, currentMinMax), serdeField, output, unitKey, writer, classScope));
    }

    protected void readWODistinct(CodegenExpressionRef row, int col, CodegenExpressionRef input, CodegenExpressionRef unitKey, CodegenMethod method, CodegenClassScope classScope) {
        method.getBlock().assignRef(rowDotMember(row, currentMinMax), cast(EPTypePremade.COMPARABLE.getEPType(), readNullable(serdeField, input, unitKey, classScope)));
    }

    private Consumer<CodegenBlock> enterConsumer(CodegenExpression valueComparableTyped) {
        return block -> block.ifCondition(equalsNull(currentMinMax))
                .assignRef(currentMinMax, valueComparableTyped)
                .blockReturnNoValue()
                .ifCondition(relational(exprDotMethod(currentMinMax, "compareTo", valueComparableTyped), factory.getParent().getMinMaxTypeEnum() == MAX ? LT : GT, constant(0)))
                .assignRef(currentMinMax, valueComparableTyped);
    }
}
