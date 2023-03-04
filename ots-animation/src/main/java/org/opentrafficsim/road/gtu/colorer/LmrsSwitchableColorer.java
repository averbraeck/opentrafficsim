package org.opentrafficsim.road.gtu.colorer;

import java.awt.Color;
import java.util.Map;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.animation.gtu.colorer.AccelerationGtuColorer;
import org.opentrafficsim.core.animation.gtu.colorer.IdGtuColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SpeedGtuColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SwitchableGtuColorer;
import org.opentrafficsim.core.gtu.GtuType;
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
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LmrsSwitchableColorer extends SwitchableGtuColorer
{

    /** */
    private static final long serialVersionUID = 20170414L;

    /**
     * Constructor.
     * @param gtuTypeColors Map&lt;GtuType, Color&gt;; colors per GTU type in the GTU type colorer.
     * @throws IndexOutOfBoundsException initial index out of bounds
     */
    public LmrsSwitchableColorer(final Map<GtuType, Color> gtuTypeColors) throws IndexOutOfBoundsException
    {
        super(0, new FixedColor(Color.BLUE, "Blue"), GtuTypeColorer.fromMap(gtuTypeColors), new IdGtuColorer(),
                new SpeedGtuColorer(new Speed(150, SpeedUnit.KM_PER_HOUR)),
                new DesiredSpeedColorer(new Speed(50, SpeedUnit.KM_PER_HOUR), new Speed(150, SpeedUnit.KM_PER_HOUR)),
                new AccelerationGtuColorer(Acceleration.instantiateSI(-6.0), Acceleration.instantiateSI(2)), new SplitColorer(),
                new SynchronizationColorer(), new DesiredHeadwayColorer(), new TotalDesireColorer(),
                new IncentiveColorer(IncentiveRoute.class), new IncentiveColorer(IncentiveSpeedWithCourtesy.class),
                new IncentiveColorer(IncentiveSpeed.class), new IncentiveColorer(IncentiveKeep.class),
                new IncentiveColorer(IncentiveGetInLane.class), new IncentiveColorer(IncentiveCourtesy.class),
                new IncentiveColorer(IncentiveSocioSpeed.class), new IncentiveColorer(IncentiveBusStop.class));
    }

}
