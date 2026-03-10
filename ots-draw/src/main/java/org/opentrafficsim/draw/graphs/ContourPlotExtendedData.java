package org.opentrafficsim.draw.graphs;

import java.util.List;
import java.util.function.Function;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.base.DoubleScalarRel;
import org.djutils.exceptions.Throw;
import org.djutils.math.means.ArithmeticMean;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.egtf.Quantity;
import org.opentrafficsim.draw.graphs.ContourDataSource.ContourAdditionalDataType;
import org.opentrafficsim.draw.graphs.ContourDataSource.ContourDataType;
import org.opentrafficsim.kpi.sampling.SamplingException;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataNumber;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;

/**
 * Contour data plot for any numerical extended trajectory data. For extended trajectory data valued with a DJUNITS type,
 * sub-class {@link UnitPlot} is available.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <Z> value type
 */
public class ContourPlotExtendedData<Z extends Number> extends AbstractContourPlot<Z>
{

    /**
     * Constructor with default paint scale (red at minimum, yellow at mid-point, green at maximum).
     * @param caption caption
     * @param source data source
     * @param extendedDataType extended data type
     * @param valueConverter value converter
     * @param bounds paint scale bounds
     * @param labelData label data
     */
    public ContourPlotExtendedData(final String caption, final ContourDataSource source,
            final ExtendedDataNumber<?> extendedDataType, final Function<Double, Z> valueConverter, final Bounds<Z> bounds,
            final LabelData<Z> labelData)
    {
        super(caption, source, constructDataType(extendedDataType, valueConverter), bounds, labelData);
    }

    /**
     * Constructor with specified paint scale.
     * @param caption caption
     * @param source data source
     * @param extendedDataType extended data type
     * @param valueConverter value converter
     * @param paintScale paint scale
     * @param labelData label data
     */
    public ContourPlotExtendedData(final String caption, final ContourDataSource source,
            final ExtendedDataNumber<?> extendedDataType, final Function<Double, Z> valueConverter,
            final BoundsPaintScale paintScale, final LabelData<Z> labelData)
    {
        super(caption, source, constructDataType(extendedDataType, valueConverter), paintScale, labelData);
    }

    /**
     * This method is a total hack, as super will call getContourDataType() which will return this.contourDataType, which at
     * that point has not been set. So we set one statically before it is called, by forwarding the caption argument through
     * this method.
     * @param <Z> value type
     * @param extendedDataType extended data type
     * @param valueConverter value converter
     * @return contour data type
     */
    private static <Z extends Number> ExtendedContourDataType<Z> constructDataType(final ExtendedDataNumber<?> extendedDataType,
            final Function<Double, Z> valueConverter)
    {
        Quantity<Z, double[][]> quantity = Quantity.si("extended_data_" + extendedDataType.getId());
        return new ExtendedContourDataType<>(extendedDataType, quantity, valueConverter);
    }

    @Override
    public GraphType getGraphType()
    {
        return GraphType.OTHER;
    }

    @Override
    protected double scale(final double si)
    {
        return si;
    }

    @Override
    public String toString()
    {
        return "ContourPlotExtendedData [" + getCaption() + "]";
    }

    /**
     * Attention contour data type.
     * @param <Z> value type
     */
    private static class ExtendedContourDataType<Z extends Number>
            implements ContourAdditionalDataType<Z, ArithmeticMean<Double, Double>>
    {

        /** Extended data type. */
        private final ExtendedDataType<?, ? extends float[], ?, ?> dataType;

        /** Quantity. */
        private final Quantity<Z, ?> quantity;

        /** Value converter. */
        private final Function<Double, Z> valueConverter;

        /**
         * Constructor.
         * @param dataType extended data type
         * @param quantity quantity
         * @param valueConverter value converter
         */
        ExtendedContourDataType(final ExtendedDataType<?, ? extends float[], ?, ?> dataType, final Quantity<Z, ?> quantity,
                final Function<Double, Z> valueConverter)
        {
            this.dataType = dataType;
            this.quantity = quantity;
            this.valueConverter = valueConverter;
        }

        @Override
        public ArithmeticMean<Double, Double> identity()
        {
            return new ArithmeticMean<>();
        }

        @Override
        public ArithmeticMean<Double, Double> processSeries(final ArithmeticMean<Double, Double> intermediate,
                final List<TrajectoryGroup<?>> trajectories, final List<Length> xFrom, final List<Length> xTo,
                final Duration tFrom, final Duration tTo)
        {
            for (int i = 0; i < trajectories.size(); i++)
            {
                TrajectoryGroup<?> trajectoryGroup = trajectories.get(i);
                for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
                {
                    if (GraphUtil.considerTrajectory(trajectory, tFrom, tTo))
                    {
                        trajectory = trajectory.subSet(xFrom.get(i), xTo.get(i), tFrom, tTo);
                        try
                        {
                            ContourDataType.weightedNaN(trajectory.getExtendedData(this.dataType), trajectory.getX(),
                                    intermediate);
                        }
                        catch (SamplingException ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
            return intermediate;
        }

        @Override
        public Z finalize(final ArithmeticMean<Double, Double> intermediate)
        {
            return this.valueConverter.apply(intermediate.getMean());
        }

        @Override
        public Quantity<Z, ?> getQuantity()
        {
            return this.quantity;
        }

        @Override
        public boolean normalize()
        {
            return false;
        }

    };

    /**
     * Extension of {@link ContourPlotExtendedData} for DJUNITS data types. The legend format will be <code>"%.1f{unit}"</code>.
     * The label format will be <code>"{quantity} %.2f {unit}"</code>. A last digit in the unit will be made in to a
     * superscript.
     * @param <U> unit type
     * @param <Z> unit value type
     */
    public static class UnitPlot<U extends Unit<U>, Z extends DoubleScalarRel<U, Z>> extends ContourPlotExtendedData<Z>
    {

        /** Character codes for superscripts 0-9. */
        private static final char[] SUPER =
                {'\u2070', '\u00B9', '\u00B2', '\u00B3', '\u2074', '\u2075', '\u2076', '\u2077', '\u2078', '\u2079'};

        /** One value. */
        private final Z one;

        /** Unit. */
        private final U unit;

        /**
         * Constructor with default paint scale (red at minimum, yellow at mid-point, green at maximum).
         * @param caption caption
         * @param source data source
         * @param extendedDataType extended data type
         * @param bounds paint scale bounds
         * @param legendStep legend step
         * @param unit unit to display values in
         */
        public UnitPlot(final String caption, final ContourDataSource source, final ExtendedDataNumber<?> extendedDataType,
                final Bounds<Z> bounds, final Z legendStep, final U unit)
        {
            super(caption, source, extendedDataType, getConverter(legendStep), bounds, constructLabelData(legendStep, unit));
            this.one = legendStep.divide(legendStep.si);
            this.unit = unit;
        }

        /**
         * Constructor with specified paint scale.
         * @param caption caption
         * @param source data source
         * @param extendedDataType extended data type
         * @param paintScale paint scale
         * @param legendStep legend step
         * @param unit unit to display values in
         */
        public UnitPlot(final String caption, final ContourDataSource source, final ExtendedDataNumber<?> extendedDataType,
                final BoundsPaintScale paintScale, final Z legendStep, final U unit)
        {
            super(caption, source, extendedDataType, getConverter(legendStep), paintScale,
                    constructLabelData(legendStep, unit));
            this.one = legendStep.divide(legendStep.si);
            this.unit = unit;
        }

        /**
         * Constructs value converter.
         * @param <U> unit type
         * @param <Z> unit value type
         * @param legendStep legend step
         * @return value converter
         */
        private static <U extends Unit<U>, Z extends DoubleScalarRel<U, Z>> Function<Double, Z> getConverter(final Z legendStep)
        {
            // TODO: use unit to create value with new DJUNITS version
            Throw.whenNull(legendStep, "legendStep");
            Z one = legendStep.divide(legendStep.si);
            return (v) -> one.times(v);
        }

        /**
         * Constructs label data. The legend format will be <code>"%.1f{unit}"</code>. The label format will be
         * <code>"{quantity} %.2f {unit}"</code>. A last digit in the unit will be made in to a superscript.
         * @param <U> unit type
         * @param <Z> unit value type
         * @param legendStep legend step
         * @param unit unit
         * @return label data
         */
        private static <U extends Unit<U>, Z extends DoubleScalarRel<U, Z>> LabelData<Z> constructLabelData(final Z legendStep,
                final U unit)
        {
            String unitString = unit.getId();
            // superscript last digit
            if (unitString != null && !unitString.isEmpty())
            {
                int last = unitString.length() - 1;
                char c = unitString.charAt(last);
                if (Character.isDigit(c))
                {
                    unitString = unitString.substring(0, last) + SUPER[c - '0'];
                }
            }
            return new LabelData<>(legendStep, "%.1f" + unitString,
                    "%.2f " + unit.getQuantity().getName().toLowerCase() + " " + unitString);
        }

        @Override
        protected double scale(final double si)
        {
            // TODO: use unit to create value with new DJUNITS version
            return this.one.times(si).getInUnit(this.unit);
        }

    }
}
