package org.opentrafficsim.draw.colorer.trajectory;

import java.awt.Color;
import java.util.List;
import java.util.function.Function;

import org.opentrafficsim.draw.colorer.AbstractLegendColorer;
import org.opentrafficsim.draw.graphs.OffsetTrajectory.TrajectorySection;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;

/**
 * Colorer based on extended data in trajectory.
 * @param <D> extended data value type
 * @param <V> value type for the colorer
 */
public class ExtendedDataTrajectoryColorer<D, V> extends AbstractLegendColorer<TrajectorySection, V>
        implements TrajectoryColorer
{

    /** Colorer name. */
    private final String name;

    /**
     * Constructor.
     * @param dataType extended data type
     * @param translateFunction function to translate extended data value type to type for the colorer
     * @param colorFunction coloring function
     * @param legend legend
     */
    public ExtendedDataTrajectoryColorer(final ExtendedDataType<? extends D, ?, ?, ?> dataType,
            final Function<D, V> translateFunction, final Function<V, Color> colorFunction, final List<LegendEntry> legend)
    {
        super((traj) -> translateFunction.apply(traj.trajectory().getValue(traj.section(), dataType)), colorFunction, legend);
        this.name = dataType.getDescription();
    }

    @Override
    public String getName()
    {
        return this.name;
    }

}
