package org.opentrafficsim.road.gtu.strategical.od;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.djunits.unit.DurationUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.vector.DurationVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.math.Draw;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.network.lane.CrossSectionLink;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Split fraction at a node with fractions per link, optionally per gtu type.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 mei 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SplitFraction
{

    /** Node. */
    private final Node node;

    /** Interpolation. */
    private final Interpolation interpolation;

    /** Random stream. */
    private final StreamInterface random;

    /** Simulator. */
    private final OTSSimulatorInterface simulator;

    /** Map of fractions by GTUType and Link. */
    private final Map<GTUType, Map<Link, Map<Duration, Double>>> fractions = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param node Node; node
     * @param interpolation Interpolation; interpolation
     * @param random StreamInterface; random stream
     * @param simulator OTSSimulatorInterface; simulator
     */
    public SplitFraction(final Node node, final Interpolation interpolation, final StreamInterface random,
            final OTSSimulatorInterface simulator)
    {
        this.node = node;
        this.interpolation = interpolation;
        this.random = random;
        this.simulator = simulator;
    }

    /**
     * Add fraction to link, this will apply to {@code GTUType.ALL} and all time.
     * @param link Link; link
     * @param fraction double; fraction
     */
    public void addFraction(final Link link, final double fraction)
    {
        addFraction(link, link.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE), fraction);
    }

    /**
     * Add fraction to link for gtu type, this will apply to all time.
     * @param link Link; link
     * @param gtuType GTUType; gtu type
     * @param fraction double; fraction
     */
    public void addFraction(final Link link, final GTUType gtuType, final double fraction)
    {
        double[] fracs = new double[2];
        fracs[0] = fraction;
        fracs[1] = fraction;
        DurationVector time;
        try
        {
            double[] t = new double[2];
            t[1] = Double.MAX_VALUE;
            time = DoubleVector.instantiate(t, DurationUnit.SI, StorageType.DENSE);
        }
        catch (ValueRuntimeException exception)
        {
            // should not happen, input is not null
            throw new RuntimeException("Input null while creating duration vector.", exception);
        }
        addFraction(link, gtuType, time, fracs);
    }

    /**
     * Add fraction to link over time, this will apply to {@code GTUType.ALL}.
     * @param link Link; link
     * @param time DurationVector; time
     * @param fraction double[]; fraction
     */
    public void addFraction(final Link link, final DurationVector time, final double[] fraction)
    {
        addFraction(link, link.getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE), time, fraction);
    }

    /**
     * Add fraction to link over time for gtu type.
     * @param link Link; link
     * @param gtuType GTUType; gtu type
     * @param time DurationVector; time
     * @param fraction double[]; fraction
     */
    public void addFraction(final Link link, final GTUType gtuType, final DurationVector time, final double[] fraction)
    {
        Throw.when(time.size() != fraction.length, IllegalArgumentException.class,
                "Time vector and fraction array require equal length.");
        Throw.when(!this.node.getLinks().contains(link), IllegalArgumentException.class, "Link %s is not connected to node %s.",
                link, this.node);
        for (double f : fraction)
        {
            Throw.when(f < 0.0, IllegalArgumentException.class, "Fraction should be larger than 0.0.");
        }
        if (this.fractions.containsKey(gtuType))
        {
            this.fractions.put(gtuType, new LinkedHashMap<>());
        }
        this.fractions.get(gtuType).put(link, new TreeMap<>());
        for (int i = 0; i <= time.size(); i++)
        {
            try
            {
                this.fractions.get(gtuType).get(link).put(time.get(i), fraction[i]);
            }
            catch (ValueRuntimeException exception)
            {
                // should not happen, sizes are checked
                throw new RuntimeException("Index out of range.", exception);
            }
        }
    }

    /**
     * Draw next link based on split fractions. If no fractions were defined, split fractions are determined based on the number
     * of lanes per link.
     * @param gtuType GTUType; gtuType
     * @return next link
     */
    public Link draw(final GTUType gtuType)
    {
        for (GTUType gtu : this.fractions.keySet())
        {
            if (gtuType.isOfType(gtu))
            {
                Map<Link, Double> currentFractions = new LinkedHashMap<>();
                double t = this.simulator.getSimulatorTime().si;
                for (Link link : this.fractions.get(gtu).keySet())
                {
                    Iterator<Duration> iterator = this.fractions.get(gtu).get(link).keySet().iterator();
                    Duration prev = iterator.next();
                    while (iterator.hasNext())
                    {
                        Duration next = iterator.next();
                        if (prev.si <= t && t < next.si)
                        {
                            // TODO let interpolation interpolate itself
                            double f;
                            if (this.interpolation.equals(Interpolation.STEPWISE))
                            {
                                f = this.fractions.get(gtuType).get(link).get(prev);
                            }
                            else
                            {
                                double r = (t - prev.si) / (next.si - prev.si);
                                f = (1 - r) * this.fractions.get(gtuType).get(link).get(prev)
                                        + r * this.fractions.get(gtuType).get(link).get(next);
                            }
                            currentFractions.put(link, f);
                            break;
                        }
                    }
                }
                return Draw.drawWeighted(currentFractions, this.random);
            }
        }
        // GTU Type not defined, distribute by number of lanes (or weight = 1.0 if not a CrossSectionLink)
        boolean fractionAdded = false;
        for (Link link : this.node.getLinks())
        {
            if ((link.getStartNode().equals(this.node) && link.getDirectionality(gtuType).isForwardOrBoth())
                    || (link.getEndNode().equals(this.node) && link.getDirectionality(gtuType).isBackwardOrBoth()))
            {
                if (link instanceof CrossSectionLink)
                {
                    int n = ((CrossSectionLink) link).getLanes().size();
                    if (n > 0)
                    {
                        fractionAdded = true;
                        addFraction(link, gtuType, n);
                    }
                }
                else
                {
                    fractionAdded = true;
                    addFraction(link, gtuType, 1.0);
                }
            }
        }
        Throw.when(!fractionAdded, UnsupportedOperationException.class,
                "Split fraction on node %s cannot be derived for gtuType %s as there are no outgoing links.", this.node,
                gtuType);
        // redraw with the information that was just set
        return draw(gtuType);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.fractions == null) ? 0 : this.fractions.hashCode());
        result = prime * result + ((this.node == null) ? 0 : this.node.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        SplitFraction other = (SplitFraction) obj;
        if (this.fractions == null)
        {
            if (other.fractions != null)
            {
                return false;
            }
        }
        else if (!this.fractions.equals(other.fractions))
        {
            return false;
        }
        if (this.node == null)
        {
            if (other.node != null)
            {
                return false;
            }
        }
        else if (!this.node.equals(other.node))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "SplitFraction [node=" + this.node + ", fractions=" + this.fractions + "]";
    }

}
