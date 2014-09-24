package nl.tudelft.otsim.SpatialTools;

/**
 * Convert WGS84 coordinates to the Dutch RD (RijksDriehoek) system.
 * 
 * @author Peter Knoppers
 */
public class DutchRD implements WGS84Converter {

	@Override
	public boolean accurateAt(java.awt.geom.Point2D.Double location) {
		return ((location.x > 3.12188) && (location.x < 7.275208) && (location.y > 50.670522) && (location.y < 53.596204));
	}

	@Override
	public java.awt.geom.Point2D.Double meters(java.awt.geom.Point2D.Double wgs84) {
		// Uses the ellipsoidal transforms described in
		// http://www.dekoepel.nl/pdf/Transformatieformules.pdf
		if (! accurateAt(wgs84))
			throw new Error ("DutchRD is not accurate for " + wgs84.toString());
		final double dn = (wgs84.y - 52.15517440) * 0.36;
		final double de = (wgs84.x -  5.38720621) * 0.36;
		double x = 0;
		double y = 0;
		final double[][] r = { // p down, q right
	            {  155000.00, 190094.945,   -0.008, -32.391, 0.0   , },
	            {     -0.705, -11832.228,    0.0  ,   0.608, 0.0   , },
	            {      0.0  ,   -114.221,    0.0  ,   0.148, 0.0   , },
	            {      0.0  ,      2.340,    0.0  ,   0.0  , 0.0   , },
	            {      0.0  ,      0.0  ,    0.0  ,   0.0  , 0.0   , }};
		final double[][] s = { // p down, q right
	            { 463000.00 ,      0.433, 3638.893,   0.0  ,  0.092, },
	            { 309056.544,     -0.032, -157.984,   0.0  , -0.054, },
	            {     73.077,      0.0  ,   -6.439,   0.0  ,  0.0  , },
	            {     59.788,      0.0  ,    0.0  ,   0.0  ,  0.0  , },
	            {      0.0  ,      0.0  ,    0.0  ,   0.0  ,  0.0  , }};
		double pown = 1;
		for (int p = 0; p < r.length; p++) {
			double powe = 1;
			for (int q = 0; q < r[0].length; q++) {
				x += r[p][q] * powe * pown;
				y += s[p][q] * powe * pown;
				powe *= de;
			}
			pown *= dn;
		}
		return new java.awt.geom.Point2D.Double(x, y);		
	}

}
