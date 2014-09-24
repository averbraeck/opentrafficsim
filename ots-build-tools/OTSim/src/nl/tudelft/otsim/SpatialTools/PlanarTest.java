package nl.tudelft.otsim.SpatialTools;

import static org.junit.Assert.*;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Locale;

import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GeoObjects.Vertex;

import org.junit.Test;

/**
 * Test the methods in the Planar class
 * 
 * @author Peter Knoppers
 */
public class PlanarTest {

	/**
	 * Test parser for String[] to Point2D.Double[]
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testCoordinatesToPointsStringArray() {
		String[] in = {"123,45", "678.90", "12", "34"};
		Point2D.Double[] result = Planar.coordinatesToPoints(in);
		assertEquals("Length of result should be 2", 2, result.length);
		assertEquals("First point of result", result[0].distance(new Point2D.Double(123.45, 678.90)), 0, 0.00001);
		assertEquals("Second point of result", result[1].distance(new Point2D.Double(12, 34)), 0, 0.00001);
		String[] in2 = {"1234a", "4567"};
		try {
			Planar.coordinatesToPoints(in2);
			fail("Should have thrown a NumberFormatException");
		} catch (NumberFormatException e) {
			; // ignore
		}
		String[] in3 = {};
		Planar.coordinatesToPoints(in3);
	}

	/**
	 * Test slicing of String[] and parsing to Point2D.Double[]
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testCoordinatesToPointsStringArrayIntInt() {
		String[] in = {"123,45", "678.90", "12", "34", "56", "78"};
		Point2D.Double[] result = Planar.coordinatesToPoints(in, 1, 5);
		assertEquals("Length of result should be 2", 2, result.length);
		assertEquals("First point of result", result[0].distance(new Point2D.Double(678.90, 12)), 0, 0.00001);
		assertEquals("Second point of result", result[1].distance(new Point2D.Double(34, 56)), 0, 0.00001);
		try {
			Planar.coordinatesToPoints(in, 1, 7);
			fail("Should have thrown an exception (array index out of bounds)");
		} catch (Exception e) {
			; // ignore
		}
		try {
			Planar.coordinatesToPoints(in, -1, 1);
			fail("Should have thrown an exception (array index out of bounds)");
		} catch (Exception e) {
			; // ignore
		}
	}

	/**
	 * Test fixRadix
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testFixRadix() {
		String in = "123.45abc";
		assertEquals("No comma in input should return unchanged input", Planar.fixRadix(in), in);
		String in2 = "123,456,78";
		assertEquals("Only first comma should be replaced", Planar.fixRadix(in2), "123.456,78");
	}

	/**
	 * Test computation of the length of an ArrayList<Vertex>
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testLength() {
		boolean errorThrown = false;
		try {
			Planar.length(null);
		} catch (Error e) {
			errorThrown = true;
		}
		assertTrue("Should have thrown an Error", errorThrown);
		ArrayList<Vertex> in = new ArrayList<Vertex> ();
		assertEquals("empty list has effective length of 0 (this is actually debatable)", 0, Planar.length(in), 0.000001);
		in.add(new Vertex(10, 20, 30));
		assertEquals("List of one vertex has effective length of 0", 0, Planar.length(in), 0.000001);
		in.add(new Vertex(10, 20, 30));
		assertEquals("List of two identical vertices has effective length of 0", 0, Planar.length(in), 0.000001);
		in.remove(1);
		assertEquals("check remove op", 1, in.size());
		in.add(new Vertex(110, 220, 330));
		assertEquals("Length of two-vertex list", Math.sqrt(100 * 100 + 200 * 200 + 300 * 300), Planar.length(in), 0.0001);
		in.add(new Vertex(10, 20, 30));
		assertEquals("Length of three-vertex list", 2 * Math.sqrt(100 * 100 + 200 * 200 + 300 * 300), Planar.length(in), 0.0001);
	}

	/**
	 * Test the rotateTranslatePolyLine method
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testRotateTranslatePolyLine() {
		Point2D.Double[] emptyArray = new Point2D.Double[0];
		assertEquals("Empty array in should result in empty array out", 0, Planar.rotateTranslatePolyLine(emptyArray, 10, 20, 30).length);
		Point2D.Double[] onePointArray = new Point2D.Double[1];
		onePointArray[0] = new Point2D.Double(1, 2);
		Point2D.Double[] result = Planar.rotateTranslatePolyLine(onePointArray, Math.PI / 2, 10, 20);
		assertEquals("One point array in should result in one point array out", 1, result.length);
		assertEquals("Expected result point", 0, new Point2D.Double(8, 21).distance(result[0]), 0.000001);
		Point2D.Double[] threePointArray = new Point2D.Double[3];
		threePointArray[0] = new Point2D.Double(1, 2);
		threePointArray[1] = new Point2D.Double(3, 4);
		threePointArray[2] = new Point2D.Double(-5, -6);
		result = Planar.rotateTranslatePolyLine(threePointArray, -Math.PI / 2, 10, 20);
		assertEquals("Three point array in should result in three point array out", 3, result.length);
		assertEquals("Expected result point", 0, new Point2D.Double(12, 19).distance(result[0]), 0.000001);
		assertEquals("Expected result point", 0, new Point2D.Double(14, 17).distance(result[1]), 0.000001);
		assertEquals("Expected result point", 0, new Point2D.Double(4, 25).distance(result[2]), 0.000001);
	}

	/**
	 * Test the rotate around origin method
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testRotatePoint() {
		Point2D.Double p = new Point2D.Double (10, 0);
		Point2D.Double q = Planar.rotatePoint(p, Math.PI / 2);
		assertEquals("rotated point should be at expected location", 0, q.distance(new Point2D.Double(0, 10)), 0.000001);
		p = new Point2D.Double (10, 10);
		q = Planar.rotatePoint(p, Math.PI / 2);
		assertEquals("rotated point should be at expected location", 0, q.distance(new Point2D.Double(-10, 10)), 0.000001);
		q = Planar.rotatePoint(q, Math.PI / 4);
		assertEquals("rotated point should be at expected location", 0, q.distance(new Point2D.Double(-Math.sqrt(2) * 10, 0)), 0.000001);
	}

	/**
	 * Test the translatePoint method
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testTranslatePoint() {
		Point2D.Double p = new Point2D.Double(10, 0);
		Point2D.Double q = Planar.translatePoint(p, 5, 6);
		assertEquals("q should be translated p", 0, q.distance(new Point2D.Double(15, 6)), 0.000001);
		q = Planar.translatePoint(q, -12, -100);
		assertEquals("q should be translated p", 0, q.distance(new Point2D.Double(3, -94)), 0.000001);
	}

	/**
	 * Test the nearestPointOnLine method
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testNearestPointOnLine() {
		// Doing the math to figure out the correct solutions was not as easy as I expected
		Line2D.Double l = new Line2D.Double(6, 0, 0, 8);
		Point2D.Double p = new Point2D.Double(0, 0);
		Point2D.Double q = Planar.nearestPointOnLine(l, p);
		Point2D.Double expected = new Point2D.Double (8 / (6d / 8 + 8d / 6), 6 / (6d / 8 + 8d / 6));
		//System.out.println("e " + expected.x + ", " + expected.y);
		//System.out.println("q " + q.x + ", " + q.y);
		assertEquals("Distance from expected location should be 0", 0, expected.distance(q), 0.00001);
		p = new Point2D.Double(3, 0);
		q = Planar.nearestPointOnLine(l, p);
		expected = new Point2D.Double (3 + 4 / (6d / 8 + 8d / 6), 3 / (6d / 8 + 8d / 6));
		//System.out.println("e " + expected.x + ", " + expected.y);
		//System.out.println("q " + q.x + ", " + q.y);
		assertEquals("Distance from expected location should be 0", 0, expected.distance(q), 0.00001);
		p = new Point2D.Double (6, 0);
		q = Planar.nearestPointOnLine(l, p);		
		assertEquals("Distance to endpoint is 0", 0, new Point2D.Double(6, 0).distance(q), 0.000001);
		p = new Point2D.Double (12, 0);
		q = Planar.nearestPointOnLine(l, p);		
		expected = new Point2D.Double (12 - 8 / (6d / 8 + 8d / 6), -6 / (6d / 8 + 8d / 6));
		//System.out.println("e " + expected.x + ", " + expected.y);
		//System.out.println("q " + q.x + ", " + q.y);
		assertEquals("Distance from expected location should be 0", 0, expected.distance(q), 0.00001);
		p = new Point2D.Double(0, 4);
		q = Planar.nearestPointOnLine(l, p);		
		expected = new Point2D.Double (4 / (6d / 8 + 8d / 6), 4 + 3 / (6d / 8 + 8d / 6));
		//System.out.println("e " + expected.x + ", " + expected.y);
		//System.out.println("q " + q.x + ", " + q.y);
		assertEquals("Distance from expected location should be 0", 0, expected.distance(q), 0.00001);
		p = new Point2D.Double(3, 4);
		q = Planar.nearestPointOnLine(l, p);		
		expected = new Point2D.Double (3,4);
		//System.out.println("e " + expected.x + ", " + expected.y);
		//System.out.println("q " + q.x + ", " + q.y);
		assertEquals("Distance from expected location should be 0", 0, expected.distance(q), 0.00001);
		p = new Point2D.Double(6, 8);
		q = Planar.nearestPointOnLine(l, p);		
		expected = new Point2D.Double (6 - 8 / (6d / 8 + 8d / 6), 8 - 6 / (6d / 8 + 8d / 6));
		//System.out.println("e " + expected.x + ", " + expected.y);
		//System.out.println("q " + q.x + ", " + q.y);
		assertEquals("Distance from expected location should be 0", 0, expected.distance(q), 0.00001);
	}

	/**
	 * Test the distanceLineSegmentToPoint method
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testDistanceLineSegmentToPoint() {
		Line2D.Double l = new Line2D.Double(6, 0, 0, 8);
		Point2D.Double p = new Point2D.Double(0, 0);
		double d = Planar.distanceLineSegmentToPoint(l, p);
		double expected = new Point2D.Double (8 / (6d / 8 + 8d / 6), 6 / (6d / 8 + 8d / 6)).distance(p);
		//System.out.println("e " + expected.x + ", " + expected.y);
		//System.out.println("q " + q.x + ", " + q.y);
		assertEquals("Expected distance", expected, d, 0.00001);
		p = new Point2D.Double(20, 0);
		assertEquals("Expected distance to end point", 14, Planar.distanceLineSegmentToPoint(l, p), 0.00001);
		p = new Point2D.Double(26, -20);
		assertEquals("Expected distance to end point", Math.sqrt(2) * 20, Planar.distanceLineSegmentToPoint(l, p), 0.00001);
		p = new Point2D.Double(-10, 18);
		assertEquals("Expected distance to end point", Math.sqrt(2) * 10, Planar.distanceLineSegmentToPoint(l, p), 0.00001);
	}

	/**
	 * Test the polygonContainsPoint method
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testPolygonContainsPoint() {
		// Simple triangle; a bit off the "grid" so we don't run into boundary cases
		Point2D.Double[] polygon = new Point2D.Double[3];
		polygon[0] = new Point2D.Double(10.2, 10.1);
		polygon[1] = new Point2D.Double(20.2, 10.1);
		polygon[2] = new Point2D.Double(20.2, 20.1);
		for (int x = -20; x <= 30; x++) {
			for (int y = -20; y <= 30; y++) {
				final Point2D.Double p = new Point2D.Double(x, y);
				// figure out the truth
				final boolean expected = (y > 10) && (x > 10) && (x <= 20) && (y < x);
				//System.out.println("p (" + x + "," + y + ") expects " + (expected ? "true" : "false"));
				if (expected)
					assertTrue("Is inside", Planar.polygonContainsPoint(polygon, p));
				else
					assertFalse("Is outside", Planar.polygonContainsPoint(polygon, p));
			}
		}
		// TODO write tests using a more "interesting" polygon
	}

	/**
	 * Test the distancePolygonToPoint method
	 * <br /> A major flaw in distancePolygonToPoint was discovered during
	 * development of this test.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testDistancePolygonToPoint() {
		Point2D.Double[] polygon = new Point2D.Double[3];
		polygon[0] = new Point2D.Double(10, 10);
		polygon[1] = new Point2D.Double(20, 10);
		polygon[2] = new Point2D.Double(20, 20);
		
		for (int x = -20; x <= 30; x++) {
			for (int y = -20; y <= 30; y++) {
				final Point2D.Double p = new Point2D.Double(x, y);
				// figure out the truth
				double expected;
				if ((y >= 10) && (x >= 10) && (x <= 20) && (y <= x))
					expected = 0;
				else if (y < 10) {
					if (x < 10)
						expected = p.distance(polygon[0]);
					else if (x > 20)
						expected = p.distance(polygon[1]);
					else
						expected = 10 - y;
				} else if (x > 20) {
					if (y < 10)
						expected = p.distance(polygon[1]);
					else if (y > 20)
						expected = p.distance(polygon[2]);
					else
						expected = x - 20;
				} else {
					if (y + x < 20)
						expected = p.distance(polygon[0]);
					else if (y + x > 40)
						expected = p.distance(polygon[2]);
					else
						expected = (y - x) / Math.sqrt(2);
				}
				//System.out.println("p (" + x + "," + y + ") expects " + expected);
				assertEquals("Expected distance", expected, Planar.distancePolygonToPoint(polygon, p), 0.00001);
			}
		}
	}

	/**
	 * Test the polygonIntersectsPolygon method
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testPolygonIntersectsPolygon() {
		// Simple triangle; a bit off the "grid" so we don't run into boundary cases
		Point2D.Double[] polygon1 = new Point2D.Double[3];
		polygon1[0] = new Point2D.Double(10.2, 10.1);
		polygon1[1] = new Point2D.Double(20.2, 10.1);
		polygon1[2] = new Point2D.Double(20.2, 20.1);
		for (int x = -20; x <= 30; x++) {
			for (int y = -20; y <= 30; y++) {
				// Simple square; on the grid
				Point2D.Double[] polygon2 = new Point2D.Double[4];
				polygon2[0] = new Point2D.Double(x, y);
				polygon2[1] = new Point2D.Double(x + 2, y);
				polygon2[2] = new Point2D.Double(x + 2, y + 2);
				polygon2[3] = new Point2D.Double(x, y + 2);
				// figure out the truth
				final boolean expected = (y > 8) && (y <= 20) && (x > 8) && (x <= 20) && (y < x + 2);
				//System.out.println("p (" + x + "," + y + ") expects " + (expected ? "true" : "false"));
				if (expected)
					assertTrue("Is intersecting", Planar.polygonIntersectsPolygon(polygon1, polygon2));
				else
					assertFalse("Is disjunct", Planar.polygonIntersectsPolygon(polygon1, polygon2));
			}
		}
	}

	/**
	 * Test the polyLineIntersectsLine method that takes two points as the last arguments.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testPolyLineIntersectsLineDoubleArrayDoubleDouble() {
		Point2D.Double[] polyLine = new Point2D.Double[3];
		polyLine[0] = new Point2D.Double(10, 10);
		polyLine[1] = new Point2D.Double(20, 10);
		polyLine[2] = new Point2D.Double(30, 30);
		assertTrue("should intersect", Planar.polyLineIntersectsLine(polyLine, new Point2D.Double(15, 10), new Point2D.Double(20, 20)));
		assertTrue("should intersect", Planar.polyLineIntersectsLine(polyLine, new Point2D.Double(20, 20), new Point2D.Double(40, 5)));
		assertFalse("should not intersect", Planar.polyLineIntersectsLine(polyLine, new Point2D.Double(0, 0), new Point2D.Double(40, 5)));
		assertFalse("should not intersect", Planar.polyLineIntersectsLine(polyLine, new Point2D.Double(9, 0), new Point2D.Double(9, 995)));
	}

	/**
	 * Test the polyLineIntersectsLine method that takes a Line2D as the second argument.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testPolyLineIntersectsLineDoubleArrayDouble() {
		Point2D.Double[] polyLine = new Point2D.Double[3];
		polyLine[0] = new Point2D.Double(10, 10);
		polyLine[1] = new Point2D.Double(20, 10);
		polyLine[2] = new Point2D.Double(30, 30);
		assertTrue("should intersect", Planar.polyLineIntersectsLine(polyLine, new Line2D.Double(15, 10, 20, 20)));
		assertTrue("should intersect", Planar.polyLineIntersectsLine(polyLine, new Line2D.Double(20, 20, 40, 5)));
		assertFalse("should not intersect", Planar.polyLineIntersectsLine(polyLine, new Line2D.Double(0, 0, 40, 5)));
		assertFalse("should not intersect", Planar.polyLineIntersectsLine(polyLine, new Line2D.Double(9, 0, 9, 995)));
	}

	/**
	 * Test the polyLineIntersectsPolyLine method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testPolyLineIntersectsPolyLine() {
		Point2D.Double[] p1 = new Point2D.Double[0];
		Point2D.Double[] p2 = new Point2D.Double[0];
		assertFalse("Degenerate case", Planar.polyLineIntersectsPolyLine(p1, p2));
		p1 = new Point2D.Double[1];
		p1[0] = new Point2D.Double(10, 20);
		assertFalse("Degenerate case", Planar.polyLineIntersectsPolyLine(p1, p2));
		assertFalse("Degenerate case", Planar.polyLineIntersectsPolyLine(p2, p1));
		p2 = new Point2D.Double[1];
		p2[0] = new Point2D.Double(20, 20);
		assertFalse("Degenerate case", Planar.polyLineIntersectsPolyLine(p1, p2));
		assertFalse("Degenerate case", Planar.polyLineIntersectsPolyLine(p2, p1));
		p1 = new Point2D.Double[2];
		p1[0] = new Point2D.Double(10, 20);
		p1[1] = new Point2D.Double(10, 40);
		assertFalse("Degenerate case", Planar.polyLineIntersectsPolyLine(p1, p2));
		assertFalse("Degenerate case", Planar.polyLineIntersectsPolyLine(p2, p1));
		p2 = new Point2D.Double[2];
		p2[0] = new Point2D.Double(0, 20);
		p2[1] = new Point2D.Double(40, 20);
		assertTrue("Simple hit", Planar.polyLineIntersectsPolyLine(p1, p2));
		assertTrue("Simple hit", Planar.polyLineIntersectsPolyLine(p2, p1));
		p2[1] = new Point2D.Double(-40, 20);
		assertFalse("Simple miss", Planar.polyLineIntersectsPolyLine(p1, p2));
		assertFalse("Simple miss", Planar.polyLineIntersectsPolyLine(p2, p1));
		p1 = new Point2D.Double[100];
		// put in a cloud of points all lying on an ellipse
		for (int n = 0; n < 100; n++)
			p1[n] = new Point2D.Double(30 * Math.sin(n) * 10, 20 + Math.cos(n) * 20);
		assertFalse("Should miss", Planar.polyLineIntersectsPolyLine(p1, p2));
		assertFalse("Should miss", Planar.polyLineIntersectsPolyLine(p2, p1));
		p2[1] = new Point2D.Double(40, 40);
		assertTrue("Should hit", Planar.polyLineIntersectsPolyLine(p1, p2));
		assertTrue("Should hit", Planar.polyLineIntersectsPolyLine(p2, p1));
	}

	/**
	 * Test the lineSegmentIntersectsLineSegment method taking 4 points.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testLineSegmentIntersectsLineSegment() {
		assertTrue("should intersect", Planar.lineSegmentIntersectsLineSegment(new Point2D.Double(10, 10), new Point2D.Double(20, 11), new Point2D.Double(15, 2), new Point2D.Double(13, 90)));
		assertTrue("should intersect", Planar.lineSegmentIntersectsLineSegment(new Point2D.Double(10, 10), new Point2D.Double(20, 11), new Point2D.Double(10, 9), new Point2D.Double(20, 12)));
		assertTrue("should intersect", Planar.lineSegmentIntersectsLineSegment(new Point2D.Double(10, 10), new Point2D.Double(20, 11), new Point2D.Double(20, 12), new Point2D.Double(10, 9)));
		assertFalse("should not intersect", Planar.lineSegmentIntersectsLineSegment(new Point2D.Double(10, 10), new Point2D.Double(20, 20), new Point2D.Double(11, 10), new Point2D.Double(21, 20)));
		assertFalse("should not intersect", Planar.lineSegmentIntersectsLineSegment(new Point2D.Double(10, 10), new Point2D.Double (20, 10), new Point2D.Double(21, 10), new Point2D.Double(30, 10)));
		assertFalse("should not intersect", Planar.lineSegmentIntersectsLineSegment(new Point2D.Double(10, 10), new Point2D.Double (20, 10), new Point2D.Double(1, 9), new Point2D.Double(15, 11)));
	}

	/**
	 * Test the lineSegmentIntersectsLineSegment method taking two lines.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testLineIntersectsLine() {
		assertTrue("should intersect", Planar.lineSegmentIntersectsLineSegment(new Line2D.Double(10, 10, 20, 11), new Line2D.Double(15, 2, 13, 90)));
		assertTrue("should intersect", Planar.lineSegmentIntersectsLineSegment(new Line2D.Double(10, 10, 20, 11), new Line2D.Double(10, 9, 20, 12)));
		assertTrue("should intersect", Planar.lineSegmentIntersectsLineSegment(new Line2D.Double(10, 10, 20, 11), new Line2D.Double(20, 12, 10, 9)));
		assertFalse("should not intersect", Planar.lineSegmentIntersectsLineSegment(new Line2D.Double(10, 10, 20, 20), new Line2D.Double(11, 10, 21, 20)));
		assertFalse("should not intersect", Planar.lineSegmentIntersectsLineSegment(new Line2D.Double(10, 10, 20, 10), new Line2D.Double(21, 10, 30, 10)));
		assertFalse("should not intersect", Planar.lineSegmentIntersectsLineSegment(new Line2D.Double(10, 10, 20, 10), new Line2D.Double(1, 9, 15, 11)));
	}

	/**
	 * Test the intersection method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testIntersection() {
		Line2D.Double l1 = new Line2D.Double(10, 20, 30, 40);
		Line2D.Double l2 = new Line2D.Double(30, 0, 0, 30);
		Point2D.Double p = Planar.intersection(l1, l2);
		assertTrue("Should give a non-null result", null != p);
		assertEquals("Should intersect at start of l1", 0, p.distance(l1.getP1()), 0.000001);
		p = Planar.intersection(l2, l1);	// order of arguments should not matter
		assertTrue("Should give a non-null result", null != p);
		assertEquals("Should intersect at start of l1", 0, p.distance(l1.getP1()), 0.000001);
		l2 = new Line2D.Double(30, 40, 50, 60);	// special case; start of l2 is end of l1
		p = Planar.intersection(l1, l2);
		assertEquals("Should return common point of l1 and l2", 0, p.distance(l1.getP2()), 0.000001);
		p = Planar.intersection(l2, l1);
		assertEquals("Should return common point of l1 and l2", 0, p.distance(l1.getP2()), 0.000001);
		l2 = new Line2D.Double(50, 60, 30, 40);
		p = Planar.intersection(l1, l2);
		assertEquals("Should return common point of l1 and l2", 0, p.distance(l1.getP2()), 0.000001);
		p = Planar.intersection(l2, l1);
		assertEquals("Should return common point of l1 and l2", 0, p.distance(l1.getP2()), 0.000001);
		// Exactly parallel won't work in general unless the lines are parallel to X or Y
		l1 = new Line2D.Double(0, 0, 0, 10);
		l2 = new Line2D.Double(10, 0, 10, 10);
		p = Planar.intersection(l1, l2);
		assertTrue("Should give a null result", null == p);
	}

	/**
	 * Test the pointSideOfLine method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testPointSideOfLine() {
		assertTrue("point is on the left of the line", Planar.pointSideOfLine(new Point2D.Double(1, 1), new Line2D.Double(1, 0, 2, 1)) > 0);
		assertFalse("point is on the left of the line", Planar.pointSideOfLine(new Point2D.Double(2, 0), new Line2D.Double(1, 0, 2, 1)) > 0);
		assertEquals("point is on the line", 0, Planar.pointSideOfLine(new Point2D.Double(3, 2), new Line2D.Double(1, 0, 2, 1)), 0.0001);
		// now reverse the direction of the line and see if the answers are inverted
		assertFalse("point is on the left of the line", Planar.pointSideOfLine(new Point2D.Double(1, 1), new Line2D.Double(2, 1, 1, 0)) > 0);
		assertTrue("point is on the left of the line", Planar.pointSideOfLine(new Point2D.Double(2, 0), new Line2D.Double(2, 1, 1, 0)) > 0);
		assertEquals("point is on the line", 0, Planar.pointSideOfLine(new Point2D.Double(3, 2), new Line2D.Double(2, 1, 1, 0)), 0.0001);
	}

	/**
	 * Test the circleCoveringPoints method taking an ArrayList.
	 */
	@Test
	public void testCircleCoveringPointsArrayListPoints() {
		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
		assertNull("Empty input list should return null", Planar.circleCoveringPoints(points));
		points.add(new Point2D.Double(123.4, 567.9));
		Circle c = Planar.circleCoveringPoints(points);
		assertEquals("radius of circle covering 1 point should be 0", 0, c.radius(), 0.00000001);
		assertEquals("center of circle covering 1 point should be the point", 0, c.center().distance(points.get(0)), 0.000001);
		points.clear();
		points.add(new Point2D.Double(1, 0));
		points.add(new Point2D.Double(-2, 0));
		c = Planar.circleCoveringPoints(points);
		assertEquals("radius of circle covering 2 point should be half the distance between those points", points.get(0).distance(points.get(1)) / 2, c.radius(), 0.002);
		assertEquals("center of circle coverint 2 points should be the half-way point", 0, c.center().distance(new Point2D.Double((points.get(0).x + points.get(1).x) / 2, (points.get(0).y + points.get(1).y) / 2)), 0.002);
		points.add(new Point2D.Double (-0.5, 0.5));
		c = Planar.circleCoveringPoints(points);
		assertEquals("adding a point clearly inside the circle should not alter the radius", points.get(0).distance(points.get(1)) / 2, c.radius(), 0.002);
		assertEquals("adding a point clearly inside the circle should not alter the center", 0, c.center().distance(new Point2D.Double((points.get(0).x + points.get(1).x) / 2, (points.get(0).y + points.get(1).y) / 2)), 0.000001);
		points.clear();
		// 4 points forming the corners of a square
		points.add(new Point2D.Double(0, 0));
		points.add(new Point2D.Double(2, 0));
		points.add(new Point2D.Double(2, 2));
		points.add(new Point2D.Double(0, 2));
		c = Planar.circleCoveringPoints(points);
		assertEquals("circle should be centered on the square it covers", 0, c.center().distance(new Point2D.Double(1, 1)), 0.002);
		assertEquals("radius should be half the diagonal of the square", Math.sqrt(8) / 2, c.radius(), 0.002);
		// Now run the test for previously found problems that were (supposedly) fixed
		testCircleCoveringPoints1();
		testCircleCoveringPoints2();
		testCircleCoveringPoints3();
	}
	
	/**
	 * Check that the circleCovereringPoints method behaves well. There has
	 * been a case where it failed (due to a bug) on this particular set of 
	 * points.
	 */
	public static void testCircleCoveringPoints1() {
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
	    Planar.circleCoveringPoints(points);
	}

	/**
	 * Check that the circleCoveringsPoints method behaves well. There has been
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
	    Planar.circleCoveringPoints(points);
	}

	@SuppressWarnings("static-method")
	private void testCircleCoveringPoints3() {
		// This really big case failed miserable due in stage 4 when dli and dlj got exactly the same value
		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
		points.add(new Point2D.Double(0.000000, -5.400000));
		points.add(new Point2D.Double(0.000000, -4.400000));
		points.add(new Point2D.Double(0.000000, -4.400000));
		points.add(new Point2D.Double(0.000000, -1.000000));
		points.add(new Point2D.Double(0.000000, -1.000000));
		points.add(new Point2D.Double(0.000000, 0.000000));
		points.add(new Point2D.Double(5.400000, 0.000000));
		points.add(new Point2D.Double(5.400000, -0.000000));
		points.add(new Point2D.Double(4.400000, 0.000000));
		points.add(new Point2D.Double(4.400000, -0.000000));
		points.add(new Point2D.Double(4.400000, 0.000000));
		points.add(new Point2D.Double(4.400000, -0.000000));
		points.add(new Point2D.Double(1.000000, 0.000000));
		points.add(new Point2D.Double(1.000000, -0.000000));
		points.add(new Point2D.Double(1.000000, 0.000000));
		points.add(new Point2D.Double(1.000000, -0.000000));
		points.add(new Point2D.Double(0.000000, 0.000000));
		points.add(new Point2D.Double(0.000000, -0.000000));
		points.add(new Point2D.Double(0.000000, 5.400000));
		points.add(new Point2D.Double(-0.000000, 5.400000));
		points.add(new Point2D.Double(0.000000, 4.400000));
		points.add(new Point2D.Double(-0.000000, 4.400000));
		points.add(new Point2D.Double(0.000000, 4.400000));
		points.add(new Point2D.Double(-0.000000, 4.400000));
		points.add(new Point2D.Double(0.000000, 1.000000));
		points.add(new Point2D.Double(-0.000000, 1.000000));
		points.add(new Point2D.Double(0.000000, 1.000000));
		points.add(new Point2D.Double(-0.000000, 1.000000));
		points.add(new Point2D.Double(0.000000, 0.000000));
		points.add(new Point2D.Double(-0.000000, 0.000000));
		points.add(new Point2D.Double(-5.400000, -0.000000));
		points.add(new Point2D.Double(-4.400000, -0.000000));
		points.add(new Point2D.Double(-4.400000, -0.000000));
		points.add(new Point2D.Double(-1.000000, -0.000000));
		points.add(new Point2D.Double(-1.000000, -0.000000));
		points.add(new Point2D.Double(0.000000, 0.000000));
		points.add(new Point2D.Double(0.000000, -0.000000));
		points.add(new Point2D.Double(0.000000, 0.000000));
		points.add(new Point2D.Double(0.000000, 0.000000));
		points.add(new Point2D.Double(0.000000, -0.000000));
		points.add(new Point2D.Double(0.000000, 0.000000));
		points.add(new Point2D.Double(1.000000, 0.000000));
		points.add(new Point2D.Double(1.000000, -5.400000));
		points.add(new Point2D.Double(1.000000, -4.400000));
		points.add(new Point2D.Double(1.000000, -4.400000));
		points.add(new Point2D.Double(1.000000, -1.000000));
		points.add(new Point2D.Double(1.000000, -1.000000));
		points.add(new Point2D.Double(1.000000, -0.000000));
		points.add(new Point2D.Double(1.000000, 0.000000));
		points.add(new Point2D.Double(1.000000, -5.400000));
		points.add(new Point2D.Double(1.000000, -4.400000));
		points.add(new Point2D.Double(1.000000, -4.400000));
		points.add(new Point2D.Double(1.000000, -1.000000));
		points.add(new Point2D.Double(1.000000, -1.000000));
		points.add(new Point2D.Double(1.000000, -0.000000));
		points.add(new Point2D.Double(4.400000, 0.000000));
		points.add(new Point2D.Double(4.400000, -5.400000));
		points.add(new Point2D.Double(4.400000, -4.400000));
		points.add(new Point2D.Double(4.400000, -4.400000));
		points.add(new Point2D.Double(4.400000, -1.000000));
		points.add(new Point2D.Double(4.400000, -1.000000));
		points.add(new Point2D.Double(4.400000, -0.000000));
		points.add(new Point2D.Double(4.400000, 0.000000));
		points.add(new Point2D.Double(4.400000, -5.400000));
		points.add(new Point2D.Double(4.400000, -4.400000));
		points.add(new Point2D.Double(4.400000, -4.400000));
		points.add(new Point2D.Double(4.400000, -1.000000));
		points.add(new Point2D.Double(4.400000, -1.000000));
		points.add(new Point2D.Double(4.400000, -0.000000));
		points.add(new Point2D.Double(5.400000, 0.000000));
		points.add(new Point2D.Double(5.400000, -5.400000));
		points.add(new Point2D.Double(5.400000, -4.400000));
		points.add(new Point2D.Double(5.400000, -4.400000));
		points.add(new Point2D.Double(5.400000, -1.000000));
		points.add(new Point2D.Double(5.400000, -1.000000));
		points.add(new Point2D.Double(5.400000, -0.000000));
		Planar.circleCoveringPoints(points);
	}

	@Test
	public void testCircleCoveringPointsArray () {
		//fail("Not implemented yet");
	}

	/**
	 * Test the intersectLineSegmentAndCircle method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testIntersectLineSegmentAndCircle() {
		Circle c = new Circle(new Point2D.Double(10, 20), 5);
		assertEquals("This line segment is too short to hit the circle", 0, Planar.intersectLineSegmentAndCircle(new Line2D.Double(5, 5, 10, 10), c).length);
		assertEquals("This line segment misses the circle", 0, Planar.intersectLineSegmentAndCircle(new Line2D.Double(5, 5, 100, 5), c).length);
		assertEquals("This line intersects the circle once", 1, Planar.intersectLineSegmentAndCircle(new Line2D.Double(11, 21, 100, 100), c).length);
		assertEquals("This line intersects the circle once", 1, Planar.intersectLineSegmentAndCircle(new Line2D.Double(100, 100, 11, 21), c).length);
		assertEquals("This line segment is entirely within the circle", 0, Planar.intersectLineSegmentAndCircle(new Line2D.Double(8, 21, 12, 19), c).length);
	}

	/**
	 * Test the pointsToString method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testPointsToString() {
		Main.locale = Locale.US;	// Luckily we can override this
		Point2D.Double[] array = new Point2D.Double[0];
		assertEquals("Empty array -> empty string", "", Planar.pointsToString(array));
		array = new Point2D.Double[1];
		array[0] = new Point2D.Double(123.4567, 987.654321);
		assertEquals("Check precision", "123.457,987.654", Planar.pointsToString(array));
		array = new Point2D.Double[3];
		array[0] = new Point2D.Double(1, 2);
		array[1] = new Point2D.Double(-3, -4);
		array[2] = new Point2D.Double(5, 6);
		assertEquals("Check precision", "1.000,2.000 -3.000,-4.000 5.000,6.000", Planar.pointsToString(array));
		Main.locale = Locale.GERMANY;
		assertEquals("Check precision", "1,000,2,000 -3,000,-4,000 5,000,6,000", Planar.pointsToString(array));
	}

	/**
	 * Test the verticesToString method (one argument version).
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVerticesToStringArrayListOfVertex() {
		Main.locale = Locale.US;	// Luckily we can override this
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		assertEquals("Empty list -> empty string", "", Planar.verticesToString(vertices));
		vertices.add(new Vertex(123.4567, 987.6543, 92.92999));
		assertEquals("Check precision", "(123.457m,987.654m,92.930m)", Planar.verticesToString(vertices));
		vertices.add(new Vertex(-1,-2,-3));
		assertEquals("Check precision", "(123.457m,987.654m,92.930m) (-1.000m,-2.000m,-3.000m)", Planar.verticesToString(vertices));
		Main.locale = Locale.GERMANY;
		assertEquals("Check precision", "(123,457m,987,654m,92,930m) (-1,000m,-2,000m,-3,000m)", Planar.verticesToString(vertices));
	}

	/**
	 * Test the verticesToString method (two arguments version).
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVerticesToStringArrayListOfVertexBoolean() {
		Main.locale = Locale.US;	// Luckily we can override this
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		assertEquals("Empty list -> empty string", "", Planar.verticesToString(vertices, false));
		vertices.add(new Vertex(123.4567, 987.6543, 92.92999));
		//System.out.println(Planar.verticesToString(vertices));
		assertEquals("Check precision", "(123.457m,987.654m,92.930m)", Planar.verticesToString(vertices, false));
		vertices.add(new Vertex(-1,-2,-3));
		//System.out.println(Planar.verticesToString(vertices));
		assertEquals("Check precision", "(123.457m,987.654m,92.930m) (-1.000m,-2.000m,-3.000m)", Planar.verticesToString(vertices, false));
		Main.locale = Locale.GERMANY;
		assertEquals("Check precision", "(123,457m,987,654m,92,930m) (-1,000m,-2,000m,-3,000m)", Planar.verticesToString(vertices, false));
		Main.locale = Locale.US;	// Luckily we can override this
		vertices = new ArrayList<Vertex>();
		assertEquals("Empty list -> empty string", "", Planar.verticesToString(vertices, true));
		vertices.add(new Vertex(123.4567, 987.6543, 92.92999));
		//System.out.println(Planar.verticesToString(vertices));
		assertEquals("Check precision", "(123.457m,987.654m)", Planar.verticesToString(vertices, true));
		vertices.add(new Vertex(-1,-2,-3));
		//System.out.println(Planar.verticesToString(vertices));
		assertEquals("Check precision", "(123.457m,987.654m) (-1.000m,-2.000m)", Planar.verticesToString(vertices, true));
		Main.locale = Locale.GERMANY;
		assertEquals("Check precision", "(123,457m,987,654m) (-1,000m,-2,000m)", Planar.verticesToString(vertices, true));
	}

	/**
	 * Test the line2dToString method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testLine2DToString() {
		Main.locale = Locale.US;	// Luckily we can override this
		Line2D.Double l = new Line2D.Double(123.4567, 987.6543, -3, -4);
		assertEquals("Check precision", "(123.457,987.654)->(-3.000,-4.000)", Planar.Line2DToString(l));
		Main.locale = Locale.GERMANY;
		assertEquals("Check precision", "(123,457,987,654)->(-3,000,-4,000)", Planar.Line2DToString(l));
	}

	/**
	 * Test the generalPathToString method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGeneralPathToString() {
		GeneralPath gp = new GeneralPath(Path2D.WIND_EVEN_ODD);
		//System.out.println(String.format("\"%s\"", Planar.generalPathToString(gp)));
		assertEquals("Empty in -> empty string", "", Planar.generalPathToString(gp));
		gp.moveTo(123.4567, 987.6543);
		//System.out.println(String.format("\"%s\"", Planar.generalPathToString(gp)));
		assertEquals("Single move command", "m 123.457,987.654", Planar.generalPathToString(gp));
		gp.lineTo(-1, -2);
		//System.out.println(String.format("\"%s\"", Planar.generalPathToString(gp)));
		assertEquals("Move and line command", "m 123.457,987.654 l -1.000,-2.000", Planar.generalPathToString(gp));
		gp.lineTo(500, 600);
		//System.out.println(String.format("\"%s\"", Planar.generalPathToString(gp)));
		assertEquals("Move, line and line command", "m 123.457,987.654 l -1.000,-2.000 l 500.000,600.000", Planar.generalPathToString(gp));
		// Cannot test quadTo and curveTo because the pathIterator reduces 
		// these to series of line commands in an unpredictable way
	}

	/**
	 * Test the logPoint method (partially)
	 */
	@SuppressWarnings({"static-method", "resource"})
	@Test
	public void testLogPoint() {
		for (int i = -100; i < 100; i += 25)
			for (int j = -1000; j < 1000; j += 100) {
				ByteArrayOutputStream myOut = new ByteArrayOutputStream();
				System.setOut(new PrintStream(myOut));
				Point2D.Double p = new Point2D.Double (0.3 * i, 0.3 * j);
				Point2D.Double q = Planar.log("test", p);
				String output = myOut.toString();
				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
				assertEquals("Should be same point", p, q);
				assertEquals("Output should be like this", String.format(Locale.US, "%s: (%.3f,%.3f)\r\n", "test", p.x, p.y), output);
			}
	}

	/**
	 * Test the convexHull method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testConvexHull() {
		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
		assertEquals("Empty in -> empty out", 0, Planar.convexHull(points).size());
		points.add(new Point2D.Double(10, 10));
		ArrayList<Point2D.Double> out = Planar.convexHull(points);
		assertEquals("One in -> one out", 1, out.size());
		assertEquals("One in -> same out", 0, out.get(0).distance(points.get(0)), 0.00001);
		points.add(new Point2D.Double(30, 30));
		out = Planar.convexHull(points);
		assertEquals("Two in -> two out", 2, out.size());
		assertEquals("Two in -> same out", 0, out.get(0).distance(points.get(0)), 0.00001);
		assertEquals("Two in -> same out", 0, out.get(1).distance(points.get(1)), 0.00001);
		points.add(new Point2D.Double(30, 100));
		out = Planar.convexHull(points);		
		assertEquals("Three in (non singular) -> three out", 3, out.size());
		points.add(new Point2D.Double(30, 30));
		out = Planar.convexHull(points);		
		assertEquals("Duplicate point -> same out", 3, out.size());
		points.clear();
		for (int i = 0; i < 100; i++) {
			// Insert a couple of points that lie ON a circle
			points.add(new Point2D.Double(100 + Math.sin(i), 200 + Math.cos(i)));
			out = Planar.convexHull(points);
			assertEquals("N on circle -> N out", i + 1, out.size());
		}
		for (int i = 0; i < 100; i++) {
			// insert additional points INSIDE the previous circle
			points.add(new Point2D.Double(100 + Math.sin(i + 0.5) / 2, 200 + Math.cos(i + 0.5) / 2));
			out = Planar.convexHull(points);
			assertEquals("Adding points within the circle should not increase number of points in result", 100, out.size());
		}
		// inserting a point far outside should reduce the number of points in the result
		Point2D.Double p = new Point2D.Double(100, 1000);
		points.add(p);
		out = Planar.convexHull(points);
		//System.out.println("number is now " + out.size());
		assertTrue("Adding a point far outside the circle reduces the number of points in the convex hull", out.size() < 100);
		assertTrue("Number of points should be at least 50", out.size() >= 50);
		// the new point should be part of the new convex hull
		int found = 0;
		for (int i = 0; i < out.size(); i++)
			if (out.get(i).distance(p) <= 0.000000001)
				found++;
		assertEquals("Should find the extreme point once", 1, found);
	}

	/**
	 * Test the lineIntersectsPolygon method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testLineIntersectsPolygon() {
		Point2D.Double[] polygon = new Point2D.Double[0];
		Line2D.Double l = new Line2D.Double(10, 10, 20, 20);
		ArrayList<Point2D.Double> out = Planar.lineIntersectsPolygon(l, polygon);
		assertEquals("Too short in -> empty out", 0, out.size());
		polygon = new Point2D.Double[1];
		out = Planar.lineIntersectsPolygon(l, polygon);
		assertEquals("Too short in -> empty out", 0, out.size());
		polygon = new Point2D.Double[2];
		polygon[0] = new Point2D.Double(5, 7);
		polygon[1] = new Point2D.Double(15, 5);
		out = Planar.lineIntersectsPolygon(l, polygon);
		assertEquals("Miss", 0, out.size());
		polygon[0] = new Point2D.Double(5, 15);
		out = Planar.lineIntersectsPolygon(l, polygon);
		assertEquals("Hit", 2, out.size());	// The polyline is implicitly closed; therefore 2 hits
		assertEquals("Expected location", 0, out.get(0).distance(new Point2D.Double(10, 10)), 0.000001);
		assertEquals("Expected location", 0, out.get(1).distance(new Point2D.Double(10, 10)), 0.000001);
		polygon = new Point2D.Double[3];
		polygon[0] = new Point2D.Double(10, 0);
		polygon[1] = new Point2D.Double(20, 0);
		polygon[2] = new Point2D.Double(10, 10);
		l = new Line2D.Double(15, 1, 16, 2);
		out = Planar.lineIntersectsPolygon(l, polygon);
		assertEquals("Line is totally within the polygon", 0, out.size());
		l = new Line2D.Double(15, -1, 15, 2);
		out = Planar.lineIntersectsPolygon(l, polygon);
		assertEquals("One hit", 1, out.size());
		assertEquals("Expected location of hit", 0, out.get(0).distance(new Point2D.Double(15, 0)), 0.000001);
		l = new Line2D.Double(15, -1, 15, 200);
		out = Planar.lineIntersectsPolygon(l, polygon);
		assertEquals("Two hits", 2, out.size());
		// Order of returned hits is not really guaranteed...
		assertEquals("First hit", 0, out.get(0).distance(new Point2D.Double(15, 0)), 0.000001);
		assertEquals("Second hit", 0, out.get(1).distance(new Point2D.Double(15, 5)), 0.000001);
	}

	/**
	 * Test the slicePolyline method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSlicePolyline() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		ArrayList<Vertex> out = Planar.slicePolyline(vertices, 5, 10);
		assertEquals("Empty in -> empty out", 0, out.size());
		vertices.add(new Vertex(10, 20, 30));
		out = Planar.slicePolyline(vertices, 5, 10);
		assertEquals("Degenerate in -> empty out", 0, out.size());
		vertices.add(new Vertex(110, 20, 30));
		//System.out.println("in :" + Planar.verticesToString(vertices));
		out = Planar.slicePolyline(vertices, 5, 10);
		assertEquals("OK in -> two out", 2, out.size());
		//System.out.println("out:" + Planar.verticesToString(out));
		assertEquals("Expected first point", 0, out.get(0).distance(new Vertex(15, 20, 30)), 0.000001);
		assertEquals("Expected second point", 0, out.get(1).distance(new Vertex(25, 20, 30)), 0.000001);
		out = Planar.slicePolyline(vertices, 5,  1000);
		//System.out.println("out:" + Planar.verticesToString(out));
		assertEquals("OK in -> two out", 2, out.size());
		//System.out.println("out:" + Planar.verticesToString(out));
		assertEquals("Expected first point", 0, out.get(0).distance(new Vertex(15, 20, 30)), 0.000001);
		assertEquals("Expected second point", 0, out.get(1).distance(new Vertex(110, 20, 30)), 0.000001);
		vertices.add(new Vertex(110, 120, 30));
		//System.out.println("in :" + Planar.verticesToString(vertices));
		out = Planar.slicePolyline(vertices, 5,  1000);
		//System.out.println("out:" + Planar.verticesToString(out));
		assertEquals("OK in -> three out", 3, out.size());
		assertEquals("Expected first point", 0, out.get(0).distance(new Vertex(15, 20, 30)), 0.000001);
		assertEquals("Expected second point", 0, out.get(1).distance(new Vertex(110, 20, 30)), 0.000001);
		assertEquals("Expected third point", 0, out.get(2).distance(new Vertex(110, 120, 30)), 0.000001);
		out = Planar.slicePolyline(vertices,  5,  150);
		//System.out.println("out:" + Planar.verticesToString(out));
		assertEquals("OK in -> three out", 3, out.size());
		assertEquals("Expected first point", 0, out.get(0).distance(new Vertex(15, 20, 30)), 0.000001);
		assertEquals("Expected second point", 0, out.get(1).distance(new Vertex(110, 20, 30)), 0.000001);
		assertEquals("Expected third point", 0, out.get(2).distance(new Vertex(110, 75, 30)), 0.000001);
		out = Planar.slicePolyline(vertices,  -5,  150);
		//System.out.println("out:" + Planar.verticesToString(out));
		assertEquals("OK in -> two out", 2, out.size());
		assertEquals("Expected first point", 0, out.get(0).distance(new Vertex(110, 115, 30)), 0.000001);
		assertEquals("Expected second point", 0, out.get(1).distance(new Vertex(110, 120, 30)), 0.000001);
		vertices.add(new Vertex(110, 120, 130));
		//System.out.println("in :" + Planar.verticesToString(vertices));
		out = Planar.slicePolyline(vertices, 5,  1000);
		//System.out.println("out:" + Planar.verticesToString(out));
		assertEquals("OK in -> four out", 4, out.size());
		assertEquals("Expected first point", 0, out.get(0).distance(new Vertex(15, 20, 30)), 0.000001);
		assertEquals("Expected second point", 0, out.get(1).distance(new Vertex(110, 20, 30)), 0.000001);
		assertEquals("Expected third point", 0, out.get(2).distance(new Vertex(110, 120, 30)), 0.000001);
		assertEquals("Expected fourth point", 0, out.get(3).distance(new Vertex(110, 120, 130)), 0.000001);
	}

	/**
	 * Test the createPartlyParallelVertices method.
	 */
	@Test
	public void testCreatePartlyParallelVertices() {
		// FIXME fail("Not yet implemented");
	}

	/**
	 * Test the createParallelVertices method taking a prevReferenceVertices argument.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testCreateParallelVerticesArrayListOfVertexArrayListOfVertexDouble() {
		ArrayList<Vertex> prev = new ArrayList<Vertex>();
		ArrayList<Vertex> next = new ArrayList<Vertex>();
		boolean errorThrown = false;
		try {
			Planar.createParallelVertices(null, null, 3);
		} catch (Error e) {
			 errorThrown = true;
		}
		assertTrue("Should have thrown an error", errorThrown);
		errorThrown = false;
		try {
			Planar.createParallelVertices(next, null, 3);
		} catch (Error e) {
			 errorThrown = true;
		}
		assertTrue("Should have thrown an error", errorThrown);
		next.add(new Vertex(10, 20, 30));
		errorThrown = false;
		try {
			Planar.createParallelVertices(next, null, 3);
		} catch (Error e) {
			 errorThrown = true;
		}
		assertTrue("Should have thrown an error", errorThrown);
		next.add(new Vertex(10, 30, 40));
		ArrayList<Vertex> result = Planar.createParallelVertices(next, prev, 3);
		//System.out.println("result: " + Planar.verticesToString(result));
		assertEquals("Expect two vertices", 2, result.size());
		assertEquals("Expected location for first point", 0, result.get(0).distance(new Vertex(13, 20, 30)), 0.000001);
		assertEquals("Expected location for second point", 0, result.get(1).distance(new Vertex(13, 30, 40)), 0.000001);
		prev.add(new Vertex(0, 18, 0));
		prev.add(new Vertex(10, 18, 0));
		//System.out.println("prev: " + Planar.verticesToString(prev));
		//System.out.println("next: " + Planar.verticesToString(next));
		result = Planar.createParallelVertices(next, prev, 3);
		//System.out.println("result: " + Planar.verticesToString(result));
		assertEquals("Expect two vertices", 2, result.size());
		assertEquals("Expected location for first point", 0, result.get(0).distance(new Vertex(13, 18, 30)), 0.000001);
		assertEquals("Expected location for second point", 0, result.get(1).distance(new Vertex(13, 30, 40)), 0.000001);
	}

	/**
	 * Test the createParallelVertices method that takes four arguments.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testCreateParallelVerticesArrayListOfVertexArrayListOfVertexDoubleDouble() {
		ArrayList<Vertex> ref = new ArrayList<Vertex>();
		boolean errorThrown = false;
		try {
			Planar.createParallelVertices(ref, null, 5, 7);
		} catch (Error e) {
			errorThrown = true;
		}
		assertTrue("Should have thrown an error", errorThrown);
		ref.add(new Vertex(5, 0, 0));
		errorThrown = false;
		try {
			Planar.createParallelVertices(ref, null, 5, 7);
		} catch (Error e) {
			errorThrown = true;
		}
		assertTrue("Should have thrown an error", errorThrown);
		ref.add(new Vertex(5, 10, 0));
		ArrayList<Vertex> result = Planar.createParallelVertices(ref, null, 5, 7);
		assertEquals("Should contain two vertices", 2, result.size());
		assertEquals("First point should be here", 0, result.get(0).distance(new Vertex(10, 0, 0)), 0.0000001);
		assertEquals("Second point should be here", 0, result.get(1).distance(new Vertex(12, 10, 0)), 0.0000001);
	}

	/**
	 * Test the simple createParallelVertices method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testCreateParallelVerticesArrayListOfVertexDouble() {
		ArrayList<Vertex> in = new ArrayList<Vertex>();
		boolean errorThrown = false;
		try {
			Planar.createParallelVertices(in, 5);
		} catch (Error e) {
			errorThrown = true;
		}
		assertTrue("Should have thrown an Error", errorThrown);
		in.add(new Vertex(10, 20, 30));
		errorThrown = false;
		try {
			Planar.createParallelVertices(in, 5);
		} catch (Error e) {
			errorThrown = true;
		}
		assertTrue("Should have thrown an Error", errorThrown);
		in.add(new Vertex(10, 20, 100));
		/* Currently no error is thrown for two vertices with identical X and Y
		errorThrown = false;
		try {
			Planar.createParallelVertices(in, 5);
		} catch (Error e) {
			errorThrown = true;
		}
		assertTrue("Should have thrown an Error", errorThrown);
		*/
		in.get(1).setX(15);
		// we now have a line segment from (10, 20, 30) to (15, 20, 100)
		ArrayList<Vertex> out = Planar.createParallelVertices(in, 3);
		assertEquals("Should have same number of vertices", in.size(), out.size());
		for (int i = in.size(); --i >= 0; ) {
			assertEquals("Should have same Z-coordinate", in.get(i).getZ(), out.get(i).getZ(), 0.000001);
			assertEquals("Line parallel to X should have same X-coordinates", in.get(i).getX(), out.get(i).getX(), 0.000001);
			assertEquals("Line parallel to X should have Y-coordinates offset by supplied offset", in.get(i).getY() - 3, out.get(i).getY(), 0.000001);
		}
		in.set(1, new Vertex(10, 50, 99));
		// we now have a line segment from (10, 20, 30) to (10, 50, 99)
		out = Planar.createParallelVertices(in, 333);
		assertEquals("Should have same number of vertices", in.size(), out.size());
		for (int i = in.size(); --i >= 0; ) {
			assertEquals("Should have same Z-coordinate", in.get(i).getZ(), out.get(i).getZ(), 0.000001);
			assertEquals("Line parallel to Y should have Y-coordinates offset by supplied offset", in.get(i).getX() + 333, out.get(i).getX(), 0.000001);
			assertEquals("Line parallel to Y should have same X-coordinates", in.get(i).getY(), out.get(i).getY(), 0.000001);
		}
		in.add(new Vertex (70, 50, -12));
		// we now have a polyline from (10, 20, 30) via (10, 50, 99) to (70, 50, -12)
		out = Planar.createParallelVertices(in, -7);
		assertEquals("Should have same number of vertices", in.size(), out.size());
		// The next test found a bug in createParallelVertices (Z was derived from the wrong input Vertex)
		for (int i = in.size(); --i >= 0; )
			assertEquals("Should have same Z-coordinate", in.get(i).getZ(), out.get(i).getZ(), 0.000001);
		assertEquals("First point should be shifted along X", 0, out.get(0).getPoint().distance(new Point2D.Double(3, 20)), 0.00001);
		assertEquals("Third point should be shifted along Y", 0, out.get(2).getPoint().distance(new Point2D.Double(70, 57)), 0.00001);
		assertEquals("Second point should be shifted diagonally", 0, out.get(1).getPoint().distance(new Point2D.Double(3, 57)), 0.00001);
		in.add(new Vertex(100, 50, 3));
		// we now have a polyline from (10, 20, 30) via (10, 50, 99) via (70, 50, -12) to (100, 50, 3)
		out = Planar.createParallelVertices(in, -4);
		assertEquals("Should have same number of vertices", in.size(), out.size());
		for (int i = in.size(); --i >= 0; )
			assertEquals("Should have same Z-coordinate", in.get(i).getZ(), out.get(i).getZ(), 0.000001);
		assertEquals("First point should be shifted along X", 0, out.get(0).getPoint().distance(new Point2D.Double(6, 20)), 0.00001);
		assertEquals("Fourth point should be shifted along Y", 0, out.get(3).getPoint().distance(new Point2D.Double(100, 54)), 0.00001);
		assertEquals("Second point should be shifted diagonally", 0, out.get(1).getPoint().distance(new Point2D.Double(6, 54)), 0.00001);
		assertEquals("Third point should be shifted laterally along Y", 0, out.get(2).getPoint().distance(new Point2D.Double(70, 54)), 0.00001);
		in = new ArrayList<Vertex>();
		in.add(new Vertex(0, 0, 0));
		in.add(new Vertex(10, 0, 0));
		in.add(new Vertex(0, 10, 0));
		out = Planar.createParallelVertices(in, 2);
		assertEquals("First point should be shifted laterally, right, Y", 0, out.get(0).getPoint().distance(new Point2D.Double(0, -2)), 0.00001);
		assertEquals("Second point should be shifted East-South-East", 0, out.get(1).getPoint().distance(new Point2D.Double(10 + 2 / Math.tan(Math.PI / 8), -2)), 0.00001);
		assertEquals("Third pount should be shifted North-East", 0, out.get(2).getPoint().distance(new Point2D.Double(Math.sqrt(2), 10 + Math.sqrt(2))), 0.00001);
	}

	/**
	 * Test the closePolyline method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testClosePolyline() {
		Point2D.Double[] in = new Point2D.Double[1];
		Point2D.Double[] out = Planar.closePolyline(in);
		assertTrue("Should return in", in == out);
		in = new Point2D.Double[2];
		in[0] = new Point2D.Double(10, 20);
		in[1] = new Point2D.Double(30, 40);
		out = Planar.closePolyline(in);
		assertEquals("Should add one point", 3, out.length);
		assertTrue("Should be same point", in[0] == out[0]);
		assertTrue("Should be same point", in[1] == out[1]);
	}

	/**
	 * Test the getALignment method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetAlignment() {
		ArrayList<Vertex> in = new ArrayList<Vertex>();
		for (int i = 0; i < 100; i++) {
			Point2D.Double[] out = Planar.getAlignment(in);
			assertEquals("Sizes should be same", in.size(), out.length);
			for (int k = 0; k < out.length; k++) {
				assertEquals("X should be equal", in.get(k).getX(), out[k].x, 0.0000001);
				assertEquals("Y should be equal", in.get(k).getY(), out[k].y, 0.0000001);
			}
			in.add(new Vertex(Math.sin(i), Math.sin(i * 0.7 + 0.5), Math.sin(i * 1.8)));
		}
	}

	/**
	 * Test the ArrayListOfPointsToArray method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testArrayListOfPointsToArray() {
		ArrayList<Point2D.Double> in = new ArrayList<Point2D.Double>();
		assertEquals("Empty in results in empty out", 0, Planar.ArrayListOfPointsToArray(in).length);
		for (int i = 0; i < 100; i++) {
			in.add(new Point2D.Double(i + 10, i + 25));
			Point2D.Double[] out = Planar.ArrayListOfPointsToArray(in);
			assertEquals("Length of result is size of input", in.size(), out.length);
			for (int k = 0; k < out.length; k++) {
				assertEquals("Point should be same location", 0, in.get(k).distance(out[k]), 0.000001);
				// out should contain the point of in
				double save = out[k].x;
				out[k].x = -1;
				assertEquals("Should be the same instance of a Point2D.Double", 0, in.get(k).distance(out[k]), 0.000001);
				out[k].x = save;
			}
		}
	}
	
	/**
	 * Test the areaOfSimplePolygon method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testAreaOfSimplePolygon() {
		Point2D.Double[] points = new Point2D.Double[0];
		assertEquals("Empty polygon has 0 area", 0, Planar.areaOfSimplePolygon(points), 0);
		points = new Point2D.Double[1];
		points[0] = new Point2D.Double(123.456, 567.890);
		assertEquals("Polygon of one point has 0 area", 0, Planar.areaOfSimplePolygon(points), 0);
		points = new Point2D.Double[2];
		points[0] = new Point2D.Double(123.456, 567.890);
		points[1] = new Point2D.Double(321.098, 765.432);
		assertEquals("Polygon of two points has 0 area", 0, Planar.areaOfSimplePolygon(points), 0.00001);
		points = new Point2D.Double[3];
		points[0] = new Point2D.Double(10, 0);
		points[1] = new Point2D.Double(20, 0);
		points[2] = new Point2D.Double(20, 20);
		assertEquals("Triangle should have area of base times height / 2", 100, Planar.areaOfSimplePolygon(points), 0.00001);
		points[2] = new Point2D.Double(20, -20);
		assertEquals("Triangle with points in wrong order should have negative area of base times height / 2", -100, Planar.areaOfSimplePolygon(points), 0.00001);
		points = new Point2D.Double[6];
		points[0] = new Point2D.Double(10, 0);
		points[1] = new Point2D.Double(20, 0);
		points[2] = new Point2D.Double(20, 10);
		points[3] = new Point2D.Double(18, 10);
		points[4] = new Point2D.Double(0, 10);
		points[5] = new Point2D.Double(10, 10);
		assertEquals("Degenerate square should have expected area", 100, Planar.areaOfSimplePolygon(points), 0.00001);
	}

	/**
	 * Test the expandBoundingBox method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testExpandBoundingBox() {
		Line2D.Double bbox = Planar.expandBoundingBox(null, 10, 20);
		assertEquals("Bounding box of only one point equals that point", 0, bbox.getP1().distance(new Point2D.Double(10, 20)), 0.0000001);
		assertEquals("Bounding box of only one point equals that point", 0, bbox.getP2().distance(new Point2D.Double(10, 20)), 0.0000001);
		bbox = Planar.expandBoundingBox(bbox, 30, 5);	// expand it a bit
		assertEquals("Bounding box of two points", 0, bbox.getP1().distance(new Point2D.Double(10, 5)), 0.0000001);
		assertEquals("Bounding box of two points", 0, bbox.getP2().distance(new Point2D.Double(30, 20)), 0.0000001);
		bbox = Planar.expandBoundingBox(bbox, 12, 8);	// should not expand the bounding box
		assertEquals("Bounding box of two points", 0, bbox.getP1().distance(new Point2D.Double(10, 5)), 0.0000001);
		assertEquals("Bounding box of two points", 0, bbox.getP2().distance(new Point2D.Double(30, 20)), 0.0000001);
	}
	
	/**
	 * Test the normalizeAngle method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testNormalizeAngle() {
		for (int i = -100; i < 100; i++) {
			double in = i;
			double out = Planar.normalizeAngle(in);
			assertEquals("sine shoud be equal", Math.sin(in), Math.sin(out), 0.0000001);
			assertEquals("cosine shoud be equal", Math.cos(in), Math.cos(out), 0.0000001);
		}
	}
	
	/**
	 * Test the createSmoothCurve method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testCreateSmoothCurve() {
		//Main.locale = Locale.US;
		ArrayList<Point2D.Double> from = new ArrayList<Point2D.Double>();
		from.add(new Point2D.Double(10, 10));
		ArrayList<Point2D.Double> to = new ArrayList<Point2D.Double>();
		to.add(new Point2D.Double(10, 10));
		ArrayList<Point2D.Double> result = Planar.createSmoothCurve(from, to, 0.1);
		assertEquals("From ends at to; no connecting line needed", 2, result.size());
		assertEquals("Start at end of from", 0, from.get(0).distance(result.get(0)), 0.000001);
		assertEquals("End at start of to", 0, to.get(0).distance(result.get(1)), 0.000001);
		to.clear();
		to.add(new Point2D.Double(15, 15));
		result = Planar.createSmoothCurve(from, to, 0.1);
		assertEquals("Degenerate from and to are connected without extra points", 2, result.size());
		assertEquals("Start at end of from", 0, from.get(0).distance(result.get(0)), 0.000001);
		assertEquals("End at start of to", 0, to.get(0).distance(result.get(1)), 0.000001);
		from.add(0, new Point2D.Double(0, 10));
		result = Planar.createSmoothCurve(from, to, 0.1);
		//String description = Planar.pointsToString(Planar.ArrayListOfPointsToArray(result));
		//System.out.println("degenerate to: curve contains " + description);
		// These points should be an approximation of an arc to within about 0.33
		double prevAngle = Double.NaN;
		Point2D.Double center = new Point2D.Double(10, 15);
		for (Point2D.Double p : result) {
			double angle = Math.atan2(p.y - center.y, p.x - center.x);
			if (! Double.isNaN(prevAngle))
				assertTrue("Should be increasing angle", angle > prevAngle);
			double radius = p.distance(center);
			assertEquals("Radius should be about 5", 6, radius, 1);
			prevAngle = angle;
		}
		to.add(new Point2D.Double(15, 100));
		// Result should be EXACTLY the same
		ArrayList<Point2D.Double> result2 = Planar.createSmoothCurve(from, to, 0.1);
		//description = Planar.pointsToString(Planar.ArrayListOfPointsToArray(result2));
		//System.out.println("non-degenerate to: curve contains " + description);
		assertEquals("Result should be exactly the same", result.size(), result2.size());
		for (int i = 0; i < result.size(); i++)
			assertEquals("Point should be the same", 0, result.get(i).distance(result2.get(i)), 0.000001);
		result = Planar.createSmoothCurve(from, to, 4);
		assertEquals("Result should be a straight line", 2, result.size());
		assertEquals("First point should be end of from", 0, from.get(1).distance(result.get(0)), 0.000001);
		assertEquals("Second point should be end of from", 0, to.get(0).distance(result.get(1)), 0.000001);
		to.remove(1);
		// See if it can create an S-curve
		to.add(new Point2D.Double(20, 15));
		result = Planar.createSmoothCurve(from, to, 0.1);
		//description = Planar.pointsToString(Planar.ArrayListOfPointsToArray(result));
		//System.out.println("S curve contains " + description);
		Point2D.Double prevPoint = null;
		for (Point2D.Double p : result) {
			if (null != prevPoint) {
				double angle = Math.atan2 (p.y - prevPoint.y, p.x - prevPoint.x);
				assertTrue("Should be a smooth slope", (angle > 0) && (angle < Math.PI / 2));
			}
			prevPoint = p;
		}
		to.clear();
		to.add(new Point2D.Double(10, 15));
		to.add(new Point2D.Double(5, 15));
		result = Planar.createSmoothCurve(from, to, 0.1);
		//description = Planar.pointsToString(Planar.ArrayListOfPointsToArray(result));
		//System.out.println("u curve contains " + description);
		to.clear();
		to.add(new Point2D.Double(10, 15));
		to.add(new Point2D.Double(10, 25));
		result = Planar.createSmoothCurve(from, to, 0.1);
		//description = Planar.pointsToString(Planar.ArrayListOfPointsToArray(result));
		//System.out.println("Moderate ? curve contains " + description);
		to.clear();
		to.add(new Point2D.Double(5, 15));
		to.add(new Point2D.Double(5, 25));
		result = Planar.createSmoothCurve(from, to, 0.1);
		//description = Planar.pointsToString(Planar.ArrayListOfPointsToArray(result));
		//System.out.println("Hard ? curve contains " + description);
	}

}
