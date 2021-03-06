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
package com.espertech.esper.common.internal.epl.join.querygraph;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluator;

public class QueryGraphValueEntryRangeIn extends QueryGraphValueEntryRange {
    public final static EPTypeClass EPTYPE = new EPTypeClass(QueryGraphValueEntryRangeIn.class);

    private ExprEvaluator exprStart;
    private ExprEvaluator exprEnd;
    private boolean allowRangeReversal; // indicate whether "a between 60 and 50" should return no results (false, equivalent to a>= X and a <=Y) or should return results (true, equivalent to 'between' and 'in')

    public QueryGraphValueEntryRangeIn(QueryGraphRangeEnum rangeType, ExprEvaluator exprStart, ExprEvaluator exprEnd, boolean allowRangeReversal) {
        super(rangeType);
        if (!rangeType.isRange()) {
            throw new IllegalArgumentException("Range type expected but received " + rangeType.name());
        }
        this.exprStart = exprStart;
        this.exprEnd = exprEnd;
        this.allowRangeReversal = allowRangeReversal;
    }

    public boolean isAllowRangeReversal() {
        return allowRangeReversal;
    }

    public ExprEvaluator getExprStart() {
        return exprStart;
    }

    public ExprEvaluator getExprEnd() {
        return exprEnd;
    }

    public ExprEvaluator[] getExpressions() {
        return new ExprEvaluator[]{exprStart, exprEnd};
    }
}
