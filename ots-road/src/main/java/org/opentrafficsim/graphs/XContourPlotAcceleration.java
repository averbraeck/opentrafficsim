package org.opentrafficsim.graphs;

import java.awt.Color;
import java.util.List;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.matrix.AccelerationMatrix;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.WeightedMeanAndSum;
import org.opentrafficsim.core.animation.EGTF.Converter;
import org.opentrafficsim.core.animation.EGTF.Quantity;
import org.opentrafficsim.graphs.XContourDataPool.ContourDataType;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.simulationengine.OTSSimulatorInterface;

/**
 * Contour plot for acceleration.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class XContourPlotAcceleration extends XAbstractContourPlot<Acceleration>
{

    /** */
    private static final long serialVersionUID = 20181010L;

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
                        return new AccelerationMatrix(filteredData, AccelerationUnit.SI, StorageType.DENSE);
                    }
                    catch (ValueException exception)
                    {
                        // should not happen as filtered data comes from the EGTF
                        throw new RuntimeException("Unexpected exception while converting acceleration to output format.",
                                exception);
                    }
                }
            });

    /** Contour data type. */
    private static final ContourDataType<Acceleration> CONTOUR_DATA_TYPE = new ContourDataType<Acceleration>()
    {
        /** {@inheritDoc} */
        @Override
        public double calculateValue(final List<TrajectoryGroup> trajectories, final List<Length> startDistances,
                final Length xFrom, final Length xTo, final Time tFrom, final Time tTo)
        {
            WeightedMeanAndSum<Double, Double> acceleration = new WeightedMeanAndSum<>();
            for (int i = 0; i < trajectories.size(); i++)
            {
                TrajectoryGroup trajectoryGroup = trajectories.get(i);
                Length x0 = xFrom.minus(startDistances.get(i));
                Length x1 = xTo.minus(startDistances.get(i));
                for (Trajectory<?> trajectory : trajectoryGroup.getTrajectories())
                {
                    if (XPlotUtil.considerTrajectory(trajectory, tFrom, tTo))
                    {
                        trajectory = trajectory.subSet(x0, x1, tFrom, tTo);
                        float[] t = trajectory.getT();
                        float[] a = trajectory.getA();
                        for (int j = 0; j < t.length - 1; j++)
                        {
                            acceleration.add((double) a[j], (double) (t[j + 1] - t[j]));
                        }
                    }
                }
            }
            return acceleration.getMean();
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
     * @param dataPool ContourDataPool; data pool
     */
    public XContourPlotAcceleration(final String caption, final OTSSimulatorInterface simulator,
            final XContourDataPool dataPool)
    {
        super(caption, simulator, dataPool, createPaintScale(), new Acceleration(1.0, AccelerationUnit.SI), "%.0fm/s\u00B2",
                "acceleration %.2f m/s\u00B2");
    }

    /**
     * Creates a paint scale from red, via yellow to green.
     * @return ContinuousColorPaintScale; paint scale
     */
    private static XBoundsPaintScale createPaintScale()
    {
        double[] boundaries = { -3.0, -1.5, 0.0, 1.0, 2.0 };
        Color[] colorValues = XBoundsPaintScale.reverse(XBoundsPaintScale.GREEN_RED_DARK);
        return new XBoundsPaintScale(boundaries, colorValues);
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
    protected ContourDataType<Acceleration> getContourDataType()
    {
        return CONTOUR_DATA_TYPE;
    }

}
