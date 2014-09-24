package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.InputValidator;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GUI.WED;
import nl.tudelft.otsim.SpatialTools.Planar;
import nl.tudelft.otsim.Utilities.Reversed;

//TODO delete TimedMessage
//TODO export to simulator

/**
 * A Variable Message Sign (VMS) shows a time-varying message to passing traffic.
 * 
 * @author Peter Knoppers
 */
public class VMS extends CrossSectionObject implements XML_IO {
	private String ID;
	/** Label in XML representation of a VMS */
	public static final String XMLTAG = "VMS";
	
	/** Label of ID in XML representation of a VMS */
	private static final String XML_ID = "ID";
	/** Label of longitudinalPosition in XML representation of a VMS */
	private static final String XML_LONGITUDINALPOSITION = "longitudinalPosition";
	/** Label of lateralCenter in XML representation of a VMS */
	private static final String XML_LATERALPOSITION = "lateralCenter";
	/** Label of width in XML representation of a VMS */
	private static final String XML_WIDTH = "width";
	/** Label of time/text set in XML representation of a VMS */
	private static final String XML_TIMETEXT = "timeText";
	
	private TreeSet<TimedMessage> messages = new TreeSet<TimedMessage> ();

	/**
	 * Create a VMS from a parsed XML file.
	 * @param crossSectionElement {@link CrossSectionElement}; owner of the new VMS
	 * @param pn {@link ParsedNode}; the root of the VMS in the parsed XML file
	 * @throws Exception
	 */
	public VMS(CrossSectionElement crossSectionElement, ParsedNode pn) throws Exception {
		this.crossSectionElement = crossSectionElement;
		lateralReference = CrossSectionElement.LateralReferenceCenter;
		longitudinalPosition = lateralPosition = lateralWidth = Double.NaN;
		longitudinalLength = 1;
		ID = null;
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_ID))
				ID = value;
			else if (fieldName.equals(XML_LONGITUDINALPOSITION))
				longitudinalPosition = Double.parseDouble(value);
			else if (fieldName.equals(XML_LATERALPOSITION))
				lateralPosition = Double.parseDouble(value);
			else if (fieldName.equals(XML_WIDTH))
				lateralWidth = Double.parseDouble(value);
			else if (fieldName.equals(XML_TIMETEXT))
				for (int index = 0; index < pn.size(XML_TIMETEXT); index++)
					messages.add(new TimedMessage(pn.getSubNode(XML_TIMETEXT, index), this));
			else
				throw new Exception("VMS does not have a field " + fieldName);
		}
		if ((null == ID) || Double.isNaN(longitudinalPosition) || Double.isNaN(lateralPosition) || Double.isNaN(lateralWidth))
			throw new Exception("VMS is not completely defined" + pn.lineNumber + ", " + pn.columnNumber);
	}
	
	/**
	 * Create a VMS from a textual description of times and messages.
	 * @param messageList String; textual description of times and messages
	 * @throws Exception 
	 */
	public VMS(String messageList) throws Exception {
		if (messageList.length() == 0)
			return;
		String[] fields = messageList.split(",");
		for (String field : fields) {
			messages.add(new TimedMessage(field, this));
		}
	}

	/**
	 * Create a new VMS, put it somewhere on the give CrossSectionElement and give it a unique ID.
	 * @param CSE CrossSectionElement; the CrossSectionElement that will own the new VMS
	 */
	public VMS(CrossSectionElement CSE) {
		longitudinalPosition = CSE.getCrossSection().getLongitudinalLength() / 2;	// put it half way
		lateralReference = CrossSectionElement.LateralReferenceCenter;
		lateralPosition = 0;
		lateralWidth = CSE.getWidth_r();
		longitudinalLength = 1;
		for (int idRank = 1; null == ID; idRank++) {
			ID = "" + idRank;
			for (CrossSectionObject cso : CSE.getCrossSectionObjects(VMS.class))
				if (((VMS) cso).ID.equals(ID)) {
					ID = null;	// try the next possible value
					break;
				}
		}
		this.crossSectionElement = CSE;
	}

	/**
	 * Return the ID of this VMS.
	 * @return String; the ID of this VMS
	 */
	public String getID_r() {
		return ID;
	}
	
	/**
	 * Change the ID of this VMS.
	 * @param newName String; the new name for this VMS
	 */
	public void setID_w(String newName) {
		this.ID = newName;
		crossSectionElement.getCrossSection().getLink().network.setModified();
	}
	
	/**
	 * Create an {@link InputValidator} that ensures a proper ID for this VMS.
	 * @return {@link InputValidator} for a proper VMS ID
	 */
	public InputValidator validateID_v() {
		return new InputValidator(new InputValidator.CustomValidator() {
			@Override
			public boolean validate(String originalValue, String proposedValue) {
				if (! proposedValue.matches("[a-zA-Z_][a-zA-Z0-9_.]*"))
					return false;	// not a decent name
				if (proposedValue.equals(originalValue))
					return true;	// current name is OK
				// Anything else must be unique among the VMS's in the Network
				return null == crossSectionElement.getCrossSection().getLink().network.lookupVMS(proposedValue);
			}
		});
	}
	
	/**
	 * Retrieve the lateral position of this VMS.
	 * @return Double; the lateral position of this VMS in m from
	 * the center line of the parent {@link CrossSectionElement}
	 */
	public double getLateralPosition_r() {
		return lateralPosition;
	}
	
	/**
	 * Change the lateral position of this VMS.
	 * @param lateralPosition Double; the new lateral position in m from the
	 * center line of the parent (@link CrossSectionElement}
	 */
	public void setLateralPosition_w(double lateralPosition) {
		this.lateralPosition = lateralPosition;
		crossSectionElement.getCrossSection().getLink().network.setModified();
	}
	
	/**
	 * Return an {@link InputValidator} for the lateral position of this VMS.
	 * @return {@link InputValidator} for the lateral position of this VMS
	 */
	public InputValidator validateLateralPosition_v() {
		double range = crossSectionElement.getWidth_r() - lateralWidth;
		return new InputValidator("[-.0-9].*", -range / 2, range / 2);
	}
	
	/**
	 * Retrieve the lateral width of this VMS.
	 * @return Double; the lateral width of this VMS in m
	 */
	public double getWidth_r() {
		return lateralWidth;
	}
	
	/**
	 * Change the lateral width of this VMS.
	 * @param width Double; the new lateral width of this VMS in m
	 */
	public void setWidth_w(double width) {
		lateralWidth = width;
		crossSectionElement.getCrossSection().getLink().network.setModified();
	}
	
	/**
	 * Return an {@link InputValidator} for the lateral width of this VMS.
	 * @return {@link InputValidator} for the lateral width of this VMS
	 */
	public InputValidator validateWidth_v() {
		double limit = crossSectionElement.getWidth_r() - Math.abs(lateralPosition);
		return new InputValidator("[.0-9].*", 0.1, limit);
	}
	
	/**
	 * Return the Vertices that form the outline of the detection area of this
	 * VMS
	 * @return ArrayList&lt;{@link Vertex}&gt; vertices of the polygon of this 
	 * VMS
	 */
	public ArrayList<Vertex> getPolygon_r() {
		ArrayList<Vertex> guideLine = Planar.slicePolyline(crossSectionElement.getLinkPointList(lateralReference, true, false), longitudinalPosition, longitudinalLength);
		ArrayList<Vertex> result = Planar.createParallelVertices(guideLine, null, -lateralWidth / 2,  -lateralWidth / 2);
		for (Vertex v : Reversed.reversed(Planar.createParallelVertices(guideLine, null, lateralWidth / 2, lateralWidth / 2)))
			result.add(v);
		return result;
	}
	
	/**
	 * Retrieve the longitudinalPosition of this VMS.
	 * @return Double; the longitudinalPosition of this VMS
	 */
	public double getLongitudinalPosition_r() {
		return longitudinalPosition;
	}
	
	/**
	 * Change the longitudinalPosition of this VMS.
	 * @param longitudinalPosition Double; the new longitudinalPosition of this
	 * VMS
	 */
	public void setLongitudinalPosition_w(double longitudinalPosition) {
		this.longitudinalPosition = longitudinalPosition;
		crossSectionElement.getCrossSection().getLink().network.setModified();
	}
	
	/**
	 * Validate a proposed longitudinalPosition for this VMS.
	 * @return InputValidator for proposed values of the longitudinalPosition 
	 * of this VMS
	 */
	public InputValidator validateLongitudinalPosition_v() {
		double length = crossSectionElement.getCrossSection().getLongitudinalLength();
		return new InputValidator("[-.,0-9].*", -length, length);
	}
	
	/**
	 * A VMS can always be deleted.
	 * <br /> This method is only used by the {@link nl.tudelft.otsim.GUI.ObjectInspector}.
	 * @return Boolean; always true
	 */
	@SuppressWarnings("static-method")
	public boolean mayDeleteVMS_d() {
		return true;
	}
	
	/**
	 * Delete this VMS.
	 */
	public void deleteVMS_d() {
		crossSectionElement.deleteCrossSectionObject(this);
	}
	
	@Override
	public String toString() {
		String messageString = "";
		String separator = "";
		for (TimedMessage tm : messages) {
			messageString += separator + tm.toString();
			separator = ", ";
		}
		if (null != crossSectionElement)
			return String.format(Main.locale, "VMS %s at longitudinalPosition %.3fm, width %.3fm: %s", ID, longitudinalPosition, lateralWidth, messageString);
		return String.format(Main.locale, "VMS: " + messageString);
	}
	
	/**
	 * Create a string representation of the messages this VMS suitable for import
	 * @return String; the string representation of the messages of this VMS
	 */
	public String export () {
		String result = "";
		String separator = "";
		for (TimedMessage tm : messages) {
			try {
				result += separator + tm.export();
			} catch (UnsupportedEncodingException e) {
				WED.showProblem(WED.PROGRAMERROR, "Cannot encode message \"%s\" for export", tm.getMessage_r());
				e.printStackTrace();
			}
			separator = ",";
		}
		return result;
	}
	
	@Override
	public void paint(GraphicsPanel graphicsPanel) {
		graphicsPanel.setStroke(0F);
		graphicsPanel.setColor(Color.BLUE);
		ArrayList<Vertex> polygon = getPolygon_r();
		//System.out.println("polygon is " + GeometryTools.verticesToString(polygon));
		if (polygon.size() > 0)
			graphicsPanel.drawPolygon(polygon.toArray());
	}
	
	private boolean writeMessages(StaXWriter staXWriter) {
		for (TimedMessage tm : messages)
			if (! tm.writeXML(staXWriter))
				return false;
		return true;
	}

	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& staXWriter.writeNode(XML_ID, getID_r())
				&& staXWriter.writeNode(XML_LATERALPOSITION, Double.toString(lateralPosition))
				&& staXWriter.writeNode(XML_WIDTH, Double.toString(lateralWidth))
				&& staXWriter.writeNode(XML_LONGITUDINALPOSITION, Double.toString(longitudinalPosition))
				&& writeMessages(staXWriter)
				&& staXWriter.writeNodeEnd(XMLTAG);
	}

	/**
	 * Retrieve a list of all messages
	 * @return ArrayList&lt;{@link TimedMessage}&gt;; the list of all messages
	 */
	public ArrayList<TimedMessage> getTimedMessages_r() {
		ArrayList<TimedMessage> result = new ArrayList<TimedMessage>(messages);
		return result;
	}
	
	/**
	 * Return a fixed string to be used to label the tree node in the
	 * {@link nl.tudelft.otsim.GUI.ObjectInspector} that must be clicked to add a
	 * {@link TimedMessage} to this VMS.
	 * @return String; <code>"new timed message"</code>
	 */
	@SuppressWarnings("static-method")
	public String getAdd_r () {
		return "new timed message";
	}
	
	/**
	 * Add a new TimedMessage with a given text and a reasonable start time.
	 * @param text String; ignored
	 * @throws Exception 
	 */
	public void setAdd_w (String text) throws Exception {
		double startTime = 0d;
		for (TimedMessage tm : messages)
			if (tm.getTime() >= startTime)
				startTime = tm.getTime() + 1;
		messages.add(new TimedMessage(startTime, text, this));
		crossSectionElement.getCrossSection().getLink().network.setModified();
	}
	
	/**
	 * Add a TimedMessage to this VMS.
	 * @param time Double; the time when the new message must be shown
	 * @param message String; the text of the message
	 * @throws Exception 
	 */
	public void addMessage(Double time, String message) throws Exception {
		messages.add(new TimedMessage(time, message, this));
	}
	
	/**
	 * Retrieve the message that is shown at the specified time.
	 * @param time Double; the time [s] for which the message must be retrieved
	 * @return String; the message that is shown at the specified time
	 */
	public String message(Double time) {
    	String result = "";
    	for (TimedMessage tm : messages)
    		if (tm.getTime() <= time)
    			result = tm.getMessage_r();
    	return result;

	}

	/**
	 * Simple fixed message that is displayed at a specified time
	 * 
	 * @author Peter Knoppers
	 */
	public static class TimedMessage implements Comparable<TimedMessage>, XML_IO {
		/** Label of a time in XML representation of a VMS time/text pair */
		private static final String XML_TIME = "time";
		/** Label of a text in XML representation of a VMS time/text pair */
		private static final String XML_TEXT = "base64Text";
		private double time;
		private String message;
		private final VMS parent;
		
		@Override
		public int compareTo(TimedMessage other) {
			if (other.time > time)
				return -1;
			else if (other.time < time)
				return 1;
			return 0;
		}
		
		/**
		 * Convert this TimedMessage to a String representation suitable for import
		 * @return String; the encoded form of this TimedMessage
		 * @throws UnsupportedEncodingException
		 */
		public String export() throws UnsupportedEncodingException {
			return String.format(Locale.US, "%.3f:%s", getTime(), encode(message));
		}

		/**
		 * Create a TimedMessage.
		 * @param time Double; time [s] when the message is displayed
		 * @param message String; the message to display
		 * @param parent VMS; the VMS that owns this TimedMessage
		 * @throws Exception 
		 */
		public TimedMessage(Double time, String message, VMS parent) throws Exception {
			this.parent = parent;	// must be done before calling setTime
			setTime(time);
			this.message = message;
		}
		
		/**
		 * Create a TimedMessage from its String representation.
		 * @param text String; the String representation
		 * @param parent VMS; the VMS that owns this TimedMessage
		 * @throws UnsupportedEncodingException
		 */
		public TimedMessage(String text, VMS parent) throws UnsupportedEncodingException {
			this.parent = parent;	// must be done before calling setTime
			String[] subFields = text.split(":");
			time = Double.parseDouble(subFields[0]);
			if (1 == subFields.length)
				message = "";
			else
				message = decode(subFields[1]);
		}
		
		/**
		 * Create a TimedMessage from a {@link ParsedNode}.
		 * @param pn {@link ParsedNode}; the XML node with the data to use
		 * @param parent VMS; the VMS that owns this TimedMessage
		 * @throws Exception 
		 */
		public TimedMessage(ParsedNode pn, VMS parent) throws Exception {
			this.parent = parent;	// must be done before calling setTime
			this.time = Double.NaN;
			this.message = null;
			for (String fieldName : pn.getKeys()) {
				String value = pn.getSubNode(fieldName, 0).getValue();
				if (fieldName.equals(XML_TIME))
					setTime (Double.parseDouble(value));
				else if (fieldName.equals(XML_TEXT)) {
					if (null == value)
						this.message = "";
					else
						this.message = decode(value);
				} else
					throw new Exception("TimedMessage does not have a field " + fieldName + " (near " + pn.description() + ")");
			}
			if (Double.isNaN(this.time))
				throw new Exception("Time not specified near " + pn.description());
			if (null == message)
				throw new Exception("Message not specified near " + pn.description());
		}
		
		/**
		 * Modify the time at which this TimedMessage is displayed
		 * @param newTime Double; the time [s] when the message is displayed
		 * @throws Exception 
		 */
		public void setTime (Double newTime) throws Exception { 
			if (newTime < 0d)
				throw new Exception("Bad time (must be >= 0; got " + newTime + ")");
			this.time = newTime;
			// Ensure this gets correctly sorted with the new time
			parent.messages.remove(this);
			parent.messages.add(this);
		};
		
		/**
		 * Modify the text that is displayed.
		 * @param newMessage String; the text that is displayed
		 * @throws Exception 
		 */
		public void setMessage_w (String newMessage) throws Exception {
			encode (newMessage);	// verify that newMessage can be encoded
			this.message = newMessage;
		};
		
		/**
		 * Retrieve the time at which this TimedMessage is displayed.
		 * @return Double; the time [s] at which this TimedMessage is displayed
		 */
		public Double getTime () { return time; };
		
		/**
		 * Retrieve the text that is displayed
		 * @return String; the text that is displayed
		 */
		public String getMessage_r () { return message; };
		
		@Override
		public String toString() {
			if (time < 0)
				return message;
			return String.format (Main.locale, "%.3f: \"%s\"", time, message);
		}
		
		/**
		 * Retrieve a string representation of the time at which this 
		 * TimedMessage is displayed.
		 * @return String; a string representation of the time at which this
		 * TimedMessage is displayed
		 */
		public String getTime_r () {
			return String.format (Main.locale, "%.3f", time);
		}
		
		/**
		 * Change the time at which this TimedMessage is displayed.
		 * @param newTime Double; the new value for the time at which this
		 * TimedMessage is displayed
		 * @throws Exception 
		 */
		public void setTime_w (String newTime) throws Exception {
			setTime (Double.parseDouble(Planar.fixRadix(newTime)));
		}
		
		/**
		 * Return an {@link InputValidator} for the time.
		 * @return {@link InputValidator}; InputValidator for the time
		 */
		@SuppressWarnings("static-method")
		public InputValidator validateTime_v () {
			return new InputValidator(new InputValidator.CustomValidator () {
				@Override
				public boolean validate(String originalValue, String proposedValue) {
					try {
						return Double.parseDouble(Planar.fixRadix(proposedValue)) >= 0;
					} catch (Exception e) {
						return false;
					}
				}
				
			});
		}
			
		/**
		 * Return an {@link InputValidator} for the text.
		 * @return {@link InputValidator} for the text
		 */
		@SuppressWarnings("static-method")
		public InputValidator validateMessage_v () {
			return new InputValidator(".*");
		}
		
		/**
		 * A TimedMessage can always be deleted.
		 * <br /> This method is only used by the {@link nl.tudelft.otsim.GUI.ObjectInspector}.
		 * @return Boolean; always true
		 */
		@SuppressWarnings("static-method")
		public boolean mayDeleteMessage_d() { 
			return true;
		}

		@Override
		public boolean writeXML(StaXWriter staXWriter) {
			try {
				return staXWriter.writeNodeStart(XML_TIMETEXT)
						&& staXWriter.writeNode(XML_TIME, String.format(Locale.US, "%.3f",  time))
						&& staXWriter.writeNode(XML_TEXT, encode(message))
						&& staXWriter.writeNodeEnd(XML_TIMETEXT);
			} catch (UnsupportedEncodingException e) {
				WED.showProblem(WED.PROGRAMERROR, "Cannot encode message \"%s\" for write", message);
				e.printStackTrace();
				return false;
			}
		}
		
		/*
		 * Base 64 encoding and decoding will be standard in Java 8; for now we'll have to use this
		 */
		private static String encode (String string) throws UnsupportedEncodingException {
			return javax.xml.bind.DatatypeConverter.printBase64Binary(string.getBytes("UTF-8"));
		}
		
		private static String decode (String string) throws UnsupportedEncodingException {
			return new String(javax.xml.bind.DatatypeConverter.parseBase64Binary(string), "UTF-8");
		}
		
	}
}

