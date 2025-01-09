package org.opentrafficsim.core.distributions;

import org.djutils.exceptions.Throw;

/**
 * Immutable storage for a frequency (or probability) plus a Generator.
 * @param frequency the (<b>not cumulative</b>) frequency (or probability) of the <cite>generatingObject</cite>
 * @param object an object
 * @param <O> Type of the object returned by the draw method
 */
public record FrequencyAndObject<O>(double frequency, O object)
{
    /**
     * Constructor.
     * @param frequency the (<b>not cumulative</b>) frequency (or probability) of the <cite>generatingObject</cite>
     * @param object an object
     */
    public FrequencyAndObject
    {
        Throw.when(frequency < 0.0, IllegalArgumentException.class, "Negative frequency.");
    }
}
