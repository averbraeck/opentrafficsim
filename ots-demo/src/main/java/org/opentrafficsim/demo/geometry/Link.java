package org.opentrafficsim.demo.geometry;

import java.rmi.RemoteException;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.CrossSectionLink;
import org.opentrafficsim.core.network.LinearGeometry;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class Link extends CrossSectionLink<String, Node>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param id id
     * @param startNode sn
     * @param endNode en
     * @param length l
     * @param capacity c
     * @param geometry g
     * @param simulator simulator
     */
    public Link(final String id, final Node startNode, final Node endNode, final DoubleScalar.Rel<LengthUnit> length,
            final DoubleScalar<FrequencyUnit> capacity, final LinearGeometry geometry,
            final OTSSimulatorInterface simulator)
    {
        super(id, startNode, endNode, length, capacity, geometry);
        addAnimation(simulator);
    }

    /**
     * @param id id
     * @param startNode sn
     * @param endNode en
     * @param length l
     * @param geometry g
     * @param simulator simulator
     */
    public Link(final String id, final Node startNode, final Node endNode, final DoubleScalar.Rel<LengthUnit> length,
            final LinearGeometry geometry, final OTSSimulatorInterface simulator)
    {
        super(id, startNode, endNode, length, geometry);
        addAnimation(simulator);
    }

    /**
     * @param id id
     * @param startNode sn
     * @param endNode en
     * @param length l
     * @param simulator simulator
     */
    public Link(final String id, final Node startNode, final Node endNode, final DoubleScalar.Rel<LengthUnit> length,
            final OTSSimulatorInterface simulator)
    {
        super(id, startNode, endNode, length);
        addAnimation(simulator);
    }

    /**
     * @param simulator simulator
     */
    private void addAnimation(final OTSSimulatorInterface simulator)
    {
        if (simulator instanceof OTSAnimatorInterface)
        {
            try
            {
                new LinkAnimation(this, simulator, 0.1f);
            }
            catch (RemoteException | NamingException ex)
            {
                //
            }
        }
    }

}
