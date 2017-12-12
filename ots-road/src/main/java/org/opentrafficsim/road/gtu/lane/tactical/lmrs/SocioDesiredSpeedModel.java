package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTU.CacheKey;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredSpeedModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Meta-desired speed model, which interpolates the base desired speed to the desired speed of the follower at the level of the
 * socio-parameter.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 nov. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SocioDesiredSpeedModel implements DesiredSpeedModel
{

    /** Socio-courtesy parameter. */
    protected static final ParameterTypeDouble SOCIO = LmrsParameters.SOCIO;

    /** GTU to obtain additional information. */
    private final LaneBasedGTU gtu;

    /** Base model for desired speed. */
    private final DesiredSpeedModel baseModel;

    /** Key to cache calculated desired speed. */
    private static final CacheKey<Speed> SPEEDKEY = new CacheKey<>();

    /**
     * Constructor.
     * @param gtu GTU
     * @param baseModel base model for desired speed
     */
    public SocioDesiredSpeedModel(final LaneBasedGTU gtu, final DesiredSpeedModel baseModel)
    {
        this.gtu = gtu;
        this.baseModel = baseModel;
    }

    /** {@inheritDoc} */
    @Override
    public Speed desiredSpeed(final Parameters parameters, final SpeedLimitInfo speedInfo) throws ParameterException
    {
        Speed desiredSpeed = this.gtu.getCachedValue(SPEEDKEY);
        if (desiredSpeed == null)
        {
            desiredSpeed = this.baseModel.desiredSpeed(parameters, speedInfo);
            SortedSet<HeadwayGTU> followers;
            NeighborsPerception neighbors =
                    this.gtu.getTacticalPlanner().getPerception().getPerceptionCategoryOrNull(NeighborsPerception.class);
            if (neighbors != null)
            {
                followers = neighbors.getFollowers(RelativeLane.CURRENT);
                if (!followers.isEmpty())
                {
                    HeadwayGTU follower = followers.first();
                    Speed desiredSpeedFollower = follower.getCarFollowingModel().desiredSpeed(follower.getParameters(),
                            follower.getSpeedLimitInfo());
                    if (desiredSpeed.lt(desiredSpeedFollower))
                    {
                        desiredSpeed = Speed.interpolate(desiredSpeed, desiredSpeedFollower, parameters.getParameter(SOCIO));
                    }
                }
            }
            this.gtu.cacheValue(SPEEDKEY, desiredSpeed);
        }
        return desiredSpeed;
    }

}
