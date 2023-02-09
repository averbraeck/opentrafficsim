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
import org.junit.Test;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OtsNetwork;
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
     * @throws OtsGeometryException should not happen uncaught; if it does the test has failed
     */
    @Test
    public final void testAbstractGtu()
            throws GtuException, SimRuntimeException, NetworkException, NamingException, RemoteException, OtsGeometryException
    {
        TestGtu firstGTU = null;
        TestGtu lastGTU = null;
        OtsSimulatorInterface simulator = new OtsSimulator("testAbstractGTU");
        OtsNetwork perceivableContext = new OtsNetwork("network", simulator);
        GtuModel model = new GtuModel(simulator);
        simulator.initialize(Time.ZERO, Duration.ZERO, new Duration(9999, DurationUnit.SI), model);
        StrategicalPlanner strategicalPlanner = new StrategicalPlanner()
        {

            @Override
            public Node nextNode(final Link link, final GtuType gtuType) throws NetworkException
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

            @Override
            public Link nextLink(final Link previousLink, final GtuType gtuType) throws NetworkException
            {
                return null;
            }

        };
        Parameters parameters = new ParameterSet();
        DirectedPoint initialLocation =
                new DirectedPoint(10, 20, 30, Math.toRadians(10), Math.toRadians(20), Math.toRadians(30));
        GtuType gtuType1 = new GtuType("gtu type 1", DefaultsNl.VEHICLE);
        GtuType gtuType2 = new GtuType("gtu type 2", DefaultsNl.VEHICLE);
        for (String id : new String[] {"id1", "id2"})
        {
            for (GtuType gtuType : new GtuType[] {gtuType1, gtuType2})
            {
                String gtuId = id + " " + gtuType.getId();
                TestGtu gtu = new TestGtu(gtuId, gtuType, simulator, perceivableContext);
                assertEquals("new GTU has correct id", gtuId, gtu.getId());
                assertEquals("new GTU has correct GtuType", gtuType, gtu.getType());
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
        assertFalse("first GTU and last GTU have different GtuType", firstGTU.getType().equals(lastGTU.getType()));
        TestGtu gtu = new TestGtu("id3", gtuType1, simulator, perceivableContext);
        assertEquals("perceivable context now contains 5 GTUs", 5, perceivableContext.getGTUs().size());
        gtu.destroy();
        assertFalse("perceivable context no longer contains the destroyed GTU", perceivableContext.containsGTU(gtu));
        try
        {
            new TestGtu((String) null, gtuType1, simulator, perceivableContext);
            fail("null id should have thrown a GTUException");
        }
        catch (GtuException ge)
        {
            // Ignore expected exception
        }

        try
        {
            new TestGtu("IdOfGTU", null, simulator, perceivableContext);
            fail("null gtuType should have thrown a GTUException");
        }
        catch (GtuException ge)
        {
            // Ignore expected exception
        }

        try
        {
            new TestGtu("IdOfGTU", gtuType1, null, perceivableContext);
            fail("null simulator should have thrown a GTUException");
        }
        catch (GtuException ge)
        {
            // Ignore expected exception
        }

        try
        {
            new TestGtu("IdOfGTU", gtuType1, simulator, null);
            fail("null perceivableContext should have thrown a GTUException");
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
    class GtuModel extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 1L;

        /**
         * @param simulator the simulator
         */
        GtuModel(final OtsSimulatorInterface simulator)
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
        public final OtsNetwork getNetwork()
        {
            return null;
        }
    }

    /** */
    class TestGtu extends Gtu
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
        TestGtu(final String id, final GtuType gtuType, final OtsSimulatorInterface simulator,
                final PerceivableContext perceivableContext) throws SimRuntimeException, GtuException
        {
            super(id, gtuType, simulator, perceivableContext, Length.instantiateSI(4.0), Length.instantiateSI(1.8),
                    Speed.instantiateSI(50.0), Length.instantiateSI(2.0), Length.ZERO);
        }
    }

}
