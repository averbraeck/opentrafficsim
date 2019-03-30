package org.opentrafficsim.draw.graphs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.djutils.immutablecollections.ImmutableList;
import org.opentrafficsim.base.WeightedMeanAndSum;

/**
 * A {@code GraphPath} defines the spatial dimension of graphs. It has a number of sections, each of which may have one or more
 * source objects depending on the number of series. For example, a 3-lane road may result in a few sections each having 3
 * series. Graphs can aggregate the series, or show multiple series.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <S> underlying type of path sections
 */
public class GraphPath<S> extends AbstractGraphSpace<S>
{

    /** Sections. */
    private final List<Section<S>> sections;

    /** Start distance per section. */
    private final List<Length> startDistances = new ArrayList<>();

    /** Total path length. */
    private final Length totalLength;

    /** Mean speed limit over the entire path. */
    private final Speed speedLimit;

    /**
     * Constructor for a one-series path.
     * @param name String; name
     * @param sections List&lt;Section&lt;S&gt;&gt;; sections
     */
    public GraphPath(final String name, final List<Section<S>> sections)
    {
        this(new ArrayList<String>()
        {
            /** */
            private static final long serialVersionUID = 20181020L;
            {
                add(name);
            }
        }, sections);
    }

    /**
     * Constructor.
     * @param seriesNames List&lt;String&gt;; names of series
     * @param sections List&lt;Section&lt;S&gt;&gt;; sections
     */
    public GraphPath(final List<String> seriesNames, final List<Section<S>> sections)
    {
        super(seriesNames);
        this.sections = sections;
        Length start = Length.ZERO;
        for (Section<S> section : sections)
        {
            this.startDistances.add(start);
            start = start.plus(section.getLength());
        }
        this.totalLength = start;
        WeightedMeanAndSum<Double, Double> mean = new WeightedMeanAndSum<>();
        for (Section<S> section : sections)
        {
            mean.add(section.getSpeedLimit().si, section.getLength().si);
        }
        this.speedLimit = Speed.createSI(mean.getMean());
    }

    /**
     * Returns the start distance of the section.
     * @param section Section&lt;S&gt;; Section&lt;S&gt; section
     * @return Length; start distance of the section
     */
    public final Length getStartDistance(final Section<S> section)
    {
        int index = this.sections.indexOf(section);
        Throw.when(index == -1, IllegalArgumentException.class, "Section is not part of the path.");
        return this.startDistances.get(index);
    }

    /**
     * Returns the total path length.
     * @return Length; total path length
     */
    public final Length getTotalLength()
    {
        return this.totalLength;
    }

    /**
     * Returns the mean speed over the entire section.
     * @return Speed; mean speed over the entire section
     */
    public final Speed getSpeedLimit()
    {
        return this.speedLimit;
    }

    /**
     * Returns a section.
     * @param index int; index of section
     * @return Section&lt;S&gt;; section
     */
    public Section<S> get(final int index)
    {
        return this.sections.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<S> iterator()
    {
        return new Iterator<S>()
        {

            /** Section iterator. */
            private Iterator<Section<S>> sectionIterator = getSections().iterator();

            /** Source object iterator per section. */
            private Iterator<S> sourceIterator = this.sectionIterator.hasNext() ? this.sectionIterator.next().iterator() : null;

            /** {@inheritDoc} */
            @Override
            public boolean hasNext()
            {
                if (this.sourceIterator != null && this.sourceIterator.hasNext())
                {
                    return true;
                }
                while (this.sectionIterator.hasNext())
                {
                    Iterator<S> it = this.sectionIterator.next().iterator();
                    if (it.hasNext())
                    {
                        this.sourceIterator = it;
                        return true;
                    }
                }
                this.sourceIterator = null;
                return false;
            }

            /** {@inheritDoc} */
            @Override
            public S next()
            {
                Throw.when(!hasNext(), NoSuchElementException.class, "No more element left.");
                return this.sourceIterator.next();
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<S> iterator(final int series)
    {
        List<S> list = new ArrayList<>();
        for (Section<S> section : this.sections)
        {
            list.add(section.getSource(series));
        }
        return new ImmutableArrayList<>(list, Immutable.WRAP).iterator();
    }

    /**
     * Returns an immutable list of the sections.
     * @return ImmutableList&lt;Section&lt;S&gt;&gt;; sections
     */
    public ImmutableList<Section<S>> getSections()
    {
        return new ImmutableArrayList<>(this.sections, Immutable.WRAP);
    }

    /**
     * Interface for sections.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 20 okt. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <S> underlying type
     */
    public interface Section<S> extends Iterable<S>
    {
        /**
         * Returns the section length.
         * @return Length; section length
         */
        Length getLength();

        /**
         * Returns the speed limit on the section.
         * @return Speed; speed limit on the section
         */
        Speed getSpeedLimit();

        /**
         * Returns the source object.
         * @param series int; number
         * @return S; underlying object of the series
         */
        S getSource(int series);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GraphPath [sections=" + this.sections + ", startDistances=" + this.startDistances + ", totalLength="
                + this.totalLength + ", speedLimit=" + this.speedLimit + "]";
    }

}