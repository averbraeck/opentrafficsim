package org.opentrafficsim.core.gtu.perception;

import java.io.Serializable;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * The perception module of a GTU. It is responsible for perceiving (sensing) the environment of the GTU, which includes the
 * locations of other GTUs. Perception is done at a certain time, and the perceived information might have a limited validity.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @param <G> GTU type
 */
public interface Perception<G extends Gtu> extends Serializable
{
    /**
     * Invoked by the GTU prior to determining the operational plan. If the implementation works in-sync with the tactical
     * planner, this method is used to update perceived information. It is however possible for the implementation to ignore
     * this and have fully autonomous perception.
     * @throws GtuException when GTU has not been properly initialized.
     * @throws NetworkException in case of inconsistencies in the network during perception calculations.
     * @throws ParameterException in case of a parameter error.
     */
    void perceive() throws GtuException, NetworkException, ParameterException;

    /**
     * Return the GTU of this perception.
     * @return GTU of this perception
     * @throws GtuException if the GTU has not been initialized
     */
    G getGtu() throws GtuException;

    /**
     * Adds given perception category to the perception.
     * @param perceptionCategory T; perception category
     * @param <T> perception category type
     */
    <T extends PerceptionCategory<?, ?>> void addPerceptionCategory(T perceptionCategory);

    /**
     * Returns whether the given perception category is present.
     * @param category Class&lt;T&gt;; perception category class
     * @param <T> perception category
     * @return whether the given perception category is present
     */
    <T extends PerceptionCategory<?, ?>> boolean contains(Class<T> category);

    /**
     * Returns the given perception category.
     * @param category Class&lt;T&gt;; perception category class
     * @param <T> perception category
     * @return given perception category
     * @throws OperationalPlanException if the perception category is not present
     */
    <T extends PerceptionCategory<?, ?>> T getPerceptionCategory(Class<T> category) throws OperationalPlanException;

    /**
     * Returns the given perception category, or {@code null} if not present.
     * @param category Class&lt;T&gt;; perception category class
     * @param <T> perception category
     * @return given perception category
     */
    <T extends PerceptionCategory<?, ?>> T getPerceptionCategoryOrNull(Class<T> category);

    /**
     * Remove give perception category.
     * @param perceptionCategory PerceptionCategory&lt;?,?&gt;; perception category to remove
     */
    void removePerceptionCategory(PerceptionCategory<?, ?> perceptionCategory);

}
