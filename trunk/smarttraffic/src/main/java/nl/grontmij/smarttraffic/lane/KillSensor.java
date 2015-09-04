/**
 * 
 */
package nl.grontmij.smarttraffic.lane;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.SinkSensor;

/**
 * @author p070518
 */
public class KillSensor extends SinkSensor
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /**
     * @param lane
     * @param longitudinalPositionFromEnd
     * @param nameSensor
     * @param nameJunction
     */
    public KillSensor(Lane lane, Length.Rel longitudinalPositionFromEnd, String nameSensor,
        final OTSDEVSSimulatorInterface simulator)
    {
        super(lane, longitudinalPositionFromEnd, simulator);
        try
        {
            new KillSensorAnimation(this, simulator);
        }
        catch (RemoteException | NamingException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * {@inheritDoc} <br>
     * For this method, we assume that the right sensor triggered this method. In this case the sensor that indicates the front
     * of the GTU. The code triggering the sensor therefore has to do the checking for sensor type.
     */
    @Override
    public void trigger(final LaneBasedGTU gtu)
    {
        GTM.listGTUsInNetwork.remove(gtu);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "KillSensor [getLane()=" + this.getLane() + ", getLongitudinalPosition()=" + this.getLongitudinalPosition()
            + ", getPositionType()=" + this.getPositionType() + "]";
    }

}
