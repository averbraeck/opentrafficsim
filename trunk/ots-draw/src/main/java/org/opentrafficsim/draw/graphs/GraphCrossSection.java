package org.opentrafficsim.draw.graphs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.opentrafficsim.draw.graphs.GraphPath.Section;

/**
 * A {@code GraphCrossSection} defines the location of graphs. It has one section having one or more source objects depending on
 * the number of series. For example, a 3-lane road may result in a section with 3 series. Graphs can aggregate the series, or
 * show multiple series.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 22 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <S> underlying type of path sections
 */
public class GraphCrossSection<S> extends AbstractGraphSpace<S>
{

    /** Section. */
    private final Section<S> section;

    /** Position on the section. */
    private final List<Length> positions;

    /**
     * Constructor for a one-series cross section.
     * @param seriesName String; name of series
     * @param section Section&lt;S&gt;; section
     * @param position Length; position on the section
     */
    public GraphCrossSection(final String seriesName, final Section<S> section, final Length position)
    {
        this(new ArrayList<String>()
        {
            /** */
            private static final long serialVersionUID = 20181022L;
            {
                add(seriesName);
            }
        }, section, new ArrayList<Length>()
        {
            /** */
            private static final long serialVersionUID = 20181022L;
            {
                add(position);
            }
        });
    }

    /**
     * Constructor.
     * @param seriesNames List&lt;String&gt;; names of series
     * @param section Section&lt;S&gt;; section
     * @param positions List&lt;Length&gt;; position on the section
     */
    public GraphCrossSection(final List<String> seriesNames, final Section<S> section, final List<Length> positions)
    {
        super(seriesNames);
        this.section = section;
        this.positions = positions;
    }

    /**
     * Returns the underlying source of the series.
     * @param series int; series number
     * @return S; underlying source of the series
     */
    public S getSource(final int series)
    {
        return this.section.getSource(series);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<S> iterator(final int series)
    {
        List<S> list = new ArrayList<>();
        list.add(this.section.getSource(series));
        return new ImmutableArrayList<>(list, Immutable.WRAP).iterator();
    }

    /**
     * Returns the position on the underlying source of the series.
     * @param series int; series number
     * @return Length; position on the underlying source of the series
     */
    public Length position(final int series)
    {
        return this.positions.get(series);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<S> iterator()
    {
        return this.section.iterator();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GraphCrossSection [section=" + this.section + ", positions=" + this.positions + "]";
    }

}
