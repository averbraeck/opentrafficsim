package nl.tudelft.otsim.FileIO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GUI.Model;
import nl.tudelft.otsim.GeoObjects.CrossSection;
import nl.tudelft.otsim.GeoObjects.CrossSectionElement;
import nl.tudelft.otsim.GeoObjects.Lane;
import nl.tudelft.otsim.GeoObjects.Link;
import nl.tudelft.otsim.GeoObjects.Node;

/**
 * Write a model in Paramics format.
 * 
 * @author G.F. Tamminga, Peter Knoppers
 */
public class DataWriter {

    private static final String dataFileCategories = "Categories";
    private static final String dataFileLinks = "Links";
    private static final String dataFileNodes = "Nodes";

    /**
     * Write a {@link Model} in Paramics format.
     * @param pathName String; directory where the files will be stored
     * @param model {@link Model}; the model that must be written
     * @throws IOException
     */
    public static void WriteData(String pathName, Model model) throws IOException {
    	FileWriter outputStream = null;    
    	final int borderSpeedHighway = 90;
    	final int borderSpeedMajor = 40;
    	final double speedCat[] = {30, 40, 50, 60, 70, 80, 90, 100, 120};
    	final int lanesCat[] = {1, 2, 3, 4, 5};
    	final String roadType1[] = {"urban", "highway"};
    	final String roadType2[] = {"minor", "major"};
    	final double width = 3.5;

        try {
        	int catNumber = 0;
        	outputStream = new FileWriter(pathName + File.separator + dataFileCategories);	 
	    	for (int i = 0; i < speedCat.length; i++)
	    		for (int j = 0; j < lanesCat.length; j++)
	    			outputStream.write(String.format("category %d lanes: %d speed: %s width: %s m type: %s %s cost factor 1.0 color: 0xb9ffff\n", 
	    					++catNumber, 
	    					lanesCat[j],
	    					Double.toString(speedCat[i]),
	    					Double.toString(lanesCat[j] * width),
	    					roadType1[speedCat[i] > borderSpeedMajor ? 1 : 0], 
	    					roadType2[speedCat[i] > borderSpeedHighway ? 1 : 0]));
        } finally {
        	outputStream.close();
        }	  

    	// Rewrite links
        try {
        	outputStream = new FileWriter(pathName + File.separator + dataFileLinks);	 
	    	Collection<Link> links = Main.mainFrame.model.network.getLinkList();
	    	for (Link link : links) {
	    		boolean oneWay = true;
	    		for (Link link2 : links)
	    	    	if (link.getFromNode_r().equals(link2.getToNode_r()) && link.getToNode_r().equals(link2.getFromNode_r()))
	    	    		oneWay = false;
	    		for (CrossSection cs : link.getCrossSections_r()) {
	    			for (CrossSectionElement cse : cs.getCrossSectionElementList_r()) {
	    				if (cse.getCrossSectionElementTypology().getName_r().contentEquals("road")) {
				    		double speed = link.getMaxSpeed_r();
				    		int laneCount = cse.getCrossSectionObjects(Lane.class).size();
				    		int catNumber = 0;
				    		int catNumberLink = 0;
					    	for (int i = 0; i < speedCat.length; i++)	
					    		for (int j = 0; j < lanesCat.length; j++) {
					    			catNumber++;
						    		if ((speed >= speedCat[i] - 5) && (speed < speedCat[i] + 5))
						    			if (laneCount == lanesCat[j])
						    				catNumberLink = catNumber;
					    		}
					    	outputStream.write(String.format("link %d %d category %d lanes %d speed %s\n",
					    			link.getFromNode_r(),
					    			link.getToNode_r(),
					    			catNumberLink,
					    			laneCount,
					    			Double.toString(speed)));
	    				}
	    			}
	    		}		    		
	    		if (oneWay)
	    			outputStream.write(String.format("link %d %d barred\n",
	    					link.getToNode_r(),
	    					link.getFromNode_r()));
    		}		    		
        } finally {
        	outputStream.close();
        }
    	// Rewrite nodes
        try {
        	outputStream = new FileWriter(pathName + File.separator + dataFileNodes);	 
	    	Collection<Node> nodes = Main.mainFrame.model.network.getNodeList(false);
    		double maxX  = Double.NEGATIVE_INFINITY;
    		double minX  = Double.POSITIVE_INFINITY;
    		double maxY  = Double.NEGATIVE_INFINITY;
    		double minY  = Double.POSITIVE_INFINITY;
	    	for (Node node : nodes) {
	    		if (node.getX() < minX)
	    			minX = node.getX();
	    		if (node.getX() > maxX)
	    			maxX = node.getX();
	    		if (node.getY() < minY)
	    			minY = node.getX();
	    		if (node.getY() > maxY)
	    			maxY = node.getX();
	    	}
	    	outputStream.write(String.format("Bounding Box %s %s %s %s\n", 
	    			Double.toString(minX),
	    			Double.toString(minY),
	    			Double.toString(maxX),
	    			Double.toString(maxY)));
    		
	    	for (Node node : nodes)
	    		outputStream.write(String.format("node %d at %s m , %s m , %s m junction\n",
	    				node.getNodeID(),
	    				Double.toString(Double.isNaN(node.getX()) ? 0.0 : node.getX()),
	    				Double.toString(Double.isNaN(node.getY()) ? 0.0 : node.getY()),
	    				Double.toString(Double.isNaN(node.getZ()) ? 0.0 : node.getZ())));
        } finally {
        	outputStream.close();
        }
    }
}