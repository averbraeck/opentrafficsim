package org.opentrafficsim.core.parameters;

import java.util.Map;
import java.util.Set;

import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 5, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
     * Returns all defined parameters for the given object. For example a specific {@code GTUType}.
     * @param object Object; the object
     * @return all defined parameters for the given object
     */
    Map<String, InputParameter<?, ?>> getInputParameters(Object object);
    
    /**
     * Returns a specific defined parameter for the given object. For example parameter "a" for a specific {@code GTUType}.
     * @param object Object; the object
     * @param id String; parameter id
     * @return specific defined parameter for the given object
     */
    InputParameter<?, ?> getInputParameter(Object object, String id);
    
}
