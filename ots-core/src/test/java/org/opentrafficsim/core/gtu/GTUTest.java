package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length.Abs;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.djunits.value.vdouble.scalar.Speed;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.core.network.route.CompleteRouteNavigator;
import org.opentrafficsim.core.network.route.RouteNavigator;

/**
 * Test the AbstractGTU class.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 11, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUTest

{

    /**
     * Test the constructor.
     * @throws GTUException
     */
    @Test
    public void testAbstractGTU() throws GTUException
    {
        TestGTU firstGTU = null;
        TestGTU lastGTU = null;
        for (String id : new String[] { "id1", "id2" })
        {
            for (GTUType gtuType : new GTUType[] { GTUType.makeGTUType("gtu type 1"), GTUType.makeGTUType("gtu type 2") })
            {
                CompleteRoute route1 = new CompleteRoute("Route 1", gtuType);
                CompleteRoute route2 = new CompleteRoute("Route 2", gtuType);
                for (RouteNavigator rn : new RouteNavigator[] { new CompleteRouteNavigator(route1),
                        new CompleteRouteNavigator(route2) })
                {
                    TestGTU gtu = new TestGTU(id, gtuType, rn);
                    assertEquals("new GTU has correct id", id, gtu.getId());
                    assertEquals("new GTU has correct GTUType", gtuType, gtu.getGTUType());
                    assertEquals("new GTU has correct route navigator", rn, gtu.getRouteNavigator());
                    assertEquals("new GTU has correct reference position", RelativePosition.REFERENCE_POSITION,
                            gtu.getReference());
                    CompleteRoute route3 = new CompleteRoute("Route 3", gtuType);
                    RouteNavigator rn3 = new CompleteRouteNavigator(route3);
                    gtu.setRouteNavigator(rn3);
                    assertEquals("new GTU now has another route navigator", rn3, gtu.getRouteNavigator());
                    gtu.setRouteNavigator(rn); // restore original route navigator
                    lastGTU = gtu;
                    if (null == firstGTU)
                    {
                        firstGTU = gtu;
                    }
                }
            }
        }
        assertFalse("first GTU and last GTU have different id", firstGTU.getId().equals(lastGTU.getId()));
        assertFalse("first GTU and last GTU have different GTUType", firstGTU.getGTUType().equals(lastGTU.getGTUType()));
        assertFalse("first GTU and last GTU have different RouteNavigator",
                firstGTU.getRouteNavigator().equals(lastGTU.getRouteNavigator()));
    }
}

/** ... */
class TestGTU extends AbstractGTU
{
    /** */
    private static final long serialVersionUID = 20151111L;

    /**
     * Construct a GTU.
     * @param id
     * @param gtuType
     * @param routeNavigator
     * @throws GTUException
     */
    TestGTU(final String id, final GTUType gtuType, final RouteNavigator routeNavigator) throws GTUException
    {
        super(id, gtuType, routeNavigator);
    }

    /** {@inheritDoc} */
    @Override
    public Rel getLength()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Rel getWidth()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Speed getMaximumVelocity()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public OTSDEVSSimulatorInterface getSimulator()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public RelativePosition getFront()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public RelativePosition getRear()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Speed getVelocity()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Map<TYPE, RelativePosition> getRelativePositions()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void destroy()
    {
        // Not used
    }

    /** {@inheritDoc} */
    @Override
    public Acceleration getAcceleration()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Abs getOdometer()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds()
    {
        return null;
    }
}
