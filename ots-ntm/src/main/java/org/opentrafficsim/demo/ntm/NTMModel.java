package org.opentrafficsim.demo.ntm;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.demo.ntm.NTMNode.TrafficBehaviourType;
import org.opentrafficsim.demo.ntm.animation.AreaAnimation;
import org.opentrafficsim.demo.ntm.animation.NodeAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpLinkAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpNodeAnimation;
import org.opentrafficsim.demo.ntm.shapeobjects.ShapeObject;
import org.opentrafficsim.demo.ntm.shapeobjects.ShapeStore;
import org.opentrafficsim.demo.ntm.trafficdemand.DepartureTimeProfile;
import org.opentrafficsim.demo.ntm.trafficdemand.TripDemand;
import org.opentrafficsim.demo.ntm.trafficdemand.TripInfoTimeDynamic;

import nl.tudelft.simulation.dsol.simulators.AnimatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Sep 9, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class NTMModel extends AbstractOTSModel
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** Detailed areas from the traffic model. */
    private Map<String, Area> areas;

    /** Detailed areas from the traffic model. */
    private Map<String, Area> bigAreas;

    /** Rougher areas from the traffic model. */
    private ShapeStore compressedAreas;

    /** Nodes from shape file. */
    private Map<String, NTMNode> nodes;

    /** Connectors from shape file. */
    private Map<String, NTMLink> shpConnectors;

    /** Connectors from shape file. */
    private Map<String, NTMLink> shpBigConnectors;

    /** Links from shape file. */
    private Map<String, NTMLink> shpLinks;

    /** Subset of links from shape file used as flow links. */
    private Map<String, NTMLink> flowLinks;

    /** Detailed areas from the traffic model. */
    private Map<String, AreaFlowLink> areaFlowLinks;

    /** The centroids. */
    private Map<String, NTMNode> centroids;

    /** The centroids. */
    private Map<String, NTMNode> bigCentroids;

    /** The demand of trips by Origin and Destination. */
    private TripDemand<TripInfoTimeDynamic> tripDemand;

    /** The compressed demand of trips by Origin and Destination. */
    private TripDemand<TripInfoTimeDynamic> compressedTripDemand;

    /** The demand of trips by Origin and Destination for simulation. */
    TripDemand<TripInfoTimeDynamic> tripDemandToUse;

    /** */
    private InputNTM inputNTM;

    /** The simulation settings. */
    private NTMSettings settingsNTM;

    /** Profiles with fractions of total demand. */
    private ArrayList<DepartureTimeProfile> departureTimeProfiles;

    /** Graph containing the original network. */
    private SimpleDirectedWeightedGraph<NTMNode, LinkEdge<NTMLink>> linkGraph;

    /** Graph containing the simplified network. */
    private SimpleDirectedWeightedGraph<NTMNode, LinkEdge<NTMLink>> areaGraph;

    /** */
    private Map<String, NTMNode> nodeAreaGraphMap = new LinkedHashMap<>();

    /** Subset of links from shape file used as flow links. */
    private LinkedHashMap<String, NTMLink> debugLinkList;

    /** TODO: make sure network is used... It's empty now... */
    private OTSNetwork network;

    /**
     * Constructor to make the graphs with the right type.
     * @param simulator OTSSimulatorInterface; the simulator
     */
    @SuppressWarnings("unchecked")
    public NTMModel(final OTSSimulatorInterface simulator)
    {
        super(simulator);
        this.network = new OTSNetwork("ntm", true, simulator);
        LinkEdge<NTMLink> l = new LinkEdge<NTMLink>(null);
        this.linkGraph =
                new SimpleDirectedWeightedGraph<NTMNode, LinkEdge<NTMLink>>((Class<? extends LinkEdge<NTMLink>>) l.getClass());
        this.areaGraph =
                new SimpleDirectedWeightedGraph<NTMNode, LinkEdge<NTMLink>>((Class<? extends LinkEdge<NTMLink>>) l.getClass());
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel()
    {
        try
        {
            // create the output base maps

            // boolean DEBUG = true;
            // set the time step value at ten seconds;
            Duration timeStepNTM = new Duration(10, DurationUnit.SECOND);
            Duration timeStepCellTransmissionModel = new Duration(10, DurationUnit.SECOND);
            Duration durationOfSimulation = new Duration(10800, DurationUnit.SECOND);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
            Calendar startTime = new GregorianCalendar(2014, 1, 28, 15, 0, 0);
            this.shpLinks = new LinkedHashMap<>();
            this.shpConnectors = new LinkedHashMap<>();

            this.centroids = ShapeFileReader.ReadNodes(this,
                    this.getInputNTM().getInputMap() + this.getInputNTM().getFileCentroids(), "NODENR",
                    this.getInputNTM().isReturnCentroidsCentroid(), this.getInputNTM().isOnlyCentroidsFileCentroid());
            this.nodes = ShapeFileReader.ReadNodes(this, this.getInputNTM().getInputMap() + this.getInputNTM().getFileNodes(),
                    "NODENR", this.getInputNTM().isReturnCentroidsNode(), this.getInputNTM().isOnlyCentroidsFileNode());
            this.areas = ShapeFileReader.readAreas(this.getInputNTM().getInputMap() + this.getInputNTM().getFileAreas(),
                    this.centroids, this.getInputNTM().getScalingFactorDemand());

            ShapeFileReader.readLinks(this, this.getInputNTM().getInputMap() + this.getInputNTM().getFileLinks(), this.shpLinks,
                    this.shpConnectors, this.nodes, this.centroids, this.getInputNTM().getLengthUnitLink(),
                    this.getInputNTM().getLinkCapacityNumberOfHours());

            if (this.getInputNTM().getFileFeederLinks() != null)
            {
                if (!this.getInputNTM().getFileFeederLinks().isEmpty())
                {
                    ShapeFileReader.readLinks(this, this.getInputNTM().getInputMap() + this.getInputNTM().getFileFeederLinks(),
                            this.shpLinks, this.shpConnectors, this.nodes, this.centroids,
                            this.getInputNTM().getLengthUnitLink(), this.getInputNTM().getLinkCapacityNumberOfHours());
                }
            }
            this.setDepartureTimeProfiles(CsvFileReader.readDepartureTimeProfiles(
                    this.getInputNTM().getInputMap() + this.getInputNTM().getFileProfiles(), ";", "\\s+"));

            this.getInputNTM().setOutputMap("/output" + this.getInputNTM().getVariantNumber());

            this.settingsNTM = new NTMSettings(startTime, durationOfSimulation, " NTM The Hague ", timeStepNTM,
                    timeStepCellTransmissionModel, this.getInputNTM().getReRouteTimeInterval(),
                    this.getInputNTM().getNumberOfRoutes(), this.getInputNTM().getWeightNewRoutes(),
                    this.getInputNTM().getVarianceRoutes(), this.getInputNTM().isReRoute(), this.getInputNTM().getInputMap(),
                    this.getInputNTM().isIncreaseDemandAreaByFactor(), this.getInputNTM().getScalingFactorDemand());

            // the Map areas contains a reference to the centroids!
            // save the selected and created areas to a shape file
            // WriteToShp.createShape(this.areas);

            // read TrafficDemand from /src/main/resources
            // including information on the time period this demand covers!
            // within "readOmnitransExportDemand" the cordon zones are determined and areas are created around them
            // - create additional centroids at cordons and add related areas!!
            // - move links from normal to connectors
            // - add time settings from the demand matrix
            // - create demand between centoids and areas
            this.setTripDemand(CsvFileReader.readOmnitransExportDemand(this,
                    this.getInputNTM().getInputMap() + this.getInputNTM().getFileDemand(), ";", "\\s+|-", this.centroids,
                    this.shpLinks, this.shpConnectors, this.settingsNTM, this.getDepartureTimeProfiles(), this.areas));

            Map<String, Area> areasToUse;
            Map<String, NTMNode> centroidsToUse;
            Map<String, NTMLink> shpConnectorsToUse;

            if (this.getInputNTM().COMPRESS_AREAS)
            {
                // to compress the areas into bigger units TODO //of compressedDemand????????????????
                File file = new File(this.getInputNTM().getInputMap() + this.getInputNTM().getFileAreasBig());
                this.compressedAreas = ShapeStore.openGISFile(file);
                this.bigAreas = new LinkedHashMap<String, Area>();
                for (ShapeObject shape : this.compressedAreas.getGeoObjects())
                {
                    String areaName = shape.getValues().get(0);
                    double accCritMaxCapStart = java.lang.Double.parseDouble(shape.getValues().get(4));
                    double accCritMaxCapEnd = java.lang.Double.parseDouble(shape.getValues().get(5));
                    double accCritJam = java.lang.Double.parseDouble(shape.getValues().get(6));
                    ArrayList<java.lang.Double> accCritical = new ArrayList<java.lang.Double>();
                    accCritical.add(accCritMaxCapStart);
                    accCritical.add(accCritMaxCapEnd);
                    accCritical.add(accCritJam);
                    ParametersNTM parametersNTM = new ParametersNTM(accCritical);
                    Point p = shape.getDesignLine().getCentroid();
                    Coordinate centroid = new Coordinate(p.getX(), p.getY());
                    Area bigArea = new Area(shape.getDesignLine(), areaName, "name", "gemeente", "gebied", "regio", 0, centroid,
                            TrafficBehaviourType.NTM, new Length(0, LengthUnit.KILOMETER), new Speed(0, SpeedUnit.KM_PER_HOUR),
                            this.getInputNTM().getScalingFactorDemand(), parametersNTM);
                    this.bigAreas.put(bigArea.getCentroidNr(), bigArea);
                }
                // create new centroids
                this.bigCentroids = new LinkedHashMap<String, NTMNode>();
                // key from small to big areas, and new connectors and new bigCentroids!
                LinkedHashMap<NTMNode, NTMNode> mapSmallAreaToBigArea = connectCentroidsToBigger(this.compressedAreas, this.centroids,
                        this.bigCentroids, this.shpConnectors, this.areas);
                this.setShpBigConnectors(createConnectors(mapSmallAreaToBigArea, this.shpConnectors));
                this.compressedTripDemand =
                        TripDemand.compressTripDemand(this.tripDemand, this.centroids, mapSmallAreaToBigArea);

                shpConnectorsToUse = this.getShpBigConnectors();
                areasToUse = this.getBigAreas();
                centroidsToUse = this.getBigCentroids();
                this.tripDemandToUse = this.getCompressedTripDemand();
            }
            else
            {
                shpConnectorsToUse = this.getShpConnectors();
                areasToUse = this.getAreas();
                centroidsToUse = this.getCentroids();
                this.tripDemandToUse = this.getTripDemand();
            }

            // set the lower values for flow links:
            this.flowLinks =
                    createFlowLinks(this.shpLinks, this.getInputNTM().getMaxSpeed(), this.getInputNTM().getMaxCapacity());

            // merge link segments between junctions on flow links:
            // Link.findSequentialLinks(this.flowLinks, this.nodes);
            // Link.findSequentialLinks(this.shpLinks, this.nodes);

            // save the selected and newly created areas to a shape file: at this position the connector areas are saved
            // also!!!
            // WriteToShp.createShape(this.areas);

            // compute the roadLength within the areas
            determineRoadLengthInAreas(this.shpLinks, areasToUse);

            // build the higher level map and the graph
            BuildGraph.buildGraph(this, areasToUse, centroidsToUse, shpConnectorsToUse);

            // this.areaFlowLinks = createFlowLinkBuffers(this.flowLinks);

            if (this.getInputNTM().COMPRESS_AREAS)
            {
                readOrSetParametersNTM(this, areasToUse, centroidsToUse,
                        this.getInputNTM().getInputMap() + this.getInputNTM().getFileNameParametersNTMBig());

                readOrSetCapacityRestraints(this, areasToUse,
                        this.getInputNTM().getInputMap() + this.getInputNTM().getFileNameCapacityRestraintBig(),
                        this.getInputNTM().getInputMap() + this.getInputNTM().getFileNameCapacityRestraintFactorBig());
            }
            else
            {
                readOrSetParametersNTM(this, areasToUse, centroidsToUse,
                        this.getInputNTM().getInputMap() + this.getInputNTM().getFileNameParametersNTM());

                readOrSetCapacityRestraints(this, areasToUse,
                        this.getInputNTM().getInputMap() + this.getInputNTM().getFileNameCapacityRestraint(),
                        this.getInputNTM().getInputMap() + this.getInputNTM().getFileNameCapacityRestraintFactor());
            }

            WriteOutput.writeInputData(this);

            Routes.createRoutes(this, this.getSettingsNTM().getNumberOfRoutes(), this.getInputNTM().getWeightNewRoutes(),
                    this.getInputNTM().getVarianceRoutes(), true, 1, 999999);

            // in case we run on an animator and not on a simulator, we create the animation
            if (getSimulator() instanceof AnimatorInterface)
            {
                createDynamicAreaAnimation(this);
            }

            // copyInputFiles(this.getInputNTM().getInputMap(), this.getInputNTM().getFileDemand(),
            // this.getInputNTM().getFileProfiles());
            this.simulator.scheduleEventRel(new Duration(0.0, DurationUnit.SECOND), this, this, "ntmFlowTimestep", null);
            // this.simulator.scheduleEventAbs(new Time(1799.99, DurationUnit.SECOND), this, this,
            // "drawGraph", null);
        }
        catch (Throwable exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @param flowLinks2
     * @return
     */
    private Map<String, AreaFlowLink> createFlowLinkBuffers(Map<String, NTMLink> flowLinks)
    {
        // Create new Areas where they are lacking
        /**
         * @param centroid
         * @return the additional areas
         */
        Map<String, Area> areasFlowLink = new LinkedHashMap<String, Area>();

        for (NTMLink link : flowLinks.values())
        {
            LinkCellTransmission linkCTM = (LinkCellTransmission) link;
            char character = 'a';
            for (FlowCell cell : linkCTM.getCells())
            {
                ArrayList<Coordinate> cellPoints = new ArrayList<Coordinate>();
                cellPoints =
                        WriteOutput.retrieveCellXY(linkCTM, cell, linkCTM.getCells().indexOf(cell), linkCTM.getCells().size());
                Coordinate[] coordinates = new Coordinate[3];
                coordinates[0] = cellPoints.get(0);
                coordinates[1] = cellPoints.get(2);
                coordinates[2] = cellPoints.get(1);
                LineString linear = new GeometryFactory().createLineString(coordinates);
                Geometry buffer = linear.buffer(30);
                Point centroidPoint = buffer.getCentroid();
                Coordinate centroid = new Coordinate(centroidPoint.getX(), centroidPoint.getY());
                double dhb = 0.0;
                ParametersNTM parametersNTM = new ParametersNTM();
                AreaFlowLink areaFlowLink = new AreaFlowLink(buffer, "test", "test", "test", "test", "test", dhb, centroid,
                        TrafficBehaviourType.NTM, new Length(0, LengthUnit.METER), new Speed(0, SpeedUnit.KM_PER_HOUR), 1.0,
                        parametersNTM, linkCTM, linkCTM.getCells().indexOf(cell));
                String Id = link.getId() + character;
                areasFlowLink.put(Id, areaFlowLink);
                character++;
            }
        }
        return null;
    }

    /**
     * @param areas Map&lt;String,Area&gt;;
     * @param shpConnectors2
     * @param compressedAreas2
     * @param areas2
     * @return
     * @throws NetworkException
     */
    private LinkedHashMap<NTMNode, NTMNode> connectCentroidsToBigger(ShapeStore compressedAreas, Map<String, NTMNode> centroids,
            Map<String, NTMNode> bigCentroids, Map<String, NTMLink> connectors, Map<String, Area> areas) throws NetworkException
    {
        LinkedHashMap<NTMNode, NTMNode> mapSmallAreaToBigArea = new LinkedHashMap<NTMNode, NTMNode>();
        // ArrayList<Node> centroidsToRemove = new ArrayList<Node>();
        for (ShapeObject bigArea : compressedAreas.getGeoObjects())
        {
            int NTM = 0;
            int CORDON = 0;
            String nr = bigArea.getValues().get(0);
            Point centroidPoint = bigArea.getDesignLine().getCentroid();
            Coordinate centroidCoordinate = new Coordinate(centroidPoint.getX(), centroidPoint.getY());
            NTMNode bigCentroid = new NTMNode(this.network, nr, centroidCoordinate, TrafficBehaviourType.NTM);
            bigCentroids.put(nr, bigCentroid);
            for (NTMNode centroid : centroids.values())
            {
                Coordinate nodeCoordinate = centroid.getPoint().getCoordinate();
                Geometry nodeGeometry = new GeometryFactory().createPoint(nodeCoordinate);
                if (bigArea.getDesignLine().covers(nodeGeometry))
                {
                    mapSmallAreaToBigArea.put(centroid, bigCentroid);
                    if (centroid.getBehaviourType() == TrafficBehaviourType.CORDON)
                    {
                        CORDON++;
                    }
                    else
                    {
                        NTM++;
                    }
                }
            }
            if (CORDON > NTM)
            {
                bigCentroid.setBehaviourType(TrafficBehaviourType.CORDON);
            }
        }
        return mapSmallAreaToBigArea;
    }

    /**
     * @param model NTMModel;
     * @param areasToUse Map&lt;String,Area&gt;;
     * @param centroidsToUse Map&lt;String,NTMNode&gt;;
     * @param file String;
     * @throws ParseException
     * @throws IOException
     */
    public void readOrSetParametersNTM(NTMModel model, Map<String, Area> areasToUse, Map<String, NTMNode> centroidsToUse,
            String file) throws IOException, ParseException
    {
        LinkedHashMap<String, ArrayList<java.lang.Double>> parametersNTMByCentroid = CsvFileReader.readParametersNTM(file, ";", ",");
        if (parametersNTMByCentroid.isEmpty())
        {
            CsvFileWriter.writeParametersNTM(this, file);
            parametersNTMByCentroid = CsvFileReader.readParametersNTM(file, ";", ",");
        }

        for (NTMNode node : centroidsToUse.values())
        {
            ParametersNTM parametersNTM = null;
            ArrayList<java.lang.Double> parameters = parametersNTMByCentroid.get(node.getId());
            if (node.getBehaviourType() == TrafficBehaviourType.NTM)
            {
                BoundedNode boundedNode = (BoundedNode) model.nodeAreaGraphMap.get(node.getId());
                if (boundedNode == null)
                {
                    System.out.println("not found " + node.getId());
                }
                else
                {
                    CellBehaviourNTM cellBehaviourNTM = (CellBehaviourNTM) boundedNode.getCellBehaviour();
                    if (parameters != null)
                    {
                        double capacity = 0.0;
                        if (parameters.get(parameters.size() - 1) > 0)
                        {
                            capacity = parameters.get(parameters.size() - 1);
                        }
                        else
                        {
                            capacity = cellBehaviourNTM.getParametersNTM().getCapacity().getInUnit(FrequencyUnit.PER_HOUR);
                        }
                        // capacity = 9000;
                        parameters.remove(parameters.size() - 1);
                        parametersNTM = new ParametersNTM(parameters, capacity, areasToUse.get(node.getId()).getRoadLength());
                    }
                    else
                    {
                        parametersNTM = new ParametersNTM(areasToUse.get(node.getId()).getAverageSpeed(),
                                areasToUse.get(node.getId()).getRoadLength());
                    }
                    cellBehaviourNTM.setParametersNTM(parametersNTM);
                }
            }

        }
    }

    /**
     * @param model NTMModel;
     * @param areasToUse Map&lt;String,Area&gt;;
     * @param file String;
     * @param fileFactor String;
     * @throws IOException
     * @throws ParseException
     */
    public void readOrSetCapacityRestraints(NTMModel model, Map<String, Area> areasToUse, String file, String fileFactor)
            throws IOException, ParseException
    {
        LinkedHashMap<String, LinkedHashMap<String, Frequency>> borderCapacityAreasMap = CsvFileReader.readCapResNTM(file, ";", ",");
        LinkedHashMap<String, LinkedHashMap<String, java.lang.Double>> borderCapacityFactorAreasMap =
                CsvFileReader.readCapResFactorNTM(fileFactor, ";", ",");

        if (borderCapacityAreasMap.isEmpty())
        {
            CsvFileWriter.writeCapresNTM(this, file, 0.0);
            borderCapacityAreasMap = CsvFileReader.readCapResNTM(file, ";", ",");
        }

        if (borderCapacityFactorAreasMap.isEmpty())
        {
            CsvFileWriter.writeCapresNTM(this, fileFactor, 1.0);
            borderCapacityFactorAreasMap = CsvFileReader.readCapResFactorNTM(fileFactor, ";", ",");
        }

        // set the border capacity
        for (NTMNode origin : model.getAreaGraph().vertexSet())
        {
            if (origin.getBehaviourType() == TrafficBehaviourType.NTM)
            {
                LinkedHashMap<BoundedNode, Frequency> borderCapacity = new LinkedHashMap<BoundedNode, Frequency>();
                BoundedNode node = (BoundedNode) origin;
                CellBehaviourNTM cellBehaviour = (CellBehaviourNTM) node.getCellBehaviour();
                Set<LinkEdge<NTMLink>> outGoing = this.getAreaGraph().outgoingEdgesOf(node);
                for (LinkEdge<NTMLink> link : outGoing)
                {
                    double factor = 1.0;
                    NTMNode neighbourNode = (NTMNode) link.getLink().getEndNode();
                    BoundedNode graphEndNode = (BoundedNode) this.getNodeAreaGraphMap().get(neighbourNode.getId());
                    if (!borderCapacityAreasMap.isEmpty())
                    {
                        if (borderCapacityAreasMap.get(origin.getId()) == null)
                        {
                            System.out.println("NT");
                        }
                        if (!borderCapacityFactorAreasMap.isEmpty())
                        {
                            if (borderCapacityFactorAreasMap.get(origin.getId()) == null)
                            {
                                System.out.println("No capres factor");
                            }
                            else
                            {
                                factor = borderCapacityFactorAreasMap.get(origin.getId()).get(graphEndNode.getId())
                                        .doubleValue();
                            }
                        }
                        double capacity = borderCapacityAreasMap.get(origin.getId()).get(graphEndNode.getId())
                                .getInUnit(FrequencyUnit.PER_HOUR);
                        capacity *= factor;
                        Frequency cap = new Frequency(capacity, FrequencyUnit.PER_HOUR);
                        borderCapacity.put(graphEndNode, cap);
                    }
                    else
                    {
                        Frequency cap = new Frequency(99999.0, FrequencyUnit.PER_HOUR);
                        borderCapacity.put(graphEndNode, cap);
                    }
                }
                cellBehaviour.setBorderCapacity(borderCapacity);
            }
        }
    }

    /**
     * @param areas
     * @param shpConnectors2
     * @param compressedAreas2
     * @param areas2
     * @return
     * @throws NetworkException
     */
    private Map<String, NTMLink> createConnectors(HashMap<NTMNode, NTMNode> mapSmallAreaToBigArea,
            Map<String, NTMLink> connectors) throws NetworkException
    {
        LinkedHashMap<String, NTMLink> mapConnectors = new LinkedHashMap<String, NTMLink>();
        for (NTMLink link : connectors.values())
        {
            NTMNode startNode = null;
            NTMNode endNode = null;
            NTMLink newConnector = null;

            if (mapSmallAreaToBigArea.containsKey(link.getStartNode()))
            {
                startNode = mapSmallAreaToBigArea.get(link.getStartNode());
            }
            if (mapSmallAreaToBigArea.containsKey(link.getEndNode()))
            {
                endNode = mapSmallAreaToBigArea.get(link.getEndNode());
            }
            if (startNode != null)
            {
                newConnector = new NTMLink(this.network, this.getSimulator(), link.getDesignLine(), link.getId(),
                        link.getLength(), startNode, (NTMNode) link.getEndNode(), link.getFreeSpeed(), null, link.getCapacity(),
                        link.getBehaviourType(), link.getLinkData());
            }
            else if (endNode != null)
            {
                newConnector = new NTMLink(this.network, this.getSimulator(), link.getDesignLine(), link.getId(),
                        link.getLength(), (NTMNode) link.getStartNode(), endNode, link.getFreeSpeed(), null, link.getCapacity(),
                        link.getBehaviourType(), link.getLinkData());
            }
            else
            {
                newConnector = new NTMLink(link);
            }
            mapConnectors.put(link.getId(), newConnector);
        }
        return mapConnectors;
    }

    /**
     * @param shpLinks2
     * @param areas2
     */
    private void determineRoadLengthInAreas(Map<String, NTMLink> shpLinks, Map<String, Area> areas)
    {
        Double speedTotal;
        Map<Area, java.lang.Double> speedTotalByArea = new LinkedHashMap<Area, java.lang.Double>();
        for (NTMLink link : shpLinks.values())
        {
            for (Area area : areas.values())
            {
                if (area.getGeometry().intersects(link.getDesignLine().getLineString()))
                {
                    double covers = 0.5;
                    if (area.getGeometry().contains(link.getDesignLine().getLineString()))
                    {
                        covers = 1;
                    }
                    double length = covers * link.getLength().getInUnit(LengthUnit.KILOMETER) * link.getNumberOfLanes();
                    Length laneLength = new Length(length, LengthUnit.KILOMETER);
                    area.addRoadLength(laneLength);
                    // in SI (m*m/s)
                    java.lang.Double speedLaneLength =
                            new java.lang.Double(link.getFreeSpeed().getInUnit(SpeedUnit.KM_PER_HOUR) * length);
                    if (speedTotalByArea.get(area) != null)
                    {
                        speedLaneLength += speedTotalByArea.get(area);
                    }
                    speedTotalByArea.put(area, speedLaneLength);
                }
            }
        }
        for (Area area : areas.values())
        {
            if (speedTotalByArea.get(area) != null && area.getRoadLength() != null)
            {
                double averageSpeed = speedTotalByArea.get(area) / area.getRoadLength().getInUnit(LengthUnit.KILOMETER);
                area.setAverageSpeed(new Speed(averageSpeed, SpeedUnit.KM_PER_HOUR));
            }
            else
            {
                System.out.println("FlowLink Area: nodeNumber " + area.getCentroidNr());
                area.setRoadLength(new Length(java.lang.Double.POSITIVE_INFINITY, LengthUnit.KILOMETER));
                double averageSpeed = 100;
                area.setAverageSpeed(new Speed(averageSpeed, SpeedUnit.KM_PER_HOUR));
            }
        }

    }

    /**
     * @param areas Map&lt;String,Area&gt;; set areas.
     */
    public void setAreas(Map<String, Area> areas)
    {
        this.areas = areas;
    }

    /**
     * @param nodes Map&lt;String,NTMNode&gt;; set nodes.
     */
    public void setNodes(Map<String, NTMNode> nodes)
    {
        this.nodes = nodes;
    }

    /**
     * @param shpLinks Map&lt;String,NTMLink&gt;; set shpLinks.
     */
    public void setShpLinks(Map<String, NTMLink> shpLinks)
    {
        this.shpLinks = shpLinks;
    }

    /**
     * @param centroids Map&lt;String,NTMNode&gt;; set centroids.
     */
    public void setCentroids(Map<String, NTMNode> centroids)
    {
        this.centroids = centroids;
    }

    /**
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected final void ntmFlowTimestep() throws Exception
    {
        NTMsimulation.simulate(this);

        if (this.getSettingsNTM().getTimeStepDurationNTM().getInUnit(DurationUnit.SECOND) * NTMsimulation.steps
                % this.getSettingsNTM().getReRouteTimeInterval().getInUnit(DurationUnit.SECOND) == 0)
        {
            // in case we run on an animator and not on a simulator, we create the animation
            if (this.simulator instanceof AnimatorInterface)
            {
                if (this.getInputNTM().isPaint())
                {
                    createDynamicAreaAnimation(this);
                }
            }
        }
        try
        {
            // start this method again
            this.simulator.scheduleEventRel(this.settingsNTM.getTimeStepDurationNTM(), this, this, "ntmFlowTimestep", null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Make the animation for each of the components that we want to see on the screen.
     */
    private void createAnimation()

    {
        try
        {
            // let's make several layers with the different types of information
            boolean showLinks = false;
            boolean showFlowLinks = true;
            boolean showConnectors = true;
            boolean showNodes = true;
            boolean showGraphEdges = true;
            boolean showAreaNode = true;
            boolean showArea = false;

            if (showArea)
            {
                for (Area area : this.areas.values())
                {
                    new AreaAnimation(area, this.simulator, 5f);
                }
            }
            if (showLinks)
            {
                for (LinkEdge<NTMLink> shpLink : this.linkGraph.edgeSet())
                {
                    new ShpLinkAnimation(shpLink.getLink(), this.simulator, 2.0F, Color.RED);
                }
            }
            if (showConnectors)
            {
                for (NTMLink shpConnector : this.shpConnectors.values())
                {
                    new ShpLinkAnimation(shpConnector, this.simulator, 5.0F, Color.BLUE);
                }
            }

            if (showFlowLinks)
            {
                for (NTMLink flowLink : this.flowLinks.values())
                {
                    new ShpLinkAnimation(flowLink, this.simulator, 10.0F, Color.RED);
                }
            }
            if (showNodes)
            {
                for (NTMNode Node : this.nodes.values())
                {
                    new ShpNodeAnimation(Node, this.simulator);
                }
            }
            // for (LinkEdge<Link> linkEdge : this.linkGraph.edgeSet())
            // {
            // new LinkAnimation(linkEdge.getEdge(), this.simulator, 0.5f);
            // }
            if (showGraphEdges)
            {
                for (LinkEdge<NTMLink> linkEdge : this.areaGraph.edgeSet())
                {
                    new ShpLinkAnimation(linkEdge.getLink(), this.simulator, 5f, Color.BLACK);
                }
            }
            if (showAreaNode)
            {
                for (NTMNode node : this.areaGraph.vertexSet())
                {
                    new NodeAnimation(node, this.simulator);
                }
            }
        }
        catch (NamingException | RemoteException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Make the animation for each of the components that we want to see on the screen.
     */
    static void createDynamicAreaAnimation(NTMModel model)

    {
        try
        {
            // let's make several layers with the different types of information
            boolean showLinks = false;
            boolean showFlowLinks = true;
            boolean showConnectors = false;
            boolean showNodes = false;
            boolean showEdges = false;
            boolean showAreaNode = false;
            boolean showArea = true;

            if (showArea)
            {
                if (model.getInputNTM().COMPRESS_AREAS)
                {
                    for (Area area : model.bigAreas.values())
                    {
                        if (area.getTrafficBehaviourType() == TrafficBehaviourType.NTM)
                        {
                            new AreaAnimation(area, model.simulator, 4f);
                        }
                    }
                }
                else
                {
                    for (Area area : model.areas.values())
                    {
                        if (area.getTrafficBehaviourType() == TrafficBehaviourType.NTM)
                        {
                            new AreaAnimation(area, model.simulator, 4f);
                        }
                    }
                }

                /*
                 * for (AreaFlowLink areaFlowLinks : model.areaFlowLinks.values()) { new AreaFlowLinkAnimation(areaFlowLinks,
                 * model.simulator, 4f); }
                 */

            }
            if (showLinks)
            {
                for (NTMLink shpLink : model.shpLinks.values())
                {
                    new ShpLinkAnimation(shpLink, model.simulator, 6.0F, Color.GRAY);
                }
            }
            if (showConnectors)
            {
                for (NTMLink shpConnector : model.shpConnectors.values())
                {
                    new ShpLinkAnimation(shpConnector, model.simulator, 5.0F, Color.BLUE);
                }
            }

            if (showFlowLinks)
            {
                for (NTMLink flowLink : model.flowLinks.values())
                {
                    new ShpLinkAnimation(flowLink, model.simulator, 2.0F, Color.RED);
                }
            }
            if (showNodes)
            {
                for (NTMNode Node : model.nodes.values())
                {
                    new ShpNodeAnimation(Node, model.simulator);
                }
            }
            // for (LinkEdge<Link> linkEdge : model.linkGraph.edgeSet())
            // {
            // new LinkAnimation(linkEdge.getEdge(), model.simulator, 0.5f);
            // }
            if (showEdges)
            {
                for (LinkEdge<NTMLink> linkEdge : model.areaGraph.edgeSet())
                {
                    new ShpLinkAnimation(linkEdge.getLink(), model.simulator, 3f, Color.BLACK);
                }
            }
            if (showAreaNode)
            {
                for (NTMNode node : model.areaGraph.vertexSet())
                {
                    new NodeAnimation(node, model.simulator);
                }
            }
        }
        catch (NamingException | RemoteException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @return settingsNTM.
     */
    public final NTMSettings getSettingsNTM()
    {
        return this.settingsNTM;
    }

    /**
     * @param settingsNTM NTMSettings; set settingsNTM.
     */
    public final void setSettingsNTM(final NTMSettings settingsNTM)
    {
        this.settingsNTM = settingsNTM;
    }

    /**
     * @return tripDemand.
     */
    public final TripDemand getTripDemand()
    {
        return this.tripDemand;
    }

    /**
     * @param tripDemand TripDemand; set tripDemand.
     */
    public final void setTripDemand(TripDemand tripDemand)
    {
        this.tripDemand = tripDemand;
    }

    /**
     * @return departureTimeProfiles.
     */
    public final ArrayList<DepartureTimeProfile> getDepartureTimeProfiles()
    {
        return this.departureTimeProfiles;
    }

    /**
     * @param departureTimeProfiles ArrayList&lt;DepartureTimeProfile&gt;; set departureTimeProfiles.
     */
    public final void setDepartureTimeProfiles(final ArrayList<DepartureTimeProfile> departureTimeProfiles)
    {
        this.departureTimeProfiles = departureTimeProfiles;
    }

    /**
     * @return flowLinks.
     */
    public final Map<String, NTMLink> getFlowLinks()
    {
        return this.flowLinks;
    }

    /**
     * @param flowLinks Map&lt;String,NTMLink&gt;; set flowLinks.
     */
    public final void setFlowLinks(final Map<String, NTMLink> flowLinks)
    {
        this.flowLinks = flowLinks;
    }

    /**
     * @return areaFlowLink.
     */
    public Map<String, AreaFlowLink> getAreaFlowLinks()
    {
        return this.areaFlowLinks;
    }

    /**
     * @param areaFlowLink set areaFlowLink.
     */
    public void setAreaFlowLinks(Map<String, AreaFlowLink> areaFlowLinks)
    {
        this.areaFlowLinks = areaFlowLinks;
    }

    /**
     * @return areaGraph.
     */
    public final SimpleDirectedWeightedGraph<NTMNode, LinkEdge<NTMLink>> getAreaGraph()
    {
        return this.areaGraph;
    }

    /**
     * @return shpConnectors.
     */
    public final Map<String, NTMLink> getShpConnectors()
    {
        return this.shpConnectors;
    }

    /**
     * @param shpConnectors Map&lt;String,NTMLink&gt;; set shpConnectors.
     */
    public final void setShpConnectors(final Map<String, NTMLink> shpConnectors)
    {
        this.shpConnectors = shpConnectors;
    }

    /**
     * @return shpBigConnectors.
     */
    public Map<String, NTMLink> getShpBigConnectors()
    {
        return this.shpBigConnectors;
    }

    /**
     * @param shpBigConnectors Map&lt;String,NTMLink&gt;; set shpBigConnectors.
     */
    public void setShpBigConnectors(Map<String, NTMLink> shpBigConnectors)
    {
        this.shpBigConnectors = shpBigConnectors;
    }

    /**
     * @return areas.
     */
    public final Map<String, Area> getAreas()
    {
        return this.areas;
    }

    /**
     * @return bigAreas.
     */
    public Map<String, Area> getBigAreas()
    {
        return this.bigAreas;
    }

    /**
     * @param bigAreas Map&lt;String,Area&gt;; set bigAreas.
     */
    public void setBigAreas(Map<String, Area> bigAreas)
    {
        this.bigAreas = bigAreas;
    }

    /**
     * @return nodes.
     */
    public final Map<String, NTMNode> getNodes()
    {
        return this.nodes;
    }

    /**
     * @return shpLinks.
     */
    public final Map<String, NTMLink> getShpLinks()
    {
        return this.shpLinks;
    }

    /**
     * @return centroids.
     */
    public final Map<String, NTMNode> getCentroids()
    {
        return this.centroids;
    }

    /**
     * @return linkGraph.
     */
    public final SimpleDirectedWeightedGraph<NTMNode, LinkEdge<NTMLink>> getLinkGraph()
    {
        return this.linkGraph;
    }

    /**
     * Links that show typical highway or mainroad behaviour are specified explicitly as roads.
     * @param shpLinks Map&lt;String,NTMLink&gt;; the links of this model
     * @return the flowLinks
     * @throws NetworkException
     */
    public static Map<String, NTMLink> createFlowLinks(final Map<String, NTMLink> shpLinks, Speed maxSpeed,
            Frequency maxCapacity) throws NetworkException
    {
        Map<String, NTMLink> flowLinks = new LinkedHashMap<String, NTMLink>();
        for (NTMLink shpLink : shpLinks.values())
        {

            if (shpLink.getFreeSpeed().doubleValue() >= maxSpeed.doubleValue()
                    && shpLink.getCapacity().doubleValue() > maxCapacity.doubleValue())
            {
                NTMLink flowLink = new NTMLink(shpLink);
                if (flowLink.getDesignLine() == null)
                {
                    System.out.println("NTMModel line 694 ... no geometry");
                }
                flowLink.setBehaviourType(TrafficBehaviourType.FLOW);
                ((NTMNode) flowLink.getStartNode()).setBehaviourType(TrafficBehaviourType.FLOW);
                ((NTMNode) flowLink.getEndNode()).setBehaviourType(TrafficBehaviourType.FLOW);
                flowLinks.put(flowLink.getId(), flowLink);
            }
        }

        for (NTMLink flowLink : flowLinks.values())
        {

            if (flowLink.getFreeSpeed().doubleValue() >= maxSpeed.doubleValue()
                    && flowLink.getCapacity().doubleValue() > maxCapacity.doubleValue())
            {
                shpLinks.remove(flowLink.getId());
            }
        }

        return flowLinks;
    }

    public void copyInputFiles(String path, String fileDemand, String fileProfiles) throws IOException
    {
        // copy input files to the output map
        File dir = new File(this.getInputNTM().getOutputMap());
        if (!dir.exists())
        {
            boolean result = false;

            try
            {
                dir.mkdir();
                result = true;
            }
            catch (SecurityException se)
            {
                // handle it
            }
            if (result)
            {
                System.out.println("DIR created");
            }
        }
        Path from = Paths.get(this.getInputNTM().getInputMap() + "/" + fileProfiles);
        Path to = Paths.get(this.getInputNTM().getInputMap() + this.getInputNTM().getOutputMap() + "/" + fileProfiles);
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        from = Paths.get(this.getInputNTM().getInputMap() + fileDemand);
        to = Paths.get(this.getInputNTM().getInputMap() + this.getInputNTM().getOutputMap() + fileDemand);
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * @return compressedAreas.
     */
    public ShapeStore getCompressedAreas()
    {
        return this.compressedAreas;
    }

    /**
     * @param compressedAreas ShapeStore; set compressedAreas.
     */
    public void setCompressedAreas(ShapeStore compressedAreas)
    {
        this.compressedAreas = compressedAreas;
    }

    /**
     * @return bigCentroids.
     */
    public Map<String, NTMNode> getBigCentroids()
    {
        return this.bigCentroids;
    }

    /**
     * @param bigCentroids Map&lt;String,NTMNode&gt;; set bigCentroids.
     */
    public void setBigCentroids(Map<String, NTMNode> bigCentroids)
    {
        this.bigCentroids = bigCentroids;
    }

    /**
     * @return compressedTripDemand.
     */
    public TripDemand<TripInfoTimeDynamic> getCompressedTripDemand()
    {
        return this.compressedTripDemand;
    }

    /**
     * @param compressedTripDemand TripDemand&lt;TripInfoTimeDynamic&gt;; set compressedTripDemand.
     */
    public void setCompressedTripDemand(TripDemand<TripInfoTimeDynamic> compressedTripDemand)
    {
        this.compressedTripDemand = compressedTripDemand;
    }

    /**
     * @return debugLinkList.
     */
    public Map<String, NTMLink> getDebugLinkList()
    {
        return this.debugLinkList;
    }

    /**
     * @param debugLinkList LinkedHashMap&lt;String,NTMLink&gt;; set debugLinkList.
     */
    public void setDebugLinkList(LinkedHashMap<String, NTMLink> debugLinkList)
    {
        this.debugLinkList = debugLinkList;
    }

    /**
     * @return nodeAreaGraphMap.
     */
    public Map<String, NTMNode> getNodeAreaGraphMap()
    {
        return this.nodeAreaGraphMap;
    }

    /**
     * @param nodeAreaGraphMap Map&lt;String,NTMNode&gt;; set nodeAreaGraphMap.
     */
    public void setNodeAreaGraphMap(Map<String, NTMNode> nodeAreaGraphMap)
    {
        this.nodeAreaGraphMap = nodeAreaGraphMap;
    }

    /**
     * @return inputNTM.
     */
    public InputNTM getInputNTM()
    {
        return this.inputNTM;
    }

    /**
     * @param inputNTM InputNTM; set inputNTM.
     */
    public void setInputNTM(InputNTM inputNTM)
    {
        this.inputNTM = inputNTM;
    }

    /** {@inheritDoc} */
    @Override
    public OTSNetwork getNetwork()
    {
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "NTMModel";
    }

    /*
     * // Create new Areas around highways, that show different behaviour
     *//**
        * @param shpLinks the links of this model
        * @param areas the intial areas
        * @return the additional areas
        */
    /*
     * public static Map<String, AreaNTM> createCordonFeederAreas(final Map<String, ShpLink> shpLinks, final Map<String,
     * AreaNTM> areas) { for (ShpLink shpLink : shpLinks.values()) { if (shpLink.getSpeed() > 70 && shpLink.getCapacity() >
     * 3400) { Geometry buffer = shpLink.getDesignLine().buffer(10); Point centroid = buffer.getCentroid(); String nr =
     * shpLink.getNr(); String name = shpLink.getName(); String gemeente = shpLink.getName(); String gebied = shpLink.getName();
     * String regio = shpLink.getName(); double dhb = 0.0; AreaNTM area = new AreaNTM(buffer, nr, name, gemeente, gebied, regio,
     * dhb, centroid); areas.put(nr, area); } } return areas; }
     */

}
