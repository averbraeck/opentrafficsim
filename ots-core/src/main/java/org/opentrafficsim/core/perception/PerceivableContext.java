package org.opentrafficsim.core.perception;

import java.util.Set;

import org.djutils.base.Identifiable;
import org.opentrafficsim.core.gtu.Gtu;

/**
 * The Model package guarantees that objects that are used in an OTS study such as GTUs are retrievable. In a spatial model, for
 * instance, objects need to be able to find each other. The model interface guarantees access to a number of important objects
 * in the study.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public interface PerceivableContext extends Identifiable
{
    /**
     * Get a descriptive Id of the perceivable context (e.g., useful for debugging purposes).
     * @return the id of the context
     */
    @Override
    String getId();

    /**
     * Get an overview of the GTUs in the model. The set returned is a defensive copy.
     * @return a set of GTUs as registered in the current model.
     */
    Set<Gtu> getGTUs();

    /**
     * Get a GTU in the model.
     * @param gtuId the id of the GTU
     * @return a GTU as registered in the current model, or null when the id could not be found.
     */
    Gtu getGTU(String gtuId);

    /**
     * Add a GTU to the network.
     * @param gtu the GTU to add
     */
    void addGTU(Gtu gtu);

    /**
     * Remove a GTU from the network.
     * @param gtu the GTU to remove
     */
    void removeGTU(Gtu gtu);

    /**
     * Test whether a GTU is registered in the network.
     * @param gtu the GTU to search for
     * @return whether the network contains this GTU
     */
    boolean containsGTU(Gtu gtu);

    /**
     * Test whether a GTU ID is registered in the network.
     * @param gtuId the GTU ID to search for
     * @return whether the network contains a GTU with this ID
     */
    boolean containsGtuId(String gtuId);

}
