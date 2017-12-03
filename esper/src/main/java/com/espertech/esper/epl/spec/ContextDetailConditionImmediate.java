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
package com.espertech.esper.epl.spec;

import com.espertech.esper.filterspec.FilterSpecCompiled;

import java.util.Collections;
import java.util.List;

public class ContextDetailConditionImmediate implements ContextDetailCondition {
    private static final long serialVersionUID = -2941853977692802522L;

    public final static ContextDetailConditionImmediate INSTANCE = new ContextDetailConditionImmediate();

    private ContextDetailConditionImmediate() {
    }

    public List<FilterSpecCompiled> getFilterSpecIfAny() {
        return Collections.emptyList();
    }
}
