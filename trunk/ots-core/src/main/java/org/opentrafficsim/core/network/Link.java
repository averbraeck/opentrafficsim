package org.opentrafficsim.core.network;

import java.io.Serializable;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * Link as a connection between two Nodes.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface Link extends Locatable, Serializable
{
    /** @return id. */
    String getId();

    /** @return start node. */
    Node getStartNode();

    /** @return end node. */
    Node getEndNode();

    /** @return the link type. */
    LinkType getLinkType();

    /** @return the design line. */
    OTSLine3D getDesignLine();

    /** @return length of the link. */
    Length getLength();

    /**
     * This method returns the directionality of the link for a GTU type. It might be that the link is FORWARD (from start node
     * to end node) for the GTU type CAR, but BOTH for the GTU type BICYCLE (i.e., bicycles can also go from end node to start
     * node). If there is no entry for the given GTU Type, the values of GTUType.ALL will be returned. If this entry is not
     * present, LongitudinalDirectionality.NONE will be returned.
     * @param gtuType the GTU type to request the directionality for
     * @return the longitudinal directionality of the link (FORWARD, BACKWARD, BOTH or NONE) for the given GTU type. NONE will
     *         be returned if no directionality is given.
     */
    LongitudinalDirectionality getDirectionality(final GTUType gtuType);

    /**
     * This method sets the directionality of the link for a GTU type. It might be that the link is FORWARD (from start node to
     * end node) for the GTU type CAR, but BOTH for the GTU type BICYCLE (i.e., bicycles can also go from end node to start
     * node). If the directionality for a GTUType is set to NONE, this means that the given GTUTYpe cannot use the Link. If a
     * Directionality is set for GTUType.ALL, the getDirectionality will default to these settings when there is no specific
     * entry for a given directionality. This means that the settings can be used additive, or restrictive. <br>
     * In <b>additive use</b>, set the directionality for GTUType.ALL to NONE, or do not set the directionality for GTUType.ALL.
     * Now, one by one, the allowed directionalities can be added. An example is a highway, which we only open for CAR, TRUCK
     * and BUS. <br>
     * In <b>restrictive use</b>, set the directionality for GTUType.ALL to BOTH, FORWARD, or BACKWARD. Override the
     * directionality for certain GTUTypes to a more restrictive access, e.g. to NONE. An example is a road that is open for all
     * road users, except PEDESTRIAN.
     * @param gtuType the GTU type to set the directionality for.
     * @param directionality the longitudinal directionality of the link (FORWARD, BACKWARD, BOTH or NONE) for the given GTU
     *            type.
     */
    void addDirectionality(final GTUType gtuType, final LongitudinalDirectionality directionality);

    /**
     * This method removes an earlier provided directionality of the link for a given GTU type, e.g. for maintenance of the
     * link. After removing, the directionality for the GTU will fall back to the provided directionality for GTUType.ALL (if
     * present). Thereby removing a directionality is different from setting the directionality to NONE.
     * @param gtuType the GTU type to remove the directionality for.
     */
    void removeDirectionality(final GTUType gtuType);

    /** {@inheritDoc} */
    @Override
    DirectedPoint getLocation();

    /** {@inheritDoc} */
    @Override
    Bounds getBounds();
}
