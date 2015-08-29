/**
 * 
 */
package nl.grontmij.smarttraffic.lane;

import java.io.BufferedWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;

/**
 * @author p070518
 */
public class ReportNumbers
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** simulator. */
    private final OTSDEVSSimulatorInterface simulator;

    /** the network. */
    private final Network network;

    /**
     * @param simulator
     */
    public ReportNumbers(final Network network, final OTSDEVSSimulatorInterface simulator,
        BufferedWriter outputFileReportNumbers)
    {
        this.simulator = simulator;
        this.network = network;
        try
        {
            simulator.scheduleEventNow(this, this, "report", new Object[]{outputFileReportNumbers});
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    /** report actual number of cars in the model at a certain time. */
    public void report(BufferedWriter outputFileReportNumbers)
    {
        try
        {
            Instant time = GTM.startTimeSimulation.plusMillis(1000 * this.simulator.getSimulatorTime().get().longValue());
            Double timeA = this.simulator.getSimulatorTime().get().getInUnit(org.djunits.unit.TimeUnit.SECOND);
            timeA = GTM.startTimeSinceZero + timeA / 86400;
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
            if (Settings.getBoolean((OTSDEVSSimulatorInterface) this.simulator, "OUTPUTNUMERIC"))
            {
                outputFileReportNumbers.write(timeA + "\t" + nr + "\n");
            }
            else
            {
                outputFileReportNumbers.write(ts + "\t" + nr + "\n");
            }
            outputFileReportNumbers.flush();

            if (LocalDateTime.ofInstant(time, ZoneId.of("UTC")).getMinute() == 0)
            {
                System.out.println("#gtu " + ts + " = " + nr);
            }
            try
            {
                simulator.scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(1.0, TimeUnit.MINUTE), this, this, "report",
                    new Object[]{outputFileReportNumbers});
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

    // used in the trigger function: register cars, detectors and time of passing
    public static void reportPassingVehicles(BufferedWriter file, final LaneBasedGTU gtu, String nameSensor,
        OTSSimulatorInterface simulator)
    {

        try
        {
            String result;
            if (Settings.getBoolean((OTSDEVSSimulatorInterface) simulator, "OUTPUTNUMERIC"))
            {
                // in numbers
                Double time = simulator.getSimulatorTime().get().getInUnit(org.djunits.unit.TimeUnit.SECOND);
                // for MATLAB only: days since year 0
                time = GTM.startTimeSinceZero + time / 86400;
                String detector = null;
                if (nameSensor.charAt(3) == 'a')
                {
                    detector = nameSensor.substring(4, 5);
                }
                else if (nameSensor.charAt(3) == 'b')
                {
                    Double det = Double.parseDouble(nameSensor.substring(4, 5)) + 10;
                    detector = Double.toString(det);
                }
                result = time + "\t" + detector + "\t" + gtu.getId() + "\n";
            }
            else
            {
                // as String
                Instant time = GTM.startTimeSimulation.plusMillis(1000 * simulator.getSimulatorTime().get().longValue());
                String ts = time.toString().replace('T', ' ').replaceFirst("Z", "");
                result = ts + "\t" + nameSensor + "\t" + gtu.getId() + "\n";
            }
            if (file != null)
            {
                file.write(result);
                file.flush();
            }

        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

}
