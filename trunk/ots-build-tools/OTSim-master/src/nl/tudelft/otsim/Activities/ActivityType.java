package nl.tudelft.otsim.Activities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.ParseException;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.GUI.Main;

/**
 * The ActivityType defines the main characteristics of an activity (like sleeping, working, having dinner).
 * Such as start time, duration of the activity and the time window during which the activity can be performed
 * Should be extendible for more elaborated applications
 * The class Activity uses constructs of the ActivityType
 * 
 */

public class ActivityType {
	/** Name for an ActivityType element when stored in XML format */
	public static final String XMLTAG = "activityType";
	/** Name for an ActivityType ID element when stored in XML format */
	public static final String XML_ID = "ID";
	/** Name for an ActivityType name element when stored in XML format */
	public static final String XML_NAME = "name";
	/** Name for an ActivityType building element when stored in XML format */
	public static final String XML_BUILDING = "building";
	/** Name for an ActivityType startTime element when stored in XML format */
	public static final String XML_STARTTIME = "startTime";
	/** Name for an ActivityType start time variance element when stored in XML format */
	public static final String XML_VARIANCESTARTTIME = "varianceStartTime";	
	/** Name for an ActivityType time window start element when stored in XML format */
	public static final String XML_TIMEWINDOWSTART = "timeWindowStart";
	/** Name for an ActivityType time window end element when stored in XML format */
	public static final String XML_TIMEWINDOWEND = "timeWindowEnd";
	/** Name for an ActivityType duration element when stored in XML format */
	public static final String XML_DURATION = "duration";	
	
	private int ID;
	private String name;
	/** name of the building (which type) */
	private String building;
	/** mean preferred starting time, with a distribution (variance) around the mean*/
	private Date preferredStartTime;
	/** variance around mean starting time*/
	private Date varianceStartTime;	
	/** start of the "time fence of occurrency" of this activity type*/	
	private Date timeWindowStart;
	private Date timeWindowEnd;
	/** duration of the activity*/
	private Date duration;
	private SimpleDateFormat formatter;
	private DateFormat timeFormatter; 
	private Activities activities;
	
	/**
	 * Create a new ActivityType.
	 * @param activities
	 * @param name
	 * @param building
	 * @param startTime
	 * @param varianceStart
	 * @param timeWindowStart
	 * @param timeWindowEnd
	 * @param duration
	 * @throws ParseException
	 */
	public ActivityType(Activities activities, String name, String building, String startTime, 
			String varianceStart, String timeWindowStart,String timeWindowEnd, String duration) throws ParseException  {
		super();
		// FIXME: format of time in the files should NOT be locale dependent
		Locale locale = Main.locale;
		formatter = new SimpleDateFormat("hh:mm");
		timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);
		this.name = name;
		this.preferredStartTime = formatter.parse(startTime);
		System.out.println(timeFormatter.format(this.preferredStartTime ));		
		this.varianceStartTime = formatter.parse(varianceStart);
		this.timeWindowStart = formatter.parse(timeWindowStart);
		this.timeWindowEnd = formatter.parse(timeWindowEnd);
		this.duration = formatter.parse(duration);
		this.activities = activities;
	}

	/**
	 * Create an ActivityType from a parsed XML file.
	 * @param pn {@link ParsedNode}; root of the ActivityType in the parsed XML file
	 * @throws Exception
	 */
	public ActivityType(ParsedNode pn) throws Exception {
		name = building = null;
		preferredStartTime = varianceStartTime = timeWindowStart = timeWindowEnd = duration = null;
		activities = null;
		// FIXME: format of time in the files should NOT be locale dependent
		Locale locale = Main.locale;
		formatter = new SimpleDateFormat("hh:mm");
		timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);
		ID = -1;
		
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_NAME))
				name = value;
			else if (fieldName.equals(XML_ID))
				ID = Integer.parseInt(value);
			else if (fieldName.equals(XML_BUILDING))
				building = value;
			else if (fieldName.equals(XML_STARTTIME))
				preferredStartTime = formatter.parse(value);
			else if (fieldName.equals(XML_VARIANCESTARTTIME))
				varianceStartTime = formatter.parse(value);
			else if (fieldName.equals(XML_TIMEWINDOWSTART))
				timeWindowStart = formatter.parse(value);
			else if (fieldName.equals(XML_TIMEWINDOWEND))
				timeWindowEnd = formatter.parse(value);
			else if (fieldName.equals(XML_DURATION))
				duration = formatter.parse(value);
			else
				throw new Exception("Unknown field in ActivityType: " + fieldName + " at " + pn.lineNumber + ", " + pn.columnNumber);
		}
		if (null == name)
			throw new Exception("ActivityType has no name " + pn.lineNumber + ", " + pn.columnNumber);
		if (-1 == ID)
			throw new Exception("ActivityType has no ID " + pn.lineNumber + ", " + pn.columnNumber);
		if (null == preferredStartTime)
			throw new Exception("ActivityType has no preferredStartTime " + pn.lineNumber + ", " + pn.columnNumber);
		if (null == varianceStartTime)
			throw new Exception("ActivityType has no varianceStartTime " + pn.lineNumber + ", " + pn.columnNumber);
		if (null == timeWindowStart)
			throw new Exception("ActivityType has no timeWindowStart " + pn.lineNumber + ", " + pn.columnNumber);
		if (null == timeWindowEnd)
			throw new Exception("ActivityType has no timeWindowEnd " + pn.lineNumber + ", " + pn.columnNumber);
		if (null == duration)
			throw new Exception("ActivityType has no duration " + pn.lineNumber + ", " + pn.columnNumber);
	}

	public int getID() {
		return ID;
	}
	
	public void setID(int iD) {
		ID = iD;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getBuilding() {
		return building;
	}
	
	public void setBuilding(String building) {
		this.building = building;
	}
	
	public Date getPreferredStartTime() {
		return preferredStartTime;
	}
	
	public void setPreferredStartTime(Date preferredStartTime) {
		this.preferredStartTime = preferredStartTime;
	}
	
	public Date getVarianceStartTime() {
		return varianceStartTime;
	}
	
	public void setVarianceStartTime(Date varianceStartTime) {
		this.varianceStartTime = varianceStartTime;
	}
	
	public Date getTimeWindowStart() {
		return timeWindowStart;
	}
	
	public void setTimeWindowStart(Date timeWindowStart) {
		this.timeWindowStart = timeWindowStart;
	}
	
	public Date getTimeWindowEnd() {
		return timeWindowEnd;
	}
	
	public void setTimeWindowEnd(Date timeWindowEnd) {
		this.timeWindowEnd = timeWindowEnd;
	}
	
	public Date getDuration() {
		return duration;
	}
	
	public void setDuration(Date duration) {
		this.duration = duration;
	}
	
}