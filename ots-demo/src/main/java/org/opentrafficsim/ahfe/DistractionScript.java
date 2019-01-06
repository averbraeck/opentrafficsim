package org.opentrafficsim.ahfe;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djutils.io.URLResource;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.animation.gtu.colorer.AccelerationGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SpeedGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SwitchableGTUColorer;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterFactoryByType;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.perception.HistoryManagerDEVS;
import org.opentrafficsim.kpi.interfaces.LaneDataInterface;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.SpaceTimeRegion;
import org.opentrafficsim.road.gtu.colorer.DesiredHeadwayColorer;
import org.opentrafficsim.road.gtu.colorer.DesiredSpeedColorer;
import org.opentrafficsim.road.gtu.colorer.FixedColor;
import org.opentrafficsim.road.gtu.colorer.GTUTypeColorer;
import org.opentrafficsim.road.gtu.colorer.IncentiveColorer;
import org.opentrafficsim.road.gtu.colorer.SynchronizationColorer;
import org.opentrafficsim.road.gtu.colorer.TaskSaturationColorer;
import org.opentrafficsim.road.gtu.colorer.TotalDesireColorer;
import org.opentrafficsim.road.gtu.generator.od.DefaultGTUCharacteristicsGeneratorOD;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.gtu.generator.od.StrategicalPlannerFactorySupplierOD;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Anticipation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Estimation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType.PerceivedHeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationHeadway;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSituationalAwareness;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.BehavioralAdaptation;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.Task;
import org.opentrafficsim.road.gtu.lane.perception.mental.TaskCarFollowing;
import org.opentrafficsim.road.gtu.lane.perception.mental.TaskLaneChanging;
import org.opentrafficsim.road.gtu.lane.perception.mental.TaskLaneChanging.LateralConsideration;
import org.opentrafficsim.road.gtu.lane.perception.mental.TaskRoadSideDistraction;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.AccelerationIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveGetInLane;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveKeep;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveRoute;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSocioSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeed;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveSpeedWithCourtesy;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.IncentiveStayRight;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.object.Distraction;
import org.opentrafficsim.road.network.lane.object.Distraction.TrapezoidProfile;
import org.opentrafficsim.road.network.sampling.LinkData;
import org.opentrafficsim.road.network.sampling.RoadSampler;
import org.opentrafficsim.road.network.sampling.data.TimeToCollision;
import org.opentrafficsim.swing.script.AbstractSimulationScript;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Distraction simulation.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class DistractionScript extends AbstractSimulationScript
{

    /** Distance to not consider at start of the network. */
    private static Length ignoreStart = Length.createSI(2900); // Not 100m on pre-link, so 3000 total

    /** Distance to not consider at end of the network. */
    private static Length ignoreEnd = Length.createSI(1000);

    /** Sampler. */
    private RoadSampler sampler;

    /**
     * Main method.
     * @param args String[]; command line arguments
     */
    public static void main(final String[] args)
    {
        // Long start = System.currentTimeMillis();
        DistractionScript script = new DistractionScript(args);
        // script.setProperty("autorun", true);
        script.start();
        // Long end = System.currentTimeMillis();
        // System.out.println("That took " + (end - start) / 1000 + "s.");
    }

    /**
     * Constructor.
     * @param properties String[]; properties as name-value pairs
     */
    private DistractionScript(final String[] properties)
    {
        super("Distraction", "Distraction simulation", properties);
        setGtuColorer(SwitchableGTUColorer.builder().addActiveColorer(new FixedColor(Color.BLUE, "Blue"))
                .addColorer(GTUTypeColorer.DEFAULT).addColorer(new SpeedGTUColorer(new Speed(150, SpeedUnit.KM_PER_HOUR)))
                .addColorer(
                        new DesiredSpeedColorer(new Speed(50, SpeedUnit.KM_PER_HOUR), new Speed(150, SpeedUnit.KM_PER_HOUR)))
                .addColorer(new AccelerationGTUColorer(Acceleration.createSI(-6.0), Acceleration.createSI(2)))
                .addColorer(new SynchronizationColorer())
                .addColorer(new DesiredHeadwayColorer(Duration.createSI(0.56), Duration.createSI(2.4)))
                .addColorer(new TotalDesireColorer()).addColorer(new IncentiveColorer(IncentiveRoute.class))
                .addColorer(new IncentiveColorer(IncentiveSpeedWithCourtesy.class))
                .addColorer(new IncentiveColorer(IncentiveSpeed.class)).addColorer(new IncentiveColorer(IncentiveKeep.class))
                .addColorer(new IncentiveColorer(IncentiveGetInLane.class))
                .addColorer(new IncentiveColorer(IncentiveCourtesy.class))
                .addColorer(new IncentiveColorer(IncentiveSocioSpeed.class)).addColorer(new TaskSaturationColorer()).build());
    }

    /** {@inheritDoc} */
    @Override
    protected void setDefaultProperties()
    {
        setProperty("fTruck", "0.05");
        setProperty("leftDemand", "3500");
        setProperty("rightDemand", "3200");
        setProperty("sampler", "true");
        setProperty("warmupTime", "360");
        setProperty("simulationTime", "3960");
        setProperty("scenario", "test");
    }

    /** {@inheritDoc} */
    @Override
    protected void onSimulationEnd()
    {
        if (this.sampler != null)
        {
            this.sampler.writeToFile(getProperty("outputDir") + getProperty("scenario") + ".csv");
        }
    }

    /** {@inheritDoc} */
    @Override
    protected OTSNetwork setupSimulation(final OTSSimulatorInterface sim) throws Exception
    {
        AbstractGTU.ALIGNED = false;
        LaneOperationalPlanBuilder.INSTANT_LANE_CHANGES = true;

        // Network
        InputStream stream = URLResource.getResourceAsStream("/AHFE/Network.xml");
        XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(sim);
        OTSNetwork network = new OTSNetwork("Distraction");
        try
        {
            nlp.build(stream, network, false);
        }
        catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException | GTUException
                | OTSGeometryException | ValueException | ParameterException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
        new Distraction("distraction", ((CrossSectionLink) network.getLink("END")).getLanes().get(0), Length.createSI(1000),
                sim, new TrapezoidProfile(0.2, Length.createSI(-400), Length.createSI(200), Length.createSI(400)));

        // OD
        List<Node> origins = new ArrayList<>();
        origins.add(network.getNode("LEFTINPRE"));
        origins.add(network.getNode("RIGHTINPRE"));
        List<Node> destinations = new ArrayList<>();
        destinations.add(network.getNode("EXIT"));
        Categorization categorization = new Categorization("Distraction", GTUType.class);
        TimeVector globalTime =
                new TimeVector(new double[] { 0, 360, 1560, 2160, 3960 }, TimeUnit.BASE_SECOND, StorageType.DENSE);
        ODMatrix od = new ODMatrix("Distraction", origins, destinations, categorization, globalTime, Interpolation.LINEAR);
        Category carCategory = new Category(categorization, GTUType.CAR);
        Category truckCategory = new Category(categorization, GTUType.TRUCK);
        double fTruck = getDoubleProperty("fTruck");
        double demandLeft = getDoubleProperty("leftDemand");
        double demandRight = getDoubleProperty("rightDemand");
        FrequencyVector leftDemandPatternCar = getDemand(demandLeft * (1.0 - fTruck));
        FrequencyVector leftDemandPatternTruck = getDemand(demandLeft * fTruck);
        FrequencyVector rightDemandPatternCar = getDemand(demandRight * (1.0 - fTruck));
        FrequencyVector rightDemandPatternTruck = getDemand(demandRight * fTruck);
        od.putDemandVector(network.getNode("LEFTINPRE"), network.getNode("EXIT"), carCategory, leftDemandPatternCar);
        od.putDemandVector(network.getNode("LEFTINPRE"), network.getNode("EXIT"), truckCategory, leftDemandPatternTruck);
        od.putDemandVector(network.getNode("RIGHTINPRE"), network.getNode("EXIT"), carCategory, rightDemandPatternCar);
        od.putDemandVector(network.getNode("RIGHTINPRE"), network.getNode("EXIT"), truckCategory, rightDemandPatternTruck);
        ODOptions odOptions = new ODOptions().set(ODOptions.ANIMATION, true).set(ODOptions.GTU_COLORER, getGtuColorer())
                .set(ODOptions.GTU_TYPE, new DefaultGTUCharacteristicsGeneratorOD(new DistractionFactorySupplier()));
        ODApplier.applyOD(network, od, sim, odOptions);

        // History
        sim.getReplication().setHistoryManager(new HistoryManagerDEVS(sim, Duration.createSI(2.0), Duration.createSI(1.0)));

        // Sampler
        this.sampler = new RoadSampler(sim);
        this.sampler.registerExtendedDataType(new TimeToCollision());
        LinkData linkData = new LinkData((CrossSectionLink) network.getLink("LEFTIN"));
        registerLinkToSampler(linkData, ignoreStart, linkData.getLength());
        linkData = new LinkData((CrossSectionLink) network.getLink("RIGHTIN"));
        registerLinkToSampler(linkData, ignoreStart, linkData.getLength());
        linkData = new LinkData((CrossSectionLink) network.getLink("CONVERGE"));
        registerLinkToSampler(linkData, Length.ZERO, linkData.getLength());
        linkData = new LinkData((CrossSectionLink) network.getLink("WEAVING"));
        registerLinkToSampler(linkData, Length.ZERO, linkData.getLength());
        linkData = new LinkData((CrossSectionLink) network.getLink("END"));
        registerLinkToSampler(linkData, Length.ZERO, linkData.getLength().minus(ignoreEnd));

        // return
        return network;
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
            Length start = laneData.getLength().multiplyBy(startDistance.si / linkData.getLength().si);
            Length end = laneData.getLength().multiplyBy(endDistance.si / linkData.getLength().si);
            this.sampler
                    .registerSpaceTimeRegion(new SpaceTimeRegion(new KpiLaneDirection(laneData, KpiGtuDirectionality.DIR_PLUS),
                            start, end, getTimeProperty("warmupTime"), getTimeProperty("simulationTime")));
        }
    }

    /**
     * Compose demand vector.
     * @param demand double; maximum demand value
     * @return FrequencyVector demand vector
     * @throws ValueException on value exception
     */
    private static FrequencyVector getDemand(final double demand) throws ValueException
    {
        return new FrequencyVector(new double[] { demand * 0.5, demand * 0.5, demand, demand, 0.0 }, FrequencyUnit.PER_HOUR,
                StorageType.DENSE);
    }

    /**
     * Supplier of strategical factory.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 9 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class DistractionFactorySupplier implements StrategicalPlannerFactorySupplierOD
    {
        /** */
        DistractionFactorySupplier()
        {
        }

        /** {@inheritDoc} */
        @Override
        public LaneBasedStrategicalPlannerFactory<?> getFactory(final Node origin, final Node destination,
                final Category category, final StreamInterface randomStream) throws GTUException
        {
            IDMPlusFactory idm = new IDMPlusFactory(randomStream);
            PerceptionFactory perc = new DefaultLMRSPerceptionFactory()
            {
                @Override
                public LanePerception generatePerception(final LaneBasedGTU gtu)
                {
                    Set<Task> tasks = new LinkedHashSet<>();
                    tasks.add(new TaskCarFollowing());
                    tasks.add(new TaskLaneChanging(LateralConsideration.DESIRE));
                    tasks.add(new TaskRoadSideDistraction());
                    Set<BehavioralAdaptation> adaptations = new LinkedHashSet<>();
                    adaptations.add(new AdaptationSituationalAwareness());
                    adaptations.add(new AdaptationHeadway());
                    LanePerception perception = new CategoricalLanePerception(gtu, new Fuller(tasks, adaptations));
                    perception.addPerceptionCategory(new DirectEgoPerception(perception));
                    perception.addPerceptionCategory(new DirectInfrastructurePerception(perception));
                    perception.addPerceptionCategory(new DirectNeighborsPerception(perception,
                            new PerceivedHeadwayGtuType(Estimation.UNDERESTIMATION, Anticipation.CONSTANT_SPEED)));
                    perception.addPerceptionCategory(new AnticipationTrafficPerception(perception));
                    return perception;
                }

                @Override
                public Parameters getParameters() throws ParameterException
                {
                    Parameters params = super.getParameters();
                    params.setParameter(Fuller.TC, 1.0);
                    params.setParameter(Fuller.TS_CRIT, 0.8);
                    params.setParameter(Fuller.TS_MAX, 2.0);
                    params.setParameter(AdaptationSituationalAwareness.SA_MAX, 1.0);
                    params.setParameter(AdaptationSituationalAwareness.SA_MIN, 0.5);
                    params.setParameter(AdaptationSituationalAwareness.TR_MAX, Duration.createSI(2.0));
                    params.setParameter(ParameterTypes.TR, Duration.ZERO);
                    params.setParameter(AdaptationHeadway.BETA_T, 1.0);
                    return params;
                }
            };
            ParameterFactoryByType params = new ParameterFactoryByType();
            Set<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();
            mandatoryIncentives.add(new IncentiveRoute());
            Set<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();
            voluntaryIncentives.add(new IncentiveSpeedWithCourtesy());
            voluntaryIncentives.add(new IncentiveKeep());
            if (category.getCategorization().entails(GTUType.class) && category.get(GTUType.class).isOfType(GTUType.TRUCK))
            {
                voluntaryIncentives.add(new IncentiveStayRight());
                params.addParameter(GTUType.TRUCK, ParameterTypes.A, Acceleration.createSI(0.8));
            }
            Set<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();
            return new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(idm, perc, Synchronization.PASSIVE, Cooperation.PASSIVE, GapAcceptance.INFORMED,
                            Tailgating.NONE, mandatoryIncentives, voluntaryIncentives, accelerationIncentives),
                    params);
        }
    }

}
