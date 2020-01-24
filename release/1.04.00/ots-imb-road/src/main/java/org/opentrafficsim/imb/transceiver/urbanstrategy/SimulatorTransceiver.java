package org.opentrafficsim.imb.transceiver.urbanstrategy;

import java.io.Serializable;

import org.djutils.event.EventInterface;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.Connector;
import org.opentrafficsim.imb.transceiver.AbstractTransceiver;
import org.opentrafficsim.imb.transceiver.IMBMessageHandler;
import org.opentrafficsim.imb.transceiver.OTSToIMBTransformer;

import nl.tno.imb.TByteBuffer;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSRealTimeClock;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * The SimulatorTranseiver sends and listens to start, stop and change speed messages. The SimulatorTransceiver publishes the
 * following information on the IMB bus:
 * <ol>
 * <li>SimulatorInterface.START_EVENT when the simulator starts executing.</li>
 * <li>SimulatorInterface.STOP_EVENT when the simulator stops executing.</li>
 * <li>DEVSRealTimeClock.CHANGE_SPEED_FACTOR_EVENT when the simulator speed changes. Note that the simulation speed values are
 * limited to the following: {0.1, 0.2, 0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0, 200.0, 500.0, 1000.0,
 * Double.MAX_DOUBLE}</li>
 * </ol>
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 11, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SimulatorTransceiver extends AbstractTransceiver
{
    /** */
    private static final long serialVersionUID = 20160911L;

    /** the Empty transformer for IMB. */
    private final EmptyTransformer emptyTransformer = new EmptyTransformer();

    /** the simulation speed transformer for IMB. */
    private final SpeedTransformer speedTransformer = new SpeedTransformer();

    /**
     * Construct a new SimulatorTransceiver.
     * @param connector Connector; the IMB connector through which this transceiver communicates
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to schedule the incoming notifications on
     * @throws IMBException when the registration of one of the channels fails
     * @throws NullPointerException in case one of the arguments is null.
     */
    public SimulatorTransceiver(final Connector connector, final DEVSSimulatorInterface.TimeDoubleUnit simulator)
            throws IMBException
    {
        super("Simulator Control", connector, simulator);

        // register the OTS to IMB updates for the simulator
        final DEVSRealTimeClock.TimeDoubleUnit animator = (DEVSRealTimeClock.TimeDoubleUnit) simulator;
        addOTSToIMBChannel(animator, SimulatorInterface.START_EVENT, "Sim_Start", new Object[] {}, this.emptyTransformer);
        addOTSToIMBChannel(animator, SimulatorInterface.STOP_EVENT, "Sim_Stop", new Object[] {}, this.emptyTransformer);
        addOTSToIMBChannel(animator, DEVSRealTimeClock.CHANGE_SPEED_FACTOR_EVENT, "Sim_Speed", new Object[] {new Double(1.0)},
                this.speedTransformer);

        // register the IMB to OTS updates for the simulator
        final IMBMessageHandler startHandler = new IMBMessageHandler()
        {
            @Override
            public void handle(TByteBuffer imbPayload) throws IMBException
            {
                try
                {
                    if (!animator.isRunning()) // to break message cycle between OTS and IMB
                    {
                        animator.start(true);
                    }
                }
                catch (SimRuntimeException exception)
                {
                    throw new IMBException(exception);
                }
            }

            @Override
            public String getIMBEventName()
            {
                return "Sim_Start";
            }
        };
        addIMBtoOTSChannel("Sim_Start", startHandler);

        final IMBMessageHandler stopHandler = new IMBMessageHandler()
        {
            @Override
            public void handle(TByteBuffer imbPayload) throws IMBException
            {
                if (animator.isRunning()) // to break message cycle between OTS and IMB
                {
                    try
                    {
                        animator.stop(true);
                    }
                    catch (SimRuntimeException exception)
                    {
                        throw new IMBException(exception);
                    }
                }
            }

            @Override
            public String getIMBEventName()
            {
                return "Sim_Stop";
            }
        };
        addIMBtoOTSChannel("Sim_Stop", stopHandler);

        final IMBMessageHandler speedHandler = new IMBMessageHandler()
        {
            @Override
            public void handle(TByteBuffer imbPayload) throws IMBException
            {
                System.out.println("About to read the new speed value");
                double speed = imbPayload.readDouble();
                System.out.println(
                        "Sim_Stop event handler: new speed is " + speed + " current speed is " + animator.getSpeedFactor());
                if (speed != animator.getSpeedFactor()) // to break message cycle between OTS and IMB
                {
                    animator.setSpeedFactor(speed, true); // TODO callback for speed not 100% ok yet...
                }
            }

            @Override
            public String getIMBEventName()
            {
                return "Sim_Speed";
            }
        };
        addIMBtoOTSChannel("Sim_Speed", speedHandler);
    }

    /**
     * Transform the an event without content to a corresponding IMB message.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 11, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    static class EmptyTransformer implements OTSToIMBTransformer
    {
        /** {@inheritDoc} */
        @Override
        public Object[] transform(final EventInterface event)
        {
            return new Object[] {};
        }
    }

    /**
     * Transform the a simulation speed change event to a corresponding IMB message.<br>
     * Event is sent from OTS to IMB to indicate that the OTS simulation has changed the intended execution speed (there is,
     * however, no guarantee that the simulator is able to reach that particular speed). The speed is coded as a 64-bits double,
     * where 1.0 indicates exactly real-time; values less than 1.0 slower than real time, and larger than 1.0 indicates faster
     * than real-time. The simulation speed values are limited to the following: {0.1, 0.2, 0.5, 1.0, 2.0, 5.0, 10.0, 20.0,
     * 50.0, 100.0, 200.0, 500.0, 1000.0, Double.MAX_DOUBLE}.
     * <p>
     * <style>table,th,td {border:1px solid grey; border-style:solid; text-align:left; border-collapse: collapse;}</style>
     * <H2>NEW</H2> Empty message, no payload
     * </p>
     * <p>
     * <h2>CHANGE</h2>
     * <table style="width:800px;"><caption>&nbsp;</caption>
     * <thead>
     * <tr>
     * <th style="width:25%;">Variable</th>
     * <th style="width:15%;">Type</th>
     * <th style="width:60%;">Comments</th>
     * </tr>
     * </thead><tbody>
     * <tr>
     * <td>newSimulationSpeed</td>
     * <td>double</td>
     * <td>Limit to {1,2,5} E {-1, 0, 1, 2}, 1000, MAX_DOUBLE</td>
     * </tr>
     * </tbody>
     * </table>
     * </p>
     * <p>
     * <h2>DELETE</h2> Not sent
     * </p>
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
     * initial version Sep 11, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    static class SpeedTransformer implements OTSToIMBTransformer
    {
        /** {@inheritDoc} */
        @Override
        public Object[] transform(final EventInterface event)
        {
            Double speed = ((Double) event.getContent());
            System.out.println("Transmitting speed " + speed + " to IMB");
            return new Object[] {speed};
        }
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "SimulatorTransceiver";
    }
}
