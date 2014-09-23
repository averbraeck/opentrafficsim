package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.util.ArrayList;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.SpatialTools.Planar;

public class TurnArrow extends CrossSectionObject {
	/** Name for a TurnArrow element when stored in XML format */
	public static final String XMLTAG = "turnArrow";
	
	private static final String XML_TURNTO = "outLinkNumber";
	private static final String XML_LATERALPOSITION = "lateralPosition";
	private static final String XML_LONGITUDINALPOSITION = "longitudinalPosition";	
	//public enum TurnMovement {ALL, RIGHT, RIGHT2, RIGHT_RIGHT2, RIGHT_AHEAD, RIGHT_LEFT, RIGHT2_AHEAD, AHEAD, AHEAD2, AHEAD_LEFT, AHEAD_LEFT2, LEFT, LEFT2, LEFT_LEFT2};
	private int[] outLinkNumbers; 
	
	public TurnArrow(CrossSectionElement cse, int[] outLinkNumbers, double lateralPosition, double longitudinalPosition) {
		//System.out.println("Creating new turn arrow at longitudinalPosition " + longitudinalPosition);
		this.crossSectionElement = cse;
		this.lateralReference = CrossSectionElement.LateralReferenceLeft;
		this.setLateralPosition(lateralPosition);
		if (Double.isNaN(longitudinalPosition))
			System.err.println("Bad longitudinalPosition");
		this.longitudinalPosition = longitudinalPosition;
		this.longitudinalLength = 3.5;	// m
		this.longitudinalRepeat = 50;	// m
		this.outLinkNumbers = outLinkNumbers;
	}

	/**
	 * Create a TurnArrow from a parsed XML file.
	 * @param cse {@link CrossSectionElement}; owner of the new TurnArrow
	 * @param pn {@link ParsedNode}; the root of the TurnArrow in the parsed XML file
	 * @throws Exception
	 */
	public TurnArrow(CrossSectionElement cse, ParsedNode pn) throws Exception {
		crossSectionElement = cse;
		lateralReference = CrossSectionElement.LateralReferenceLeft;
		longitudinalPosition = lateralPosition = Double.NaN;
		longitudinalLength = 3.5; 	// m
		longitudinalRepeat = 50;	// m
		outLinkNumbers = null;
		
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_LATERALPOSITION))
				lateralPosition = Double.parseDouble(value);
			else if (fieldName.equals(XML_LONGITUDINALPOSITION))
				longitudinalPosition = Double.parseDouble(value);
			else if (fieldName.equals(XML_TURNTO)) {
				String[] fields = value.split("  *");
				outLinkNumbers = new int[fields.length];
				int index = 0;
				for (String field : fields)
					outLinkNumbers[index++] = Integer.parseInt(field);
			} else
				throw new Exception("TurnArrow does not have a field " + fieldName);
		}
		if (Double.isNaN(lateralPosition))
			throw new Exception("TurnArrow has no lateralPosition " + pn.lineNumber + ", " + pn.columnNumber);
		if (Double.isNaN(longitudinalPosition))
			throw new Exception("TurnArrow has no longitudinalPosition " + pn.lineNumber + ", " + pn.columnNumber);
		if (null == outLinkNumbers)
			throw new Exception("TurnArrow has not outLinkNumber(s) " + pn.lineNumber + ", " + pn.columnNumber);
	}

	public int[] getOutLinkNumbers() {
		return outLinkNumbers;
	}

	public void setLateralPosition(double lateralPosition) {
		this.lateralPosition = lateralPosition;
	}

	@Override
	public void paint(GraphicsPanel graphicsPanel) {
		if (crossSectionElement != null) {
			if (crossSectionElement.getVerticesInner() != null)
				;//System.out.println("no linkPointList for Turn arrow paint");
			else {
				double referencePos = longitudinalPosition;
				if (referencePos < 0)
					referencePos += crossSectionElement.getCrossSection().getLongitudinalLength();
				for (int repeat = 0; ; repeat++) {
					double fromDistance = referencePos + longitudinalRepeat * repeat;
					if (crossSectionElement.getVerticesInner() == null) {
						System.out.println("turn???");
						return;	// avoid null-pointer exception in slicePolyline
					}
					ArrayList<Vertex> slice = Planar.slicePolyline(crossSectionElement.getVerticesInner(), fromDistance, longitudinalLength);
					if (slice.size() < 2)
						return;	// improper slice
					// We only want the first and the last Vertex of the slice
					while (slice.size() > 2)
						slice.remove(1);
					slice = Planar.createParallelVertices(slice, null, lateralPosition, lateralPosition);
					graphicsPanel.setColor(Color.BLUE);
					// FIXME: draw a proper road surface arrow
					// Drawing a credible arrow is a bit hard for now
					// Especially since we don't know if the arrow should point to the left or the right, or straight on, or some combination of those.
					// For now, we'll draw a thick line segment.
					graphicsPanel.setStroke(1f);	// 1 m thick
					graphicsPanel.drawLine(slice.get(0).getPoint(), slice.get(1).getPoint());
				}
			}
		} else
			System.out.print("STRANGE!!!!!!!!!!!!!!!!, no cross section element for Turn arrow paint");
	}

	@Override
	public boolean writeXML(StaXWriter staxWriter) {
		return true;	// Currently (!) TurnArrows are generated and never saved 
	}

}
