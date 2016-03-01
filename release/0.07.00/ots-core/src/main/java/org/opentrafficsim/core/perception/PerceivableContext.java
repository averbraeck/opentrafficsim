package org.opentrafficsim.core.perception;

import java.util.Set;

import org.opentrafficsim.core.gtu.GTU;

/**
 * The Model package guarantees that objects that are used in an OTS study such as GTUs are retrievable. In a spatial model, for
 * instance, objects need to be able to find each other. The model interface guarantees access to a number of important objects
 * in the study.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 10, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface PerceivableContext
{
    /**
     * Get an overview of the GTUs in the model.
     * @return a set of GTUs as registered in the current model.
     */
    Set<GTU> getGTUs();

    /**
     * Add a GTU to the network.
     * @param gtu the GTU to add
     */
    void addGTU(GTU gtu);

    /**
     * Remove a GTU from the network.
     * @param gtu the GTU to remove
     */
    void removeGTU(GTU gtu);

    /**
     * Test whether a GTU is registered in the network.
     * @param gtu the GTU to search for
     * @return whether the network contains this GTU
     */
    boolean containsGTU(GTU gtu);

}
