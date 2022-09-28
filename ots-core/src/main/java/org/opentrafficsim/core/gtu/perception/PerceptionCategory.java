package org.opentrafficsim.core.gtu.perception;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Super interface for all perception categories.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <G> GTU type
 * @param <P> perception type
 */
public interface PerceptionCategory<G extends Gtu, P extends Perception<G>>
{

    /**
     * Update all information in the perception category.
     * @throws GtuException if the GTU was not initialized
     * @throws NetworkException when lanes are not properly linked
     * @throws ParameterException when a necessary parameter to carry our perception is not defined (e.g., LOOKAHEAD)
     */
    void updateAll() throws GtuException, NetworkException, ParameterException;

}
