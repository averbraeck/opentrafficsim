package org.opentrafficsim.road.gtu.perception.categories;

import java.util.Iterator;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.DistancedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.AbstractPerceptionReiterable;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.object.PerceivedLaneBasedObjectBase;
import org.opentrafficsim.road.gtu.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.network.object.LocalDistraction;

/**
 * Perception category for distractions.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LocalDistractionPerception extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
{

    /** Look-ahead distance. */
    public static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /**
     * Constructor.
     * @param perception perception.
     */
    public LocalDistractionPerception(final LanePerception perception)
    {
        super(perception);
    }

    /**
     * Return distractions.
     * @return distractions.
     * @throws ParameterException on missing parameter
     */
    public PerceptionCollectable<PerceivedLocalDistraction, LocalDistraction> getDistractions() throws ParameterException
    {
        Iterable<Entry<LocalDistraction>> iterable = getPerception().getLaneStructure()
                .getDownstreamObjects(RelativeLane.CURRENT, LocalDistraction.class, RelativePosition.FRONT, false);
        return new AbstractPerceptionReiterable<>(getGtu())
        {
            @Override
            protected Iterator<DistancedObject<LocalDistraction>> primaryIterator()
            {
                Iterator<Entry<LocalDistraction>> iterator = iterable.iterator();
                return new Iterator<>()
                {
                    @Override
                    public boolean hasNext()
                    {
                        return iterator.hasNext();
                    }

                    @Override
                    public DistancedObject<LocalDistraction> next()
                    {
                        Entry<LocalDistraction> entry = iterator.next();
                        return new DistancedObject<>(entry.object(), entry.distance());
                    }
                };
            }

            @Override
            protected PerceivedLocalDistraction perceive(final LocalDistraction object, final Length distance)
                    throws GtuException, ParameterException
            {
                return new PerceivedLocalDistraction(distance, object);
            }
        };
    }

    /**
     * Information on distraction.
     */
    public class PerceivedLocalDistraction extends PerceivedLaneBasedObjectBase
    {
        /** Distraction. */
        private final LocalDistraction distraction;

        /**
         * Constructor.
         * @param distance distance
         * @param distraction distraction
         */
        public PerceivedLocalDistraction(final Length distance, final LocalDistraction distraction)
        {
            super(distraction.getFullId(), ObjectType.OBJECT, Length.ZERO, Kinematics.staticAhead(distance),
                    distraction.getLane());
            this.distraction = distraction;
        }

        /**
         * Returns the range of the distraction, which applies upstream of the location.
         * @return range of the distraction
         */
        public Length getRange()
        {
            return this.distraction.getRange();
        }

        /**
         * Returns the distraction level as normalized task demand.
         * @return distraction level
         */
        public double getDistractionLevel()
        {
            return this.distraction.getDistractionLevel();
        }

        /**
         * Returns the side of the distraction, relative to the driving direction.
         * @return side of the distraction
         */
        public LateralDirectionality getSide()
        {
            return this.distraction.getSide();
        }
    }
}
