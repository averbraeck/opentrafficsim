package org.opentrafficsim.road.gtu;

import java.util.Iterator;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.event.EventListenerMap;
import org.djutils.event.EventProducer;
import org.djutils.event.EventType;
import org.djutils.math.AngleUtil;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.base.DistancedObject;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.road.gtu.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;
import org.opentrafficsim.road.network.object.LaneBasedObject;

/**
 * Checks for collisions.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CollisionDetector extends AbstractLaneBasedMoveChecker implements EventProducer
{

    /** Collision event. */
    public static final EventType COLLISION = new EventType(new MetaData("COLLISION",
            "Event when a GTU collides into an object", new ObjectDescriptor("GTU", "Colliding GTU", LaneBasedGtu.class),
            new ObjectDescriptor("Object", "Object that is collided into", LaneBasedObject.class)));

    /** Listener map. */
    private final EventListenerMap listenerMap = new EventListenerMap();

    /**
     * Constructor.
     * @param network network
     */
    public CollisionDetector(final Network network)
    {
        super(network);
    }

    @Override
    public void checkMove(final LaneBasedGtu gtu) throws Exception
    {
        try
        {
            NeighborsPerception neighbors =
                    gtu.getTacticalPlanner().getPerception().getPerceptionCategory(NeighborsPerception.class);
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neighbors.getLeaders(RelativeLane.CURRENT);
            Iterator<DistancedObject<LaneBasedGtu>> gtus = leaders.underlyingWithDistance();
            if (!gtus.hasNext())
            {
                return;
            }
            DistancedObject<LaneBasedGtu> leader = gtus.next();
            if (leader.distance().lt0())
            {
                fireEvent(COLLISION, new Object[] {gtu, leader.object()});

                // throw new CollisionException("GTU " + gtu.getId() + " collided with GTU " + leader.object().getId());
            }
        }
        catch (OperationalPlanException exception)
        {
            throw new GtuException(exception);
        }
    }

    @Override
    public EventListenerMap getEventListenerMap()
    {
        return this.listenerMap;
    }

    /**
     * Logs collision with distance, speed difference and angle at info level.
     * @return this collision detector for method chaining
     */
    public CollisionDetector logCollisions()
    {
        addListener((e) ->
        {
            Object[] payload = (Object[]) e.getContent();
            LaneBasedGtu gtu = (LaneBasedGtu) payload[0];
            LaneBasedObject object = (LaneBasedObject) payload[1];
            Speed objectSpeed = object instanceof Gtu otherGtu ? otherGtu.getSpeed() : Speed.ZERO;
            Speed dv = gtu.getSpeed().minus(objectSpeed);
            Length distance = Length.ofSI(gtu.getLocation().distance(object.getLocation()));
            Angle angle = Angle.ofSI(AngleUtil.normalizeAroundZero(object.getDirZ() - gtu.getDirZ()));
            Logger.ots().info("GTU " + gtu.getId() + " collided with " + object.getId() + " at a point distance of " + distance
                    + " with a speed difference of " + dv + " and and angle of " + angle + ".");
        }, COLLISION);
        return this;
    }

    /**
     * Stops the GTU and the object if it is a GTU.
     * @return this collision detector for method chaining
     */
    public CollisionDetector stopCollided()
    {
        addListener((e) ->
        {
            Object[] payload = (Object[]) e.getContent();
            LaneBasedGtu gtu = (LaneBasedGtu) payload[0];
            LaneBasedObject object = (LaneBasedObject) payload[1];
            gtu.stop();
            if (object instanceof LaneBasedGtu otherGtu)
            {
                otherGtu.stop();
            }
        }, COLLISION);
        return this;
    }

    /**
     * Throws an exception upon a collision.
     * @return this collision detector for method chaining
     */
    public CollisionDetector throwException()
    {
        addListener((e) ->
        {
            Object[] payload = (Object[]) e.getContent();
            LaneBasedGtu gtu = (LaneBasedGtu) payload[0];
            LaneBasedObject object = (LaneBasedObject) payload[1];
            throw new CollisionException("GTU " + gtu.getId() + " collided with " + object.getId());
        }, COLLISION);
        return this;
    }

    /**
     * Destroys the GTU upon a collision.
     * @return this collision detector for method chaining
     */
    public CollisionDetector destroyGtu()
    {
        addListener((e) -> ((LaneBasedGtu) ((Object[]) e.getContent())[0]).destroy(), COLLISION);
        return this;
    }

}
