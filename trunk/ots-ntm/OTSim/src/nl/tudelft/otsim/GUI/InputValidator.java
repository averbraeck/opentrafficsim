package nl.tudelft.otsim.GUI;

/**
 * Class to handle validation of user input in the {@link ObjectInspector}.
 * 
 * @author Peter Knoppers
 */
public class InputValidator {
	final String regex;
	final double minimumValue;
	final double maximumValue;
	
	/**
	 * Interface specification for a custom input validator
	 * 
	 * @author Peter Knoppers
	 */
	public interface CustomValidator {
		/**
		 * Validation method for a CustomValidator.
		 * @param originalValue String; old value of the parameter
		 * @param proposedValue String; proposed new value for the parameter
		 * @return Boolean; true if the proposed value is acceptable; false if
		 * the proposed value is not acceptable
		 */
		boolean validate(String originalValue, String proposedValue);
	}
	
	CustomValidator customValidator = null;
	
	/**
	 * Create an InputValidator for a floating point, or integer value.
	 * @param regex String; regular expression for acceptable floating point
	 * values
	 * @param minimumValue Double; minimum value accepted by this InputValidator
	 * @param maximumValue Double; maximum value accepted by this InputValidator
	 */
	public InputValidator(String regex, double minimumValue, double maximumValue) {
		this.regex = regex;
		this.minimumValue = minimumValue;
		this.maximumValue = maximumValue;
	}
	
	/**
	 * Create an InputValidator for a String value.
	 * @param regex String; regular expression for acceptable values
	 */
	public InputValidator(String regex) {
		this.regex = regex;
		minimumValue = Double.NaN;
		maximumValue = Double.NaN;
	}
	
	/**
	 * Create an InputValidator that uses a CustomValidator.
	 * @param customValidator CustomValidator; customValidator to use.
	 */
	public InputValidator(CustomValidator customValidator) {
		this.regex = ".*";
		this.minimumValue = Double.NaN;
		this.maximumValue = Double.NaN;
		this.customValidator = customValidator;
	}
	
	/**
	 * Check whether the range of values permissible in a floating point or
	 * integer value InputValidator is restricted to one value.
	 * @return Boolean; true if exactly one value is accepted
	 */
	public boolean totallyConstrained() {
		if (Double.isNaN(minimumValue) || Double.isNaN(maximumValue))
			return false;
		return minimumValue == maximumValue;
	}
	
	/**
	 * Check a proposed value for acceptability.
	 * @param originalValue String; original value of the parameter
	 * @param proposedValue String; proposed new value for the parameter
	 * @return Boolean; true if the proposed value is acceptable; false if the
	 * proposed value is not acceptable
	 */
	public boolean validate(String originalValue, String proposedValue) {
		if (! proposedValue.matches(regex))
			return false;
		if (! Double.isNaN(minimumValue)) {
			try {
				double dv = Double.parseDouble(proposedValue);
				if (dv < minimumValue)
					return false;
			} catch (Exception e) {
				return false;
			}
		}
		if (! Double.isNaN(maximumValue)) {
			try {
				double dv = Double.parseDouble(proposedValue);
				if (dv > maximumValue)
					return false;
			} catch (Exception e) {
				return false;
			}
		}
		if (null != customValidator)
			return customValidator.validate(originalValue, proposedValue);
		return true;
	}
}
