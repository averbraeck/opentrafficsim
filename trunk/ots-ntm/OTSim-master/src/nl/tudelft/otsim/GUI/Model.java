package nl.tudelft.otsim.GUI;

import java.util.ArrayList;

import nl.tudelft.otsim.Activities.Activities;
import nl.tudelft.otsim.Charts.MeasurementPlan;
import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.GeoObjects.Network;
import nl.tudelft.otsim.Population.Population;
import nl.tudelft.otsim.ShortesPathAlgorithms.CreatePaths;
import nl.tudelft.otsim.TrafficDemand.TrafficDemand;

/**
 * This class holds a traffic model with activities, population, network,
 * settings and traffic demand.
 * 
 * @author gtamminga
 */
public class Model implements Storable {
	/** Tag of Model node in an XML file */
	public static final String XMLTAG = "model";
	/** Extension of Model files */
	public static final String FILETYPE = "otsm";
	/** {@link Network} of this model */
	public Network network = new Network();
	/** {@link Population} of this model */
	public Population population = new Population(this);
	/** {@link Activities} of this model */
	public Activities activities = new Activities();
	/** {@link Activities} of this model */
	public TrafficDemand trafficDemand = new TrafficDemand(this);
	private ArrayList<MeasurementPlan> measurementPlans = new ArrayList<MeasurementPlan>();
	private String fileName = null;
	
	/**
	 * Create a new Model from a {@link ParsedNode}.
	 * @param modelRoot {@link ParsedNode}; the root of the XML tree
	 * @throws Exception
	 */
	public Model(ParsedNode modelRoot) throws Exception {
		if ((modelRoot.getKeys().size() != 1) || (modelRoot.size(XMLTAG) != 1))
			throw new Exception("There should be precisely one \"" + XMLTAG + "\" node in the XML tree");
		ParsedNode modelNode = modelRoot.getSubNode(XMLTAG, 0);
		// Check that all required nodes are present (and there are no unknown nodes)
		for (String key : modelNode.getKeys()) {
			if (key.equals(Network.XMLTAG)) {
				if (modelNode.size(Network.XMLTAG) != 1)
					throw new Exception("There should be precisely one \"" + Network.XMLTAG + "\" node in the XML tree");
			} else if (key.equals(TrafficDemand.XMLTAG)) { 
				if (modelNode.size(TrafficDemand.XMLTAG) != 1)
					throw new Exception("There should be precisely one \"" + TrafficDemand.XMLTAG + "\" node in the XML tree");
			} else if (key.equals(MeasurementPlan.XMLTAG))
				;	// no check to be done
			else	// complain, then continue
				WED.showProblem(WED.INFORMATION, "Unsupported node \"%s\" ignored", key);
		}
		network = new Network(modelNode.getSubNode(Network.XMLTAG, 0));
		trafficDemand = new TrafficDemand(this, modelNode.getSubNode(TrafficDemand.XMLTAG, 0));	
		for (int index = 0; index < modelNode.size(MeasurementPlan.XMLTAG); index++)
			addMeasurementPlan(new MeasurementPlan(this, modelNode.getSubNode(MeasurementPlan.XMLTAG, index)));
    	network.rebuild();
    	network.clearModified();
    	activities.rebuild();
    	population.rebuild();
    	trafficDemand.rebuild();
	}
	
	/**
	 * Add one {@link MeasurementPlan} to this Model.
	 * @param mp {@link MeasurementPlan} the MeasurementPlan to add
	 */
	public void addMeasurementPlan(MeasurementPlan mp) {
		measurementPlans.add(mp);
		Main.mainFrame.measurementPlanListChanged();
	}
	
	/**
	 * Remove a {@link MeasurementPlan} from this Model.
	 * @param mp {@link MeasurementPlan} the MeasurementPlan to remove
	 */
	public void deleteMeasurementPlan(MeasurementPlan mp) {
		if (! measurementPlans.contains(mp))
			throw new Error("MeasurementPlane " + mp.toString() + " is not stored in this Model");
		measurementPlans.remove(mp);
		Main.mainFrame.measurementPlanListChanged();
	}

	/**
	 * Retrieve the number of {@link MeasurementPlan MeasurementPlans} in this Model.
	 * @return Integer; the number of {@link MeasurementPlan MeasurementPlans} in this Model
	 */
	public int measurementPlanCount() {
		return measurementPlans.size();
	}
	
	/**
	 * Retrieve the Nth {@link MeasurementPlan} of this Model.
	 * @param index Integer; the rank of the {@link MeasurementPlan} to retrieve
	 * @return {@link MeasurementPlan} the MeasurementPlan stored at index
	 */
	public MeasurementPlan getMeasurementPlan(int index) {
		return measurementPlans.get(index);
	}
	
	/**
	 * Create a new Model given a network file name and a traffic demand file name.
	 * <br /> if a file name is null, that part of the new Model will be
	 * empty.
	 * @param networkFileName String; name of the {@link Network} file name
	 * @param demandFileName String; name of the {@link TrafficDemand} file name
	 * @throws Exception
	 */
	public Model(String networkFileName, String demandFileName) throws Exception {
		if ((null != networkFileName) && (networkFileName.length() > 0)) {
			// Create the XML parser and read the data
			ParsedNode modelTree = new ParsedNode(networkFileName);
			//System.out.print(modelTree.toString(""));
			if (modelTree.getKeys().size() < 1)
				throw new Exception("File " + networkFileName + " contains no data");
			for (String key : modelTree.getKeys()) {
				System.out.println("key is " + key);
				if (Network.XMLTAG.equals(key)) {
					if (modelTree.size(key) != 1)
						throw new Error("Network XML node contains " + modelTree.size(key) + " networks (should be 1)");
					ParsedNode networkNode = modelTree.getSubNode(key,  0);
						network = new Network(networkNode);
				} else
					throw new Error("Do not know what to do with a " + key);
			}
		}
		if ((null != demandFileName) && (demandFileName.length() > 0)) {
			ParsedNode demandTree = new ParsedNode(demandFileName);
			if (demandTree.getKeys().size() < 1)
				throw new Exception("File " + networkFileName + " contains no data");
			for (String key : demandTree.getKeys()) {
				System.out.println("key is " + key);
				if (TrafficDemand.XMLTAG.equals(key)) {
					if (demandTree.size(key) != 1)
						throw new Error("TraffcDemand XML node contains " + demandTree.size(key) + " demands (should be 1)");
					ParsedNode demandNode = demandTree.getSubNode(key,  0);
						trafficDemand = new TrafficDemand(this, demandNode);
				} else
					throw new Error("Do not know what to do with a " + key);
			}
		}
		// Construct the model
    	//settings.rebuild();
    	network.rebuild();
    	network.clearModified();
    	activities.rebuild();
    	population.rebuild();
    	trafficDemand.rebuild();
	}
	
	/**
	 * Create a new (empty) Traffic Model
	 */
	public Model() {
		network = new Network();
		activities = new Activities();
		population = new Population(this);
		trafficDemand = new TrafficDemand(this);
	}
	
	/**
	 * Create a (possibly very long) string with a text description of those
	 * aspects of this Model that are relevant to a micro-simulator.
	 * @return String; Textual description of this Model
	 */
    public String exportToMicroSimulation() {
    	// Create the routes for all traffic demand
    	CreatePaths.CreatePathsTripPatterns(network, trafficDemand.getTripPatternList());
    	//HashMap<Node, Double> tripsByNode = createTripsByNode(trafficDemand);
		return network.exportLanes() 
				+ trafficDemand.exportTripPattern()
				// + exportTripPattern(tripsByNode)
    			+ network.exportTrafficLights()
    			+ network.exportDetectors()
    			+ network.exportTrafficLightControllers();
		// TrafficLightControllers MUST come AFTER the lights and the detectors
	}
    
    /**
     * Create a textual description of those aspects of this Model that are
     * relevant to a sub-micro-simulator.
     * @return String; textual description of this Model
     */
    public String exportToSubMicroSimulation() {
		return network.exportDrivableBoundaries() 
		+ network.exportDetectors() 
		+ network.exportTrafficLights() 
		+ network.exportTrafficLightControllers();
    }

	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& network.writeXML(staXWriter)
				&& trafficDemand.writeXML(staXWriter)
				&& staXWriter.writeNodeEnd(XMLTAG);
	}

	@Override
	public String description() {
		return "Model";
	}

	@Override
	public String fileType() {
		return FILETYPE;
	}

	@Override
	public boolean isModified() {
		return network.isModified() || trafficDemand.isModified();
	}

	@Override
	public void clearModified() {
		network.clearModified();
		trafficDemand.clearModified();
	}

	@Override
	public String storageName() {
		return fileName;
	}

	@Override
	public void setStorageName(String name) {
		fileName = name;
	}

    /**
     * Create a string representation of the lanes and the connectivity between
     * the lanes in the Network and the traffic demand
     * @param trafficDemand BLABLA
     * @return String; Description of the Network and the traffic demand
     *//*
    public static HashMap<Node, Double> createTripsByNode(TrafficDemand trafficDemand) {
    	HashMap<Node, Double> tripsByNode = new HashMap<Node, Double> ();
    	for (TripPattern tripPattern : trafficDemand.getTripPatternList())   {
            for (TripPatternPath tripPatternPath : tripPattern.getTripPatternPathList())   {
            	double oldTrips = 0;
            	Double trips = tripPatternPath.getNumberOfTrips();
				Node start = tripPatternPath.getPathList().get(0).getNodeList().get(0);
				if (!(tripsByNode.get(start) == null))  {
					oldTrips = tripsByNode.get(start);
				}
	            tripsByNode.put(start, oldTrips + trips);
			}
		}
		return tripsByNode;
	}*/
    
}