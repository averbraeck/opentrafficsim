package org.opentrafficsim.road.gtu.lane.perception.object;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;

/**
 * Interface for perceived objects including kinematics. Kinematics describe either a static or dynamic object at a certain
 * distance of, or as adjacent to, a reference object (e.g. the perceiving GTU or a conflict).
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface PerceivedObject extends Identifiable, Comparable<PerceivedObject>
{

    /**
     * Returns object type.
     * @return the (perceived) object Type, can be null if no object type unknown.
     */
    ObjectType getObjectType();

    /**
     * Returns length.
     * @return the length of the other object; can be null if unknown.
     */
    Length getLength();

    /**
     * Returns information on the kinematics of the perceived object. This includes position, speed, acceleration and overlap.
     * @return information on the kinematics of the perceived object
     */
    Kinematics getKinematics();

    /**
     * Returns the distance from the kinematics.
     * @return the distance from the kinematics
     */
    default Length getDistance()
    {
        return getKinematics().getDistance();
    }

    /**
     * Returns the speed from the kinematics.
     * @return the speed from the kinematics
     */
    default Speed getSpeed()
    {
        return getKinematics().getSpeed();
    }

    /**
     * Returns the acceleration from the kinematics.
     * @return the acceleration from the kinematics
     */
    default Acceleration getAcceleration()
    {
        return getKinematics().getAcceleration();
    }

    /**
     * Type of object.
     */
    enum ObjectType
    {
        /** The observed object for headway is a GTU. */
        GTU,

        /** The observed object for headway is a traffic light. */
        TRAFFICLIGHT,

        /** The observed object for headway is a generic object. */
        OBJECT,

        /** There is no observed object, just a distance. */
        DISTANCEONLY,

        /** Intersection conflict. */
        CONFLICT,

        /** Stop line. */
        STOPLINE,

        /** Bus stop. */
        BUSSTOP;

        /**
         * Returns whether this object is a GTU or not.
         * @return whether this object is a GTU or not.
         */
        public boolean isGtu()
        {
            return this.equals(GTU);
        }

        /**
         * Returns whether this object is a GTU or not.
         * @return whether this object is a GTU or not.
         */
        public boolean isTrafficLight()
        {
            return this.equals(TRAFFICLIGHT);
        }

        /**
         * Returns whether this object is an object or not.
         * @return whether this object is an object or not.
         */
        public boolean isObject()
        {
            return this.equals(OBJECT);
        }

        /**
         * Returns whether no object was observed and only a distance was stored.
         * @return whether no object was observed and only a distance was stored.
         */
        public boolean isDistanceOnly()
        {
            return this.equals(DISTANCEONLY);
        }

        /**
         * Returns whether this object is a conflict or not.
         * @return whether this object is a conflict or not.
         */
        public boolean isConflict()
        {
            return this.equals(CONFLICT);
        }

        /**
         * Returns whether this object is a stop line or not.
         * @return whether this object is a stop line or not.
         */
        public boolean isStopLine()
        {
            return this.equals(STOPLINE);
        }

        /**
         * Returns whether this object is a bus stop or not.
         * @return whether this object is a bus stop or not.
         */
        public boolean isBusStop()
        {
            return this.equals(BUSSTOP);
        }
    }

    /**
     * Information on the kinematics of the perceived object.
     */
    interface Kinematics
    {
        /**
         * Retrieve the strongly typed distance to the other object.
         * @return the distance to the object
         */
        Length getDistance();

        /**
         * Returns speed.
         * @return the (perceived) speed of the other object; can be null if unknown
         */
        Speed getSpeed();

        /**
         * Returns acceleration.
         * @return acceleration the (perceived) acceleration of the other object; can be null if unknown
         */
        Acceleration getAcceleration();

        /**
         * Returns whether the object is facing the same direction.
         * @return whether the object is facing the same direction
         */
        boolean isFacingSameDirection();

        /**
         * Returns information on the overlap for parallel objects. For objects fully ahead or behind that fact is provided,
         * with {@code null} overlap values.
         * @return information on the overlap for parallel objects
         */
        Overlap getOverlap();

        /**
         * Return kinematics for a static object at given distance ahead. Overlap is considered non-existent. The object is
         * considered to face the same direction, which might not mean much for a static object.
         * @param distance distance to object
         * @return kinematics for a static object at given distance
         */
        static Kinematics staticAhead(final Length distance)
        {
            return new Record(distance, Speed.ZERO, Acceleration.ZERO, true, Overlap.AHEAD);
        }

        /**
         * Return kinematics for a static object at given distance behind. Overlap is considered non-existent. The object is
         * considered to face the same direction, which might not mean much for a static object.
         * @param distance distance to object
         * @return kinematics for a static object at given distance
         */
        static Kinematics staticBehind(final Length distance)
        {
            return new Record(distance, Speed.ZERO, Acceleration.ZERO, true, Overlap.BEHIND);
        }

        /**
         * Return kinematics for a dynamic object ahead. The distance may be negative up to an absolute value equal to the
         * object length plus the ego length.
         * @param distance distance from ego front to object rear (or front when not facing the same direction)
         * @param objectSpeed speed of perceived object
         * @param objectAcceleration acceleration of perceived object
         * @param facingSameDirection whether the object is facing the same direction
         * @param objectLength object length
         * @param referenceLength length of reference object, usually the perceiving GTU
         * @return kinematic for a dynamic object
         * @throws IllegalArgumentException when the distance beyond the extent of object length plus reference length
         */
        static Kinematics dynamicAhead(final Length distance, final Speed objectSpeed, final Acceleration objectAcceleration,
                final boolean facingSameDirection, final Length objectLength, final Length referenceLength)
        {
            Throw.whenNull(distance, "distance");
            Throw.whenNull(objectLength, "objectLength");
            Throw.whenNull(referenceLength, "referenceLength");
            Throw.when(distance.si < 0.0 && -distance.si > objectLength.si + referenceLength.si, IllegalArgumentException.class,
                    "Distance is negative beyond the combined length of perceived object and ego.");
            Overlap overlap;
            if (distance.ge0())
            {
                overlap = Overlap.AHEAD;
            }
            else
            {
                Length overlapRear = distance.plus(referenceLength);
                Length overlapVal = distance.neg();
                Length overlapFront = distance.plus(objectLength);
                if (overlapRear.lt0())
                {
                    overlapVal = overlapVal.plus(overlapRear);
                }
                if (overlapFront.lt0())
                {
                    overlapVal = overlapVal.plus(overlapFront);
                }
                overlap = new Overlap.Record(overlapVal, overlapFront, overlapRear, false, false);
            }
            return new Record(distance, objectSpeed, objectAcceleration, facingSameDirection, overlap);
        }

        /**
         * Return kinematics for a dynamic object behind. The distance may be negative up to an absolute value equal to the
         * object length plus the ego length.
         * @param distance distance from ego front to object rear (or front when not facing the same direction)
         * @param objectSpeed speed of perceived object
         * @param objectAcceleration acceleration of perceived object
         * @param facingSameDirection whether the object is facing the same direction
         * @param objectLength object length
         * @param referenceLength length of reference object, usually the perceiving GTU
         * @return kinematic for a dynamic object
         * @throws IllegalArgumentException when the distance beyond the extent of object length plus reference length
         */
        static Kinematics dynamicBehind(final Length distance, final Speed objectSpeed, final Acceleration objectAcceleration,
                final boolean facingSameDirection, final Length objectLength, final Length referenceLength)
        {
            Throw.whenNull(distance, "distance");
            Throw.whenNull(objectLength, "objectLength");
            Throw.whenNull(referenceLength, "referenceLength");
            Throw.when(distance.si < 0.0 && -distance.si > objectLength.si + referenceLength.si, IllegalArgumentException.class,
                    "Distance is negative beyond the combined length of perceived object and ego.");
            Overlap overlap;
            if (distance.ge0())
            {
                overlap = Overlap.BEHIND;
            }
            else
            {
                Length overlapRear = distance.plus(objectLength).neg();
                Length overlapVal = distance.neg();
                Length overlapFront = distance.plus(referenceLength).neg();
                if (overlapRear.gt0())
                {
                    overlapVal = overlapVal.minus(overlapRear);
                }
                if (overlapFront.gt0())
                {
                    overlapVal = overlapVal.minus(overlapFront);
                }
                overlap = new Overlap.Record(overlapVal, overlapFront, overlapRear, false, false);
            }
            return new Record(distance, objectSpeed, objectAcceleration, facingSameDirection, overlap);
        }

        /**
         * Record storing kinematics information.
         * @param getDistance distance
         * @param getSpeed speed
         * @param getAcceleration acceleration
         * @param isFacingSameDirection whether the object is facing the same direction
         * @param getOverlap overlap
         */
        record Record(Length getDistance, Speed getSpeed, Acceleration getAcceleration, boolean isFacingSameDirection,
                Overlap getOverlap) implements Kinematics
        {
            /**
             * Null checks.
             * @param getDistance distance
             * @param getSpeed speed
             * @param getAcceleration acceleration
             * @param isFacingSameDirection whether the object is facing the same direction
             * @param getOverlap overlap
             */
            public Record
            {
                Throw.whenNull(getDistance, "getDistance");
                Throw.whenNull(getSpeed, "getSpeed");
                Throw.whenNull(getAcceleration, "getAcceleration");
                Throw.whenNull(getOverlap, "getOverlap");
            }
        }

        /**
         * Description of overlap information. If the object is fully ahead or behind, overlap values are {@code null}.
         */
        interface Overlap
        {
            /** Overlap information for objects ahead. */
            Overlap AHEAD = new Record(null, null, null, true, false);

            /** Overlap information for objects behind. */
            Overlap BEHIND = new Record(null, null, null, false, true);

            /**
             * Return the (perceived) overlap with the other object. This value should be null if there is no overlap. In the
             * figure below for two GTUs, it is distance b, positive for GTU1 and GTU2.
             *
             * <pre>
             * ----------
             * |  GTU 1 |          -----&gt;
             * ----------
             *      ---------------
             *      |    GTU 2    |          -----&gt;
             *      ---------------
             * | a  | b |     c   |
             * </pre>
             *
             * @return Length, the (perceived) overlap with the other object or null if there is no overlap
             */
            Length getOverlap();

            /**
             * Return the (perceived) front overlap to the other object. This value should be null if there is no overlap. In
             * the figure for two GTUs below, it is distance c, positive for GTU1, negative for GTU2.
             *
             * <pre>
             * ----------
             * |  GTU 1 |          -----&gt;
             * ----------
             *      ---------------
             *      |    GTU 2    |          -----&gt;
             *      ---------------
             * | a  | b |     c   |
             * </pre>
             *
             * @return the (perceived) front overlap to the other object or null if there is no overlap
             */
            Length getOverlapFront();

            /**
             * Return the (perceived) rear overlap to the other object. This value should be null if there is no overlap.In the
             * figure below for two GTUs, it is distance a, positive for GTU1, negative for GTU2.
             *
             * <pre>
             * ----------
             * |  GTU 1 |          -----&gt;
             * ----------
             *      ---------------
             *      |    GTU 2    |          -----&gt;
             *      ---------------
             * | a  | b |     c   |
             * </pre>
             *
             * @return the (perceived) rear overlap to the other object or null if there is no overlap
             */
            Length getOverlapRear();

            /**
             * Returns whether the object is fully ahead.
             * @return whether the other object is in front of the reference object
             */
            boolean isAhead();

            /**
             * Returns whether the object is fully behind.
             * @return whether the other object is behind the reference object
             */
            boolean isBehind();

            /**
             * Returns whether the object is parallel, partially or fully.
             * @return whether the other object is parallel the reference object
             */
            default boolean isParallel()
            {
                return getOverlap() != null;
            }

            /**
             * Record storing overlap information. The three overlap values are either all {@code null} or they all have a
             * value. In the former case, either of {@code isAhead} and {@code isBehind} is true.
             * @param getOverlap overlap
             * @param getOverlapFront front overlap
             * @param getOverlapRear rear overlap
             * @param isAhead whether the object is ahead
             * @param isBehind whether the object is behind
             */
            record Record(Length getOverlap, Length getOverlapFront, Length getOverlapRear, boolean isAhead, boolean isBehind)
                    implements Overlap
            {

                /**
                 * Constructor.
                 * @param getOverlap overlap
                 * @param getOverlapFront front overlap
                 * @param getOverlapRear rear overlap
                 * @param isAhead whether the object is ahead
                 * @param isBehind whether the object is behind
                 * @throws NullPointerException when getOverlapFront or getOverlapRear is null while getOverlap is not
                 * @throws IllegalArgumentException when getOverlapFront or getOverlapRear is not null while getOverlap is null
                 * @throws IllegalArgumentException if isAhead or isBehind is true while overlap is specified
                 */
                public Record
                {
                    if (getOverlap == null)
                    {
                        Throw.when(getOverlapFront != null, IllegalArgumentException.class,
                                "getOverlapFront is not null while getOverlap is null.");
                        Throw.when(getOverlapRear != null, IllegalArgumentException.class,
                                "getOverlapRear is not null while getOverlap is null.");
                        Throw.when(isAhead == isBehind, IllegalArgumentException.class,
                                "if getOverlap is null either of isAhead or isBehind, but not both, should be true.");
                    }
                    else
                    {
                        Throw.whenNull(getOverlapFront, "getOverlapFront is null while getOverlap is not null.");
                        Throw.whenNull(getOverlapRear, "getOverlapRear is null while getOverlap is not null.");
                        Throw.when(isAhead, IllegalArgumentException.class,
                                "if getOverlap is not null isAhead should be false.");
                        Throw.when(isBehind, IllegalArgumentException.class,
                                "if getOverlap is not null isBehind should be false.");
                    }
                }
            }
        }
    }

    @Override
    default int compareTo(final PerceivedObject headway)
    {
        if (getKinematics().getDistance() != null)
        {
            if (headway.getKinematics().getDistance() != null)
            {
                return getKinematics().getDistance().compareTo(headway.getKinematics().getDistance());
            }
            return 1;
        }
        else if (headway.getKinematics().getDistance() != null)
        {
            return -1;
        }
        return getKinematics().getOverlap().getOverlapFront().compareTo(headway.getKinematics().getOverlap().getOverlapFront());
    }
}
