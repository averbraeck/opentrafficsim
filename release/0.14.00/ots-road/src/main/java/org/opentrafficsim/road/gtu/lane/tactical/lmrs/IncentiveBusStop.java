package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.BusStopPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayBusStop;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.pt.BusSchedule;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;

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
public class IncentiveBusStop implements MandatoryIncentive
{

    /** {@inheritDoc} */
    @Override
    public Desire determineDesire(final BehavioralCharacteristics behavioralCharacteristics, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire)
            throws ParameterException, OperationalPlanException
    {

        HeadwayBusStop firstStop = null;
        SortedSet<HeadwayBusStop> stops = perception.getPerceptionCategory(BusStopPerception.class).getBusStops();
        Time now;
        try
        {
            now = perception.getGtu().getSimulator().getSimulatorTime().getTime();
        }
        catch (GTUException exception)
        {
            throw new RuntimeException("GTU not initialized.", exception);
        }
        for (HeadwayBusStop stop : stops)
        {
            try
            {
                if (((BusSchedule) perception.getGtu().getStrategicalPlanner().getRoute()).isLineStop(stop.getId(), now))
                {
                    firstStop = stop;
                    break;
                }
            }
            catch (GTUException exception)
            {
                throw new OperationalPlanException("Could not obtain bus schedule.", exception);
            }
        }

        if (firstStop == null)
        {
            return new Desire(0, 0);
        }
        Speed speed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
        if (firstStop.getRelativeLane().isCurrent())
        {
            double d = -IncentiveRoute.getDesireToLeave(behavioralCharacteristics, firstStop.getDistance(), 1, speed);
            return new Desire(d, d);
        }
        
        int n = firstStop.getRelativeLane().getNumLanes();
        
        double dNotGood = -IncentiveRoute.getDesireToLeave(behavioralCharacteristics, firstStop.getDistance(), n + 1, speed);
        double dGood = IncentiveRoute.getDesireToLeave(behavioralCharacteristics, firstStop.getDistance(), n, speed);
        return firstStop.getRelativeLane().getLateralDirectionality().isRight() ? new Desire(dNotGood, dGood)
                : new Desire(dGood, dNotGood);

    }
    
    @Override
    public final String toString()
    {
        return "IncentiveBusStop";
    }

}
