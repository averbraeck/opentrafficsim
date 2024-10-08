package org.opentrafficsim.base.parameters;

/**
 * Interface for parameter objects containing the methods for during a simulation.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface Parameters
{

    /**
     * Set parameter value of given parameter type.
     * @param parameterType the parameter type.
     * @param value new value for the parameter of type <code>parameterType</code>.
     * @param <T> Class of value.
     * @throws ParameterException If the value does not comply with value type constraints.
     */
    // @docs/06-behavior/parameters.md (without throws)
    <T> void setParameter(ParameterType<T> parameterType, T value) throws ParameterException;

    /**
     * Set parameter value of given parameter type, store old value to allow a reset.
     * @param parameterType the parameter type.
     * @param value new value for the parameter of type <code>parameterType</code>.
     * @param <T> Class of value.
     * @throws ParameterException If the value does not comply with value type constraints.
     */
    <T> void setParameterResettable(ParameterType<T> parameterType, T value) throws ParameterException;

    /**
     * Resets the parameter value to the value from before the last set. This goes only a single value back.
     * @param parameterType the parameter type.
     * @throws ParameterException If the parameter was never set.
     */
    void resetParameter(ParameterType<?> parameterType) throws ParameterException;

    /**
     * Get parameter of given type.
     * @param parameterType the parameter type.
     * @param <T> Class of value.
     * @return parameter of the requested type if it exists
     * @throws ParameterException If the parameter was never set.
     */
    // @docs/06-behavior/parameters.md (without throws)
    <T> T getParameter(ParameterType<T> parameterType) throws ParameterException;

    /**
     * Returns a parameter value, or {@code null} if not present. This can be used to prevent frequent calls to both
     * {@code contains()} and {@code getParameter()} in performance critical code.
     * @param parameterType parameter type
     * @param <T> type of parameter value
     * @return parameter value, or {@code null} if not present
     */
    <T> T getParameterOrNull(ParameterType<T> parameterType);

    /**
     * Indicate whether the given parameter type has been set.
     * @param parameterType the parameter type to check
     * @return true if <code>parameterType</code> has been set; false if <code>parameterType</code> has not been set
     */
    boolean contains(ParameterType<?> parameterType);

    /**
     * Sets the parameters of this set in the given set.
     * @param parameters parameters to set the values in
     */
    void setAllIn(Parameters parameters);

}
