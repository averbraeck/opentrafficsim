package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.core.gtu.perception.TimeStampedObject;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.perception.PerceivableContext;
import org.opentrafficsim.core.perception.PerceivedObject;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * Test the AbstractGTU class.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 11, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTUTest implements OTSModelInterface

{

    /** */
    private static final long serialVersionUID = 20151217L;

    /**
     * Test the constructor.
     * @throws GTUException should not happen uncaught; if it does the test has failed
     * @throws NetworkException should not happen uncaught; if it does the test has failed
     * @throws SimRuntimeException should not happen uncaught; if it does the test has failed
     * @throws NamingException should not happen uncaught; if it does the test has failed
     * @throws RemoteException should not happen uncaught; if it does the test has failed
     * @throws OTSGeometryException should not happen uncaught; if it does the test has failed
     */
    @Test
    public final void testAbstractGTU() throws GTUException, SimRuntimeException, NetworkException, NamingException,
            RemoteException, OTSGeometryException
    {
        TestGTU firstGTU = null;
        TestGTU lastGTU = null;
        Perception perception = new Perception()
        {

            /** */
            private static final long serialVersionUID = 20160311L;

            @Override
            public void perceive() throws GTUException, NetworkException
            {
                // Fake implementation; do nothing
            }

            @Override
            public Collection<PerceivedObject> getPerceivedObjects()
            {
                return new HashSet<PerceivedObject>();
            }

            /** {@inheritDoc} */
            @Override
            public TimeStampedObject<Collection<PerceivedObject>> getTimeStampedPerceivedObjects()
            {
                return new TimeStampedObject<Collection<PerceivedObject>>(new HashSet<PerceivedObject>(), Time.ZERO);
            }
        };
        OTSNetwork perceivableContext = new OTSNetwork("network");
        OTSDEVSSimulatorInterface simulator =
                new SimpleSimulator(new Time(0, TimeUnit.SI), new Duration(0, TimeUnit.SI),
                        new Duration(9999, TimeUnit.SI), this);
        StrategicalPlanner strategicalPlanner = new StrategicalPlanner()
        {

            @Override
            public Node nextNode(final Node node, final Link previousLink, final GTUType gtuType) throws NetworkException
            {
                return null;
            }

            @Override
            public Node nextNode(final Link link, final GTUDirectionality direction, final GTUType gtuType)
                    throws NetworkException
            {
                return null;
            }

            @Override
            public LinkDirection nextLinkDirection(final Node node, final Link previousLink, final GTUType gtuType)
                    throws NetworkException
            {
                return null;
            }

            @Override
            public LinkDirection nextLinkDirection(final Link link, final GTUDirectionality direction, final GTUType gtuType)
                    throws NetworkException
            {
                return null;
            }

            @Override
            public TacticalPlanner generateTacticalPlanner(final GTU gtu)
            {
                return null;
            }
        };
        DirectedPoint initialLocation =
                new DirectedPoint(10, 20, 30, Math.toRadians(10), Math.toRadians(20), Math.toRadians(30));
        GTUType gtuType1 = new GTUType("gtu type 1"); 
        GTUType gtuType2 = new GTUType("gtu type 2");
        for (String id : new String[] { "id1", "id2" })
        {
            for (GTUType gtuType : new GTUType[] { gtuType1, gtuType2 })
            {
                String gtuId = id + " " + gtuType.getId();
                TestGTU gtu =
                        new TestGTU(gtuId, gtuType, simulator, strategicalPlanner, perception, initialLocation,
                                perceivableContext);
                assertEquals("new GTU has correct id", gtuId, gtu.getId());
                assertEquals("new GTU has correct GTUType", gtuType, gtu.getGTUType());
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
        assertFalse("first GTU and last GTU have different GTUType", firstGTU.getGTUType().equals(lastGTU.getGTUType()));
        TestGTU gtu =
                new TestGTU("id3", gtuType1, simulator, strategicalPlanner, perception,
                        initialLocation, perceivableContext);
        DirectedPoint actualLocation = gtu.getLocation();
        assertEquals("initial location", 0, initialLocation.distance(actualLocation), 0.002);
        // Only the Z-rotation is returned non-zero; the returned X and Y rotation are zero
        assertEquals("initial rotation X", 0, actualLocation.getRotX(), 0.00001);
        assertEquals("initial rotation Y", 0, actualLocation.getRotY(), 0.00001);
        assertEquals("initial rotation Z", initialLocation.getRotZ(), actualLocation.getRotZ(), 0.00001);
        assertEquals("perceivable context now contains 5 GTUs", 5, perceivableContext.getGTUs().size());
        gtu.destroy();
        assertFalse("perceivable context no longer contains the destroyed GTU", perceivableContext.containsGTU(gtu));
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(final SimulatorInterface<Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator)
            throws SimRuntimeException, RemoteException
    {
        // Not used
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        return null;
    }
}

/** ... */
class TestGTU extends AbstractGTU
{
    /** */
    private static final long serialVersionUID = 20151111L;

    /**
     * @param id String; id of the new GTU
     * @param gtuType GTUType; type of the new GTU
     * @param simulator OTSDEVSSimulatorInterface; simulator that controls the new GTU
     * @param strategicalPlanner StrategicalPlanner; the strategical planner of the new GTU
     * @param perception Perception;
     * @param initialLocation DirectedPoint; initial location and direction of the new GTU
     * @param perceivableContext PerceivableContext; the perceivable context of the new GTU
     * @throws SimRuntimeException when something goes wrong in the scheduling of the first move event
     * @throws GTUException when something goes wrong during GTU instantiation
     */
    TestGTU(final String id, final GTUType gtuType, final OTSDEVSSimulatorInterface simulator,
            final StrategicalPlanner strategicalPlanner, final Perception perception, final DirectedPoint initialLocation,
            final PerceivableContext perceivableContext) throws SimRuntimeException, GTUException
    {
        super(id, gtuType, simulator, strategicalPlanner, perception, initialLocation, Speed.ZERO, perceivableContext);
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
    public Speed getMaximumVelocity()
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
    public Map<TYPE, RelativePosition> getRelativePositions()
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
    public BehavioralCharacteristics getBehavioralCharacteristics()
    {
        return null;
    }

}
