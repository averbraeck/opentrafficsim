package org.opentrafficsim.simulationengine.properties;

import java.util.Iterator;

/**
 * User readable and settable properties.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> Type of the property
 */
public interface Property<T>
{
    /**
     * Retrieve the current value of the property.
     * @return T; the current value of the property
     */
    T getValue();

    /**
     * Return a short description of the property.
     * @return String; a short description of the property
     */
    String getShortName();

    /**
     * Return a description of the property (may use HTML markup).
     * @return String; the description of the property
     */
    String getDescription();

    /**
     * Change the value of the property.
     * @param newValue T; the new value for the property
     * @throws PropertyException when this Property is read-only, or newValue is not valid
     */
    void setValue(T newValue) throws PropertyException;

    /**
     * Return true if the property can not be altered.
     * @return boolean; true if this property can not be altered, false if this property can be altered
     */
    boolean isReadOnly();

    /**
     * Display priority determines the order in which properties should be displayed. Properties with lower values should be
     * displayed above or before those with higher values.
     * @return int; the display priority of this AbstractProperty
     */
    int getDisplayPriority();

    /**
     * Generate a description of the state of this property in HTML (excluding the &lt;html&gt; at the start and the
     * &lt;/html&gt; at the end. The result can be embedded in a html-table.
     * @return String; the description of this property and the current state in HTML
     */
    String htmlStateDescription();

    /**
     * Construct a deep copy of this property (duplicates everything except immutable fields).
     * @return AbstractProperty&lt;T&gt;; a deep copy of this AbstractProperty
     */
    AbstractProperty<T> deepCopy();

    /**
     * Return an iterator.
     * @return Iterator&lt;AbstractProperty&gt;; iterator that will visit this property (if this is not compound), or all
     *         sub-properties (if this is compound)
     */
    Iterator<AbstractProperty<T>> iterator();

}
