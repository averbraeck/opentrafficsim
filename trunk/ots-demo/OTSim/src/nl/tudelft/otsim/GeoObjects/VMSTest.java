package nl.tudelft.otsim.GeoObjects;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Locale;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.GUI.InputValidator;
import nl.tudelft.otsim.SpatialTools.Planar;

import org.junit.Test;

/**
 * Test the methods in the VMS class.
 * @author Peter Knoppers
 */
public class VMSTest {

	private static Link createLink (Network network, String name, Node from, Node to, int laneCount) {
		if (0 == laneCount)
			return null;

		final double laneWidth = 3.0;
		final double grassWidth = 1.0;
		final double stripeRoom = 0.2;
		final double stripeWidth = 0.1;

		ArrayList<CrossSection> csl = new ArrayList<CrossSection>();
		ArrayList<CrossSectionElement> csel = new ArrayList<CrossSectionElement>();
		CrossSection cs = new CrossSection(0, 0, csel);
		csel.add(new CrossSectionElement(cs, "grass", grassWidth, new ArrayList<RoadMarkerAlong>(), null));
		ArrayList<RoadMarkerAlong> rmal = new ArrayList<RoadMarkerAlong>();
		rmal.add(new RoadMarkerAlong("|", stripeRoom / 2 + stripeWidth));
		for (int i = 1; i < laneCount; i++)
			rmal.add(new RoadMarkerAlong(":", i * (laneWidth + stripeRoom) + stripeRoom / 2 + stripeWidth));
		rmal.add(new RoadMarkerAlong("|", laneCount * (laneWidth + stripeWidth) + stripeRoom / 2 + stripeWidth));
		csel.add(new CrossSectionElement(cs, "road", laneCount * (laneWidth + stripeRoom) + stripeRoom, rmal, null));
		csel.add(new CrossSectionElement(cs, "grass", grassWidth, new ArrayList<RoadMarkerAlong>(), null));
		cs.setCrossSectionElementList_w(csel);
		csl.add(cs);
		return network.addLink(name, from.getNodeID(), to.getNodeID(), from.distance(to), false, csl, new ArrayList<Vertex>());
	}
	
	private static VMS createVMSOnLink () {
		Network network = new Network(null);
		Node n1 = network.addNode("n1", 1, 10, 20, 30);
		Node n2 = network.addNode("n2", 2, 20, 30, 50);
		Link l = createLink(network, "n1_n2", n1, n2, 1);
		CrossSectionElement cse = l.getCrossSections_r().get(0).getCrossSectionElementList_r().get(1);
		VMS result = null;
		try {
			result = new VMS(cse);
			cse.addCrossSectionObject(result);
		} catch (Exception e) {
			System.out.println(e.toString());
			fail("Unexpected exception");
		}
		return result;
	}

	/**
	 * Test the paint method
	 */
	@Test
	public void testPaint() {
		// TODO: This is not so easy to test...
		//fail("Not yet implemented");
	}

	/**
	 * Test write to XML stream
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testWriteXML() {
		VMS vms = null;
		try {
			vms = new VMS("");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		StaXWriter writer = null;
		try {
			writer = new StaXWriter(outputStream);
		} catch (Exception e) {
			fail("Caught unexpected exception in creation of the StaXWriter");
		}
		vms.writeXML(writer);
		writer.close();
		String result = outputStream.toString();
		assertEquals("XML version of VMS with zero messages should look like this", 
				"<?xml version=\"1.0\"?>\n"
				+ "<VMS>\n"
				+ "  <ID></ID>\n"
				+ "  <lateralCenter>NaN</lateralCenter>\n"
				+ "  <width>NaN</width>\n"
				+ "  <longitudinalPosition>NaN</longitudinalPosition>\n"
				+ "</VMS>\n", result);
		try {
			vms.addMessage(123.456, "One\r\nTwo\r\nThree");
		} catch (Exception e1) {
			fail("Unexpected exception");
		}
		outputStream = new ByteArrayOutputStream();
		try {
			writer = new StaXWriter(outputStream);
		} catch (Exception e) {
			fail("Caught unexpected exception in creation of the StaXWriter");
		}
		vms.writeXML(writer);
		writer.close();
		result = outputStream.toString();
		//System.out.println("result contains " + result);
		assertEquals("XML version of VMS with one message should look like this", 
				"<?xml version=\"1.0\"?>\n"
				+ "<VMS>\n"
				+ "  <ID></ID>\n"
				+ "  <lateralCenter>NaN</lateralCenter>\n"
				+ "  <width>NaN</width>\n"
				+ "  <longitudinalPosition>NaN</longitudinalPosition>\n"
				+ "  <timeText>\n"
				+ "    <time>123.456</time>\n"
				+ "    <base64Text>T25lDQpUd28NClRocmVl</base64Text>\n"
				+ "  </timeText>\n"
				+ "</VMS>\n", result);
	}

	/**
	 * Test that we can create a VMS from a {@link ParsedNode}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVMSCrossSectionElementParsedNode() {
		VMS vms = createVMSOnLink();
		String xmlText = null;
		try {
			xmlText = StaXWriter.XMLString(vms);
		} catch (Exception e) {
			fail("Caught unexpected exception in creation of the XML text");
		}
		//System.out.println(xmlText);
		// change the position of the original vms
		double originalPosition = vms.getLongitudinalPosition();
		vms.setLongitudinalPosition_w(originalPosition / 2);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlText.getBytes());
		VMS vms2 = null;
		try {
			ParsedNode pn = new ParsedNode(inputStream);
			//System.out.println(pn.toString("All XML "));
			ParsedNode vmsNode = pn.getSubNode(VMS.XMLTAG, 0);
			vms2 = new VMS(vms.crossSectionElement, vmsNode);
		} catch (Exception e) {
			fail("Parsing the XML should not throw any Exception");
		}
		assertEquals("The new VMS should have 0 messages", 0, vms2.getTimedMessages_r().size());
		assertEquals("The new VMS should be at the original position", originalPosition, vms2.getLongitudinalPosition(), 0.00001);
		// add one message and repeat
		try {
			vms.addMessage(0d, "zero");
		} catch (Exception e) {
			fail("Caught unexpected exception");
		}
		try {
			xmlText = StaXWriter.XMLString(vms);
		} catch (Exception e) {
			fail("Caught unexpected exception in creation of the XML text");
		}
		//System.out.println(xmlText);
		inputStream = new ByteArrayInputStream(xmlText.getBytes());
		try {
			ParsedNode pn = new ParsedNode(inputStream);
			//System.out.println(pn.toString("All XML "));
			ParsedNode vmsNode = pn.getSubNode(VMS.XMLTAG, 0);
			vms2 = new VMS(vms.crossSectionElement, vmsNode);
		} catch (Exception e) {
			fail("Parsing the XML should not throw any Exception");
		}
		assertEquals("The new VMS should have 1 message", 1, vms2.getTimedMessages_r().size());
		assertEquals("The message should be what we put in earlier", "zero", vms2.getTimedMessages_r().get(0).getMessage_r());
		assertEquals("The new VMS should be at the changed position", originalPosition / 2, vms2.getLongitudinalPosition(), 0.00001);
		// add another message and repeat
		try {
			vms.addMessage(111d, "one");
		} catch (Exception e) {
			fail("Caught unexpected exception");
		}
		try {
			xmlText = StaXWriter.XMLString(vms);
		} catch (Exception e) {
			fail("Caught unexpected exception in creation of the XML text");
		}
		//System.out.println(xmlText);
		inputStream = new ByteArrayInputStream(xmlText.getBytes());
		try {
			ParsedNode pn = new ParsedNode(inputStream);
			//System.out.println(pn.toString("All XML "));
			ParsedNode vmsNode = pn.getSubNode(VMS.XMLTAG, 0);
			vms2 = new VMS(vms.crossSectionElement, vmsNode);
		} catch (Exception e) {
			fail("Parsing the XML should not throw any Exception");
		}
		assertEquals("The new VMS should have 2 messages", 2, vms2.getTimedMessages_r().size());
		assertEquals("The message should be what we put in earlier", "zero", vms2.getTimedMessages_r().get(0).getMessage_r());
		assertEquals("The message should be what we put in earlier", "one", vms2.getTimedMessages_r().get(1).getMessage_r());
		assertEquals("The new VMS should be at the changed position", originalPosition / 2, vms2.getLongitudinalPosition(), 0.00001);
	}

	/**
	 * Test import
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVMSString() {
		VMS vms = null;
		try {
			vms = new VMS("");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		String out = vms.export();
		assertEquals("VMS with no messages should export as an empty string", "", out);
		try {
			vms.addMessage(10.5, "one");
			vms.addMessage(20.7, "two");
			vms.addMessage(0d, "zero");
		} catch (Exception e1) {
			fail("Unexpected exception");
		}
		ArrayList<VMS.TimedMessage> messages = vms.getTimedMessages_r();
		assertEquals("after inserting three messages there should be three in the VMS", 3, messages.size());
		assertEquals("messages should be sorted and the first one should be at time 0", 0, messages.get(0).getTime(), 0.000001);
		assertEquals("label of very first message should be \"zero\"", "zero", messages.get(0).getMessage_r());
		assertEquals("messages should be sorted and the second one should be at time 10.5", 10.5, messages.get(1).getTime(), 0.000001);
		assertEquals("label of next message should be \"one\"", "one", messages.get(1).getMessage_r());
		assertEquals("messages should be sorted and the third one should be at time 20.7", 20.7, messages.get(2).getTime(), 0.000001);
		assertEquals("label of last message should be \"two\"", "two", messages.get(2).getMessage_r());
		out = vms.export();
		VMS vms2 = null;
		try {
			vms2 = new VMS(out);
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		assertEquals("VMS from exported string should have original number of messages", vms.getTimedMessages_r().size(), vms2.getTimedMessages_r().size());
		for (int i = 0; i < vms.getTimedMessages_r().size(); i++) {
			assertEquals("times should match", vms.getTimedMessages_r().get(i).getTime(), vms2.getTimedMessages_r().get(i).getTime(), 0.00001);
			assertEquals("messages should match", vms.getTimedMessages_r().get(i).getMessage_r(), vms2.getTimedMessages_r().get(i).getMessage_r());
		}
		try {
			vms.addMessage(1234d, "");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		out = vms.export();
		try {
			vms2 = new VMS(out);
		} catch (Exception e) {
			fail("Unexpected exception");
		}
	}

	/**
	 * Create a VMS owned by a CrossSectionElement.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testVMSCrossSectionElement() {
		createVMSOnLink();
	}

	/**
	 * Test that ID is readable and alterable
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetID_r() {
		VMS vms = null;
		try {
			vms = new VMS("");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		assertEquals("Unset ID should yield null", null, vms.getID_r());
		vms = createVMSOnLink();
		vms.setID_w("abc def");
		assertEquals("ID returned should be last ID set", "abc def", vms.getID_r());
	}

	/**
	 * Check that we can call setID
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSetID_w() {
		VMS vms = null;
		try {
			vms = new VMS("");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		boolean exceptionThrown = false;
		try {
			vms.setID_w("abc");
		} catch (Exception e) {
			exceptionThrown = true;
		}
		assertTrue ("Setting name of VMS that is not linked to a CrossSectionElement should have thrown an exception", exceptionThrown);
		vms = createVMSOnLink();
		vms.setID_w("pqr");	// should NOT throw an exception
	}

	/**
	 * Test the {@link InputValidator} for ID.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testValidateID_v() {
		VMS vms = createVMSOnLink();
		InputValidator iv = vms.validateID_v();
		assertFalse("ID should not be totally constrained", iv.totallyConstrained());
		assertFalse("Empty ID is not acceptable", iv.validate("", ""));
		assertTrue("A single letter is a valid ID", iv.validate("", "a"));
		assertTrue("A single letter is a valid ID", iv.validate("", "Z"));
		assertFalse("ID must not start with a digit", iv.validate("", "6a"));
		assertTrue("Letter followed by letters, digits and underscores is a valid ID", iv.validate("", "aBcD7658948364__44"));
		assertTrue("Underscore followed by letters, digits and underscores is a valid ID", iv.validate("", "_aBcD7658948364__44"));
		String currentID = vms.getID_r();
		assertFalse("CurrentName is not acceptable as a name", iv.validate("",  currentID));
		assertFalse("CurrentName is acceptable as a name for the current VMS", iv.validate(currentID,  currentID));
	}

	/**
	 * Check the default lateral position.
	 * <br /> The non-default lateral position is tested in testSetLateralPosition_w.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetLateralPosition_r() {
		VMS vms = createVMSOnLink();
		assertEquals("Lateral position should be 0 (unless modified)", vms.getLateralPosition(), 0, 0.00001);
	}

	/**
	 * Test setting the lateral position.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSetLateralPosition_w() {
		VMS vms = createVMSOnLink();
		for (int step = -100; step <= 100; step++) {
			double pos = 0.1 * step;
			vms.setLateralPosition_w(pos);
			assertEquals("Last set lateral position should be returned", pos, vms.getLateralPosition(), 0.00001);
		}
	}

	/**
	 * Test the {@link InputValidator} for lateral position.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testValidateLateralPosition_v() {
		VMS vms = createVMSOnLink();
		InputValidator iv = vms.validateLateralPosition_v();
		assertTrue("initially the lateralPosition should be totally constrained", iv.totallyConstrained());
		String currentValue = String.format(Locale.US, "%.3f", vms.longitudinalPosition);
		assertTrue("zero is an acceptable lateral position", iv.validate(currentValue, "0"));
		// In fact virtually anything is acceptable
		for (int i = -100; i <= 100; i++) {
			String proposedValue = String.format(Locale.US,  "%.3f", i * 0.1d);
			if (0 == i)
				assertTrue("Anything but the fully constrained value 0 is acceptable", iv.validate(currentValue,  proposedValue));
			else
				assertFalse("The value 0 is acceptable", iv.validate("12", proposedValue));
		}
		double csWidth = vms.crossSectionElement.getWidth_r();
		vms.setWidth_w(csWidth - 1.5);
		iv = vms.validateLateralPosition_v();
		assertFalse("The lateralPosition should not be totally constrained", iv.totallyConstrained());
		for (int i = -100; i <= 100; i++) {
			double proposedLateralPosition = 0.101 * i;
			String proposedValue = String.format(Locale.US,  "%.3f", proposedLateralPosition);
			if (Math.abs(proposedLateralPosition) < 1.5 / 2)
				assertTrue("Value in range should be acceptable", iv.validate(currentValue,  proposedValue));
			else
				assertFalse("Value out of range should be rejected", iv.validate(currentValue, proposedValue));
		}
		vms.setWidth_w(0d);
		iv = vms.validateLateralPosition_v();
		assertFalse("The lateralPosition should not be totally constrained", iv.totallyConstrained());
		for (int i = -100; i <= 100; i++) {
			double proposedLateralPosition = 0.101 * i;
			String proposedValue = String.format(Locale.US,  "%.3f", proposedLateralPosition);
			if (Math.abs(proposedLateralPosition) < csWidth / 2)
				assertTrue("Value in range should be acceptable", iv.validate(currentValue,  proposedValue));
			else
				assertFalse("Value out of range should be rejected", iv.validate(currentValue, proposedValue));
		}
		
	}

	/**
	 * Check that the getWidth_r method works.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetWidth_r() {
		VMS vms = createVMSOnLink();
		assertEquals("Default width of VMS should be width of CrossSectionElement", vms.crossSectionElement.getWidth_r(), vms.getWidth_r(), 0.0001);;
	}

	/**
	 * Check that reasonable values of lateral width can be set and read back.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSetWidth_w() {
		VMS vms = createVMSOnLink();
		for (int i = -100; i <= 100; i++) {
			double w = 0.1 * i;
			vms.setWidth_w(w);
			assertEquals("Returned width should equals last set width", w, vms.getWidth_r(), 0.000001);
		}
	}

	/**
	 * Test the {@link InputValidator} for width.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testValidateWidth_v() {
		VMS vms = createVMSOnLink();
		double crossSectionWidth = vms.crossSectionElement.getWidth_r();
		int limit = (int) Math.floor(10 * crossSectionWidth / 2);
		for (int latStep = -limit; latStep <= limit; latStep++) {
			double latPos = 0.1 * latStep;
			vms.setLateralPosition_w(latPos);
			InputValidator iv = vms.validateWidth_v();
			for (int widthStep = -10; widthStep < 50; widthStep++) {
				double width = 0.1 * widthStep;
				// Test with a locale that uses a comma as radix symbol
				String proposedValue = String.format(Locale.GERMAN, "%.3f", width);
				//System.out.println(String.format("latpos=%.3f, width=%s", latPos, proposedValue));
				if ((width < 0.1) || (width > crossSectionWidth - Math.abs(latPos)))
					assertFalse("Illegal value for lateral pos should be rejected", iv.validate("", Planar.fixRadix(proposedValue)));
				else
					assertTrue("Legal value for lateral position should be accepted", iv.validate("", Planar.fixRadix(proposedValue)));
			}
		}
	}

	/**
	 * Test that getPolygon returns a rectangular area of the expected size
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetPolygon_r() {
		VMS vms = createVMSOnLink();
		ArrayList<Vertex> polygon = vms.getPolygon_r(); 
		assertEquals("Polygon of VMS should have 4 vertices", 4, polygon.size());
		double w = vms.crossSectionElement.getWidth_r();
		double l = vms.getLongitudinalLength();
		//System.out.println(String.format("w=%f, dist=%f", w, polygon.get(0).distance(polygon.get(1))));
		assertEquals("Long edge of polygon should equal length", l, polygon.get(0).distance(polygon.get(1)), 0.0001);
		assertEquals("Long edge of polygon should equal width", w, polygon.get(1).distance(polygon.get(2)), 0.0001);
		assertEquals("Long edge of polygon should equal length", l, polygon.get(2).distance(polygon.get(3)), 0.0001);
		assertEquals("Long edge of polygon should equal width", w, polygon.get(3).distance(polygon.get(0)), 0.0001);
		double diagonal = Math.sqrt(l * l + w * w);
		assertEquals("Angles of polygon should be 90 degrees", diagonal, polygon.get(0).distance(polygon.get(2)), 0.0001);
		assertEquals("Angles of polygon should be 90 degrees", diagonal, polygon.get(1).distance(polygon.get(3)), 0.0001);
		vms.longitudinalLength = l = 3;
		polygon = vms.getPolygon_r();
		//System.out.println(String.format("w=%f, dist=%f", w, polygon.get(0).distance(polygon.get(1))));
		assertEquals("Long edge of polygon should equal length", l, polygon.get(0).distance(polygon.get(1)), 0.0001);
		assertEquals("Long edge of polygon should equal width", w, polygon.get(1).distance(polygon.get(2)), 0.0001);
		assertEquals("Long edge of polygon should equal length", l, polygon.get(2).distance(polygon.get(3)), 0.0001);
		assertEquals("Long edge of polygon should equal width", w, polygon.get(3).distance(polygon.get(0)), 0.0001);
		diagonal = Math.sqrt(l * l + w * w);
		assertEquals("Angles of polygon should be 90 degrees", diagonal, polygon.get(0).distance(polygon.get(2)), 0.0001);
		assertEquals("Angles of polygon should be 90 degrees", diagonal, polygon.get(1).distance(polygon.get(3)), 0.0001);
		vms.setWidth_w(w = 2);
		polygon = vms.getPolygon_r();
		//System.out.println(String.format("w=%f, dist=%f", w, polygon.get(0).distance(polygon.get(1))));
		assertEquals("Long edge of polygon should equal length", l, polygon.get(0).distance(polygon.get(1)), 0.0001);
		assertEquals("Long edge of polygon should equal width", w, polygon.get(1).distance(polygon.get(2)), 0.0001);
		assertEquals("Long edge of polygon should equal length", l, polygon.get(2).distance(polygon.get(3)), 0.0001);
		assertEquals("Long edge of polygon should equal width", w, polygon.get(3).distance(polygon.get(0)), 0.0001);
		diagonal = Math.sqrt(l * l + w * w);
		assertEquals("Angles of polygon should be 90 degrees", diagonal, polygon.get(0).distance(polygon.get(2)), 0.0001);
		assertEquals("Angles of polygon should be 90 degrees", diagonal, polygon.get(1).distance(polygon.get(3)), 0.0001);
	}

	/**
	 * Test the default longitudinal position and see if we can change it.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetLongitudinalPosition_r() {
		VMS vms = createVMSOnLink();
		assertEquals("default longitudinal position is halfway on the CrossSection", vms.crossSectionElement.getCrossSection().getLongitudinalLength() / 2, vms.getLongitudinalPosition(), 0.0001);
		vms.setLongitudinalPosition_w(1.2345);
		assertEquals("default longitudinal position is halfway on the CrossSection", 1.2345, vms.getLongitudinalPosition(), 0.0001);
	}

	/**
	 * Prove that we can set the longitudinal position and read it back.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSetLongitudinalPosition_w() {
		VMS vms = null;
		try {
			vms = new VMS("");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		boolean exceptionThrown = false;
		try {
			vms.setLongitudinalPosition_w(123.45);
		} catch (Exception e) {
			exceptionThrown = true;
		}
		assertTrue("Setting the longitudinal position is not permitted if the VMS is not associated with a CrossSectionElement", exceptionThrown);
				
		vms = createVMSOnLink();
		double limit = vms.crossSectionElement.getCrossSection().getLongitudinalLength();
		int steps = ((int) limit) - 1;
		for (int step = -steps; step <= steps; step++) {
			double longPosition = step;
			vms.setLongitudinalPosition_w(longPosition);
			double got = vms.getLongitudinalPosition();
			assertEquals("returned longitudinal position should equal set longitudinal position", longPosition, got, 0.0001);
		}
	}

	/**
	 * Check the {@link InputValidator} returned for the longitudinal position
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testValidateLongitudinalPosition_v() {
		VMS vms = createVMSOnLink();
		InputValidator iv = vms.validateLongitudinalPosition_v();
		assertFalse("longitudinalposition should not be totally constrained", iv.totallyConstrained());
		String currentValue = String.format(Locale.US, "%.3f", vms.longitudinalPosition);
		assertTrue("zero is acceptable longitudinal position", iv.validate(currentValue, "0"));
		double limit = vms.crossSectionElement.getCrossSection().getLongitudinalLength();
		String limitValue = String.format(Locale.US,  "%.3f", limit - 0.1);
		assertTrue("just below length of crossSection is a valid position", iv.validate(currentValue, limitValue));
		limitValue = String.format(Locale.US,  "%.3f", - limit + 0.1);
		assertTrue("just above minus length of crossSection is a valid position", iv.validate(currentValue, limitValue));
		limitValue = String.format(Locale.US,  "%.3f", limit + 0.1);
		assertFalse("just above length of crossSection is not a valid position", iv.validate(currentValue, limitValue));
		limitValue = String.format(Locale.US,  "%.3f", - limit - 0.1);
		assertFalse("just below minus length of crossSection is not a valid position", iv.validate(currentValue, limitValue));
	}

	/**
	 * mayDelete should return true;
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testMayDeleteVMS_d() {
		VMS vms = null;
		try {
			vms = new VMS("");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		assertTrue("A VMS can always be deleted", vms.mayDeleteVMS_d());
	}

	/**
	 * Test that a VMS can be deleted
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testDeleteVMS_d() {
		VMS vms = createVMSOnLink();
		CrossSectionElement cse = vms.crossSectionElement;
		assertTrue(cse.getCrossSectionObjects(VMS.class).contains(vms));
		vms.deleteVMS_d();
		assertFalse(cse.getCrossSectionObjects(VMS.class).contains(vms));
	}

	/**
	 * Execute the toString method.
	 * <br /> This method behaves differently depending on the presence of
	 * a parent CrossSectionElement.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testToString() {
		VMS vms = null;
		try {
			vms = new VMS("");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		try {
			vms.addMessage(10.11, "Hello world!");
			vms.addMessage(11.11, "Goodbye!");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		vms.toString();
		vms = createVMSOnLink();
		try {
			vms.addMessage(210.11, "Hello world!");
			vms.addMessage(3210.11, "Goodbye!");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		vms.toString();
	}

	/**
	 * Test the export method (more thorough testing is done in testVMSString).
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testExport() {
		VMS vms = null;
		try {
			vms = new VMS("");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		System.out.println(vms.export());
	}

	/**
	 * Check that getTimedMessages_r returns the right thing.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetTimedMessages_r() {
		VMS vms = null;
		try {
			vms = new VMS("");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		assertEquals("newly created vms should have 0 TimedMessages", 0, vms.getTimedMessages_r().size());
		try {
			vms.addMessage(0.11, "Hello world!");
			vms.addMessage(210.11, "Goodbye!");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		assertEquals("vms with two messages added should have 2 TimedMessages", 2, vms.getTimedMessages_r().size());
		assertEquals("check first TimedMessage time", 0.11, vms.getTimedMessages_r().get(0).getTime(), 0.00001);
		assertTrue("check first TimedMessage text", "Hello world!".equals(vms.getTimedMessages_r().get(0).getMessage_r()));
		assertEquals("check second TimedMessage time", 210.11, vms.getTimedMessages_r().get(1).getTime(), 0.00001);
		assertTrue("check second TimedMessage text", "Goodbye!".equals(vms.getTimedMessages_r().get(1).getMessage_r()));
	}

	/**
	 * Check that getAdd_r returns the expected (constant) String.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testGetAdd_r() {
		VMS vms = null;
		try {
			vms = new VMS("");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		assertEquals("Check prompt for add menu message", "new timed message", vms.getAdd_r());
	}

	/**
	 * Check that setAdd_w creates messages with the supplied text and the expected time.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testSetAdd_w() {
		VMS vms = createVMSOnLink();
		try {
			vms.setAdd_w("one");
		} catch (Exception e) {
			fail("Caught unexpected exception");
		}
		assertEquals("check TimedMessage time", 0, vms.getTimedMessages_r().get(0).getTime(), 0.00001);
		assertTrue("check TimedMessage text", "one".equals(vms.getTimedMessages_r().get(0).getMessage_r()));
		try {
			vms.setAdd_w("two");
		} catch (Exception e) {
			fail("Caught unexpected exception");
		}
		assertEquals("check TimedMessage time", 0, vms.getTimedMessages_r().get(0).getTime(), 0.00001);
		assertTrue("check TimedMessage text", "one".equals(vms.getTimedMessages_r().get(0).getMessage_r()));
		assertEquals("check TimedMessage time", 1, vms.getTimedMessages_r().get(1).getTime(), 0.00001);
		assertTrue("check TimedMessage text", "two".equals(vms.getTimedMessages_r().get(1).getMessage_r()));
		try {
			vms.getTimedMessages_r().get(0).setTime(1234.5);
		} catch (Exception e) {
			fail("Caught unexpected exception");
		}
		//System.out.println(vms.export());
		assertEquals("check TimedMessage time", 1, vms.getTimedMessages_r().get(0).getTime(), 0.00001);
		assertTrue("check TimedMessage text", "two".equals(vms.getTimedMessages_r().get(0).getMessage_r()));
		assertEquals("check TimedMessage time", 1234.5, vms.getTimedMessages_r().get(1).getTime(), 0.00001);
		assertTrue("check TimedMessage text", "one".equals(vms.getTimedMessages_r().get(1).getMessage_r()));
		try {
			vms.setAdd_w("two");
		} catch (Exception e) {
			fail("Caught unexpected exception");
		}
		
	}
	
	/**
	 * Check that we can add messages with sensible time for them to appear.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testAddMessageDoubleString () {
		VMS vms = null;
		try {
			vms = new VMS("");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		try {
			vms.addMessage(123.4, "test");
		} catch (Exception e1) {
			fail("Unexpected exception");
		}
		boolean exceptionThrown = false;
		try {
			vms.addMessage(-0.01, "should fail");
		} catch (Exception e) {
			exceptionThrown = true;
		}
		assertTrue("Trying to add a message that should appear at a negative time should throw an exception", exceptionThrown);
	}

	/**
	 * Test that the correct messages are shown depending on the time.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testMessageDouble () {
		VMS vms = null;
		try {
			vms = new VMS("");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		for (int t = 0; t <= 1000; t++)
			assertEquals("If no messages are to be shown ever, the result must be the empty string", "", vms.message(1d * t));
		try {
			vms.addMessage(10.5, "one");
			vms.addMessage(20.7, "two");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		for (int t = 0; t <= 10; t++)
			assertEquals("Before the first scheduled message nothing is shown", "", vms.message(1d * t));
		for (int t = 11; t <= 20; t++)
			assertEquals("The first scheduled message should be shown", "one", vms.message(1d * t));
		for (int t = 21; t <= 10000; t++)
			assertEquals("The second scheduled message should be shown", "two", vms.message(1d * t));
		try {
			vms.addMessage(0d, "zero");
		} catch (Exception e) {
			fail("Unexpected exception");
		}
		for (int t = 0; t <= 10; t++)
			assertEquals("The first scheduled message should be shown", "zero", vms.message(1d * t));
	}
	
}
