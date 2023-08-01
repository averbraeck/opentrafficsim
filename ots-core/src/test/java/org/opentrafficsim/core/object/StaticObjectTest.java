package org.opentrafficsim.core.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.junit.Test;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.mock.MockSimulator;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Test the StaticObject class.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class StaticObjectTest implements EventListener
{
    /** ... */
    private static final long serialVersionUID = 1L;

    /** Last received event. */
    private Event lastEvent = null;

    /**
     * Test the StaticObject class.
     * @throws OtsGeometryException if that happens, this test has failed
     * @throws NetworkException if that happens, this test has failed
     */
    @Test
    public void staticObjectTest() throws OtsGeometryException, NetworkException
    {
        String id = "id of static object";
        PolyLine2d geometry =
                new PolyLine2d(new Point2d[] {new Point2d(0, 0), new Point2d(1, 0), new Point2d(1, 1), new Point2d(0, 1)});
        Length height = new Length(1, LengthUnit.METER);
        try
        {
            StaticObject.create(null, geometry, height);
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            StaticObject.create(id, null, height);
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            StaticObject.create(id, geometry, null);
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        this.lastEvent = null;
        Network network = new Network("Test network for static object test", MockSimulator.createMock());
        network.addListener(this, Network.OBJECT_ADD_EVENT);
        StaticObject so = new StaticObject(id, geometry, height);
        assertNull("Constructor should not have fired an event", this.lastEvent);
        network.addObject(so);
        assertEquals("id", id, so.getId());
        assertEquals("full id", id, so.getFullId());
        assertEquals("geometry", geometry, so.getGeometry());
        assertEquals("height", height, so.getHeight());
        assertEquals("location", geometry.getBounds().midPoint(), so.getLocation());
        // djutils PolyLine2d returns absolute bounds, StaticObject returns centered around (0, 0)
        //assertEquals("bounds", geometry.getBounds(), so.getBounds());
        assertTrue("toString returns something descriptive", so.toString().startsWith("StaticObject"));
        so.init();
        assertNotNull("adding so to network should have fired an event", this.lastEvent);
        assertEquals("Payload of event is the static object id", so.getId(), this.lastEvent.getContent());
        this.lastEvent = null;
        StaticObject so2 = StaticObject.create(id, geometry, height);
        assertEquals("id", id, so2.getId());
        assertEquals("geometry", geometry, so2.getGeometry());
        assertEquals("height", height, so2.getHeight());
        assertNull("init should not have fired an event because there are no listeners", this.lastEvent);
        so2 = StaticObject.create(id, geometry);
        assertEquals("id", id, so2.getId());
        assertEquals("geometry", geometry, so2.getGeometry());
        assertEquals("height", Length.ZERO, so2.getHeight());
    }

    /**
     * Receiver for the events that should be emitted when a StaticObject is constructed.
     */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        this.lastEvent = event;
    }

}
