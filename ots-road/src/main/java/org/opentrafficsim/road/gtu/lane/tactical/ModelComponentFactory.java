package org.opentrafficsim.road.gtu.lane.tactical;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * Interface for factories of model components, such as strategical planners, tactical planners and car-following models. This
 * interface defines no method to obtain the model component. This is because different factories may require different input.
 * For all cases that require no input {@code ModelComponentSupplier} defines an additional {@code get()} method.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@FunctionalInterface
public interface ModelComponentFactory
{

    /**
     * Returns parameters for the given component. These parameters should contain, and possibly overwrite, parameters from
     * sub-components. A parameter factory at the highest level (strategical planner) may overwrite any parameter. This
     * combination allows that for sub-components, default factories can be used, while the parameter factory only overwrites
     * parameters different for specific GTU types. The default implementation returns all default parameter values declared at
     * the class.<br>
     * <br>
     * Conventional use is:<br>
     *
     * <pre>
     * Parameters parameters = this.subComponent1Factory.getParameters();
     * this.subComponent2Factory.getParameters().setAllIn(parameters);
     * parameters.setDefaultParameters(componentClass);
     * parameters.setDefaultParameters(staticUtilityClass);
     * return parameters;
     * </pre>
     *
     * where all parameters used in {@code componentClass} are defined or forwarded in {@code componentClass}.<br>
     *
     * <pre>
     * // forwarded
     * public static final ParameterTypeAcceleration A = ParameterTypes.A;
     *
     * // defined
     * public static final ParameterTypeDouble FACTOR = new ParameterTypeDouble("factor", "factor on response", 1.0);
     * </pre>
     *
     * The same holds for static utilities that are used. Parameters should be defined at the utility class, and parameters of
     * used utilities should be included.<br>
     * <br>
     * Because high-level model components might determine what low-level components to use depending on GTU type, and hence
     * which parameters might be required, the GTU type is given as input. Many components will however not need it to return
     * the required parameters.<br>
     * <br>
     * @param gtuType GTU type
     * @return parameters for the given component
     * @throws ParameterException on parameter exception
     */
    Parameters getParameters(GtuType gtuType) throws ParameterException;

}
