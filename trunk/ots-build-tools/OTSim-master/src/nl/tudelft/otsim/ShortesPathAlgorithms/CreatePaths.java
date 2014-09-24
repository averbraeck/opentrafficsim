package nl.tudelft.otsim.ShortesPathAlgorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.tudelft.otsim.GeoObjects.Network;
import nl.tudelft.otsim.GeoObjects.Node;
import nl.tudelft.otsim.TrafficDemand.TripPattern;

/**
 *
 * @author gtamminga
 */
public class CreatePaths {
   
    /**
     * Create paths between all pairs of nodes, if possible.
     * Save the paths in a list of paths
     */
    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
      return bytes / MEGABYTE;
    }
    
    public static void CreatePathsTripPatterns(Network network, ArrayList<TripPattern> tripPatternList) {

    	class CompareNodeNumbers implements Comparator<Path> {
			@Override
			public int compare(Path path1, Path path2) {
				return path1.getNodeList().get(0).getNodeID() - path2.getNodeList().get(0).getNodeID();
			}
		}
    	
    	// Generate a list of all paths from the tripPatternList and sort it by node
    	ArrayList<Path> pathList = new ArrayList<Path>();
		for (int index1 = 0; index1 < tripPatternList.size(); index1++) {
			TripPattern tripPattern = tripPatternList.get(index1);
			for (int index2 = 0; index2 < tripPattern.getTripPatternPathList().size();
					index2++)   {
				ArrayList<Path> paths = tripPattern.getTripPatternPathList().get(index2).getPathList();
				pathList.addAll(paths);
			}
		}
		// sort list by NodeID (node number)
		Collections.sort(pathList, new CompareNodeNumbers());
		
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(network);
		ArrayList<Node> nodeList = new ArrayList<Node>();
		Node prevNode = null;
		// loop through all paths and select unique starting nodes
		// paths are generated for these nodes only
		for (int pathIndex = 0; pathIndex < pathList.size(); pathIndex++) {
			Path path = pathList.get(pathIndex);
			Node startNode = path.getNodeList().get(0);
			if (pathIndex == 0)   {
				nodeList.add(startNode);
			}				
			if (prevNode != null && startNode != null )  {
				if (startNode.getNodeID() > prevNode.getNodeID())  {
					nodeList.add(startNode);
				}				
			}	
			prevNode = startNode;
		}
        int index = 0;
		for (Node node: nodeList)  {
		    int ii=0;
		    int jj=0;

		    long startTime = System.currentTimeMillis();
	        ii++;
	        // Run the garbage collector
	        Runtime runtime = Runtime.getRuntime();
	        runtime.gc();
	        // Calculate the used memory
	        long memory = runtime.totalMemory() - runtime.freeMemory();
	        if (ii > 50) {
	        	ii = 0;
		    	System.out.println("done  " + node.getNodeID());
		        System.out.println("Used memory is megabytes: "
	            + bytesToMegabytes(memory));
		        long stopTime = System.currentTimeMillis();
		        long elapsedTime = stopTime - startTime;
		        System.out.println("Elapsed time: " + elapsedTime);
		        System.out.println("Found routes: " + jj);
	        }
	        
	        // Find all routes from a certain node to all other nodes
	        dijkstra.execute(node);
	        
	        while (index < pathList.size())  {
	        	if (pathList.get(index).getNodeList().get(0).getNodeID() > node.getNodeID())
	        		break;
	        	if (pathList.get(index).getNodeList().get(0).equals(node)) {
					int nodeCount = pathList.get(index).getNodeList().size();
			        Node toNode = pathList.get(index).getNodeList().get(nodeCount-1);
			        Node from = pathList.get(index).getNodeList().get(0);
			        	if (from.equals(node)) {
			        		ArrayList<Node> getPath = dijkstra.getPathNodes(toNode);
			        		if ( !(getPath == null))
			        			pathList.get(index).setNodeList(getPath);
			        		else
			        			System.out.println("no valid path found between " + from.getNodeID() + " and " + toNode.getNodeID());
			        	}
					}
	        	index++; 
	        	}
	        	
	        }
		
    }

}