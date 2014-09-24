package nl.tudelft.otsim.SpatialTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nl.tudelft.otsim.GeoObjects.Link;
import nl.tudelft.otsim.GeoObjects.MicroZone;
import nl.tudelft.otsim.GeoObjects.Vertex;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

public class SpatialQueries {
	
    /**
     * createLinkQuery sets up a spatially index for a collection of geometries
     * It return a tree with a List of Links
     * @param links
     * @param bound 
     * @return tree
     */
    public static SpatialIndex createLinkQuery(Collection<Link> links, double bound)   {
		GeometryFactory geometryFactory = new GeometryFactory();
		Envelope searchEnvelope = new Envelope(-bound, bound, -bound, bound); // this should contain the bounding box of all links
		final SpatialIndex tree = new STRtree(); //STRtree is a R-tree bases spatial index for efficient searching
		for (Link link : links)   {
			ArrayList<Vertex> vertices = link.getVertices();
			Coordinate[] coords  = new Coordinate[vertices.size()]; 
			int i = 0;
			for (Vertex vertex : vertices) {
				coords[i] = new Coordinate(vertex.getX(),vertex.getY(),vertex.getZ());
				i++;
			}
			LineString line = geometryFactory.createLineString(coords);
			line.setUserData(link);
			tree.insert(searchEnvelope, line);
		}
		return tree;
    }
    
    public static SpatialIndex createVertexQuery(List<MicroZone> microZones, double bound)   {
		GeometryFactory geometryFactory = new GeometryFactory();
		Envelope searchEnvelope = new Envelope(-bound, bound, -bound, bound); // this should contain the bounding box of all links
		final SpatialIndex tree = new STRtree(); //STRtree is a R-tree bases spatial index for efficient searching
		for (MicroZone microZone : microZones)   {
			Coordinate coord  = new Coordinate(microZone.getX(),microZone.getY(),microZone.getZ()); 
			Point point = geometryFactory.createPoint(coord);
			point.setUserData(microZone);
			tree.insert(searchEnvelope, point);
		}
		return tree;
    }  
    
}