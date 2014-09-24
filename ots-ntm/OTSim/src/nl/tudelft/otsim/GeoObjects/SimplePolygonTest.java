package nl.tudelft.otsim.GeoObjects;

import static org.junit.Assert.*;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;

import org.junit.Test;

/**
 * Test the SimplePolygon class.
 * 
 * @author Peter Knoppers
 */
public class SimplePolygonTest {

	/**
	 * Test creator with no arguments.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSimplePolygon() {
		SimplePolygon sp = new SimplePolygon();
		assertEquals("SimplePolygon from nothing has zero vertices", 0, sp.size());
		assertTrue("SimplePolygon created from nothing has null name", null == sp.getName_r());
	}

	/**
	 * Test creator from ArrayList<Point2D.Double> and z;
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSimplePolygonArrayListOfDoubleDouble() {
		ArrayList<Point2D.Double> pointList = new ArrayList<Point2D.Double>();
		SimplePolygon sp = new SimplePolygon(pointList, 123.456);
		assertEquals("SimplePolygon from empty list has zero vertices", 0, sp.size());
		assertTrue("SimplePolygon created from empty list has null name", null == sp.getName_r());
		pointList.add(new Point2D.Double(12.3456, 789.012));
		sp = new SimplePolygon(pointList, 123.456);
		assertEquals("SimplePolygon from list of one point has one vertex", 1, sp.size());
		assertTrue("SimplePolygon created from list has null name", null == sp.getName_r());
		assertEquals("Check the vertex", 0, sp.getVertex(0).distance(new Vertex(12.3456, 789.012, 123.456)), 0.00001);
		pointList.add(new Point2D.Double(1, 2));
		sp = new SimplePolygon(pointList, 123.456);
		assertEquals("SimplePolygon from list of two points has two vertices", 2, sp.size());
		assertTrue("SimplePolygon created from list has null name", null == sp.getName_r());
		assertEquals("Check the first vertex", 0, sp.getVertex(0).distance(new Vertex(12.3456, 789.012, 123.456)), 0.00001);
		assertEquals("Check the second vertex", 0, sp.getVertex(1).distance(new Vertex(1, 2, 123.456)), 0.00001);
	}

	/**
	 * Test the creator that takes an ArrayList<Vertex>
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSimplePolygonArrayListOfVertex() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		SimplePolygon sp = new SimplePolygon(vertices);
		assertEquals("SimplePolygon from empty list has zero vertices", 0, sp.size());
		assertTrue("SimplePolygon created from empty list has null name", null == sp.getName_r());
		vertices.add(new Vertex(123.456, 789.012, 34.56));
		sp = new SimplePolygon(vertices);
		assertEquals("SimplePolygon from list of one point has one vertex", 1, sp.size());
		assertTrue("SimplePolygon created from list has null name", null == sp.getName_r());
		assertEquals("Check the vertex", 0, sp.getVertex(0).distance(new Vertex(123.456, 789.012, 34.56)), 0.00001);
		vertices.add(new Vertex(1, 2, 3));
		sp = new SimplePolygon(vertices);
		assertEquals("SimplePolygon from list of two points has two vertices", 2, sp.size());
		assertTrue("SimplePolygon created from list has null name", null == sp.getName_r());
		assertEquals("Check the first vertex", 0, sp.getVertex(0).distance(new Vertex(123.456, 789.012, 34.56)), 0.00001);
		assertEquals("Check the second vertex", 0, sp.getVertex(1).distance(new Vertex(1, 2, 3)), 0.00001);
	}

	/**
	 * Test the creator that takes a {@link ParsedNode} as argument.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSimplePolygonParsedNode() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		vertices.add(new Vertex(12.3456, 23.4567, 34.5678));
		vertices.add(new Vertex(120.3456, 230.4567, 340.5678));
		vertices.add(new Vertex(120.3456, 230.4567, 340.5678));
		SimplePolygon sp = new SimplePolygon(vertices);
		sp.setName_w("Hello");
		String xmlText = null;
		try {
			xmlText = StaXWriter.XMLString(sp);
		} catch (Exception e) {
			fail("Caught unexpected exception in creation of the XML text");
		}
		// System.out.println(xmlText);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlText.getBytes());
		SimplePolygon sp2 = null;
		try {
			ParsedNode pn = new ParsedNode(inputStream);
			// System.out.println(pn.toString("All XML "));
			ParsedNode spNode = pn.getSubNode(SimplePolygon.XMLTAG, 0);
			sp2 = new SimplePolygon(spNode);
		} catch (Exception e) {
			fail("Parsing the XML should not throw any Exception");
		}
		assertEquals("Re-created SimplePolygon should have same name", sp.getName_r(), sp2.getName_r());
		assertEquals("Re-created SimplePolygon should have same number of vertices", sp.size(), sp2.size());
		for (int i = 0; i < sp.size(); i++)
			assertEquals("Re-created vertices should be at same location", 0, sp.getVertex(i).distance(sp2.getVertex(i)), 0.000001);
	}

	/**
	 * Test the writeXML method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testWriteXML() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		SimplePolygon sp = new SimplePolygon(vertices);
		sp.setName_w("Hello");
		String xmlText = null;
		try {
			xmlText = StaXWriter.XMLString(sp);
			fail("Did not catch expected exception in creation of the XML text");
		} catch (Error e) {
		} catch (Exception e) {
			fail("Caught unexpected exception in creation of the XML text");
		}
		vertices.add(new Vertex(12.3456, 23.4567, 34.5678));
		sp = new SimplePolygon(vertices);
		sp.setName_w("Hello");
		try {
			xmlText = StaXWriter.XMLString(sp);
			fail("Did not catch expected exception in creation of the XML text");
		} catch (Error e) {
		} catch (Exception e) {
			fail("Caught unexpected exception in creation of the XML text");
		}
		vertices.add(new Vertex(120.3456, 230.4567, 340.5678));
		sp = new SimplePolygon(vertices);
		sp.setName_w("Hello");
		try {
			xmlText = StaXWriter.XMLString(sp);
			fail("Did not catch expected exception in creation of the XML text");
		} catch (Error e) {
		} catch (Exception e) {
			fail("Caught unexpected exception in creation of the XML text");
		}
		vertices.add(new Vertex(120.3456, 230.4567, 340.5678));
		sp = new SimplePolygon(vertices);
		try {
			xmlText = StaXWriter.XMLString(sp);
			fail("Did not catch expected error in creation of the XML text");
		} catch (Error e) {
		} catch (Exception e) {
		}
		sp.setName_w("Hello");
		try {
			xmlText = StaXWriter.XMLString(sp);
		} catch (Exception e) {
			fail("Caught unexpected exception in creation of the XML text");
		}
		// System.out.println(xmlText);
		assertEquals("Expected XML", "<?xml version=\"1.0\"?>\n" + "<polygon>\n" + "  <name>Hello</name>\n"
				+ "  <vertex rank=\"0\">\n" + "    <X>12.3456</X>\n" + "    <Y>23.4567</Y>\n" + "    <Z>34.5678</Z>\n"
				+ "  </vertex>\n" + "  <vertex rank=\"1\">\n" + "    <X>120.3456</X>\n" + "    <Y>230.4567</Y>\n"
				+ "    <Z>340.5678</Z>\n" + "  </vertex>\n" + "  <vertex rank=\"2\">\n" + "    <X>120.3456</X>\n"
				+ "    <Y>230.4567</Y>\n" + "    <Z>340.5678</Z>\n" + "  </vertex>\n" + "</polygon>\n", xmlText);
	}

	/**
	 * Test the getProjection method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetProjection() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		SimplePolygon sp = new SimplePolygon(vertices);
		Point2D.Double[] points = sp.getProjection();
		assertEquals("empty sp has empty projection", 0, points.length);
		vertices.add(new Vertex(1, 2, 3));
		sp = new SimplePolygon(vertices);
		points = sp.getProjection();
		assertEquals("sp with one vertex has projection with one point", 1, points.length);
		assertEquals("Point should be projection of the only vertex", 0, points[0].distance(vertices.get(0).getPoint()), 0.000001);
		vertices.add(new Vertex(4, 5, 6));
		sp = new SimplePolygon(vertices);
		points = sp.getProjection();
		assertEquals("sp with two vertices has projection with two points", 2, points.length);
		assertEquals("First point should be projection of the first vertex", 0, points[0].distance(vertices.get(0).getPoint()),
				0.000001);
		assertEquals("Second point should be projection of the second vertex", 0, points[1].distance(vertices.get(1).getPoint()),
				0.000001);
	}

	/**
	 * Test the getGeneralPath method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetGeneralPath() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		vertices.add(new Vertex(1, 2, 3));
		SimplePolygon sp = new SimplePolygon(vertices);
		GeneralPath gp = sp.getGeneralPath();
		Rectangle2D r = gp.getBounds();
		assertEquals("should have zero size", 0, r.getWidth(), 0);
		assertEquals("should have zero size", 0, r.getHeight(), 0);
		assertEquals("should be at our vertex", 1, r.getX(), 0);
		assertEquals("should be at our vertex", 2, r.getY(), 0);
		vertices.add(new Vertex(10, 20, 30));
		sp = new SimplePolygon(vertices);
		gp = sp.getGeneralPath();
		r = gp.getBounds();
		assertEquals("Expected width", 9, r.getWidth(), 0);
		assertEquals("Expected height", 18, r.getHeight(), 0);
		assertEquals("should be at our start vertex", 1, r.getX(), 0);
		assertEquals("should be at our start vertex", 2, r.getY(), 0);
	}

	/**
	 * Test the surfaceArea method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSurfaceArea() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		SimplePolygon sp = new SimplePolygon(vertices);
		assertEquals("Empty vertices should give 0 surface area", 0, sp.surfaceArea(), 0);
		vertices.add(new Vertex(1, 2, 3));
		sp = new SimplePolygon(vertices);
		assertEquals("One vertices should give 0 surface area", 0, sp.surfaceArea(), 0);
		vertices.add(new Vertex(4, 2, 5));
		sp = new SimplePolygon(vertices);
		assertEquals("Two vertices should give 0 surface area", 0, sp.surfaceArea(), 0);
		vertices.add(new Vertex(4, 12, 15));
		sp = new SimplePolygon(vertices);
		assertEquals("Three vertices should give real surface area", 15, sp.surfaceArea(), 0.000001);
		vertices.clear();
		final int steps = 100;
		for (int i = 0; i < steps; i++)
			vertices.add(new Vertex(10 * Math.cos(Math.PI * 2 * i / steps) - 123, 10 * Math.sin(Math.PI * 2 * i / steps) + 456, i));
		sp = new SimplePolygon(vertices);
		assertEquals("Surface of approximated circle", Math.PI * 100, sp.surfaceArea(), 0.3);
	}

	/**
	 * Test the mayUpdateVertex method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testMayUpdateVertex() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		vertices.add(new Vertex(10, 10, 10));
		vertices.add(new Vertex(20, 10, 11));
		vertices.add(new Vertex(20, 20, 12));
		vertices.add(new Vertex(10, 20, 13));
		SimplePolygon sp = new SimplePolygon(vertices);
		// Try many combinations of x and y as replacement for the Vertex at 20, 20, 12
		for (int i = -10; i <= 40; i++)
			for (int j = -10; j <= 40; j++) {
				double x = i + 0.1;
				double y = j + 0.2;
				Vertex proposed = new Vertex(x, y, 123);
				boolean expect = ((x > 10) && (y > 10)) || (x + y > 30);
				// System.out.println(String.format("x=%.2f, y=%.2f, expect=%s", x, y, expect ? "true" : " false"));
				if (expect)
					assertTrue("Should be good", sp.mayUpdateVertex(2, proposed));
				else
					assertFalse("Should be bad", sp.mayUpdateVertex(2, proposed));
			}
	}

	/**
	 * Test the mayAddVertex method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testMayAddVertex() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		vertices.add(new Vertex(10, 10, 10));
		vertices.add(new Vertex(20, 10, 11));
		vertices.add(new Vertex(20, 20, 12));
		vertices.add(new Vertex(10, 20, 13));
		SimplePolygon sp = new SimplePolygon(vertices);
		// Try many combinations of x and y as additional Vertex
		for (int i = -10; i <= 40; i++)
			for (int j = -10; j <= 40; j++) {
				double x = i + 0.1;
				double y = j + 0.2;
				Vertex proposed = new Vertex(x, y, 123);
				boolean expect = (x < 10) || ((x > 10) && (x < 20) && (y > 10) && (y < 20));
				// System.out.println(String.format("x=%.2f, y=%.2f, expect=%s", x, y, expect ? "true" : " false"));
				if (expect)
					assertTrue("Should be good", sp.mayAddVertex(proposed));
				else
					assertFalse("Should be bad", sp.mayAddVertex(proposed));
			}
	}

	/**
	 * Test the mayDeleteVertex method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testMayDeleteVertex() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		SimplePolygon sp = new SimplePolygon(vertices);
		for (int i = -10; i < 10; i++)
			assertFalse("Cannot delete a vertex from a polygon with no vertices", sp.mayDeleteVertex(i));
		vertices.add(new Vertex(10, 10, 10));
		sp = new SimplePolygon(vertices);
		for (int i = -10; i < 10; i++)
			assertFalse("May not delete a vertex from a polygon with no more than three vertices", sp.mayDeleteVertex(i));
		vertices.add(new Vertex(20, 10, 11));
		sp = new SimplePolygon(vertices);
		for (int i = -10; i < 10; i++)
			assertFalse("May not delete a vertex from a polygon with no more than three vertices", sp.mayDeleteVertex(i));
		vertices.add(new Vertex(20, 20, 12));
		sp = new SimplePolygon(vertices);
		for (int i = -10; i < 10; i++)
			if ((i >= 0) && (i < 3))
				assertTrue("May delete any vertix from a polygon with three vertices", sp.mayDeleteVertex(i));
			else
				assertFalse("May not delete a non existent vertex", sp.mayDeleteVertex(i));
		vertices.add(new Vertex(10, 20, 13));
		sp = new SimplePolygon(vertices);
		for (int i = -10; i < 20; i++) {
			boolean expect = (i >= 0) && (i < 4);
			if (expect)
				assertTrue("Should be good", sp.mayDeleteVertex(i));
			else
				assertFalse("Should be bad", sp.mayDeleteVertex(i));

		}
		vertices.get(1).setPoint(0, 30, 123);
		vertices.get(3).setPoint(-10, 100, 543);
		sp = new SimplePolygon(vertices);
		for (int i = -10; i < 20; i++) {
			boolean expect = (i >= 0) && (i < 4) && (i != 2) && (i != 3);
			// System.out.println(String.format("i=%d, expect=%s", i, expect ? "true" : " false"));
			if (expect)
				assertTrue("Should be good", sp.mayDeleteVertex(i));
			else
				assertFalse("Should be bad", sp.mayDeleteVertex(i));
		}
	}

	/**
	 * Test the getVertex method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetVertex() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		vertices.add(new Vertex(10, 10, 10));
		vertices.add(new Vertex(20, 10, 11));
		vertices.add(new Vertex(20, 20, 12));
		vertices.add(new Vertex(10, 20, 13));
		SimplePolygon sp = new SimplePolygon(vertices);
		for (int i = 0; i < 4; i++) {
			Vertex v = sp.getVertex(i);
			assertEquals("Should be at same location", 0, v.distance(vertices.get(i)), 0.00001);
			assertTrue("Should be a new instance of a Vertex", v != vertices.get(i));
		}
	}

	/**
	 * Test the size method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSize() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		SimplePolygon sp = new SimplePolygon(vertices);
		assertEquals("Size should be 0", 0, sp.size());
		vertices.add(new Vertex(10, 10, 10));
		sp = new SimplePolygon(vertices);
		assertEquals("Size should be 1", 1, sp.size());
		vertices.add(new Vertex(20, 10, 11));
		sp = new SimplePolygon(vertices);
		assertEquals("Size should be 2", 2, sp.size());
		vertices.add(new Vertex(20, 20, 12));
		sp = new SimplePolygon(vertices);
		assertEquals("Size should be 3", 3, sp.size());
		vertices.add(new Vertex(10, 20, 13));
		sp = new SimplePolygon(vertices);
		assertEquals("Size should be 4", 4, sp.size());
	}

	/**
	 * Test the getName_r method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetName_r() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		SimplePolygon sp = new SimplePolygon(vertices);
		assertTrue("name should be null", null == sp.getName_r());
	}

	/**
	 * Test the setName_w method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSetName() {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		SimplePolygon sp = new SimplePolygon(vertices);
		sp.setName_w("Hello World!");
		assertEquals("Name should be what was just set", "Hello World!", sp.getName_r());
		sp.setName_w(null);
		assertTrue("name should be null", null == sp.getName_r());
	}

}
