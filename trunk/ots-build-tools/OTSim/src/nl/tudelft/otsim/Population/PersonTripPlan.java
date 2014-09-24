package nl.tudelft.otsim.Population;

import java.util.ArrayList;
import java.util.LinkedList;

import nl.tudelft.otsim.Activities.ActivityType;
import nl.tudelft.otsim.GeoObjects.ActivityLocation;

/**
 * @author gtamminga
 * PersonTripPlan: connect a person with his/hers activities (Activitytype) and the
 * location (Building)
 * to be extended 
 */
public class PersonTripPlan {
	public static final String XMLTAG = "personTripPlan";
	public static final String XML_PERSONID = "personID";
	public static final String XML_PATTERNID = "patternID";
	public static final String XML_ACTIVITYLOCATIONLIST = "activityLocationIDList";
	private Population population;
    private ArrayList<Integer> activityLocationIDList;
    private LinkedList<ActivityLocation> activityLocationList;
    private LinkedList<ActivityType> activityTypeList;
    private int personID;
    private MovingPerson movingPerson;
    private int patternID;

	public PersonTripPlan(Population population, LinkedList<ActivityLocation> activityLocationList, MovingPerson movingPerson,
			int patternID) {
		super();
		this.activityLocationList = activityLocationList;
		this.movingPerson = movingPerson;
		this.patternID = patternID;
		this.population = population;
	}
	
	public PersonTripPlan(Population population, ArrayList<Integer> activityLocationIDList, int personID,
			int patternID) {
		super();
		this.activityLocationIDList = activityLocationIDList;
		this.personID = personID;
		this.patternID = patternID;
		this.population = population;
	}

	public ArrayList<Integer> getActivityLocationIDList() {
		return activityLocationIDList;
	}

	public void setActivityLocationIDList(ArrayList<Integer> activityLocationIDList) {
		this.activityLocationIDList = activityLocationIDList;
	}

	public LinkedList<ActivityLocation> getActivityLocationList() {
		return activityLocationList;
	}

	public void setActivityLocationList(
			LinkedList<ActivityLocation> newActivityLocationList) {
		this.activityLocationList = newActivityLocationList;
	}
	

	public Population getPopulation() {
		return population;
	}
	
	public void setPopulation(Population population) {
		this.population = population;
	}
	
	public LinkedList<ActivityType> getActivityTypeList() {
		return activityTypeList;
	}

	public void setActivityTypeList(LinkedList<ActivityType> activityTypeList) {
		this.activityTypeList = activityTypeList;
	}
	
	public int getPersonID() {
		return personID;
	}

	public void setPersonID(int personID) {
		this.personID = personID;
	}

	public int getPatternID() {
		return patternID;
	}

	public MovingPerson getMovingPerson() {
		return movingPerson;
	}

	public void setMovingPerson(MovingPerson movingPerson) {
		this.movingPerson = movingPerson;
	}

	public void setPatternID(int patternID) {
		this.patternID = patternID;
	}
	
}