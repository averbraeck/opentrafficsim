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

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * @author p070518
 */
public class ReportNumbers
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** filename for sensor write. */
    private static BufferedWriter outputFile;

    /** simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /** the network. */
    private final Network<?, ?> network;

    static
    {
        try
        {
            String dirBase = System.getProperty("user.dir") + "/src/main/resources/";
            File file = new File(dirBase + "/reportNumbers.xls");
            if (!file.exists())
            {
                file.createNewFile();
            }
            outputFile = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
            outputFile.write("Time\tNrCars\n");
            outputFile.flush();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * @param simulator
     */
    public ReportNumbers(final Network<?, ?> network, final OTSDEVSSimulatorInterface simulator)
    {
        this.simulator = simulator;
        this.network = network;
        try
        {
            simulator.scheduleEventNow(this, this, "report", null);
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    /** report number of cars in the model. */
    public void report()
    {
        try
        {
            Instant time = GTM.startTimeSimulation.plusMillis(1000 * this.simulator.getSimulatorTime().get().longValue());
            String ts = time.toString().replace('T', ' ').replaceFirst("Z", "");
            int nr = 0;
            for (Link link : this.network.getLinkMap().values())
            {
                if (link instanceof CrossSectionLink)
                {
                    for (Object cse : ((CrossSectionLink) link).getCrossSectionElementList())
                    {
                        if (cse instanceof Lane)
                        {
                            for (Object gtu : ((Lane) cse).getGtuList())
                            {
                                if (gtu instanceof LaneBasedIndividualCar)
                                {
                                    nr++;
                                }
                            }
                        }
                    }
                }
            }
            outputFile.write(ts + "\t" + nr + "\n");
            outputFile.flush();
            System.out.println("#gtu " + ts + " = " + nr);
            try
            {
                simulator.scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(1.0, TimeUnit.MINUTE), this, this, "report", null);
            }
            catch (RemoteException | SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

}
