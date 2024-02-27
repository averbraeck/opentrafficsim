package org.opentrafficsim.kpi.interfaces;

import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.base.Identifiable;

/**
 * Represents a GTU for sampling.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface GtuData extends Identifiable
{

    /**
     * Returns the origin id.
     * @return origin id of the gtu
     */
    String getOriginId();

    /**
     * Returns the destination id.
     * @return destination id of the gtu
     */
    String getDestinationId();

    /**
     * Returns the GTU type id.
     * @return type id of the gtu
     */
    String getGtuId();

    /**
     * Returns the route id.
     * @return route id of the gtu
     */
    String getRouteId();

    /**
     * Returns the reference speed.
     * @return reference speed
     */
    Speed getReferenceSpeed();

}
