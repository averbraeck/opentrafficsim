package org.opentrafficsim.core.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.rmi.RemoteException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.mock.MockSimulator;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Test the StaticObject class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class StaticObjectTest implements EventListener
{
    /** ... */
    private static final long serialVersionUID = 1L;

    /** Last received event. */
    private Event lastEvent = null;

    /**
     * Constructor.
     */
    public StaticObjectTest()
    {
        //
    }

    /**
     * Test the StaticObject class.
     * @throws NetworkException if that happens, this test has failed
     */
    @Test
    public void staticObjectTest() throws NetworkException
    {
        String id = "id of static object";
        Polygon2d geometry =
                new Polygon2d(new Point2d[] {new Point2d(0, 0), new Point2d(1, 0), new Point2d(1, 1), new Point2d(0, 1)});
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
        StaticObject so = StaticObject.create(id, geometry, height);
        assertNull(this.lastEvent, "Constructor should not have fired an event");
        network.addObject(so);
        assertEquals(id, so.getId(), "id");
        assertEquals(id, so.getFullId(), "full id");
        assertEquals(geometry, so.getContour(), "contour");
        assertEquals(height, so.getHeight(), "height");
        assertEquals(new DirectedPoint2d(geometry.getBounds().midPoint(), 0.0), so.getLocation(), "location");
        // djutils PolyLine2d returns absolute bounds, StaticObject returns centered around (0, 0)
        // assertEquals("bounds", geometry.getBounds(), so.getBounds());
        assertTrue(so.toString().startsWith("StaticObject"), "toString returns something descriptive");
        so.init();
        assertNotNull(this.lastEvent, "adding so to network should have fired an event");
        assertEquals(so.getId(), this.lastEvent.getContent(), "Payload of event is the static object id");
        this.lastEvent = null;
        StaticObject so2 = StaticObject.create(id, geometry, height);
        assertEquals(id, so2.getId(), "id");
        assertEquals(geometry, so2.getContour(), "contour");
        assertEquals(height, so2.getHeight(), "height");
        assertNull(this.lastEvent, "init should not have fired an event because there are no listeners");
        so2 = StaticObject.create(id, geometry);
        assertEquals(id, so2.getId(), "id");
        assertEquals(geometry, so2.getContour(), "contour");
        assertEquals(Length.ZERO, so2.getHeight(), "height");
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
