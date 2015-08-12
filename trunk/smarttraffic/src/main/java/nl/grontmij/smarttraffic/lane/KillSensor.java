/**
 * 
 */
package nl.grontmij.smarttraffic.lane;

import java.util.HashMap;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * @author p070518
 */
public class KillSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20141231L;

    private HashMap<DoubleScalar.Abs<TimeUnit>, Integer> statusByTime = new HashMap<DoubleScalar.Abs<TimeUnit>, Integer>();

    /**
     * @param lane
     * @param longitudinalPositionFromEnd
     * @param nameSensor
     * @param nameJunction
     */
    /*
     * public SensorLaneST(Lane<?, ?> lane, Rel<LengthUnit> longitudinalPositionFromEnd, final RelativePosition.TYPE front,
     * String sensorType, String nameSensor, String nameJunction) { super(lane, longitudinalPositionFromEnd, front, nameSensor);
     * this.nameJunction = nameJunction; this.sensorType = sensorType; }
     */

    /**
     * @param lane
     * @param longitudinalPositionFromEnd
     * @param nameSensor
     * @param nameJunction
     */
    public KillSensor(Lane<?, ?> lane, Rel<LengthUnit> longitudinalPositionFromEnd, final RelativePosition.TYPE front,
        String nameSensor, final OTSSimulatorInterface simulator)
    {
        super(lane, longitudinalPositionFromEnd, front, nameSensor, simulator);
    }

    public HashMap<DoubleScalar.Abs<TimeUnit>, Integer> getStatusByTime()
    {
        return statusByTime;
    }

    public void setStatusByTime(HashMap<DoubleScalar.Abs<TimeUnit>, Integer> statusByTime)
    {
        this.statusByTime = statusByTime;
    }

    public void addStatusByTime(DoubleScalar.Abs<TimeUnit> timeNow, Integer status)
    {
        this.statusByTime.put(timeNow, status);
    }

    /**
     * {@inheritDoc} <br>
     * For this method, we assume that the right sensor triggered this method. In this case the sensor that indicates the front
     * of the GTU. The code triggering the sensor therefore has to do the checking for sensor type.
     */
    @Override
    public void trigger(final LaneBasedGTU<?> gtu)
    {
        // no action needed
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GenerateSensor [getLane()=" + this.getLane() + ", getLongitudinalPosition()=" + this.getLongitudinalPosition()
            + ", getPositionType()=" + this.getPositionType() + "]";
    }

}
