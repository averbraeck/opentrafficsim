/**
 * Special types to store the information from the XML bindings. Note that many types do little but define the generics argument
 * of their parent class {@code ExpressionType}. This is required as JAXB bindings do not allow generics to be used. That is,
 * the following binding does <b>not</b> work.<br>
 * <br>
 * {@code <xjc:javaType name="org.opentrafficsim.xml.bindings.types.ExpressionType<Boolean>" xmlType="ots:boolean" ... />}
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
package org.opentrafficsim.xml.bindings.types;
