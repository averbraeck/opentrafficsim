/**
 * 
 */
package nl.grontmij.smarttraffic.lane;

import java.io.BufferedWriter;
import java.rmi.RemoteException;

import javax.naming.NamingException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.DOUBLE_SCALAR.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.Lane;

/**
 * @author p070518
 */
public class MeasureSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** filename for sensor write. */
    private BufferedWriter outputFile;

    /**
     * @param lane
     * @param position
     * @param nameSensor
     */
    public MeasureSensor(Lane lane, Length.Rel position, final RelativePosition.TYPE front, String nameSensor,
        final OTSDEVSSimulatorInterface simulator)
    {
        super(lane, position, front, nameSensor, simulator);
        try
        {
            new MeasureSensorAnimation(this, simulator);

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
        ReportNumbers.reportPassingVehicles(GTM.outputFileMeasures, gtu, this.getName(), this.getSimulator());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "MeasureSensor [getLane()=" + this.getLane() + ", getLongitudinalPosition()="
            + this.getLongitudinalPosition() + ", getPositionType()=" + this.getPositionType() + "]";
    }

}
