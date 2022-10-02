package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.immutablecollections.ImmutableMap;
import org.djutils.immutablecollections.ImmutableSet;
import org.junit.Test;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.perception.PerceivableContext;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the AbstractGTU class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class GtuTest
{
    /** GTU that will be returned when the fake strategical planner is asked for the associated GTU with getGTU. */
    public Gtu gtuOfStrategicalPlanner = null;

    /** */
    private static final long serialVersionUID = 20151217L;

    /**
     * Test the constructor.
     * @throws GtuException should not happen uncaught; if it does the test has failed
     * @throws NetworkException should not happen uncaught; if it does the test has failed
     * @throws SimRuntimeException should not happen uncaught; if it does the test has failed
     * @throws NamingException should not happen uncaught; if it does the test has failed
     * @throws RemoteException should not happen uncaught; if it does the test has failed
     * @throws OTSGeometryException should not happen uncaught; if it does the test has failed
     */
    @Test
    public final void testAbstractGTU()
            throws GtuException, SimRuntimeException, NetworkException, NamingException, RemoteException, OTSGeometryException
    {
        TestGTU firstGTU = null;
        TestGTU lastGTU = null;
        OtsSimulatorInterface simulator = new OtsSimulator("testAbstractGTU");
        OTSNetwork perceivableContext = new OTSNetwork("network", true, simulator);
        GTUModel model = new GTUModel(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(9999, DurationUnit.SI), model);
        StrategicalPlanner strategicalPlanner = new StrategicalPlanner()
        {

            @Override
            public Node nextNode(final Node node, final Link previousLink, final GtuType gtuType) throws NetworkException
            {
                return null;
            }

            @Override
            public Node nextNode(final Link link, final GtuType gtuType) throws NetworkException
            {
                return null;
            }

            @Override
            public Link nextLink(final Node node, final Link previousLink, final GtuType gtuType) throws NetworkException
            {
                return null;
            }

            @Override
            public Link nextLink(final Link link, final GtuType gtuType) throws NetworkException
            {
                return null;
            }

            @Override
            public TacticalPlanner<?, ?> getTacticalPlanner()
            {
                return null;
            }

            @Override
            public TacticalPlanner<?, ?> getTacticalPlanner(final Time time)
            {
                return null;
            }

            @Override
            public Route getRoute()
            {
                return null;
            }

            @Override
            public Gtu getGtu()
            {
                return GtuTest.this.gtuOfStrategicalPlanner;
            }

            @Override
            public Node getOrigin()
            {
                return null;
            }

            @Override
            public Node getDestination()
            {
                return null;
            }

        };
        Parameters parameters = new ParameterSet();
        DirectedPoint initialLocation =
                new DirectedPoint(10, 20, 30, Math.toRadians(10), Math.toRadians(20), Math.toRadians(30));
        GtuType gtuType1 = new GtuType("gtu type 1", perceivableContext.getGtuType(GtuType.DEFAULTS.VEHICLE));
        GtuType gtuType2 = new GtuType("gtu type 2", perceivableContext.getGtuType(GtuType.DEFAULTS.VEHICLE));
        for (String id : new String[] {"id1", "id2"})
        {
            for (GtuType gtuType : new GtuType[] {gtuType1, gtuType2})
            {
                String gtuId = id + " " + gtuType.getId();
                TestGTU gtu = new TestGTU(gtuId, gtuType, simulator, perceivableContext);
                assertEquals("new GTU has correct id", gtuId, gtu.getId());
                assertEquals("new GTU has correct GtuType", gtuType, gtu.getGtuType());
                assertEquals("new GTU has correct reference position", RelativePosition.REFERENCE_POSITION, gtu.getReference());
                assertEquals("new GTU has correct simulator", simulator, gtu.getSimulator());
                assertEquals("new GTU has odometer value 0", 0, gtu.getOdometer().si, 0);
                assertTrue("new GTU is stored in the perceivable context", perceivableContext.getGTUs().contains(gtu));
                lastGTU = gtu;
                if (null == firstGTU)
                {
                    firstGTU = gtu;
                }
            }
        }
        assertFalse("first GTU and last GTU have different id", firstGTU.getId().equals(lastGTU.getId()));
        assertFalse("first GTU and last GTU have different GtuType", firstGTU.getGtuType().equals(lastGTU.getGtuType()));
        TestGTU gtu = new TestGTU("id3", gtuType1, simulator, perceivableContext);
        assertEquals("perceivable context now contains 5 GTUs", 5, perceivableContext.getGTUs().size());
        gtu.destroy();
        assertFalse("perceivable context no longer contains the destroyed GTU", perceivableContext.containsGTU(gtu));
        try
        {
            new TestGTU((String) null, gtuType1, simulator, perceivableContext);
            fail("null id should have thrown a GTUException");
        }
        catch (GtuException ge)
        {
            // Ignore expected exception
        }

        try
        {
            new TestGTU("IdOfGTU", null, simulator, perceivableContext);
            fail("null gtuType should have thrown a GTUException");
        }
        catch (GtuException ge)
        {
            // Ignore expected exception
        }

        try
        {
            new TestGTU("IdOfGTU", gtuType1, null, perceivableContext);
            fail("null simulator should have thrown a GTUException");
        }
        catch (GtuException ge)
        {
            // Ignore expected exception
        }

        try
        {
            new TestGTU("IdOfGTU", gtuType1, simulator, null);
            fail("null perceivableContext should have thrown a GTUException");
        }
        catch (GtuException ge)
        {
            // Ignore expected exception
        }

        IdGenerator idGenerator = new IdGenerator("baseName");
        assertTrue("The toString method returns something descriptive", idGenerator.toString().startsWith("IdGenerator"));
        int lastBeforeId = Integer.parseInt(idGenerator.nextId().substring(8));
        gtu = new TestGTU(idGenerator, gtuType1, simulator, perceivableContext);
        int firstAfterId = Integer.parseInt(idGenerator.nextId().substring(8));
        assertEquals("Id generator was called once in the constructor", 1 + 1, firstAfterId - lastBeforeId);
        try
        {
            new TestGTU((IdGenerator) null, gtuType1, simulator, perceivableContext);
            fail("null idGenerator should have thrown a GTUException");
        }
        catch (GtuException ge)
        {
            // Ignore expected exception
        }

        Speed initialSpeed = new Speed(10, SpeedUnit.KM_PER_HOUR);
        try
        {
            gtu.init(null, initialLocation, initialSpeed);
            fail("null strategicalPlanner should have thrown a GTUException");
        }
        catch (GtuException ge)
        {
            // Ignore expected exception
        }

        // FIXME should the next one not ge a GTUException?
        try
        {
            gtu.init(strategicalPlanner, null, initialSpeed);
            fail("null initialLocation should have thrown a NullPointerException");
        }
        catch (NullPointerException ne)
        {
            // Ignore expected exception
        }

        try
        {
            gtu.init(strategicalPlanner, new DirectedPoint(Double.NaN, 20, 30), initialSpeed);
            fail("null initialSpeed should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException ge)
        {
            // Ignore expected exception
        }

        try
        {
            gtu.init(strategicalPlanner, new DirectedPoint(10, Double.NaN, 30), initialSpeed);
            fail("null initialSpeed should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException ge)
        {
            // Ignore expected exception
        }

        try
        {
            gtu.init(strategicalPlanner, new DirectedPoint(10, 20, Double.NaN), initialSpeed);
            fail("null initialSpeed should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException ge)
        {
            // Ignore expected exception
        }

        try
        {
            gtu.init(strategicalPlanner, initialLocation, null);
            fail("null initialSpeed should have thrown a GTUException");
        }
        catch (GtuException ge)
        {
            // Ignore expected exception
        }

        // The null pointer returned by the strategical planner will cause a NullPointerException
        // FIXME should probably explicitly throw an exception for a misbehaving strategical planner
        try
        {
            gtu.init(strategicalPlanner, initialLocation, initialSpeed);
            fail("strategicalPlanner that returns a null pointer should have thrown a NullPointerException");
        }
        catch (NullPointerException ne)
        {
            // Ignore expected exception
        }

        this.gtuOfStrategicalPlanner = firstGTU;
        try
        {
            gtu.init(strategicalPlanner, initialLocation, initialSpeed);
            fail("wrong strategicalPlanner should have thrown a GTUException");
        }
        catch (GtuException ge)
        {
            // Ignore expected exception
        }

        this.gtuOfStrategicalPlanner = gtu;
        // FIXME should the AbstractGTU not complain more directly about a null returned by
        // strategicalPlanner.generateTacticalPlanner()?
        try
        {
            gtu.init(strategicalPlanner, initialLocation, initialSpeed);
            fail("init with fake strategical planner should have caused a GTUExeption in move");
        }
        catch (GtuException ne)
        {
            // Ignore expected exception
        }
    }

    /** */
    class GTUModel extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 1L;

        /**
         * @param simulator the simulator
         */
        GTUModel(final OtsSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public void constructModel() throws SimRuntimeException
        {
            // Not used
        }

        /** {@inheritDoc} */
        @Override
        public final OTSNetwork getNetwork()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "GTUModel";
        }
    }

    /** */
    class TestGTU extends AbstractGtu
    {
        /** */
        private static final long serialVersionUID = 20151111L;

        /**
         * @param id String; id of the new GTU
         * @param gtuType GtuType; type of the new GTU
         * @param simulator OTSSimulatorInterface; simulator that controls the new GTU
         * @param perceivableContext PerceivableContext; the perceivable context of the new GTU
         * @throws SimRuntimeException when something goes wrong in the scheduling of the first move event
         * @throws GtuException when something goes wrong during GTU instantiation
         */
        TestGTU(final String id, final GtuType gtuType, final OtsSimulatorInterface simulator,
                final PerceivableContext perceivableContext) throws SimRuntimeException, GtuException
        {
            super(id, gtuType, simulator, perceivableContext);
        }

        /**
         * @param idGenerator IdGenerator; id generator that will generate the id of the new GTU
         * @param gtuType GtuType; type of the new GTU
         * @param simulator OTSSimulatorInterface; simulator that controls the new GTU
         * @param perceivableContext PerceivableContext; the perceivable context of the new GTU
         * @throws SimRuntimeException when something goes wrong in the scheduling of the first move event
         * @throws GtuException when something goes wrong during GTU instantiation
         */
        TestGTU(final IdGenerator idGenerator, final GtuType gtuType, final OtsSimulatorInterface simulator,

                final PerceivableContext perceivableContext) throws SimRuntimeException, GtuException
        {
            super(idGenerator, gtuType, simulator, perceivableContext);
        }

        /** {@inheritDoc} */
        @Override
        public Length getLength()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Length getWidth()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Speed getMaximumSpeed()
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
        public RelativePosition getCenter()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public ImmutableMap<TYPE, RelativePosition> getRelativePositions()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Bounds getBounds()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public ImmutableSet<RelativePosition> getContourPoints()
        {
            return null;
        }
    }

}
