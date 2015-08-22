/**
 * 
 */
package nl.grontmij.smarttraffic.lane;

import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * @author p070518
 */
public class KillSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /**
     * @param lane
     * @param longitudinalPositionFromEnd
     * @param nameSensor
     * @param nameJunction
     */
    public KillSensor(Lane lane, Rel<LengthUnit> longitudinalPositionFromEnd, final RelativePosition.TYPE front,
        String nameSensor, final OTSSimulatorInterface simulator)
    {
        super(lane, longitudinalPositionFromEnd, front, nameSensor, simulator);
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
        // no action needed
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "KillSensor [getLane()=" + this.getLane() + ", getLongitudinalPosition()=" + this.getLongitudinalPosition()
            + ", getPositionType()=" + this.getPositionType() + "]";
    }

}
