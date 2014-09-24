package nl.tudelft.otsim.SpatialTools;

/**
 * This class will contain conversions from WGS (World Geodetic System) to
 * local coordinate systems (and possibly the reverse transforms as well).
 * 
 * @author Peter Knoppers
 */
public interface WGS84Converter {
	/**
	 * Report whether this converter is sufficiently accurate for a specified point in WGS84
	 * @param location Point2D.Double; the location in WGS84 coordinates
	 * (positive x is East; positive y is North)
	 * @return Boolean; true if this converter is sufficiently accurate for the 
	 * specified location; false if this converter is not sufficiently accurate 
	 * for the specified location
	 */
	abstract public boolean accurateAt(java.awt.geom.Point2D.Double location);
	
	/**
	 * Convert a location in WGS84 coordinates to coordinates in meters
	 * relative to some local reference point.
	 * @param wgs84 java.awt.geom.Point2D.Double; the location to convert
	 * (positive x is East; positive y is North)
	 * @return java.awt.geom.Point2D.Double; the converted location
	 */
	abstract public java.awt.geom.Point2D.Double meters (java.awt.geom.Point2D.Double wgs84);
	
}
