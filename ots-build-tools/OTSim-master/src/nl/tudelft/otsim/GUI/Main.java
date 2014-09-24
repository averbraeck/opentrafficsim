package nl.tudelft.otsim.GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.File;
import javax.swing.table.DefaultTableModel;
import java.util.Locale;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.tudelft.otsim.Charts.MeasurementPlan;
import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.GeoObjects.Network;
import nl.tudelft.otsim.GeoObjects.Node;
import nl.tudelft.otsim.ModelIO.ExportModel;
import nl.tudelft.otsim.ModelIO.ImportModelShapeWizard;
import nl.tudelft.otsim.ModelIO.LoadModel;
import nl.tudelft.otsim.ModelIO.SaveModel;
import nl.tudelft.otsim.Simulators.Simulator;
import nl.tudelft.otsim.Simulators.LaneSimulator.LaneSimulator;
import nl.tudelft.otsim.Simulators.RoadwaySimulator.RoadwaySimulator;
import nl.tudelft.otsim.TrafficDemand.TrafficDemand;

/**
 * This class implements the Main window of OpenTraffic.
 * 
 * @author gftamminga, Peter Knoppers
 *
 */
public class Main extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	/** Locale value used in the application */
	public static Locale locale = new Locale("en", "US");
    private volatile static boolean initialized = false;
    /** Main Frame of OpenTraffic */
    public static Main mainFrame;
    
    /**
     * Create main window and parse command line arguments.
     * 
     * @param args Array of String; command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
			public void run() {
                  mainFrame = new Main();
                  mainFrame.setMinimumSize(new Dimension(1000, 800));
                  mainFrame.setLocation(100, 100);
                  initialized = true;
                  mainFrame.setVisible(true);
            }
        });
        // wait for start up of GUI
        while (! initialized) {
    		System.out.println("Waiting for initialization of GUI");
    		try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				System.err.println("Sleep interrupted");
				e.printStackTrace();
			}
        }
        mainFrame.newModel();
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
        		else
        			WED.showProblem(WED.ENVIRONMENTERROR, "Unknown program argument \"%s\" (ignored)", arg);
        	} else
    			WED.showProblem(WED.ENVIRONMENTERROR, "Unknown program argument \"%s\" (ignored)", arg);        		
        }
        System.out.println("Argument processing finished");
        Log.logMessage(null, false, "Ready");	// test that LogMessage can log to the Event log
    }

    /** GraphicsPanel used in the main window */
	public GraphicsPanel graphicsPanel;
	private String workingDir = System.getProperty("user.dir");
	JPanel panelMeasurementPlan;
	/** Directory that was specified on the command line */
    public String initialDirectory = workingDir; 
    private String fileSelectedNetwork;
    /** Currently loaded traffic model */
    public Model model;
    /** JMenuItem of the Save Model ... menu */
    public javax.swing.JMenuItem menuItemSaveModel;
    /** JMenuItem of the Export Model ... menu */
    public javax.swing.JMenuItem menuItemExportModel;
    
    /** Name of the OpenTraffic application */
    public final String myName = "Open Traffic";
  //shape
    private JTable table;
  //shape
    
    /**
     * Create the main window.
     */
    public Main() {
    	Main.mainFrame = this;

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    	// Build the GUI
        textAreaLogging = new javax.swing.JTextArea();	// Make this one early
        getContentPane().setLayout(new java.awt.BorderLayout(10, 10));
        
        addWindowListener(new WindowAdapter() {
        	@Override
			public void windowClosing(WindowEvent e) {
        		closeProgramCheck();
        	}
        });

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
		getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		this.setTitle(myName);
        
        tabbedPaneProperties = new javax.swing.JTabbedPane();
        tabbedPaneProperties.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.JToolBar toolBar = new javax.swing.JToolBar();
        toolBar.setRollover(true);
        toolBar.setPreferredSize(new java.awt.Dimension(13, 22));
        getContentPane().add(toolBar, java.awt.BorderLayout.PAGE_START);

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

        getContentPane().add(scrollPaneProperties, java.awt.BorderLayout.LINE_START);

        //shape
        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(new DefaultTableModel(5, 5));
        table.setPreferredScrollableViewportSize(new Dimension(500, 200));
        //shape
        
        // Build the main menu
        // File menu
        javax.swing.JMenu menuFile = new javax.swing.JMenu();
        menuFile.setText("File");
        
        makeMenuItem ("New model", "newModel", menuFile, "New.png");
        makeMenuItem ("Open model ...", "openDialogLoadModel", menuFile, "Briefcase.png");
        makeMenuItem ("Import model ...", "openDialogImportModel", menuFile, "Back.png");
        
        menuItemExportModel = makeMenuItem("Export model ...", "openDialogExportModel", menuFile, "Forward.png");
        menuItemExportModel.setEnabled(false);  
        
        menuItemSaveModel = makeMenuItem("Save model ...", "openDialogSaveModel", menuFile, "Save.png");
        menuItemSaveModel.setEnabled(false);
        
        makeMenuItem("Exit", "Exit", menuFile, "Exit.png");
        
        menuBar = new javax.swing.JMenuBar();
        menuBar.add(menuFile);
        
        // Experimental File2 menu
        
        JMenu menuFile2 = new JMenu();
        menuFile2.setText("File2");
        
        makeMenu("New", "new", menuFile2, "New.png");
        makeMenu("Open", "load", menuFile2, "Briefcase.png");
        makeMenu("Save", "save", menuFile2, "Save.png");
        makeMenuItem("Import model ...", "openDialogImportModel", menuFile2, "Back.png");
        makeMenuItem("Exit", "Exit", menuFile2, "Exit.png");
        
        menuBar.add(menuFile2);
        
        // View menu
        javax.swing.JMenu menuView = new javax.swing.JMenu();
        menuView.setText("View");
        menuBar.add(menuView);
        
        // View zoom to scene (bounding box)
        makeMenuItem("Entire network", "zoomToScene", menuView, "Expand.png");
        makeMenuItem("Zoom in", "zoomIn", menuView, "Zoom.png");
        makeMenuItem("Zoom out", "zoomOut", menuView, "Earth.png");

        // Charts menu
        javax.swing.JMenu menuCharts = new javax.swing.JMenu();
        menuCharts.setText("Charts");

        javax.swing.JMenuItem menuItemChartsPie = new javax.swing.JMenuItem();
        menuItemChartsPie.setText("PieChart");
        menuItemChartsPie.setActionCommand("chartPie");
        menuItemChartsPie.addActionListener(this);
        menuCharts.add(menuItemChartsPie);

        menuBar.add(menuCharts);
        // Show the menu (in the jFrame)
        setJMenuBar(menuBar);

    	graphicsPanel = new GraphicsPanel();
   
    	getContentPane().add(graphicsPanel, java.awt.BorderLayout.CENTER);
    	
        JPanel controls = new JPanel();
        controls.setLayout(new GridLayout(20,2));
        controls.add(showNodes = makeCheckBox("Show Nodes", "redrawMap", true));
        controls.add(showLinks = makeCheckBox("Show Links", "redrawMap", true));
        controls.add(showDrivable = makeCheckBox("Only drivable links", "redrawMap", false));
        controls.add(showPaths = makeCheckBox("Show Paths", "redrawMap", false));
        controls.add(showBuildings = makeCheckBox("Show Buildings", "redrawMap", true));
        controls.add(showPolyZones = makeCheckBox("Show PolyZones", "redrawMap", true));
        controls.add(showLaneIDs = makeCheckBox("Show Lane IDs", "redrawMap", false));
        
        JScrollPane scrollPaneNetworkEditor = new JScrollPane();
        scrollPaneNetworkEditor.setViewportView(controls);
        tabbedPaneProperties.add("Network editor", scrollPaneNetworkEditor);
        
        Main.mainFrame.tabbedPaneProperties.setSelectedIndex(Main.mainFrame.tabbedPaneProperties.indexOfComponent(scrollPaneNetworkEditor));
        
        controls = new JPanel();
        controls.setLayout(new BorderLayout());
        controls.add(comboBoxMeasurementPlans = new JComboBox<MeasurementPlan>(), BorderLayout.NORTH);
        controls.add(panelMeasurementPlan = new JPanel());
        comboBoxMeasurementPlans.setActionCommand("MeasurementPlanChanged");
        comboBoxMeasurementPlans.addActionListener(this);
        JScrollPane scrollPaneMeasurementPlans = new JScrollPane();
        scrollPaneMeasurementPlans.setViewportView(controls);
        tabbedPaneProperties.add("Measurement plans", scrollPaneMeasurementPlans);
        measurementPlanIndex = tabbedPaneProperties.indexOfComponent(scrollPaneMeasurementPlans);
        
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
        controls.add(new JPanel());
        // add a filler
        gbc.gridy++;
        gbc.weighty = 0.5;
        controls.add(new JPanel(), gbc);
        
        JScrollPane scrollPaneLaneSimulator = new JScrollPane();
        scrollPaneLaneSimulator.setViewportView(controls);
        tabbedPaneProperties.add("Lane Simulator", scrollPaneLaneSimulator);
        laneSimulatorIndex = tabbedPaneProperties.indexOfComponent(scrollPaneLaneSimulator);
        
        buttonUndo = makeButton("Undo", "Back to previous something-or-other", "Undo", "undo16");
        toolBar.add(buttonUndo);
        buttonRedo = makeButton("Redo", "Forward to something-or-other", "Redo", "redo16");
        toolBar.add(buttonRedo);
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
     * @param parent JMenu; parent of the JMenuItem
     * @return JMenuItem; the newly created JMenuItem
     */
    javax.swing.JMenuItem makeMenuItem(String caption, String actionCommand, javax.swing.JMenu parent, String iconName) {
        javax.swing.JMenuItem menuItem = new javax.swing.JMenuItem();
        menuItem.setText(caption);
        if (null != actionCommand) {
        	menuItem.setActionCommand(actionCommand);
            menuItem.addActionListener(this);
        }
        // Try to load the image from the resources
        String imgLocation = "/resources/" + iconName;
        java.net.URL imageURL = Main.mainFrame.getClass().getResource(imgLocation);
        if (imageURL != null)
            menuItem.setIcon(new ImageIcon(imageURL, caption));
        parent.add(menuItem);
        return menuItem;
    }
    
    javax.swing.JMenu makeMenu(String caption, String actionCommandPrefix, javax.swing.JMenu parent, String iconName) {
    	javax.swing.JMenu menu = new javax.swing.JMenu();
    	menu.setText(caption);
    	parent.add(menu);
        // Try to load the image from the resources
        String imgLocation = "/nl/tudelft/otsim/Resources/" + iconName;
        java.net.URL imageURL = Main.mainFrame.getClass().getResource(imgLocation);
        if (imageURL != null)
            menu.setIcon(new ImageIcon(imageURL, caption));
    	makeMenuItem("network ...", actionCommandPrefix + " network", menu, null);
    	makeMenuItem("demand ...", actionCommandPrefix + " demand", menu, null);
    	if (actionCommandPrefix.equals("save")) {
    		saveMeasurementPlan = new javax.swing.JMenu("measurement plan");
    		saveMeasurementPlan.setEnabled(false);
    		menu.add(saveMeasurementPlan);
    	} else
    		makeMenuItem("measurement plan ...", actionCommandPrefix + " measurementPlan", menu, null);
    	makeMenuItem("model ...", actionCommandPrefix + " model", menu, null);
    	//makeMenuItem("settings ...", actionCommandPrefix + " settings", menu);
    	return menu;
    }

    /**
     * Create a JButton and initialize some of its properties
     * @param caption String; caption of the JButton
     * @param toolTipText String toolTipText of the JButton. If null, no
     * toolTipText is set
     * @param actionCommand String; actionCommand of the JButton. If non-null
     * <code>this</code> is added to the ActionListeners of the JButton
     * @return JButton; the newly created JButton
     */
    private JButton makeButton (String caption, String toolTipText, String actionCommand) {
    	JButton button = new JButton(caption);
    	if (null != toolTipText)
    		button.setToolTipText(toolTipText);
    	if (null != actionCommand) {
    		button.setActionCommand(actionCommand);
    		button.addActionListener(this);
    	}
    	return button;
    }
  
    /**
     * Create a JButton and initialize some of its properties
     * @param caption String; caption of the JButton
     * @param toolTipText String toolTipText of the JButton. If null, no
     * toolTipText is set
     * @param actionCommand String; actionCommand of the JButton. If non-null
     * <code>this</code> is added to the ActionListeners of the JButton
     * @param iconName String; name of the icon for the JButton. This icon
     * must be loaded in the resources directory of the jar file 
     * @return JButton; the newly created JButton
     */
    private JButton makeButton(String caption, String toolTipText, String actionCommand, String iconName) {
        JButton button = makeButton("", toolTipText, actionCommand);
        // Try to load the image from the resources
        String imgLocation = "/nl/tudelft/otsim/Resources/" + iconName + ".gif";
        java.net.URL imageURL = Main.mainFrame.getClass().getResource(imgLocation);
        if (imageURL != null) {                     // image found
            button.setIcon(new ImageIcon(imageURL, caption));
            button.setText("");						// clear caption
        } else                                    // no image found
            System.err.println("Resource not found: " + imgLocation);
        return button;
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
		menuItemSaveModel.setEnabled(true);
		menuItemExportModel.setEnabled(true);
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
  //shape
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
     * Lane IDs should be drawn if and only if the showLaneIDs check bos is
     * checked.
     */
    public JCheckBox showLaneIDs;
    
    private JButton buttonUndo;
    private JButton buttonRedo;
    private JPanel laneSimulatorControlPanel;
    private JPanel roadwaySimulatorControlPanel;
    /** Simulators must show the leader of a vehicle. */
    public JCheckBox checkBoxShowLeader;
    /** Simulators must show the follower of a vehicle. */
    public JCheckBox checkBoxShowFollower;
	private StatusBar statusBar;
	private JProgressBar mainStatusLabel;
	private final int roadWaySimulatorIndex;
	private final int laneSimulatorIndex;
	private javax.swing.JMenu saveMeasurementPlan; 
	private JComboBox<MeasurementPlan> comboBoxMeasurementPlans;
	
	/**
	 * Update the save measurement plan menu item so it expands to show the
	 * names of all measurement plans in the Model.
	 */
	public void measurementPlanListChanged() {
		while (saveMeasurementPlan.getItemCount() > 0)
			saveMeasurementPlan.remove(0);
		for (int i = 0; i < model.measurementPlanCount(); i++)
			saveMeasurementPlan.add(makeMenuItem (model.getMeasurementPlan(i).getName(), "save measurementPlan", saveMeasurementPlan, null));
		saveMeasurementPlan.setEnabled(saveMeasurementPlan.getItemCount() > 0);
		MeasurementPlan currentMeasurementPlan = (MeasurementPlan) comboBoxMeasurementPlans.getSelectedItem();
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
		if (type.equals(LaneSimulator.simulatorType))
			return model.exportToMicroSimulation();
		if (type.equals(RoadwaySimulator.simulatorType))
			return model.exportToSubMicroSimulation();
		throw new Error("Do not know how to create configuration of type " + type);
	}
	
	/**
	 * Create a new Simulator on request of the Scheduler.
	 * @param type String; type of Simulator to create
	 * @param configuration String; configuration text for the Simulator
	 * @param scheduler
	 * @return Simulator; the newly created Simulator
	 * @throws Exception
	 */
	public static Simulator createSimulator(String type, String configuration, Scheduler scheduler) throws Exception {
		if (type.equals(LaneSimulator.simulatorType)) {
			return new LaneSimulator(configuration, scheduler.getGraphicsPanel(), scheduler);
		}
		if (type.equals(RoadwaySimulator.simulatorType)) {
			return new RoadwaySimulator(configuration, scheduler.getGraphicsPanel(), scheduler);
		}
		throw new Error("Do not know how to create a simulator of type " + type);
	}

	/**
	 * Schedule a re-paint for the map display 
	 */
	public void setActiveGraph() {
		int index = tabbedPaneProperties.getSelectedIndex();
		System.out.println("SetActiveGraph: index is " + index);
		if (index < 0)
			return;
    	if (roadWaySimulatorIndex == index) {
    		if (0 == roadwaySimulatorControlPanel.getComponentCount())
    			roadwaySimulatorControlPanel.add(new Scheduler(RoadwaySimulator.simulatorType, graphicsPanel));
    		if (0 == roadwaySimulatorControlPanel.getComponentCount()) {
    			WED.showProblem(WED.INFORMATION, "Could not load roadway simulator");
    			return;
    		}
    		graphicsPanel.setClient(((Scheduler)(roadwaySimulatorControlPanel.getComponent(0))).getSimulator());
    	} else if (laneSimulatorIndex == index) {
    		if (0 == laneSimulatorControlPanel.getComponentCount())
    			laneSimulatorControlPanel.add(new Scheduler(LaneSimulator.simulatorType, graphicsPanel));
    		if (0 == laneSimulatorControlPanel.getComponentCount()) {
    			WED.showProblem(WED.INFORMATION, "Could not load lane simulator");
    			return;
    		}
    		graphicsPanel.setClient(((Scheduler)(laneSimulatorControlPanel.getComponent(0))).getSimulator());
    	} else if ((measurementPlanIndex == index) && (comboBoxMeasurementPlans.getSelectedIndex() >= 0))
    		graphicsPanel.setClient((GraphicsPanelClient) comboBoxMeasurementPlans.getSelectedItem());
    	else if (null != model)
    	  	graphicsPanel.setClient(model.network);
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
		return mayDiscardChanges(model.network) && mayDiscardChanges(model.trafficDemand);  
	}
	
	private Storable identifyStorable(String name) {
		// TODO add support for Storable settings
		if (name.equals("network")) {
			if (null == model)
				model = new Model();
			return model.network;
		} else if (name.equals("demand")) {
			if (null == model)
				model = new Model();
			return model.trafficDemand;
		} else if (name.equals("measurementPlan")) {
			return new MeasurementPlan(model);
		} else if (name.equals("model")) {
			return model;
		} else if (name.equals("settings")) {
			throw new Error("settings is not yet storable");
		}
		throw new Error("Unknown storable type " + name);
	}
	
	private Storable identifyStorableExtension(String extension) {
		if (extension.equals(Network.FILETYPE))
			return model.network;
		else if (extension.equals(TrafficDemand.FILETYPE))
			return model.trafficDemand;
		else if (extension.equals(Model.FILETYPE))
			return model;
		return null;
	}
	
	private void newStorable(Storable storable) {
		if (! mayDiscardChanges(storable))
			return;		// cancel New
		if (storable instanceof Network)
			model.network = new Network();
		else if (storable instanceof TrafficDemand)
			model.trafficDemand = new TrafficDemand(model);
		else if (storable instanceof MeasurementPlan)
			model.addMeasurementPlan(new MeasurementPlan(model));
		else if (storable instanceof Model)
			model = new Model();
		else
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
					storable = model.network = new Network(subNode);
				else if (storable instanceof TrafficDemand)
					storable = model.trafficDemand = new TrafficDemand(model, subNode);
				else if (storable instanceof MeasurementPlan)
					model.addMeasurementPlan((MeasurementPlan)(storable = new MeasurementPlan(model, subNode)));
				else if (storable instanceof Model)
					storable = model = new Model(pn);
				else
					throw new Error("Cannot happen");
			setActiveGraph();
			if ((storable instanceof Network) || (storable instanceof Model))
				zoomToScene();
			storable.setStorageName(fileName);
			initialDirectory = new File(fileName).getParent();
		} catch (Exception e) {
			if (e instanceof java.io.FileNotFoundException)
				WED.showProblem(WED.ENVIRONMENTERROR, "File \"%s\" does not exist", fileName);
			else
				WED.showProblem(WED.ENVIRONMENTERROR, "Could not load %s from file %s:\r\n%s", storable.description(), fileName, WED.exeptionStackTraceToString(e));
		}		
	}
	
	private void loadFile(String fileName) {
		int pos = fileName.lastIndexOf(".");
		if (pos < 0) {
			WED.showProblem(WED.ENVIRONMENTERROR, "Cannot identify type of file \"%s\"", fileName);
			return;
		}
		String extension = fileName.substring(pos + 1);
		Storable storable = identifyStorableExtension(extension);
		if (null == storable)
			WED.showProblem(WED.ENVIRONMENTERROR, "Cannot identify file type of \"%s\"", fileName);
		else
			loadStorable(storable, fileName);
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
		model.network = new Network();
        fileSelectedNetwork = "";
        mainFrame.setTitle(myName + " - " + fileSelectedNetwork);
    	setActiveGraph();
        System.out.println("created new empty network");
		menuItemSaveModel.setEnabled(true);
		menuItemExportModel.setEnabled(true);
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
		double minX = Double.MAX_VALUE;
		double maxX = - Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = - Double.MAX_VALUE;
		for (Node node : model.network.getAllNodeList(true)) {
			if (minX > node.getX())
				minX = node.getX();
			if (maxX < node.getX())
				maxX = node.getX();
			if (minY > node.getY())
				minY = node.getY();
			if (maxY < node.getY())
				maxY = node.getY();
		}
		double xRatio = (maxX - minX) / (graphicsPanel.getWidth() - 2 * margin);
		double yRatio = (maxY - minY) / (graphicsPanel.getHeight() - 2 * margin);
		double ratio = xRatio > yRatio ? xRatio : yRatio;
		//System.out.format("x: [%.2f - %.2f], y: [%.2f - %.2f], width %d, height %d ratio %.4f\r\n", minX, maxX, minY, maxY, graphicsPanel.getWidth(), graphicsPanel.getHeight(), ratio);
		graphicsPanel.setZoom(1d / ratio, new Point2D.Double(0, 0));
		graphicsPanel.setPan(graphicsPanel.getWidth() / 2 - (minX + maxX) / 2 / ratio, - graphicsPanel.getHeight() / 2 + (minY + maxY) / 2 / ratio);
	}
	
	int testStepState = 0;
	/**
	 * Used for debugging.
	 * <br /> Execute some action when the user clicks in the status bar.
	 */
	public void testStep() {
	}
	
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		System.out.println("actionPerformed: " + actionEvent.getActionCommand());
		String command = actionEvent.getActionCommand();
		if ("redrawMap".equals(command)) {
			//TODO make this correct
//            if (showPaths.isSelected())
//                CreatePaths.CreatePathsAll(Main.mainFrame.model.network);
		} else if ("Undo".equals(command))
			System.out.println("undo not handled yet");
        else if ("Redo".equals(command))
			System.out.println("redo not handled yet"); 
		/*
        else if ("chartPie".equals(command))  {
        	//System.out.println("ChartPie to be implemented");\
        	
        	DynamicTrajectoryBuilder dtb = new DynamicTrajectoryBuilder("Time Series");
	        dtb.pack();
	        dtb.setVisible(true);
	        TrajectoryBuilder tb = new TrajectoryBuilder("Comparison", "Which operating system are you using?");
	        tb.pack();
	        tb.setVisible(true);
	        PieChart demo = new PieChart("This is NO science", "Would you like to use Open Traffic?");
	        demo.pack();
	        demo.setVisible(true);

        }
        */
        else if ("openDialogSaveModel".equals(command))
        	openDialogSaveModel();
        else if ("openDialogExportModel".equals(command))
        	openDialogExportModel();//TODO GUUS export!!
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
        		javax.swing.JPopupMenu parent = (JPopupMenu) mpItem.getParent();
        		int rank;
        		for (rank = parent.getComponentCount(); --rank >= 0; )
        			if (parent.getComponent(rank) == mpItem)
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
		else if ("MeasurementPlanChanged".equals(command))
			switchMeasurementPlan();
		else if ("statusBarClicked".equals(command)) {
			testStep();
			return;
		}
		else
			System.out.println("Unhandled actionevent " + command);
		showDrivable.setEnabled(showLinks.isSelected());
		graphicsPanel.repaint();
	}

	private void switchMeasurementPlan() {
		int index = comboBoxMeasurementPlans.getSelectedIndex();
		while (panelMeasurementPlan.getComponentCount() > 0)
			panelMeasurementPlan.remove(0);
		if (index >= 0)
			panelMeasurementPlan.add(model.getMeasurementPlan(index));
	}

}