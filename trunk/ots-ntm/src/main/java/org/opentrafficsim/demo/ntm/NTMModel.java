package org.opentrafficsim.demo.ntm;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
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

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.opentrafficsim.core.dsol.OTSAnimatorInterface;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.network.LinkEdge;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.demo.ntm.Node.TrafficBehaviourType;
import org.opentrafficsim.demo.ntm.animation.AreaAnimation;
import org.opentrafficsim.demo.ntm.animation.NodeAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpLinkAnimation;
import org.opentrafficsim.demo.ntm.animation.ShpNodeAnimation;
import org.opentrafficsim.demo.ntm.shapeobjects.ShapeObject;
import org.opentrafficsim.demo.ntm.shapeobjects.ShapeStore;
import org.opentrafficsim.demo.ntm.trafficdemand.DepartureTimeProfile;
import org.opentrafficsim.demo.ntm.trafficdemand.TripDemand;
import org.opentrafficsim.demo.ntm.trafficdemand.TripInfoTimeDynamic;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionSep 9, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class NTMModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** detailed areas from the traffic model. */
    private Map<String, Area> areas;

    /** detailed areas from the traffic model. */
    private Map<String, Area> bigAreas;

    /** rougher areas from the traffic model. */
    private ShapeStore compressedAreas;

    /** nodes from shape file. */
    private Map<String, Node> nodes;

    /** connectors from shape file. */
    private Map<String, Link> shpConnectors;

    /** connectors from shape file. */
    private Map<String, Link> shpBigConnectors;

    /** links from shape file. */
    private Map<String, Link> shpLinks;

    /** subset of links from shape file used as flow links. */
    private Map<String, Link> flowLinks;

    /** detailed areas from the traffic model. */
    private Map<String, AreaFlowLink> areaFlowLinks;

    /** the centroids. */
    private Map<String, Node> centroids;

    /** the centroids. */
    private Map<String, Node> bigCentroids;

    /** the demand of trips by Origin and Destination. */
    private TripDemand<TripInfoTimeDynamic> tripDemand;

    /** the compressed demand of trips by Origin and Destination. */
    private TripDemand<TripInfoTimeDynamic> compressedTripDemand;

    /** the demand of trips by Origin and Destination for simulation. */
    TripDemand<TripInfoTimeDynamic> tripDemandToUse;

    /** */
    private InputNTM inputNTM;

    /** The simulation settings. */
    private NTMSettings settingsNTM;

    /** profiles with fractions of total demand. */
    private ArrayList<DepartureTimeProfile> departureTimeProfiles;

    /** graph containing the original network. */
    private SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> linkGraph;

    /** graph containing the simplified network. */
    private SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> areaGraph;

    /** */
    private Map<String, Node> nodeAreaGraphMap = new HashMap<>();

    /** subset of links from shape file used as flow links. */
    private LinkedHashMap<String, Link> debugLinkList;

    /**
     * Constructor to make the graphs with the right type.
     */
    @SuppressWarnings("unchecked")
    public NTMModel()
    {
        LinkEdge<Link> l = new LinkEdge<Link>(null);
        this.linkGraph =
                new SimpleDirectedWeightedGraph<Node, LinkEdge<Link>>((Class<? extends LinkEdge<Link>>) l.getClass());
        this.areaGraph =
                new SimpleDirectedWeightedGraph<Node, LinkEdge<Link>>((Class<? extends LinkEdge<Link>>) l.getClass());
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel(
            final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> _simulator)
    {
        this.simulator = (OTSDEVSSimulatorInterface) _simulator;
        try
        {
            // create the output base maps

            // boolean DEBUG = true;
            // set the time step value at ten seconds;
            DoubleScalar.Rel<TimeUnit> timeStepNTM = new DoubleScalar.Rel<TimeUnit>(10, TimeUnit.SECOND);
            DoubleScalar.Rel<TimeUnit> timeStepCellTransmissionModel =
                    new DoubleScalar.Rel<TimeUnit>(10, TimeUnit.SECOND);
            Rel<TimeUnit> durationOfSimulation = new DoubleScalar.Rel<TimeUnit>(10800, TimeUnit.SECOND);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
            Calendar startTime = new GregorianCalendar(2014, 1, 28, 15, 0, 0);
            this.shpLinks = new HashMap<>();
            this.shpConnectors = new HashMap<>();

            this.centroids =
                    ShapeFileReader.ReadNodes(this.getInputNTM().getInputMap() + this.getInputNTM().getFileCentroids(),
                            "NODENR", this.getInputNTM().isReturnCentroidsCentroid(), this.getInputNTM()
                                    .isOnlyCentroidsFileCentroid());
            this.nodes =
                    ShapeFileReader.ReadNodes(this.getInputNTM().getInputMap() + this.getInputNTM().getFileNodes(),
                            "NODENR", this.getInputNTM().isReturnCentroidsNode(), this.getInputNTM()
                                    .isOnlyCentroidsFileNode());
            this.areas =
                    ShapeFileReader.readAreas(this.getInputNTM().getInputMap() + this.getInputNTM().getFileAreas(),
                            this.centroids, this.getInputNTM().getScalingFactorDemand());

            ShapeFileReader.readLinks(this.getInputNTM().getInputMap() + this.getInputNTM().getFileLinks(),
                    this.shpLinks, this.shpConnectors, this.nodes, this.centroids, this.getInputNTM()
                            .getLengthUnitLink(), this.getInputNTM().getLinkCapacityNumberOfHours());

            if (this.getInputNTM().getFileFeederLinks() != null)
            {
                if (!this.getInputNTM().getFileFeederLinks().isEmpty())
                {
                    ShapeFileReader.readLinks(this.getInputNTM().getInputMap()
                            + this.getInputNTM().getFileFeederLinks(), this.shpLinks, this.shpConnectors, this.nodes,
                            this.centroids, this.getInputNTM().getLengthUnitLink(), this.getInputNTM()
                                    .getLinkCapacityNumberOfHours());
                }
            }
            this.setDepartureTimeProfiles(CsvFileReader.readDepartureTimeProfiles(this.getInputNTM().getInputMap()
                    + this.getInputNTM().getFileProfiles(), ";", "\\s+"));

            this.getInputNTM().setOutputMap("/output" + this.getInputNTM().getVariantNumber());

            this.settingsNTM =
                    new NTMSettings(startTime, durationOfSimulation, " NTM The Hague ", timeStepNTM,
                            timeStepCellTransmissionModel, this.getInputNTM().getReRouteTimeInterval(), this
                                    .getInputNTM().getNumberOfRoutes(), this.getInputNTM().getWeightNewRoutes(), this
                                    .getInputNTM().getVarianceRoutes(), this.getInputNTM().isReRoute(), this
                                    .getInputNTM().getInputMap(), this.getInputNTM().isIncreaseDemandAreaByFactor(),
                            this.getInputNTM().getScalingFactorDemand());

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
            this.setTripDemand(CsvFileReader.readOmnitransExportDemand(this.getInputNTM().getInputMap()
                    + this.getInputNTM().getFileDemand(), ";", "\\s+|-", this.centroids, this.shpLinks,
                    this.shpConnectors, this.settingsNTM, this.getDepartureTimeProfiles(), this.areas));

            Map<String, Area> areasToUse;
            Map<String, Node> centroidsToUse;
            Map<String, Link> shpConnectorsToUse;

            if (this.getInputNTM().COMPRESS_AREAS)
            {
                // to compress the areas into bigger units TODO //of compressedDemand????????????????
                File file = new File(this.getInputNTM().getInputMap() + this.getInputNTM().getFileAreasBig());
                this.compressedAreas = ShapeStore.openGISFile(file);
                this.bigAreas = new HashMap<String, Area>();
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
                    Point p = shape.getGeometry().getCentroid();
                    Coordinate centroid = new Coordinate(p.getX(), p.getY());
                    Area bigArea =
                            new Area(shape.getGeometry(), areaName, "name", "gemeente", "gebied", "regio", 0, centroid,
                                    TrafficBehaviourType.NTM, new Rel<LengthUnit>(0, LengthUnit.KILOMETER),
                                    new Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR), this.getInputNTM()
                                            .getScalingFactorDemand(), parametersNTM);
                    this.bigAreas.put(bigArea.getCentroidNr(), bigArea);
                }
                // create new centroids
                this.bigCentroids = new HashMap<String, Node>();
                // key from small to big areas, and new connectors and new bigCentroids!
                HashMap<Node, Node> mapSmallAreaToBigArea =
                        connectCentroidsToBigger(this.compressedAreas, this.centroids, this.bigCentroids,
                                this.shpConnectors, this.areas);
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
                    createFlowLinks(this.shpLinks, this.getInputNTM().getMaxSpeed(), this.getInputNTM()
                            .getMaxCapacity());

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
                readOrSetParametersNTM(this, areasToUse, centroidsToUse, this.getInputNTM().getInputMap()
                        + this.getInputNTM().getFileNameParametersNTMBig());

                readOrSetCapacityRestraints(this, areasToUse, this.getInputNTM().getInputMap()
                        + this.getInputNTM().getFileNameCapacityRestraintBig(), this.getInputNTM().getInputMap()
                        + this.getInputNTM().getFileNameCapacityRestraintFactorBig());
            }
            else
            {
                readOrSetParametersNTM(this, areasToUse, centroidsToUse, this.getInputNTM().getInputMap()
                        + this.getInputNTM().getFileNameParametersNTM());

                readOrSetCapacityRestraints(this, areasToUse, this.getInputNTM().getInputMap()
                        + this.getInputNTM().getFileNameCapacityRestraint(), this.getInputNTM().getInputMap()
                        + this.getInputNTM().getFileNameCapacityRestraintFactor());
            }

            WriteOutput.writeInputData(this);

            Routes.createRoutes(this, this.getSettingsNTM().getNumberOfRoutes(), this.getInputNTM()
                    .getWeightNewRoutes(), this.getInputNTM().getVarianceRoutes(), true, 1, 999999);

            // in case we run on an animator and not on a simulator, we create the animation
            if (_simulator instanceof OTSAnimatorInterface)
            {
                createDynamicAreaAnimation(this);
            }

            // copyInputFiles(this.getInputNTM().getInputMap(), this.getInputNTM().getFileDemand(),
            // this.getInputNTM().getFileProfiles());
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), this, this,
                    "ntmFlowTimestep", null);
            // this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(1799.99, TimeUnit.SECOND), this, this,
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
    private Map<String, AreaFlowLink> createFlowLinkBuffers(Map<String, Link> flowLinks)
    {
        // Create new Areas where they are lacking
        /**
         * @param centroid
         * @return the additional areas
         */
        Map<String, Area> areasFlowLink = new HashMap<String, Area>();

        for (Link link : flowLinks.values())
        {
            LinkCellTransmission linkCTM = (LinkCellTransmission) link;
            char character = 'a';
            for (FlowCell cell : linkCTM.getCells())
            {
                ArrayList<Coordinate> cellPoints = new ArrayList<Coordinate>();
                cellPoints =
                        WriteOutput.retrieveCellXY(linkCTM, cell, linkCTM.getCells().indexOf(cell), linkCTM.getCells()
                                .size());
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
                AreaFlowLink areaFlowLink =
                        new AreaFlowLink(buffer, "test", "test", "test", "test", "test", dhb, centroid,
                                TrafficBehaviourType.NTM, new Rel<LengthUnit>(0, LengthUnit.METER), new Abs<SpeedUnit>(
                                        0, SpeedUnit.KM_PER_HOUR), 1.0, parametersNTM, linkCTM, linkCTM.getCells()
                                        .indexOf(cell));
                String Id = link.getId() + character;
                areasFlowLink.put(Id, areaFlowLink);
                character++;
            }
        }
        return null;
    }

    /**
     * @param areas
     * @param shpConnectors2
     * @param compressedAreas2
     * @param areas2
     * @return
     */
    private HashMap<Node, Node> connectCentroidsToBigger(ShapeStore compressedAreas, Map<String, Node> centroids,
            Map<String, Node> bigCentroids, Map<String, Link> connectors, Map<String, Area> areas)
    {
        HashMap<Node, Node> mapSmallAreaToBigArea = new HashMap<Node, Node>();
        // ArrayList<Node> centroidsToRemove = new ArrayList<Node>();
        for (ShapeObject bigArea : compressedAreas.getGeoObjects())
        {
            int NTM = 0;
            int CORDON = 0;
            String nr = bigArea.getValues().get(0);
            Point centroidPoint = bigArea.getGeometry().getCentroid();
            Coordinate centroidCoordinate = new Coordinate(centroidPoint.getX(), centroidPoint.getY());
            Node bigCentroid = new Node(nr, centroidCoordinate, TrafficBehaviourType.NTM);
            bigCentroids.put(nr, bigCentroid);
            for (Node centroid : centroids.values())
            {
                Coordinate nodeCoordinate = centroid.getPoint();
                Geometry nodeGeometry = new GeometryFactory().createPoint(nodeCoordinate);
                if (bigArea.getGeometry().covers(nodeGeometry))
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
     * @param model
     * @param areasToUse
     * @param centroidsToUse
     * @param file
     * @throws ParseException
     * @throws IOException
     */
    public void readOrSetParametersNTM(NTMModel model, Map<String, Area> areasToUse, Map<String, Node> centroidsToUse,
            String file) throws IOException, ParseException
    {
        HashMap<String, ArrayList<java.lang.Double>> parametersNTMByCentroid =
                CsvFileReader.readParametersNTM(file, ";", ",");
        if (parametersNTMByCentroid.isEmpty())
        {
            CsvFileWriter.writeParametersNTM(this, file);
            parametersNTMByCentroid = CsvFileReader.readParametersNTM(file, ";", ",");
        }

        for (Node node : centroidsToUse.values())
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
                            capacity =
                                    cellBehaviourNTM.getParametersNTM().getCapacity().getInUnit(FrequencyUnit.PER_HOUR);
                        }
                        // capacity = 9000;
                        parameters.remove(parameters.size() - 1);
                        parametersNTM =
                                new ParametersNTM(parameters, capacity, areasToUse.get(node.getId()).getRoadLength());
                    }
                    else
                    {
                        parametersNTM =
                                new ParametersNTM(areasToUse.get(node.getId()).getAverageSpeed(), areasToUse.get(
                                        node.getId()).getRoadLength());
                    }
                    cellBehaviourNTM.setParametersNTM(parametersNTM);
                }
            }

        }
    }

    /**
     * @param model
     * @param areasToUse
     * @param file
     * @param fileFactor
     * @throws IOException
     * @throws ParseException
     */
    public void readOrSetCapacityRestraints(NTMModel model, Map<String, Area> areasToUse, String file, String fileFactor)
            throws IOException, ParseException
    {
        HashMap<String, HashMap<String, Abs<FrequencyUnit>>> borderCapacityAreasMap =
                CsvFileReader.readCapResNTM(file, ";", ",");
        HashMap<String, HashMap<String, java.lang.Double>> borderCapacityFactorAreasMap =
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
        for (Node origin : model.getAreaGraph().vertexSet())
        {
            if (origin.getBehaviourType() == TrafficBehaviourType.NTM)
            {
                HashMap<BoundedNode, Abs<FrequencyUnit>> borderCapacity =
                        new HashMap<BoundedNode, Abs<FrequencyUnit>>();
                BoundedNode node = (BoundedNode) origin;
                CellBehaviourNTM cellBehaviour = (CellBehaviourNTM) node.getCellBehaviour();
                Set<LinkEdge<Link>> outGoing = this.getAreaGraph().outgoingEdgesOf(node);
                for (LinkEdge<Link> link : outGoing)
                {
                    double factor = 1.0;
                    Node neighbourNode = (Node) link.getLink().getEndNode();
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
                                factor =
                                        borderCapacityFactorAreasMap.get(origin.getId()).get(graphEndNode.getId())
                                                .doubleValue();
                            }
                        }
                        double capacity =
                                borderCapacityAreasMap.get(origin.getId()).get(graphEndNode.getId())
                                        .getInUnit(FrequencyUnit.PER_HOUR);
                        capacity *= factor;
                        Abs<FrequencyUnit> cap = new Abs<FrequencyUnit>(capacity, FrequencyUnit.PER_HOUR);
                        borderCapacity.put(graphEndNode, cap);
                    }
                    else
                    {
                        Abs<FrequencyUnit> cap = new Abs<FrequencyUnit>(99999.0, FrequencyUnit.PER_HOUR);
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
     */
    private Map<String, Link> createConnectors(HashMap<Node, Node> mapSmallAreaToBigArea, Map<String, Link> connectors)
    {
        HashMap<String, Link> mapConnectors = new HashMap<String, Link>();
        for (Link link : connectors.values())
        {
            Node startNode = null;
            Node endNode = null;
            Link newConnector = null;

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
                newConnector =
                        new Link(link.getGeometry(), link.getId(), link.getLength(), startNode,
                                (Node) link.getEndNode(), link.getFreeSpeed(), null, link.getCapacity(),
                                link.getBehaviourType(), link.getLinkData());
            }
            else if (endNode != null)
            {
                newConnector =
                        new Link(link.getGeometry(), link.getId(), link.getLength(), (Node) link.getStartNode(),
                                endNode, link.getFreeSpeed(), null, link.getCapacity(), link.getBehaviourType(),
                                link.getLinkData());
            }
            else
            {
                newConnector = new Link(link);
            }
            mapConnectors.put(link.getId(), newConnector);
        }
        return mapConnectors;
    }

    /**
     * @param shpLinks2
     * @param areas2
     */
    private void determineRoadLengthInAreas(Map<String, Link> shpLinks, Map<String, Area> areas)
    {
        Double speedTotal;
        Map<Area, java.lang.Double> speedTotalByArea = new HashMap<Area, java.lang.Double>();
        for (Link link : shpLinks.values())
        {
            for (Area area : areas.values())
            {
                if (area.getGeometry().intersects(link.getGeometry().getLineString()))
                {
                    double covers = 0.5;
                    if (area.getGeometry().contains(link.getGeometry().getLineString()))
                    {
                        covers = 1;
                    }
                    double length = covers * link.getLength().getInUnit(LengthUnit.KILOMETER) * link.getNumberOfLanes();
                    DoubleScalar.Rel<LengthUnit> laneLength =
                            new DoubleScalar.Rel<LengthUnit>(length, LengthUnit.KILOMETER);
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
                area.setAverageSpeed(new DoubleScalar.Abs<SpeedUnit>(averageSpeed, SpeedUnit.KM_PER_HOUR));
            }
            else
            {
                System.out.println("FlowLink Area: nodeNumber " + area.getCentroidNr());
                area.setRoadLength(new Rel<LengthUnit>(java.lang.Double.POSITIVE_INFINITY, LengthUnit.KILOMETER));
                double averageSpeed = 100;
                area.setAverageSpeed(new DoubleScalar.Abs<SpeedUnit>(averageSpeed, SpeedUnit.KM_PER_HOUR));
            }
        }

    }

    /**
     * @param areas set areas.
     */
    public void setAreas(Map<String, Area> areas)
    {
        this.areas = areas;
    }

    /**
     * @param nodes set nodes.
     */
    public void setNodes(Map<String, Node> nodes)
    {
        this.nodes = nodes;
    }

    /**
     * @param shpLinks set shpLinks.
     */
    public void setShpLinks(Map<String, Link> shpLinks)
    {
        this.shpLinks = shpLinks;
    }

    /**
     * @param centroids set centroids.
     */
    public void setCentroids(Map<String, Node> centroids)
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

        if (this.getSettingsNTM().getTimeStepDurationNTM().getInUnit(TimeUnit.SECOND) * NTMsimulation.steps
                % this.getSettingsNTM().getReRouteTimeInterval().getInUnit(TimeUnit.SECOND) == 0)
        {
            // in case we run on an animator and not on a simulator, we create the animation
            if (this.simulator instanceof OTSAnimatorInterface)
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
            this.simulator.scheduleEventRel(this.settingsNTM.getTimeStepDurationNTM(), this, this, "ntmFlowTimestep",
                    null);
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
                for (LinkEdge<Link> shpLink : this.linkGraph.edgeSet())
                {
                    new ShpLinkAnimation(shpLink.getLink(), this.simulator, 2.0F, Color.RED);
                }
            }
            if (showConnectors)
            {
                for (Link shpConnector : this.shpConnectors.values())
                {
                    new ShpLinkAnimation(shpConnector, this.simulator, 5.0F, Color.BLUE);
                }
            }

            if (showFlowLinks)
            {
                for (Link flowLink : this.flowLinks.values())
                {
                    new ShpLinkAnimation(flowLink, this.simulator, 10.0F, Color.RED);
                }
            }
            if (showNodes)
            {
                for (Node Node : this.nodes.values())
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
                for (LinkEdge<Link> linkEdge : this.areaGraph.edgeSet())
                {
                    new ShpLinkAnimation(linkEdge.getLink(), this.simulator, 5f, Color.BLACK);
                }
            }
            if (showAreaNode)
            {
                for (Node node : this.areaGraph.vertexSet())
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
                 * for (AreaFlowLink areaFlowLinks : model.areaFlowLinks.values()) { new
                 * AreaFlowLinkAnimation(areaFlowLinks, model.simulator, 4f); }
                 */

            }
            if (showLinks)
            {
                for (Link shpLink : model.shpLinks.values())
                {
                    new ShpLinkAnimation(shpLink, model.simulator, 6.0F, Color.GRAY);
                }
            }
            if (showConnectors)
            {
                for (Link shpConnector : model.shpConnectors.values())
                {
                    new ShpLinkAnimation(shpConnector, model.simulator, 5.0F, Color.BLUE);
                }
            }

            if (showFlowLinks)
            {
                for (Link flowLink : model.flowLinks.values())
                {
                    new ShpLinkAnimation(flowLink, model.simulator, 2.0F, Color.RED);
                }
            }
            if (showNodes)
            {
                for (Node Node : model.nodes.values())
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
                for (LinkEdge<Link> linkEdge : model.areaGraph.edgeSet())
                {
                    new ShpLinkAnimation(linkEdge.getLink(), model.simulator, 3f, Color.BLACK);
                }
            }
            if (showAreaNode)
            {
                for (Node node : model.areaGraph.vertexSet())
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

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        return this.simulator;
    }

    /**
     * @return settingsNTM.
     */
    public final NTMSettings getSettingsNTM()
    {
        return this.settingsNTM;
    }

    /**
     * @param settingsNTM set settingsNTM.
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
     * @param tripDemand set tripDemand.
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
     * @param departureTimeProfiles set departureTimeProfiles.
     */
    public final void setDepartureTimeProfiles(final ArrayList<DepartureTimeProfile> departureTimeProfiles)
    {
        this.departureTimeProfiles = departureTimeProfiles;
    }

    /**
     * @return flowLinks.
     */
    public final Map<String, Link> getFlowLinks()
    {
        return this.flowLinks;
    }

    /**
     * @param flowLinks set flowLinks.
     */
    public final void setFlowLinks(final Map<String, Link> flowLinks)
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
    public final SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> getAreaGraph()
    {
        return this.areaGraph;
    }

    /**
     * @return shpConnectors.
     */
    public final Map<String, Link> getShpConnectors()
    {
        return this.shpConnectors;
    }

    /**
     * @param shpConnectors set shpConnectors.
     */
    public final void setShpConnectors(final Map<String, Link> shpConnectors)
    {
        this.shpConnectors = shpConnectors;
    }

    /**
     * @return shpBigConnectors.
     */
    public Map<String, Link> getShpBigConnectors()
    {
        return this.shpBigConnectors;
    }

    /**
     * @param shpBigConnectors set shpBigConnectors.
     */
    public void setShpBigConnectors(Map<String, Link> shpBigConnectors)
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
     * @param bigAreas set bigAreas.
     */
    public void setBigAreas(Map<String, Area> bigAreas)
    {
        this.bigAreas = bigAreas;
    }

    /**
     * @return nodes.
     */
    public final Map<String, Node> getNodes()
    {
        return this.nodes;
    }

    /**
     * @return shpLinks.
     */
    public final Map<String, Link> getShpLinks()
    {
        return this.shpLinks;
    }

    /**
     * @return centroids.
     */
    public final Map<String, Node> getCentroids()
    {
        return this.centroids;
    }

    /**
     * @return linkGraph.
     */
    public final SimpleDirectedWeightedGraph<Node, LinkEdge<Link>> getLinkGraph()
    {
        return this.linkGraph;
    }

    /**
     * Links that show typical highway or mainroad behaviour are specified explicitly as roads.
     * @param shpLinks the links of this model
     * @return the flowLinks
     */
    public static Map<String, Link> createFlowLinks(final Map<String, Link> shpLinks, DoubleScalar<SpeedUnit> maxSpeed,
            DoubleScalar<FrequencyUnit> maxCapacity)
    {
        Map<String, Link> flowLinks = new HashMap<String, Link>();
        for (Link shpLink : shpLinks.values())
        {

            if (shpLink.getFreeSpeed().doubleValue() >= maxSpeed.doubleValue()
                    && shpLink.getCapacity().doubleValue() > maxCapacity.doubleValue())
            {
                Link flowLink = new Link(shpLink);
                if (flowLink.getGeometry() == null)
                {
                    System.out.println("NTMModel line 694 ... no geometry");
                }
                flowLink.setBehaviourType(TrafficBehaviourType.FLOW);
                ((Node) flowLink.getStartNode()).setBehaviourType(TrafficBehaviourType.FLOW);
                ((Node) flowLink.getEndNode()).setBehaviourType(TrafficBehaviourType.FLOW);
                flowLinks.put(flowLink.getId(), flowLink);
            }
        }

        for (Link flowLink : flowLinks.values())
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
     * @param compressedAreas set compressedAreas.
     */
    public void setCompressedAreas(ShapeStore compressedAreas)
    {
        this.compressedAreas = compressedAreas;
    }

    /**
     * @return bigCentroids.
     */
    public Map<String, Node> getBigCentroids()
    {
        return this.bigCentroids;
    }

    /**
     * @param bigCentroids set bigCentroids.
     */
    public void setBigCentroids(Map<String, Node> bigCentroids)
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
     * @param compressedTripDemand set compressedTripDemand.
     */
    public void setCompressedTripDemand(TripDemand<TripInfoTimeDynamic> compressedTripDemand)
    {
        this.compressedTripDemand = compressedTripDemand;
    }

    /**
     * @return debugLinkList.
     */
    public Map<String, Link> getDebugLinkList()
    {
        return this.debugLinkList;
    }

    /**
     * @param debugLinkList set debugLinkList.
     */
    public void setDebugLinkList(LinkedHashMap<String, Link> debugLinkList)
    {
        this.debugLinkList = debugLinkList;
    }

    /**
     * @return nodeAreaGraphMap.
     */
    public Map<String, Node> getNodeAreaGraphMap()
    {
        return this.nodeAreaGraphMap;
    }

    /**
     * @param nodeAreaGraphMap set nodeAreaGraphMap.
     */
    public void setNodeAreaGraphMap(Map<String, Node> nodeAreaGraphMap)
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
     * @param inputNTM set inputNTM.
     */
    public void setInputNTM(InputNTM inputNTM)
    {
        this.inputNTM = inputNTM;
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
     * AreaNTM> areas) { for (ShpLink shpLink : shpLinks.values()) { if (shpLink.getSpeed() > 70 &&
     * shpLink.getCapacity() > 3400) { Geometry buffer = shpLink.getGeometry().buffer(10); Point centroid =
     * buffer.getCentroid(); String nr = shpLink.getNr(); String name = shpLink.getName(); String gemeente =
     * shpLink.getName(); String gebied = shpLink.getName(); String regio = shpLink.getName(); double dhb = 0.0; AreaNTM
     * area = new AreaNTM(buffer, nr, name, gemeente, gebied, regio, dhb, centroid); areas.put(nr, area); } } return
     * areas; }
     */

}
