package org.opentrafficsim.core.parameters;

import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * Only use given parameters, do not set any others.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ParameterFactoryDefault implements ParameterFactory
{

    /** {@inheritDoc} */
    @Override
    public void setValues(final Parameters parameters, final GTUType gtuType)
    {
        //
    }

}
