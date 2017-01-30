package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.network.lane.object.BusStop;

import nl.tudelft.simulation.immutablecollections.ImmutableSet;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 jan. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class HeadwayBusStop extends AbstractHeadway
{

    /** */
    private static final long serialVersionUID = 20170127L;

    /** Relative lane. */
    private final RelativeLane relativeLane;

    /** Lines. */
    private final ImmutableSet<String> lines;

    /**
     * @param busStop bus stop
     * @param distance distance
     * @param relativeLane relative lane
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public HeadwayBusStop(final BusStop busStop, final Length distance, final RelativeLane relativeLane) throws GTUException
    {
        super(ObjectType.BUSSTOP, busStop.getId(), distance);
        this.relativeLane = relativeLane;
        this.lines = busStop.getLines();
    }

    /**
     * @return relativeLane.
     */
    public RelativeLane getRelativeLane()
    {
        return this.relativeLane;
    }

    /**
     * @return lines.
     */
    public ImmutableSet<String> getLines()
    {
        return this.lines;
    }

}
