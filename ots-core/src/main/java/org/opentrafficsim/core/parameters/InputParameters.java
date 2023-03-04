package org.opentrafficsim.core.parameters;

import java.util.Map;
import java.util.Set;

import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface InputParameters
{

    /**
     * Return all object instances of the given class, for which parameters have been defined.
     * @param clazz Class&lt;T&gt;; class
     * @param <T> type of object instances
     * @return all object instances of the given class, for which parameters have been defined
     */
    <T> Set<T> getObjects(Class<T> clazz);

    /**
     * Returns all defined parameters for the given object. For example a specific {@code GtuType}.
     * @param object Object; the object
     * @return all defined parameters for the given object
     */
    Map<String, InputParameter<?, ?>> getInputParameters(Object object);

    /**
     * Returns a specific defined parameter for the given object. For example parameter "a" for a specific {@code GtuType}.
     * @param object Object; the object
     * @param id String; parameter id
     * @return specific defined parameter for the given object
     */
    InputParameter<?, ?> getInputParameter(Object object, String id);

}
