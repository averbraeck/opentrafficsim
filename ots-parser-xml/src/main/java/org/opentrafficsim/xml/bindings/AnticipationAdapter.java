package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Anticipation;
import org.opentrafficsim.xml.bindings.types.AnticipationType;

/**
 * Adapter for Anticipation expression type.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AnticipationAdapter extends StaticFieldAdapter<Anticipation, AnticipationType>
{

    /**
     * Constructor.
     */
    public AnticipationAdapter()
    {
        super(Anticipation.class, AnticipationType.class);
    }

}
