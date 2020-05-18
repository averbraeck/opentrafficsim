package org.opentrafficsim.demo.fd;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.cli.CliUtil;
import org.djutils.exceptions.Try;
import org.djutils.means.HarmonicMean;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUErrorHandler;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.GTUType.DEFAULTS;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.DirectedLinkPosition;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.parameters.ParameterFactory;
import org.opentrafficsim.draw.graphs.FundamentalDiagram;
import org.opentrafficsim.draw.graphs.FundamentalDiagram.FdLine;
import org.opentrafficsim.draw.graphs.FundamentalDiagram.FdSource;
import org.opentrafficsim.draw.graphs.FundamentalDiagram.Quantity;
import org.opentrafficsim.draw.graphs.GraphCrossSection;
import org.opentrafficsim.draw.graphs.GraphPath;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;
import org.opentrafficsim.draw.graphs.road.GraphLaneUtil;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.road.gtu.generator.CFBARoomChecker;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBias;
import org.opentrafficsim.road.gtu.generator.GeneratorPositions.LaneBiases;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristics;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGTUCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlus;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRS;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.swing.graphs.SwingFundamentalDiagram;
import org.opentrafficsim.swing.graphs.SwingTrajectoryPlot;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSAnimationPanel.DemoPanelPosition;
import org.opentrafficsim.swing.script.AbstractSimulationScript;

import nl.tudelft.simulation.dsol.swing.gui.TablePanel;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Demo showing what fundamental diagrams are. This demo is for education purposes.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class FundamentalDiagramDemo extends AbstractSimulationScript
{

    /** */
    private static final long serialVersionUID = 20200509L;

    /** Dynamic demand. */
    private Frequency demand = new Frequency(3500.0, FrequencyUnit.PER_HOUR);

    /** Dynamic truck fraction. */
    private double truckFraction = 0.05;

    /** Speed limit. */
    private Speed speedLimit = new Speed(120.0, SpeedUnit.KM_PER_HOUR);

    /** Tmin. */
    private Duration tMin = Duration.instantiateSI(0.56);

    /** Tmax. */
    private Duration tMax = Duration.instantiateSI(1.2);

    /** Panel splitting controls from graphs. */
    private JPanel splitPanel;

    /** Panel with graphs. */
    private TablePanel graphPanel;

    /** Sampler. */
    private RoadSampler sampler;

    /** Selected cross-section. */
    private String absoluteCrossSection1 = "1.50";

    /** Second selected cross-section. */
    private String absoluteCrossSection2 = "None";

    /** Third selected cross-section. */
    private String absoluteCrossSection3 = "None";

    /** Fd line in graphs based on settings. */
    @SuppressWarnings("synthetic-access")
    private DynamicFdLine fdLine = new DynamicFdLine();

    /** Fundamental diagrams that are updated when a setting is changed. */
    private Set<FundamentalDiagram> funamentalDiagrams = new LinkedHashSet<>();

    /** Sources by name for each cross-section. */
    private Map<String, FdSource> fdSourceMap = new LinkedHashMap<>();

    /** Panel of trajectory graph. */
    private Container trajectoryPanel;

    /**
     * Constructor.
     */
    public FundamentalDiagramDemo()
    {
        super("FD Demo", "Fundamental diagram demo");
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        FundamentalDiagramDemo demo = new FundamentalDiagramDemo();
        try
        {
            CliUtil.changeOptionDefault(demo, "simulationTime", "360000s"); // 100h
            CliUtil.execute(demo, args);
            demo.start();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected OTSRoadNetwork setupSimulation(final OTSSimulatorInterface sim) throws Exception
    {
        // Network
        OTSRoadNetwork network = new OTSRoadNetwork("FD demo network", true, sim);
        GTUType car = network.getGtuType(DEFAULTS.CAR);
        GTUType truck = network.getGtuType(DEFAULTS.TRUCK);

        OTSRoadNode nodeA = new OTSRoadNode(network, "Origin", new OTSPoint3D(0.0, 0.0), Direction.ZERO);
        OTSRoadNode nodeB = new OTSRoadNode(network, "Lane-drop", new OTSPoint3D(1500.0, 0.0), Direction.ZERO);
        OTSRoadNode nodeC = new OTSRoadNode(network, "Destination", new OTSPoint3D(2500.0, 0.0), Direction.ZERO);

        LinkType linkType = network.getLinkType(LinkType.DEFAULTS.FREEWAY);
        LaneKeepingPolicy policy = LaneKeepingPolicy.KEEPRIGHT;
        Length laneWidth = Length.instantiateSI(3.5);
        LaneType laneType = network.getLaneType(LaneType.DEFAULTS.FREEWAY);
        Speed speedLim = new Speed(120.0, SpeedUnit.KM_PER_HOUR);

        List<Lane> lanesAB = new LaneFactory(network, nodeA, nodeB, linkType, sim, policy).leftToRight(3.0, laneWidth, laneType,
            speedLim).addLanes(Permeable.BOTH, Permeable.BOTH).getLanes();
        List<Lane> lanesBC = new LaneFactory(network, nodeB, nodeC, linkType, sim, policy).leftToRight(2.0, laneWidth, laneType,
            speedLim).addLanes(Permeable.BOTH).getLanes();

        // Generator
        // inter-arrival time generator
        StreamInterface stream = sim.getReplication().getStream("generation");
        Generator<Duration> interarrivelTimeGenerator = new Generator<Duration>()
        {
            @Override
            public Duration draw() throws ProbabilityException, ParameterException
            {
                @SuppressWarnings("synthetic-access")
                double mean = 1.0 / FundamentalDiagramDemo.this.demand.si;
                return Duration.instantiateSI(-mean * Math.log(stream.nextDouble()));
            }
        };
        // GTU characteristics generator
        CarFollowingModelFactory<IDMPlus> carFollowingModelFactory = new IDMPlusFactory(stream);
        PerceptionFactory perceptionFactory = new DefaultLMRSPerceptionFactory();
        LaneBasedTacticalPlannerFactory<LMRS> tacticalPlannerFactory = new LMRSFactory(carFollowingModelFactory,
            perceptionFactory);
        DistNormal fSpeed = new DistNormal(stream, 123.7 / 120.0, 12.0 / 120.0);
        ParameterFactory parametersFactory = new ParameterFactory()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void setValues(final Parameters parameters, final GTUType gtuType) throws ParameterException
            {
                if (gtuType.equals(truck))
                {
                    parameters.setParameter(ParameterTypes.A, Acceleration.instantiateSI(0.4));
                }
                else
                {
                    parameters.setParameter(ParameterTypes.A, Acceleration.instantiateSI(2.0));
                }
                parameters.setParameter(ParameterTypes.FSPEED, fSpeed.draw()); // also for trucks due to low speed limit option
                parameters.setParameter(ParameterTypes.TMIN, FundamentalDiagramDemo.this.tMin);
                parameters.setParameter(ParameterTypes.TMAX, FundamentalDiagramDemo.this.tMax);
            }
        };
        LaneBasedStrategicalRoutePlannerFactory laneBasedStrategicalPlannerFactory =
                new LaneBasedStrategicalRoutePlannerFactory(tacticalPlannerFactory, parametersFactory);
        LaneBasedGTUCharacteristicsGenerator laneBasedGTUCharacteristicsGenerator = new LaneBasedGTUCharacteristicsGenerator()
        {
            @Override
            public LaneBasedGTUCharacteristics draw() throws ProbabilityException, ParameterException, GTUException
            {
                @SuppressWarnings("synthetic-access")
                GTUType gtuType = stream.nextDouble() > FundamentalDiagramDemo.this.truckFraction ? car : truck;
                return new LaneBasedGTUCharacteristics(GTUType.defaultCharacteristics(gtuType, network, stream),
                    laneBasedStrategicalPlannerFactory, null, nodeA, nodeC, VehicleModel.MINMAX);
            }
        };
        // generator positions
        Set<DirectedLanePosition> initialPosition = new LinkedHashSet<>();
        for (Lane lane : lanesAB)
        {
            initialPosition.add(new DirectedLanePosition(lane, Length.ZERO, GTUDirectionality.DIR_PLUS));
        }
        LaneBiases biases = new LaneBiases();
        biases.addBias(car, LaneBias.bySpeed(new Speed(130.0, SpeedUnit.KM_PER_HOUR), new Speed(70.0, SpeedUnit.KM_PER_HOUR)));
        biases.addBias(truck, LaneBias.TRUCK_RIGHT);
        GeneratorPositions generatorPositions = GeneratorPositions.create(initialPosition, stream, biases);
        // room checker
        RoomChecker roomChecker = new CFBARoomChecker();
        // id generator
        IdGenerator idGenerator = new IdGenerator("");
        // generator
        LaneBasedGTUGenerator generator = new LaneBasedGTUGenerator("generator", interarrivelTimeGenerator,
            laneBasedGTUCharacteristicsGenerator, generatorPositions, network, sim, roomChecker, idGenerator);
        generator.setErrorHandler(GTUErrorHandler.DELETE);
        generator.setInstantaneousLaneChange(true);
        generator.setNoLaneChangeDistance(Length.instantiateSI(100.0));

        // Sinks
        for (Lane lane : lanesBC)
        {
            new SinkSensor(lane, lane.getLength(), GTUDirectionality.DIR_PLUS, sim);
        }

        return network;
    }

    /** {@inheritDoc} */
    @Override
    protected void setupDemo(final OTSAnimationPanel animationPanel, final OTSRoadNetwork net)
    {
        this.fdLine.update();

        // Demo panel
        animationPanel.createDemoPanel(DemoPanelPosition.BOTTOM);
        animationPanel.getDemoPanel().setPreferredSize(new Dimension(1000, 500));
        this.splitPanel = new JPanel(); // controls vs. graphs
        JPanel controlPanel = new JPanel();
        this.splitPanel.setLayout(new BoxLayout(this.splitPanel, BoxLayout.X_AXIS));
        this.splitPanel.add(controlPanel);
        animationPanel.getDemoPanel().add(this.splitPanel);

        // Control panel
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        controlPanel.setPreferredSize(new Dimension(250, 500));
        Dimension controlSize = new Dimension(250, 0);
        int strutSize = 20;
        // cross section dropdown
        JLabel crossSectionLabel = new JLabel("<html>Cross-section location [km]</html>");
        crossSectionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        crossSectionLabel.setMinimumSize(controlSize);
        controlPanel.add(crossSectionLabel);
        List<String> list = new ArrayList<>();
        for (int i = 250; i <= 2250; i += 250)
        {
            list.add(String.format("%.2f", i / 1000.0));
        }
        JComboBox<String> crossSectionMenu = new JComboBox<String>(list.toArray(new String[0]));
        Dimension crossSectionMenuSize = new Dimension(250, 25);
        crossSectionMenu.setMinimumSize(crossSectionMenuSize);
        crossSectionMenu.setMaximumSize(crossSectionMenuSize);
        crossSectionMenu.setSelectedIndex(5);
        crossSectionMenu.addActionListener(new ActionListener()
        {
            @SuppressWarnings({"synthetic-access", "unchecked"})
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                FundamentalDiagramDemo.this.absoluteCrossSection1 = (String) ((JComboBox<String>) e.getSource())
                    .getSelectedItem();
                createFundamentalDiagramsForCrossSections();
            }
        });
        controlPanel.add(crossSectionMenu);
        // 2nd drop down
        list = new ArrayList<>();
        list.add("None");
        for (int i = 250; i <= 2250; i += 250)
        {
            list.add(String.format("%.2f", i / 1000.0));
        }
        crossSectionMenu = new JComboBox<String>(list.toArray(new String[0]));
        crossSectionMenu.setMinimumSize(crossSectionMenuSize);
        crossSectionMenu.setMaximumSize(crossSectionMenuSize);
        crossSectionMenu.setSelectedIndex(0);
        crossSectionMenu.addActionListener(new ActionListener()
        {
            @SuppressWarnings({"synthetic-access", "unchecked"})
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                FundamentalDiagramDemo.this.absoluteCrossSection2 = (String) ((JComboBox<String>) e.getSource())
                    .getSelectedItem();
                createFundamentalDiagramsForCrossSections();
            }
        });
        controlPanel.add(crossSectionMenu);
        // 3rd drop down
        crossSectionMenu = new JComboBox<String>(list.toArray(new String[0]));
        crossSectionMenu.setMinimumSize(crossSectionMenuSize);
        crossSectionMenu.setMaximumSize(crossSectionMenuSize);
        crossSectionMenu.setSelectedIndex(0);
        crossSectionMenu.addActionListener(new ActionListener()
        {
            @SuppressWarnings({"synthetic-access", "unchecked"})
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                FundamentalDiagramDemo.this.absoluteCrossSection3 = (String) ((JComboBox<String>) e.getSource())
                    .getSelectedItem();
                createFundamentalDiagramsForCrossSections();
            }
        });
        controlPanel.add(crossSectionMenu);
        // spacer
        controlPanel.add(Box.createVerticalStrut(strutSize));
        // reset button
        JButton reset = new JButton("Clear data & graphs");
        reset.setAlignmentX(Component.CENTER_ALIGNMENT);
        reset.addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                clearDataAndGraphs();
            }
        });
        controlPanel.add(reset);
        // spacer
        controlPanel.add(Box.createVerticalStrut(strutSize));
        // demand
        JLabel demandLabel = new JLabel("<html>Demand [veh/h]</html>");
        demandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        demandLabel.setPreferredSize(controlSize);
        controlPanel.add(demandLabel);
        JSlider demandSlider = new JSlider(500, 5000, 3500);
        demandSlider.setPreferredSize(controlSize);
        demandSlider.setSnapToTicks(true);
        demandSlider.setMinorTickSpacing(250);
        demandSlider.setMajorTickSpacing(1000);
        demandSlider.setPaintTicks(true);
        demandSlider.setPaintLabels(true);
        demandSlider.setToolTipText("<html>Demand [veh/h]</html>");
        demandSlider.addChangeListener(new ChangeListener()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void stateChanged(final ChangeEvent e)
            {
                double value = ((JSlider) e.getSource()).getValue();
                FundamentalDiagramDemo.this.demand = new Frequency(value, FrequencyUnit.PER_HOUR);
            }
        });
        controlPanel.add(demandSlider);
        // spacer
        controlPanel.add(Box.createVerticalStrut(strutSize));
        // truck percentage
        JLabel truckLabel = new JLabel("<html>Truck percentage [%]</html>");
        truckLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        truckLabel.setPreferredSize(controlSize);
        controlPanel.add(truckLabel);
        JSlider truckSlider = new JSlider(0, 30, 5);
        truckSlider.setPreferredSize(controlSize);
        truckSlider.setSnapToTicks(true);
        truckSlider.setMinorTickSpacing(5);
        truckSlider.setMajorTickSpacing(10);
        truckSlider.setPaintTicks(true);
        truckSlider.setPaintLabels(true);
        truckSlider.setToolTipText("<html>Truck percentage [%]</html>");
        truckSlider.addChangeListener(new ChangeListener()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void stateChanged(final ChangeEvent e)
            {
                double value = ((JSlider) e.getSource()).getValue() / 100.0;
                FundamentalDiagramDemo.this.truckFraction = value;
                FundamentalDiagramDemo.this.fdLine.update();
                notifyPlotsChanged();
            }
        });
        controlPanel.add(truckSlider);
        // spacer
        controlPanel.add(Box.createVerticalStrut(strutSize));
        // Tmax
        JLabel tLabel = new JLabel("<html>Max. headway [s]</html>");
        tLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tLabel.setPreferredSize(controlSize);
        controlPanel.add(tLabel);
        JSlider tSlider = new JSlider(10, 20, 12);
        Dictionary<Integer, JLabel> labels = new Hashtable<>();
        for (int i = 10; i <= 20; i += 2)
        {
            labels.put(i, new JLabel(String.format("%.1f", i / 10.0)));
        }
        tSlider.setLabelTable(labels);
        tSlider.setPreferredSize(controlSize);
        tSlider.setSnapToTicks(true);
        tSlider.setMinorTickSpacing(1);
        tSlider.setMajorTickSpacing(2);
        tSlider.setPaintTicks(true);
        tSlider.setPaintLabels(true);
        tSlider.setToolTipText("<html>Max. headway [s]</html>");
        tSlider.addChangeListener(new ChangeListener()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void stateChanged(final ChangeEvent e)
            {
                double value = ((JSlider) e.getSource()).getValue() / 10.0;
                FundamentalDiagramDemo.this.tMin = Duration.instantiateSI((0.56 / 1.2) * value);
                FundamentalDiagramDemo.this.tMax = Duration.instantiateSI(value);
                FundamentalDiagramDemo.this.fdLine.update();
                notifyPlotsChanged();
                for (GTU gtu : getNetwork().getGTUs())
                {
                    try
                    {
                        gtu.getParameters().setParameter(ParameterTypes.TMIN, FundamentalDiagramDemo.this.tMin);
                        gtu.getParameters().setParameter(ParameterTypes.TMAX, FundamentalDiagramDemo.this.tMax);
                    }
                    catch (ParameterException exception)
                    {
                        System.err.println("Unable to set headway parameter.");
                    }
                }
            }
        });
        controlPanel.add(tSlider);
        // spacer
        controlPanel.add(Box.createVerticalStrut(strutSize));
        // V max
        JLabel vLabel = new JLabel("<html>Speed limit [km/h]</html>");
        vLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        vLabel.setPreferredSize(controlSize);
        controlPanel.add(vLabel);
        JSlider vSlider = new JSlider(80, 130, 120);
        vSlider.setPreferredSize(controlSize);
        vSlider.setSnapToTicks(true);
        vSlider.setMinorTickSpacing(10);
        vSlider.setMajorTickSpacing(10);
        vSlider.setPaintTicks(true);
        vSlider.setPaintLabels(true);
        vSlider.setToolTipText("<html>Speed limit [km/h]</html>");
        vSlider.addChangeListener(new ChangeListener()
        {
            @SuppressWarnings("synthetic-access")
            @Override
            public void stateChanged(final ChangeEvent e)
            {
                FundamentalDiagramDemo.this.speedLimit = new Speed(((JSlider) e.getSource()).getValue(), SpeedUnit.KM_PER_HOUR);
                FundamentalDiagramDemo.this.fdLine.update();
                notifyPlotsChanged();
                for (Link link : getNetwork().getLinkMap().values())
                {
                    for (Lane lane : ((CrossSectionLink) link).getLanes())
                    {
                        lane.setSpeedLimit(getNetwork().getGtuType(DEFAULTS.VEHICLE), FundamentalDiagramDemo.this.speedLimit);
                    }
                }
                for (GTU gtu : getNetwork().getGTUs())
                {
                    DirectInfrastructurePerception infra;
                    try
                    {
                        infra = (DirectInfrastructurePerception) gtu.getTacticalPlanner().getPerception().getPerceptionCategory(
                            InfrastructurePerception.class);
                        // hack to reset the perceived speed limit cache
                        Field field = DirectInfrastructurePerception.class.getDeclaredField("root");
                        field.setAccessible(true);
                        field.set(infra, null);
                    }
                    catch (OperationalPlanException | NoSuchFieldException | SecurityException | IllegalArgumentException
                        | IllegalAccessException exception)
                    {
                        System.err.println("Unable to update perceived speed limit.");
                    }
                }
            }
        });
        controlPanel.add(vSlider);

        // Initiate graphs
        clearDataAndGraphs();
    }
    
    /**
     * Response when settings were changed that affect the shape of the theoretical fundamental diagram, i.e. the FD line.
     */
    void notifyPlotsChanged()
    {
        for (FundamentalDiagram diagram : this.funamentalDiagrams)
        {
            diagram.notifyPlotChange();
        }
    }

    /**
     * Response to clear data button.
     */
    private void clearDataAndGraphs()
    {
        // creating a sampler (and graphs) while running gives issues with scheduling in the past (sometimes)
        boolean wasRunning = getSimulator().isStartingOrRunning();
        if (wasRunning)
        {
            getSimulator().stop();
        }

        // new sampler to loose all data
        this.sampler = new RoadSampler(getNetwork());

        // create fundamental diagram source for each cross section (plots are (re)created in setCrossSections())
        for (int i = 250; i <= 2250; i += 250)
        {
            List<String> names = new ArrayList<>();
            names.add("Left");
            names.add("Right");
            Length lanePosition;
            String linkId;
            if (i >= 1500.0)
            {
                lanePosition = Length.instantiateSI(i - 1500.0);
                linkId = "Lane-dropDestination";
            }
            else
            {
                names.add(1, "Middle");
                lanePosition = Length.instantiateSI(i);
                linkId = "OriginLane-drop";
            }
            DirectedLinkPosition linkPosition = new DirectedLinkPosition(getNetwork().getLink(linkId), lanePosition,
                GTUDirectionality.DIR_PLUS);
            GraphCrossSection<KpiLaneDirection> crossSection;
            try
            {
                crossSection = GraphLaneUtil.createCrossSection(names, linkPosition);
            }
            catch (NetworkException exception)
            {
                throw new RuntimeException("Unable to create cross section.", exception);
            }
            Duration aggregationTime = Duration.instantiateSI(30.0);
            FdSource source = FundamentalDiagram.sourceFromSampler(this.sampler, crossSection, true, aggregationTime, false);
            this.fdSourceMap.put(String.format("%.2f", i / 1000.0), source);
        }

        // create sampler plot
        List<String> names = new ArrayList<>();
        names.add("Left lane");
        names.add("Middle lane");
        names.add("Right lane");
        List<LaneDirection> firstLanes = new ArrayList<>();
        for (Lane lane : ((CrossSectionLink) getNetwork().getLink("OriginLane-drop")).getLanes())
        {
            firstLanes.add(new LaneDirection(lane, GTUDirectionality.DIR_PLUS));
        }
        GraphPath<KpiLaneDirection> path = Try.assign(() -> GraphLaneUtil.createPath(names, firstLanes), "");
        TrajectoryPlot trajectoryPlot = new TrajectoryPlot("Trajectories", Duration.instantiateSI(5.0), getSimulator(),
            this.sampler.getSamplerData(), path);
        trajectoryPlot.updateFixedDomainRange(true);
        SwingTrajectoryPlot swingTrajectoryPlot = new SwingTrajectoryPlot(trajectoryPlot)
        {
            /** */
            private static final long serialVersionUID = 20200516L;

            /** {@inheritDoc} */
            @Override
            protected void addPopUpMenuItems(final JPopupMenu popupMenu)
            {
                // disable
            }
        };
        this.trajectoryPanel = swingTrajectoryPlot.getContentPane();

        // reset simulator state
        if (!getSimulator().isStartingOrRunning() && wasRunning)
        {
            getSimulator().start();
        }

        // create fundamental diagrams
        createFundamentalDiagramsForCrossSections();
    }

    /**
     * Creates the fundamental diagrams based on the selected cross-sections.
     */
    private void createFundamentalDiagramsForCrossSections()
    {
        // avoid update scheduling in the past as simulator is running during creation
        boolean wasRunning = getSimulator().isStartingOrRunning();
        if (wasRunning)
        {
            getSimulator().stop();
        }

        // keep color of selected theme in new GUI elements
        Color color = null;
        if (this.graphPanel != null)
        {
            color = this.graphPanel.getBackground();
            // remove previous graphs
            this.splitPanel.remove(this.graphPanel);
        }
        // create new panel for graphs
        this.graphPanel = new TablePanel(2, 2);
        this.graphPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        if (color != null)
        {
            this.graphPanel.setBackground(color);
        }
        this.splitPanel.add(this.graphPanel);

        // compose a combined source if required
        FdSource source;
        if (this.absoluteCrossSection2.equals("None") && this.absoluteCrossSection3.equals("None"))
        {
            source = this.fdSourceMap.get(this.absoluteCrossSection1);
            source.clearFundamentalDiagrams();
        }
        else
        {
            Map<String, FdSource> sources = new LinkedHashMap<>();
            sources.put(this.absoluteCrossSection1 + "km", this.fdSourceMap.get(this.absoluteCrossSection1));
            if (!this.absoluteCrossSection2.equals("None"))
            {
                sources.put(this.absoluteCrossSection2 + "km", this.fdSourceMap.get(this.absoluteCrossSection2));
            }
            if (!this.absoluteCrossSection3.equals("None"))
            {
                sources.put(this.absoluteCrossSection3 + "km", this.fdSourceMap.get(this.absoluteCrossSection3));
            }
            for (FdSource subSource : sources.values())
            {
                subSource.clearFundamentalDiagrams();
            }
            source = FundamentalDiagram.combinedSource(sources);
        }

        // because "Aggregate" and "Theoretical" looks ugly in the legend, we set the actual location as legend label
        source.setAggregateName(this.absoluteCrossSection1);
        
        // create the fundamental diagrams
        FundamentalDiagram fdPlota = new FundamentalDiagram("Density-speed", Quantity.DENSITY, Quantity.SPEED, getSimulator(),
            source, this.fdLine);
        FundamentalDiagram fdPlotb = new FundamentalDiagram("Density-flow", Quantity.DENSITY, Quantity.FLOW, getSimulator(),
            source, this.fdLine);
        FundamentalDiagram fdPlotc = new FundamentalDiagram("Flow-speed", Quantity.FLOW, Quantity.SPEED, getSimulator(), source,
            this.fdLine);
        
        // recalculate over past data
        source.recalculate(getSimulator().getSimulatorTime());
        
        // store graphs so changes to setting may affect the graphs
        this.funamentalDiagrams.clear();
        this.funamentalDiagrams.add(fdPlota);
        this.funamentalDiagrams.add(fdPlotb);
        this.funamentalDiagrams.add(fdPlotc);

        // create swing plots and add them to the graph panel
        Container fda = new SwingFundamentalDiagramNoControl(fdPlota).getContentPane();
        Container fdb = new SwingFundamentalDiagramNoControl(fdPlotb).getContentPane();
        Container fdc = new SwingFundamentalDiagramNoControl(fdPlotc).getContentPane();
        Dimension preferredGraphSize = new Dimension(375, 230);
        fda.setPreferredSize(preferredGraphSize);
        fdb.setPreferredSize(preferredGraphSize);
        fdc.setPreferredSize(preferredGraphSize);
        this.graphPanel.setCell(fda, 0, 0);
        this.graphPanel.setCell(fdb, 0, 1);
        this.graphPanel.setCell(fdc, 1, 0);

        // also add the trajectory panel (which hasn't changed but should be moved to the new graphs panel)
        this.trajectoryPanel.setPreferredSize(preferredGraphSize);
        this.graphPanel.setCell(this.trajectoryPanel, 1, 1);

        // set theme color of fundamental diagrams too (the little bar below where the mouse-over info is shown is visible)
        if (color != null)
        {
            fda.setBackground(color);
            fdb.setBackground(color);
            fdc.setBackground(color);
            this.trajectoryPanel.setBackground(color);
        }
        
        // reorganize panels
        fda.getParent().getParent().validate();

        // reset simulator state
        if (!getSimulator().isStartingOrRunning() && wasRunning)
        {
            getSimulator().start();
        }
    }

    /**
     * Class to disable aggregation period and update frequency.
     */
    private class SwingFundamentalDiagramNoControl extends SwingFundamentalDiagram
    {
        /** */
        private static final long serialVersionUID = 20200516L;

        /**
         * @param plot FundamentalDiagram; fundamental diagram
         */
        SwingFundamentalDiagramNoControl(final FundamentalDiagram plot)
        {
            super(plot);
        }

        /** {@inheritDoc} */
        @Override
        protected void addPopUpMenuItems(final JPopupMenu popupMenu)
        {
            // disable
        }
    }
    
    /**
     * Fundamental diagram line class based on local settings.
     */
    private class DynamicFdLine implements FdLine
    {
        /** Map of points for each quantity. */
        private Map<Quantity, double[]> map = new LinkedHashMap<>();

        /** {@inheritDoc} */
        @Override
        public double[] getValues(final Quantity quantity)
        {
            return this.map.get(quantity);
        }

        /** {@inheritDoc} */
        @Override
        public String getName()
        {
            return "Theoretical";
        }

        /**
         * Recalculates the FD based on input parameters.
         */
        @SuppressWarnings("synthetic-access")
        public void update()
        {
            // harmonic mean of desired speed of cars and trucks
            HarmonicMean<Speed, Double> meanSpeed = new HarmonicMean<>();
            Speed carSpeed = FundamentalDiagramDemo.this.speedLimit.times(123.7 / 120.0);
            meanSpeed.add(carSpeed, 1.0 - FundamentalDiagramDemo.this.truckFraction);
            Speed truckSpeed = Speed.min(carSpeed, new Speed(85.0, SpeedUnit.KM_PER_HOUR));
            meanSpeed.add(truckSpeed, FundamentalDiagramDemo.this.truckFraction);

            // mean of lengths
            double meanLength = 4.19 * (1.0 - FundamentalDiagramDemo.this.truckFraction) + 12.0
                * FundamentalDiagramDemo.this.truckFraction;

            // calculate triangular FD parameters
            double vMax = meanSpeed.getMean();
            double kCrit = 1000.0 / (vMax * FundamentalDiagramDemo.this.tMax.si + meanLength + 3.0);
            vMax = vMax * 3.6;
            double qMax = vMax * kCrit;
            int kJam = (int) (1000.0 / (meanLength + 3.0));

            // initialize and fill arrays for each quantity
            double[] k = new double[kJam * 10 + 1];
            double[] q = new double[kJam * 10 + 1];
            double[] v = new double[kJam * 10 + 1];
            for (int kk = 0; kk <= kJam * 10; kk++)
            {
                double kVal = kk / 10.0;
                k[kk] = kVal;
                if (kVal > kCrit)
                {
                    // congestion branch
                    q[kk] = qMax * (1.0 - (kVal - kCrit) / (kJam - kCrit));
                    v[kk] = q[kk] / k[kk];
                }
                else
                {
                    // free-flow branch
                    v[kk] = vMax;
                    q[kk] = k[kk] * v[kk];
                }
            }

            // cache values
            this.map.put(Quantity.DENSITY, k);
            this.map.put(Quantity.FLOW, q);
            this.map.put(Quantity.SPEED, v);
        }
    }

}
