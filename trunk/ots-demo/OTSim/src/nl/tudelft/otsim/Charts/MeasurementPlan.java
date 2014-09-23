package nl.tudelft.otsim.Charts;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.GraphicsPanelClient;
import nl.tudelft.otsim.GUI.Model;
import nl.tudelft.otsim.GUI.Storable;
import nl.tudelft.otsim.GUI.WED;
import nl.tudelft.otsim.GeoObjects.CrossSection;
import nl.tudelft.otsim.GeoObjects.CrossSectionElement;
import nl.tudelft.otsim.GeoObjects.Link;
import nl.tudelft.otsim.GeoObjects.Network;
import nl.tudelft.otsim.GeoObjects.Node;
import nl.tudelft.otsim.GeoObjects.Vertex;
import nl.tudelft.otsim.SpatialTools.Planar;

/**
 * This class holds the description for a measurement plan.
 * 
 * @author Peter Knoppers
 */
public class MeasurementPlan extends JPanel implements GraphicsPanelClient, Storable, ActionListener {
	private static final long serialVersionUID = 1L;
	/** Name for a Network element when stored in XML format */
	public static final String XMLTAG = "MeasurementPlan";
	/** File extension for Network files */
	public static final String FILETYPE = "otsp";
	private static final String XML_NAME = "Name";
	private static final String XML_NODE = "Node";
	private static final String XML_NODERANK = "Rank";
	private static final String XML_NODE_ID = "ID";
	private String fileName = null;
	private ArrayList<Integer> route = new ArrayList<Integer>();
	private String name;
	private JTable routeTable;
	private JScrollPane tablePane;
	private final Model model;
	private boolean modified = false;
	private JPopupMenu tablePopupMenu;
	private JMenuItem deleteRow;
	private JMenuItem moveDown;
	private JMenuItem moveUp;
	private int row = -1;

	/**
	 * Create a MeasurementPlan from a {@link ParsedNode}.
	 * @param model {@link Model}; the Model that this MeasurementPlan will belong to
	 * @param pn {@link ParsedNode}; XML node of the MeasurementPlan
	 * @throws Exception 
	 */
	public MeasurementPlan(Model model, ParsedNode pn) throws Exception {
		this(model);
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer> ();
		if (pn.size(XML_NAME) != 1)
			throw new Exception("Measurement plan must have a name");
		name = pn.getSubNode(XML_NAME, 0).getValue();
		for (int index = 0; index < pn.size(XML_NODE); index++) {
			ParsedNode subNode = pn.getSubNode(XML_NODE, index);
			Integer rank = null;
			Integer nodeID = null;
			for (String key : subNode.getKeys()) {
				if (subNode.size(key) != 1)
					throw new Exception("There should be only one node with tag " + key + " at " + subNode.lineNumber + ", " + subNode.columnNumber);
				if (key.equals(XML_NODERANK))
					rank = Integer.parseInt(subNode.getSubNode(key, 0).getValue());
				else if (key.equals(XML_NODE_ID))
					nodeID = Integer.parseInt(subNode.getSubNode(key, 0).getValue());
				else
					throw new Exception("Unknown key: " + key);
			}
			if ((null == rank) || (null == nodeID))
				throw new Exception("Incompletely defined node at " + subNode.lineNumber + ", " + subNode.columnNumber);
			map.put(rank, nodeID);
		}
		for (int index = 0; index < map.size(); index++) {
			Integer nodeID = map.get(index);
			if (null == nodeID)
				throw new Exception("Ranks of nodes are not numbered consecutively from 0");
			route.add(nodeID);
		}
		routeChanged();
		if (route.size() < 2)
			WED.showProblem(WED.INFORMATION, "Route of measurement plan \"%s\" has too few nodes (%d)", name, route.size());
	}
	
	/**
	 * Create an empty MeasurementPlan.
	 * @param model {@link Model}; the model that the new MeasurementPlan will belong to
	 */
	public MeasurementPlan(Model model) {
		this.model = model;
		this.setLayout(new BorderLayout());
		DefaultTableModel tableModel = new DefaultTableModel();
		tableModel.addColumn("ID");
		tableModel.addColumn("Name");
		routeTable = new JTable(tableModel);
		routeTable.getColumnModel().getColumn(0).setMaxWidth(30);
		tablePane = new JScrollPane(routeTable);
		tablePane.setPreferredSize(new Dimension(200, 400));
		tablePopupMenu = new JPopupMenu();
		tablePopupMenu.add(moveUp = makeMenuItem("Move up", "moveUp"));
		tablePopupMenu.add(moveDown = makeMenuItem("Move down", "moveDown"));
		tablePopupMenu.add(deleteRow = makeMenuItem("Delete node from route", "delete"));
		routeTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}
		});
		add(tablePane, BorderLayout.CENTER);
		name = "Unnamed measurement plan";
		routeChanged();
	}
	
	/**
	 * Create a dummy MeasurementPlan.
	 */
	public MeasurementPlan() {
		model = null;
	}
	
	private JMenuItem makeMenuItem(String caption, String actionCommand) {
		JMenuItem result = new JMenuItem(caption);
		result.addActionListener(this);
		result.setActionCommand(actionCommand);
		return result;
	}
	
	private void maybeShowPopup(MouseEvent e) {
		if (! e.isPopupTrigger())
			return;
		Point p = new Point(e.getX(), e.getY());
		int col = routeTable.columnAtPoint(p);
		row = routeTable.rowAtPoint(p);
		if ((col >= 0) && (row >= 0)) {
			DefaultTableModel tableModel = (DefaultTableModel) routeTable.getModel();
			deleteRow.setEnabled(tableModel.getRowCount() > 2);
			moveUp.setEnabled(row > 0);
			moveDown.setEnabled(row < tableModel.getRowCount() - 1);
			tablePopupMenu.show(routeTable, p.x, p.y);
		}
	}
	
	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& staXWriter.writeNode(XML_NAME, name)
				&& writeNodeList(staXWriter)
				&& staXWriter.writeNodeEnd(XMLTAG);
	}

	private boolean writeNodeList(StaXWriter staXWriter) {
		int rank = 0;
		for(int node : route)
			if (! (staXWriter.writeNodeStart(XML_NODE)
					&& staXWriter.writeNode(XML_NODERANK, String.format("%d", rank++))
					&& staXWriter.writeNode(XML_NODE_ID, String.format("%d", node))
					&& staXWriter.writeNodeEnd(XML_NODE)))
				return false;
		return true;
	}
	
	/**
	 * Generate a list of {@link Link Links} that describe the path of this MeasurementPlan.
	 * @param network {@link Network}; the Network that contains the route of this MeasurementPlan
	 * @return ArrayList&lt;{@link Link}&gt;; the list of Links that describe the path
	 * @throws Exception
	 */
	public ArrayList<Link> route (Network network) throws Exception {
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (int nodeID : route) {
			Node node = network.lookupNode(nodeID, true);
			if (null != node)
				nodes.add(node);
			else
				System.out.println("MeasurementPlan: skipping non-existent node " + nodeID);
		}
		ArrayList<Link> result = new ArrayList<Link> ();
		Node prevNode = null;
		for (Node node : nodes) {
			if (null != prevNode) {
				int found = 0;
				for (Link link : prevNode.getLinks_r())
					if (link.getToNode_r().equals(node)) {
						result.add(link);
						found++;
					}
				if (found != 1) 
					throw new Exception("No obvious (single and direct) route from " + prevNode + " to " + node);
				// We could "fix" this by employing a ShortestPathAlgorithm
			}
			prevNode = node;
		}
		return result;	
	}
	
	private void routeChanged() {
		DefaultTableModel tableModel = (DefaultTableModel) routeTable.getModel();
		while (tableModel.getRowCount() > 0)
			tableModel.removeRow(0);
		for (int index = 0; index < route.size(); index++) {
			int id = route.get(index);
			Node node = model.network.lookupNode(id, true);
			tableModel.addRow(new Object[] {String.format("%d", id), node.getName_r()});
		}
	}

	@Override
	public String description() {
		return "Measurement plan";
	}

	@Override
	public String fileType() {
		return FILETYPE;
	}

	@Override
	public boolean isModified() {
		return modified;
	}

	@Override
	public void clearModified() {
		modified = false;
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
	 * Retrieve the name of this MeasurementPlan.
	 * @return String; the name of this MeasurementPlan
	 */
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String newName) {
		name = newName;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		System.out.println("actionPerformed: " + actionEvent.getActionCommand());
		String command = actionEvent.getActionCommand();
		if ("moveUp".equals(command)) {
			int nodeNumber = route.get(row);
			route.remove(row);
			route.add(row - 1, nodeNumber);
		} else if ("moveDown".equals(command)) {
			int nodeNumber = route.get(row);
			route.remove(row);
			route.add(row + 1, nodeNumber);
		} else if ("delete".equals(command)) {
			route.remove(row);
		} else
			System.err.println("Unhandled event: " + command);
		routeChanged();
	} 

	@Override
	public void setModified() {
		modified = true;
	}

	@Override
	public void repaintGraph(GraphicsPanel graphicsPanel) {
		model.network.repaintGraph(graphicsPanel);
	}

	@Override
	public void mousePressed(GraphicsPanel graphicsPanel, MouseEvent evt) {
        Point2D.Double mouseDown = new Point2D.Double(evt.getX(), evt.getY());
        model.network.selectedNode = null;
		model.network.SelectNode(graphicsPanel, mouseDown);
		if (null != model.network.selectedNode) {
			int position = routeTable.getSelectedRow();
			if (position < 0)
				position = route.size();
			else
				position++;
			route.add(position, model.network.selectedNode.getNodeID());
			modified = true;
			routeChanged();
			routeTable.setRowSelectionInterval(position, position);
		}
	}

	@Override
	public void mouseDragged(GraphicsPanel graphicsPanel, MouseEvent evt) {
	}

	@Override
	public void mouseReleased(GraphicsPanel graphicsPanel, MouseEvent evt) {
	}

	@Override
	public void mouseMoved(GraphicsPanel graphicsPanel, MouseEvent evt) {
	}

	private void appendPart(ArrayList<Vertex> existingPart, ArrayList<Vertex> newPart) {
		int first = 0;
		if ((existingPart.size() > 0) && (existingPart.get(existingPart.size() - 1).distance(newPart.get(0)) < veryClose))
			first = 1;
		while (first < newPart.size())
			existingPart.add(newPart.get(first++));
	}
	
	final double veryClose = 0.1;	// m
	private ArrayList<Vertex> exportBoundary(Node from, Node to, int lateralReference) throws Exception {
		// Find the link connecting "from" to "to".
		ArrayList<Vertex> boundary = new ArrayList<Vertex>();
		boolean foundLink = false;
		for (Link l : from.getLinks_r()) {
			if (l.getToNode_r() == to) {
				for (CrossSection cs : l.getCrossSections_r()) {
					ArrayList<Vertex> part = null;
					for (CrossSectionElement cse : cs.getCrossSectionElementList_r())
						if (cse.getCrossSectionElementTypology().getDrivable())
							if ((null == part) || (CrossSectionElement.LateralReferenceRight == lateralReference))
								part = cse.getLinkPointList(lateralReference, true, true);
					if (null == part)
						throw new Exception("measurementPlan " + name + " cannot be applied to this network (no drivable CrossSectionElement)");
					appendPart(boundary, part);
				}
				foundLink = true;
				break;
			}
		}
		if (! foundLink)
			throw new Exception("measurementPlane " + name + " cannot be applied to this network (no link found)");
		return boundary;
	}
	
	private ArrayList<Vertex> exportBoundary (int lateralReference) throws Exception {
		ArrayList<Vertex> result = new ArrayList<Vertex> ();
		Node prevNode = null;
		for (int nodeID : route) {
			Node node = model.network.lookupNode(nodeID, true);
			if (null == node)
				throw new Exception("measurementPlan " + name + " cannot be applied to this network (node " + nodeID + " not found)");
			if (null != prevNode) {
				appendPart(result, exportBoundary(prevNode, node, lateralReference));
			}
			prevNode = node;
		}
		return result;
	}

	/**
	 * Create a textual description of this MeasurementPlan for export to a simulator
	 * @return String; the textual description of this MeasurementPlan
	 * @throws Exception 
	 */
	public String export() throws Exception {
		ArrayList<Vertex> area = exportBoundary(CrossSectionElement.LateralReferenceLeft);
		// Append the right boundary in reverse order
		int index = area.size();
		for (Vertex v : exportBoundary(CrossSectionElement.LateralReferenceRight))
			area.add(index, v);
		ArrayList<Vertex> projectOnto = exportBoundary(CrossSectionElement.LateralReferenceCenter);
		return "MeasurementPlan\t" + name + "\t" + Planar.verticesToString(area, true) + "\t" + Planar.verticesToString(projectOnto, true) + "\n";
	}

}