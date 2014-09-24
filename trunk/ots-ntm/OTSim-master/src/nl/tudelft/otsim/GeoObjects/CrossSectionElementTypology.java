package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;

/**
 * CrossSectionElementTypology implements common properties of CrossSectionElements. 
 * @author gtamminga, Peter Knoppers
 *
 */
public class CrossSectionElementTypology implements XML_IO {
	/** Label in XML representation of a CrossSectionElementTypology */
	public static final String XMLTAG = "crossSectionElementTypology";
	/** Assign coupling priority base on the name (when creating a new CrossSectionElementTypology) */
	public static final int COUPLING_PRIORITY_AUTOMATIC = -1;
	/** Default coupling priority for grass */
	public static final int COUPLING_PRIORITY_GRASS = 50;
	/** Default coupling priority for barriers */
	public static final int COUPLING_PRIORITY_BARRIER = 40;
	/** Default coupling priority for foot paths */
	public static final int COUPLING_PRIORITY_FOOTPATH = 30;
	/** Default coupling priority for cycle paths */
	public static final int COUPLING_PRIORITY_CYCLISTS = 20;
	/** Default coupling priority for roadways */
	public static final int COUPLING_PRIORITY_ROADWAY = 10;
	
	/** Label of name in XML representation of a CrossSectionElementTypology */
	private static final String XML_NAME = "name";
	/** Label of drivable in XML representation of a CrossSectionElementTypology */
	private static final String XML_DRIVABLE = "drivable";
	private String name;
	private boolean drivable;
	private int couplingPriority;
	
	/**
	 * Set/change the coupling priority of this CrossSectionElementTypology.
	 * <br /> The coupling priority indicates how important it is that
	 * {@link CrossSectionElement CrossSectionElements} in successive
	 * {@link CrossSection CrossSections} are connected.
	 * <br /> Lower values indicate a higher priority.
	 * @param couplingPriority Integer; the new couplingPriority for this
	 * CrossSectionElementTypology
	 */
	public void setCouplingPriority(int couplingPriority) {
		this.couplingPriority = couplingPriority;
	}

	// from the city GML descriptions
	/**
	 * enum used to describe intended use of a CrossSectionElementTypology.
	 * <br /> These options are derived from the city GML descriptions.
	 * 
	 * @author gtamminga, Peter Knoppers
	 */
	public enum TrafficAreaUsageType {
		/** Intended use is walking */
		pedestrian, 
		/** Intended use is private cars */
		car, 
		/** Intended use is trucking */
		truck, 
		/** Intended use is buses and taxis */
		bus_taxi, 
		/** Intended use is trains */
		train, 
		/** Intended use is cycling */
		bicycle, 
		/** Intended use is motor cycling */
		motorcycle, 
		/** Intended use is tram, or streetcar */
		tram_streetcar,
		/** Intended use is ferry */
		boat_ferry_ship, 
		/** Intended use is airial tram */
		teleferic, 
		/** Intended use is airplanes */
		aeroplane, 
		/** Intended use is helicopter pad */
		helicopter, 
		/** Intended use is taxis */
		taxi, 
		/** Intended use is horse driving */
		horse 
	};
	
	/**
	 * Create a CrossSectionElementTypology with name, drivable property and
	 * couplingPriority.
	 * @param name String; name of the new CrossSectionElementTypology
	 * @param drivable Boolean; true if vehicles are allowed to drive on
	 * CrossSections with this CrossSectionElementTypology
	 * @param couplingPriority Integer; importance of connecting 
	 * CrossSectionElements with this CrossSectionElementTypology to a
	 * compatible CrossSectionElement in adjacent CrossSections. Lower values
	 * indicate higher importance. Provide the value COUPLING_PRIORITY_AUTOMATIC
	 * to use automatic setting based on the name
	 */
	public CrossSectionElementTypology(String name, boolean drivable, int couplingPriority) {
		this.name = name;
		this.drivable = drivable;
		if (COUPLING_PRIORITY_AUTOMATIC == couplingPriority) {
			if (name.equals("grass"))
				couplingPriority = COUPLING_PRIORITY_GRASS;
			else if (name.equals("barrier"))
				couplingPriority = COUPLING_PRIORITY_BARRIER;
			else if (name.equals("footpath"))
				couplingPriority = COUPLING_PRIORITY_FOOTPATH;
			else if (name.equals("bicycles"))
				couplingPriority = COUPLING_PRIORITY_CYCLISTS;
			else if (name.equals("road"))
				couplingPriority = COUPLING_PRIORITY_ROADWAY;
			else
				couplingPriority = 0;
		}
		this.couplingPriority = couplingPriority;
	}	
	
	/**
	 * Create a CrossSectionElementTypology from a parsed XML file.
	 * @param pn {@link ParsedNode}; root of the CrossSectionElementTypology in the XML file
	 * @throws Exception
	 */
	public CrossSectionElementTypology(ParsedNode pn) throws Exception {
    	name = null;
    	drivable = false;
    	couplingPriority = CrossSectionElementTypology.COUPLING_PRIORITY_AUTOMATIC;
    	
    	//System.out.print("\n" + fields.toString("crossSection"));
		for (String fieldName : pn.getKeys()) {
			if (pn.size(fieldName) != 1)
				throw new Exception("Field " + fieldName + " has " + pn.size(fieldName) + "elements (should be 1)");
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (null == value)
				throw new Exception("Value of " + fieldName + " is null");
			if (fieldName.equals(XML_NAME))
				name = value;
			else if (fieldName.equals(XML_DRIVABLE))
				drivable = Boolean.parseBoolean(value);
			else
				throw new Exception("Unknown field in CrossSectionElementTypology: \"" + fieldName + "\"");
		}
		if (null == name)
			throw new Exception("Name of CrossSectionElementTypology not defined");
	}

	public CrossSectionElementTypology() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Generate a textual description of this CrossSectionElementTypology.
	 */
	@Override
	public String toString() {
		return String.format("%s%s", name, drivable ? " (drivable)" : "");
	}

	/**
	 * Retrieve the drivable property of this CrossSectionElementTypology.
	 * @return Boolean; true if vehicles are allowed to drive on
	 * CrossSections with this CrossSectionElementTypology; false otherwise
	 */
	public boolean getDrivable() {
		return drivable;
	}
	
	/**
	 * Modify the drivable property of this CrossSectionElementTypology.
	 * @param drivable Boolean; true if vehicles are allowed to drive on
	 * CrossSections with this CrossSectionElementTypology; false otherwise
	 */
	public void setDrivable(Boolean drivable) {
		this.drivable = drivable;
	}
	
	/**
	 * Retrieve the name of this CrossSectionElementTypology.
	 * @return String; the name of this CrossSectionElementTypology
	 */
	public String getName_r() {
		return name;
	}
	
	/**
	 * Modify the name of this CrossSectionElementTypology/
	 * @param crossSectionElementName String; the new name. The
	 * couplingPriority of this CrossSectionElementTypology is <b>not</b>
	 * updated.
	 */
	public void setCrossSectionElementName(String crossSectionElementName) {
		this.name = crossSectionElementName;
	}
	
	/**
	 * Retrieve the couplingPriority of this CrossSectionElementTypology.
	 * @return Integer; the couplingPriority
	 */
	public int getCouplingPriority() {
		return couplingPriority;
	}

	// from the city GML descriptions
	enum TrafficAreaFunctionType {
		driving_lane,
		footpath, 
		cyclepath, 
		combined_foot_cyclepath, 
		square,
		car_park, 
		parking_lay_by, 
		rail,
		rail_road_combined, 
		drainage,
		road_marking, 
		road_marking_direction, 
		road_marking_lane, 
		road_marking_restricted, 
		road_marking_crosswalk, 
		road_marking_stop, 
		road_marking_other, 
		overhead_wire_trolley, 
		train_platform,
		crosswalk, 
		barrier, 
		stairs, 
		escalator, 
		filtering_lane, 
		airport_runway, 
		airport_taxiway, 
		airport_apron, 
		airport_heliport, 
		airport_runway_marking, 
		green_spaces, 
		recreation, 
		bus_lay_by, 
		motorway, 
		motorway_entry, 
		motorway_exit, 
		motorway_emergency_lane, 
		private_area 
	}; 

	enum TransportationComplexFunction {
		road,
		freeway_motorway, 
		highway_national_primary_road, 
		land_road, 
		district_road, 
		main_through_road1, 
		main_through_road2, 
		freeway_interchange_highway_junction, 
		junction, 
		road2, 
		driveway, 
		footpath_footway, 
		hiking_trail, 
		bikeway_cycle_path, 
		bridleway_bridlepath, 
		main_agricultural_road, 
		agricultural_road, 
		bikeway_footway, 
		dead_end_road1, 
		dead_end_road2, 
		lane, 
		lane_one_direction, 
		lane_both_direction, 
		pedestrian_zone, 
		place, 
		parking_area, 
		marketplace, 
		service_area, 
		rail_transport, 
		rail, 
		urban_city_train, 
		city_railway, 
		tram, 
		subway, 
		funicular_mountain_railway, 
		mountain_railway, 
		chairlift, 
		ski_lift_ski_tow_lift,
		suspension_railway, 
		railway_track, 
		magnetic_levitation_train, 
		railway_station, 
		stop, 
		station, 
		power_wheel, 
		airport1, 
		international_airport, 
		regional_airport, 
		landing_place1, 
		heliport, 
		landing_place2, 
		gliding_airfield, 
		taxiway, 
		apron, 
		runway, 
		canal, 
		harbor, 
		pleasure_craft_harbour, 
		ferry, 
		car_ferry, 
		train_ferry, 
		ferry1, 
		landing_stage, 
		waterway_I_order, 
		navigable_river, 
		inland_navigation_waterway, 
		inland_navigation_waterway_0, 
		inland_navigation_waterway_I, 
		inland_navigation_waterway_II, 
		inland_navigation_waterway_III, 
		inland_navigation_waterway_IV, 
		inland_navigation_waterway_V, 
		inland_navigation_waterway_VI, 
		inland_navigation_waterway_VII, 
		maritime_navigation, 
		navigable_lake 
	}

	/**
	 * Retrieve the Color to use to paint CrossSectionElements that have this
	 * CrossSectionElementTypology.
	 * @return Color; the color to paint CrossSectionElements with that have
	 * this CrossSectionElementTypology 
	 */
	public Color getColor_r() {
    	if (getDrivable())
    		return Color.GRAY;
    	if (getName_r().equals("barrier"))
    		return Color.RED;
    	return Color.GREEN;
	}

	/**
	 * Write this CrossSectionElementTypology to an XML file.
	 * @param staXWriter {@link StaXWriter}; the writer for the XML file
	 * @return Boolean; true on success; false on failure
	 */
	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& staXWriter.writeNode(XML_NAME, getName_r())
				&& staXWriter.writeNode(XML_DRIVABLE, Boolean.toString(getDrivable()))
				&& staXWriter.writeNodeEnd(XMLTAG);
	}
	
}