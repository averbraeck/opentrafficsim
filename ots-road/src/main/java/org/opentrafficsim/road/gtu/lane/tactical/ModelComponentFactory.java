package org.opentrafficsim.road.gtu.lane.tactical;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;

/**
 * Interface for factories of model components, such as strategical planners, tactical planners and car-following models.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
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
     * @return Parameters; parameters for the given component
     * @throws ParameterException on parameter exception
     */
    Parameters getParameters() throws ParameterException;

}
