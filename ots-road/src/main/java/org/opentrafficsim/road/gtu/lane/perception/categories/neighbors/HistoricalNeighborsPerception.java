package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 31 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class HistoricalNeighborsPerception extends AbstractPerceptionCategory<LaneBasedGTU, LanePerception>
        implements NeighborsPerception, Serializable
{

    /** */
    private static final long serialVersionUID = 20180131L;

    /** Number of followers. */
    private final int nFollowers;

    /** Number of leaders. */
    private final int nLeaders;

    /** Set of followers per relative lane. */
    private final Map<RelativeLane, TimeStampedObject<SortedSet<HeadwayGTU>>> followers = new HashMap<>();

    /** Set of leaders per relative lane. */
    private final Map<RelativeLane, TimeStampedObject<SortedSet<HeadwayGTU>>> leaders = new HashMap<>();

    /** Set of first followers per lane upstream of merge per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, TimeStampedObject<SortedSet<HeadwayGTU>>> firstFollowers = new HashMap<>();

    /** Set of first leaders per lane downstream of split per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, TimeStampedObject<SortedSet<HeadwayGTU>>> firstLeaders = new HashMap<>();

    /** Whether a GTU is alongside per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, TimeStampedObject<Boolean>> gtuAlongside = new HashMap<>();

    /** Headway GTU type that should be used. */
    private final HeadwayGtuType headwayGtuType;

    public HistoricalNeighborsPerception(final LanePerception perception, final HeadwayGtuType headwayGtuType)
    {
        this(perception, Integer.MAX_VALUE, Integer.MAX_VALUE, headwayGtuType);
    }

    public HistoricalNeighborsPerception(final LanePerception perception, final int nFollowers, final int nLeaders,
            final HeadwayGtuType headwayGtuType)
    {
        super(perception);
        this.nFollowers = nFollowers;
        this.nLeaders = nLeaders;
        this.headwayGtuType = headwayGtuType;
    }

    /** {@inheritDoc} */
    @Override
    public void updateAll() throws GTUException, NetworkException, ParameterException
    {
        this.followers.clear();
        this.leaders.clear();
        this.firstFollowers.clear();
        this.firstLeaders.clear();
        this.gtuAlongside.clear();
        // TODO how to get a specific String to getCrossSection???
        Set<RelativeLane> crossSection =
                Try.assign(() -> getPerception().getPerceptionCategory(InfrastructurePerception.class).getCrossSection(),
                        GTUException.class, "HistoricalNeighborsPerception requires InfrastructurePerception");
        for (RelativeLane lane : crossSection)
        {

        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateFirstLeaders(final LateralDirectionality lat) throws ParameterException, GTUException, NetworkException
    {
        updateAll();
    }

    /** {@inheritDoc} */
    @Override
    public void updateFirstFollowers(final LateralDirectionality lat) throws GTUException, ParameterException, NetworkException
    {
        updateAll();
    }

    /** {@inheritDoc} */
    @Override
    public void updateGtuAlongside(final LateralDirectionality lat) throws GTUException, ParameterException, NetworkException
    {
        updateAll();
    }

    /** {@inheritDoc} */
    @Override
    public void updateLeaders(final RelativeLane lane) throws ParameterException, GTUException, NetworkException
    {
        updateAll();
    }

    /** {@inheritDoc} */
    @Override
    public void updateFollowers(final RelativeLane lane) throws GTUException, NetworkException, ParameterException
    {
        updateAll();
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<HeadwayGTU> getFirstLeaders(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<HeadwayGTU> getFirstFollowers(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isGtuAlongside(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getLeaders(final RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getFollowers(final RelativeLane lane)
    {
        return null;
    }

}
