package nl.tudelft.otsim.GUI;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import nl.tudelft.otsim.Charts.MeasurementPlan;
import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.GeoObjects.Lane;
import nl.tudelft.otsim.GeoObjects.Link;
import nl.tudelft.otsim.GeoObjects.Network;
import nl.tudelft.otsim.GeoObjects.Node;
import nl.tudelft.otsim.GeoObjects.Vertex;
import nl.tudelft.otsim.ModelIO.ExportModel;
import nl.tudelft.otsim.ModelIO.ImportModelShapeWizard;
import nl.tudelft.otsim.ModelIO.ImportOSM;
import nl.tudelft.otsim.ModelIO.LoadModel;
import nl.tudelft.otsim.ModelIO.SaveModel;
import nl.tudelft.otsim.Simulators.Simulator;
import nl.tudelft.otsim.Simulators.LaneSimulator.LaneSimulator;
import nl.tudelft.otsim.Simulators.LaneSimulator.Movable;
import nl.tudelft.otsim.Simulators.MacroSimulator.MacroSimulator;
import nl.tudelft.otsim.Simulators.RoadwaySimulator.RoadwaySimulator;
import nl.tudelft.otsim.SpatialTools.Planar;
import nl.tudelft.otsim.TrafficDemand.TrafficDemand;

/**
 * This class implements the Main window of OpenTraffic.
 * 
 * @author gftamminga, Peter Knoppers
 *
 */
public class Main extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	/** Locale value used in the application */
	public static Locale locale = new Locale("en", "US");
    volatile static boolean initialized = false;
    /** Main Frame of OpenTraffic */
    public static Main mainFrame = null;
    private Container parent = null;
    
    /**
     * Create main window and parse command line arguments.
     * 
     * @param args Array of String; command line arguments
     */
    public static void main(String args[]) {
    	if (null == Main.mainFrame)
    		throw new Error("GUI has not been (properly) created");
        System.out.println("GUI initialized; process program arguments");
        for (String arg : args) {
        	int pos = arg.indexOf('=');
        	if (pos >= 0) {
        		String left = arg.substring(0, pos);
        		String right = arg.substring(pos + 1);
        		if (left.equalsIgnoreCase("initialdir"))
        			mainFrame.initialDirectory = right;
        		else if (left.equalsIgnoreCase("network"))
        			mainFrame.loadModel(right);
        		else if (left.equalsIgnoreCase("load"))
        			mainFrame.loadFile(right);
        		else if (left.equalsIgnoreCase("import"))
					try {
						mainFrame.importModel(right);
					} catch (Exception e) {
						WED.showProblem(WED.ENVIRONMENTERROR, "Could not import file \"%s\"\n%s", right, WED.exeptionStackTraceToString(e));
					}
        		else if (left.equalsIgnoreCase("Seed"))
        			Main.seed = Integer.parseInt(right);
        		else if (left.equalsIgnoreCase("EndTime"))
        			Main.endTime = Double.parseDouble(right);
        		else if (left.equalsIgnoreCase("vehicleLifeLog"))
        			Main.mainFrame.vehicleLifeLogFileName = right;
        		else if (left.equalsIgnoreCase("GenerateEvent"))
    				Main.mainFrame.actionPerformed(new ActionEvent(Main.mainFrame, 0, right));
        		else if (left.equalsIgnoreCase("RunSimulation"))
        			Main.mainFrame.runSimulation();
        		else if (left.equalsIgnoreCase("SetStatus"))
        			Main.mainFrame.setStatus(-1, "%s", right);
        		else
        			WED.showProblem(WED.ENVIRONMENTERROR, "Unknown program argument \"%s\" (ignored)", arg);
        	} else
    			WED.showProblem(WED.ENVIRONMENTERROR, "Unknown program argument \"%s\" (ignored)", arg);        		
        }
        System.out.println("Argument processing finished");
        Log.logMessage(null, false, "Ready");	// test that LogMessage can log to the Event log
    }

	private void runSimulation() {
		int index = tabbedPaneProperties.getSelectedIndex();
		System.out.println("runSimulation: index is " + index);
		if (index < 0)
			return;
		ActionEvent event = new ActionEvent(this, 0, "RunFast");
    	if (roadWaySimulatorIndex == index) {
    		if (0 == roadwaySimulatorControlPanel.getComponentCount()) {
    			WED.showProblem(WED.INFORMATION, "Could not load roadway simulator");
    			return;
    		}
    		((SchedulerController) roadwaySimulatorControlPanel.getComponent(0)).actionPerformed(event);
    	} else if (laneSimulatorIndex == index) {
    		if (0 == laneSimulatorControlPanel.getComponentCount()) {
    			WED.showProblem(WED.INFORMATION, "Could not load lane simulator");
    			return;
    		}
    		((SchedulerController) laneSimulatorControlPanel.getComponent(0)).actionPerformed(event);
    	} else 
    		throw new Error("no Simulator selected");
	}

	/** GraphicsPanel used in the main window */
	public GraphicsPanel graphicsPanel;
	private String workingDir;
	JPanel panelMeasurementPlan;
	/** Directory that was specified on the command line */
    public String initialDirectory = workingDir; 
    private String fileSelectedNetwork;
    /** Currently loaded traffic model */
    public Model model;
    private static int seed = 1;	// seed for the random generator of the Simulator
    private static double endTime = 3600d;	// end time of the Simulator
    /** JMenuItem of the Export Model ... menu */
    private JPopupMenu measurementPlanPopup;
    
    /** Name of the OpenTraffic application */
    public final String myName = "Open Traffic Simulator";
  //shape
    private JTable table;
  //shape
    
    /**
     * Create the main window.
     * @param parent Container; either a {@link StandAlone} javax.swing.JFrame or a {@link OTSim} java.applet.Applet
     */
    public Main(Container parent) {
    	Main.mainFrame = this;
    	this.parent = parent;

    	// Build the GUI
        textAreaLogging = new javax.swing.JTextArea();	// Make this one early
        setLayout(new java.awt.BorderLayout(10, 10));
        
		statusBar = new StatusBar();
		
		statusBar.addZone(StatusBar.DEFAULT_ZONE, mainStatusLabel = new JProgressBar(0, 100), "*");
		mainStatusLabel.setStringPainted(true);
		statusBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				actionPerformed(new ActionEvent(statusBar, 0, "statusBarClicked"));
			}
		});
		setStatus(-1, "");
		add(statusBar, BorderLayout.SOUTH);
		
		setTitle(myName);
        
        tabbedPaneProperties = new javax.swing.JTabbedPane();
        tabbedPaneProperties.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        //javax.swing.JToolBar toolBar = new javax.swing.JToolBar();
        //toolBar.setRollover(true);
        //toolBar.setPreferredSize(new java.awt.Dimension(13, 22));
        //getContentPane().add(toolBar, java.awt.BorderLayout.PAGE_START);

        // configure the event log
        textAreaLogging.setColumns(20);
        textAreaLogging.setRows(5);
        JScrollPane scrollPaneEventLog = new javax.swing.JScrollPane();
        scrollPaneEventLog.setAutoscrolls(true);
        scrollPaneEventLog.setViewportView(textAreaLogging);
        tabbedPaneProperties.addTab("Event log", scrollPaneEventLog);
        
        tabbedPaneProperties.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				int index = tabbedPaneProperties.getSelectedIndex();
				if (index < 0)
					return;		// no tab is currently selected;
	        	// Send an event to indicate that the active tab has changed
				// System.out.println("tab index is " + index + ", " + tabbedPaneProperties.getTitleAt(index));
				actionPerformed(new ActionEvent(tabbedPaneProperties, index, "propertiesTabChanged"));
			}
        });

        JScrollPane scrollPaneProperties = new javax.swing.JScrollPane();
        scrollPaneProperties.setViewportView(tabbedPaneProperties);

        add(scrollPaneProperties, java.awt.BorderLayout.LINE_START);

        //shape
        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(new DefaultTableModel(5, 5));
        table.setPreferredScrollableViewportSize(new Dimension(500, 200));
        //shape
        
        // Build the main menu
        menuBar = new javax.swing.JMenuBar();
        
        // File menu        
        JMenu menuFile = new JMenu();
        menuFile.setText("File");
        
        menuFile.add(makeMenu("New", "new", "New.png", true));
        menuFile.add(makeMenu("Open", "load", "Briefcase.png", haveDiskAccess()));
        menuFile.add(makeMenu("Save", "save", "Save.png", haveDiskAccess()));
        menuFile.add(makeMenuItem("Log vehicle creation and destruction", "logVehicleLifes", "Notes.png", haveDiskAccess()));
        menuFile.add(makeMenuItem("Import model ...", "openDialogImportModel", "Back.png", haveDiskAccess()));
        menuFile.add(makeMenuItem("Import OpenStreetMap Network ...", "openDialogImportOSM", "Back.png", haveDiskAccess()));
        menuFile.add(makeMenuItem("Exit", "Exit", "Exit.png", haveDiskAccess()));
        
        menuBar.add(menuFile);
        
        // View menu
        javax.swing.JMenu menuView = new javax.swing.JMenu();
        menuView.setText("View");
        menuBar.add(menuView);
        
        // View zoom to scene (bounding box)
        menuView.add(makeMenuItem("Entire network", "zoomToScene", "Expand.png", true));
        menuView.add(makeMenuItem("Zoom in", "zoomIn", "Zoom.png", true));
        menuView.add(makeMenuItem("Zoom out", "zoomOut", "Earth.png", true));
        menuView.add(makeMenuItem("Zoom to link ...", "zoomToLink", null, true));
        menuView.add(makeMenuItem("Zoom to node ...", "zoomToNode", null, true));
        menuView.add(makeMenuItem("Zoom to lane ...", "zoomToLane", null, true));
        menuView.add(makeMenuItem("Zoom to vehicle ...", "zoomToVehicle", null, true));

        // Show the menu
        if (null != parent) {
        	if (parent instanceof StandAlone)
        		((StandAlone) parent).setMenuBar(menuBar);
        	else if (parent instanceof OTSim)
        		((OTSim) parent).setMenuBar(menuBar);
        	else
        		throw new Error("Cannot set menu bar in containing window");
        }

    	graphicsPanel = new GraphicsPanel();
   
    	add(graphicsPanel, java.awt.BorderLayout.CENTER);
    	
        JPanel controls = new JPanel();
        controls.setLayout(new GridLayout(20,2));
        controls.add(showNodes = makeCheckBox("Show Nodes", "redrawMap", true));
        controls.add(showLabelsOnAutogeneratedNodes = makeCheckBox("Label auto-generated Nodes", "redrawMap", false));
        controls.add(showLinks = makeCheckBox("Show Links", "redrawMap", true));
        controls.add(showDrivable = makeCheckBox("Only drivable links", "redrawMap", false));
        controls.add(showPaths = makeCheckBox("Show Paths", "redrawMap", false));
        controls.add(showBuildings = makeCheckBox("Show Buildings", "redrawMap", true));
        controls.add(showPolyZones = makeCheckBox("Show PolyZones", "redrawMap", true));
        controls.add(showLaneIDs = makeCheckBox("Show Lane IDs", "redrawMap", false));
        controls.add(showFormPoints = makeCheckBox("Show form points", "redrawMap", false));
        controls.add(showFormLines = makeCheckBox("Show form lines", "redrawMap", false));
        
        JScrollPane scrollPaneNetworkEditor = new JScrollPane();
        scrollPaneNetworkEditor.setViewportView(controls);
        tabbedPaneProperties.add("Network editor", scrollPaneNetworkEditor);
        
        Main.mainFrame.tabbedPaneProperties.setSelectedIndex(Main.mainFrame.tabbedPaneProperties.indexOfComponent(scrollPaneNetworkEditor));
        
        controls = new JPanel();
        controls.setLayout(new BorderLayout());
        controls.add(comboBoxMeasurementPlans = new JComboBox<MeasurementPlan>(), BorderLayout.NORTH);
        comboBoxMeasurementPlans.setActionCommand("MeasurementPlanChanged");
        comboBoxMeasurementPlans.addActionListener(this);
        JPanel subPanel = new JPanel(new BorderLayout());
        controls.add(subPanel, BorderLayout.CENTER);
        subPanel.add(editMeasurementPlanName = new JTextField(), BorderLayout.NORTH);
        editMeasurementPlanName.setVisible(false);
        editMeasurementPlanName.addFocusListener(new FocusListener() {
        	@Override
        	public void focusGained(FocusEvent arg0) {
        		// Do nothing
        	}

        	@Override
        	public void focusLost(FocusEvent arg0) {
        		System.out.println("Focus lost");
				actionPerformed(new ActionEvent(statusBar, 0, "UpdateMeasurementPlanName"));
        	}

        });
        editMeasurementPlanName.addKeyListener(new KeyAdapter() {
        	@Override
			public void keyTyped(KeyEvent e) {
        		String key = "" + e.getKeyChar();
        		Pattern pattern = Pattern.compile("[ a-zA-Z0-9-_\b]");
        		Matcher matcher = pattern.matcher(key);
        		if (! matcher.find()) {
        			e.consume();
        			Toolkit.getDefaultToolkit().beep();
        		}
        	}
        	
        });
        subPanel.add(panelMeasurementPlan = new JPanel(), BorderLayout.CENTER);
        JScrollPane scrollPaneMeasurementPlans = new JScrollPane();
        scrollPaneMeasurementPlans.setViewportView(controls);
        tabbedPaneProperties.add("Measurement plans", scrollPaneMeasurementPlans);
        measurementPlanIndex = tabbedPaneProperties.indexOfComponent(scrollPaneMeasurementPlans);
        
        measurementPlanPopup = new JPopupMenu();
        measurementPlanPopup.add(makeMenuItem("Edit name", "EditMeasurementPlanName", "Bubble.png", true));
        measurementPlanPopup.add(makeMenuItem("Delete measurement plan", "DeleteMeasurementPlan", "Delete.png", true));
        comboBoxMeasurementPlans.add(measurementPlanPopup);
        comboBoxMeasurementPlans.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
		});
        
        controls = new JPanel();
        controls.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        controls.add(roadwaySimulatorControlPanel = new JPanel(), gbc);
        gbc.gridy++;
        // add a filler
        gbc.weighty = 0.5;
        controls.add(new JPanel(), gbc);
        
        JScrollPane scrollPaneRoadwaySimulator = new JScrollPane();
        scrollPaneRoadwaySimulator.setViewportView(controls);
        tabbedPaneProperties.add(RoadwaySimulator.simulatorType, scrollPaneRoadwaySimulator);
        roadWaySimulatorIndex = tabbedPaneProperties.indexOfComponent(scrollPaneRoadwaySimulator);
        
        controls = new JPanel();
        controls.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        controls.add(laneSimulatorControlPanel = new JPanel(), gbc);
        gbc.gridy++;
        controls.add(checkBoxShowLeader = makeCheckBox("Show leader", "LaneSimulatorRedraw", false), gbc);
        gbc.gridy++;
        controls.add(checkBoxShowFollower = makeCheckBox("Show follower", "LaneSimulatorRedraw", false), gbc);
        gbc.gridy++;
        controls.add(checkBoxShowLCVehicles = makeCheckBox("Show lane changing ghosts", "LaneSimulatorRedraw", false), gbc);
        controls.add(new JPanel());
        // add a filler
        gbc.gridy++;
        gbc.weighty = 0.5;
        controls.add(new JPanel(), gbc);
        
        JScrollPane scrollPaneLaneSimulator = new JScrollPane();
        scrollPaneLaneSimulator.setViewportView(controls);
        tabbedPaneProperties.add("Lane Simulator", scrollPaneLaneSimulator);
        laneSimulatorIndex = tabbedPaneProperties.indexOfComponent(scrollPaneLaneSimulator);
        
        JScrollPane scrollPaneMacroSimulator = new JScrollPane();
        scrollPaneMacroSimulator.setViewportView(macroSimulatorControlPanel = new JPanel());
        tabbedPaneProperties.add("Macro Simulator", scrollPaneMacroSimulator);
        macroSimulatorIndex = tabbedPaneProperties.indexOfComponent(scrollPaneMacroSimulator);
        
        try {
			initialDirectory = workingDir = System.getProperty("user.dir");	// fails when running as Applet
		} catch (Exception e1) {
			initialDirectory = workingDir = ".";
		}
        
        mainFrame.newModel();
    }
    
    private boolean haveDiskAccess() {
    	return parent instanceof StandAlone;
    }

    /**
     * Change the text in the title bar of the main window.
     * <br /> This method has no effect when running as an Applet.
     * @param caption String; the new text for the title bar of the main window
     */
	public void setTitle(String caption) {
    	if (parent instanceof StandAlone)
    		((StandAlone) parent).setTitle(caption);
	}

	private void maybeShowPopup (MouseEvent me) {
		if ((! me.isPopupTrigger()) || (comboBoxMeasurementPlans.getItemCount() == 0))
			return;
		Point p = new Point(me.getX(), me.getY());
			measurementPlanPopup.show(comboBoxMeasurementPlans, p.x, p.y);
    }

    /**
     * Create a JCheckBox and initialize some of its properties
     * @param caption String; caption of the JCheckBox
     * @param actionCommand String; actionCommand of the JCheckBox. If non-null
     * <code>this</code> is added to the ActionListeners of the JCheckBox
     * @param selected Boolean; initial state of the JCheckBox
     * @return JCheckBox; the newly created JCheckBox
     */
    JCheckBox makeCheckBox (String caption, String actionCommand, boolean selected) {
    	JCheckBox checkBox = new JCheckBox(caption);
    	checkBox.setSelected(selected);
    	if (null != actionCommand) {
    		checkBox.setActionCommand(actionCommand);
    		checkBox.addActionListener(this);
    	}
    	return checkBox;
    }

    /**
     * Create a JMenuItem and initialize some of its properties
     * @param caption String; caption of the JMenu
     * @param actionCommand String; actionCommand of the JMenu. If non-null
     * <code>this</code> is added to the ActionListeners of the JMenuItem
     * @return JMenuItem; the newly created JMenuItem
     */
    javax.swing.JMenuItem makeMenuItem(String caption, String actionCommand, String iconName, boolean enabled) {
        javax.swing.JMenuItem menuItem = new javax.swing.JMenuItem();
        menuItem.setEnabled(enabled);
        menuItem.setText(caption);
        if (null != actionCommand) {
        	menuItem.setActionCommand(actionCommand);
            menuItem.addActionListener(this);
        }
        // Try to load the image from the resources
        String imgLocation = "/nl/tudelft/otsim/resources/" + iconName;
        java.net.URL imageURL = Main.mainFrame.getClass().getResource(imgLocation);
        if (imageURL != null)
            menuItem.setIcon(new ImageIcon(imageURL, caption));
        return menuItem;
    }
    
    javax.swing.JMenu makeMenu(String caption, String actionCommandPrefix, String iconName, boolean enabled) {
    	javax.swing.JMenu menu = new javax.swing.JMenu();
    	menu.setEnabled(enabled);
    	menu.setText(caption);
        // Try to load the image from the resources
        String imgLocation = "/nl/tudelft/otsim/Resources/" + iconName;
        java.net.URL imageURL = Main.mainFrame.getClass().getResource(imgLocation);
        if (imageURL != null)
            menu.setIcon(new ImageIcon(imageURL, caption));
    	menu.add(makeMenuItem("network ...", actionCommandPrefix + " network", null, true));
    	menu.add(makeMenuItem("demand ...", actionCommandPrefix + " demand", null, true));
    	if (actionCommandPrefix.equals("save")) {
    		menu.add(saveMeasurementPlan = new javax.swing.JMenu("measurement plan"));
    		saveMeasurementPlan.setEnabled(false);
    	} else
    		menu.add(makeMenuItem("measurement plan ...", actionCommandPrefix + " measurementPlan", null, true));
    	menu.add(makeMenuItem("model ...", actionCommandPrefix + " model", null, true));
    	return menu;
    }

    private boolean importModel(String fileName) throws Exception {
    	int lastPos = fileName.lastIndexOf(".");
    	if (lastPos < 0)
    		throw new Exception("import file \"" + fileName + "\" has no file type");
        String type = fileName.substring(lastPos + 1);
    	if (type.equalsIgnoreCase("osm"))
    		model.network = ImportOSM.loadOSM(fileName);
    	else
    		throw new Exception("don't know how to import file of type \"" + type + "\"");
    	mainFrame.setTitle(fileName);
    	zoomToScene();
    	setActiveGraph();
    	return true;
	}

    /**
     * Load and display a traffic model.
     * @param filename String; file name of the model file
     * @return Boolean; true on success; false if some error occurred
     */
    public boolean loadModel(String filename) {
        try {
        	model = new Model(filename, null);
		} catch (Exception e) {
			WED.showProblem(WED.ENVIRONMENTERROR, "Error loading model \"%s\":\r\n%s", 
					filename, WED.exeptionStackTraceToString(e));
			e.printStackTrace();
			return false;
		}
        fileSelectedNetwork = filename;
        mainFrame.setTitle(myName + " - " + fileSelectedNetwork);
    	setActiveGraph();
    	zoomToScene();
        System.out.printf("Loaded network \"%s\"\r\n", filename);
        return true;
    }
        
    private LoadModel loadModel = null;
    /**
     * Load a load-model.
     * @return Boolean; true on success, false if some error occurred
     * The dialog opens the class LoadModel that enables the loading 
     * of the geo-data (network and environment) and the traffic demand
     */
    private boolean openDialogLoadModel() {
    	loadModel = new LoadModel();
        try {
        	model = loadModel.getLoadedModel();
		} catch (Exception e) {
			WED.showProblem(WED.ENVIRONMENTERROR, "Error loading loadModel:\r\n%s", 
					WED.exeptionStackTraceToString(e));
			e.printStackTrace();
			return false;
		}
        return true;
    }
    
    /**
     * Return the currently loaded traffic load model.
     * @return LoadModel; the currently loaded Load Model
     */
    public LoadModel getLoadModel() {
		return loadModel;
	}

    /**
     * Return the file name of the currently loaded Network. 
     * @return String; name of the Network file
     */
    public String getFileSelectedNetwork() {
		return fileSelectedNetwork;
	}

	private SaveModel saveModel = null;
	
    private boolean openDialogSaveModel() {
    	saveModel = new SaveModel();
		return true;
    }
    
    public SaveModel getSaveModel() {
		return saveModel;
	}
	
	private ExportModel exportModel = null;
	
    private boolean openDialogExportModel() {
    	exportModel = new ExportModel();
		return true;
    }
    
    public ExportModel getExportModel() {
		return exportModel;
	}

    private ImportModelShapeWizard importModelShapeWizard = null;    
    
    public boolean openDialogImportModel() {
    	importModelShapeWizard = new ImportModelShapeWizard();
        try {
        	model = importModelShapeWizard.getImportedModel();
		} catch (Exception e) {
			WED.showProblem(WED.ENVIRONMENTERROR, "Error importing model shapes:\r\n%s", 
					 WED.exeptionStackTraceToString(e));
			e.printStackTrace();
			return false;
		}
        return true;
    }
    
    public ImportModelShapeWizard getImportModelShapeWizard() {
		return importModelShapeWizard;
	}
    
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JTabbedPane tabbedPaneProperties;
    /** JTextArea used for logging debug information (with {@link Log}) */
    public javax.swing.JTextArea textAreaLogging;
    /** Nodes should be drawn when the showNodes check box is checked */
    public JCheckBox showNodes;
    /** Show labels on auto-generated Nodes */
    public JCheckBox showLabelsOnAutogeneratedNodes;
    /** Links should be drawn when the showLinks check box is checked */
    public JCheckBox showLinks;

    /** 
     * Only drive-able links should be drawn if the showDrivable links check 
     * box is checked */
    public JCheckBox showDrivable;
    /** 
     * The paths from the currently selected origin to the currently selected 
     * destination should be drawn in the showPaths check box is checked. 
     */
    public JCheckBox showPaths;
    /**
     * Buildings should be drawn if and only if the showBuildings check box is 
     * checked. */
    public JCheckBox showBuildings;
    /**
     * PolyZones should be drawn if and only if the showPolyZones check box is
     * checked.
     */
    public JCheckBox showPolyZones;
    /**
     * Lane IDs should be drawn if and only if the showLaneIDs check box is
     * checked.
     */
    public JCheckBox showLaneIDs;
    /**
     * Form points of lanes should be drawn as circles if this check box is 
     * checked.
     */
    public JCheckBox showFormPoints;
    /**
     * Form points of lanes should be drawn by connecting lines if this check
     * box is checked.
     */
    public JCheckBox showFormLines;
    
    private JPanel laneSimulatorControlPanel;
    private JPanel roadwaySimulatorControlPanel;
    private JPanel macroSimulatorControlPanel;
    /** Simulators must show the leader of a vehicle. */
    public JCheckBox checkBoxShowLeader;
    /** Simulators must show the follower of a vehicle. */
    public JCheckBox checkBoxShowFollower;
    /** LCVehicles should be drawn then the checkBoxShowLCVehicles check box is checked */
	public JCheckBox checkBoxShowLCVehicles;
	private StatusBar statusBar;
	private JProgressBar mainStatusLabel;
	private final int roadWaySimulatorIndex;
	private final int laneSimulatorIndex;
	private final int macroSimulatorIndex;
	private javax.swing.JMenu saveMeasurementPlan; 
	private JComboBox<MeasurementPlan> comboBoxMeasurementPlans;
	private JTextField editMeasurementPlanName;
	
	/**
	 * Update the save measurement plan menu item so it expands to show the
	 * names of all measurement plans in the Model.
	 */
	private void measurementPlanListChanged() {
		MeasurementPlan currentMeasurementPlan = (MeasurementPlan) comboBoxMeasurementPlans.getSelectedItem();
		while (saveMeasurementPlan.getItemCount() > 0)
			saveMeasurementPlan.remove(0);
		for (int i = 0; i < model.measurementPlanCount(); i++)
			saveMeasurementPlan.add(makeMenuItem (model.getMeasurementPlan(i).getName(), "save measurementPlan", null, haveDiskAccess()));
		saveMeasurementPlan.setEnabled(saveMeasurementPlan.getItemCount() > 0);
		comboBoxMeasurementPlans.removeAllItems();
		for (int i = 0; i < model.measurementPlanCount(); i++) {
			MeasurementPlan mp = model.getMeasurementPlan(i);
			comboBoxMeasurementPlans.addItem(mp);
			if (currentMeasurementPlan == mp)
				comboBoxMeasurementPlans.setSelectedItem(mp);
		}
	}
    
	/** 
	 * Constant to use in setProgress if the current value should be maintained. 
	 */
	private final double currentProgressBarValue = -2d;
	private double lastStatusValue = -1d;
	private int measurementPlanIndex = -1;
	/**
	 * Update the message in the status bar and set the position of the
	 * progress bar that is embedded in the status bar.
	 * @param value Double new value for the progress bar, or
	 * <code>currentProgressBarValue</code> to keep the current value
	 * @param format String; format string for the new status bar message
	 * @param arguments Zero or more java.lang.Object arguments that are
	 * used by the <code>format</code> String
	 */
	public void setStatus(double value, String format, Object... arguments) {
		//System.out.println("format: " + format);
		if (currentProgressBarValue == value)
			value = lastStatusValue;
		lastStatusValue = value;
		String message;
		try {
			message = String.format(locale, format, arguments);
		} catch (Exception e) {
			message = "Caught error in String.format (\"" + format + "\")";
		}
		mainStatusLabel.setValue((int) value);
		mainStatusLabel.setString(" " + message + " ");
	}
	
	/**
	 * Return the textual description of the traffic {@link Model} for a Simulator.
	 * @param type String; type of Simulator to create the textual description for
	 * @return String; the textual description of the traffic {@link Model} 
	 */
	public static String configuration(String type) {
		Model model = mainFrame.model;
		String config = String.format("EndTime:\t%.2f\nSeed:\t%d\n", endTime, seed);
		if (type.equals(LaneSimulator.simulatorType))
			return config + model.exportToMicroSimulation();
		if (type.equals(RoadwaySimulator.simulatorType))
			return config + model.exportToSubMicroSimulation();
		if (type.equals(MacroSimulator.simulatorType))
			return config + model.exportToMacroSimulation();
		throw new Error("Do not know how to create configuration of type " + type);
	}
	
	/**
	 * Schedule a re-paint for the map display 
	 */
	public void setActiveGraph() {
		int index = tabbedPaneProperties.getSelectedIndex();
		System.out.println("SetActiveGraph: index is " + index);
		if (index < 0)
			return;
		String simulatorType = null;
		JPanel controlPanel = null;
		if (roadWaySimulatorIndex == index) {
			simulatorType = RoadwaySimulator.simulatorType;
			controlPanel = roadwaySimulatorControlPanel;
		} else if (laneSimulatorIndex == index) {
			simulatorType = LaneSimulator.simulatorType;
			controlPanel = laneSimulatorControlPanel;
		} else if (macroSimulatorIndex == index) {
			simulatorType = MacroSimulator.simulatorType;
			controlPanel = macroSimulatorControlPanel;
		} else if ((measurementPlanIndex == index) && (comboBoxMeasurementPlans.getSelectedIndex() >= 0))
    		graphicsPanel.setClient((GraphicsPanelClient) comboBoxMeasurementPlans.getSelectedItem());
    	else if (null != model)
    	  	graphicsPanel.setClient(model.network);

		if (null != controlPanel) {
			if (0 == controlPanel.getComponentCount())
				controlPanel.add(new SchedulerController(new Scheduler(simulatorType, graphicsPanel)));
			if (0 == controlPanel.getComponentCount()) {
				WED.showProblem(WED.INFORMATION, "Could not load " + simulatorType);
				return;
			}
			graphicsPanel.setClient(((SchedulerController)(controlPanel.getComponent(0))).getScheduler().getSimulator());
		}
		graphicsPanel.repaint();			
    	System.out.println("SetActiveGraph: done");
	}
	
	/**
	 * Let the user decide if unsaved changes may be discarded, or must be
	 * saved.
	 * @param storable {@link Storable}; the type of data that is about to be discarded
	 * @return Boolean; true if there are no unsaved changes, or the user
	 * agrees to discard them; false if the user decides not to quit
	 */
	public boolean mayDiscardChanges(Storable storable) {
		if (null == storable)
			return true;
    	while (storable.isModified()) {
    		Object[] options = {"Yes", "No", "Cancel"};
    		switch (JOptionPane.showOptionDialog(mainFrame,
    				storable.description() + " is modified; save changes?", "Warning",
    				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
    				null, options, options[0])) {
    			case JOptionPane.YES_OPTION: {
    				saveStorable(storable);
    				continue;
    			}
    			case JOptionPane.NO_OPTION: return true;
    			case JOptionPane.CANCEL_OPTION: return false;
    		}
     	}
		return true;
	}
	
	private void openDialogImportOSM() {
		if (! mayDiscardChanges(model.network))
			return;	// cancel Open file operation
		String fileName = FileDialog.showFileDialog(true, "osm", "Open StreetMap", initialDirectory);
		if (null != fileName)
			try {
				model.network = ImportOSM.loadOSM(fileName);
				setActiveGraph();
				zoomToScene();
				setActiveGraph();
			} catch (Exception e) {
				if (e instanceof java.io.FileNotFoundException)
					WED.showProblem(WED.ENVIRONMENTERROR, "File \"%s\" does not exist", fileName);
				else
					WED.showProblem(WED.ENVIRONMENTERROR, "Could not load %s from file %s:\r\n%s", model.network.description(), fileName, WED.exeptionStackTraceToString(e));
			}
	}


	/**
	 * Check for any unsaved changes.
	 * @return Boolean; true if there are no unsaved changes, or the user has
	 * indicated that those changes should be discarded; false if there are
	 * unsaved changes and the user wants to preserve them
	 */
	public boolean mayDiscardChanges() {
		// TODO add check for unsaved settings when those are made Storable
		// If/when Population becomes a Storable; add that too.
		if (null == model)
			return true;
		return mayDiscardChanges(model);  
	}
	
	private Storable identifyStorable(String name) {
		// TODO add support for Storable settings
		if (null == model)
			model = new Model();
		if (name.equals("network"))
			return model.network;
		else if (name.equals("demand"))
			return model.trafficDemand;
		else if (name.equals("measurementPlan"))
			return new MeasurementPlan(model);
		else if (name.equals("model"))
			return model;
		else if (name.equals("settings"))
			throw new Error("settings is not yet storable");
		throw new Error("Unknown storable type " + name);
	}
	
	private Storable identifyStorableExtension(String extension) {
		if (extension.equals(Network.FILETYPE))
			return model.network;
		else if (extension.equals(TrafficDemand.FILETYPE))
			return model.trafficDemand;
		else if (extension.equals(Model.FILETYPE))
			return model;
		else if (extension.equals(MeasurementPlan.FILETYPE))
			return new MeasurementPlan();
		return null;
	}
	
	private void newStorable(Storable storable) {
		if (! mayDiscardChanges(storable))
			return;		// cancel New
		if (storable instanceof Network)
			model.network = new Network(null);
		else if (storable instanceof TrafficDemand)
			model.trafficDemand = new TrafficDemand(model);
		else if (storable instanceof MeasurementPlan) {
			model.addMeasurementPlan(new MeasurementPlan(model));
			measurementPlanListChanged();
		} else if (storable instanceof Model) {
			model = new Model();
			setTitle(myName);
		} else
			throw new Error("Cannot happen");
		setActiveGraph();
	}
	
	private void loadStorable(Storable storable) {
		if (! mayDiscardChanges(storable))
			return;	// cancel Open file operation
		String fileName = FileDialog.showFileDialog(true, storable.fileType(), storable.description(), initialDirectory);
		if (null != fileName)
			loadStorable(storable, fileName);
	}
	
	private void loadStorable(Storable storable, String fileName) {
		try {
			ParsedNode pn = new ParsedNode(fileName);
			int numberOfKeys = pn.getKeys().size();
			if (numberOfKeys < 1)
				throw new Exception("File " + fileName + " contains no data");
			else if (numberOfKeys > 1)
				throw new Exception("File " + fileName + " contains more than one " + storable.description());
			String key = (String) pn.getKeys().toArray()[0];
			String expectedKey = storable instanceof Network ? Network.XMLTAG
					: storable instanceof TrafficDemand ? TrafficDemand.XMLTAG
							: storable instanceof MeasurementPlan ? MeasurementPlan.XMLTAG
									: storable instanceof Model ? Model.XMLTAG : "oops";
			if (! key.equals(expectedKey))
				throw new Exception("file " + fileName + " contains wrong root node (got \"" + key + "\", expected \"" + expectedKey + "\")");
			if (pn.size(key) != 1)
				throw new Error("XML node contains " + pn.size(key) + " " + storable.description()  + "s (should be 1)");
				ParsedNode subNode = pn.getSubNode(key,  0);
				if (storable instanceof Network)
					storable = model.network = new Network(subNode, null);
				else if (storable instanceof TrafficDemand) {
					storable = model.trafficDemand = new TrafficDemand(model, subNode);
					model.trafficDemand.rebuild();
				} else if (storable instanceof MeasurementPlan) {
					model.addMeasurementPlan((MeasurementPlan)(storable = new MeasurementPlan(model, subNode)));
					measurementPlanListChanged();
				} else if (storable instanceof Model) {
					storable = model = new Model(pn);
					measurementPlanListChanged();
				} else
					throw new Error("Cannot happen");
			setActiveGraph();
			if ((storable instanceof Network) || (storable instanceof Model))
				zoomToScene();
			if ((storable instanceof Model))
				this.setTitle(myName + " - " + fileName);
			storable.setStorageName(fileName);
			initialDirectory = new File(fileName).getParent();
		} catch (Exception e) {
			if (e instanceof java.io.FileNotFoundException)
				WED.showProblem(WED.ENVIRONMENTERROR, "File \"%s\" does not exist", fileName);
			else
				WED.showProblem(WED.ENVIRONMENTERROR, "Could not load %s from file %s:\r\n%s", storable.description(), fileName, WED.exeptionStackTraceToString(e));
		}		
	}
	
	/**
	 * Extract the file type from a file name
	 * @param fileName String; the name of the file
	 * @return String; the type (without dot), or empty string if the file does not have a file type
	 */
	public static String getFileExtension(String fileName) {
		String lastElement = "" + Paths.get(fileName).getFileName();
		int pos = lastElement.lastIndexOf(".");
		if (pos < 0)
			return "";
		return lastElement.substring(pos + 1);
	}
	
	private void loadFile(String fileName) {
		String extension = getFileExtension(fileName);
		if ("".equals(extension)) {
			WED.showProblem(WED.ENVIRONMENTERROR, "Cannot identify type of file \"%s\"", fileName);
			return;
		}
		Storable storable = identifyStorableExtension(extension);
		if (null == storable)
			WED.showProblem(WED.ENVIRONMENTERROR, "Cannot identify file type (\"%s\") of \"%s\"", extension, fileName);
		else {
			loadStorable(storable, fileName);
		}
	}
		
	private void saveStorable(Storable storable) {
		String defaultName = storable.storageName();
		if (null == defaultName)
			defaultName = initialDirectory;
		String fileName = FileDialog.showFileDialog(false, storable.fileType(), storable.description(), defaultName);
		if (null == fileName)
			return;
		StaXWriter staXWriter = null;
		boolean result = true;
		try {
			staXWriter = new StaXWriter(fileName, true);
			if (! storable.writeXML(staXWriter))
				throw new Exception("Could not write trafficDemand");
			if (! staXWriter.close())
				throw new Exception("Could not close XML file");
		} catch (Exception e) {
			result = false;
			WED.showProblem(WED.ENVIRONMENTERROR, "Could not save %s:\r\n", storable.description(), WED.exeptionStackTraceToString(e));
		}
		if (result) {
			storable.clearModified();
			storable.setStorageName(fileName);
		}
		initialDirectory = new File(fileName).getParentFile().getAbsolutePath();
	}
	
	/**
	 * Close the program if there are no unsaved changed, or the user elects
	 * to discard the unsaved changes.
	 */
	public void closeProgramCheck() {
		if (mayDiscardChanges ())
			System.exit(0);
	}
	
	private void newModel() {
		if (! mayDiscardChanges())
			return;
		model = new Model();
		model.network = new Network(null);
        fileSelectedNetwork = "";
        mainFrame.setTitle(myName);
    	setActiveGraph();
        System.out.println("created new empty network");
	}
	
	/**
	 * Set zoom and pan to show the entire network.
	 */
	public void zoomToScene() {
		final int margin = 20; // pixels
		// TODO: figure out the bounding box by calling repaintGraph
		// for now we will compute the bounding box of the nodes in the network
		graphicsPanel.setZoom(1, new Point2D.Double(0, 0));
		if ((null == model) || (null == model.network))
			return;
		Line2D.Double bbox = null;
		for (Node node : model.network.getAllNodeList(true))
			bbox = Planar.expandBoundingBox(bbox, node.getX(), node.getY());
		if (null != bbox)
			setZoomRect(bbox, margin);
	}
	
	private void setZoomRect(Line2D.Double bbox, int margin) {
		double xRatio = (bbox.getX2() - bbox.getX1() + margin) / (graphicsPanel.getWidth() - 2 * margin);
		double yRatio = (bbox.getY2() - bbox.getY1() + margin) / (graphicsPanel.getHeight() - 2 * margin);
		double ratio = xRatio > yRatio ? xRatio : yRatio;
		//System.out.format("x: [%.2f - %.2f], y: [%.2f - %.2f], width %d, height %d ratio %.4f\r\n", minX, maxX, minY, maxY, graphicsPanel.getWidth(), graphicsPanel.getHeight(), ratio);
		if (Double.isInfinite(ratio))
			ratio = 1;
		if (ratio <= 0)
			ratio = 1;
		double meanX = 0;
		if (bbox.getX2() != Double.MAX_VALUE)
			meanX = (bbox.getX1() + bbox.getX2()) / 2;
		double meanY = 0;
		if (bbox.getY2() != Double.MAX_VALUE)
			meanY = (bbox.getY1() + bbox.getY2()) / 2;
		graphicsPanel.setZoom(1d / ratio, new Point2D.Double(0, 0));
		graphicsPanel.setPan(graphicsPanel.getWidth() / 2 - meanX / ratio, - graphicsPanel.getHeight() / 2 + meanY / ratio);		
	}
	
	int testStepState = 0;
	/**
	 * Used for debugging.
	 * <br /> Execute some action when the user clicks in the status bar.
	 */
	public void testStep() {
		
	}
	
	/**
	 * Show a list of all instances of some Geo Object type; let the user
	 * select one item from the list and zoom to the selected object.
	 * @param what String; the kind of Geo Object to zoom to
	 */
	public void showZoomDialog(String what) {
		TreeMap<String, Object> mapping = new TreeMap<String, Object>();
		if ("link".equals(what))
			for (Link link : model.network.getLinks())
				mapping.put(link.getName_r(), link);
		else if ("node".equals(what))
			for (Node node : model.network.getNodeList(true))
				mapping.put (node.getName_r(), node);
		else if ("lane".equals(what))
			for (Lane lane : model.network.getLanes())
				mapping.put ("lane_" + lane.getID(), lane);
		else if ("vehicle".equals(what)) {
			GraphicsPanelClient gpc = graphicsPanel.getClient();
			if (gpc instanceof LaneSimulator) {
				for (Movable m : ((LaneSimulator) gpc).getModel().getVehicles())
					mapping.put("vehicle_" + m.id, m);
			} else
				throw new Error("Collecting vehicle list not supported in simulator " + gpc.toString());
		} else
			throw new Error("Don't know how to build a list of " + what);
		String selected = (String) JOptionPane.showInputDialog(new JFrame(), "", "Please select", JOptionPane.PLAIN_MESSAGE, null, mapping.keySet().toArray(), "");
		if (null == selected)
			return;
		int margin = 20;
		Line2D.Double bbox = null;
		if ("link".equals(what)) {
			Link link = (Link) mapping.get(selected);
			if (null == link)
				WED.showProblem(WED.PROGRAMERROR, "Cannot find link %s", selected);
			for (Vertex v : link.getVertices())
				bbox = Planar.expandBoundingBox(bbox, v.getX(), v.getY());
		} else if ("node".equals(what)) {
			Node node = (Node) mapping.get(selected);
			if ((null != node.getCircle()) && (node.getCircle().radius() < margin))
				margin = (int) node.getCircle().radius();
			bbox = Planar.expandBoundingBox(bbox, node.getX(), node.getY());
		} else if ("lane".equals(what)) {
			Lane lane = (Lane) mapping.get(selected);
			for (Vertex v : lane.getLaneVerticesInner())
				bbox = Planar.expandBoundingBox(bbox, v.getX(), v.getY());
			for (Vertex v : lane.getLaneVerticesOuter())
				bbox = Planar.expandBoundingBox(bbox, v.getX(), v.getY());
		} else if ("vehicle".equals(what)) {
			Movable m = (Movable) mapping.get(selected);
			bbox = Planar.expandBoundingBox(bbox, m.global.x, m.global.y);
		}
		if (null != bbox)
			setZoomRect(bbox, margin);			
	}
	
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		System.out.println("actionPerformed: " + actionEvent.getActionCommand());
		String command = actionEvent.getActionCommand();
		if ("redrawMap".equals(command))
			;
        else if ("openDialogSaveModel".equals(command))
        	openDialogSaveModel();
        else if ("openDialogExportModel".equals(command))
        	openDialogExportModel();//TODO GUUS export!!
        else if ("openDialogImportOSM".equals(command))
        	openDialogImportOSM();
        else if ("newModel".equals(command))
        	newModel();
        else if ("openDialogLoadModel".equals(command))
        	openDialogLoadModel();
		//shape
        else if ("openDialogImportModel".equals(command))
			openDialogImportModel();
        else if (command.startsWith("load "))
        	loadStorable(identifyStorable(command.substring(5)));
        else if (command.startsWith("save ")) {
        	Storable s;
        	if (command.endsWith("measurementPlan")) {
        		javax.swing.JMenuItem mpItem = (javax.swing.JMenuItem) actionEvent.getSource();
        		javax.swing.JPopupMenu popupParent = (JPopupMenu) mpItem.getParent();
        		int rank;
        		for (rank = popupParent.getComponentCount(); --rank >= 0; )
        			if (popupParent.getComponent(rank) == mpItem)
        				break;
        		if (rank < 0)
        			throw new Error("Cannot find menu item that sent " + command);
        		s = model.getMeasurementPlan(rank);
        	} else
        		s = identifyStorable(command.substring(5));
        	saveStorable(s);
        } else if (command.startsWith("new "))
        	newStorable(identifyStorable(command.substring(4)));
		else if ("Exit".equals(command))
        	closeProgramCheck();
		else if ("logVehicleLifes".equals(command))
			setVehicleLifeLog();
        else if ("propertiesTabChanged".equals(command))
        	setActiveGraph();
		else if ("LaneSimulatorRedraw".equals(command))
			;	// Redraw is done below; but the event has to be sent and received
		else if ("zoomToScene".equals(command))
			zoomToScene();
		else if ("zoomIn".equals(command))
			graphicsPanel.setZoom(graphicsPanel.getZoom() * 2, new Point2D.Double(graphicsPanel.getWidth() / 2, graphicsPanel.getHeight() / 2));
		else if ("zoomOut".equals(command))
			graphicsPanel.setZoom(graphicsPanel.getZoom() / 2, new Point2D.Double(graphicsPanel.getWidth() / 2, graphicsPanel.getHeight() / 2));
		else if ("zoomToLane".equals(command))
			showZoomDialog("lane");
		else if ("zoomToLink".equals(command))
			showZoomDialog("link");
		else if ("zoomToNode".equals(command))
			showZoomDialog("node");
		else if ("zoomToVehicle".equals(command))
			showZoomDialog("vehicle");
		else if ("MeasurementPlanChanged".equals(command))
			switchMeasurementPlan();
		else if ("EditMeasurementPlanName".equals(command))
			editMeasurementPlanName();
		else if ("UpdateMeasurementPlanName".equals(command))
			updateMeasurementPlanName();
		else if ("DeleteMeasurementPlan".equals(command))
			deleteMeasurementPlan();
		else if (command.startsWith("SelectTab"))
			try {
				tabbedPaneProperties.setSelectedIndex(Integer.parseInt(command.split("[ ]")[1]));
			} catch (NumberFormatException e) {
				WED.showProblem(WED.ENVIRONMENTERROR, "Error switching to tab \"%s\"", command.split("[ ]")[1]);
			}
		else if ("statusBarClicked".equals(command)) {
			testStep();
			return;
		}
		else
			System.out.println("Unhandled actionevent " + command);
		showDrivable.setEnabled(showLinks.isSelected());
		graphicsPanel.repaint();
	}

	private String vehicleLifeLogFileName = null;
	/**
	 * Retrieve the currently set name for logging vehicle creation and destruction.
	 * @return String; the current name for the logging file, or null if no logging is to be performed
	 */
	public String getVehicleLifeLogFileName () {
		return vehicleLifeLogFileName;
	}
	
	private void setVehicleLifeLog() {
		final String defaultExt = "txt";
		String defaultName = initialDirectory;
		if (null != vehicleLifeLogFileName)
			defaultName = vehicleLifeLogFileName;
		System.out.println("defaultName is " + defaultName);
		vehicleLifeLogFileName = FileDialog.showFileDialog(true, defaultExt, "Vehicle life log", defaultName);
		if ("".equals(getFileExtension(vehicleLifeLogFileName)))
			vehicleLifeLogFileName += "." + defaultExt;
	}

	private void deleteMeasurementPlan() {
		model.deleteMeasurementPlan((MeasurementPlan) comboBoxMeasurementPlans.getSelectedItem());
		measurementPlanListChanged();
	}

	private void editMeasurementPlanName() {
		measurementPlanListChanged();
		editMeasurementPlanName.setText(comboBoxMeasurementPlans.getSelectedItem().toString());
		editMeasurementPlanName.setSelectionStart(0);
		editMeasurementPlanName.setSelectionEnd(editMeasurementPlanName.getText().length());
		editMeasurementPlanName.setVisible(true);
		editMeasurementPlanName.grabFocus();
	}
	
	private void updateMeasurementPlanName() {
		String newName = editMeasurementPlanName.getText();
		editMeasurementPlanName.setVisible(false);
		((MeasurementPlan) comboBoxMeasurementPlans.getSelectedItem()).setName(newName);
		measurementPlanListChanged();
	}

	private void switchMeasurementPlan() {
		int index = comboBoxMeasurementPlans.getSelectedIndex();
		while (panelMeasurementPlan.getComponentCount() > 0)
			panelMeasurementPlan.remove(0);
		if (index >= 0)
			panelMeasurementPlan.add(model.getMeasurementPlan(index));
	}

}
