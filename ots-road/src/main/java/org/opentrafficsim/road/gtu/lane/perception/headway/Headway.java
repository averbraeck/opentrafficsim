package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.perception.PerceivedObject;

/**
 * Interface for a reference to information about a (lane based) GTU and a headway. The Headway can store information about GTUs
 * or objects ahead of the reference GTU, behind the reference GTU, or (partially) parallel to the reference GTU. In addition to
 * the (perceived) headway, several other pieces of information can be stored, such as (perceived) speed, (perceived)
 * acceleration, (perceived) turn indicators, and (perceived) braking lights. <br>
 * Special care must be taken in curves when perceiving headway of a GTU or object on an adjacent lane.The question is whether
 * we perceive the parallel or ahead/behind based on a line perpendicular to the front/back of the GTU (rectangular), or
 * perpendicular to the center line of the lane (wedge-shaped in case of a curve). The difficulty of a wedge-shaped situation is
 * that reciprocity might be violated: in case of a clothoid, for instance, it is not sure that the point on the center line
 * when projected from lane 1 to lane 2 is the same as the projection from lane 2 to lane 1. The same holds for shapes with
 * sharp bends. Therefore, algorithms implementing headway should only project the <i>reference point</i> of the reference GTU
 * on the center line of the adjacent lane, and then calculate the forward position and backward position on the adjacent lane
 * based on the reference point. Still, our human perception of what is parallel and what not, is not reflected by fractional
 * positions. See examples in
 * <a href= "http://simulation.tudelft.nl:8085/browse/OTS-113">http://simulation.tudelft.nl:8085/browse/OTS-113</a>.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1368 $, $LastChangedDate: 2015-09-02 00:20:20 +0200 (Wed, 02 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 11 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface Headway extends PerceivedObject, Comparable<Headway>
{
    /** the object types that can be distinguished for headway. */
    enum ObjectType
    {
        /** the observed object for headway is a GTU. */
        GTU,

        /** the observed object for headway is a traffic light. */
        TRAFFICLIGHT,

        /** the observed object for headway is a generic object. */
        OBJECT,

        /** there is no observed object, just a distance. */
        DISTANCEONLY,

        /** intersection conflict. */
        CONFLICT,

        /** stop line. */
        STOPLINE,

        /** bus stop. */
        BUSSTOP;

        /** @return whether this object is a GTU or not. */
        public boolean isGtu()
        {
            return this.equals(GTU);
        }

        /** @return whether this object is a GTU or not. */
        public boolean isTrafficLight()
        {
            return this.equals(TRAFFICLIGHT);
        }

        /** @return whether this object is an object or not. */
        public boolean isObject()
        {
            return this.equals(OBJECT);
        }

        /** @return whether no object was observed and only a distance was stored. */
        public boolean isDistanceOnly()
        {
            return this.equals(DISTANCEONLY);
        }

        /** @return whether this object is a conflict or not. */
        public boolean isConflict()
        {
            return this.equals(CONFLICT);
        }

        /** @return whether this object is a stop line or not. */
        public boolean isStopLine()
        {
            return this.equals(STOPLINE);
        }

        /** @return whether this object is a bus stop or not. */
        public boolean isBusStop()
        {
            return this.equals(BUSSTOP);
        }

    }

    /**
     * @return String; the id of the other object for comparison purposes, cannot be null.
     */
    @Override
    String getId();

    /**
     * @return Length; the length of the other object; can be null if unknown.
     */
    Length getLength();

    /**
     * @return Speed; the (perceived) speed of the other object; can be null if unknown.
     */
    Speed getSpeed();

    /**
     * Retrieve the strongly typed distance to the other object.
     * @return Length; the distance to the object, return value null indicates that the other object is parallel to the
     *         reference object
     */
    Length getDistance();

    /**
     * @return Length; the (perceived) object Type, can be null if no object type unknown.
     */
    ObjectType getObjectType();

    /**
     * @return Acceleration; acceleration the (perceived) acceleration of the other object; can be null if unknown.
     */
    Acceleration getAcceleration();

    /**
     * Return the (perceived) front overlap to the other object. This value should be null if there is no overlap. In the figure
     * for two GTUs below, it is distance c, positive for GTU1, negative for GTU2.
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
     * @return Length; the (perceived) front overlap to the other object or null if there is no overlap.
     */
    Length getOverlapFront();

    /**
     * Return the (perceived) rear overlap to the other object. This value should be null if there is no overlap.In the figure
     * below for two GTUs, it is distance a, positive for GTU1, negative for GTU2.
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
     * @return Length; the (perceived) rear overlap to the other object or null if there is no overlap.
     */
    Length getOverlapRear();

    /**
     * Return the (perceived) overlap with the other object. This value should be null if there is no overlap. In the figure
     * below for two GTUs, it is distance b, positive for GTU1 and GTU2.
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
     * @return Length, the (perceived) overlap with the other object or null if there is no overlap.
     */
    Length getOverlap();

    /**
     * @return whether the other object is in front of the reference object.
     */
    boolean isAhead();

    /**
     * @return whether the other object is behind the reference object.
     */
    boolean isBehind();

    /**
     * @return whether the other object is parallel the reference object.
     */
    boolean isParallel();

    /** {@inheritDoc} */
    @Override
    default int compareTo(final Headway headway)
    {
        if (getDistance() != null)
        {
            if (headway.getDistance() != null)
            {
                return getDistance().compareTo(headway.getDistance());
            }
            return 1;
        }
        else if (headway.getDistance() != null)
        {
            return -1;
        }
        return getOverlapFront().compareTo(headway.getOverlapFront());
    }
}
