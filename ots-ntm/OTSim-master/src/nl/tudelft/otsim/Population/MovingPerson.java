package nl.tudelft.otsim.Population;

import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.GUI.Storable;

/**
 * @author gtamminga
 * Moving Person: all the people (synthetic?) with their movement characteristics
 * to be extended 
 */
public class MovingPerson  implements Storable {
	public static final String XMLTAG = "movingPerson";
	
	private static final String XML_ID = "ID";
	private static final String XML_CARAVAILABILITY = "carAvailability";
	private static final String XML_PERSONID = "personID";
	private int ID;
	private double carAvailability;
	private Population population;
	
	public MovingPerson(Population population, int id, double carAvailability) {
		ID = id;
		this.carAvailability = carAvailability;
		this.population = population;
	}
	
	public int getID() {
		return ID;
	}
	public void setID(int id) {
		ID = id;
	}

	public double getCarAvailability() {
		return carAvailability;
	}

	public void setCarAvailability(double carAvailability) {
		this.carAvailability = carAvailability;
	}

	public Population getPopulation() {
		return population;
	}

	public void setPopulation(Population population) {
		this.population = population;
	}

	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNode(XML_PERSONID, Integer.toString(getID()));
	}

	@Override
	public String description() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String fileType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isModified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearModified() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String storageName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStorageName(String name) {
		// TODO Auto-generated method stub
		
	}
	
}