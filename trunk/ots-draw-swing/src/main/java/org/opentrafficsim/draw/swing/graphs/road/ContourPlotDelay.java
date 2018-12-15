package org.opentrafficsim.draw.swing.graphs.road;

import java.awt.Color;
import java.util.List;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.matrix.DurationMatrix;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vfloat.vector.FloatSpeedVector;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.egtf.Converter;
import org.opentrafficsim.core.egtf.Quantity;
import org.opentrafficsim.draw.swing.core.BoundsPaintScale;
import org.opentrafficsim.draw.swing.graphs.AbstractContourPlot;
import org.opentrafficsim.draw.swing.graphs.ContourDataSource;
import org.opentrafficsim.draw.swing.graphs.ContourDataSource.ContourDataType;
import org.opentrafficsim.draw.swing.graphs.GraphType;
import org.opentrafficsim.draw.swing.graphs.GraphUtil;
import org.opentrafficsim.kpi.sampling.SamplingException;
import org.opentrafficsim.kpi.sampling.Trajectory;
import org.opentrafficsim.kpi.sampling.TrajectoryGroup;
import org.opentrafficsim.road.network.sampling.GtuData;
import org.opentrafficsim.road.network.sampling.data.ReferenceSpeed;

/**
 * Contour plot for delay.
 * <p>
 * <i>A note on the unit "/km"</i>. This unit is derived by measuring the total delay over a cell in space-time, which gives an
 * SI value in [s]. With varying granularity, the value needs to be normalized to space-time. Hence, the value is divided by the
 * length of the cell [m], and divided by the duration of the cell [s]. This gives a unit of [s/s/m] = [1/m]. This means that a
 * traffic state represented by a value of D/km, gives a total amount of delay equal to D * x * t, where x * t is the size of
 * the cell, and the resulting value is in the same unit as t. So if D = 50/km, then measuring this state over 2km and during 3
 * hours gives 50 * 2 * 3 = 300h of delay.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ContourPlotDelay extends AbstractContourPlot<Duration>
{

    /** */
    private static final long serialVersionUID = 20181010L;

    /** Quantity for the EGTF. */
    private static final Quantity<Duration, DurationMatrix> QUANTITY = new Quantity<>("delay", new Converter<DurationMatrix>()
    {
        /** {@inheritDoc} */
        @Override
        public DurationMatrix convert(final double[][] filteredData)
        {
            try
            {
                return new DurationMatrix(filteredData, DurationUnit.SI, StorageType.DENSE);
            }
            catch (ValueException exception)
            {
                // should not happen as filtered data comes from the EGTF
                throw new RuntimeException("Unexpected exception while converting duration to output format.", exception);
            }
        }
    });

    /** Contour data type. */
    private static final ContourDataType<Duration, Duration> CONTOUR_DATA_TYPE = new ContourDataType<Duration, Duration>()
    {
        /** {@inheritDoc} */
        @Override
        public Duration identity()
        {
            return Duration.ZERO;
        }

        /** {@inheritDoc} */
        @Override
        public Duration processSeries(final Duration intermediate, final List<TrajectoryGroup<?>> trajectories,
                final List<Length> xFrom, final List<Length> xTo, final Time tFrom, final Time tTo)
        {
            double sumActualTime = 0.0;
            double sumRefTime = 0.0;
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
                            FloatSpeedVector ref = trajectory.getExtendedData(ReferenceSpeed.INSTANCE);
                            float[] v = trajectory.getV();
                            float[] x = trajectory.getX();
                            for (int j = 0; j < v.length - 1; j++)
                            {
                                sumRefTime += (x[j + 1] - x[j]) / ref.get(j).si;
                            }
                        }
                        catch (SamplingException | ValueException exception)
                        {
                            throw new RuntimeException("Unexpected exception while calculating delay.", exception);
                        }
                        sumActualTime += trajectory.getTotalDuration().si;
                    }
                }
            }
            return Duration.createSI(intermediate.si + sumActualTime - sumRefTime);
        }

        /** {@inheritDoc} */
        @Override
        public Duration finalize(final Duration intermediate)
        {
            return intermediate;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public Quantity<Duration, ?> getQuantity()
        {
            return QUANTITY;
        }

    };

    /**
     * Constructor.
     * @param caption String; caption
     * @param simulator OTSSimulatorInterface; simulator
     * @param dataPool ContourDataSource&lt;GtuData&gt;; data pool
     */
    public ContourPlotDelay(final String caption, final OTSSimulatorInterface simulator,
            final ContourDataSource<GtuData> dataPool)
    {
        super(caption, simulator, dataPool, createPaintScale(), new Duration(0.05, DurationUnit.SI), "%.1f/km",
                "delay %.1f /km");
        dataPool.getSampler().registerExtendedDataType(ReferenceSpeed.INSTANCE);
    }

    /**
     * Creates a paint scale from red, via yellow to green.
     * @return ContinuousColorPaintScale; paint scale
     */
    private static BoundsPaintScale createPaintScale()
    {
        double[] boundaries = { 0.0, 0.05, 0.2 };
        Color[] colorValues = BoundsPaintScale.GREEN_RED;
        return new BoundsPaintScale(boundaries, colorValues);
    }

    /** {@inheritDoc} */
    @Override
    public GraphType getGraphType()
    {
        return GraphType.DELAY_CONTOUR;
    }

    /** {@inheritDoc} */
    @Override
    protected double scale(final double si)
    {
        return LinearDensityUnit.PER_KILOMETER.getScale().fromStandardUnit(si);
    }

    /** {@inheritDoc} */
    @Override
    protected double getValue(final int item, final double cellLength, final double cellSpan)
    {
        return getDataPool().get(item, CONTOUR_DATA_TYPE) / (cellLength * cellSpan);
    }

    /** {@inheritDoc} */
    @Override
    protected ContourDataType<Duration, Duration> getContourDataType()
    {
        return CONTOUR_DATA_TYPE;
    }

}
