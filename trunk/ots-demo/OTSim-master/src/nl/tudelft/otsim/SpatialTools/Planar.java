package nl.tudelft.otsim.SpatialTools;

import java.lang.Double;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GeoObjects.ActivityLocation;
import nl.tudelft.otsim.GeoObjects.CrossSection;
import nl.tudelft.otsim.GeoObjects.Lane;
import nl.tudelft.otsim.GeoObjects.Link;
import nl.tudelft.otsim.GeoObjects.Vertex;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

/**
 * 
 * @author Peter Knoppers
 * <br />
 * Perform various geometric operations.
 */
public class Planar {
	
	/**
	 * Convert a list of coordinates to an array of Point2D.Double. Any commas
	 * in the coordinates are replaced by dots before conversion is attempted.
	 * @param coordinates String[]; the list of coordinates
	 * @param start Integer; index of first entry in coordinates to use
	 * @param end Integer; the index of the last entry in coordinates to use <b>plus one</b>
	 * @return Point2D.Double[]; array of points
	 * @throws NumberFormatException 
	 */
	public static Point2D.Double[] coordinatesToPoints (String coordinates[], int start, int end) throws NumberFormatException {
		Point2D.Double[] points = new Point2D.Double[(end - start) / 2];
		for (int pair = 0; pair < points.length; pair++)
			points[pair] = new Point2D.Double(Double.parseDouble(fixRadix(coordinates[start + 2 * pair])),
					Double.parseDouble(fixRadix(coordinates[start + 2 * pair + 1])));
		return points;
	}
	
	/**
	 * Replace all commas in a text by a dot.
	 * @param text String; the text to modify
	 * @return String; the result where all commas are replaced by a dot
	 */
	public static String fixRadix(String text) {
		int pos = text.indexOf(",");
		if (pos >= 0)
			text = text.substring(0, pos) + "." + text.substring(pos + 1);
		return text;
	}
	
	/**
	 * Compute path length of ArrayList of vertices.
	 * @param vertices ArrayList&lt;{@link Vertex}&gt vertices to compute the
	 * path length of
	 * @return Double; length of the path
	 */
	public static double length(ArrayList<Vertex> vertices) {
		double result = 0;
		Vertex prevVertex = null;
		for (Vertex v : vertices) {
			if (null != prevVertex)
				result += prevVertex.distanceTo(v);
			prevVertex = v;
		}
		return result;
	}
	  
	/**
	 * Create a rotated, then translated array of an array of points. 
	 * @param polyLine Array of Point2D.Double (the points)
	 * @param rotation Rotation (in radians) around (0, 0)
	 * @param deltaX X-component of translation
	 * @param deltaY Y-component of translation
	 * @return Array of Point2D.Double holding the resulting points
	 */
	public static Point2D.Double[] rotateTranslatePolyLine(Point2D.Double[] polyLine, double rotation, double deltaX, double deltaY) {
		Point2D.Double[] result = new Point2D.Double[polyLine.length];
		for(int i = polyLine.length; --i >= 0; )
			result[i] = translatePoint(rotatePoint(polyLine[i], rotation), deltaX, deltaY);
		return result;
	}
	
	/**
	 * Rotate one point around (0, 0).
	 * @param in Point2D.Double; the point to rotate
	 * @param angle Rotation angle in radians
	 * @return new Point2D.Double of the resulting point
	 */
	public static Point2D.Double rotatePoint(Point2D.Double in, double angle) {
		double sinA = Math.sin(angle);
		double cosA = Math.cos(angle);
		return new Point2D.Double(in.x * cosA - in.y * sinA, + in.y * cosA + in.x * sinA);
	}
	
	/**
	 * Translate a point.
	 * @param in Point2D.Double; the point to translate
	 * @param deltaX X-component of translation
	 * @param deltaY Y-component of translation
	 * @return new Point2D.Double of the resulting point
	 */
	public static Point2D.Double translatePoint(Point2D.Double in, double deltaX, double deltaY) {
		return new Point2D.Double(in.x + deltaX, in.y + deltaY);
	}
	
	/**
	 * Project a point on a line (2D). The line is defined by two points. The
	 * result of this function is a point that may lie outside the line segment
	 * defined by the line parameter.
	 * @param line Line2D.Double; the line segment to project onto
	 * @param point Point2D.Double; the point to project onto the line
	 * @return new Point2D.Double projection of the point on the line
	 */
	public static Point2D.Double nearestPointOnLine(Line2D.Double line, Point2D.Double point) {
		// Derived from distanceLineSegmentToPoint (below) by PK
		double dX = line.x2 - line.x1;
		double dY = line.y2 - line.y1;
		if ((0 == dX) && (0 == dY))
			return new Point2D.Double(line.x1, line.y1);
		final double u = ((point.x - line.x1) * dX + (point.y - line.y1) * dY) 
				/ (dX * dX + dY * dY);
		return new Point2D.Double(line.x1 + u * dX, line.y1 + u * dY);	
	}
	
	/**
	 * Project a point on a line (2D). If the the projected points lies outside
	 * the line segment, the nearest end point of the line segment is returned.
	 * Otherwise the point return lies between the end points of the line
	 * segment. 
	 * <br />
	 * Adapted from <a href="http://paulbourke.net/geometry/pointlineplane/DistancePoint.java">
	 * example code provided by Paul Bourke</a>.
	 * @param lineSegment Line segment to project the point on to
	 * @param point Point to project onto the line segment
	 * @return new Point2D.Double; the projected point or one of the end points of the line segment
	 */
	public static double distanceLineSegmentToPoint(Line2D.Double lineSegment, Point2D.Double point) {
		double dX = lineSegment.x2 - lineSegment.x1;
		double dY = lineSegment.y2 - lineSegment.y1;
		if ((0 == dX) && (0 == dY))
			return (lineSegment.getP1().distance(point));
		final double u = ((point.x - lineSegment.x1) * dX + (point.y - lineSegment.y1) * dY)
				/ (dX * dX + dY * dY);
		final Point2D.Double closestPoint;
		if (u < 0)
			closestPoint = (Point2D.Double) lineSegment.getP1();
		else if (u > 1)
			closestPoint = (Point2D.Double) lineSegment.getP2();
		else
			closestPoint = new Point2D.Double(lineSegment.x1 + u * dX, lineSegment.y1 + u * dY);
		return closestPoint.distance(point);
	}
	
	/**
	 * Determine if a point is contained in a 2D polygon
	 * <br />
	 * Derived from <a href="http://bbs.dartmouth.edu/~fangq/MATH/download/source/Determining%20if%20a%20point%20lies%20on%20the%20interior%20of%20a%20polygon.htm">
	 * Paul Bourke's Determining if a point lies on the interior of a polygon</a>.
	 * @param polygon Array of Point2D.Double defining the polygon
	 * @param point The point to check
	 * @return true if the point is inside the polygon, otherwise false
	 */
	public static boolean polygonContainsPoint(Point2D.Double[] polygon, Point2D.Double point) {
		// First take care of the case where point coincides with one of the points on the polygon
		for (Point2D.Double p : polygon)
			if ((p.x == point.x) && (p.y == point.y))
				return true;	// we'll consider that a hit
		// http://paulbourke.net/geometry/insidepoly/ (Solution 2; 2D)
		double sumAngle = 0;
		Point2D.Double prevPoint = polygon[polygon.length - 1];
		for (Point2D.Double p : polygon) {
			Point2D.Double vector1 = new Point2D.Double(prevPoint.x - point.x, prevPoint.y - point.y);
			Point2D.Double vector2 = new Point2D.Double(p.x - point.x, p.y - point.y);
			double theta1 = Math.atan2(vector1.y, vector1.x);
			double theta2 = Math.atan2(vector2.y, vector2.x);
			double diffTheta = theta2 - theta1;
			while (diffTheta > Math.PI)
				diffTheta -= 2 * Math.PI;
			while (diffTheta < - Math.PI)
				diffTheta += 2 * Math.PI;
			sumAngle += diffTheta;
			//System.out.println(String.format("theta1=%.3f, theta2=%.3f diff=%.3f sum=%.3f", Math.toDegrees(theta1), Math.toDegrees(theta2), Math.toDegrees(diffTheta), Math.toDegrees(sumAngle)));
			prevPoint = p;
		}
		return (sumAngle > Math.PI) || (sumAngle < - Math.PI);
	}
	
	/**
	 * Determine the distance of a point to a polygon. If the point is inside
	 * the polygon the distance is 0; otherwise it is the distance to the
	 * nearest vertex of edge of the polygon.
	 * @param polygon Array of Point2D.Double defining the polygon
	 * @param point Point2D.Double the point
	 * @return Double; the distance of the point to the polygon
	 */
	public static double distancePolygonToPoint(Point2D.Double[] polygon, Point2D.Double point) {
		if (polygonContainsPoint(polygon, point))
			return 0;
		Point2D.Double prevPoint = polygon[polygon.length - 1];
		double closest = Double.MAX_VALUE;
		for (Point2D.Double p : polygon) {
			double distance = distanceLineSegmentToPoint(new Line2D.Double(prevPoint, p), point);
			if (distance < closest)
				closest = distance;
		}
		return closest;
	}
	
	/**
	 * Check if two polygons intersect. The polygons need not be closed.
	 * Derived from <href="http://www.codeproject.com/Articles/15573/2D-Polygon-Collision-Detection">2D
	 * Polygon Collision Detection - CodeProject</a>.
	 * @param polygon1 Point2D.Double[] first polygon
	 * @param polygon2 Point2D.Double[] second polygon
	 * @return Boolean; true if there is an intersection; false if there is no
	 * intersection
	 */
	public static boolean polygonIntersectsPolygon (Point2D.Double[] polygon1, Point2D.Double[] polygon2) {
		// Take care of the easy cases first
		if (polygonContainsPoint(polygon1, polygon2[0]))
			return true;
		if (polygonContainsPoint(polygon2, polygon1[0]))
			return true;
		// Find intersections of each edge of polygon 1 with polygon 2
		Point2D.Double prevPoint = polygon1[polygon1.length - 1];
		for (Point2D.Double p : polygon1) {
			if (lineIntersectsPolygon(new Line2D.Double(prevPoint, p), polygon2).size() > 0)
				return true;
			prevPoint = p;
		}
		// find intersections of each edge of polygon 2 with polygon 1
		prevPoint = polygon2[polygon2.length - 1];
		for (Point2D.Double p : polygon2) {
			if (lineIntersectsPolygon(new Line2D.Double(prevPoint, p), polygon1).size() > 0)
				return true;
			prevPoint = p;
		}
		return false;
	}
	
	/**
	 * Check if a polyLine intersects a line segment
	 * @param polyLine Point2D.Double[]; array of points of the polyLine
	 * @param p1 Point2D.Double; first end point of the line
	 * @param p2 Point2D.Double; second end point of the line
	 * @return Boolean; true if the polyLine intersects the line segment; false otherwise
	 */
	public static boolean polyLineIntersectsLine(Point2D.Double[] polyLine, Point2D.Double p1, Point2D.Double p2) {
		for (int i = polyLine.length - 1; --i >= 0; )
			if (lineSegmentIntersectsLineSegment(polyLine[i], polyLine[i + 1], p1, p2))
				return true;
		return false;
	}
	
	/**
	 * Check if a polyLine intersects a line segment
	 * @param polyLine Point2D.Double[]; array of points of the polyLine
	 * @param line Line2D.Double; the line segment
	 * @return Boolean; true if the polyLine intersects the line segment; false otherwise
	 */
	public static boolean polyLineIntersectsLine(Point2D.Double[] polyLine, Line2D.Double line) {
		return polyLineIntersectsLine(polyLine, new Point2D.Double(line.getX1(), line.getY1()), new Point2D.Double(line.getX2(), line.getY2()));
	}
	
	/**
	 * Check if two polyLines intersect
	 * @param firstPolyLine Point2D.Double[]; array of points of the first polyLine
	 * @param secondPolyLine Point2D.Double[]; array of points of the seconds polyLine
	 * @return Boolean; true if any line segment of the first polyLine intersects any line segment of the second polyLine; false otherwise
	 */
	public static boolean polyLineIntersectsPolyLine(Point2D.Double[] firstPolyLine, Point2D.Double[] secondPolyLine) {
		for (int i = firstPolyLine.length - 1; --i >= 0; )
			if (polyLineIntersectsLine(secondPolyLine, firstPolyLine[i], firstPolyLine[i + 1]))
				return true;
		return false;
	}
    
	/**
	 * Determine if to line segments intersect
	 * <br />
	 * Derived from <a href="http://paulbourke.net/geometry/pointlineplane/">Point,
	 * Line, Plane by Paul Bourke</a>.
	 * @param p1 Point2D.Double; begin point of the first line segment
	 * @param p2 Point2D.Double; end point of the first line segment
	 * @param p3 Point2D.Double; begin point of the second line segment
	 * @param p4 Point2D.Double; end point of the second line segment
	 * @return Boolean; true if the line segments intersect; false otherwise
	 */
	public static boolean lineSegmentIntersectsLineSegment(Point2D.Double p1, Point2D.Double p2, Point2D.Double p3, Point2D.Double p4) {
		double denominator = (p4.y - p3.y) * (p2.x - p1.x) - (p4.x - p3.x) * (p2.y - p1.y);
		if (denominator == 0f)
			return false;	// lines are parallel (they might even be on top of each other, but we don't check that)
		double uA = ((p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x)) / denominator;
		if ((uA < 0f) || (uA > 1f))
			return false;
		double uB = ((p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x)) / denominator;
		return ((uB >= 0) && (uB <= 1f));
	}
	
	/**
	 * Determine if to line segments intersect
	 * <br />
	 * Derived from <a href="http://paulbourke.net/geometry/pointlineplane/">Point,
	 * Line, Plane by Paul Bourke</a>.
	 * @param l1 Line2D.Double; first line segment
	 * @param l2 Line2D.Double; second line segment
	 * @return Boolean; true if the line segments intersect; false otherwise
	 */
	public static boolean lineIntersectsLine(Line2D.Double l1, Line2D.Double l2) {
		return (lineSegmentIntersectsLineSegment(new Point2D.Double(l1.x1, l1.y1), new Point2D.Double(l1.x2, l1.y2),
				new Point2D.Double(l2.x1, l2.y1), new Point2D.Double(l2.x2, l2.y2)));
	}
	
	/**
	 * Compute intersection (2D) of two (endless) lines. If the two presented 
	 * lines are (almost) parallel and share an end point; that end point is 
	 * returned
	 * <br />
	 * Derived from <a href="http://en.wikipedia.org/wiki/Line-line_intersection">Wikepedia
	 * Line-line intersection</a>.
	 * @param l1 Line2D.Double first line
	 * @param l2 Line2D.Double second line
	 * @return new Point2D.Double; the intersection point or null
	 */
	// TODO this is too similar to the lineIntersectsLine variants above
	public static Point2D.Double intersection(Line2D.Double l1, Line2D.Double l2) {
		double determinant = (l1.getX1() - l1.getX2()) * (l2.getY1() - l2.getY2()) - (l1.getY1() - l1.getY2()) * (l2.getX1() - l2.getX2());
		//System.out.println(String.format("determinant is %f", determinant));
		if (Math.abs(determinant) < 0.0000001) {
			// Lines are parallel; work-around code by PK
			// If the lines have a point EXACTLY in common, return that point
			if (l1.getX1() == l2.getX1() && l1.getY1() == l2.getY1())
				return new Point2D.Double(l1.getX1(), l1.getY1());
			if (l1.getX1() == l2.getX2() && l1.getY1() == l2.getY2())
				return new Point2D.Double(l1.getX1(), l1.getY1());
			if (l1.getX2() == l2.getX1() && l1.getY2() == l2.getY1())
				return new Point2D.Double(l1.getX2(), l1.getY2());
			if (l1.getX2() == l2.getX2() && l1.getY2() == l2.getY2())
				return new Point2D.Double(l1.getX2(), l1.getY2());
			//System.out.println("intersection returns null");
			// Otherwise let the caller deal with this
			return null;
		}
		return new Point2D.Double(
				((l1.getX1() * l1.getY2() - l1.getY1() * l1.getX2()) * (l2.getX1() - l2.getX2()) 
				- (l1.getX1() - l1.getX2()) * (l2.getX1() * l2.getY2() - l2.getY1() * l2.getX2())) 
				/ determinant, 
				((l1.getX1() * l1.getY2() - l1.getY1() * l1.getX2()) * (l2.getY1() - l2.getY2()) 
				- (l1.getY1() - l1.getY2()) * (l2.getX1() * l2.getY2() - l2.getY1() * l2.getX2())) 
				/ determinant);
	}
	
	// TODO: Move this function out of GeometryTools
	// Things like "ActivityLocation" and "CrossSection" should not be known about this class
	/**
	 * Find the line/link that is nearest to a certain point
	 * - first selects lines within a certain search distance
	 * - than find the nearest point at that line
	 * @param linkTree the linkTree (Spatial Index) contains all the links of the network
	 * @param list in this case a list of Buildings (point objects)
	 */
	public static void NearestPointAtLink(SpatialIndex linkTree, List<ActivityLocation> list) {
		for (ActivityLocation activityLocation : list)  {
			final double MAX_SEARCH_DISTANCE = 1000; 
			Coordinate point = new Coordinate(activityLocation.getX(),activityLocation.getY(), 0);
			Envelope search = new Envelope(point);  // search area: set it up
	        search.expandBy(MAX_SEARCH_DISTANCE);   // search area creates circle
			@SuppressWarnings("unchecked")
			List<LineString> links = linkTree.query(search);  // find the links within this area
	        double minDist = MAX_SEARCH_DISTANCE + 1.0e-6;
	        Coordinate minDistPoint = null;   // the coordinates of the nearest point of the nearest line 
	        Link attachedLink = null;
	        // Find the closest line within the search area (so within MAX_SEARCH_DISTANCE)
			for (LineString link : links) {
				LocationIndexedLine line1 = new LocationIndexedLine(link);  // Linear referencing along a line
	            LinearLocation here = line1.project(point);  // "here" is the point nearest on the line from "point" 
	            double longitudinal = here.getSegmentFraction() * here.getSegmentLength(link);	            
	            Coordinate pointFound = line1.extractPoint(here);
	            double dist = pointFound.distance(point);
	            if (dist < minDist) {
	                attachedLink = (Link) link.getUserData(); 	// attached to the line is the field "user data" 
			   													//	which points to the object "Link"
	                // find a section with parking lots or parking opportunities
	                for (CrossSection cs : attachedLink.getCrossSections_r()) {
	                	for (Lane lane : cs.collectLanes()) {
	                	//for (CrossSectionElement cse : cs.getCrossSectionElementList_r()) {
	                		//if (cse.getCrossSectionElementTypology().getDrivable()) {
	                			//for (Lane lane : cse.getLaneList()) {
            				if(lane.isParkingLane()) {
            					double distanceToLane = Math.abs(cs.getLongitudinalPosition_r() - longitudinal);
            					if (dist + distanceToLane < minDist)  {
            						minDist = dist + Math.abs(cs.getLongitudinalPosition_r() - longitudinal);
            		                minDistPoint = pointFound;
            		                Vertex v = new Vertex(line1.extractPoint(here, minDist));
            		                activityLocation.setPointAtLinkNearLocation(v);
            		                activityLocation.setLaneNearLocation(lane); 
            					}
	                				//}
	                			//}
	                		}
	                	}
	                }
	            }
	        }
	        if (minDistPoint == null)	// No line close enough to snap the point to
	            System.out.println(point + "- X" + attachedLink.getName_r());
	        else {
	            System.out.printf("%s - snapped by moving %.4fm\n", point.toString(), minDist );
	            System.out.println("Link " + attachedLink.getName_r() + "Lane and speed: " + activityLocation.getLaneNearLocation().getID() + "  " + activityLocation.getLaneNearLocation().getMaxSpeed());
	        }
		}
	}
	
	/**
	 * Determine on which side of a line a point lies.
	 * <br />
	 * This code is derived from the first answer on
	 * <a href="http://stackoverflow.com/questions/3461453/determine-which-side-of-a-line-a-point-lies">Determine
	 * which side of a line a point lies</a>
	 * @param p Point2D.Double; the point
	 * @param l Line2D.Double; the line
	 * @return Boolean; true for left; false otherwise (not 100% sure of this)
	 */
	// Derived from
	// http://stackoverflow.com/questions/3461453/determine-which-side-of-a-line-a-point-lies
	// First answer
	public static double pointSideOfLine(Point2D.Double p, Line2D.Double l) {
		return (l.x2 - l.x1) * (p.y - l.y1) - (l.y2 - l.y1)* (p.x - l.x1);
	}
	/**
	 * Find the minimum circle that covers a cloud of points.
	 * <br />
	 * Derived from <a href="http://www.ces.clemson.edu//~pmdrn/Dearing/location/minimax.pdf">minimax.pdf</a>.
	 * The algorithm presented there is n<sup>2</sup>, but it is fairly easy to 
	 * understand and n should be quite small for our uses.
	 * @param points List of points that must be covered
	 * @return The minimal circle covering the points
	 */
	public static Circle circleCoveringPoints(ArrayList<Point2D.Double> points) {
		//Log.logToFile("d:/circleCovering.txt", false, "Entering; points:");
		//for (Point2D.Double p : points)
		//	Log.logToFile("d:/circleCovering.txt", false, "Point2D.Double(%f, %f)", p.x, p.y);
		//System.out.println("Enter circleCoveringPoints: pointCloud: " + points.toString());
		// Find the smallest circle that covers the pointCloud
		// http://www.ces.clemson.edu//~pmdrn/Dearing/location/minimax.pdf
		// Take care of two degenerate cases first
		if (points.size() == 0)
			return null;	// Empty cloud... Garbage in; garbage out
		else if (points.size() == 1)
			return new Circle(points.get(0), 0);
		Point2D.Double pk = null;
		Point2D.Double pl = null;
		Point2D.Double center = null;
		double radius = 0d;
		final double margin = 0.0001;	// should take care of rounding errors
		// step 1: select (any) two points for pi and pj
		Point2D.Double pi = points.get(0);
		Point2D.Double pj = points.get(1);
		int stage = 2;	// go to step 2
		
		while (0 != stage) {
			//if (null == pk)
			//	Log.logToFile("d:/circleCovering.txt", false, "step=%d, pi=%f,%f, pj=%f,%f", stage, pi.x, pi.y, pj.x, pj.y);
			//else
			//	Log.logToFile("d:/circleCovering.txt", false, "step=%d, pi=%f,%f, pj=%f,%f, pk=%f,%f", stage, pi.x, pi.y, pj.x, pj.y, pk.x, pk.y);
			switch (stage) {
			case 2:
				center = new Point2D.Double((pi.x + pj.x) / 2, (pi.y + pj.y) / 2);
				double newRadius = pi.distance(pj) / 2 + margin;
				//Log.logToFile("d:/circleCovering.txt", false, "New circle (crossing 2 points) is %f,%f, radius %f", center.x, center.y, newRadius);
				if (newRadius <= radius - margin) {
					System.err.println("newRadius " + newRadius + " is not bigger than old radius " + radius);
					stage = 0;
					//Log.logToFile("d:/circleCovering.txt", false, "Failure: newRadius %f is not bigger than old radius %f", newRadius, radius);
					break;
				}
				radius = newRadius;
				pk = null;
				for (Point2D.Double p : points) {
					if (p.distance(center) > radius) {
						//Log.logToFile("d:/circleCovering.txt", false, "point %f,%f lies outside circle", p.x, p.y);
						pk = p;
						stage = 3;
						break;
					}
				}
				if (null == pk) {
					stage = 0;
					//Log.logToFile("d:/circleCovering.txt", false, "All points lie within the circle");
				}
				break;
				
			case 3: {
				// Check if the triangle pi,pj,pk is obtuse or right angled
				// If so, remove the point with the right or obtuse angle and go to step 2
				double d2ij = pi.distanceSq(pj);
				double d2ik = pi.distanceSq(pk);
				double d2jk = pj.distanceSq(pk);
				if (d2jk >= d2ij + d2ik) {
					//Log.logToFile("d:/circleCovering.txt", false, "d2jk (%f) >= d2ik (%f) + d2jk (%f) not acute; deleting pi", d2jk, d2ik, d2jk);
					pi = pj;
					pj = pk;
					pk = null;
					stage = 2;
					break;
				} else if (d2ik >= d2ij + d2jk) {
					//Log.logToFile("d:/circleCovering.txt", false, "d2ik (%f) >= d2ij (%f) + d2jk (%f) not acute; deleting pj", d2ik, d2ij, d2jk);
					pj = pk;
					pk = null;
					stage = 2;
					break;
				} else if (d2ij > d2ik + d2jk) {
					//Log.logToFile("d:/circleCovering.txt", false, "d2ij (%f) >= d2ik (%f) + d2jk (%f) not acute; deleting pk", d2ij, d2ik, d2jk);
					pk = null;
					stage = 2;
					break;
				} else {
					// We have an acute triangle
					// Construct the circle passing through pi,pj,pk
					//System.out.println("Constructing circle through " + pi.toString() + " " + pj.toString() + " " + pk.toString());
					Point2D.Double p11 = new Point2D.Double((pi.x + pj.x) / 2, (pi.y + pj.y) / 2);
					Point2D.Double p12 = new Point2D.Double(p11.x + pj.y - pi.y, p11.y - pj.x + pi.x);
					Line2D.Double line1 = new Line2D.Double(p11, p12);
					Point2D.Double p21 = new Point2D.Double((pi.x + pk.x) / 2, (pi.y + pk.y)/ 2);
					Point2D.Double p22 = new Point2D.Double(p21.x + pk.y - pi.y, p21.y - pk.x + pi.x);
					Line2D.Double line2 = new Line2D.Double(p21, p22);
					center = intersection(line1, line2);
					newRadius = pi.distance(center) + margin;
					//Log.logToFile("d:/circleCovering.txt", false, "New circle (crossing 3 points) %f,%f, radius %f", center.x, center.y, newRadius);
					if (newRadius <= radius - margin) {
						System.err.println("newRadius " + newRadius + " is not bigger than old radius " + radius);
						stage = 0;
						//Log.logToFile("d:/circleCovering.txt", false, "Failure: newRadius %f is not bigger than old radius %f", newRadius, radius);
						break;
					}
					radius = newRadius;
					// That was complex; check the result
					if (Math.abs(pi.distance(center) - radius) > 2 * margin)
						throw new Error("Cannot happen");
					if (Math.abs(pj.distance(center) - radius) > 2 * margin)
						throw new Error("Cannot happen");
					if (Math.abs(pk.distance(center) - radius) > 2 * margin)
						throw new Error("Cannot happen");
					pl = null;
					for (Point2D.Double p : points) {
						if (p.distance(center) > radius) {
							//Log.logToFile("d:/circleCovering.txt", false, "point %f,%f lies outside circle", p.x, p.y);
							pl = p;
							stage = 4;
							break;
						}
					}
					if (null == pl) {
						stage = 0;
						//Log.logToFile("d:/circleCovering.txt", false, "All points lie within the circle");
					}
					break;
				}
			}
			
			case 4: {
				// Find the point pq among pi, pj, pk that is furthest from pl
				double dli = pl.distance(pi);
				double dlj = pl.distance(pj);
				double dlk = pl.distance(pk);
				Point2D.Double pq = null;
				if ((dli > dlj) && (dli > dlk))
					pq = pi;
				else if ((dlj > dli) && (dlj > dlk))
					pq = pj;
				else 
					pq = pk;
				//Log.logToFile("d:/circleCovering.txt", false, "The point furthest from pl (%f,%f) is pq (%f,%f)", pl.x, pl.y, pq.x, pq.y);
				// This code is hard to read; verify the result
				double furthestDistance = pq.distance(pl);
				if (furthestDistance < dli)
					throw new Error("Cannot happen");
				if (furthestDistance < dlj)
					throw new Error("Cannot happen");
				if (furthestDistance < dlk)
					throw new Error("Cannot happen");
				
				// Construct the line through the center of the current circle and pq.
				// (This line divides the plane in two half planes.)
				// Then find the point pr among pi, pj, pk that is in the half plane opposite pl
				// NB. the point pq lies exactly on that line.
				Line2D.Double referenceLine = new Line2D.Double(pq, center);
				//Log.logToFile("d:/circleCovering.txt", false, "Reference line through pq is %f,%f -> %f,%f", referenceLine.x1, referenceLine.y1, referenceLine.x2, referenceLine.y2);
				double goodSide = pointSideOfLine(pl, referenceLine);
				if (0 == goodSide)
					throw new Error ("Cannot happen");
				int sign = goodSide > 0 ? 1 : -1;
				Point2D.Double[] pts = new Point2D.Double[] { pi, pj, pk };
				Point2D.Double pr = null;
				double errorMargin = 0;
				while (null == pr) {
					for(Point2D.Double p : pts) {
						if (p == pq)
							continue;	// ignore this one; it should lie ON the line
						double signedPosition = pointSideOfLine (p, referenceLine) * sign;
						//Log.logToFile("d:/circleCovering.txt", false, "signedPosition of %f,%f is %f", p.x, p.y, signedPosition);
						if (signedPosition <= errorMargin) {
							//Log.logToFile("d:/circleCovering.txt", false, "Setting pr to %f,%f", p.x, p.y);
							pr = p;
						}
					}
					if (null == pr) {
						if (0 == errorMargin)
							errorMargin = margin;
						else
							errorMargin = errorMargin * 10;
						System.out.println("Rounding error in circleCoveringPoints; trying " + errorMargin);
					}
				}
				pi = new Point2D.Double(pq.x, pq.y);
				pj = new Point2D.Double(pr.x, pr.y);
				pk = pl;
				stage = 3;
				break;
			}
		}
		}
		//Log.logToFile("d:/circleCovering.txt", false, "Final result: circle centered at %f,%f, radius %f", center.x, center.y, radius);
		return new Circle(center, radius);
	}
	
	/**
	 * Check that the circleCovereringPoints method behaves well. There has
	 * been a case where it failed (due to a bug) on this particular set of 
	 * points.
	 */
	public static void testCircleCoveringPoints() {
	    // This set of points caused circleCoveringPoints to fail (never finish) when margin was 0.000001
	    ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
	    points.add(new Point2D.Double(86444.20734684722, 442696.03123710706));
	    points.add(new Point2D.Double(86444.18221227782, 442699.5311468564));
	    points.add(new Point2D.Double(86444.28509257668, 442699.530562569));
	    points.add(new Point2D.Double(86444.22020688458, 442696.0311640711));
	    points.add(new Point2D.Double(86444.20166811542, 442695.0313359289));
	    points.add(new Point2D.Double(86444.13678242332, 442691.531937431));
	    points.add(new Point2D.Double(86444.11598296637, 442691.5323772043));
	    points.add(new Point2D.Double(86444.1990681833, 442695.03139090055));
	    circleCoveringPoints(points);
	}

	/**
	 * Check tat the circleCoveringsPoints method behaves well. There has been
	 * a case where it failed (due to another bug) on this particular set of
	 * points.
	 */
	public static void testCircleCoveringPoints2() {
	    // This set of points caused circleCoveringPoints to fail (never finish) when margin was 0.000001
	    ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
	    points.add(new Point2D.Double(50.896195, -200.443661));
	    points.add(new Point2D.Double(46.415221, -198.225357));
	    //points.add(new Point2D.Double(46.415221, -198.225357));
	    points.add(new Point2D.Double(51.164450, -186.377939));
	    //points.add(new Point2D.Double(51.164450, -186.377939));
	    points.add(new Point2D.Double(45.631733, -184.030182));
	    //points.add(new Point2D.Double(45.631733, -184.030182));
	    points.add(new Point2D.Double(36.660364, -190.882476));
	    points.add(new Point2D.Double(44.697691, -184.743599));
	    points.add(new Point2D.Double(39.150151, -185.853107));
	    points.add(new Point2D.Double(35.653602, -191.651437));
	    //points.add(new Point2D.Double(35.653602, -191.651437));
	    points.add(new Point2D.Double(36.559156, -197.589149));
	    //points.add(new Point2D.Double(36.559156, -197.589149));
	    points.add(new Point2D.Double(49.215535, -196.077677));
	    points.add(new Point2D.Double(47.287538, -196.463277));
	    //points.add(new Point2D.Double(47.287538, -196.463277));
	    points.add(new Point2D.Double(49.215535, -196.077677));
	    points.add(new Point2D.Double(47.287538, -196.463277));
	    //points.add(new Point2D.Double(47.287538, -196.463277));
	    points.add(new Point2D.Double(50.196116, -200.980581));
	    circleCoveringPoints(points);
	}

	/**
	 * Compute all intersection points of a ray and a circle. There are zero,
	 * one, or two intersection. NB. A ray is a line with one real end point. 
	 * The other end lies at infinity.
	 * <br />
	 * Derived from <a href="http://paulbourke.net/geometry/circlesphere/">Circles
	 * and spheres by Paul Bourke</a>.
	 * @param l Line2D.Double; the ray
	 * @param circle Circle; the circle.
	 * @return Array of Point2D.Double with all intersections
	 */
	static public Point2D.Double[] intersectRayAndCircle(Line2D.Double l, Circle circle) {
		double a = l.getP1().distanceSq(l.getP2());
		double b = 2 * ((l.x2 - l.x1) * (l.x1 - circle.center().x) + (l.y2 - l.y1) * (l.y1 - circle.center().y));
		double c = circle.center().x * circle.center().x + circle.center().y * circle.center().y + l.x1 * l.x1 + l.y1 * l.y1 - 2 * (circle.center().x * l.x1 + circle.center().y * l.y1) - circle.radius() * circle.radius(); 
		double discr = b * b - 4 * a * c;
		if (discr < 0)
			return new Point2D.Double[0];	// no solutions
		double u1 = (-b + Math.sqrt(discr)) / 2 / a;
		double u2 = (-b - Math.sqrt(discr)) / 2 / a;
		int solutions = 0;
		if ((0 <= u1) && (u1 <= 1))
			solutions++;
		if ((0 <= u2) && (u1 <= 2))
			solutions++;
		Point2D.Double result[] = new Point2D.Double[solutions];
		int index = 0;
		if ((0 <= u1) && (u1 <= 1))
			result[index++] = new Point2D.Double(l.x1 + (l.x2 - l.x1)* u1, l.y1 + (l.y2 - l.y1)* u1);
		if ((0 <= u2) && (u2 <= 1))
			result[index++] = new Point2D.Double(l.x1 + (l.x2 - l.x1)* u2, l.y1 + (l.y2 - l.y1)* u2);
		return result;
	}
	
	/**
	 * Return a String describing an Array of Point2D.Double. (Used for
	 * debugging.)
	 * @param points Array of Point2D.Double
	 * @return String
	 */
	public static String pointsToString(Point2D.Double[] points) {
		String result = "";
		for (Point2D.Double p : points)
			result += String.format(Main.locale, " %.3f,%.3f", p.x, p.y);
		return result;
	}
	
	/**
	 * Convert Vertices to a String
	 * @param vertices ArrayList&lt;{@link Vertex}&gt;; list of vertices to
	 * convert 
	 * @return String; space-separated list of the toString() representation
	 * of the vertices
	 */
	public static String verticesToString(ArrayList<Vertex> vertices) {
		String result = "";
		for (Vertex v : vertices)
			result += v.toString() + " ";
		return result;
	}

	/**
	 * Return a String describing a Line2D.Double. (Used for debuggin.)
	 * @param l Line2D.Double; the line
	 * @return String
	 */
	public static String Line2DToString(Line2D l) {
		return String.format(Main.locale, "(%.3f, %.3f)->(%.3f,%.3f)", l.getX1(), l.getY1(), l.getX2(), l.getY2());
	}

	/**
	 * Convert a {@link GeneralPath} to a human-readable String.
	 * <br /> Adapted from http://www.javadocexamples.com/java_source/edu/umd/cs/piccolo/util/PUtil.java.html
	 * @param generalPath {@link GeneralPath}; the path to convert
	 * @return String; the human-readable representation of the {@link GeneralPath}
	 */
	public static String GeneralPathToString (GeneralPath generalPath) {
		String result = "";
		PathIterator it = generalPath.getPathIterator(null, 1);
		while (! it.isDone())
		{
			float[] data = new float[6];
			switch(it.currentSegment(data)) {
			case PathIterator.SEG_MOVETO: result += String.format(Locale.US, "m %.3f,%.3f ", data[0], data[1]); break;
			case PathIterator.SEG_LINETO: result += String.format(Locale.US, "l %.3f,%.3f ", data[0], data[1]); break;
			case PathIterator.SEG_QUADTO: result += String.format(Locale.US, "q %.3f,%.3f %.3f,%.3f ", data[0], data[1], data[2], data[3]); break;
			case PathIterator.SEG_CUBICTO: result += String.format(Locale.US, "m %.3f,%.3f %.3f,%.3f %.3f,%.3f ", data[0], data[1], data[2], data[3], data[4], data[5]); break;
			case PathIterator.SEG_CLOSE: result += "c "; break;
			default: throw new Error("unknown segment type " + it.currentSegment(data));
			}
			it.next();
		}			
		return result;
	}

	/**
	 * Print the value of a Point2D.Double to the console with a description
	 * and return the Point2D.Double. (To be inserted around Point2D.Double
	 * parameters of methods for debugging.")
	 * @param where String; description of the parameter or the situation
	 * @param p Point2D.Double; the point
	 * @return Point2D.Double; the point
	 */
	public static Point2D.Double logPoint(String where, Point2D.Double p) {
		System.out.format(Main.locale, "%s: (%.3f,%.3f)\r\n", where, p.x, p.y);
		return p;
	}
	
	/**
	 * Compute the convex hull of a set of points.
	 * <br />
	 * Derived from
	 * <a href="http://profs.etsmtl.ca/mmcguffin/code/java/SimplePaint-2D_JavaApplication/Point2DUtil.java">http://profs.etsmtl.ca/mmcguffin/code/java/SimplePaint-2D_JavaApplication/Point2DUtil.java</a>.
	 * Michael J. McGuffin, Ph.D. has given permission to use this code in an
	 * email message to Peter Knoppers.
	 * @param points ArrayList&lt;Point2D.Double&gt; the set of points
	 * @return ArrayList&lt;Point2D.Double&gt; a polygon describing the convex hull
	 */
	public static ArrayList<Point2D.Double> convexHull(ArrayList<Point2D.Double> points) {
		ArrayList<Point2D.Double> returnValue = new ArrayList<Point2D.Double>();

		if (points.size() < 3) {
			for (Point2D.Double p : points)
				returnValue.add(p);
			return returnValue;
		}
		// There could be one or more points with minimal y coordinate.
		// We'll call these the "bottom" points.
		// Of these, we find the one with minimal x coordinate (the "bottom left" point)
		// and maximal x coordinate (the "bottom right" point).
		int indexOfBottomLeftPoint = 0;
		Point2D.Double bottomLeftPoint = points.get(0);
		int indexOfBottomRightPoint = 0;
		Point2D.Double bottomRightPoint = points.get(0);
		for (int i = 1; i < points.size(); ++i) {
			Point2D.Double candidatePoint = points.get(i);
			if (candidatePoint.y < bottomLeftPoint.y) {
				indexOfBottomLeftPoint = indexOfBottomRightPoint = i;
				bottomLeftPoint = bottomRightPoint = candidatePoint;
			} else if (candidatePoint.y == bottomLeftPoint.y) {
				if (candidatePoint.x < bottomLeftPoint.x) {
					indexOfBottomLeftPoint = i;
					bottomLeftPoint = candidatePoint;
				}
				else if (candidatePoint.x > bottomRightPoint.x) {
					indexOfBottomRightPoint = i;
					bottomRightPoint = candidatePoint;
				}
			}
		}

		// Imagine that for each point, we compute the point's angle with respect to bottomLeftPoint,
		// and then sort the points by this angle.
		// This is equivalent to sorting the points by their cotangent, which is faster to compute.
		// Points with minimal y coordinate (i.e., "bottom" points)
		// will be given a cotangent of +infinity and dealt with later.
		Point2DAndScore [] pointsWithCotangents = new Point2DAndScore[points.size()];
		for (int i = 0; i < points.size(); ++i) {
			Point2D.Double p = points.get(i);
			double delta_y = p.y - bottomLeftPoint.y;
			assert delta_y >= 0;
			if (delta_y == 0)
				pointsWithCotangents[i] = new Point2DAndScore(p, 0, true);
			else
				pointsWithCotangents[i] = new Point2DAndScore(p, (p.x - bottomLeftPoint.x) / delta_y /* the cotangent */, false);
		}
		// sort the points by their cotangent
		Arrays.sort(pointsWithCotangents, new Point2DAndScoreComparator());

		// We'll need to be able to efficiently remove points from consideration,
		// so we copy them into a linked list.
		// In doing this, we also reverse the order of points
		// (so they are in descending order of cotangent, i.e., in counter-clockwise order).
		// The points with +infinity cotangent (i.e. the "bottom" points)
		// can also be removed from consideration here,
		// so long as we keep the "bottom left" and "bottom right" points.
		LinkedList<Point2D.Double> orderedPoints = new LinkedList<Point2D.Double>();
		orderedPoints.add(bottomLeftPoint);
		// check if the "bottom left" and "bottom right" points are distinct
		if (indexOfBottomLeftPoint != indexOfBottomRightPoint)
			orderedPoints.add(bottomRightPoint);
		for (int i = pointsWithCotangents.length - 1; i >= 0; --i)
			if (! pointsWithCotangents[i].isScorePositiveInfinity)
				orderedPoints.add(pointsWithCotangents[i].point);

		if (orderedPoints.size() > 2) {
			// We will loop through the ordered points, processing 3 consecutive points at a time.
			// Two iterators are used to backup and move forward.
			Point2D.Double p0 = orderedPoints.get(0);
			Point2D.Double p1 = orderedPoints.get(1);
			Point2D.Double p2 = orderedPoints.get(2);
			ListIterator<Point2D.Double> it3 = orderedPoints.listIterator(3);
			assert it3.nextIndex() == 3;
			while (true) {
				assert orderedPoints.size() > 2;
				Point2D.Double v01 = new Point2D.Double(p1.x - p0.x, p1.y - p0.y);
				Point2D.Double v12 = new Point2D.Double(p2.x - p1.x, p2.y - p1.y);

				// Compute the z component of the cross product of v1 and v2
				// (Note that the x and y components of the cross product are zero,
				// because the z components of a and b are both zero)
				double crossProduct_z = v01.x * v12.y - v01.y * v12.x;

				if (crossProduct_z > 0) {
					// we have a left turn; try to step forward
					if (it3.hasNext()) {
						p0 = p1;
						p1 = p2;
						p2 = it3.next();
					} else // we can't step forward
						break;
				} else {
					// Either we have a right-hand turn,
					// or the points are collinear (with the 3rd point either in front, or behind, the 2nd)
					// In any case, we remove the 2nd point from consideration and (try to) backup.
					assert it3.hasPrevious();
					it3.previous();
					assert it3.hasPrevious();
					it3.previous();
					it3.remove(); // deletes the 2nd point
					assert it3.hasNext();
					it3.next(); // now the iterator is back to where it used to be

					// now we try to backup
					assert it3.hasPrevious();
					it3.previous();
					assert it3.hasPrevious();
					it3.previous();
					if (it3.hasPrevious()) {
						p1 = p0;
						p0 = it3.previous();
						it3.next();
						it3.next();
						it3.next();
					} else {
						it3.next();
						it3.next();
						// we step forward instead
						if (it3.hasNext()) {
							p1 = p2;
							p2 = it3.next();
						}
						else // we can't move in either direction
							break;
					}
				}
			} // while
		}

		for (Point2D.Double p : orderedPoints )
			returnValue.add(p);
		return returnValue;
	}

	/**
	 * Determine all intersections of a line and a polygon. NB. It is not
	 * necessary that the last point of the polygon is a repeat of the first,
	 * but it does not hurt either; the polygon is implicitly closed by this 
	 * method.
	 * <br />
	 * If the line intersects the polygon at a vertex, this intersection might
	 * be reported twice.
	 * @param line Line2D.Double; the line
	 * @param polygon ArrayList&lt;Point2D.Double&gt;; the polygon
	 * @return ArrayList&ltPoint2D.Double&gt;; All intersection points.
	 */
	public static ArrayList<Point2D.Double> lineIntersectsPolygon(Line2D.Double line, Point2D.Double[] polygon) {
		ArrayList<Point2D.Double> result = new ArrayList<Point2D.Double>(); 
		if (polygon.length < 2)
			return result;
		Point2D.Double prevPoint = polygon[polygon.length - 1];
		for (Point2D.Double p : polygon) {
			Line2D.Double polygonLine = new Line2D.Double(prevPoint, p);
			if (lineIntersectsLine (line, polygonLine))
				result.add(intersection(line, polygonLine));
			prevPoint = p;
		}
		return result;
	}
	
	/**
	 * Shorten a polyline to a sub-section specified by distance. If the
	 * requested distance range lies outside the range of the polyline and
	 * empty list of vertices is returned.
	 * @param polyline ArrayList&lt;{@link Vertex}&gt;; the polyline
	 * @param longitudinalPosition Double; starting position of the slice
	 * (negative value means starting position is measured from the end)
	 * @param longitudinalLength Double; length of the requested sub-section
	 * @return ArrayList&lt;{@link Vertex}&gt;; the sub-section
	 */
	public static ArrayList<Vertex> slicePolyline(ArrayList<Vertex> polyline, double longitudinalPosition, double longitudinalLength) {
		ArrayList<Vertex> result = new ArrayList<Vertex> ();
		double pos = longitudinalPosition;
		double pathLength = length(polyline);
		if (pos < 0)
			pos = pathLength + pos;
		if (pos < 0) {
			//System.err.println("Slice lies before range of polyline");
			return result;
		}
		if (pos > pathLength) {
			//System.err.println("Slice lies beyond range of polyline");
			return result;
		}
		Vertex prevVertex = null;
		for (Vertex v : polyline) {
			if (null != prevVertex) {
				double distance = prevVertex.distanceTo(v);
				if (distance > pos) {
					// Slice starts between prevVertex and v
					result.add(Vertex.weightedVertex(pos / distance, prevVertex, v));
					double useLength = longitudinalLength;
					if (useLength <= 0)
						useLength = 0.001;
					result.add(Vertex.weightedVertex((pos + longitudinalLength) / distance, prevVertex, v));
					return result;
				}
				pos -= distance;
			}
			prevVertex = v;
		}
		System.err.println("Slice lies unexpectadly outside range of polyline");
		return result;
	}
	
	/**
	 * Create a polyline which is starts or ends from a reference polyline. 
	 * If the reference polyline is malformed (double vertices or no vertices),
	 * the result may be malformed.
	 * @param prevVertices ArrayList&lt;{@link Vertex}&gt;; the previous
	 * polyline
	 * @param sameUp boolean; start at prevVertices
	 * @param sameDown boolean; end at prevVertices
	 * @return ArrayList&lt;{@link Vertex}&gt;; the new polyline
	 */
	
	public static ArrayList<Vertex> createPartlyParallelVertices(ArrayList<Vertex> prevVertices, ArrayList<Vertex> curVertices, boolean sameUp, boolean sameDown) {
    	ArrayList<Vertex> result = new ArrayList<Vertex>();
    	Vertex prevV = null;
    	double distLaneTotal = 0.0;
    	for (Vertex v : prevVertices)  {
    		if (prevV != null) {
    			distLaneTotal = distLaneTotal + v.distanceTo(prevV); 
    		}
    		prevV = new Vertex(v);		
    	}
    	prevV = null;    	
    	double distLane = 0.0;
    	int i = 0;
    	for (Vertex v : prevVertices)  {
        	double weight = 0.0;
    		if (prevV != null) {
    			distLane = distLane + v.distanceTo(prevV); 
    		}
			if (sameUp == true)
				weight = (distLane / distLaneTotal); 
			else if (sameDown == true)
				weight = 1 - (distLane / distLaneTotal); 
			result.add(Vertex.weightedVertex(weight,  v, curVertices.get(i)));
			prevV = new Vertex(v);
    		i++;
    	}
		return result;
	}

	/**
	 * Create a polyline with specified offset from a reference polyline. If
	 * the reference polyline is malformed (double vertices or no vertices),
	 * the result may be malformed.
	 * @param referenceVertices ArrayList&lt;{@link Vertex}&gt;; the reference
	 * polyline
	 * @param prevReferenceVertices ArrayList&lt;{@link Vertex}&gt;; the reference
	 * polyline of the preceding design line
	 * @param lateralPosition Double; offset used for each vertex
	 * vertices
	 * @return ArrayList&lt;{@link Vertex}&gt;; the new polyline
	 */
    public static ArrayList<Vertex> createParallelVertices(ArrayList<Vertex> referenceVertices, ArrayList<Vertex> prevReferenceVertices, double lateralPosition) {
    	return createParallelVertices(referenceVertices, prevReferenceVertices, lateralPosition, lateralPosition);
    }
	
	/**
	 * Create a polyline with specified offset from a reference polyline. If
	 * the reference polyline is malformed (double vertices or no vertices),
	 * the result may be malformed.
	 * @param referenceVertices ArrayList&lt;{@link Vertex}&gt;; the reference
	 * polyline
	 * @param prevReferenceVertices ArrayList&lt;{@link Vertex}&gt;; the reference
	 * polyline of the preceding design line
	 * @param firstLateralPosition Double; offset used for the first vertex
	 * @param subsequentLateralPosition Double; offset used for all other
	 * vertices
	 * @return ArrayList&lt;{@link Vertex}&gt;; the new polyline
	 */
    public static ArrayList<Vertex> createParallelVertices(ArrayList<Vertex> referenceVertices, ArrayList<Vertex> prevReferenceVertices, double firstLateralPosition, double subsequentLateralPosition) {
    	// Create an ArrayList of vertices at a certain offset from a reference
    	//System.out.println(String.format("\r\ncreateParallelVertices: offset is %f, number of vertices is %d\r\n\t%s", lateralPosition, referenceVertices.size(), referenceVertices.toString()));
    	ArrayList<Vertex> result = new ArrayList<Vertex>();
    	Vertex prevVertex = null;
    	Line2D.Double prevParallel = null;
    	if (prevReferenceVertices != null) {
    		int size = prevReferenceVertices.size();
    	    Vertex Vertex1 = prevReferenceVertices.get(size-2);
    		Vertex vertex2 = prevReferenceVertices.get(size-1);
			prevParallel = new Line2D.Double(Vertex1.getX(), Vertex1.getY(), vertex2.getX(), vertex2.getY());
    	}
    	for (Vertex vertex : referenceVertices) {		
    		if (null != prevVertex)	{
    			// compute the line parallel to reference line
    			double direction = Math.atan2(vertex.getY() - prevVertex.getY(), vertex.getX() - prevVertex.getX());
    			double perpendicular = direction - Math.PI / 2;
    			//System.out.println(String.format("dir=%f, perp=%f, offset=%f,%f", direction, perpendicular, offsetX, offsetY));
    			Line2D.Double parallel = new Line2D.Double(prevVertex.getX() + Math.cos(perpendicular) * firstLateralPosition, prevVertex.getY() + Math.sin(perpendicular) * firstLateralPosition,
    					vertex.getX() + Math.cos(perpendicular) * subsequentLateralPosition, vertex.getY() + Math.sin(perpendicular) * subsequentLateralPosition);
    			//System.out.println(String.format("parallel is %f,%f -> %f,%f", parallel.x1, parallel.y1, parallel.x2, parallel.y2));
    			if (null == prevParallel) {
    				// simply offset the first point laterally
    				result.add(new Vertex(parallel.x1, parallel.y1, prevVertex.getZ()));
    			} else {
    				// We have a previous parallel and a current parallel.
    				// Compute the intersection
    				Point2D.Double p = intersection(prevParallel, parallel);
    				if (null == p)	// probably an (almost) straight line; use the previous point
    					result.add(new Vertex(parallel.x1, parallel.y1, vertex.getZ()));
    				else
    					result.add(new Vertex(p, vertex.getZ()));
    			}    			
    			prevParallel = parallel;
    			firstLateralPosition = subsequentLateralPosition;
    		}
			prevVertex = vertex;
    	}
    	// If we had at least two vertices, add the end point of the last parallel
    	if (null != prevParallel)
    		result.add(new Vertex(prevParallel.x2, prevParallel.y2, prevVertex.getZ()));
    	//System.out.println(String.format("result: %s", result.toString()));
        return result;		        	
    }
    
    /**
	 * Create a polyline with specified offset from a reference polyline. If
	 * the reference polyline is malformed (double vertices or no vertices),
	 * the result may be malformed.
     * @param referenceVertices ArrayList&lt;{@link Vertex}&gt;; the reference
	 * polyline
     * @param lateralPosition Double; offset used for all vertices
     * @return ArrayList&lt;{@link Vertex}&gt;; the new polyline
     */
    public static ArrayList<Vertex> createParallelVertices(ArrayList<Vertex> referenceVertices, double lateralPosition) {
    	return createParallelVertices(referenceVertices, null, lateralPosition, lateralPosition);
    }

    /**
     * Close a polyLine.
     * @param shape Point2D.Double[]; given polyLine.
     * @return Point2D.Doubel[]; the original shape if it was already closed,
     * or a copy with the first point added as the last
     */
	public static Point2D.Double[] closePolyline(Point2D.Double[] shape) {
		if (shape[0] == shape[shape.length - 1])
			return shape;
		Point2D.Double result[] = new Point2D.Double[shape.length + 1];
		for(int i = shape.length; --i >= 0; )
			result[i] = shape[i];
		result[shape.length]= shape[0]; 
		return result;
	}

	/**
	 * Project a set of {@link Vertex Vertices} onto the elevation == 0 plane;
	 * returning only the X and Y values as an array of Point2D.Double.
	 * @param polygon ArrayList&lt;{@link Vertex}&gt;; the set of vertices
	 * @return Point2D.Double[]; array of Point2D.Double; the projected Vertices
	 */
	public static Point2D.Double[] getAlignment(ArrayList<Vertex> polygon) {
		Point2D.Double result[] = new Point2D.Double[polygon.size()];
		for (int index = 0; index < result.length; index++)
			result[index] = polygon.get(index).getPoint();
		return result;
	}    
	
	/**
	 * Convert an ArrayList&lt;Point2D.Double&gt; into an array of Point2D.Double.
	 * @param polyLine ArrayList&lt;Point2D.Double&gt;; the input ArrayList
	 * @return Point2D.Double[]; array of Point2D.Double
	 */
	public static Point2D.Double[] ArrayListOfPointsToArray (ArrayList<Point2D.Double> polyLine) {
		Point2D.Double result[] = new Point2D.Double[polyLine.size()];
		for (int index = 0; index < result.length; index++)
			result[index] = polyLine.get(index);	
		return result;
	}

}


// This is used to sort points by some "score",
// which could be an angle or other metric associated with each point.
// Used (only) in the convexHull algorithm
class Point2DAndScore {
	public Point2D.Double point;
	public double score;
	public boolean isScorePositiveInfinity; // if true, ``score'' is ignored
	public Point2DAndScore(Point2D.Double p, double s, boolean isPosInf) {
		point = p;
		score = s;
		isScorePositiveInfinity = isPosInf;
	}
}

class Point2DAndScoreComparator implements Comparator<Point2DAndScore> {
	@Override
	public int compare(Point2DAndScore a, Point2DAndScore b) {
		if (a.isScorePositiveInfinity) {
			if (b.isScorePositiveInfinity)
				return 0; // equal
			return 1; // a is greater
		} else if (b.isScorePositiveInfinity)
			return -1; // b is greater
		else
			return (a.score < b.score) ? -1 : ((a.score > b.score) ? 1 : 0 );
	}
}
