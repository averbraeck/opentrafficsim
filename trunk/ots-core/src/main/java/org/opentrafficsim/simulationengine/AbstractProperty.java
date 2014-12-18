package org.opentrafficsim.simulationengine;

/**
 * Abstract property.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 18 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <T> type of the property
 */
public abstract class AbstractProperty<T>
{
    /**
     * Retrieve the current value of the property.
     * @return T; the current value of the property
     */
    abstract T getValue();
    
    /**
     * Return a short description of the property.
     * @return String; a short description of the property
     */
    abstract String shortName();
    
    /**
     * Return a description of the property (may use HTML markup).
     * @return String; the description of the property
     */
    abstract String description();
    
    /**
     * Change the value of the property.
     * @param newValue T; the new value for the property
     */
    abstract void setValue(T newValue) throws IncompatiblePropertyException;
    
    /**
     * Return true if the property can not be altered.
     * @return boolean; true if this property can not be altered, false if this property can be altered
     */
    abstract boolean isReadOnly();
}
