package org.opentrafficsim.core.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.junit.Test;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.mock.MockSimulator;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OtsNetwork;

/**
 * Test the StaticObject class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class StaticObjectTest implements EventListenerInterface
{
    /** ... */
    private static final long serialVersionUID = 1L;

    /** Last received event. */
    private EventInterface lastEvent = null;

    /**
     * Test the StaticObject class.
     * @throws OtsGeometryException if that happens, this test has failed
     * @throws NetworkException if that happens, this test has failed
     */
    @Test
    public void staticObjectTest() throws OtsGeometryException, NetworkException
    {
        String id = "id of static object";
        OtsLine3D geometry = new OtsLine3D(new OtsPoint3D[] {new OtsPoint3D(0, 0, 0), new OtsPoint3D(1, 0, 0),
                new OtsPoint3D(1, 1, 0), new OtsPoint3D(0, 1, 0)});
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
        OtsNetwork network = new OtsNetwork("Test network for static object test", false, MockSimulator.createMock());
        network.addListener(this, Network.ANIMATION_OBJECT_ADD_EVENT);
        StaticObject so = new StaticObject(id, geometry, height);
        assertNull("Constructor should not have fired an event", this.lastEvent);
        network.addObject(so);
        assertEquals("id", id, so.getId());
        assertEquals("full id", id, so.getFullId());
        assertEquals("geometry", geometry, so.getGeometry());
        assertEquals("height", height, so.getHeight());
        assertEquals("location", geometry.getLocation(), so.getLocation());
        assertEquals("bounds", geometry.getBounds(), so.getBounds());
        assertTrue("toString returns something descriptive", so.toString().startsWith("StaticObject"));
        so.init();
        assertNotNull("adding so to network should have fired an event", this.lastEvent);
        assertEquals("Payload of event is the static object", so, this.lastEvent.getContent());
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
    public void notify(final EventInterface event) throws RemoteException
    {
        this.lastEvent = event;
    }

}
