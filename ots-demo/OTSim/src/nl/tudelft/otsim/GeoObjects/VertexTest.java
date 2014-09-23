package nl.tudelft.otsim.GeoObjects;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * Test the methods in the Vertex class.
 * 
 * @author Peter Knoppers
 */
public class VertexTest {

	/**
	 * Test the creator taking three double arguments.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVertexDoubleDoubleDouble() {
		Vertex v = new Vertex(1.23456, 2.34567, 3.45678);
		assertEquals("Check X", 1.23456, v.x, 0.0000001);
		assertEquals("Check Y", 2.34567, v.y, 0.0000001);
		assertEquals("Check Z", 3.45678, v.z, 0.0000001);
	}

	/**
	 * Test the creator taking a Vertex as argument.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVertexVertex() {
		Vertex v1 = new Vertex(1, 2, 3);
		Vertex v2 = new Vertex(v1);
		assertFalse("Should not be the same instance", v1 == v2);
		assertEquals("Check X", v1.x, v2.x, 0.0000001);
		assertEquals("Check Y", v1.y, v2.y, 0.0000001);
		assertEquals("Check Z", v1.z, v2.z, 0.0000001);
		
		v1.x = 123;
		assertFalse("Should be independent", v1.x == v2.x);
		
	}

	/**
	 * Test the creator taking a Point2D.Double and a double as arguments.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVertexDoubleDouble() {
		Vertex v = new Vertex(new Point2D.Double(1.23456, 2.34567), 3.45678);
		assertEquals("Check X", 1.23456, v.x, 0.0000001);
		assertEquals("Check Y", 2.34567, v.y, 0.0000001);
		assertEquals("Check Z", 3.45678, v.z, 0.0000001);
	}

	/**
	 * Test the creator taking no arguments.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVertex() {
		Vertex v = new Vertex();
		assertEquals("Check X", 0, v.x, 0.0000001);
		assertEquals("Check Y", 0, v.y, 0.0000001);
		assertTrue("Check Z", Double.isNaN(v.z));
	}

	/**
	 * Test the creator taking a ParsedNode as argument.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVertexParsedNode() {
		Vertex v1 = new Vertex(new Point2D.Double(1.23456, 2.34567), 3.45678);
		String xmlText = String.format(Locale.US, "<?xml version=\"1.0\"?>\r\n<bla><%s>%s</%s><%s>%s</%s><%s>%s</%s>\r\n</bla>\r\n",
					Vertex.XML_X, Double.toString(v1.x), Vertex.XML_X, 
					Vertex.XML_Y, Double.toString(v1.y), Vertex.XML_Y, 
					Vertex.XML_Z, Double.toString(v1.z), Vertex.XML_Z);
		//System.out.println(xmlText);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlText.getBytes());
		ParsedNode pn = null;
		try {
			pn = new ParsedNode(inputStream);
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		Vertex v2 = null;
		try {
			v2 = new Vertex(pn.getSubNode("bla", 0));
		} catch (Exception e) {
			fail ("Unexpected exception");
		}
		assertEquals("Reconstructed vertex should be at same location", 0, v1.distance(v2), 0.000001);
	}

	/**
	 * Test the creator taking a Coordinate as argument.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVertexCoordinate() {
		Coordinate c = new Coordinate(10, 20, 30.5);
		Vertex v = new Vertex(c);
		assertEquals("X", c.x, v.x, 0.000001);
		assertEquals("Y", c.y, v.y, 0.000001);
		assertEquals("Z", c.z, v.z, 0.000001);
	}

	/**
	 * Test the writeVertexXML method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testWriteVertexXML() {
		Vertex v1 = new Vertex(new Point2D.Double(1.23456, 2.34567), 3.45678);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		StaXWriter sw = null;
		try {
			sw = new StaXWriter(outputStream);
		} catch (Exception e1) {
			fail("Caught unexpected exception");
		}
		try {
			assertTrue("Writer should go fine", v1.writeVertexXML(sw));
			sw.close();
		} catch (Exception e) {
			fail("Caught unexpected exception in creation of the XML text");
		}
		//System.out.println("outputStream contains\"" + outputStream.toString() + "\"");
		assertEquals("Expected xml", 
				"<?xml version=\"1.0\"?>\n" +
				"<X>1.23456</X>\n" +
				"<Y>2.34567</Y>\n" +
				"<Z>3.45678</Z>\n", outputStream.toString());
	}

	/**
	 * Test the weightedVertex method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testWeightedVertex() {
		Vertex v1 = new Vertex(10, 20, 30);
		Vertex v2 = new Vertex(30, 40, 50);
		assertEquals("Weight 0 should return the first vector", 0, Vertex.weightedVertex(0, v1, v2).distance(v1), 0.000001);
		assertEquals("Weight 1 should return the second vector", 0, Vertex.weightedVertex(1, v1, v2).distance(v2), 0.000001);
		assertEquals("Weight 0.5 should return the center", 0, Vertex.weightedVertex(0.5, v1, v2).distance(
				new Vertex((v1.x + v2.x) / 2, (v1.y + v2.y) / 2, (v1.z + v2.z) / 2)), 0.000001);
		assertEquals("Extrapolation", 0, Vertex.weightedVertex(2, v1, v2).distance(
				new Vertex(2 * v2.x - v1.x, 2 * v2.y - v1.y, 2 * v2.z - v1.z)), 0.000001);
		assertEquals("Extrapolation", 0, Vertex.weightedVertex(-1, v2, v1).distance(
				new Vertex(2 * v2.x - v1.x, 2 * v2.y - v1.y, 2 * v2.z - v1.z)), 0.000001);
	}

	/**
	 * Test the getX method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetX() {
		for (int i = 0; i < 1000; i += 10) {
			Vertex v = new Vertex(i / 3.0, i / 7.0, i / 11.0);
			assertEquals("The getX method should return the x field", i / 3.0, v.getX(), 0.000001);
		}
	}

	/**
	 * Test the setX method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSetX() {
		Vertex v = new Vertex (10, 20, 30);
		for (int i = 0; i < 100; i+= 10) {
			double value = 123.456 * i;
			v.setX(value);
			assertEquals("The setX method should set the x field", value, v.x, 0.0000001);
			assertEquals("The setX method should not change other fields", 20, v.y, 0.000001);
			assertEquals("The setX method should not change other fields", 30, v.z, 0.000001);
		}
	}

	/**
	 * Test the getY method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetY() {
		for (int i = 0; i < 1000; i += 10) {
			Vertex v = new Vertex(i / 3.0, i / 7.0, i / 11.0);
			assertEquals("The getY method should return the x field", i / 7.0, v.getY(), 0.000001);
		}
	}

	/**
	 * Test the setY method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSetY() {
		Vertex v = new Vertex (10, 20, 30);
		for (int i = 0; i < 100; i+= 10) {
			double value = 123.456 * i;
			v.setY(value);
			assertEquals("The setY method should set the y field", value, v.y, 0.0000001);
			assertEquals("The setY method should not change other fields", 10, v.x, 0.000001);
			assertEquals("The setY method should not change other fields", 30, v.z, 0.000001);
		}
	}

	/**
	 * Test the getZ method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetZ() {
		for (int i = 0; i < 1000; i += 10) {
			Vertex v = new Vertex(i / 3.0, i / 7.0, i / 11.0);
			assertEquals("The getZ method should return the x field", i / 11.0, v.getZ(), 0.000001);
		}
	}

	/**
	 * Test the setZ method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSetZ() {
		Vertex v = new Vertex (10, 20, 30);
		for (int i = 0; i < 100; i+= 10) {
			double value = 123.456 * i;
			v.setZ(value);
			assertEquals("The setZ method should set the z field", value, v.z, 0.0000001);
			assertEquals("The setZ method should not change other fields", 10, v.x, 0.000001);
			assertEquals("The setZ method should not change other fields", 20, v.y, 0.000001);
		}
	}

	/**
	 * Test the getPoint method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetPoint() {
		Vertex v = new Vertex (10, 20, 30);
		Point2D.Double p = v.getPoint();
		assertEquals("X of getPoint should be X of vertex", 10, p.x, 0.0000001);
		assertEquals("Y of getPoint should be Y of vertex", 20, p.y, 0.0000001);
	}

	/**
	 * Test the setPoint method taking three double arguments.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSetPointDoubleDoubleDouble() {
		Vertex v = new Vertex (10, 20, 30);
		v.setPoint(123, 456, 789.01);
		assertEquals("X after setPoint", 123, v.x, 0.000001);
		assertEquals("Y after setPoint", 456, v.y, 0.000001);
		assertEquals("Z after setPoint", 789.01, v.z, 0.000001);
	}

	/**
	 * Test the setPoint method taking a Point2D.Double argument.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSetPointDouble() {
		Vertex v = new Vertex(10, 20, 30);
		v.setPoint(new Point2D.Double(40, 50));
		assertEquals("X after setPoint", 40, v.x, 0.000001);
		assertEquals("Y after setPoint", 50, v.y, 0.000001);
		assertEquals("Z after setPoint", 30, v.z, 0.000001);
	}

	/**
	 * Test the setPoint method taking a Vertex argument.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSetPointVertex() {
		Vertex v1 = new Vertex(10, 20, 30);
		Vertex v2 = new Vertex(30, 40, 50);
		v1.setPoint(v2);
		assertEquals("X after setPoint", 30, v2.x, 0.000001);
		assertEquals("Y after setPoint", 40, v2.y, 0.000001);
		assertEquals("Z after setPoint", 50, v2.z, 0.000001);
		
	}

	/**
	 * Test the toString method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testToString() {
		Vertex v = new Vertex(10, 20, 30);
		nl.tudelft.otsim.GUI.Main.locale = Locale.US;
		String out = v.toString();
		assertEquals("ToString using US locale", "(10.000m,20.000m,30.000m)", out);
		nl.tudelft.otsim.GUI.Main.locale = Locale.GERMAN;
		out = v.toString();
		assertEquals("ToString using US locale", "(10,000m,20,000m,30,000m)", out);
	}

	/**
	 * Test the log method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testLog() {
		for (int i = -100; i < 100; i += 25)
			for (int j = -1000; j < 1000; j += 100) {
				ByteArrayOutputStream myOut = new ByteArrayOutputStream();
				System.setOut(new PrintStream(myOut));
				Vertex v1 = new Vertex (0.3 * i, 0.3 * j, 0.7 * i + 0.11 * j);
				Vertex v2 = v1.log("test");
				assertEquals("Should be same vertex", v1, v2);
				String output = myOut.toString();
				assertEquals("Output should be like this", String.format(Locale.US, "%s: (%.3fm,%.3fm,%.3fm)\r\n", "test", v1.x, v1.y, v1.z), output);
			}
	}

	/**
	 * Test the paint method. <br /> This is not so easy...
	 */
	@Test
	public void testPaintGraphicsPanel() {
		// fail("Very hard to test");
	}

	/**
	 * Test the paint method. <br /> This is not so easy...
	 */
	@Test
	public void testPaintGraphicsPanelColor() {
		//fail("Very hard to test");
	}

	/**
	 * Test the equals2D method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testEquals2D() {
		Vertex v1 = new Vertex(10, 20, 30);
		Vertex v2 = new Vertex(30, 40, 50);
		assertFalse("Should test different", v1.equals2D(v2));
		v2.x = v1.x;
		assertFalse("Should test different", v1.equals2D(v2));
		v2.y = v1.y;
		assertTrue("Should test equal", v1.equals2D(v2));
		v2.x = 100;
		assertFalse("Should test different", v1.equals2D(v2));
	}

	/**
	 * Test the distanceTo method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testDistance() {
		Vertex v1 = new Vertex(10, 20, 30);
		Vertex v2 = new Vertex(30, 40, 50);
		assertEquals("Distance to itself is 0", 0, v1.distance(v1), 0.000001);
		assertEquals("Distance to itself is 0", 0, v2.distance(v2), 0.000001);
		assertEquals("Distance to other", Math.sqrt(1200), v1.distance(v2), 0.000001);
		assertEquals("Distance to other", Math.sqrt(1200), v2.distance(v1), 0.000001);
	}


}
