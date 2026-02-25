package org.opentrafficsim.base.parameters;

import java.util.Optional;

/**
 * Interface for parameter objects containing the methods for during a simulation.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @param parameterType the parameter type
     * @param value new value for the parameter of type {@code parameterType}
     * @param <T> class of value
     * @throws ParameterException if the value does not comply with value type constraints or is claimed
     */
    // @docs/06-behavior/parameters.md (without throws)
    <T> void setParameter(ParameterType<T> parameterType, T value) throws ParameterException;

    /**
     * Set parameter value of given parameter type. This method claims setting the value by the key. No other key may be used to
     * set the parameter. Different locations of a single logical unit may set the same parameter (with claim) if they share a
     * common key to do so.
     * @param parameterType the parameter type
     * @param value new value for the parameter of type {@code parameterType}
     * @param key key object for unique right to set the parameter value
     * @param <T> class of value
     * @throws ParameterException if the value does not comply with value type constraints or is claimed by another key
     */
    <T> void setClaimedParameter(ParameterType<T> parameterType, T value, Object key) throws ParameterException;

    /**
     * Set parameter value of given parameter type, store old value to allow a reset. This method ignores any claim on the
     * parameter, and should always be followed by a reset.
     * @param parameterType the parameter type
     * @param value new value for the parameter of type {@code parameterType}
     * @param <T> class of value
     * @throws ParameterException if the value does not comply with value type constraints
     */
    <T> void setParameterResettable(ParameterType<T> parameterType, T value) throws ParameterException;

    /**
     * Resets the parameter value to the value from before the last resettable set. This goes only a single value back.
     * @param parameterType the parameter type
     * @throws ParameterException if the parameter was never set
     * @throws NullPointerException when any input is null
     */
    void resetParameter(ParameterType<?> parameterType) throws ParameterException;

    /**
     * Get parameter of given type.
     * @param parameterType the parameter type
     * @param <T> class of value
     * @return parameter of the requested type if it exists
     * @throws ParameterException if the parameter was never set
     */
    // @docs/06-behavior/parameters.md (without throws)
    <T> T getParameter(ParameterType<T> parameterType) throws ParameterException;

    /**
     * Returns a parameter value, or {@code null} if not present. This can be used to prevent frequent calls to both
     * {@code contains()} and {@code getParameter()} in performance critical code.
     * @param parameterType parameter type
     * @param <T> type of parameter value
     * @return parameter value, empty if not present
     */
    <T> Optional<T> getOptionalParameter(ParameterType<T> parameterType);

    /**
     * Indicate whether the given parameter type has been set.
     * @param parameterType the parameter type to check
     * @return true if {@code parameterType} has been set; false if {@code parameterType} has not been set
     */
    boolean contains(ParameterType<?> parameterType);

    /**
     * Sets the parameters of this set in the given set.
     * @param parameters parameters to set the values in
     */
    void setAllIn(Parameters parameters);

}
