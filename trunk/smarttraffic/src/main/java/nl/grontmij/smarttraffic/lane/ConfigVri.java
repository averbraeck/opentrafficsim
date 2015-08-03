package nl.grontmij.smarttraffic.lane;

import java.util.HashMap;

public class ConfigVri {

	private String name;
	private HashMap<Integer, String> detectors = new HashMap<Integer, String>();
	private HashMap<Integer, String> signalGroups = new HashMap<Integer, String>();
	
	public ConfigVri(String name, HashMap<Integer, String> detectors,
			HashMap<Integer, String> signalGroups) {
		super();
		this.name = name;
		this.detectors = detectors;
		this.signalGroups = signalGroups;
	}

	public String getName() {
		return name;
	}

	public HashMap<Integer, String> getDetectors() {
		return detectors;
	}

	public HashMap<Integer, String> getSignalGroups() {
		return signalGroups;
	}	
	
	

}
