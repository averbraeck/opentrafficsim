package nl.tudelft.otsim.ModelIO;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import nl.tudelft.otsim.GUI.FileDialog;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GUI.Model;
import nl.tudelft.otsim.GeoObjects.CrossSection;
import nl.tudelft.otsim.GeoObjects.CrossSectionElement;
import nl.tudelft.otsim.GeoObjects.Node;
import nl.tudelft.otsim.GeoObjects.RoadMarkerAlong;
import nl.tudelft.otsim.GeoObjects.TurnArrow;
import nl.tudelft.otsim.GeoObjects.Vertex;
import nl.tudelft.otsim.TrafficDemand.TripPattern;

import org.geotools.data.DataStore;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class ImportModelShapeWizard implements ActionListener {
	// FIXME: Why is cards declared static?
    private static JPanel cards = new JPanel(new CardLayout());
	private int cardIndex ;
	private JButton cancelButton;
	private JButton finishButton;
    private JButton nextButton;
    private JButton prevButton;

	private JCheckBox optionNetwork;
	private JCheckBox optionTripPattern;

	private JFrame frame;
    private static FileChooser fChooser;
    private Model importedModel = new Model();
    private static String fileImportedMatrix;
	private static TableModelImport shapeImport = null;	
	private static int index; 
	private static int fileCount;

	public ImportModelShapeWizard() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// Why? It does not actually work because it is overridden later!
        frame.setPreferredSize(new Dimension(700, 400));
        cards.setBorder(BorderFactory.createLineBorder(Color.black));
        cardIndex = 0;  // start at the initial/first card
       
    	JPanel choices = new JPanel() ;
        choices.setLayout(new GridBagLayout());	        
        GridBagConstraints gbConstraints = new GridBagConstraints();

        gbConstraints.anchor = GridBagConstraints.NORTHWEST;
        gbConstraints.insets = new Insets(15,30,0,0);
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 0;
        optionNetwork= new JCheckBox("Network Import (shape format)");
        optionNetwork.addActionListener(this);
        optionNetwork.setEnabled(true);
        choices.add(optionNetwork, gbConstraints);	
        gbConstraints.weightx = 0.4;
        gbConstraints.weighty = 0.4;
        gbConstraints.gridx = 0;
        gbConstraints.gridy = 1;
        optionTripPattern = new JCheckBox("TripPattern import (Origin, Destination, number of Trips");
        optionTripPattern.addActionListener(this);
        optionTripPattern.setEnabled(true);
        choices.add(optionTripPattern, gbConstraints);
        cards.add(choices, "Options");

    	String[] fileNames = { "Browse", "Browse", "Browse", "Browse" }; 
    	String[] labels = { "Shape Links: ", "Shape Nodes: ", "Shape Zones: ", "Matrix file: " };	    	
    	fileCount = fileNames.length;
    	String[] commandNames = { "ShapeLinks", "ShapeNodes", "ShapeZones", "Matrix" };
    	fChooser = new FileChooser(labels, fileNames, commandNames, "ImportModelShape");
    	fChooser.setVisible(true); 
    	cards.add(fChooser, fChooser.toString());

    	JPanel control = new JPanel() ;
        prevButton = new JButton("\u22b2Prev");
        prevButton.addActionListener(this);
        prevButton.setEnabled(false);
        control.add(prevButton);	        
        nextButton = new JButton("Next\u22b3");
        nextButton.addActionListener(this);
        nextButton.setEnabled(false);
        control.add(nextButton);
        finishButton = new JButton("Finish");
    	finishButton.addActionListener(this);
    	finishButton.setEnabled(false);  	
    	control.add(finishButton);
    	cancelButton = new JButton("Cancel");
    	cancelButton.addActionListener(this);
    	control.add(cancelButton);

    	frame.add(cards, BorderLayout.CENTER);
        frame.add(control, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        fixButtons();
        frame.setVisible(true);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		System.out.println("actionperformed: command is " + command);
		if (command.startsWith("Network Import") )   {
	        Main.mainFrame.getImportModelShapeWizard().getNextButton().setEnabled(true);
	        for (int i = 0; i <= 2; i++)
	        	fChooser.getFileButton()[i].setEnabled(optionNetwork.isSelected());
	    }

		if (command.startsWith("TripPattern") )   {
	        Main.mainFrame.getImportModelShapeWizard().getNextButton().setEnabled(true);
	        fChooser.getFileButton()[3].setEnabled(optionNetwork.isSelected());
		}

		if (command.startsWith("Finish")) {
			System.out.println("Finish" + command);
			try {
				importedModel = getOmnitransFeatures(getDataStoreLinks(), getDataStoreNodes(), 
						getDataStoreZones(), getTableShapeImport().getTableField(), 
						getTableShapeImport().getTableDirection());
				if (optionTripPattern.isSelected())  {
					ArrayList<TripPattern> tripPatternList = importMatrix(getFileImportedMatrix());
					importedModel.trafficDemand.setTripPatternList(tripPatternList);
					importedModel.trafficDemand.rebuild();
				}
				//importedModel.trafficDemand.createTripPatternList(tripPatternList);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        Main.mainFrame.setTitle("Imported");
	        Main.mainFrame.setActiveGraph();
	        this.frame.dispose();
		}
		if (command.startsWith("Cancel")) {
			System.out.println("Cancel" + command);
			importedModel = null;
			this.frame.dispose();
			
			//add close statement
		}
		if (command.startsWith("\u22b2Prev")) {
			if (0 == cardIndex)
				return;	// should not happen; race ?
			cardIndex--;
            ((CardLayout) cards.getLayout()).previous(cards);
			fixButtons();
		}
		if (command.startsWith("Next\u22b3")) {
			if (cards.getComponentCount() - 1 == cardIndex)
				return;	// should not happen; race ?
			cardIndex++;
            ((CardLayout) cards.getLayout()).next(cards);
			fixButtons();
        }
	}
	
	private void fixButtons() {
    	System.out.print( "cardnumber" + cardIndex + "next" );
		nextButton.setEnabled(cardIndex < cards.getComponentCount() - 1);
		prevButton.setEnabled(cardIndex > 0);
		finishButton.setEnabled(cardIndex == cards.getComponentCount() - 1);
	}
	
	static void importModel() throws Exception   {
		if (fChooser.getCommand().startsWith("Shape")) {
			File file = new File(Main.mainFrame.initialDirectory);
	        file = JFileDataStoreChooser.showOpenFile("shp", null);
	        if (file == null)
	            return;
	        
	        String name = fChooser.getCommand();
	        // FIXME: Peter K thinks this is dangerous and might not work as intended.
	        // see https://stackoverflow.com/questions/767372/java-string-equals-versus
        	if (name == "ShapeLinks") {
        		index = 0;
		        dataStoreLinks = FileDataStoreFinder.getDataStore(file);
		        dataStoreLinks.getFeatureSource();        		
            	linkAttributeNames = getAttributeNames(dataStoreLinks);
        	} else if (name == "ShapeNodes") {
        		index = 1;
		        dataStoreNodes = FileDataStoreFinder.getDataStore(file);
		        dataStoreLinks.getFeatureSource();        		
            	getAttributeNames(dataStoreLinks);
        	} else if (name == "ShapeZones") {
        		index = 2;
		        dataStoreZones = FileDataStoreFinder.getDataStore(file);
		        dataStoreLinks.getFeatureSource();        		
            	getAttributeNames(dataStoreLinks);
        	}
	        fChooser.getTextField()[index].setText(file.getAbsolutePath());
		}
		else if (fChooser.getCommand().startsWith("Matrix")) {
			index = 3;
			openDialogImportMatrix();
		}
		//set enabled true, but if not all files are selected puts it back to false
        Main.mainFrame.getImportModelShapeWizard().getNextButton().setEnabled(true);
        for (int i = 0; i < fileCount - 1; i++)
        	if (fChooser.getTextField()[i].getText().toString().isEmpty())
        		Main.mainFrame.getImportModelShapeWizard().getNextButton().setEnabled(false);

        // TODO explain what this does
        if (Main.mainFrame.getImportModelShapeWizard().getNextButton().isEnabled()) {	
        	Object[] types = {"Type String", "Type String", Boolean.TRUE};
        	Object[][] dataDir = {
				{"Direction indicator", "DIRECTION"},
				{"AB", "1"},
				{"BA", "2"},
				{"AB and BA", "3"},
			};
    		shapeImport = new TableModelImport(linkAttributeNames, types, dataDir);
    		shapeImport.setOpaque(true); //content panes must be opaque
			cards.add(shapeImport, shapeImport.toString());
        }
    
	}
	
	private static FileDataStore dataStoreLinks;
    private static FileDataStore dataStoreNodes;
    private static FileDataStore dataStoreZones;
    private static String[] linkAttributeNames;
	
    private static String[] names = null;
    
    private static String[] getAttributeNames(DataStore dataStore) throws Exception {
        String[] typeName = dataStore.getTypeNames();
        SimpleFeatureSource source = dataStore.getFeatureSource(typeName[0]);
        source.getFeatures();
        FeatureType ft = source.getSchema();
        Collection<PropertyDescriptor> attributeDescriptors = ft.getDescriptors();
        Iterator<PropertyDescriptor> iteratorAttributes = attributeDescriptors.iterator();
        int i = 0;
        names = new String[attributeDescriptors.size()];
        while( iteratorAttributes.hasNext())   {
        	PropertyDescriptor descr = iteratorAttributes.next();
        	Name name = descr.getName();
        	names[i] = name.toString();
        	i++;
        }
        return names;
    }

	private static DataStore getDataStoreLinks() {
		return dataStoreLinks;
	}

	private static DataStore getDataStoreNodes() {
		return dataStoreNodes;
	}

	private static DataStore getDataStoreZones() {
		return dataStoreZones;
	}

	private static String getFileImportedMatrix() {
		return fileImportedMatrix;
	}

	private static TableModelImport getTableShapeImport() {
		return shapeImport;
	}
	private HashMap<Integer, Integer> readZoneData(Model importedModel, DataStore zoneStore, int zoneID)  {
		// Reading Centroids (as nodes)
		HashMap<Integer, Integer> findZoneIDfromNodeNr = new HashMap<Integer, Integer>();
        String[] typeNameZone = null;
		try {
			typeNameZone = zoneStore.getTypeNames();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        SimpleFeatureSource sourceZone = null;
		try {
			sourceZone = zoneStore.getFeatureSource(typeNameZone[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        SimpleFeatureCollection featuresZone = null;
		try {
			featuresZone = sourceZone.getFeatures();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //FeatureCollectionTableModel model = new FeatureCollectionTableModel(featuresNode);
        FeatureIterator<?> iteratorZone = featuresZone.features();	
        String propertyZone = "CENTROIDNR";	
        while( iteratorZone.hasNext()) {
        	Vertex v = new Vertex();
        	Feature feature = iteratorZone.next();
        	Geometry test = (Geometry) feature.getDefaultGeometryProperty().getValue();
        	Coordinate[] coords = test.getCoordinates();
        	for (int i = 0; i < coords.length; i++)
        		v = new Vertex(coords[i]);
         	Property property = feature.getProperty(propertyZone);
        	Integer nodeNr = (int) (long) property.getValue();
        	//zoneID = Integer.parseInt(property.getValue().toString());
        	importedModel.network.addMicroZone(nodeNr.toString(), null, zoneID, v.getX(), v.getY(), v.getZ());
        	findZoneIDfromNodeNr.put(nodeNr, zoneID);
        	zoneID++;
        }
        return findZoneIDfromNodeNr;
	}
	
	private void readNodeData(Model importedModel, DataStore nodeStore, int zoneID)  {
	    String[] typeNameNode = null;
		try {
			typeNameNode = nodeStore.getTypeNames();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    SimpleFeatureSource sourceNode = null;
		try {
			sourceNode = nodeStore.getFeatureSource(typeNameNode[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    SimpleFeatureCollection featuresNode = null;
		try {
			featuresNode = sourceNode.getFeatures();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    //FeatureCollectionTableModel model = new FeatureCollectionTableModel(featuresNode);
	    FeatureIterator<?> iteratorNode = featuresNode.features();	
	    String propertyNode = "NODENR";	
	    int nodeID = zoneID + 1;
	    while( iteratorNode.hasNext()) {
	    	Vertex v = new Vertex();
	    	Feature feature = iteratorNode.next();
	    	Geometry test = (Geometry) feature.getDefaultGeometryProperty().getValue();
	    	Coordinate[] coords = test.getCoordinates();
	    	for (int i = 0; i < coords.length; i++)
	    		v = new Vertex(coords[i]);
	    	Property property = feature.getProperty(propertyNode);
	    	String nodeNr = property.getValue().toString();
	    	nodeID = Integer.parseInt(property.getValue().toString());
	    	importedModel.network.addNode(nodeNr, nodeID, v.getX(), v.getY(), v.getZ());
	    	nodeID++;
	    }
	}	
	
	private void readLinkData(Model importedModel, DataStore linkStore, int zoneID, HashMap<Integer, Integer> findZoneIDfromNodeNr, TableImport tableDirection, TableImport tableField)  {
	    //Reading Links
	    String[] typeNameLink = null;
		try {
			typeNameLink = linkStore.getTypeNames();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    SimpleFeatureSource sourceLink = null;
		try {
			sourceLink = linkStore.getFeatureSource(typeNameLink[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    SimpleFeatureCollection featuresLink = null;
		try {
			featuresLink = sourceLink.getFeatures();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    FeatureIterator<?> iteratorLink = featuresLink.features();	 
	    // select the attributes
	    String propertyLength = null;
	    String propertyDirection = null;
	    String propertyFromNode = null;
	    String propertyToNode = null;
	    // we assume a bidirectional link (AB and BA direction) 
	    String[] propertyCapacity = {"",""};
	    String[] propertyLanes = {"",""};
	    String[] propertyTurnLanes = {"",""};
	    String[] propertyExitLanes = {"",""};
	    String[] propertyMaxSpeed = {"",""};
	    //int valueDirectionAB = -1;
	    int valueDirectionBA = -1;
	    int valueDirectionABBA = -1;
	    for (int i = 0; i < tableDirection.getTable().getRowCount(); i++) {
	    	// reads the name of the direction indicator from the table
	    	// and the values that indicate if it is from A to B, B to A, or both directions
			tableDirection.getTable().getValueAt(i, 0);
			Object attr2 = tableDirection.getTable().getValueAt(i, 1);	 
			if (i == 0)
				 propertyDirection = attr2.toString();
/*			if (i == 1)
				// A to B
				 valueDirectionAB = Integer.parseInt( attr2.toString() );*/
			if (i == 2)
				// B to A
				valueDirectionBA = Integer.parseInt(attr2.toString() );
			if (i == 3)
				// both directions
				 valueDirectionABBA = Integer.parseInt(attr2.toString() );
	    }

	    for (int i = 0; i < tableField.getTable().getRowCount(); i++) {
			tableField.getTable().getValueAt(i, 0);
			Object attr2 = tableField.getTable().getValueAt(i, 1);
			tableField.getTable().getValueAt(i, 2);
			if (!attr2.toString().equals(TableImport.EMPTYCOLUMN)) {
				if (i == 1)
					 propertyFromNode = attr2.toString();
				else if (i == 2)
					 propertyToNode = attr2.toString();
				else if (i == 3) {
					 propertyCapacity[0] = attr2.toString();
					 propertyCapacity[1] = propertyCapacity[0].replaceAll("AB", "BA");
				} else if (i == 4) {
					 propertyLanes[0] = attr2.toString();
					 propertyLanes[1] = propertyLanes[0].replaceAll("AB", "BA");
				} else if (i == 5) {
					propertyTurnLanes[0] = attr2.toString();
					propertyTurnLanes[1] = propertyTurnLanes[0].replaceAll("AB", "BA");
				} else if (i == 6) {
					propertyExitLanes[0] = attr2.toString();
					propertyExitLanes[1] = propertyExitLanes[0].replaceAll("AB", "BA");
				} else if (i == 7) {
					 propertyMaxSpeed[0] = attr2.toString();
					 propertyMaxSpeed[1] = propertyMaxSpeed[0].replaceAll("AB", "BA");
				} else if (i == 8)
					 propertyLength = attr2.toString();
			}
	
		}
	
		int linkID = 0;
	    while( iteratorLink.hasNext())   {
	    	Feature feature = iteratorLink.next();
	    	Geometry test = (Geometry) feature.getDefaultGeometryProperty().getValue();
	    	Coordinate[] coords = test.getCoordinates();
	    	List<List<Vertex>> pointList = new ArrayList<List<Vertex>>(2);
	    	List<Vertex> list = new ArrayList<Vertex>(); 
	    	pointList.add(list);
	    	list = new ArrayList<Vertex>(); 
	    	pointList.add(list);
			int fromNodeID = -1;
			int toNodeID = -1;
			double[] length = {-1,-1};
			double[] capacity = {-1,-1};
			String[] turnLanes = {"",""};
			int exitLanes[] = {-1,-1};
			double maxSpeed[] = {-1,-1};
			int lanes[] = {-1,-1};
			int direction = -1; 
			String typologyName = "road";
//			String linkType = "ANODE";
			Property property = null;
//	    	property = feature.getProperty(linkType);
			boolean voedingsLink = false;
//			int node;
//	    	node = Integer.parseInt(property.getValue().toString());
/*	    	if (zoneIDtoNumber.get(node) != null)  {	    		
	    		voedingsLink = true;
	    	}*/
	    	if (!propertyDirection.equals(""))   {
	        	property = feature.getProperty(propertyDirection);
	        	direction = Integer.parseInt(property.getValue().toString());
	    	}
	    	
	    	if (!propertyFromNode.equals(""))   {
	        	property = feature.getProperty(propertyFromNode);
	        	fromNodeID = Integer.parseInt(property.getValue().toString());
		    	if (findZoneIDfromNodeNr.get(fromNodeID) != null)  	    		
		    		voedingsLink = true;
	    	}
	    	if (!propertyToNode.equals(""))   {
	        	property = feature.getProperty(propertyToNode);
	        	toNodeID = Integer.parseInt(property.getValue().toString());
		    	if (findZoneIDfromNodeNr.get(toNodeID) != null)  	    		
		    		voedingsLink = true;
	    	}

        	for (int i = 1; i < coords.length - 1; i++) {
        		Vertex v = new Vertex(coords[i]);
        		pointList.get(0).add(v);
        	}
        	for (int i = coords.length - 2; i > 0; i--) {
        		Vertex v = new Vertex(coords[i]);
        		pointList.get(1).add(v);
        	}

        	int start = 0;
        	int end = 0;
        	if (direction == valueDirectionBA) {
            	start = 1;
            	end = 1;        		
        	}
        	if (direction == valueDirectionABBA) {
        		start = 0;
        		end = 1;
        	}
        	for (int i = start; i <= end; i++) {
	        	if (! (propertyCapacity[i].equals("") || feature.getProperty(propertyCapacity[i]) == null))   {
		        	property = feature.getProperty(propertyCapacity[i]);	        	
		        	capacity[i] = Double.parseDouble(property.getValue().toString());
	        	}
	        	if (! (propertyTurnLanes[i].equals("") || feature.getProperty(propertyTurnLanes[i])==null))   {
		        	property = feature.getProperty(propertyTurnLanes[i]);
		        	turnLanes[i] = property.getValue().toString();
	        	}
	        	if (! (propertyExitLanes[i].equals("") || feature.getProperty(propertyExitLanes[i])==null))   {
		        	property = feature.getProperty(propertyExitLanes[i]);
		        	// exitLanes are defined for the opposite direction!!!
		        	int j = i == 0 ? 1 : 0;
		        	exitLanes[j] = Integer.parseInt(property.getValue().toString());
	        	}
	        	if (! (propertyMaxSpeed[i].equals("") || feature.getProperty(propertyMaxSpeed[i])==null))   {
		        	property = feature.getProperty(propertyMaxSpeed[i]);
		        	maxSpeed[i] = Double.parseDouble(property.getValue().toString());
	        	}			        	
	        	if (! (propertyLength.equals("") || feature.getProperty(propertyLength)==null))   {
		        	property = feature.getProperty(propertyLength);
		        	length[i] = Double.parseDouble(property.getValue().toString()) * 1000 ;
	        	}			        	
	        	if (! (propertyLanes[i].equals("") || feature.getProperty(propertyLanes[i])==null))   {
	        		property = feature.getProperty(propertyLanes[i]);
		        	lanes[i] = Integer.parseInt(property.getValue().toString());
	        	}	        	
		    	if (lanes[i] <= 0)
		    		lanes[i] = deriveLanes(capacity[i], maxSpeed[i]);
		    	if (lanes[i] == 0)
		    		System.out.print("geen lanes??????");	        	
        	}
	    	final double defaultLaneWidth = 3.5;
	    	for (int i = start; i <= end; i++) {
		    	if (! voedingsLink) {
		    		if (i == 0)
		    			addImportedLink(linkID++, typologyName, fromNodeID, toNodeID, defaultLaneWidth, lanes[i], exitLanes[i], (ArrayList<Vertex>) pointList.get(i), turnLanes[i], length[i]);
		    		else if (i == 1)
		    			addImportedLink(linkID++, typologyName, toNodeID, fromNodeID, defaultLaneWidth, lanes[i], exitLanes[i], (ArrayList<Vertex>) pointList.get(i), turnLanes[i], length[i]);
		    	} else {
					List<Integer> nodeList = new ArrayList<Integer>();
		    		if (findZoneIDfromNodeNr.get(fromNodeID) != null) {
		    			nodeList.add(toNodeID);
		    			importedModel.network.lookupMicroZone(findZoneIDfromNodeNr.get(fromNodeID)).setNodeList(nodeList);
		    		} else {
		        		nodeList.add(fromNodeID);
		    			importedModel.network.lookupMicroZone(findZoneIDfromNodeNr.get(fromNodeID));
		    		}
		    		break;
		    	}
		    	
	    	}
	    }
	}
	
	private Model getOmnitransFeatures( DataStore linkStore, DataStore nodeStore, DataStore zoneStore, TableImport tableField, TableImport tableDirection) throws IOException {        
		int zoneID = 0;
		HashMap<Integer, Integer> findZoneIDfromNodeNr = new HashMap<Integer, Integer>();
		findZoneIDfromNodeNr = readZoneData(importedModel, zoneStore, zoneID);
		readNodeData(importedModel, nodeStore, zoneID);
		readLinkData(importedModel, linkStore, zoneID, findZoneIDfromNodeNr, tableDirection, tableField);
		// Construct the model
    	//settings.rebuild();
        importedModel.network.rebuild();
        importedModel.network.clearModified();
        importedModel.activities.rebuild();
        importedModel.population.rebuild();
        importedModel.trafficDemand.rebuild();
        return importedModel;
	}
	
	private static ArrayList<RoadMarkerAlong> createRMA(int lanes, double defaultLaneWidth)  {
		ArrayList<RoadMarkerAlong> rmaList = new ArrayList<RoadMarkerAlong>();
		rmaList.add(new RoadMarkerAlong("|", 0));
		for (int i = 0; i < lanes; i++)
			rmaList.add(new RoadMarkerAlong(i < lanes - 1 ? ":" : "|", (i + 1) * defaultLaneWidth));
		return rmaList;
	}
    private static void openDialogImportMatrix() {
    	String fileName = FileDialog.showFileDialog(true, "dat", "*.dat", 
    			Main.mainFrame.initialDirectory);
    	if (null == fileName)
    		return;
    	fileImportedMatrix = new File(fileName).getPath();
		fChooser.getTextField()[index].setText(fileImportedMatrix);
        System.out.printf("User selected network file \"%s\"", fileImportedMatrix);
    }
    
	private void addImportedLink(int linkID, String typologyName, int fromNodeID, int toNodeID,  double laneWidth, int lanes, 
			int exitLanes, ArrayList<Vertex> pointList, String turnLanes, double length)   {
		if (null == typologyName)
			typologyName = "road";
			//throw new Error ("CrossSectionElement has null typologyName");
    	ArrayList<RoadMarkerAlong> rmaList = createRMA(lanes, laneWidth);
    	ArrayList<CrossSection> csList = new ArrayList<CrossSection>();
    	CrossSection cs = new CrossSection(0.0, 0.0, null);
    	csList.add(cs);
    	CrossSectionElement cse = new CrossSectionElement(cs, typologyName, laneWidth * lanes, rmaList, null);
    	ArrayList<CrossSectionElement> cseList = new ArrayList<CrossSectionElement>();
    	cseList.add(cse);
    	cs.setCrossSectionElementList_w(cseList);
		ArrayList<Vertex> pointListAll = new ArrayList<Vertex>();
		pointListAll.addAll(pointList);
		// FIXME: rewrite using Network.lookupNode
		for (Node node : importedModel.network.getNodeList(false)) {
			if (node.getNodeID() == fromNodeID)
				pointListAll.add(0, node);	// insert from-node at start
			if (node.getNodeID() == toNodeID)
				pointListAll.add(node);		// append to-node at end
		}
    	double calculatedLength = calculateLength(pointListAll);
    	// if turnlanes are defined, we create an intermediate crossSection at a certain pre-defined distance from the junction (toNode)
    	if (turnLanes != null  && ! turnLanes.isEmpty())  {
    		ArrayList<TurnArrow> turnArrowList = analyseTurns(turnLanes, laneWidth);

    		if (lanes != turnArrowList.size()) {
    			double longPosition1 = 0;
    			double longPosition2 = 0;
    			final int minimalLengthTurnLanes = 70;
    			
    			if (calculatedLength > minimalLengthTurnLanes)   {
        			final int createCrossSection1 = 50;
        			final int createCrossSection2 = 35;
    				longPosition1 = calculatedLength - createCrossSection1;
    				longPosition2 = calculatedLength - createCrossSection2;
    			} else  {
        			final double createCrossSectionRelative1 = 0.6;
        			final double createCrossSectionRelative2 = 0.7;
    				longPosition1 = createCrossSectionRelative1 * calculatedLength;
    				longPosition2 = createCrossSectionRelative2 * calculatedLength;				
    			}
    			CrossSection cs1 = new CrossSection(longPosition1, 0.0, null);    			
    			CrossSection cs2 = new CrossSection(longPosition2, 0.0, null);
    	    	csList.add(cs1);
            	csList.add(cs2);	
    			ArrayList<RoadMarkerAlong> newRmaList = new ArrayList<RoadMarkerAlong>();
    			int newLanes = turnArrowList.size();
    			newRmaList = createRMA(newLanes, laneWidth);	
    			CrossSectionElement cse1 = new CrossSectionElement(cs1, typologyName, laneWidth * newLanes , newRmaList, turnArrowList);
    			newRmaList = createRMA(newLanes, laneWidth);	
    			CrossSectionElement cse2 = new CrossSectionElement(cs2, typologyName, laneWidth * newLanes , newRmaList, turnArrowList);
        		ArrayList<CrossSectionElement> cse1List = new ArrayList<CrossSectionElement>();
            	cse1List.add(cse1);
            	cs1.setCrossSectionElementList_w(cse1List);
        		ArrayList<CrossSectionElement> cse2List = new ArrayList<CrossSectionElement>();
            	cse2List.add(cse2);
            	cs2.setCrossSectionElementList_w(cse2List);
    		} else
   	        if (null != turnArrowList)  {    	        	
  	        	for (TurnArrow ta : turnArrowList)
    	        		cse.getObjects().add(ta);
            }
    			//GT volgende uitcommentarieren
  //  			cse = new CrossSectionElement(cs, typologyName, laneWidth * lanes, rmaList, turnArrowList);
    	}
    	/*if (exitLanes > 0) {
        	// FIXME: looks too much like the if (lanes !- turnArrorList.size()) code above
    		if (lanes != exitLanes) {
    			double longPosition1 = 0;
    			double longPosition2 = 0;
    			final double minimalLengthLinkExitLanes = 40.0;
    			
    			if (calculatedLength > minimalLengthLinkExitLanes)   {
        			final double endPositionExitLanes = .90;
        			final double relativeLengthExitLanes = .80;
    				longPosition1 = 0;
    				longPosition2 = relativeLengthExitLanes * minimalLengthLinkExitLanes;
    				cs.setLongitudalPosition_w(endPositionExitLanes * minimalLengthLinkExitLanes);
    			} else {
        			final double endPositionExitLanes = .90;
        			final double relativeLengthExitLanes = .80;
    				longPosition1 = 0;
    				longPosition2 = relativeLengthExitLanes * calculatedLength;
    				cs.setLongitudalPosition_w( endPositionExitLanes * calculatedLength);
    			}
    			CrossSection cs1 = new CrossSection(longPosition1, 0.0, null);    			
    			CrossSection cs2 = new CrossSection(longPosition2, 0.0, null);
    	    	csList.add(0, cs1);
            	csList.add(1, cs2);	
    			ArrayList<RoadMarkerAlong> newRmaList = new ArrayList<RoadMarkerAlong>();
    			int newLanes = exitLanes;
    			newRmaList = createRMA(newLanes, laneWidth);	
    			CrossSectionElement cse1 = new CrossSectionElement(cs1, typologyName, laneWidth * newLanes , newRmaList, null);
    			newRmaList = createRMA(newLanes, laneWidth);
    			CrossSectionElement cse2 = new CrossSectionElement(cs2, typologyName, laneWidth * newLanes , newRmaList, null);
        		ArrayList<CrossSectionElement> cse1List = new ArrayList<CrossSectionElement>();
            	cse1List.add(cse1);
            	cs1.setCrossSectionElementList_w(cse1List);
        		ArrayList<CrossSectionElement> cse2List = new ArrayList<CrossSectionElement>();
            	cse2List.add(cse2);
            	cs2.setCrossSectionElementList_w(cse2List);
    		}
    	}*/

    	String name = String.valueOf(linkID);
    	importedModel.network.addLink(name, fromNodeID, toNodeID, length, true, csList, pointList);
    }
	
	private static ArrayList<TurnArrow> analyseTurns(String turnLanes, Double laneWidth) {
		int lanes = turnLanes.length();
		int[] outLinkNumber = null;
		ArrayList<TurnArrow> turnArrowList = new ArrayList<TurnArrow>();
		// in case a turn starts with a straight movement, the outLinkNumber starts with a zero (0)
		int outLink = 0;
		boolean right = false;
		boolean straight = false;
		boolean left = false;
		outLinkNumber = new int[] {outLink};
		// first we detect all movements from the incoming link by checking all turns
		for (int i = 0; i < lanes; i++)   {
			char turn = '\0';
			if(Character.isLetter(turnLanes.charAt(i)))
				turn = Character.toLowerCase(turnLanes.charAt(i));
			switch (turn)   {
				case 'r': // Right turn only
					right = true;
					break;
				case 's': // Main (straight on) lane 
					straight = true;
					break;
				case 'l': // Left turn only 
					left = true;
					break;
				case 'q': // Left turn and straight on 
					straight = true;
					left = true;					
					break;
				case 'p': // Right turn and straight on
					right = true;
					straight = true;					
					break;
				case 'u': // Left turn and right turn 
					right = true;
					left = true;					
					break;
				case 'a': // Any turn lane 
					right = true;
					straight = true;
					left = true;					
					break;
				case 'b': // Bus lane (straight on) 
					straight = true;
					break;
			}
		}
		for (int i = 0; i < lanes; i++)   {
			char turn = '\0';
			if(Character.isLetter(turnLanes.charAt(i)))
				turn = Character.toLowerCase(turnLanes.charAt(i));
			switch (turn)   {
				case 'r': // Right turn only
					outLink = 0;
					outLinkNumber = new int[] {outLink};
					break;
				case 's': // Main (straight on) lane 
					if (right)
						outLink = 1;
					else
						outLink = 0;
					outLinkNumber = new int[] {outLink};
					break;
				case 'l': // Left turn only 
					if (right && straight)
						outLink = 2;
					else if (right)
						outLink = 1;
					else if (straight)
						outLink = 1;
					else
						outLink = 0;
					outLinkNumber = new int[] {outLink};
					break;
				case 'q': // Left turn and straight on 
					if (right)
						outLink = 1;
					else
						outLink = 0;
					outLinkNumber = new int[] {outLink, outLink + 1};
					left = true;					
					break;
				case 'p': // Right turn and straight on
					outLink = 0;
					outLinkNumber = new int[] {outLink, outLink + 1};
					break;
				case 'u': // Left turn and right turn 
					outLink = 0;
					outLinkNumber = new int[] {outLink, outLink + 1};
					break;
				case 'a': // Any turn lane 
					outLink = 0;
					outLinkNumber = new int[] {outLink, outLink + 1, outLink + 2};
					break;
				case 'b': // Bus lane (straight on) 
					outLink = 0;
					if (right)
						outLink = 1;
					outLinkNumber = new int[] {outLink};
					break;
			}
			Double lateralPosition = (i * laneWidth) + laneWidth / 2;
			TurnArrow turnArrow = new TurnArrow(null, outLinkNumber, lateralPosition, 0);
			turnArrowList.add(turnArrow);
		}
		return turnArrowList;
	}

	private static int deriveLanes(double capacity, double maxSpeed) {
		int lanes = 0;
		// assume a one hour period
    	final int periodHours = 1;
    	final int speedBorderUrbanFreeway = 95;
    	final int maxSpeedFreeway = 140;    	
    	final int capacityLaneUrban = 2000;
    	final int capacityLaneFreeway = 2500;

    	if (maxSpeed < speedBorderUrbanFreeway) {
			if (capacity / periodHours < capacityLaneUrban)
				lanes = 1;
			else if ((capacity / periodHours >= capacityLaneUrban) && (capacity / periodHours < 2 * capacityLaneUrban))
				lanes = 2;
			else
				lanes = 3;
		}
		
		if (maxSpeed >= speedBorderUrbanFreeway  && maxSpeed < maxSpeedFreeway) {
			if (capacity / periodHours < capacityLaneFreeway)
				lanes = 1;
			else if ((capacity / periodHours >= capacityLaneFreeway) && (capacity / periodHours < capacityLaneFreeway * 2))
				lanes = 2;
			else if ((capacity / periodHours >= capacityLaneFreeway * 2) && (capacity / periodHours < capacityLaneFreeway * 3))
				lanes = 3;
			else if ((capacity / periodHours >= capacityLaneFreeway * 3) && (capacity / periodHours < capacityLaneFreeway * 4))
				lanes = 4;
			else if ((capacity / periodHours >= capacityLaneFreeway * 4) && (capacity / periodHours < capacityLaneFreeway * 5))
				lanes = 5;
			else if (capacity / periodHours >= capacityLaneFreeway * 5)
				lanes = 6;
		}
		return lanes;
	}
	
    private static double calculateLength(List<Vertex> vertices) {	// compute length
        double cumLength = 0;
        for (int i = 1; i <= vertices.size() - 1; i++) {
            double dx = vertices.get(i).getX() - vertices.get(i - 1).getX();
            double dy = vertices.get(i).getY() - vertices.get(i - 1).getY();
            cumLength = cumLength + Math.sqrt(dx * dx + dy * dy);
        }
        return cumLength;
    }
    
    private static ArrayList<TripPattern> importMatrix(String fullFileName) throws IOException  {
    	InputStream fis = null;
    	BufferedReader br = null;
    	ArrayList<TripPattern> tripPatternList = new  ArrayList<TripPattern>();
    	try {
    		fis = new FileInputStream(fullFileName);
    		  DataInputStream in = new DataInputStream(fis);
    		  br = new BufferedReader(new InputStreamReader(in));
    		  String strLine;
    		  //Read File Line By Line
    		  while ((strLine = br.readLine()) != null)   {
    		  // Print the content on the console  
    			  if (strLine.startsWith("*"))
    				  continue;
    			  Scanner s = new Scanner(strLine);
    			  s.useDelimiter("\\s+");
    			  while (s.hasNext()) {
    				  String origin = "z" + s.nextInt();
    				  String destin = "z" + s.nextInt();
    				  ArrayList<Object> activityLocationID = new ArrayList<Object>(); 
    				  activityLocationID.add(origin);
    				  activityLocationID.add(destin);
    				  double count = s.nextDouble();
    				  TripPattern tripPattern = new TripPattern(null, count, activityLocationID);
    				  tripPatternList.add(tripPattern);	  
    			  }
    			  s.close();
    		  }	    		
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fis)
					fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (null != br)
				br.close();
		}
		return tripPatternList;
    }
    
	public Model getImportedModel() {
		return importedModel;
	}

	JButton getFinishButton() {
		return finishButton;
	}

	JButton getNextButton() {
		return nextButton;
	}

	JButton getPrevButton() {
		return prevButton;
	}

}
