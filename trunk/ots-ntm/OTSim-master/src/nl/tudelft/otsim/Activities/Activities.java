package nl.tudelft.otsim.Activities;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author gtamminga
 * The activities describe the motivations for movement. This package contains 
 * information on the activity patterns and their impact on trips.
 */
public class Activities {
	/** Name of an activity when stored in XML format */
	public static final String XMLTAG = "activities";
	/** List of patterns of activity (like breakfast work shop dinner */
	public ArrayList<ActivityTypePattern> activityTypePatternList = new ArrayList<ActivityTypePattern>();
	/** List of activity types and their characteristics (like start-time, duration, time-fence of occurrence )*/
	public ArrayList<ActivityType> activityTypeList = new  ArrayList<ActivityType>();
	
    /**
     * Constructor that describes the elements of activities.<br>
     * <br><pre>
     * ArrayList<ActivityTypePattern> 	"activityTypePatternList"
     * ArrayList<ActivityType> 			"activityTypeList" </pre> 
     */
	public Activities() {
		
	}

	public void addActivityType(String name, String building, String startTime,
			String varianceStartTime, String timeWindowStart,
			String timeWindowEnd, String duration) {
		try {
			this.activityTypeList.add(new ActivityType(this, name, building, startTime,
					varianceStartTime, timeWindowStart,
					timeWindowEnd, duration));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}
	
	public void addActivityTypePattern(int ID, String activityNames) {
		String[] splitted = activityNames.split("\\s");
		ArrayList<String> activityList = new ArrayList<String>();
		for (String location : splitted) 
			activityList.add(location);
		this.activityTypePatternList.add(new ActivityTypePattern(this, ID, activityList));
	}

	/** Rebuild: generate a list of activity related objects/lists*/
	public void rebuild()   {
		fixActivityTypePattern();
		}
	
	/** generate a list of patterns of activity (like breakfast work shop dinner */
	void fixActivityTypePattern()   {
		for (ActivityTypePattern activityTypePattern : activityTypePatternList)  {
			LinkedList<ActivityType> newActivityList = new LinkedList<ActivityType>();
			for (String activityName : activityTypePattern.getActivityTypePatternText())  {
				ActivityType activityType = lookupActivityType(activityName);
				newActivityList.add(activityType);
			}
			activityTypePattern.setActivityTypePattern(newActivityList);			 
		}
	}
	
	public LinkedList<ActivityType> lookupPatternList(int patternID)  {
		for (ActivityTypePattern activityTypePattern : activityTypePatternList)
			if (activityTypePattern.getID() == patternID)
				return activityTypePattern.getActivityTypePattern();
		return null;		
	}
	
	public ActivityType lookupActivityType(String name) {
		for (ActivityType activityType : activityTypeList)
			if (activityType.getName().equals(name))
				return activityType;
		return null;
	}
	
}
