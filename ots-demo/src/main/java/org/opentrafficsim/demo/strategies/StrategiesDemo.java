package org.opentrafficsim.demo.strategies;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

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
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.cli.CliException;
import org.djutils.cli.CliUtil;
import org.djutils.draw.curve.Arc2d;
import org.djutils.draw.curve.OffsetCurve2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Try;
import org.opentrafficsim.animation.gtu.colorer.AccelerationGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.DesiredHeadwayGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.IncentiveGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.SocialPressureGtuColorer;
import org.opentrafficsim.animation.gtu.colorer.SpeedGtuColorer;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.definitions.DefaultsNl;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuCharacteristics;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.draw.colorer.FixedColorer;
import org.opentrafficsim.road.definitions.DefaultsRoadNl;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.LaneBookkeeping;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.gtu.tactical.lmrs.IncentiveSocioSpeed;
import org.opentrafficsim.road.gtu.tactical.lmrs.Lmrs;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory;
import org.opentrafficsim.road.gtu.tactical.lmrs.LmrsFactory.Setting;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.network.CrossSectionLink;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.LaneKeepingPolicy;
import org.opentrafficsim.road.network.LanePosition;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsAnimationPanel.DemoPanelPosition;
import org.opentrafficsim.swing.script.AbstractSimulationScript;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import picocli.CommandLine.Option;

/**
 * Demo of lane change strategies.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StrategiesDemo extends AbstractSimulationScript
{
    /** Factory. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalRoutePlanner> factory;

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
        setGtuColorers(List.of(new FixedColorer<>(Color.BLUE, "Blue"), new SpeedGtuColorer(), new AccelerationGtuColorer(),
                new SocialPressureGtuColorer(), new DesiredHeadwayGtuColorer(Duration.ofSI(0.5), Duration.ofSI(1.6)),
                new IncentiveGtuColorer(IncentiveSocioSpeed.class)));
        try
        {
            CliUtil.changeOptionDefault(this, "simulationTime", "3600000s");
        }
        catch (NoSuchFieldException | IllegalStateException | IllegalArgumentException | CliException exception)
        {
            throw new OtsRuntimeException(exception);
        }
    }

    /**
     * Main method.
     * @param args arguments
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
                + "and observe how traffic is affected." + "</html>"); // Detailed instructions are in the attached read-me.
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
                        Try.execute(() -> gtu.getParameters().setClaimedParameter(LmrsParameters.VGAIN, vGain, egoSlider),
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
                        Try.execute(() -> gtu.getParameters().setClaimedParameter(LmrsParameters.SOCIO, sigma, socioSlider),
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
            int n = getNetwork().getGTUs().size();
            int i = n <= 1 ? 0 : StrategiesDemo.this.stream.nextInt(0, n - 1);
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
                        Length up = Try.assign(() -> gtu1.getPosition(l, gtu1.getFront()), "");
                        LaneBasedGtu gtu2;
                        Length down;
                        if (i < l.numberOfGtus() - 1)
                        {
                            gtu2 = l.getGtu(i + 1);
                            down = Try.assign(() -> gtu2.getPosition(l, gtu2.getRear()), "");
                        }
                        else
                        {
                            Lane nextLane = l.nextLanes(DefaultsNl.VEHICLE).iterator().next();
                            if (nextLane.numberOfGtus() == 0)
                            {
                                continue;
                            }
                            gtu2 = nextLane.getGtu(0);
                            down = l.getLength().plus(Try.assign(() -> gtu2.getPosition(nextLane, gtu2.getRear()), ""));
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
                catch (NamingException | GtuException | NetworkException | SimRuntimeException exception)
                {
                    throw new OtsRuntimeException(exception);
                }
                this.nextGtuType = this.stream.nextDouble() < this.truckFraction ? DefaultsNl.TRUCK : DefaultsNl.CAR;
                this.queue.clear();
            }
        }
        getSimulator().scheduleEventRel(Duration.ofSI(0.5), () -> checkVehicleNumber());
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
         * @param label label
         * @param network network
         */
        @SuppressWarnings("synthetic-access")
        KmplcListener(final JLabel label, final RoadNetwork network)
        {
            this.label = label;
            this.network = network;
            StrategiesDemo.this.queue.add(0.0);
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void notify(final Event event)
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

    @Override
    protected RoadNetwork setupSimulation(final OtsSimulatorInterface sim) throws Exception
    {
        RoadNetwork network = new RoadNetwork("Strategies demo", getSimulator());

        GtuCharacteristics truck = Defaults.NL.apply(DefaultsNl.TRUCK, this.stream)
                .orElseThrow(() -> new GtuException("No characteristics for NL.TRUCK could be generated.")).get();
        GtuCharacteristics car = Defaults.NL.apply(DefaultsNl.CAR, this.stream)
                .orElseThrow(() -> new GtuException("No characteristics for NL.CAR could be generated.")).get();
        this.truckLength = truck.getLength();
        this.truckMid = truck.getLength().times(0.5).minus(truck.getFront());
        this.carLength = car.getLength();
        this.carMid = car.getLength().times(0.5).minus(car.getFront());

        double radius = 150;
        Speed speedLimit = new Speed(120.0, SpeedUnit.KM_PER_HOUR);
        Node nodeA = new Node(network, "A", new Point2d(-radius, 0), new Direction(270, DirectionUnit.EAST_DEGREE));
        Node nodeB = new Node(network, "B", new Point2d(radius, 0), new Direction(90, DirectionUnit.EAST_DEGREE));

        OffsetCurve2d half1 = new Arc2d(new DirectedPoint2d(radius, 0.0, Math.PI / 2), radius, true, Math.PI);
        List<Lane> lanes1 = new LaneFactory(network, nodeB, nodeA, DefaultsNl.FREEWAY, sim, LaneKeepingPolicy.KEEPRIGHT,
                DefaultsNl.VEHICLE, half1).leftToRight(0.0, Length.ofSI(3.5), DefaultsRoadNl.FREEWAY, speedLimit)
                        .addLanes(DefaultsRoadNl.DASHED).getLanes();
        OffsetCurve2d half2 = new Arc2d(new DirectedPoint2d(-radius, 0.0, -Math.PI / 2), radius, true, Math.PI);
        List<Lane> lanes2 = new LaneFactory(network, nodeA, nodeB, DefaultsNl.FREEWAY, sim, LaneKeepingPolicy.KEEPRIGHT,
                DefaultsNl.VEHICLE, half2).leftToRight(0.0, Length.ofSI(3.5), DefaultsRoadNl.FREEWAY, speedLimit)
                        .addLanes(DefaultsRoadNl.DASHED).getLanes();

        LmrsFactory<Lmrs> lmrsFactory = new LmrsFactory<>(Lmrs::new).set(Setting.SOCIO_TAILGATING, true)
                .set(Setting.SOCIO_LANE_CHANGE, true).set(Setting.SOCIO_SPEED, true)
                .set(Setting.INCENTIVE_STAY_RIGHT, true, DefaultsNl.TRUCK).setStream(this.stream);
        lmrsFactory.addParameter(DefaultsNl.CAR, LmrsParameters.SOCIO, 0.5);
        lmrsFactory.addParameter(DefaultsNl.TRUCK, LmrsParameters.SOCIO, 1.0);
        lmrsFactory.addParameter(DefaultsNl.CAR, LmrsParameters.VGAIN, new Speed(35.0, SpeedUnit.KM_PER_HOUR));
        lmrsFactory.addParameter(DefaultsNl.TRUCK, LmrsParameters.VGAIN, new Speed(50.0, SpeedUnit.KM_PER_HOUR));
        lmrsFactory.addParameter(ParameterTypes.TMAX, Duration.ofSI(1.6));
        lmrsFactory.addParameter(DefaultsNl.TRUCK, ParameterTypes.A, Acceleration.ofSI(0.4));
        lmrsFactory.addParameter(DefaultsNl.TRUCK, ParameterTypes.FSPEED, 1.0);
        this.factory = new LaneBasedStrategicalRoutePlannerFactory(lmrsFactory, lmrsFactory);

        for (int i = 0; i < lanes1.size(); i++)
        {
            Length pos = Length.ofSI(10.0);
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
        sim.scheduleEventNow(() -> checkVehicleNumber());

        return network;
    }

    /**
     * Creates a GTU.
     * @param lane lane
     * @param pos position
     * @param gtuType GTU type
     * @param initialSpeed initial speed
     * @param net network
     * @throws NamingException on exception
     * @throws GtuException on exception
     * @throws NetworkException on exception
     * @throws SimRuntimeException on exception
     */
    public void createGtu(final Lane lane, final Length pos, final GtuType gtuType, final Speed initialSpeed,
            final RoadNetwork net) throws NamingException, GtuException, NetworkException, SimRuntimeException
    {
        GtuCharacteristics gtuCharacteristics = Defaults.NL.apply(gtuType, this.stream)
                .orElseThrow(() -> new GtuException("No characteristics for GTU type " + gtuType + " could be generated."))
                .get();

        LaneBasedGtu gtu = new LaneBasedGtu("" + (++this.gtuIdNum), gtuType, gtuCharacteristics.getLength(),
                gtuCharacteristics.getWidth(), gtuCharacteristics.getMaximumSpeed(), gtuCharacteristics.getFront(), net);
        gtu.setMaximumAcceleration(gtuCharacteristics.getMaximumAcceleration());
        gtu.setMaximumDeceleration(gtuCharacteristics.getMaximumDeceleration());
        gtu.setNoLaneChangeDistance(Length.ofSI(50));
        gtu.setBookkeeping(LaneBookkeeping.INSTANT);

        // strategical planner
        LaneBasedStrategicalPlanner strategicalPlanner = this.factory.create(gtu, null, null, null);
        // this.factories.get(gtuType).create(gtu, null, null, null);

        // init
        gtu.init(strategicalPlanner, new LanePosition(lane, pos).getLocation(), initialSpeed);

        if (this.kmplcListener != null)
        {
            Try.execute(() -> gtu.addListener(this.kmplcListener, LaneBasedGtu.LANE_CHANGE_EVENT),
                    "Exception while adding lane change listener");
        }
    }

}
