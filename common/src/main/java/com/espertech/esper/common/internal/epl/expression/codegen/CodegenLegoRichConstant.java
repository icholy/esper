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
package com.espertech.esper.common.internal.epl.expression.codegen;

import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;

import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

public class CodegenLegoRichConstant {
    public static CodegenExpression toExpression(SortedSet<Integer> values) {
        if (values.isEmpty()) {
            return staticMethod(Collections.class, "emptySortedSet");
        }
        Integer[] arr = values.toArray(new Integer[values.size()]);
        return newInstance(EPTypePremade.TREESET.getEPType(), staticMethod(Arrays.class, "asList", constant(arr)));
    }
}
