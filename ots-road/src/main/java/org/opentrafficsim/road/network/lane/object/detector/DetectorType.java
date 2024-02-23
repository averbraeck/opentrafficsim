package org.opentrafficsim.road.network.lane.object.detector;

import org.opentrafficsim.core.compatibility.GtuCompatibleInfraType;

/**
 * Defines the type of a Detector, and with it the GTU compatibility.
 * <p>
 * Copyright (c) 2022-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class DetectorType extends GtuCompatibleInfraType<DetectorType, Detector>
{

    /** */
    private static final long serialVersionUID = 20230201L;

    /**
     * Constructor for root-level types.
     * @param id String; id.
     */
    public DetectorType(final String id)
    {
        super(id);
    }

    /**
     * Constructor for types with parent.
     * @param id String; id.
     * @param parent DetectorType; parent type,
     */
    public DetectorType(final String id, final DetectorType parent)
    {
        super(id, parent);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "DetectorType [id=" + getId() + "]";
    }

}
