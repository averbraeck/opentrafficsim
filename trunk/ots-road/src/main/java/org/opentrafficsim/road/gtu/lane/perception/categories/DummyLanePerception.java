package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.perception.TimeStampedObject;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.PerceivedObject;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.AbstractHeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.Headway;
import org.opentrafficsim.road.gtu.lane.perception.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.HeadwayTrafficLight;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * Dummy for initial development. TODO Properties and methods should move to AbstractLanePerception.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DummyLanePerception implements LanePerception
{

    /** Set of available perception categories. */
    private final Map<Class<?>, AbstractPerceptionCategory> perceptionCategories = new HashMap<>();

    /**
     * Adds given perception category to the perception.
     * @param perceptionCategory perception category
     */
    public final void addPerceptionCategory(final AbstractPerceptionCategory perceptionCategory)
    {
        // guarantees correct combination of class and perception category
        this.perceptionCategories.put(perceptionCategory.getClass(), perceptionCategory);
    }

    /**
     * Returns whether the given perception category is present.
     * @param clazz perception category class
     * @param <T> perception category
     * @return whether the given perception category is present
     */
    public final <T extends AbstractPerceptionCategory> boolean contains(final Class<T> clazz)
    {
        return this.perceptionCategories.containsKey(clazz);
    }

    /**
     * Returns the given perception category.
     * @param clazz perception category class
     * @param <T> perception category
     * @return given perception category
     * @throws OperationalPlanException if the perception category is not present
     */
    @SuppressWarnings("unchecked")
    public final <T extends AbstractPerceptionCategory> T getPerceptionCategory(final Class<T> clazz)
        throws OperationalPlanException
    {
        Throw.when(!contains(clazz), OperationalPlanException.class, "Perception category" + clazz + " is not present.");
        // addPerceptionCategory guarantees correct combination of class and perception category
        return (T) this.perceptionCategories.get(clazz);
    }

    // *****************************************************************************
    // *** All code below is dummy implementation of the current LanePerception. ***
    // *****************************************************************************

    /** */
    private static final long serialVersionUID = 1L;

    /** {@inheritDoc} */
    @Override
    public void perceive() throws GTUException, NetworkException, ParameterException
    {
    }

    /** {@inheritDoc} */
    @Override
    public Collection<PerceivedObject> getPerceivedObjects()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Collection<PerceivedObject>> getTimeStampedPerceivedObjects() throws GTUException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setGTU(LaneBasedGTU gtu)
    {
    }

    /** {@inheritDoc} */
    @Override
    public LaneBasedGTU getGTU()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Headway getForwardHeadway()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Headway getBackwardHeadway()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Map<Lane, Set<Lane>> getAccessibleAdjacentLanesLeft()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Map<Lane, Set<Lane>> getAccessibleAdjacentLanesRight()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Map<Lane, Set<Lane>> accessibleAdjacentLaneMap(LateralDirectionality lateralDirection)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Headway> getNeighboringHeadwaysLeft()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Headway> getNeighboringHeadwaysRight()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Headway> getNeighboringHeadways(LateralDirectionality lateralDirection)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Headway> getParallelHeadwaysLeft()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Headway> getParallelHeadwaysRight()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Headway> getParallelHeadways(LateralDirectionality lateralDirection)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Speed getSpeedLimit()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<AbstractHeadwayGTU> getFirstLeaders(LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<AbstractHeadwayGTU> getFirstFollowers(LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsGtuAlongside(LateralDirectionality lat)
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<AbstractHeadwayGTU> getLeaders(RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<AbstractHeadwayGTU> getFollowers(RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<InfrastructureLaneChangeInfo> getInfrastructureLaneChangeInfo(RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int getSplitNumber(InfrastructureLaneChangeInfo info)
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public SpeedLimitProspect getSpeedLimitProspect(RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Length getLegalLaneChangePossibility(RelativeLane fromLane, LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Length getPhysicalLaneChangePossibility(RelativeLane fromLane, LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<RelativeLane> getCurrentCrossSection()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<HeadwayTrafficLight> getTrafficLights()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<HeadwayConflict> getIntersectionConflicts(RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Headway> getTimeStampedForwardHeadway()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Headway> getTimeStampedBackwardHeadway()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Map<Lane, Set<Lane>>> getTimeStampedAccessibleAdjacentLanesLeft()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Map<Lane, Set<Lane>>> getTimeStampedAccessibleAdjacentLanesRight()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Collection<Headway>> getTimeStampedNeighboringHeadwaysLeft()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Collection<Headway>> getTimeStampedNeighboringHeadwaysRight()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Collection<Headway>> getTimeStampedParallelHeadwaysLeft()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Collection<Headway>> getTimeStampedParallelHeadwaysRight()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Speed> getTimeStampedSpeedLimit()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedFirstLeaders(LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedFirstFollowers(LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Boolean> existsGtuAlongsideTimeStamped(LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedLeaders(RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedFollowers(RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<InfrastructureLaneChangeInfo>> getTimeStampedInfrastructureLaneChangeInfo(
        RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Integer> getTimeStampedSplitNumber(InfrastructureLaneChangeInfo info)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SpeedLimitProspect> getTimeStampedSpeedLimitProspect(RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Length> getTimeStampedLegalLaneChangePossibility(RelativeLane fromLane,
        LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Length> getTimeStampedPhysicalLaneChangePossibility(RelativeLane fromLane,
        LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<RelativeLane>> getTimeStampedCurrentCrossSection()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<HeadwayTrafficLight>> getTimeStampedTrafficLights()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<HeadwayConflict>> getTimeStampedIntersectionConflicts(RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void updateForwardHeadway() throws GTUException, NetworkException, ParameterException
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateBackwardHeadway() throws GTUException, NetworkException, ParameterException
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateAccessibleAdjacentLanesLeft() throws GTUException
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateAccessibleAdjacentLanesRight() throws GTUException
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateParallelHeadwaysLeft() throws GTUException
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateParallelHeadwaysRight() throws GTUException
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateLaneTrafficLeft() throws GTUException, NetworkException, ParameterException
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateLaneTrafficRight() throws GTUException, NetworkException, ParameterException
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateSpeedLimit() throws GTUException, NetworkException
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateFirstLeaders()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateFirstFollowers()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateGtuAlongside()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateLeaders()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateFollowers()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateInfrastructureLaneChangeInfo()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateSplitNumber()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateSpeedLimitProspect()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateLegalLaneChangePossibility()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updatePhysicalLaneChangePossibility()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateCurrentCrossSection()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateTrafficLights()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateIntersectionConflicts()
    {
    }

}
