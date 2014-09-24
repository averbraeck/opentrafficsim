package nl.tudelft.otsim.GeoObjects;

import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.SpatialTools.Planar;

/**
 * Editable polygon that may not be self-intersecting. Vertices are actually 3D, the X and Y components must define a
 * non-self-intersecting polygon
 * 
 * @author Peter Knoppers
 */
public class SimplePolygon implements XML_IO {
	/** Name of a SimplePolygon in XML representation */
	public static final String XMLTAG = "polygon";
	private static final String XML_NAME = "name";
	private static final String XML_VERTEX = "vertex";
	private static final String XML_VERTEXINDEX = "rank";
	private static final double tooClose = 0.0001;

	private java.util.ArrayList<Vertex> vertices = new java.util.ArrayList<Vertex>();
	private String name = null;

	/**
	 * Create a SimplyPolygon with no name and zero vertices.
	 */
	public SimplePolygon() {
		// Everything is already correctly initialized
	}

	/**
	 * Create a SimplePolygon with no name and vertices derived from an ordered list of Point2D.Doube and a common z value.
	 * @param points ArrayList&lt;Point2D.Double&gt;; the list of points
	 * @param z Double; the z-value for the vertices
	 */
	public SimplePolygon(java.util.ArrayList<java.awt.geom.Point2D.Double> points, Double z) {
		int end = points.size();
		while ((end >= 2) && (points.get(0).distance(points.get(end - 1)) < tooClose))
			end--;
		Point2D.Double prevPoint = end > 1 ? points.get(end - 1) : null;
		for (Point2D.Double p : points) {
			if ((null != prevPoint) && (prevPoint.distance(p) < tooClose))
				continue;
			vertices.add(new Vertex(p.x, p.y, z));
			prevPoint = p;
		}
	}

	/**
	 * Create a SimplePolygon with no name and vertices from an ordered list of {@link Vertex Vertices}.
	 * @param vertices ArrayList&lt;{@link Vertex}&gt;; the vertices to initialize this SimplePolygon with
	 */
	public SimplePolygon(java.util.ArrayList<Vertex> vertices) {
		for (Vertex v : vertices)
			this.vertices.add(new Vertex(v));
	}

	/**
	 * Create a SimplePolygon from an XML description.
	 * @param pn {@link ParsedNode} XML description of the SimplePolygon
	 * @throws Exception
	 */
	public SimplePolygon(ParsedNode pn) throws Exception {
		int vertexCount = pn.size(XML_VERTEX);
		if (vertexCount < 3)
			throw new Exception("Polygon has too few vertices (got " + vertexCount + " minimum is 3)");
		Vertex[] tempVertices = new Vertex[vertexCount];
		for (int i = 0; i < vertexCount; i++)
			tempVertices[i] = null;
		for (int i = 0; i < vertexCount; i++) {
			ParsedNode vertexNode = pn.getSubNode(XML_VERTEX, i);
			String indexString = vertexNode.getAttributeValue(XML_VERTEXINDEX);
			if (null == indexString)
				throw new Exception("Vertex in Polygon has no " + XML_VERTEXINDEX + " attribute");
			int index = 0;
			try {
				index = Integer.parseInt(indexString);
			} catch (Exception e) {
				throw new Exception("Vertex in Polygon has bad " + XML_VERTEXINDEX + " attribute");
			}
			if ((index < 0) || (index >= vertexCount))
				throw new Exception("Vertex in Polygon has bad " + XML_VERTEXINDEX + " value (got " + indexString + ")");
			if (null != tempVertices[index])
				throw new Exception("Polygon has duplicate " + XML_VERTEXINDEX + " (" + indexString + ")");
			tempVertices[index] = new Vertex(vertexNode);
		}
		for (int i = 0; i < vertexCount; i++)
			vertices.add(tempVertices[i]);
		if (pn.size(XML_NAME) != 1)
			throw new Exception("Polygon must have one " + XML_NAME);
		name = pn.getSubNode(XML_NAME, 0).getValue();
		if ((null == name) || (name.length() == 0))
			throw new Exception("Polygon has bad name");
	}

	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		if (vertices.size() < 3)
			throw new Error("Stored polygon must have at least 3 vertices");
		if ((null == name) || (0 == name.length()))
			throw new Error("Stored polygon must have a name");
		return staXWriter.writeNodeStart(XMLTAG) && staXWriter.writeNode(XML_NAME, name) && writeVertices(staXWriter)
				&& staXWriter.writeNodeEnd(XMLTAG);
	}

	private boolean writeVertices(StaXWriter staXWriter) {
		for (int i = 0; i < vertices.size(); i++) {
			java.util.HashMap<String, String> hashMap = new java.util.HashMap<String, String>(1);
			hashMap.put(XML_VERTEXINDEX, "" + i);
			if (!(staXWriter.writeNodeStart(XML_VERTEX, hashMap) && vertices.get(i).writeVertexXML(staXWriter) && staXWriter
					.writeNodeEnd(XML_VERTEX)))
				return false;
		}
		return true;
	}

	/**
	 * Generate an array of 2D points of the z=0 projection of this SimplePolygon.
	 * @return java.awt.geom.Point2D.Double[]; the array of 2D points
	 */
	public java.awt.geom.Point2D.Double[] getProjection() {
		return Planar.getAlignment(vertices);
	}

	/**
	 * Return a GeneralPath describing the z=0 projection of this SimplePolygon.
	 * @return GeneralPath; a GeneralPath that describes the z=0 projection of this SimplePolygon
	 */
	public GeneralPath getGeneralPath() {
		GeneralPath result = new GeneralPath(Path2D.WIND_EVEN_ODD);
		boolean firstPoint = true;
		for (Vertex v : vertices) {
			if (firstPoint)
				result.moveTo(v.getX(), v.getY());
			else
				result.lineTo(v.getX(), v.getY());
			firstPoint = false;
		}
		if (vertices.size() >= 2)
			result.closePath();
		return result;
	}

	/**
	 * Compute the surface area of the z=0 projection of this SimplePolygon. <br />
	 * Derived from <a href="http://geomalgorithms.com/a01-_area.html#2D%20Polygons">Areas of Triangles and Polygons</a>.
	 * @return Double; the surface area of the z=0 projection of this SimplePolygon
	 */
	public double surfaceArea() {
		return Planar.areaOfSimplePolygon(getProjection());
	}

	/**
	 * Check that a proposed value of a Vertex is permissible. A vertex may be added if the result is a valid SimplePolygin (the
	 * edges do not self-intersect and the surface area is >= 0).
	 * @param i Integer; index of the Vertex that might be changed
	 * @param v {@link Vertex}; the proposed new Vertex
	 * @return Boolean; true if the proposed new Vertex is permissible; false if the proposed new Vertex is not permissible
	 */
	public boolean mayUpdateVertex(int i, Vertex v) {
		Point2D.Double[] points = getProjection();
		if ((i < 0) || (i >= points.length))
			throw new Error("index i out of range");
		if (points.length <= 2)
			return true;
		Line2D.Double replacedLine1 = new Line2D.Double(points[i], points[(i - 1 + points.length) % points.length]);
		Line2D.Double replacedLine2 = new Line2D.Double(points[i], points[(i + 1) % points.length]);
		Line2D.Double line = new Line2D.Double(v.getPoint(), points[(i + 1) % points.length]);
		if (line.getP1().distance(line.getP2()) < tooClose)
			return false;
		java.util.ArrayList<Point2D.Double> intersections = Planar.lineIntersectsPolygon(line, points);
		for (Point2D.Double p : intersections)
			if ((Planar.distanceLineSegmentToPoint(replacedLine1, p) > tooClose)
					&& (Planar.distanceLineSegmentToPoint(replacedLine2, p) > tooClose))
				return false;
		line.x2 = points[(i - 1 + points.length) % points.length].x;
		line.y2 = points[(i - 1 + points.length) % points.length].y;
		if (line.getP1().distance(line.getP2()) < tooClose)
			return false;
		intersections = Planar.lineIntersectsPolygon(line, points);
		for (Point2D.Double p : intersections)
			if ((Planar.distanceLineSegmentToPoint(replacedLine1, p) > tooClose)
					&& (Planar.distanceLineSegmentToPoint(replacedLine2, p) > tooClose))
				return false;
		points[i] = v.getPoint();
		return Planar.areaOfSimplePolygon(points) >= 0;
	}

	/**
	 * Check that a proposed vertex may be added to the end of this SimplePolygon. A vertex may be added if the result is a valid
	 * SimplePolygin (the edges do not self-intersect and the surface area is >= 0).
	 * @param v {@link Vertex}; the proposed vertex
	 * @return Boolean; true if the proposed vertex is permissible; false if the proposed vertex is not permissible
	 */
	public boolean mayAddVertex(Vertex v) {
		Point2D.Double[] points = getProjection();
		if (points.length < 2)
			return true;
		Line2D.Double line = new Line2D.Double(v.getPoint(), points[points.length - 1]);
		if (line.getP1().distance(line.getP2()) < tooClose)
			return false;
		Line2D.Double replacedLine = new Line2D.Double(points[points.length - 1], points[0]);
		java.util.ArrayList<Point2D.Double> intersections = Planar.lineIntersectsPolygon(line, points);
		for (Point2D.Double p : intersections)
			if ((Planar.distanceLineSegmentToPoint(replacedLine, p)) > tooClose)
				return false;
		line.x2 = points[0].x;
		line.y2 = points[0].y;
		if (line.getP1().distance(line.getP2()) < tooClose)
			return false;
		intersections = Planar.lineIntersectsPolygon(line, points);
		for (Point2D.Double p : intersections)
			if ((Planar.distanceLineSegmentToPoint(replacedLine, p)) > tooClose)
				return false;
		Point2D.Double[] testPoints = new Point2D.Double[points.length + 1];
		for (int i = points.length; --i >= 0;)
			testPoints[i] = points[i];
		testPoints[points.length] = v.getPoint();
		return Planar.areaOfSimplePolygon(testPoints) >= 0;
	}

	/**
	 * Check that a vertex may be deleted from this SimplePolygon. A vertex may be deleted if the result is a valid SimplePolygin
	 * (the edges do not self-intersect and the surface area is >= 0).
	 * @param i Integer; index of the vertex to check for deletion.
	 * @return Boolean; true if the indicated vertex may be deleted; false if the indicated vertex may not be deleted
	 */
	public boolean mayDeleteVertex(int i) {
		if (i < 0)
			return false;
		int range = vertices.size();
		if (i >= range)
			return false;
		if (range <= 2)
			return false;
		Point2D.Double[] points = getProjection();
		if (vertices.get((i - 1 + range) % range).getPoint().distance(vertices.get((i + 1) % range).getPoint()) < tooClose)
			return false;
		points[i] = Vertex.weightedVertex(0.5, vertices.get((i - 1 + range) % range), vertices.get((i + 1) % range)).getPoint();
		return Planar.areaOfSimplePolygon(points) >= 0;
	}

	/**
	 * Return a copy of a {@link Vertex}.
	 * @param i Integer; index of the requested vertex
	 * @return {@link Vertex} copy of the indicated vertex
	 */
	public Vertex getVertex(int i) {
		return new Vertex(vertices.get(i));
	}

	/**
	 * Return the number of vertices of this SimplePolygon.
	 * @return Integer; the number of vertices of this SimplePolygon
	 */
	public int size() {
		return vertices.size();
	}

	/**
	 * Retrieve the name of this SimplePolygon.
	 * @return String; the name of this SimplePolygon (can be null)
	 */
	public String getName_r() {
		return name;
	}

	/**
	 * Set/Change the name of this SimplePolygon.
	 * @param newName String; the new name for this SimplePolygon (may be null)
	 */
	public void setName_w(String newName) {
		name = newName;
	}

}
