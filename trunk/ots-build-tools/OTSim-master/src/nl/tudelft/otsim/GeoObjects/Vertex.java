package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.awt.geom.Point2D;
import com.vividsolutions.jts.geom.Coordinate;
import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.GUI.GraphicsPanel;

/**
 * The Vertex class handles 3D coordinates.
 * <br />
 * This class does not explicitly implement XML_IO because doing so would
 * cause confusing name space pollution problems in all classes that extend
 * this class. Instead, the <code>writeXML</code> method in this class is
 * named <code>writeVertexXML</code>.
 * 
 * @author Peter Knoppers
 *
 */
public class Vertex {
	/** Name for the X coordinate of a Vertex when stored in XML format */
	public static final String XML_X = "X";
	/** Name for the Y coordinate of a Vertex when stored in XML format */
	public static final String XML_Y = "Y";
	/** Name for the Z coordinate of a Vertex when stored in XML format */
	public static final String XML_Z = "Z";
	
	protected double x;
	protected double y;
	protected double z;

	private void setValues(double X, double Y, double Z) {
		this.x = X;
		this.y = Y;
		this.z = Z;
	}

	/**
	 * Create a new Vertex
	 * @param X X-coordinate
	 * @param Y Y-coordinate
	 * @param Z Z-coordinate
	 */
	public Vertex (double X, double Y, double Z) {
		super();
		setValues(X, Y, Z);
    }
	
	/**
	 * Duplicate a Vertex
	 * @param v Vertex to copy
	 */
	public Vertex (Vertex v) {
		super();
		setValues(v.getX(), v.getY(), v.getZ());
	}

	/**
	 * Create a Vertex from a @link Point2D.Double and a Z-coordinate
	 * @param p
	 * @param Z
	 */
	public Vertex (Point2D.Double p, double Z) {
		super();
		setValues(p.getX(), p.getY(), Z);

	}
	
	/**
	 * Create a Vertex with (X, Y, Z) = (0, 0, NaN);
	 */
	public Vertex () {
		super();
	}
	
	/**
	 * Create a Vertex from a parsed XML file.
	 * @param pn {@link ParsedNode}; the root of the Vertex in the parsed XML file
	 * @throws Exception
	 */
	public Vertex(ParsedNode pn) throws Exception {
		super();
		x = y = z = Double.NaN;
		for (String fieldName : pn.getKeys()) {
			if (pn.size(fieldName) != 1)
				throw new Exception("Field " + fieldName + " has " + pn.size(fieldName) + "elements (should be 1)");
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (null == value)
				throw new Exception("Value of " + fieldName + " is null");
			if (fieldName.equals(XML_X))
				x = Double.parseDouble(value);
			else if (fieldName.equals(XML_Y))
				y = Double.parseDouble(value);
			else if (fieldName.equals(XML_Z))
				z = Double.parseDouble(value);
			else
				throw new Exception("Unknown field in Vertex: " + fieldName);
		}
		if (Double.isNaN(x) || Double.isNaN(y))
			throw new Exception("Incompletely defined Vertex");
	}

	/**
	 * Create a Vertex from a Coordinate.
	 * @param coordinate {@link com.vividsolutions.jts.geom.Coordinate}; the Coordinate to create a Vertex from
	 */
	public Vertex(Coordinate coordinate) {
		x = coordinate.x;
		y = coordinate.y;
		z = coordinate.z;
	}

	/**
	 * Write this Vertex to an XML file.
	 * @param staXWriter {@link StaXWriter}; the writer for the XML file
	 * @return Boolean; true on success; false on failure
	 */
	public boolean writeVertexXML (StaXWriter staXWriter) {
		return staXWriter.writeNode(XML_X, Double.toString(getX()))
		&& staXWriter.writeNode(XML_Y, Double.toString(getY()))
		&& staXWriter.writeNode(XML_Z, Double.toString(getZ()));
	}
	
	/**
	 * Create a Vertex located on (straight) line connecting vertices v1 and v2.
	 * Extrapolation is OK, but extreme extrapolation currently outputs a warning
	 * on System.out.
	 * @param v2Weight If 0; return v1; if 1 return v2, if -.5 return point halfway v1 and v2, etc.
	 * @param v1 First Vertex
	 * @param v2 Second Vertex
	 * @return Newly created Vertex
	 */
	public static Vertex weightedVertex(double v2Weight, Vertex v1, Vertex v2) {
		double complement = 1d - v2Weight;
		if ((v2Weight < -9) || (v2Weight > 10))
			System.out.println("weightedVertex: extreme extrapolation; v2Weight is " + v2Weight);
		Vertex result =  new Vertex (v1.x * complement + v2.x * v2Weight, v1.y * complement + v2.y * v2Weight, v1.z * complement + v2.z * v2Weight);
		return result;
	}

	/**
	 * Compute the 3D distance between this Vertex and another Vertex.
	 * @param to Other Vertex
	 * @return Distance between this Vertex and the <code>to</code> Vertex
	 */
	public double distanceTo(Vertex to) {
		double dx = to.x - x;
		double dy = to.y - y;
		double dz = to.z - z;
		if (Double.isNaN(dz))
			dz = 0.0;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/** Retrieve the X-coordinate of this Vertex
	 * @return Double; the value of the X-coordinate of this Vertex
	 */
	public double getX() {
		return x;
	}

	/**
	 * Change the X-coordinate of this Vertex.
	 * @param x New value for the X-coordinate
	 */
	public void setX(double x) {
		this.x = x;
	}

	/** Retrieve the Y-coordinate of this Vertex 
	 * @return Double; the value of the Y-coordinate of this Vertex
	 */
	public double getY() {
		return y;
	}

	/**
	 * Change the y-coordinate of this Vertex.
	 * @param y New value for the X-coordinate
	 */
	public void setY(double y) {
		this.y = y;
	}

	/** Retrieve the Z-coordinate of this Vertex
	 * @return Double; the value of the Z-coordinate of this Vertex
	 */
	public double getZ() {
		return z;
	}

	/**
	 * Change the z-coordinate of this Vertex.
	 * @param z New value for the X-coordinate
	 */
	public void setZ(double z) {
		this.z = z;
	}

	/**
	 * Create a Point2D.Double from the x and y coordinates of this Vertex.
	 * @return Newly created Point2D.Double
	 */
	public Point2D.Double getPoint() {
		return new Point2D.Double(x, y);
	}
	
	/**
	 * Change the x, y, and z-coordinates of this Vertex.
	 * @param X New x-coordinate value
	 * @param Y New y-coordinate value
	 * @param Z New z-coordinate value
	 */
	public void setPoint(double X, double Y, double Z) {
		this.x = X;
		this.y = Y;
		this.z = Z;
	}
	
	/**
	 * Change the x and y coordinates of this Vertex
	 * @param p Point2D.Double specifying the new x and y-coordinates
	 */
	public void setPoint(Point2D.Double p) {
		x = p.x;
		y = p.y;
	}

	/**
	 * Change the x, y and z-coordinates of this Vertex.
	 * @param v Vertex with the new x, y and z-coordinates
	 */
	public void setPoint(Vertex v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	/**
	 * Produce a String representation of this Vertex
	 */
	@Override
	public String toString() {
		return String.format(nl.tudelft.otsim.GUI.Main.locale, "(%.3fm, %.3fm, %.3fm)", x, y, z);
	}
	
	/**
	 * Write the current value of this Vertex to the console using System.out
	 * along with a descriptive text and return the Vertex.
	 * This can be embedded in argument lists of various methods to log the
	 * values of the parameters of those methods.
	 * @param where String; description to prepend to the message
	 * @param v Vertex to write
	 * @return Vertex (unaltered)
	 */
	public static Vertex log(String where, Vertex v) {
		System.out.println(where + ": vertex is " + v.toString());
		return v;
	}
	
    /**
     * Draw this Node on a GraphicsPanel.
     * @param graphicsPanel GraphicsPanel; graphicsPanel to draw onto
     */
	public void paint(GraphicsPanel graphicsPanel) {
        final int nonSelectedNodeDiameter = (int) (0.2 *  graphicsPanel.getZoom());

    	Point2D.Double point = getPoint();            
        final Color color = Color.RED;
        graphicsPanel.setColor(color);
        graphicsPanel.setStroke(1f);
        graphicsPanel.drawCircle(point, color, nonSelectedNodeDiameter);
        graphicsPanel.setStroke(3f);
	}

	/**
	 * Check if the projection of this Vertex is equal to the projection of another Vertex
	 * @param other {@link Vertex}; the other Vertex
	 * @return Boolean; true if both are exactly equals; false otherwise
	 */
	public boolean equals2D(Vertex other) {
		return (x == other.x) && (y == other.y);
	}

	/**
	 * Return the Cathesian distance between this Vertex and another Vertex.
	 * <br /> If either vertex has an invalid z-component; the z components
	 * are considered equal.
	 * @param other Vertex; the other Vertex
	 * @return Double; the distance between this Vertex and the other Vertex
	 */
	public double distance(Vertex other) {
		double dx = other.x - x;
		double dy = other.y - y;
		double dz = other.z - z;
		if (Double.isNaN(dz))
			dz = 0;
		return (Math.sqrt(dx * dx + dy * dy + dz * dz));
	}
}