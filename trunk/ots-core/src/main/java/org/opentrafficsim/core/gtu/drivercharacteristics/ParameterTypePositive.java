package org.opentrafficsim.core.gtu.drivercharacteristics;

import org.djunits.value.vdouble.scalar.DoubleScalar;

/**
 * Extends ParameterType with a default check for a positive and non-zero value.
 * @author Wouter Schakel
 */
public class ParameterTypePositive<T extends DoubleScalar.Rel<?>> extends ParameterType<T> {

	/**
     * Constructor with default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     */
	public ParameterTypePositive(String id, String description, Class<T> valueClass, 
			T defaultValue) {
		super(id, description, valueClass, defaultValue);
	}

	/**
     * Constructor without default value.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     */
	public ParameterTypePositive(String id, String description, Class<T> valueClass) {
		super(id, description, valueClass);
	}

	/**
     * Check if value is positive and non-zero.
     * @param value Value to check with constraints.
     * @throws ParameterException If the value does not comply with constraints.
     */
    public void check(T value) throws ParameterException
    {
        ParameterException.failIf(value.si<=0, "Value of positive parameter type '"+this.getId()+"' is negative or zero.");
    }
	
}