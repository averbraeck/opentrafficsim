package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.TimedEventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.animation.Drawable;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * Link as a connection between two Nodes.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface Link extends Locatable, Serializable, Identifiable, Drawable
{
    /**
     * Return the network in which this link is registered. Cannot be null.
     * @return Network; the network in which this link is registered
     */
    Network getNetwork();

    /** @return id. */
    @Override
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
     * @param gtuType GTUType; the GTU type to request the directionality for
     * @return the longitudinal directionality of the link (FORWARD, BACKWARD, BOTH or NONE) for the given GTU type. NONE will
     *         be returned if no directionality is given.
     */
    LongitudinalDirectionality getDirectionality(GTUType gtuType);

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

    @Override
    Bounds getBounds();
    
    /**
     * The <b>timed</b> event type for pub/sub indicating the addition of a GTU to the link. <br>
     * Payload: Object[] {String gtuId, int count_after_addition}
     */
    TimedEventType GTU_ADD_EVENT = new TimedEventType("LINK.GTU.ADD",
            new MetaData("GTU entered link", "GTU added to link", new ObjectDescriptor[] {
                    new ObjectDescriptor("GTU id", "GTU id", String.class),
                    new ObjectDescriptor("Number of GTUs in link", "Resulting number of GTUs in link", Integer.class) }));

    /**
     * The <b>timed</b> event type for pub/sub indicating the removal of a GTU from the link. <br>
     * Payload: Object[] {String gtuId, int count_after_removal}
     */
    TimedEventType GTU_REMOVE_EVENT = new TimedEventType("LINK.GTU.REMOVE",
            new MetaData("GTU exited link", "GTU removed from link", new ObjectDescriptor[] {
                    new ObjectDescriptor("GTU id", "GTU id", String.class),
                    new ObjectDescriptor("Number of GTUs in link", "Resulting number of GTUs in link", Integer.class) }));

}
