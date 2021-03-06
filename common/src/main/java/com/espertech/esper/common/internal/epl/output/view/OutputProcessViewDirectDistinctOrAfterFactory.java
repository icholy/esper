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
package com.espertech.esper.common.internal.epl.output.view;

import com.espertech.esper.common.client.EventPropertyValueGetter;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.context.util.AgentInstanceContext;
import com.espertech.esper.common.internal.epl.expression.time.eval.TimePeriodCompute;
import com.espertech.esper.common.internal.epl.output.core.OutputProcessView;
import com.espertech.esper.common.internal.epl.resultset.core.ResultSetProcessor;

/**
 * Output process view that does not enforce any output policies and may simply
 * hand over events to child views, but works with distinct and after-output policies
 */
public class OutputProcessViewDirectDistinctOrAfterFactory extends OutputProcessViewDirectFactory {
    public final static EPTypeClass EPTYPE = new EPTypeClass(OutputProcessViewDirectDistinctOrAfterFactory.class);

    private final boolean isDistinct;
    private final EventPropertyValueGetter distinctKeyGetter;

    protected final TimePeriodCompute afterTimePeriod;
    protected final Integer afterConditionNumberOfEvents;

    public OutputProcessViewDirectDistinctOrAfterFactory(OutputStrategyPostProcessFactory postProcessFactory,
                                                         boolean distinct,
                                                         EventPropertyValueGetter distinctKeyGetter,
                                                         TimePeriodCompute afterTimePeriod,
                                                         Integer afterConditionNumberOfEvents) {
        super(postProcessFactory);
        isDistinct = distinct;
        this.distinctKeyGetter = distinctKeyGetter;
        this.afterTimePeriod = afterTimePeriod;
        this.afterConditionNumberOfEvents = afterConditionNumberOfEvents;
    }

    @Override
    public OutputProcessView makeView(ResultSetProcessor resultSetProcessor, AgentInstanceContext agentInstanceContext) {

        boolean isAfterConditionSatisfied = true;
        Long afterConditionTime = null;
        if (afterConditionNumberOfEvents != null) {
            isAfterConditionSatisfied = false;
        } else if (afterTimePeriod != null) {
            isAfterConditionSatisfied = false;
            long time = agentInstanceContext.getTimeProvider().getTime();
            long delta = afterTimePeriod.deltaAdd(time, null, true, agentInstanceContext);
            afterConditionTime = time + delta;
        }

        if (super.postProcessFactory == null) {
            return new OutputProcessViewDirectDistinctOrAfter(agentInstanceContext, resultSetProcessor, afterConditionTime, afterConditionNumberOfEvents, isAfterConditionSatisfied, this);
        }
        OutputStrategyPostProcess postProcess = postProcessFactory.make(agentInstanceContext);
        return new OutputProcessViewDirectDistinctOrAfterPostProcess(agentInstanceContext, resultSetProcessor, afterConditionTime, afterConditionNumberOfEvents, isAfterConditionSatisfied, this, postProcess);
    }

    public boolean isDistinct() {
        return isDistinct;
    }

    public EventPropertyValueGetter getDistinctKeyGetter() {
        return distinctKeyGetter;
    }
}