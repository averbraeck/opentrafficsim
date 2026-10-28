package org.opentrafficsim.animation.graphs;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.Range;

/**
 * {@link NumberAxis} that applies optional bounds when auto bounds are determined. Either end may also be {@code null}, in
 * which case the automatic data bound is used.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class AutoBoundedNumberAxis extends NumberAxis
{

    /** ID. */
    private static final long serialVersionUID = 20261028L;

    /** Lower bound. */
    private final Double lowerBound;

    /** Upper bound. */
    private final Double upperBound;

    /**
     * Constructor.
     * @param label axis label
     * @param lowerBound lower bound, may be {@code null}
     * @param upperBound upper bound, may be {@code null}
     */
    public AutoBoundedNumberAxis(final String label, final Double lowerBound, final Double upperBound)
    {
        super(label);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    protected void autoAdjustRange()
    {
        super.autoAdjustRange();
        double lower = this.lowerBound != null ? this.lowerBound : getLowerBound();
        double upper = this.upperBound != null ? this.upperBound : getUpperBound();
        setRange(new Range(lower, upper), false, true);
    }

}
