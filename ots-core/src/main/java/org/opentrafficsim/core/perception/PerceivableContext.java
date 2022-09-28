package org.opentrafficsim.core.perception;

import java.util.Set;

import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.core.definitions.Definitions;
import org.opentrafficsim.core.gtu.GTU;

/**
 * The Model package guarantees that objects that are used in an OTS study such as GTUs are retrievable. In a spatial model, for
 * instance, objects need to be able to find each other. The model interface guarantees access to a number of important objects
 * in the study.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 10, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface PerceivableContext extends Definitions, Identifiable
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
    Set<GTU> getGTUs();

    /**
     * Get a GTU in the model.
     * @param gtuId String; the id of the GTU
     * @return a GTU as registered in the current model, or null when the id could not be found.
     */
    GTU getGTU(String gtuId);

    /**
     * Add a GTU to the network.
     * @param gtu GTU; the GTU to add
     */
    void addGTU(GTU gtu);

    /**
     * Remove a GTU from the network.
     * @param gtu GTU; the GTU to remove
     */
    void removeGTU(GTU gtu);

    /**
     * Test whether a GTU is registered in the network.
     * @param gtu GTU; the GTU to search for
     * @return whether the network contains this GTU
     */
    boolean containsGTU(GTU gtu);

    /**
     * Test whether a GTU ID is registered in the network.
     * @param gtuId String; the GTU ID to search for
     * @return whether the network contains a GTU with this ID
     */
    boolean containsGtuId(String gtuId);

}
