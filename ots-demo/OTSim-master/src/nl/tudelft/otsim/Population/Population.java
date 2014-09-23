package nl.tudelft.otsim.Population;

import java.util.ArrayList;
import java.util.LinkedList;

import nl.tudelft.otsim.Activities.ActivityType;
import nl.tudelft.otsim.GUI.Model;
import nl.tudelft.otsim.GeoObjects.ActivityLocation;

/**
 * @author gtamminga
 * The Population provides information about inhabitants and their relation with the activities. 
 */
public class Population {
	public static final String XMLTAG = "population";
    private ArrayList<MovingPerson>	movingPersonList = new ArrayList<MovingPerson>(); 
    private ArrayList<PersonTripPlan> personTripPlanList = new ArrayList<PersonTripPlan>(); 
    private Model model;
	
	public Population(Model model) {
		this.model = model;
	}
	
	public void addMovingPerson(int ID, double carAvailability) {
		this.movingPersonList.add(new MovingPerson(this, ID, carAvailability));
	}

	public void addPersonTripPlan(String locationList, int personID, int patternID) {
		String[] splitted = locationList.split("\\s");
		ArrayList<Integer> locList = new ArrayList<Integer>();
		for (String location : splitted)
			locList.add(Integer.parseInt(location));
		this.personTripPlanList.add(new PersonTripPlan(this, locList, personID, patternID));
	}

	public ArrayList<MovingPerson> getMovingPersonList() {
		return movingPersonList;
	}

	public void setMovingPersonList(ArrayList<MovingPerson> movingPersonList) {
		this.movingPersonList = movingPersonList;
	}

	public ArrayList<PersonTripPlan> getPersonTripPlanList() {
		return personTripPlanList;
	}

	public void setPersonTripPlanList(ArrayList<PersonTripPlan> personTripPlanList) {
		this.personTripPlanList = personTripPlanList;
	}
	
	public void rebuild() {
		fixPersonTripPlan();		
	}
	
	public MovingPerson lookupMovingPerson(int personID)  {
		for (MovingPerson movingPerson : movingPersonList)
			if (movingPerson.getID() == personID)
				return movingPerson;
		return null;		
	}
	
	void fixPersonTripPlan()   {
		for (PersonTripPlan personTripPlan : personTripPlanList)  {
			LinkedList<ActivityLocation> newBuildingList = new LinkedList<ActivityLocation>();
			for (Integer activityLocationID : personTripPlan.getActivityLocationIDList())  {
				//TODO GUUS
				//ActivityLocation activityLocation = model.network.lookupActivityLocationID(activityLocationID);
				//newBuildingList.add(activityLocation);
			}
			personTripPlan.setActivityLocationList(newBuildingList);
			
			int patternID = personTripPlan.getPatternID();
			LinkedList<ActivityType> activityPattern = model.activities.lookupPatternList(patternID);
			personTripPlan.setActivityTypeList(activityPattern);

			int personID = personTripPlan.getPersonID();
			MovingPerson movingPerson = model.population.lookupMovingPerson(personID);
			personTripPlan.setMovingPerson(movingPerson);
		}
	}
	
}