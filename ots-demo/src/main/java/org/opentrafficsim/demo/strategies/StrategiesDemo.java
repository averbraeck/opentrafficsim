package org.opentrafficsim.demo.strategies;

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
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.cli.CliException;
import org.djutils.cli.CliUtil;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Try;
import org.opentrafficsim.animation.colorer.DesiredHeadwayColorer;
import org.opentrafficsim.animation.colorer.FixedColor;
import org.opentrafficsim.animation.colorer.IncentiveColorer;
import org.opentrafficsim.animation.colorer.SocialPressureColorer;
import org.opentrafficsim.animation.gtu.colorer.AccelerationGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.SpeedGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.SwitchableGtuColorer;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.ContinuousArc;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuCharacteristics;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.parameters.ParameterFactoryByType;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIdm;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveKeep;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveRoute;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSocioSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeedWithCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveStayRight;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LmrsFactory;
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
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.Stripe.Type;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsAnimationPanel.DemoPanelPosition;
import org.opentrafficsim.swing.script.AbstractSimulationScript;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import picocli.CommandLine.Option;

/**
 * Demo of lane change strategies.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StrategiesDemo extends AbstractSimulationScript
{
    /** Factories. */
    private final Map<GtuType, LaneBasedStrategicalPlannerFactory<?>> factories = new LinkedHashMap<>();

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
    private GtuType nextGtuType;

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
        setGtuColorer(SwitchableGtuColorer.builder().addColorer(new FixedColor(Color.BLUE, "Blue"))
                .addColorer(new SpeedGtuColorer(new Speed(150, SpeedUnit.KM_PER_HOUR)))
                .addColorer(new AccelerationGtuColorer(Acceleration.instantiateSI(-6.0), Acceleration.instantiateSI(2)))
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
    protected void setupDemo(final OtsAnimationPanel animation, final RoadNetwork network)
    {
        // demo panel
        animation.createDemoPanel(DemoPanelPosition.RIGHT);
        animation.getDemoPanel().setBorder(new EmptyBorder(10, 10, 10, 10));
        animation.getDemoPanel().setLayout(new BoxLayout(animation.getDemoPanel(), BoxLayout.Y_AXIS));
        animation.getDemoPanel().setPreferredSize(new Dimension(300, 300));

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
                for (Gtu gtu : getNetwork().getGTUs())
                {
                    if (gtu.getType().isOfType(DefaultsNl.CAR))
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
                for (Gtu gtu : getNetwork().getGTUs())
                {
                    if (gtu.getType().isOfType(DefaultsNl.CAR))
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
        for (Gtu gtu : network.getGTUs())
        {
            Try.execute(() -> gtu.addListener(this.kmplcListener, LaneBasedGtu.LANE_CHANGE_EVENT),
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
            Iterator<Gtu> it = getNetwork().getGTUs().iterator();
            Gtu gtu = it.next();
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
                        LaneBasedGtu gtu1 = l.getGtu(i);
                        Length up = Try.assign(() -> gtu1.position(l, gtu1.getFront()), "");
                        LaneBasedGtu gtu2;
                        Length down;
                        if (i < l.numberOfGtus() - 1)
                        {
                            gtu2 = l.getGtu(i + 1);
                            down = Try.assign(() -> gtu2.position(l, gtu2.getRear()), "");
                        }
                        else
                        {
                            Lane nextLane = l.nextLanes(DefaultsNl.VEHICLE).iterator().next();
                            if (nextLane.numberOfGtus() == 0)
                            {
                                continue;
                            }
                            gtu2 = nextLane.getGtu(0);
                            down = l.getLength().plus(Try.assign(() -> gtu2.position(nextLane, gtu2.getRear()), ""));
                        }
                        Length tentativeGap = down.minus(up)
                                .minus(this.nextGtuType.isOfType(DefaultsNl.TRUCK) ? this.truckLength : this.carLength);
                        if (tentativeGap.gt(gap))
                        {
                            // check reasonable gap (0.3s)
                            Speed maxSpeed = Speed.max(gtu1.getSpeed(), gtu2.getSpeed());
                            if (maxSpeed.eq0() || tentativeGap.divide(maxSpeed).si * .5 > .3)
                            {
                                gap = tentativeGap;
                                initialSpeed = Speed.interpolate(gtu1.getSpeed(), gtu2.getSpeed(), 0.5);
                                pos = up.plus(tentativeGap.times(0.5))
                                        .minus(this.nextGtuType.isOfType(DefaultsNl.TRUCK) ? this.truckMid : this.carMid);
                                if (pos.gt(l.getLength()))
                                {
                                    pos = pos.minus(l.getLength());
                                    lane = l.nextLanes(DefaultsNl.VEHICLE).iterator().next();
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
                catch (NamingException | GtuException | NetworkException | SimRuntimeException | OtsGeometryException exception)
                {
                    throw new RuntimeException(exception);
                }
                this.nextGtuType = this.stream.nextDouble() < this.truckFraction ? DefaultsNl.TRUCK : DefaultsNl.CAR;
                this.queue.clear();
            }
        }
        Try.execute(
                () -> getSimulator().scheduleEventRel(Duration.instantiateSI(0.5), this, "checkVehicleNumber", new Object[] {}),
                "");
    }

    /** Lane change listener. */
    private class KmplcListener implements EventListener
    {
        /** Label to show statistic. */
        private final JLabel label;

        /** Network. */
        private final RoadNetwork network;

        /**
         * Constructor.
         * @param label JLabel; label
         * @param network RoadNetwork; network
         */
        @SuppressWarnings("synthetic-access")
        KmplcListener(final JLabel label, final RoadNetwork network)
        {
            this.label = label;
            this.network = network;
            StrategiesDemo.this.queue.add(0.0);
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public void notify(final Event event) throws RemoteException
        {
            if (event.getType().equals(LaneBasedGtu.LANE_CHANGE_EVENT))
            {
                double cumul = 0.0;
                for (Gtu gtu : this.network.getGTUs())
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
    protected RoadNetwork setupSimulation(final OtsSimulatorInterface sim) throws Exception
    {
        RoadNetwork network = new RoadNetwork("Strategies demo", getSimulator());

        GtuType.registerTemplateSupplier(DefaultsNl.TRUCK, Defaults.NL);
        GtuType.registerTemplateSupplier(DefaultsNl.CAR, Defaults.NL);
        GtuCharacteristics truck = GtuType.defaultCharacteristics(DefaultsNl.TRUCK, network, this.stream);
        GtuCharacteristics car = GtuType.defaultCharacteristics(DefaultsNl.CAR, network, this.stream);
        this.truckLength = truck.getLength();
        this.truckMid = truck.getLength().times(0.5).minus(truck.getFront());
        this.carLength = car.getLength();
        this.carMid = car.getLength().times(0.5).minus(car.getFront());

        double radius = 150;
        Speed speedLimit = new Speed(120.0, SpeedUnit.KM_PER_HOUR);
        Node nodeA = new Node(network, "A", new Point2d(-radius, 0), new Direction(270, DirectionUnit.EAST_DEGREE));
        Node nodeB = new Node(network, "B", new Point2d(radius, 0), new Direction(90, DirectionUnit.EAST_DEGREE));

        ContinuousArc half1 =
                new ContinuousArc(new OrientedPoint2d(radius, 0.0, Math.PI / 2), radius, true, Angle.instantiateSI(Math.PI));
        List<Lane> lanes1 = new LaneFactory(network, nodeB, nodeA, DefaultsNl.FREEWAY, sim, LaneKeepingPolicy.KEEPRIGHT,
                DefaultsNl.VEHICLE, half1).leftToRight(0.0, Length.instantiateSI(3.5), DefaultsRoadNl.FREEWAY, speedLimit)
                        .addLanes(Type.DASHED).getLanes();
        ContinuousArc half2 =
                new ContinuousArc(new OrientedPoint2d(-radius, 0.0, -Math.PI / 2), radius, true, Angle.instantiateSI(Math.PI));
        List<Lane> lanes2 = new LaneFactory(network, nodeA, nodeB, DefaultsNl.FREEWAY, sim, LaneKeepingPolicy.KEEPRIGHT,
                DefaultsNl.VEHICLE, half2).leftToRight(0.0, Length.instantiateSI(3.5), DefaultsRoadNl.FREEWAY, speedLimit)
                        .addLanes(Type.DASHED).getLanes();

        // Strategical factories
        PerceptionFactory perceptionFactory = new LmrsStrategiesPerceptionFactory();
        // random parameters
        ParameterFactoryByType parameterFactory = new ParameterFactoryByType();
        parameterFactory.addParameter(Tailgating.RHO, 0.0);
        parameterFactory.addParameter(DefaultsNl.CAR, LmrsParameters.SOCIO, 0.5);
        parameterFactory.addParameter(DefaultsNl.TRUCK, LmrsParameters.SOCIO, 1.0);
        parameterFactory.addParameter(DefaultsNl.CAR, LmrsParameters.VGAIN, new Speed(35.0, SpeedUnit.KM_PER_HOUR));
        parameterFactory.addParameter(DefaultsNl.TRUCK, LmrsParameters.VGAIN, new Speed(50.0, SpeedUnit.KM_PER_HOUR));
        parameterFactory.addParameter(ParameterTypes.TMAX, Duration.instantiateSI(1.6));
        parameterFactory.addParameter(DefaultsNl.CAR, ParameterTypes.FSPEED,
                new DistNormal(this.stream, 123.7 / 120.0, 12.0 / 120.0));
        parameterFactory.addParameter(DefaultsNl.TRUCK, ParameterTypes.A, Acceleration.instantiateSI(0.4));
        parameterFactory.addParameter(DefaultsNl.TRUCK, ParameterTypes.FSPEED, 1.0);
        for (GtuType gtuType : new GtuType[] {DefaultsNl.CAR, DefaultsNl.TRUCK})
        {
            // incentives
            Set<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();
            Set<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();
            Set<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();
            mandatoryIncentives.add(new IncentiveRoute());
            voluntaryIncentives.add(new IncentiveSpeedWithCourtesy());
            voluntaryIncentives.add(new IncentiveKeep());
            voluntaryIncentives.add(new IncentiveSocioSpeed());
            if (gtuType.equals(DefaultsNl.TRUCK))
            {
                voluntaryIncentives.add(new IncentiveStayRight());
            }
            // car-following factory
            CarFollowingModelFactory<?> cfFactory = // trucks don't change their desired speed
                    gtuType.equals(DefaultsNl.CAR) ? new SocioIDMFactory() : new IdmPlusFactory(this.stream);
            // tailgating
            Tailgating tlgt = Tailgating.PRESSURE;
            // strategical and tactical factory
            LaneBasedStrategicalPlannerFactory<?> laneBasedStrategicalPlannerFactory =
                    new LaneBasedStrategicalRoutePlannerFactory(new LmrsFactory(cfFactory, perceptionFactory,
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
                    GtuType gtuType;
                    if (i == 0)
                    {
                        gtuType = DefaultsNl.CAR;
                    }
                    else
                    {
                        gtuType = this.stream.nextDouble() < 2 * this.truckFraction ? DefaultsNl.TRUCK : DefaultsNl.CAR;
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

        this.nextGtuType = this.stream.nextDouble() < this.truckFraction ? DefaultsNl.TRUCK : DefaultsNl.CAR;
        sim.scheduleEventNow(this, "checkVehicleNumber", new Object[] {});

        return network;
    }

    /**
     * Creates a GTU.
     * @param lane Lane; lane
     * @param pos Length; position
     * @param gtuType GtuType; GTU type
     * @param initialSpeed Speed; initial speed
     * @param net RoadNetwork; network
     * @throws NamingException on exception
     * @throws GtuException on exception
     * @throws NetworkException on exception
     * @throws SimRuntimeException on exception
     * @throws OtsGeometryException on exception
     */
    public void createGtu(final Lane lane, final Length pos, final GtuType gtuType, final Speed initialSpeed,
            final RoadNetwork net)
            throws NamingException, GtuException, NetworkException, SimRuntimeException, OtsGeometryException
    {
        GtuCharacteristics gtuCharacteristics = Try.assign(() -> GtuType.defaultCharacteristics(gtuType, net, this.stream),
                "Exception while applying default GTU characteristics.");

        LaneBasedGtu gtu = new LaneBasedGtu("" + (++this.gtuIdNum), gtuType, gtuCharacteristics.getLength(),
                gtuCharacteristics.getWidth(), gtuCharacteristics.getMaximumSpeed(), gtuCharacteristics.getFront(), net);
        gtu.setMaximumAcceleration(gtuCharacteristics.getMaximumAcceleration());
        gtu.setMaximumDeceleration(gtuCharacteristics.getMaximumDeceleration());
        gtu.setNoLaneChangeDistance(Length.instantiateSI(50));
        gtu.setInstantaneousLaneChange(true);

        // strategical planner
        LaneBasedStrategicalPlanner strategicalPlanner = this.factories.get(gtuType).create(gtu, null, null, null);

        // init
        gtu.init(strategicalPlanner, new LanePosition(lane, pos), initialSpeed);

        if (this.kmplcListener != null)
        {
            Try.execute(() -> gtu.addListener(this.kmplcListener, LaneBasedGtu.LANE_CHANGE_EVENT),
                    "Exception while adding lane change listener");
        }
    }

    /** IDM factory with socio speed. */
    class SocioIDMFactory implements CarFollowingModelFactory<IdmPlus>
    {
        /** {@inheritDoc} */
        @Override
        public Parameters getParameters() throws ParameterException
        {
            ParameterSet parameters = new ParameterSet();
            parameters.setDefaultParameters(AbstractIdm.class);
            return parameters;
        }

        /** {@inheritDoc} */
        @Override
        public IdmPlus generateCarFollowingModel()
        {
            return new IdmPlus(AbstractIdm.HEADWAY, new SocioDesiredSpeed(AbstractIdm.DESIRED_SPEED));
        }
    }

    /** Perception factory. */
    class LmrsStrategiesPerceptionFactory implements PerceptionFactory
    {
        /** {@inheritDoc} */
        @Override
        public LanePerception generatePerception(final LaneBasedGtu gtu)
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
