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
package com.espertech.esper.common.internal.bytecodemodel.name;

import com.espertech.esper.common.internal.bytecodemodel.base.CodegenPackageScopeNames;

public class CodegenFieldNameSubqueryAgg implements CodegenFieldName {
    private final int subqueryNumber;

    public CodegenFieldNameSubqueryAgg(int subqueryNumber) {
        this.subqueryNumber = subqueryNumber;
    }

    public String getName() {
        return CodegenPackageScopeNames.aggregationSubquery(subqueryNumber);
    }

    public int getSubqueryNumber() {
        return subqueryNumber;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CodegenFieldNameSubqueryAgg that = (CodegenFieldNameSubqueryAgg) o;

        return subqueryNumber == that.subqueryNumber;
    }

    public int hashCode() {
        return subqueryNumber;
    }
}
