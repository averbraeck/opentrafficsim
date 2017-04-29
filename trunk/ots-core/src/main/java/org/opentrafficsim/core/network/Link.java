package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.event.EventType;

/**
 * Link as a connection between two Nodes.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface Link extends Locatable, Serializable
{
    /**
     * Return the network in which this link is registered. Cannot be null.
     * @return Network; the network in which this link is registered
     */
    Network getNetwork();

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

    /** @return the simulator. */
    OTSSimulatorInterface getSimulator();

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

    /**
     * Add a GTU to this link (e.g., for statistical purposes, or for a model on macro level). It is safe to add a GTU again. No
     * warning or error will be given. The GTU_ADD_EVENT will only be fired when the GTU was not already on the link.
     * @param gtu GTU; the GTU to add.
     */
    void addGTU(GTU gtu);

    /**
     * Remove a GTU from this link. It is safe to try to remove a GTU again. No warning or error will be given. The
     * GTU_REMOVE_EVENT will only be fired when the GTU was on the link.
     * @param gtu GTU; the GTU to remove.
     */
    void removeGTU(GTU gtu);

    /**
     * Provide a safe copy of the set of GTUs.
     * @return Set&lt;GTU&gt;; a safe copy of the set of GTUs
     */
    Set<GTU> getGTUs();

    /**
     * Provide the number of GTUs on this link.
     * @return int; the number of GTUs on this link
     */
    int getGTUCount();

    /**
     * The <b>timed</b> event type for pub/sub indicating the addition of a GTU to the link. <br>
     * Payload: Object[] {String gtuId, GTU gtu, int count_after_addition}
     */
    EventType GTU_ADD_EVENT = new EventType("GTU.ADD");

    /**
     * The <b>timed</b> event type for pub/sub indicating the removal of a GTU from the link. <br>
     * Payload: Object[] {String gtuId, GTU gtu, int count_after_removal}
     */
    EventType GTU_REMOVE_EVENT = new EventType("GTU.REMOVE");

}
