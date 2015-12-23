package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.Map;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.perception.PerceivableContext;
import org.opentrafficsim.simulationengine.SimpleSimulator;

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
public class GTUTest implements OTSModelInterface

{

    /** */
    private static final long serialVersionUID = 20151217L;

    /**
     * Test the constructor.
     * @throws GTUException
     * @throws NetworkException
     * @throws SimRuntimeException
     * @throws NamingException
     * @throws RemoteException
     */
    @Test
    public void testAbstractGTU() throws GTUException, SimRuntimeException, NetworkException, NamingException, RemoteException
    {
        TestGTU firstGTU = null;
        TestGTU lastGTU = null;
        OTSNetwork perceivableContext = new OTSNetwork("network");
        OTSDEVSSimulatorInterface simulator =
                new SimpleSimulator(new Time.Abs(0, TimeUnit.SI), new Time.Rel(0, TimeUnit.SI),
                        new Time.Rel(9999, TimeUnit.SI), this);
        for (String id : new String[] { "id1", "id2" })
        {
            for (GTUType gtuType : new GTUType[] { GTUType.makeGTUType("gtu type 1"), GTUType.makeGTUType("gtu type 2") })
            {
                TestGTU gtu = new TestGTU(id, gtuType, simulator, null, perceivableContext);
                assertEquals("new GTU has correct id", id, gtu.getId());
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
        DirectedPoint initialLocation =
                new DirectedPoint(10, 20, 30, Math.toRadians(10), Math.toRadians(20), Math.toRadians(30));
        TestGTU gtu = new TestGTU("id3", GTUType.makeGTUType("gtu type 1"), simulator, initialLocation, perceivableContext);
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
    public void constructModel(
            SimulatorInterface<Abs<TimeUnit>, org.djunits.value.vdouble.scalar.DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator)
            throws SimRuntimeException, RemoteException
    {
        // Not used
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, org.djunits.value.vdouble.scalar.DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
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
     * @param initialLocation DirectedPoint; initial location and direction of the new GTU
     * @param perceivableContext PerceivableContext; the perceivable context of the new GTU
     * @throws NetworkException when the network is inconsistent
     * @throws SimRuntimeException when something goes wrong in the scheduling of the first move event
     */
    public TestGTU(String id, GTUType gtuType, OTSDEVSSimulatorInterface simulator, DirectedPoint initialLocation,
            PerceivableContext perceivableContext) throws SimRuntimeException, NetworkException
    {
        super(id, gtuType, simulator, null, null, initialLocation, perceivableContext);
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
    public DrivingCharacteristics getDrivingCharacteristics()
    {
        return null;
    }

}
