package org.opentrafficsim.core.parameters;

/**
 * Interface for input parameters.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface InputParameters
{

    /**
     * Returns the value for a parameter of given name.
     * @param parameter String; parameter name.
     * @return value for a parameter of given name.
     */
    Object getValue(String parameter);
    
}
