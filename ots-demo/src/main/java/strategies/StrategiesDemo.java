package strategies;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.cli.CliException;
import org.djutils.cli.CliUtil;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.animation.gtu.colorer.AccelerationGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SpeedGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SwitchableGTUColorer;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUCharacteristics;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.road.gtu.colorer.DesiredHeadwayColorer;
import org.opentrafficsim.road.gtu.colorer.FixedColor;
import org.opentrafficsim.road.gtu.colorer.IncentiveColorer;
import org.opentrafficsim.road.gtu.colorer.SocialPressureColorer;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIDM;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveKeep;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveRoute;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSocioSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeedWithCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveStayRight;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.SocioDesiredSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.script.AbstractSimulationScript;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import picocli.CommandLine.Option;

/**
 * Demo of lane change strategies.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 jun. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class StrategiesDemo extends AbstractSimulationScript
{
    /** Factories. */
    private final Map<GTUType, LaneBasedStrategicalPlannerFactory<?>> factories = new LinkedHashMap<>();

    /** GTU id number. */
    private int gtuIdNum = 0;

    /** Number of GTUs. */
    private int gtuNum = 60;

    /** Random stream. */
    private final StreamInterface stream = new MersenneTwister(1L);

    /** Queue of cumulative odometers. */
    private final List<Double> queue = new ArrayList<>();

    /** Lane change listener. */
    private KmplcListener kmplcListener;

    /** Truck fraction. */
    private double truckFraction = 0.1;

    /** Next GTU type. */
    private GTUType nextGtuType;

    /** Truck length. */
    private Length truckLength;

    /** Truck mid. */
    private Length truckMid;

    /** Car length. */
    @Option(names = "--length", description = "Length", defaultValue = "2m")
    private Length carLength;

    /** Car mid. */
    private Length carMid;

    /**
     * Constructor.
     */
    protected StrategiesDemo()
    {
        super("Strategies demo", "Demo of driving strategies in LMRS.");
        setGtuColorer(SwitchableGTUColorer.builder().addColorer(new FixedColor(Color.BLUE, "Blue"))
                .addColorer(new SpeedGTUColorer(new Speed(150, SpeedUnit.KM_PER_HOUR)))
                .addColorer(new AccelerationGTUColorer(Acceleration.instantiateSI(-6.0), Acceleration.instantiateSI(2)))
                .addActiveColorer(new SocialPressureColorer())
                .addColorer(new DesiredHeadwayColorer(Duration.instantiateSI(0.5), Duration.instantiateSI(1.6)))
                .addColorer(new IncentiveColorer(IncentiveSocioSpeed.class)).build());
        try
        {
            CliUtil.changeOptionDefault(this, "simulationTime", "3600000s");
        }
        catch (NoSuchFieldException | IllegalStateException | IllegalArgumentException | CliException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Main method.
     * @param args String[]; arguments
     */
    public static void main(final String[] args)
    {
        StrategiesDemo demo = new StrategiesDemo();
        CliUtil.execute(demo, args);
        try
        {
            demo.start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void setupDemo(final OTSAnimationPanel animation, final OTSRoadNetwork network)
    {
        // text
        JLabel textLabel = new JLabel("<html><p align=\"justify\">"
                + "Adjust the sliders below to change the ego-speed sensitivity and socio-speed sensitivity of the drivers, "
                + "and observe how traffic is affected. Detailed instructions are in the attached read-me." + "</html>");
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        animation.getDemoPanel().add(textLabel);

        // spacer
        animation.getDemoPanel().add(Box.createVerticalStrut(20));

        // number of vehicles
        JLabel gtuLabel = new JLabel("<html>Number of vehicles</html>");
        gtuLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gtuLabel.setPreferredSize(new Dimension(200, 0));
        gtuLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        animation.getDemoPanel().add(gtuLabel);
        JSlider gtuSlider = new JSlider(0, 120, this.gtuNum);
        gtuSlider.setMinorTickSpacing(10);
        gtuSlider.setMajorTickSpacing(30);
        gtuSlider.setPaintTicks(true);
        gtuSlider.setPaintLabels(true);
        gtuSlider.setToolTipText("<html>Number of vehicles</html>");
        gtuSlider.addChangeListener(new ChangeListener()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void stateChanged(final ChangeEvent e)
            {
                StrategiesDemo.this.gtuNum = ((JSlider) e.getSource()).getValue();
                if (!StrategiesDemo.this.getSimulator().isStartingOrRunning())
                {
                    // StrategiesDemo.this.checkVehicleNumber();
                    animation.getDemoPanel().getParent().repaint();
                }
            }
        });
        animation.getDemoPanel().add(gtuSlider);

        // spacer
        animation.getDemoPanel().add(Box.createVerticalStrut(20));

        // ego
        JLabel egoLabel = new JLabel("<html>Ego-speed sensitivity<sup>-1</sup> [km/h]</html>");
        egoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        egoLabel.setPreferredSize(new Dimension(200, 0));
        egoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        animation.getDemoPanel().add(egoLabel);
        int egoSteps = 70;
        JSlider egoSlider = new JSlider(0, egoSteps, egoSteps / 2);
        egoSlider.setMinorTickSpacing(2);
        egoSlider.setMajorTickSpacing(egoSteps / 5);
        egoSlider.setPaintTicks(true);
        egoSlider.setPaintLabels(true);
        Hashtable<Integer, JLabel> egoTable = new Hashtable<>();
        for (int i = 0; i <= egoSteps; i += egoSteps / 5)
        {
            egoTable.put(i, new JLabel(String.format("%d", i)));
        }
        egoSlider.setLabelTable(egoTable);
        egoSlider.setToolTipText("<html>Ego-speed sensitivity as 1/<i>v<sub>gain</sub></i></html>");
        egoSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(final ChangeEvent e)
            {
                double v = Math.max(((JSlider) e.getSource()).getValue(), 0.01);
                Speed vGain = new Speed(v, SpeedUnit.KM_PER_HOUR);
                for (GTU gtu : getNetwork().getGTUs())
                {
                    if (gtu.getGTUType().isOfType(GTUType.DEFAULTS.CAR))
                    {
                        Try.execute(() -> gtu.getParameters().setParameter(LmrsParameters.VGAIN, vGain),
                                "Exception while setting vGain");
                    }
                }
            }
        });
        animation.getDemoPanel().add(egoSlider);

        // spacer
        animation.getDemoPanel().add(Box.createVerticalStrut(20));

        // socio
        JLabel socioLabel = new JLabel("Socio-speed sensitivity [-]");
        socioLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        animation.getDemoPanel().add(socioLabel);
        int socioSteps = 20;
        JSlider socioSlider = new JSlider(0, socioSteps, socioSteps / 2);
        socioSlider.setMinorTickSpacing(1);
        socioSlider.setMajorTickSpacing(socioSteps / 5);
        socioSlider.setPaintTicks(true);
        socioSlider.setPaintLabels(true);
        Hashtable<Integer, JLabel> socioTable = new Hashtable<>();
        for (int i = 0; i <= socioSteps; i += socioSteps / 5)
        {
            double val = (double) i / socioSteps;
            socioTable.put(i, new JLabel(String.format("%.2f", val)));
        }
        socioSlider.setLabelTable(socioTable);
        socioSlider.setToolTipText("Socio-speed sensitivity between 0 and 1");
        socioSlider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(final ChangeEvent e)
            {
                JSlider slider = (JSlider) e.getSource();
                double sigma = ((double) slider.getValue()) / slider.getMaximum();
                for (GTU gtu : getNetwork().getGTUs())
                {
                    if (gtu.getGTUType().isOfType(GTUType.DEFAULTS.CAR))
                    {
                        Try.execute(() -> gtu.getParameters().setParameter(LmrsParameters.SOCIO, sigma),
                                "Exception while setting vGain");
                    }
                }
            }
        });
        animation.getDemoPanel().add(socioSlider);

        // spacer
        animation.getDemoPanel().add(Box.createVerticalStrut(20));

        // km/lc
        JLabel kmplcLabel = new JLabel("Km between lane changes (last 0): -");
        this.kmplcListener = new KmplcListener(kmplcLabel, network);
        for (GTU gtu : network.getGTUs())
        {
            Try.execute(() -> gtu.addListener(this.kmplcListener, LaneBasedGTU.LANE_CHANGE_EVENT),
                    "Exception while adding lane change listener");
        }
        kmplcLabel.setHorizontalAlignment(SwingConstants.LEFT);
        kmplcLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        animation.getDemoPanel().add(kmplcLabel);
    }

    /**
     * Create or destroy vehicles.
     */
    @SuppressWarnings("unused")
    private void checkVehicleNumber()
    {
        while (getNetwork().getGTUs().size() > this.gtuNum)
        {
            int i = StrategiesDemo.this.stream.nextInt(0, getNetwork().getGTUs().size());
            Iterator<GTU> it = getNetwork().getGTUs().iterator();
            GTU gtu = it.next();
            for (int j = 0; j < i; j++)
            {
                gtu = it.next();
            }
            gtu.destroy();
            this.queue.clear();
        }
        // if: add one vehicle per cycle, so we limit the disturbance
        if (getNetwork().getGTUs().size() < this.gtuNum)
        {
            Lane lane = null;
            Length pos = null;
            Speed initialSpeed = null;
            Length gap = Length.ZERO;
            for (Link link : getNetwork().getLinkMap().values())
            {
                for (Lane l : ((CrossSectionLink) link).getLanes())
                {
                    if (l.numberOfGtus() == 0)
                    {
                        lane = l;
                        pos = lane.getLength().times(0.5);
                        gap = Length.POSITIVE_INFINITY;
                        initialSpeed = Speed.ZERO;
                    }
                    for (int i = 0; i < l.numberOfGtus(); i++)
                    {
                        LaneBasedGTU gtu1 = l.getGtu(i);
                        Length up = Try.assign(() -> gtu1.position(l, gtu1.getFront()), "");
                        LaneBasedGTU gtu2;
                        Length down;
                        if (i < l.numberOfGtus() - 1)
                        {
                            gtu2 = l.getGtu(i + 1);
                            down = Try.assign(() -> gtu2.position(l, gtu2.getRear()), "");
                        }
                        else
                        {
                            Lane nextLane =
                                    l.nextLanes(getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE)).keySet().iterator().next();
                            if (nextLane.numberOfGtus() == 0)
                            {
                                continue;
                            }
                            gtu2 = nextLane.getGtu(0);
                            down = l.getLength().plus(Try.assign(() -> gtu2.position(nextLane, gtu2.getRear()), ""));
                        }
                        Length tentativeGap =
                                down.minus(up).minus(this.nextGtuType.isOfType(getNetwork().getGtuType(GTUType.DEFAULTS.TRUCK))
                                        ? this.truckLength : this.carLength);
                        if (tentativeGap.gt(gap))
                        {
                            // check reasonable gap (0.3s)
                            Speed maxSpeed = Speed.max(gtu1.getSpeed(), gtu2.getSpeed());
                            if (maxSpeed.eq0() || tentativeGap.divide(maxSpeed).si * .5 > .3)
                            {
                                gap = tentativeGap;
                                initialSpeed = Speed.interpolate(gtu1.getSpeed(), gtu2.getSpeed(), 0.5);
                                pos = up.plus(tentativeGap.times(0.5))
                                        .minus(this.nextGtuType.isOfType(getNetwork().getGtuType(GTUType.DEFAULTS.TRUCK))
                                                ? this.truckMid : this.carMid);
                                if (pos.gt(l.getLength()))
                                {
                                    pos = pos.minus(l.getLength());
                                    lane = l.nextLanes(getNetwork().getGtuType(GTUType.DEFAULTS.VEHICLE)).keySet().iterator()
                                            .next();
                                }
                                else
                                {
                                    lane = l;
                                }
                            }
                        }
                    }
                }
            }
            if (lane != null)
            {
                try
                {
                    createGtu(lane, pos, this.nextGtuType, initialSpeed, getNetwork());
                }
                catch (NamingException | GTUException | NetworkException | SimRuntimeException | OTSGeometryException exception)
                {
                    throw new RuntimeException(exception);
                }
                this.nextGtuType = this.stream.nextDouble() < this.truckFraction
                        ? getNetwork().getGtuType(GTUType.DEFAULTS.TRUCK) : getNetwork().getGtuType(GTUType.DEFAULTS.CAR);
                this.queue.clear();
            }
        }
        Try.execute(() -> getSimulator().scheduleEventRel(Duration.instantiateSI(0.5), this, this, "checkVehicleNumber",
                new Object[] {}), "");
    }

    /** Lane change listener. */
    private class KmplcListener implements EventListenerInterface
    {
        /** Label to show statistic. */
        private final JLabel label;

        /** Network. */
        private final OTSRoadNetwork network;

        /**
         * Constructor.
         * @param label JLabel; label
         * @param network OTSRoadNetwork; network
         */
        @SuppressWarnings("synthetic-access")
        KmplcListener(final JLabel label, final OTSRoadNetwork network)
        {
            this.label = label;
            this.network = network;
            StrategiesDemo.this.queue.add(0.0);
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public void notify(final EventInterface event) throws RemoteException
        {
            if (event.getType().equals(LaneBasedGTU.LANE_CHANGE_EVENT))
            {
                double cumul = 0.0;
                for (GTU gtu : this.network.getGTUs())
                {
                    cumul += gtu.getOdometer().si;
                }
                cumul /= 1000;
                StrategiesDemo.this.queue.add(cumul);
                while (StrategiesDemo.this.queue.size() > 51)
                {
                    StrategiesDemo.this.queue.remove(0);
                }
                double val =
                        (StrategiesDemo.this.queue.get(StrategiesDemo.this.queue.size() - 1) - StrategiesDemo.this.queue.get(0))
                                / (StrategiesDemo.this.queue.size() - 1.0);
                this.label.setText(
                        String.format("Lane change rate (last %d): %.1f km/lc", StrategiesDemo.this.queue.size() - 1, val));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected OTSRoadNetwork setupSimulation(final OTSSimulatorInterface sim) throws Exception
    {
        OTSRoadNetwork network = new OTSRoadNetwork("Strategies demo", true, getSimulator());

        GTUCharacteristics truck =
                GTUType.defaultCharacteristics(network.getGtuType(GTUType.DEFAULTS.TRUCK), network, this.stream);
        GTUCharacteristics car = GTUType.defaultCharacteristics(network.getGtuType(GTUType.DEFAULTS.CAR), network, this.stream);
        this.truckLength = truck.getLength();
        this.truckMid = truck.getLength().times(0.5).minus(truck.getFront());
        this.carLength = car.getLength();
        this.carMid = car.getLength().times(0.5).minus(car.getFront());

        double radius = 150;
        Speed speedLimit = new Speed(120.0, SpeedUnit.KM_PER_HOUR);
        OTSRoadNode nodeA = new OTSRoadNode(network, "A", new OTSPoint3D(-radius, 0, 0), 
                new Direction(270, DirectionUnit.EAST_DEGREE));
        OTSRoadNode nodeB = new OTSRoadNode(network, "B", new OTSPoint3D(radius, 0, 0), 
                new Direction(90, DirectionUnit.EAST_DEGREE));

        OTSPoint3D[] coordsHalf1 = new OTSPoint3D[127];
        for (int i = 0; i < coordsHalf1.length; i++)
        {
            double angle = Math.PI * (i) / (coordsHalf1.length - 1);
            coordsHalf1[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle), 0);
        }
        List<Lane> lanes1 = new LaneFactory(network, nodeB, nodeA, network.getLinkType(LinkType.DEFAULTS.FREEWAY), sim,
                LaneKeepingPolicy.KEEPLEFT, new OTSLine3D(coordsHalf1))
                        .leftToRight(0.0, Length.instantiateSI(3.5), network.getLaneType(LaneType.DEFAULTS.FREEWAY), speedLimit)
                        .addLanes(Permeable.BOTH).getLanes();
        OTSPoint3D[] coordsHalf2 = new OTSPoint3D[127];
        for (int i = 0; i < coordsHalf2.length; i++)
        {
            double angle = Math.PI + Math.PI * (i) / (coordsHalf2.length - 1);
            coordsHalf2[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle), 0);
        }
        List<Lane> lanes2 = new LaneFactory(network, nodeA, nodeB, network.getLinkType(LinkType.DEFAULTS.FREEWAY), sim,
                LaneKeepingPolicy.KEEPLEFT, new OTSLine3D(coordsHalf2))
                        .leftToRight(0.0, Length.instantiateSI(3.5), network.getLaneType(LaneType.DEFAULTS.FREEWAY), speedLimit)
                        .addLanes(Permeable.BOTH).getLanes();

        // Strategical factories
        PerceptionFactory perceptionFactory = new LmrsStrategiesPerceptionFactory();
        // random parameters
        ParameterFactoryByType parameterFactory = new ParameterFactoryByType();
        parameterFactory.addParameter(Tailgating.RHO, 0.0);
        parameterFactory.addParameter(network.getGtuType(GTUType.DEFAULTS.CAR), LmrsParameters.SOCIO, 0.5);
        parameterFactory.addParameter(network.getGtuType(GTUType.DEFAULTS.TRUCK), LmrsParameters.SOCIO, 1.0);
        parameterFactory.addParameter(network.getGtuType(GTUType.DEFAULTS.CAR), LmrsParameters.VGAIN,
                new Speed(35.0, SpeedUnit.KM_PER_HOUR));
        parameterFactory.addParameter(network.getGtuType(GTUType.DEFAULTS.TRUCK), LmrsParameters.VGAIN,
                new Speed(50.0, SpeedUnit.KM_PER_HOUR));
        parameterFactory.addParameter(ParameterTypes.TMAX, Duration.instantiateSI(1.6));
        parameterFactory.addParameter(network.getGtuType(GTUType.DEFAULTS.CAR), ParameterTypes.FSPEED,
                new DistNormal(this.stream, 123.7 / 120.0, 12.0 / 120.0));
        parameterFactory.addParameter(network.getGtuType(GTUType.DEFAULTS.TRUCK), ParameterTypes.A, Acceleration.instantiateSI(0.4));
        parameterFactory.addParameter(network.getGtuType(GTUType.DEFAULTS.TRUCK), ParameterTypes.FSPEED, 1.0);
        for (GTUType gtuType : new GTUType[] {network.getGtuType(GTUType.DEFAULTS.CAR),
                network.getGtuType(GTUType.DEFAULTS.TRUCK)})
        {
            // incentives
            Set<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();
            Set<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();
            Set<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();
            mandatoryIncentives.add(new IncentiveRoute());
            voluntaryIncentives.add(new IncentiveSpeedWithCourtesy());
            voluntaryIncentives.add(new IncentiveKeep());
            voluntaryIncentives.add(new IncentiveSocioSpeed());
            if (gtuType.equals(network.getGtuType(GTUType.DEFAULTS.TRUCK)))
            {
                voluntaryIncentives.add(new IncentiveStayRight());
            }
            // car-following factory
            CarFollowingModelFactory<?> cfFactory = // trucks don't change their desired speed
                    gtuType.equals(network.getGtuType(GTUType.DEFAULTS.CAR)) ? new SocioIDMFactory()
                            : new IDMPlusFactory(this.stream);
            // tailgating
            Tailgating tlgt = Tailgating.PRESSURE;
            // strategical and tactical factory
            LaneBasedStrategicalPlannerFactory<?> laneBasedStrategicalPlannerFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(new LMRSFactory(cfFactory, perceptionFactory,
                            Synchronization.PASSIVE, Cooperation.PASSIVE, GapAcceptance.INFORMED, tlgt, mandatoryIncentives,
                            voluntaryIncentives, accelerationIncentives), parameterFactory);
            this.factories.put(gtuType, laneBasedStrategicalPlannerFactory);
        }
        for (int i = 0; i < lanes1.size(); i++)
        {
            Length pos = Length.instantiateSI(10.0);
            Length gap = lanes1.get(i).getLength().plus(lanes2.get(i).getLength()).divide(this.gtuNum / 2);
            for (int j = 0; j < 2; j++)
            {
                Lane lane = j == 0 ? lanes1.get(i) : lanes2.get(i);
                while (true)
                {
                    GTUType gtuType;
                    if (i == 0)
                    {
                        gtuType = network.getGtuType(GTUType.DEFAULTS.CAR);
                    }
                    else
                    {
                        gtuType = this.stream.nextDouble() < 2 * this.truckFraction ? network.getGtuType(GTUType.DEFAULTS.TRUCK)
                                : network.getGtuType(GTUType.DEFAULTS.CAR);
                    }

                    Speed initialSpeed = Speed.ZERO;

                    createGtu(lane, pos, gtuType, initialSpeed, network);

                    pos = pos.plus(gap);
                    if (pos.si > lane.getLength().si)
                    {
                        pos = pos.minus(lane.getLength());
                        break;
                    }
                }
            }
        }

        this.nextGtuType = this.stream.nextDouble() < this.truckFraction ? network.getGtuType(GTUType.DEFAULTS.TRUCK)
                : network.getGtuType(GTUType.DEFAULTS.CAR);
        sim.scheduleEventNow(this, this, "checkVehicleNumber", new Object[] {});

        return network;
    }

    /**
     * Creates a GTU.
     * @param lane Lane; lane
     * @param pos Length; position
     * @param gtuType GTUType; GTU type
     * @param initialSpeed Speed; initial speed
     * @param net OTSRoadNetwork; network
     * @throws NamingException on exception
     * @throws GTUException on exception
     * @throws NetworkException on exception
     * @throws SimRuntimeException on exception
     * @throws OTSGeometryException on exception
     */
    public void createGtu(final Lane lane, final Length pos, final GTUType gtuType, final Speed initialSpeed,
            final OTSRoadNetwork net)
            throws NamingException, GTUException, NetworkException, SimRuntimeException, OTSGeometryException
    {
        GTUCharacteristics gtuCharacteristics = Try.assign(() -> GTUType.defaultCharacteristics(gtuType, net, this.stream),
                "Exception while applying default GTU characteristics.");

        LaneBasedIndividualGTU gtu = new LaneBasedIndividualGTU("" + (++this.gtuIdNum), gtuType, gtuCharacteristics.getLength(),
                gtuCharacteristics.getWidth(), gtuCharacteristics.getMaximumSpeed(), gtuCharacteristics.getFront(),
                getSimulator(), net);
        gtu.setMaximumAcceleration(gtuCharacteristics.getMaximumAcceleration());
        gtu.setMaximumDeceleration(gtuCharacteristics.getMaximumDeceleration());
        gtu.setNoLaneChangeDistance(Length.instantiateSI(50));
        gtu.setInstantaneousLaneChange(true);

        // strategical planner
        LaneBasedStrategicalPlanner strategicalPlanner = this.factories.get(gtuType).create(gtu, null, null, null);

        // init
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        initialPositions.add(new DirectedLanePosition(lane, pos, GTUDirectionality.DIR_PLUS));
        if (pos.plus(gtu.getFront().getDx()).gt(lane.getLength()))
        {
            Lane nextLane = lane.nextLanes(gtuType).keySet().iterator().next();
            Length nextPos = pos.minus(lane.getLength());
            initialPositions.add(new DirectedLanePosition(nextLane, nextPos, GTUDirectionality.DIR_PLUS));
        }
        if (pos.plus(gtu.getRear().getDx()).lt0())
        {
            Lane prevLane = lane.prevLanes(gtuType).keySet().iterator().next();
            Length prevPos = prevLane.getLength().plus(pos.plus(gtu.getRear().getDx()));
            initialPositions.add(new DirectedLanePosition(prevLane, prevPos, GTUDirectionality.DIR_PLUS));
        }
        gtu.init(strategicalPlanner, initialPositions, initialSpeed);

        Try.execute(() -> gtu.addListener(this.kmplcListener, LaneBasedGTU.LANE_CHANGE_EVENT),
                "Exception while adding lane change listener");
    }

    /** IDM factory with socio speed. */
    class SocioIDMFactory implements CarFollowingModelFactory<IDMPlus>
    {
        /** {@inheritDoc} */
        @Override
        public Parameters getParameters() throws ParameterException
        {
            ParameterSet parameters = new ParameterSet();
            parameters.setDefaultParameters(AbstractIDM.class);
            return parameters;
        }

        /** {@inheritDoc} */
        @Override
        public IDMPlus generateCarFollowingModel()
        {
            return new IDMPlus(AbstractIDM.HEADWAY, new SocioDesiredSpeed(AbstractIDM.DESIRED_SPEED));
        }
    }

    /** Perception factory. */
    class LmrsStrategiesPerceptionFactory implements PerceptionFactory
    {
        /** {@inheritDoc} */
        @Override
        public LanePerception generatePerception(final LaneBasedGTU gtu)
        {
            LanePerception perception = new CategoricalLanePerception(gtu);
            perception.addPerceptionCategory(new DirectEgoPerception<>(perception));
            perception.addPerceptionCategory(new DirectInfrastructurePerception(perception));
            perception.addPerceptionCategory(new DirectNeighborsPerception(perception, HeadwayGtuType.WRAP));
            perception.addPerceptionCategory(new AnticipationTrafficPerception(perception));
            return perception;
        }

        /** {@inheritDoc} */
        @Override
        public Parameters getParameters() throws ParameterException
        {
            return new ParameterSet().setDefaultParameter(ParameterTypes.LOOKAHEAD)
                    .setDefaultParameter(ParameterTypes.LOOKBACKOLD).setDefaultParameter(ParameterTypes.PERCEPTION)
                    .setDefaultParameter(ParameterTypes.LOOKBACK);
        }
    }

}
