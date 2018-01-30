package org.opentrafficsim.core.gtu.perception;

import java.io.Serializable;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * The perception module of a GTU. It is responsible for perceiving (sensing) the environment of the GTU, which includes the
 * locations of other GTUs. Perception is done at a certain time, and the perceived information might have a limited validity.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 15, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <G> GTU type
 */
public interface Perception<G extends GTU> extends Serializable
{
    /**
     * Update the perceived information and store it in a stateful manner.
     * @throws GTUException when GTU has not been properly initialized.
     * @throws NetworkException in case of inconsistencies in the network during perception calculations.
     * @throws ParameterException in case of a parameter error.
     */
    void perceive() throws GTUException, NetworkException, ParameterException;

    /**
     * Return the GTU of this perception.
     * @return GTU of this perception
     * @throws GTUException if the GTU has not been initialized
     */
    G getGtu() throws GTUException;

    /**
     * Adds given perception category to the perception.
     * @param perceptionCategory perception category
     */
    <T extends PerceptionCategory<?, ?>> void addPerceptionCategory(T perceptionCategory);

    /**
     * Returns whether the given perception category is present.
     * @param clazz perception category class
     * @param <T> perception category
     * @return whether the given perception category is present
     */
    <T extends PerceptionCategory<?, ?>> boolean contains(Class<T> clazz);

    /**
     * Returns the given perception category.
     * @param category perception category class
     * @param <T> perception category
     * @return given perception category
     * @throws OperationalPlanException if the perception category is not present
     */
    <T extends PerceptionCategory<?, ?>> T getPerceptionCategory(Class<T> category)
            throws OperationalPlanException;

    /**
     * Returns the given perception category, or {@code null} if not present.
     * @param category perception category class
     * @param <T> perception category
     * @return given perception category
     */
    <T extends PerceptionCategory<?, ?>> T getPerceptionCategoryOrNull(Class<T> category);

    /**
     * Remove give perception category.
     * @param perceptionCategory perception category to remove
     */
    void removePerceptionCategory(PerceptionCategory<?, ?> perceptionCategory);

}
