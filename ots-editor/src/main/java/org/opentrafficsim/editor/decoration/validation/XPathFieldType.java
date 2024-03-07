package org.opentrafficsim.editor.decoration.validation;

/**
 * Defines the type of a field in xsd:key, xsd:unique or xsd:keyref. 
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public enum XPathFieldType
{
    
    /** Value of an attribute. */
    ATTRIBUTE,
    
    /** Value of a child. */
    CHILD,

    /** Value of the node itself. */
    VALUE;
    
}
