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
package com.espertech.esper.runtime.internal.filtersvcimpl;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.util.HashableMultiKey;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.epl.expression.core.ExprFilterSpecLookupable;
import com.espertech.esper.common.internal.filterspec.FilterOperator;
import com.espertech.esper.common.internal.filtersvc.FilterHandle;
import com.espertech.esper.runtime.internal.metrics.instrumentation.InstrumentationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Index for filter parameter constants to match using the 'in' operator to match against a supplied set of values
 * (i.e. multiple possible exact matches).
 * The implementation is based on a regular HashMap.
 */
public final class FilterParamIndexIn extends FilterParamIndexLookupableBase {
    private final Map<Object, List<EventEvaluator>> constantsMap;
    private final Map<HashableMultiKey, EventEvaluator> evaluatorsMap;
    private final ReadWriteLock constantsMapRWLock;

    public FilterParamIndexIn(ExprFilterSpecLookupable lookupable, ReadWriteLock readWriteLock) {
        super(FilterOperator.IN_LIST_OF_VALUES, lookupable);

        constantsMap = new HashMap<Object, List<EventEvaluator>>();
        evaluatorsMap = new HashMap<HashableMultiKey, EventEvaluator>();
        constantsMapRWLock = readWriteLock;
    }

    public final EventEvaluator get(Object filterConstant) {
        HashableMultiKey keyValues = (HashableMultiKey) filterConstant;
        return evaluatorsMap.get(keyValues);
    }

    public final void put(Object filterConstant, EventEvaluator evaluator) {
        // Store evaluator keyed to set of values
        HashableMultiKey keys = (HashableMultiKey) filterConstant;

        // make sure to remove the old evaluator for this constant
        EventEvaluator oldEvaluator = evaluatorsMap.put(keys, evaluator);

        // Store each value to match against in Map with it's evaluator as a list
        Object[] keyValues = keys.getKeys();
        for (int i = 0; i < keyValues.length; i++) {
            List<EventEvaluator> evaluators = constantsMap.get(keyValues[i]);
            if (evaluators == null) {
                evaluators = new LinkedList<EventEvaluator>();
                constantsMap.put(keyValues[i], evaluators);
            } else {
                if (oldEvaluator != null) {
                    evaluators.remove(oldEvaluator);
                }
            }
            evaluators.add(evaluator);
        }
    }

    public final void remove(Object filterConstant) {
        HashableMultiKey keys = (HashableMultiKey) filterConstant;

        // remove the mapping of value set to evaluator
        EventEvaluator eval = evaluatorsMap.remove(keys);

        Object[] keyValues = keys.getKeys();
        for (int i = 0; i < keyValues.length; i++) {
            List<EventEvaluator> evaluators = constantsMap.get(keyValues[i]);
            if (evaluators != null) {
                // could be removed already as same-value constants existed
                evaluators.remove(eval);
                if (evaluators.isEmpty()) {
                    constantsMap.remove(keyValues[i]);
                }
            }
        }
    }

    public final int sizeExpensive() {
        return constantsMap.size();
    }

    public boolean isEmpty() {
        return constantsMap.isEmpty();
    }

    public final ReadWriteLock getReadWriteLock() {
        return constantsMapRWLock;
    }

    public final void matchEvent(EventBean theEvent, Collection<FilterHandle> matches, ExprEvaluatorContext ctx) {
        Object attributeValue = lookupable.getEval().eval(theEvent, ctx);
        if (InstrumentationHelper.ENABLED) {
            InstrumentationHelper.get().qFilterReverseIndex(this, attributeValue);
        }

        if (attributeValue == null) {
            if (InstrumentationHelper.ENABLED) {
                InstrumentationHelper.get().aFilterReverseIndex(false);
            }
            return;
        }

        // Look up in hashtable
        constantsMapRWLock.readLock().lock();
        List<EventEvaluator> evaluators = constantsMap.get(attributeValue);

        // No listener found for the value, return
        if (evaluators == null) {
            constantsMapRWLock.readLock().unlock();
            if (InstrumentationHelper.ENABLED) {
                InstrumentationHelper.get().aFilterReverseIndex(false);
            }
            return;
        }

        try {
            for (EventEvaluator evaluator : evaluators) {
                evaluator.matchEvent(theEvent, matches, ctx);
            }
        } finally {
            constantsMapRWLock.readLock().unlock();
        }

        if (InstrumentationHelper.ENABLED) {
            InstrumentationHelper.get().aFilterReverseIndex(null);
        }
    }

    public void getTraverseStatement(EventTypeIndexTraverse traverse, Set<Integer> statementIds, ArrayDeque<FilterItem> evaluatorStack) {
        for (Map.Entry<HashableMultiKey, EventEvaluator> entry : evaluatorsMap.entrySet()) {
            evaluatorStack.add(new FilterItem(lookupable.getExpression(), getFilterOperator(), entry.getValue(), this));
            entry.getValue().getTraverseStatement(traverse, statementIds, evaluatorStack);
            evaluatorStack.removeLast();
        }
    }

    private static final Logger log = LoggerFactory.getLogger(FilterParamIndexIn.class);
}
