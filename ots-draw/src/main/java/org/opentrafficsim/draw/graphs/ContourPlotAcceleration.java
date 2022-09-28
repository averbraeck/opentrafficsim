package org.opentrafficsim.draw.graphs;

import java.awt.Color;
import java.util.List;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.matrix.AccelerationMatrix;
import org.djunits.value.vdouble.matrix.base.DoubleMatrix;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.WeightedMeanAndSum;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.egtf.Converter;
import org.opentrafficsim.core.egtf.Quantity;
import org.opentrafficsim.draw.core.BoundsPaintScale;
import org.opentrafficsim.draw.graphs.ContourDataSource.ContourDataType;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;

/**
 * Contour plot for acceleration.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ContourPlotAcceleration extends AbstractContourPlot<Acceleration>
{

    /** Quantity for the EGTF. */
    private static final Quantity<Acceleration, AccelerationMatrix> QUANTITY =
            new Quantity<>("acceleration", new Converter<AccelerationMatrix>()
            {
                /** {@inheritDoc} */
                @Override
                public AccelerationMatrix convert(final double[][] filteredData)
                {
                    try
                    {
                        return DoubleMatrix.instantiate(filteredData, AccelerationUnit.SI, StorageType.DENSE);
                    }
                    catch (ValueRuntimeException exception)
                    {
                        // should not happen as filtered data comes from the EGTF
                        throw new RuntimeException("Unexpected exception while converting acceleration to output format.",
                                exception);
                    }
                }
            });

    /** Contour data type. */
    private static final ContourDataType<Acceleration, WeightedMeanAndSum<Double, Double>> CONTOUR_DATA_TYPE =
            new ContourDataType<Acceleration, WeightedMeanAndSum<Double, Double>>()
            {
                /** {@inheritDoc} */
                @Override
                public WeightedMeanAndSum<Double, Double> identity()
                {
                    return new WeightedMeanAndSum<>();
                }

                /** {@inheritDoc} */
                @Override
                public WeightedMeanAndSum<Double, Double> processSeries(final WeightedMeanAndSum<Double, Double> intermediate,
                        final List<TrajectoryGroup<?>> trajectories, final List<Length> xFrom, final List<Length> xTo,
                        final Time tFrom, final Time tTo)
                {
                    for (int i = 0; i < trajectories.size(); i++)
                    {
                        TrajectoryGroup<?> trajectoryGroup = trajectories.get(i);
                        for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
                        {
                            if (GraphUtil.considerTrajectory(trajectory, tFrom, tTo))
                            {
                                trajectory = trajectory.subSet(xFrom.get(i), xTo.get(i), tFrom, tTo);
                                float[] t = trajectory.getT();
                                float[] a = trajectory.getA();
                                for (int j = 0; j < t.length - 1; j++)
                                {
                                    intermediate.add((double) a[j], (double) (t[j + 1] - t[j]));
                                }
                            }
                        }
                    }
                    return intermediate;
                }

                /** {@inheritDoc} */
                @Override
                public Acceleration finalize(final WeightedMeanAndSum<Double, Double> intermediate)
                {
                    return Acceleration.instantiateSI(intermediate.getMean());
                }

                /** {@inheritDoc} */
                @SuppressWarnings("synthetic-access")
                @Override
                public Quantity<Acceleration, ?> getQuantity()
                {
                    return QUANTITY;
                }

            };

    /**
     * Constructor.
     * @param caption String; caption
     * @param simulator OTSSimulatorInterface; simulator
     * @param dataPool ContourDataSource&lt;?&gt;; data pool
     */
    public ContourPlotAcceleration(final String caption, final OTSSimulatorInterface simulator,
            final ContourDataSource<?> dataPool)
    {
        super(caption, simulator, dataPool, createPaintScale(), new Acceleration(1.0, AccelerationUnit.SI), "%.0fm/s\u00B2",
                "acceleration %.2f m/s\u00B2");
    }

    /**
     * Creates a paint scale from red, via yellow to green.
     * @return ContinuousColorPaintScale; paint scale
     */
    private static BoundsPaintScale createPaintScale()
    {
        double[] boundaries = {-3.0, -1.5, 0.0, 1.0, 2.0};
        Color[] colorValues = BoundsPaintScale.reverse(BoundsPaintScale.GREEN_RED_DARK);
        return new BoundsPaintScale(boundaries, colorValues);
    }

    /** {@inheritDoc} */
    @Override
    public GraphType getGraphType()
    {
        return GraphType.ACCELERATION_CONTOUR;
    }

    /** {@inheritDoc} */
    @Override
    protected double scale(final double si)
    {
        return si;
    }

    /** {@inheritDoc} */
    @Override
    protected double getValue(final int item, final double cellLength, final double cellSpan)
    {
        return getDataPool().get(item, CONTOUR_DATA_TYPE);
    }

    /** {@inheritDoc} */
    @Override
    protected ContourDataType<Acceleration, WeightedMeanAndSum<Double, Double>> getContourDataType()
    {
        return CONTOUR_DATA_TYPE;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ContourPlotAcceleration []";
    }

}
