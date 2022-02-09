package org.opentrafficsim.road.gtu.lane;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * Checks acceleration bounds.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 6, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class AccelerationChecker extends AbstractLaneBasedMoveChecker
{

    /** Minimum allowable acceleration. */
    private final Acceleration min;

    /** Maximum allowable acceleration. */
    private final Acceleration max;

    /** Speed above which acceleration should be checked. */
    private final Speed minSpeed;

    /**
     * Constructor.
     * @param network OTSNetwork; network
     */
    public AccelerationChecker(final OTSNetwork network)
    {
        this(network, Acceleration.instantiateSI(-10.0), Acceleration.instantiateSI(5), Speed.instantiateSI(2.5));
    }

    /**
     * Constructor.
     * @param network OTSNetwork; network
     * @param min Acceleration; minimum allowable acceleration
     * @param max Acceleration; maximum allowable acceleration
     * @param minSpeed Speed; speed above which acceleration should be checked
     */
    public AccelerationChecker(final OTSNetwork network, final Acceleration min, final Acceleration max, final Speed minSpeed)
    {
        super(network);
        this.min = min;
        this.max = max;
        this.minSpeed = minSpeed;
    }

    /** {@inheritDoc} */
    @Override
    public void checkMove(final LaneBasedGTU gtu) throws Exception
    {
        Acceleration a = gtu.getOperationalPlan().getAcceleration(Duration.ZERO);
        if (gtu.getOperationalPlan().getSpeed(Duration.ZERO).si > this.minSpeed.si
                && (a.si < this.min.si || a.si > this.max.si))
        {
            gtu.getSimulator().getLogger().always().error("GTU: {} acceleration out of bounds ({}, {})", this.min, this.max);
        }
    }

}
