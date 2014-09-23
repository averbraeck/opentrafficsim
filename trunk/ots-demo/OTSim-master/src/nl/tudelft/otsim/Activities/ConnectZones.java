package nl.tudelft.otsim.Activities;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.otsim.GeoObjects.ActivityLocation;
import nl.tudelft.otsim.GeoObjects.CrossSection;
import nl.tudelft.otsim.GeoObjects.CrossSectionElement;
import nl.tudelft.otsim.GeoObjects.CrossSectionObject;
import nl.tudelft.otsim.GeoObjects.Lane;
import nl.tudelft.otsim.GeoObjects.Link;
import nl.tudelft.otsim.GeoObjects.MicroZone;
import nl.tudelft.otsim.GeoObjects.Vertex;
import nl.tudelft.otsim.SpatialTools.SpatialQueries;
import nl.tudelft.otsim.TrafficDemand.TripPattern;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

public class ConnectZones {
	/**
	 * For every activity location, search for the nearest link with "parking lanes"
	 * @param linkTree
	 * @param activityLocation 
	 * @param maxSearchDistance 
	 * @param maxSpeed 
	 */
	public static void NearestPointAtLink(SpatialIndex linkTree, ActivityLocation activityLocation, double maxSearchDistance, double maxSpeed) {		
		Coordinate pointBuilding = new Coordinate(activityLocation.getX(),activityLocation.getY(), 0);
		Envelope search = new Envelope(pointBuilding);  // search area: set it up
        search.expandBy(maxSearchDistance);   // search area creates circle
		List<LineString> links = linkTree.query(search);  // find the links within this area
        double minDist = maxSearchDistance + 1.0e-6;
        Coordinate minDistPoint = null;   // the coordinates of the nearest point of the nearest line 
        Link attachedLink = null;
        // Find the closest line within the search area (so within MAX_SEARCH_DISTANCE)
		for (LineString link : links) {
			LocationIndexedLine line1 = new LocationIndexedLine(link);  // Linear referencing along a line
            LinearLocation here = line1.project(pointBuilding);  // "here" is the point nearest on the line from "point" 
            double longitudinal = here.getSegmentFraction() * here.getSegmentLength(link);	            
            Coordinate pointFound = line1.extractPoint(here);
            double dist = pointFound.distance(pointBuilding);
            if (dist < minDist) {
                attachedLink = (Link) link.getUserData(); 	// attached to the line is the field "user data" 
		   													//	which points to the object "Link"
                // find a section with parking lots or parking opportunities
                ArrayList<CrossSection> csList = attachedLink.getCrossSections_r(); 
                for (CrossSection cs : csList) {
                	List<CrossSectionElement> cseList = cs.getCrossSectionElementList_r();
                	for (CrossSectionElement cse : cseList) {
                		for (CrossSectionObject cso : cse.getCrossSectionObjects(Lane.class)) {
                			Lane lane = (Lane) cso;
            				if(lane.getMaxSpeed() < maxSpeed) {
            					double distanceToLane = cs.getLongitudinalPosition_r() - longitudinal;
            					// may be positive or negative
            					if (dist + Math.abs(distanceToLane) < minDist) {
            						minDist = dist + Math.abs(distanceToLane);
            		                minDistPoint = pointFound;
                					Vertex v = new Vertex(cse.getLinkPointOuter(0, false, true, false));
            		                activityLocation.setPointAtLinkNearLocation(v);
            		                activityLocation.setFromNodeNearLocation(attachedLink.getFromNode_r());
            		                activityLocation.setLinkNearLocation(attachedLink); 
            		                activityLocation.setLaneNearLocation(lane); 
            					}
            				}
                		}
                		
                	}
                }
            }
        }
        if (minDistPoint == null) {
            // No line close enough to snap the point to
            System.out.println(pointBuilding + "- X" + attachedLink.getName_r());

        } else {
            System.out.printf("%s - snapped by moving %.4f\n", 
            		pointBuilding.toString(), minDist );
            System.out.println("Link " + attachedLink.getName_r() + "Lane and speed: " + activityLocation.getLaneNearLocation().getID() + "  " + activityLocation.getLaneNearLocation().getMaxSpeed());
        }

	}

	private static void ConnectMicrozoneToLinks(ArrayList<Link> linkList, ArrayList<MicroZone> microZoneList, ArrayList<TripPattern> tripPatternList )  {
		/*returns a new list of trip Patterns but now from link to link
		 * 
		 */	
		GeometryFactory geometryFactory = new GeometryFactory();
		for (Link link: linkList )   {
			double MAX_SEARCH_DISTANCE = 1000;
			SpatialIndex microZoneTree = SpatialQueries.createVertexQuery(microZoneList, MAX_SEARCH_DISTANCE);
			double x = (link.getToNode_r().getX() + link.getFromNode_r().getX())/2;
			double y = (link.getToNode_r().getY() + link.getFromNode_r().getY())/2;
			Coordinate pointOfLink = new Coordinate(x, y, 0.0);
			Envelope search = new Envelope(pointOfLink);  // search area: set it up
			Point pointLink = geometryFactory.createPoint(pointOfLink);
	        search.expandBy(MAX_SEARCH_DISTANCE);   // search area creates circle
			List<Point> microZones = microZoneTree.query(search);  // find the microZones within this area
	        double minDist = MAX_SEARCH_DISTANCE + 1.0e-6;
	        MicroZone attachedMicroZone = null;
			for (Point microZone : microZones) {
	            double dist = microZone.distance(pointLink);
	            if (dist < minDist) {
	            	attachedMicroZone = (MicroZone) microZone.getUserData(); 	// attached to the line is the field "user data" 
			   													//	which points to the object "Link"
	            }
			}
			if (attachedMicroZone.getMicroZoneLink() == null)   {
				attachedMicroZone.setMicroZoneLink(new ArrayList<Link>());
			}
			attachedMicroZone.getMicroZoneLink().add(link);			
		}
	}

}