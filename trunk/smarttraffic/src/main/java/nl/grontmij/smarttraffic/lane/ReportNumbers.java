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
import java.time.LocalDateTime;
import java.time.ZoneId;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.lane.AbstractLaneBasedGTU;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * @author p070518
 */
public class ReportNumbers {
	/** */
	private static final long serialVersionUID = 20141231L;


	/** simulator. */
	private final OTSDEVSSimulatorInterface simulator;

	/** the network. */
	private final Network network;

	/**
	 * @param simulator
	 */
	public ReportNumbers(final Network network,
			final OTSDEVSSimulatorInterface simulator,
			BufferedWriter outputFileReportNumbers) {
		this.simulator = simulator;
		this.network = network;
		try {
			simulator.scheduleEventNow(this, this, "report",
					new Object[] { outputFileReportNumbers });
		} catch (RemoteException | SimRuntimeException exception) {
			exception.printStackTrace();
		}
	}

	/** report number of cars in the model. */
	public void report(BufferedWriter outputFileReportNumbers) {
		try {
			Instant time = GTM.startTimeSimulation
					.plusMillis(1000 * this.simulator.getSimulatorTime().get()
							.longValue());
        	Double timeA = this.simulator.getSimulatorTime().get().getInUnit(org.opentrafficsim.core.unit.TimeUnit.SECOND);
			String ts = time.toString().replace('T', ' ').replaceFirst("Z", "");
			int nr = 0;
			for (Link link : this.network.getLinkMap().values()) {
				if (link instanceof CrossSectionLink) {
					for (Object cse : ((CrossSectionLink) link)
							.getCrossSectionElementList()) {
						if (cse instanceof Lane) {
							for (Object gtu : ((Lane) cse).getGtuList()) {
								if (gtu instanceof LaneBasedIndividualCar) {
									nr++;
								}
							}
						}
					}
				}
			}
			//outputFileReportNumbers.write(ts + "\t" + nr + "\n");
			outputFileReportNumbers.write(timeA + "\t" + nr + "\n");
			outputFileReportNumbers.flush();
			if (LocalDateTime.ofInstant(time, ZoneId.of("UTC")).getMinute() == 0) {
				System.out.println("#gtu " + ts + " = " + nr);
			}
			try {
				simulator.scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(1.0,
						TimeUnit.MINUTE), this, this, "report",
						new Object[] { outputFileReportNumbers });
			} catch (RemoteException | SimRuntimeException exception) {
				exception.printStackTrace();
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

}
