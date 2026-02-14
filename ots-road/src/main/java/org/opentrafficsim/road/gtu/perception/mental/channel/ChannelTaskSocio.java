package org.opentrafficsim.road.gtu.perception.mental.channel;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.perception.mental.AbstractTask;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Tailgating;

/**
 * Task demand for social pressure. This is equal to the social pressure from the follower multiplied with the socio speed
 * sensitivity.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ChannelTaskSocio extends AbstractTask implements ChannelTask, Stateless<ChannelTaskSocio>
{

    /** Singleton instance. */
    public static final ChannelTaskSocio SINGLETON = new ChannelTaskSocio();

    /** Default set that is returned by the supplier. */
    private static final Set<ChannelTask> SET = Set.of(SINGLETON);

    /** Standard supplier that supplies a single instance of the socio task. */
    public static final Function<LanePerception, Set<ChannelTask>> SUPPLIER = (p) -> SET;

    /**
     * Constructor.
     */
    public ChannelTaskSocio()
    {
        super("socio");
    }

    @Override
    public ChannelTaskSocio get()
    {
        return SINGLETON;
    }

    @Override
    public Object getChannel()
    {
        return REAR;
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception)
    {
        NeighborsPerception neighbors = perception.getPerceptionCategoryOptional(NeighborsPerception.class)
                .orElseThrow(() -> new NoSuchElementException("NeighborsPerception not present."));
        Iterator<LaneBasedGtu> followers = neighbors.getFollowers(RelativeLane.CURRENT).underlying();
        if (!followers.hasNext())
        {
            return 0.0;
        }
        try
        {
            double socio = perception.getGtu().getParameters().getParameter(LmrsParameters.SOCIO);
            return followers.next().getParameters().getParameter(Tailgating.RHO) * socio;
        }
        catch (ParameterException ex)
        {
            // follower does not provide social pressure, ignore
            return 0.0;
        }
    }

}
