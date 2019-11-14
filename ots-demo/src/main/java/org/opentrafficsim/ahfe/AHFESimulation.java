package org.opentrafficsim.ahfe;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;

import org.djunits.unit.FrequencyUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.AbstractOTSSimulationApplication;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.kpi.interfaces.LaneDataInterface;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.sampling.GtuData;
import org.opentrafficsim.road.network.sampling.LinkData;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.road.network.sampling.data.TimeToCollision;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Simulation for AHFE congress.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Feb 28, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class AHFESimulation extends AbstractOTSSimulationApplication
{
    /** Warm-up time. */
    static final Time WARMUP = Time.instantiateSI(360);

    /** Simulation time, including warm-up time. */
    static final Time SIMEND = Time.instantiateSI(360 + 3600);

    /** Distance to not consider at start of the network. */
    private static Length ignoreStart = Length.instantiateSI(2900); // Not 100m on pre-link, so 3000 total

    /** Distance to not consider at end of the network. */
    private static Length ignoreEnd = Length.instantiateSI(1000);

    /** */
    private static final long serialVersionUID = 20170228L;

    /**
     * @param model OTSModelInterface; the model
     */
    public AHFESimulation(final OTSModelInterface model)
    {
        super(model);
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments
     * @throws SimRuntimeException should never happen
     */
    @SuppressWarnings("checkstyle:methodlength")
    public static void main(final String[] args) throws SimRuntimeException
    {
        AbstractGTU.ALIGNED = false;
        long t1 = System.currentTimeMillis();
        boolean autorun = true;
        int replication = 1;
        String anticipationStrategy = "none";
        Duration reactionTime = Duration.instantiateSI(0.0);
        Duration anticipationTime = Duration.ZERO;
        double truckFraction = 0.05;
        double distanceError = 0.0; // 0.05;
        double speedError = 0.0; // 0.01;
        double accelerationError = 0.0; // 0.10;
        Frequency leftDemand = new Frequency(3500.0, FrequencyUnit.PER_HOUR);
        Frequency rightDemand = new Frequency(3200.0, FrequencyUnit.PER_HOUR);
        double leftFraction = 0.55;
        String scenario = "test";

        for (String arg : args)
        {
            int equalsPos = arg.indexOf("=");
            if (equalsPos >= 0)
            {
                // set something
                String key = arg.substring(0, equalsPos);
                String value = arg.substring(equalsPos + 1);
                if ("autorun".equalsIgnoreCase(key))
                {
                    if ("true".equalsIgnoreCase(value))
                    {
                        autorun = true;
                    }
                    else if ("false".equalsIgnoreCase(value))
                    {
                        autorun = false;
                    }
                    else
                    {
                        System.err.println("bad autorun value " + value + " (ignored)");
                    }
                }
                else if ("replication".equalsIgnoreCase(key))
                {
                    try
                    {
                        replication = Integer.parseInt(value);
                    }
                    catch (NumberFormatException nfe)
                    {
                        System.err.println("Ignoring unparsable replication number \"" + value + "\"");
                    }
                }
                else if ("anticipation".equalsIgnoreCase(key))
                {
                    if (value.equalsIgnoreCase("none") || value.equalsIgnoreCase("constant_speed")
                            || value.equalsIgnoreCase("constant_acceleration"))
                    {
                        anticipationStrategy = value;
                    }
                    else
                    {
                        System.err.println("Ignoring unparsable anticipation \"" + value + "\"");
                    }
                }
                else if ("reactiontime".equalsIgnoreCase(key))
                {
                    try
                    {
                        reactionTime = Duration.instantiateSI(java.lang.Double.parseDouble(value));
                    }
                    catch (NumberFormatException nfe)
                    {
                        System.err.println("Ignoring unparsable reaction time \"" + value + "\"");
                    }
                }
                else if ("anticipationtime".equalsIgnoreCase(key))
                {
                    try
                    {
                        anticipationTime = Duration.instantiateSI(java.lang.Double.parseDouble(value));
                    }
                    catch (NumberFormatException nfe)
                    {
                        System.err.println("Ignoring unparsable anticipation time \"" + value + "\"");
                    }
                }
                else if ("truckfraction".equalsIgnoreCase(key))
                {
                    try
                    {
                        truckFraction = java.lang.Double.parseDouble(value);
                        Throw.when(truckFraction < 0.0 || truckFraction > 1.0, IllegalArgumentException.class,
                                "Truck fraction must be between 0 and 1.");
                    }
                    catch (NumberFormatException nfe)
                    {
                        System.err.println("Ignoring unparsable truck fraction \"" + value + "\"");
                    }
                }
                else if ("distanceerror".equalsIgnoreCase(key))
                {
                    try
                    {
                        distanceError = java.lang.Double.parseDouble(value);
                    }
                    catch (NumberFormatException nfe)
                    {
                        System.err.println("Ignoring unparsable distance error \"" + value + "\"");
                    }
                }
                else if ("speederror".equalsIgnoreCase(key))
                {
                    try
                    {
                        speedError = java.lang.Double.parseDouble(value);
                    }
                    catch (NumberFormatException nfe)
                    {
                        System.err.println("Ignoring unparsable speed error \"" + value + "\"");
                    }
                }
                else if ("accelerationerror".equalsIgnoreCase(key))
                {
                    try
                    {
                        accelerationError = java.lang.Double.parseDouble(value);
                    }
                    catch (NumberFormatException nfe)
                    {
                        System.err.println("Ignoring unparsable acceleration error \"" + value + "\"");
                    }
                }
                else if ("leftdemand".equalsIgnoreCase(key))
                {
                    try
                    {
                        leftDemand = new Frequency(java.lang.Double.parseDouble(value), FrequencyUnit.PER_HOUR);
                    }
                    catch (NumberFormatException nfe)
                    {
                        System.err.println("Ignoring unparsable left demand \"" + value + "\"");
                    }
                }
                else if ("rightdemand".equalsIgnoreCase(key))
                {
                    try
                    {
                        rightDemand = new Frequency(java.lang.Double.parseDouble(value), FrequencyUnit.PER_HOUR);
                    }
                    catch (NumberFormatException nfe)
                    {
                        System.err.println("Ignoring unparsable right demand \"" + value + "\"");
                    }
                }
                else if ("leftfraction".equalsIgnoreCase(key))
                {
                    try
                    {
                        leftFraction = java.lang.Double.parseDouble(value);
                    }
                    catch (NumberFormatException nfe)
                    {
                        System.err.println("Ignoring unparsable left fraction \"" + value + "\"");
                    }
                }
                else if ("scenario".equalsIgnoreCase(key))
                {
                    scenario = value;
                }
                else
                {
                    System.out.println("Ignoring unknown setting " + arg);
                }
            }
            else
            {
                // not a flag
                System.err.println("Ignoring argument " + arg);
            }
        }
        final boolean finalAutoRun = autorun;
        final int finalReplication = replication;
        final String finalAnticipationStrategy = anticipationStrategy;
        final Duration finalReactionTime = reactionTime;
        final Duration finalAnticipationTime = anticipationTime;
        final double finalTruckFraction = truckFraction;
        final double finalDistanceError = distanceError;
        final double finalSpeedError = speedError;
        final double finalAccelerationError = accelerationError;
        final Frequency finalLeftDemand = leftDemand;
        final Frequency finalRightDemand = rightDemand;
        final double finalLeftFraction = leftFraction;
        final String finalScenario = scenario;
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    OTSSimulator simulator = new OTSSimulator();
                    final AHFEModel ahfeModel = new AHFEModel(simulator, finalReplication, finalAnticipationStrategy,
                            finalReactionTime, finalAnticipationTime, finalTruckFraction, finalDistanceError, finalSpeedError,
                            finalAccelerationError, finalLeftDemand, finalRightDemand, finalLeftFraction);
                    System.out.println("Setting up replication " + finalReplication);
                    simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(SIMEND.si), ahfeModel, finalReplication);
                    new AHFESimulation(ahfeModel);
                    // 1 hour simulation run for testing
                    if (finalAutoRun)
                    {
                        int lastReportedTime = -60;
                        int reportTimeClick = 60;
                        while (true)
                        {
                            int currentTime = (int) simulator.getSimulatorTime().si;
                            if (currentTime >= lastReportedTime + reportTimeClick)
                            {
                                lastReportedTime = currentTime / reportTimeClick * reportTimeClick;
                                System.out.println("time is " + simulator.getSimulatorTime());
                            }
                            try
                            {
                                simulator.step();
                            }
                            catch (SimRuntimeException sre)
                            {
                                if (sre.getCause() != null && sre.getCause().getCause() != null
                                        && sre.getCause().getCause().getMessage().equals(
                                                "Model has calcalated a negative infinite or negative max value acceleration."))
                                {
                                    System.err.println("Collision detected.");
                                    String file = finalScenario + ".csv.zip";
                                    FileOutputStream fos = null;
                                    ZipOutputStream zos = null;
                                    OutputStreamWriter osw = null;
                                    BufferedWriter bw = null;
                                    try
                                    {
                                        fos = new FileOutputStream(file);
                                        zos = new ZipOutputStream(fos);
                                        zos.putNextEntry(new ZipEntry(finalScenario + ".csv"));
                                        osw = new OutputStreamWriter(zos);
                                        bw = new BufferedWriter(osw);
                                        bw.write("Collision");
                                        bw.write(simulator.getSimulatorTime().toString());
                                    }
                                    catch (IOException exception2)
                                    {
                                        throw new RuntimeException("Could not write to file.", exception2);
                                    }
                                    // close file on fail
                                    finally
                                    {
                                        try
                                        {
                                            if (bw != null)
                                            {
                                                bw.close();
                                            }
                                            if (osw != null)
                                            {
                                                osw.close();
                                            }
                                            if (zos != null)
                                            {
                                                zos.close();
                                            }
                                            if (fos != null)
                                            {
                                                fos.close();
                                            }
                                        }
                                        catch (IOException ex)
                                        {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                                else
                                {
                                    System.out.println("Simulation ends; time is " + simulator.getSimulatorTime());
                                    if (ahfeModel.getSampler() != null)
                                    {
                                        ahfeModel.getSampler().writeToFile(finalScenario + ".csv");
                                    }
                                }
                                long t2 = System.currentTimeMillis();
                                System.out.println("Run took " + (t2 - t1) / 1000 + "s.");
                                System.exit(0);
                                break;
                            }
                        }

                    }
                }
                catch (SimRuntimeException | NamingException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /**
     * The AHFE simulation model.
     */
    static class AHFEModel extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20170228L;

        /** The network. */
        private OTSRoadNetwork network;

        /** Replication. */
        private final Integer replication;

        /** Anticipation strategy. */
        private final String anticipationStrategy;

        /** Reaction time. */
        private final Duration reactionTime;

        /** Future anticipation time. */
        private final Duration anticipationTime;

        /** Truck fraction. */
        private final double truckFraction;

        /** Distance error. */
        private final double distanceError;

        /** Speed error. */
        private final double speedError;

        /** Acceleration error. */
        private final double accelerationError;

        /** Left demand. */
        private final Frequency leftDemand;

        /** Right demand. */
        private final Frequency rightDemand;

        /** Left fraction, per road. */
        private final double leftFraction;

        /** Sampler. */
        private Sampler<GtuData> sampler;

        /**
         * @param simulator OTSSimulatorInterface; the simulator
         * @param replication Integer; replication
         * @param anticipationStrategy String; anticipation strategy
         * @param reactionTime Duration; reaction time
         * @param anticipationTime Duration; anticipation time
         * @param truckFraction double; truck fraction
         * @param distanceError double; distance error
         * @param speedError double; speed error
         * @param accelerationError double; acceleration error
         * @param leftFraction double; left demand
         * @param rightDemand Frequency; right demand
         * @param leftDemand Frequency; left fraction, per road
         */
        @SuppressWarnings("checkstyle:parameternumber")
        AHFEModel(final OTSSimulatorInterface simulator, final Integer replication, final String anticipationStrategy,
                final Duration reactionTime, final Duration anticipationTime, final double truckFraction,
                final double distanceError, final double speedError, final double accelerationError, final Frequency leftDemand,
                final Frequency rightDemand, final double leftFraction)
        {
            super(simulator);
            this.replication = replication;
            this.anticipationStrategy = anticipationStrategy;
            this.reactionTime = reactionTime;
            this.anticipationTime = anticipationTime;
            this.truckFraction = truckFraction;
            this.distanceError = distanceError;
            this.speedError = speedError;
            this.accelerationError = accelerationError;
            this.leftDemand = leftDemand;
            this.rightDemand = rightDemand;
            this.leftFraction = leftFraction;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public void constructModel() throws SimRuntimeException
        {
            this.sampler = new RoadSampler(this.simulator);
            this.sampler.registerExtendedDataType(new TimeToCollision());
            try
            {
                URL xmlURL = URLResource.getResource("/AHFE/Network.xml");
                this.network = new OTSRoadNetwork("AHFE", true);
                XmlNetworkLaneParser.build(xmlURL, this.network, getSimulator());

                // Space-time regions for sampler
                LinkData linkData = new LinkData((CrossSectionLink) this.network.getLink("LEFTIN"));
                registerLinkToSampler(linkData, ignoreStart, linkData.getLength());
                linkData = new LinkData((CrossSectionLink) this.network.getLink("RIGHTIN"));
                registerLinkToSampler(linkData, ignoreStart, linkData.getLength());
                linkData = new LinkData((CrossSectionLink) this.network.getLink("CONVERGE"));
                registerLinkToSampler(linkData, Length.ZERO, linkData.getLength());
                linkData = new LinkData((CrossSectionLink) this.network.getLink("WEAVING"));
                registerLinkToSampler(linkData, Length.ZERO, linkData.getLength());
                linkData = new LinkData((CrossSectionLink) this.network.getLink("END"));
                registerLinkToSampler(linkData, Length.ZERO, linkData.getLength().minus(ignoreEnd));

                // Generator
                AHFEUtil.createDemand(this.network, null, this.simulator, getReplication(), getAnticipationStrategy(),
                        getReactionTime(), getAnticipationTime(), getTruckFraction(), SIMEND, getLeftDemand(), getRightDemand(),
                        getLeftFraction(), getDistanceError(), getSpeedError(), getAccelerationError());

            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /**
         * Register a link to the sampler, so data is sampled there.
         * @param linkData LinkData; link data
         * @param startDistance Length; start distance on link
         * @param endDistance Length; end distance on link
         */
        private void registerLinkToSampler(final LinkData linkData, final Length startDistance, final Length endDistance)
        {
            for (LaneDataInterface laneData : linkData.getLaneDatas())
            {
                Length start = laneData.getLength().times(startDistance.si / linkData.getLength().si);
                Length end = laneData.getLength().times(endDistance.si / linkData.getLength().si);
                this.sampler.registerSpaceTimeRegion(new SpaceTimeRegion(
                        new KpiLaneDirection(laneData, KpiGtuDirectionality.DIR_PLUS), start, end, WARMUP, SIMEND));
            }
        }

        /** {@inheritDoc} */
        @Override
        public OTSRoadNetwork getNetwork()
        {
            return this.network;
        }

        /**
         * @return replication.
         */
        public Integer getReplication()
        {
            return this.replication;
        }

        /**
         * @return anticipationStrategy.
         */
        public String getAnticipationStrategy()
        {
            return this.anticipationStrategy;
        }

        /**
         * @return reactionTime.
         */
        public Duration getReactionTime()
        {
            return this.reactionTime;
        }

        /**
         * @return anticipationTime.
         */
        public Duration getAnticipationTime()
        {
            return this.anticipationTime;
        }

        /**
         * @return truckFraction.
         */
        public double getTruckFraction()
        {
            return this.truckFraction;
        }

        /**
         * @return distanceError.
         */
        public double getDistanceError()
        {
            return this.distanceError;
        }

        /**
         * @return speedError.
         */
        public double getSpeedError()
        {
            return this.speedError;
        }

        /**
         * @return accelerationError.
         */
        public double getAccelerationError()
        {
            return this.accelerationError;
        }

        /**
         * @return leftDemand.
         */
        public Frequency getLeftDemand()
        {
            return this.leftDemand;
        }

        /**
         * @return rightDemand.
         */
        public Frequency getRightDemand()
        {
            return this.rightDemand;
        }

        /**
         * @return leftFraction.
         */
        public double getLeftFraction()
        {
            return this.leftFraction;
        }

        /**
         * @return sampler
         */
        public final Sampler<GtuData> getSampler()
        {
            return this.sampler;
        }

    }
}
