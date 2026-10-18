package org.opentrafficsim.core.gtu.perception;

import java.util.Optional;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * The perception module of a GTU. It is responsible for perceiving (sensing) the environment of the GTU, which includes the
 * locations of other GTUs. Perception is done at a certain time, and the perceived information might have a limited validity.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 * @param <G> GTU type
 */
public interface Perception<G extends Gtu>
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
     */
    G getGtu();

    /**
     * Adds given perception category to the perception.
     * @param perceptionCategory perception category
     * @param <P> perception type
     */
    <P extends Perception<? extends G>> void addPerceptionCategory(
            PerceptionCategory<? extends G, ? extends P, ?> perceptionCategory);

    /**
     * Returns whether the given perception category is present.
     * @param category perception category class
     * @return whether the given perception category is present
     */
    boolean contains(PerceptionCategory.Type<?> category);

    /**
     * Returns the given perception category.
     * @param category perception category class
     * @param <C> perception category
     * @return given perception category
     * @throws OperationalPlanException if the perception category is not present
     */
    <C extends PerceptionCategory<?, ?, ?>> C getPerceptionCategory(PerceptionCategory.Type<C> category)
            throws OperationalPlanException;

    /**
     * Returns the given perception category.
     * @param category perception category class
     * @param <C> perception category
     * @return given perception category, empty if not present
     */
    <C extends PerceptionCategory<?, ?, ?>> Optional<C> getPerceptionCategoryOptional(PerceptionCategory.Type<C> category);

    /**
     * Remove give perception category.
     * @param category perception category to remove
     */
    void removePerceptionCategory(PerceptionCategory<?, ?, ?> category);

}
