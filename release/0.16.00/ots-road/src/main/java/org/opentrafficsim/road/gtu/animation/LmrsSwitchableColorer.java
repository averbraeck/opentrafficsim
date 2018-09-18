package org.opentrafficsim.road.gtu.animation;

import java.awt.Color;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.animation.AccelerationGTUColorer;
import org.opentrafficsim.core.gtu.animation.IDGTUColorer;
import org.opentrafficsim.core.gtu.animation.SpeedGTUColorer;
import org.opentrafficsim.core.gtu.animation.SwitchableGTUColorer;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveBusStop;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveGetInLane;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveKeep;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveRoute;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSocioSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeedWithCourtesy;

/**
 * Colorer for LMRS.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 apr. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LmrsSwitchableColorer extends SwitchableGTUColorer
{

    /** */
    private static final long serialVersionUID = 20170414L;

    /**
     * Constructor.
     * @throws IndexOutOfBoundsException initial index out of bounds
     */
    public LmrsSwitchableColorer() throws IndexOutOfBoundsException
    {
        super(0, new FixedColor(Color.BLUE, "Blue"), GTUTypeColorer.DEFAULT, new IDGTUColorer(),
                new SpeedGTUColorer(new Speed(150, SpeedUnit.KM_PER_HOUR)),
                new DesiredSpeedColorer(new Speed(50, SpeedUnit.KM_PER_HOUR), new Speed(150, SpeedUnit.KM_PER_HOUR)),
                new AccelerationGTUColorer(Acceleration.createSI(-6.0), Acceleration.createSI(2)), new SplitColorer(),
                new SynchronizationColorer(), new DesiredHeadwayColorer(), new TotalDesireColorer(),
                new IncentiveColorer(IncentiveRoute.class), new IncentiveColorer(IncentiveSpeedWithCourtesy.class),
                new IncentiveColorer(IncentiveSpeed.class), new IncentiveColorer(IncentiveKeep.class),
                new IncentiveColorer(IncentiveGetInLane.class), new IncentiveColorer(IncentiveCourtesy.class),
                new IncentiveColorer(IncentiveSocioSpeed.class), new IncentiveColorer(IncentiveBusStop.class));
    }

}
