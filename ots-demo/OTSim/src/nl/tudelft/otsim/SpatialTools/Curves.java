package nl.tudelft.otsim.SpatialTools;

/*
 * Copyright (c) 2005 David Benson
 *  
 * See LICENSE file in distribution for licensing details of this source file
 */

import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.otsim.GeoObjects.Vertex;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class Curves {

	public static Geometry createBezierCurve(Coordinate start,
            Coordinate end,
            Coordinate ctrlPoint1,
            Coordinate ctrlPoint2,
            double smooth) {
		Shape curve = new CubicCurve2D.Double(
		start.x, start.y,
		ctrlPoint1.x, ctrlPoint1.y,
		ctrlPoint2.x, ctrlPoint2.y,
		end.x, end.y);
		
		// the value of the smooth arg determines how closely the line
		// segments between points approximate the smooth curve
		// (see javadocs for Shape.getPathIterator method)
		
		PathIterator iter = curve.getPathIterator(null, smooth);
		
		// a length 6 array is required for the iterator
		double[] iterBuf = new double[6];
		
		List<Coordinate> coords = new ArrayList<Coordinate>();
		while (!iter.isDone()) {
			iter.currentSegment(iterBuf);
			coords.add(new Coordinate(iterBuf[0], iterBuf[1]));
			iter.next();
		}
		
		GeometryFactory gf = new GeometryFactory();
		return gf.createLineString(coords.toArray(new Coordinate[coords.size()]));
	}

	public static Geometry createQuadCurve(Coordinate start, Coordinate end,
            Coordinate ctrlPoint1, double smooth) {
		Shape curve = new QuadCurve2D.Double(start.x, start.y, ctrlPoint1.x, ctrlPoint1.y,
		end.x, end.y);
		
		// the value of the smooth arg determines how closely the line
		// segments between points approximate the smooth curve
		// (see javadocs for Shape.getPathIterator method)
		
		PathIterator iter = curve.getPathIterator(null, smooth);
		
		// a length 6 array is required for the iterator
		double[] iterBuf = new double[6];
		
		List<Coordinate> coords = new ArrayList<Coordinate>();
		while (!iter.isDone()) {
			iter.currentSegment(iterBuf);
			coords.add(new Coordinate(iterBuf[0], iterBuf[1]));
			iter.next();
		}
		
		GeometryFactory gf = new GeometryFactory();
		return gf.createLineString(coords.toArray(new Coordinate[coords.size()]));
	}

	public static Point2D.Double createControlPoint(ArrayList<Vertex> up, ArrayList<Vertex> down)  {	
		Vertex start = up.get(up.size() - 1);
		Vertex prevStart = up.get(up.size() - 2);
		//when distance between vertices the angle is not robust. Choose a vertex further away
		for (int i = 2; (start.distance(prevStart) < 3)  && (up.size() - i - 1 >= 0); i++)
			prevStart = up.get(up.size() - i - 1);
		Vertex end = down.get(0);
		Vertex endNext = down.get(1);
		for (int i = 2; (end.distance(endNext) < 3) && (i < down.size()); i++)
			endNext = down.get(i);
		
		Line2D.Double line1 = new Line2D.Double(prevStart.getPoint(), start.getPoint());
		Line2D.Double line2 = new Line2D.Double(end.getPoint(), endNext.getPoint());
		if (headingDifference(line1, line2) < 0.3 * Math.PI || headingDifference(line1, line2) > 1.7 * Math.PI)
			// Take care of the case that the lines are (almost) parallel (and ctrlPoint1 would be null)
			return new Point2D.Double(line1.getX2() + (line2.getX1() - line1.getX2()) / 2, 
					line1.getY2() + (line2.getY1() - line1.getY2()) / 2);
		Point2D.Double ctrlPoint1 = Planar.intersection(line1, line2);
		if (ctrlPoint1 == null)
			System.out.println("stop strange");
		return ctrlPoint1;
	}
	
	private static double headingDifference(Line2D.Double line1, Line2D.Double line2)  {
		Double angle1 = Planar.normalizeAngle(Math.atan2(line1.getY2() - line1.getY1() , line1.getX2() - line1.getX1()));
		Double angle2 = Planar.normalizeAngle(Math.atan2(line2.getY2() - line2.getY1() , line2.getX2() - line2.getX1()));
		return Math.abs(angle2 - angle1);
	}
	
    public static ArrayList<Vertex> connectVerticesCurve(ArrayList<Vertex> up, ArrayList<Vertex> down, 
    		Point2D.Double ctrlPoint1, Double smooth) {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();	
		Vertex start = up.get(up.size() - 1);
		Vertex end = down.get(0);
		int a1 = (int) start.getX();
		int a2= (int) end.getX();
		int a3 = (int) ctrlPoint1.getX();
		int a4 = smooth.intValue();
		
		System.out.println("Curve " + a1 + " b " + a2 + " c " + a3 + " d " + a4);
		Geometry curve = createQuadCurve(new Coordinate(start.getX(), start.getY()), new Coordinate(end.getX(), end.getY()), new Coordinate(ctrlPoint1.getX(), ctrlPoint1.getY()), smooth);
        Coordinate[] points1 = curve.getCoordinates();
    	for (Coordinate c : points1)
    		vertices.add(new Vertex(c.x, c.y, 0.0));
		return vertices;
    }
}
