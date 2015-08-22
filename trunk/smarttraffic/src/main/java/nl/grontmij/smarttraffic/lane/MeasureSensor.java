/**
 * 
 */
package nl.grontmij.smarttraffic.lane;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.time.Instant;

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
public class MeasureSensor extends AbstractSensor
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** filename for sensor write. */
    private static BufferedWriter outputFile;
    
    static
    {
        try
        {
            String dirBase = System.getProperty("user.dir") + "/src/main/resources/";
            File file = new File(dirBase + "/measure.xls");
            if (!file.exists())
            {
                file.createNewFile();
            }
            outputFile = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
            outputFile.write("Time\tSensor\tCar\n");
            outputFile.flush();        }
        catch (IOException exception)
        {
            exception.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * @param lane
     * @param position
     * @param nameSensor
     */
    public MeasureSensor(Lane lane, Rel<LengthUnit> position, final RelativePosition.TYPE front, String nameSensor,
        final OTSSimulatorInterface simulator)
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
        try
        {
            Instant time = GTM.startTimeSimulation.plusMillis(1000 * getSimulator().getSimulatorTime().get().longValue());
            String ts = time.toString().replace('T', ' ').replaceFirst("Z", "");
            outputFile.write(ts + "\t" + getName() + "\t" + gtu.getId() + "\n");
            outputFile.flush();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "MeasureSensor [getLane()=" + this.getLane() + ", getLongitudinalPosition()="
            + this.getLongitudinalPosition() + ", getPositionType()=" + this.getPositionType() + "]";
    }

}
