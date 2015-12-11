package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.value.vdouble.scalar.Length.Rel;
import org.djunits.value.vdouble.scalar.Speed;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.model.OTSModel;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;

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
     * @throws NetworkException
     * @throws SimRuntimeException
     */
    @Test
    public void testAbstractGTU() throws GTUException, SimRuntimeException, NetworkException
    {
        TestGTU firstGTU = null;
        TestGTU lastGTU = null;
        OTSNetwork model = new OTSNetwork("network");
        for (String id : new String[]{"id1", "id2"})
        {
            for (GTUType gtuType : new GTUType[]{GTUType.makeGTUType("gtu type 1"), GTUType.makeGTUType("gtu type 2")})
            {
                TestGTU gtu = new TestGTU(id, gtuType, null, model);
                assertEquals("new GTU has correct id", id, gtu.getId());
                assertEquals("new GTU has correct GTUType", gtuType, gtu.getGTUType());
                assertEquals("new GTU has correct reference position", RelativePosition.REFERENCE_POSITION, gtu
                    .getReference());
                lastGTU = gtu;
                if (null == firstGTU)
                {
                    firstGTU = gtu;
                }
            }
        }
        assertFalse("first GTU and last GTU have different id", firstGTU.getId().equals(lastGTU.getId()));
        assertFalse("first GTU and last GTU have different GTUType", firstGTU.getGTUType().equals(lastGTU.getGTUType()));

    }
}

/** ... */
class TestGTU extends AbstractGTU
{
    /** */
    private static final long serialVersionUID = 20151111L;

    /**
     * @param id
     * @param gtuType
     * @param simulator
     * @param model
     * @throws NetworkException
     * @throws SimRuntimeException
     */
    public TestGTU(String id, GTUType gtuType, OTSDEVSSimulatorInterface simulator, OTSModel model) throws SimRuntimeException,
        NetworkException
    {
        super(id, gtuType, simulator, null, null, null, model);
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
    public void destroy()
    {
        // Not used
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
