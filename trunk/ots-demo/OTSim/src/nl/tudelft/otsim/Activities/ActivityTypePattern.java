package nl.tudelft.otsim.Activities;

import java.util.ArrayList;
import java.util.LinkedList;
/**
 * Typical examples of activities in a linked pattern (like breakfast-work-shopping-dinner).
 * 
 * @author gtamminga
 */

public class ActivityTypePattern {
	/** Name for an ActivityTypePattern element when stored in XML format */
	public static final String XMLTAG = "activityTypePattern";
	/** Name for an ActivityTypePattern ID element when stored in XML format */
	public static final String XML_ID = "ID";
	/** Name for an ActivityTypePattern activity names element when stored in XML format */
	public static final String XML_ACTIVITYNAMES = "activityNames";
	
    private LinkedList<ActivityType> activityTypePattern = new LinkedList<ActivityType>();
    private ArrayList<String> activityTypePatternText;
    private Activities activities;
    private int ID;
    
    public ActivityTypePattern(Activities activities, int ID, LinkedList<ActivityType> activityList)  {
        this.activityTypePattern = activityList;
        this.ID = ID;
        this.activities = activities;
    }

    public ActivityTypePattern(Activities activities, int ID, ArrayList<String> activityList)  {
        this.activityTypePatternText = activityList;
        this.ID = ID;
        this.activities = activities;
    }
    
	public LinkedList<ActivityType> getActivityTypePattern() {
		return activityTypePattern;
	}

	public void setActivityTypePattern(LinkedList<ActivityType> activityTypePattern) {
		this.activityTypePattern = activityTypePattern;
	}

	public int getID() {
		return ID;
	}

	public void setID(int id) {
		ID = id;
	}

	public ArrayList<String> getActivityTypePatternText() {
		return activityTypePatternText;
	}

	public void setActivityTypePatternText(ArrayList<String> activityTypePatternText) {
		this.activityTypePatternText = activityTypePatternText;
	}
    
}