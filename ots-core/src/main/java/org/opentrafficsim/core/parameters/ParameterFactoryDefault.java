package org.opentrafficsim.core.parameters;

import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * Only use given parameters, do not set any others.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
public class ParameterFactoryDefault implements ParameterFactory
{

    /**
     * Constructor.
     */
    public ParameterFactoryDefault()
    {
        //
    }

    @Override
    public void setValues(final Parameters parameters, final GtuType gtuType)
    {
        //
    }

}
