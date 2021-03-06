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
package com.espertech.esper.common.internal.type;

import com.espertech.esper.common.client.annotation.JsonSchemaField;
import com.espertech.esper.common.client.type.EPTypeClass;

import java.lang.annotation.Annotation;

public class AnnotationJsonSchemaField implements JsonSchemaField {
    public final static EPTypeClass EPTYPE = new EPTypeClass(AnnotationJsonSchemaField.class);
    private final String name;
    private final String adapter;

    public AnnotationJsonSchemaField(String name, String adapter) {
        this.name = name;
        this.adapter = adapter;
    }

    public String name() {
        return name;
    }

    public String adapter() {
        return adapter;
    }

    public Class<? extends Annotation> annotationType() {
        return JsonSchemaField.class;
    }
}
