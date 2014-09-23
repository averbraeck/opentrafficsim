package nl.tudelft.otsim.GUI;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JPanel;

import nl.tudelft.otsim.GeoObjects.Vertex;

/**
 * This class implements the minimum drawing primitives that allow a
 * {@link GraphicsPanelClient} to paint itself with the correct zoom and pan state.
 * 
 * @author Peter Knoppers
 */
public class GraphicsPanel extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;

	private Graphics2D g2 = null;
	
	private double panX = 0;
	private double panY = 0;
	private double zoom = 1;
	private int height = 0;
	private volatile boolean paintComplete = true;	// this one MUST be declared volatile!
	
	//private double rotation = 0;	// Rotation is too hard for now
	// reading material for manipulating a 3D scene:
	// Arcball: http://www.talisman.org/~erlkonig/misc/shoemake92-arcball.pdf
	// Java3D:  http://java.sun.com/developer/onlineTraining/java3d/
	private GraphicsPanelClient client = null;
	
	/**
	 * Assign a {@link GraphicsPanelClient} to this GraphicsPanel.
	 * @param client GraphicsPanelClient; the client
	 */
	public void setClient(GraphicsPanelClient client) {
		this.client = client;
	}
	
	/**
	 * Retrieve the GraphicsPanelClient of this GraphicsPanel
	 * @return GraphicsPanelClient of this GraphicsPanel
	 */
	public GraphicsPanelClient getClient() {
		return client;
	}
	
	/**
	 * Convert coordinates from {@link nl.tudelft.otsim.GeoObjects.Network Network}, or 
	 * {@link nl.tudelft.otsim.Simulators.Simulator Simulator} space to the coordinate system of 
	 * the JPanel that displays the Network, or Simulator. 
	 * @param p Point2D.Double; the point to be converted
	 * @return Point2D.Double; the converted point
	 */
	public Point2D.Double translate(Point2D.Double p) {
		if (null == p) {
			System.err.println("null pointer in translate");
		}
		double x = p.x * zoom + panX;
		double y = height - (p.y * zoom - panY);
		Point2D.Double result = new Point2D.Double(x, y);
		return result;
	}
	
	/**
	 * Convert coordinates from the coordinate system of the JPanel that
	 * displays the {@link nl.tudelft.otsim.GeoObjects.Network Network}, or 
	 * {@link nl.tudelft.otsim.Simulators.Simulator Simulator} to Network, or Simulator space.
	 * @param p Point2D.Double; the point to be converted
	 * @return Point2D.Double; the converted point
	 */
	public Point2D.Double reverseTranslate(Point2D.Double p) {
		double x = (p.x - panX) / zoom;
		double y = ((height - p.y) + panY) / zoom;
		Point2D.Double result = new Point2D.Double(x, y);
		return result;
	}
	
	/**
	 * Change the pan setting of this GraphicsPanel.
	 * <br />
	 * The pan setting arguments are in pixels on the JPanel that displays the
	 * {@link nl.tudelft.otsim.GeoObjects.Network}, or {@link nl.tudelft.otsim.Simulators.Simulator Simulator}.
	 * @param panX Double; X-coordinate of the new pan setting
	 * @param panY Double; Y-coordinate of the new pan setting
	 */
	public void setPan(double panX, double panY) {
		//System.out.println(String.format("pan changed from %.1f,%.1f to %.1f,%.1f", this.panX, this.panY, panX, panY));
		this.panX = panX;
		this.panY = panY;
	}
	
	/**
	 * Shift the pan setting of this GraphicsPanel.
	 * <br />
	 * The shift is specified in pixels on the JPanel that displays the
	 * {@link nl.tudelft.otsim.GeoObjects.Network Network}, or
	 * {@link nl.tudelft.otsim.Simulators.Simulator Simulator}.
	 * @param deltaX Double; difference for the X-coordinate of the pan setting
	 * @param deltaY Double; difference for the Y-coordinate of the pan setting 
	 */
	public void addPan(double deltaX, double deltaY) {
		setPan(panX + deltaX, panY + deltaY);
	}
	
	/**
	 * Retrieve the current pan setting.
	 * <br />
	 * The pan setting arguments are in pixels on the JPanel that displays the
	 * {@link nl.tudelft.otsim.GeoObjects.Network Network}, or 
	 * {@link nl.tudelft.otsim.Simulators.Simulator Simulator}. 
	 * @return Point2D.Double; the current pan setting
	 */
	public Point2D.Double getPan() {
		return new Point2D.Double(panX, panY);
	}
	
	/**
	 * Change the zoom and pan settings for this GraphicsPanel.
	 * @param zoom Double; the new zoom factor
	 * @param center Point2D.Double; the new pan setting
	 */
	public void setZoom(double zoom, Point2D.Double center) {
		//System.out.println(String.format("pan %.0f,%.0f zoom changed from %.2f to %.2f, centered around %.0f,%.0f", panX, panY, this.zoom, zoom, center.x, center.y));
		// Figuring out the computation for the new panX, panY was NOT easy ...
		center = new Point2D.Double(center.x, center.y - getHeight());
		panX = center.x - (center.x - panX) / this.zoom * zoom;
		panY = center.y - (center.y - panY) / this.zoom * zoom;
		//System.out.println(String.format("new pan %.1f,%.1f", panX, panY));
		this.zoom = zoom;
	}
	
	/**
	 * Retrieve the zoom factor for this GraphicsPanel.
	 * @return Double; the zoom factor
	 */
	public double getZoom() {
		return zoom;
	}
	
	/**
	 * Create a new GraphicsPanel with mouse wheel handling for the zoom
	 * factor.
	 * <br />
	 * Zoom setting is initialized to 1, pan to (0, 0).
	 */
    public GraphicsPanel() {
    	addMouseListener(this); // Register mouse listener.
    	addMouseMotionListener(this); // Register mouse listener.
    	MouseWheelListener listener = new MouseWheelListener() {
    	    @Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int wheelCounts = e.getWheelRotation();
				final double factor = wheelCounts > 0 ? 1 / 1.5d : 1.5d;
				setZoom(getZoom() * factor, new Point2D.Double(e.getX(), e.getY()));
				//System.out.printf("Scroll %d at %d,%d\r\n", wheelCounts, e.getX(), e.getY());
				repaint();
    	    }
    	};
    	addMouseWheelListener(listener);
        repaint();
        revalidate();
    }
    
    /**
     * Schedule a repaint that background threads can verify with the
     * paintComplete method.
     * @param clearPaintComplete
     */
    public void repaint(boolean clearPaintComplete) {
    	if (clearPaintComplete)
    		paintComplete = false;
    	repaint();
    }

    /**
     * Check if a scheduled paint operation has finished.
     * @return Boolean; true if a scheduled paint operation has finished;
     * false if it is still pending
     */
    public boolean paintComplete() {
    	return paintComplete;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        height = getHeight();
        
        // Draw the graphical representation of the network / simulation
        g2 = (Graphics2D)g;
        if (null != client)
        	client.repaintGraph(this);
        g2 = null;
        paintComplete = true;
    }
    
    /**
     * Draw a line on this GraphicsPanel in the current Color and Stroke.
     * @param from Point2D.Double; start of the line in 
     * {@link nl.tudelft.otsim.GeoObjects.Network Network}, or
     * {@link nl.tudelft.otsim.Simulators.Simulator Simulator} coordinates
     * @param to Point2D.Double; end of the line in  Network, or Simulator 
     * coordinates
     */
    public void drawLine(Point2D.Double from, Point2D.Double to) {
    	if (null == g2)
    		throw new Error("not painting");
    	from = translate(from);
    	to = translate(to);
    	g2.drawLine((int) from.x, (int) from.y, (int) to.x, (int) to.y);
    }
    
    /**
     * Draw a line along a series of points using the current Color and Stroke.
     * @param points Array of Point2D.Double; the points to draw the line
     * along. These coordinates must be specified in 
     * {@link nl.tudelft.otsim.GeoObjects.Network Network}, or
     * {@link nl.tudelft.otsim.Simulators.Simulator Simulator} coordinates.
     * @param closeIt Boolean; connect the end point to the start point
     */
    public void drawPolyLine(Point2D.Double[] points, boolean closeIt) {
    	if (null == g2)
    		throw new Error("not painting");
    	Point2D.Double from = null;
    	for (Point2D.Double p : points) {
    		//if (p.distance(new Point2D.Double(161,-17)) < 5)
    		//	System.out.println("weird point");
    		if (null != from)
    			drawLine(from, p);
     		from = p;
    	}
    	if (closeIt && (null != from))
    		drawLine(from, points[0]);
    }
    
    /**
     * Draw a line along a series of points using the current Color and Stroke.
     * @param vertices ArrayList&lt;Point2D.Double&gt; List of points to draw the
     * line along. These coordinates must be specified in 
     * {@link nl.tudelft.otsim.GeoObjects.Network Network}, or
     * {@link nl.tudelft.otsim.Simulators.Simulator Simulator} coordinates.
     */
    public void drawPolyLine(ArrayList<Vertex> vertices) {
    	if (null == g2)
    		throw new Error("not painting");
    	if (0 == vertices.size())
    		return;
    	Point2D.Double from = null;
    	for (Vertex v : vertices) {
    		Point2D.Double p = v.getPoint();
    		if (null != from)
    			drawLine(from, p);
    		from = p;
    	}
    }
    
    /**
     * Draw a polygon using the current Color and Stroke. The polygon need not
     * be closed; a line from the last point to the first is added
     * automatically.
     * @param points Array of Point2D.Double, or {@link Vertex}; the points 
     * to draw the line along. These coordinates must be specified in 
     * {@link nl.tudelft.otsim.GeoObjects.Network Network}, or
     * {@link nl.tudelft.otsim.Simulators.Simulator Simulator} coordinates.
     */
	public void drawPolygon(Object[] points) {
    	if (null == g2)
    		throw new Error("not painting");
		java.awt.Polygon polygon = new Polygon();
		for (Object o : points) {
			Point2D.Double point;
			if (o instanceof Vertex)
				point = ((Vertex) o).getPoint();
			else if (o instanceof Point2D.Double)
				point = (Point2D.Double) o;
			else   {
				throw new Error("Don't know how to draw a polygon of a " + o.getClass().getCanonicalName());
			}
			Point2D.Double translated = translate(point);
			polygon.addPoint((int) translated.x, (int) translated.y);
		}
		g2.drawPolygon(polygon);
		g2.fillPolygon(polygon);
	}
    
	/**
	 * Draw a GeneralPath on this GraphicsPanel.
	 * @param polygon GeneralPath; the GeneralPath to draw
	 * @param lineColor Color to draw the line with (if null; the line is not drawn)
	 * @param fillColor Color to fill the area with (if null; the area is not filled)
	 */
    public void drawGeneralPath(GeneralPath polygon, Color lineColor, Color fillColor) {
    	if (null == g2)
    		throw new Error("not painting");
    	GeneralPath drawPath = translatePath(polygon);
    	checkHugeJumps(drawPath);
    	if (null != fillColor) {
    		g2.setColor(fillColor);
    		g2.fill(drawPath);
    	}
    	if (null != lineColor)
    		g2.setColor(lineColor);
    }
    
    private static void checkHugeJumps (GeneralPath path) {
    	final double huge = 250;	// [m]
    	Point2D.Double currentPoint = null;
        for (PathIterator pi = path.getPathIterator(null); ! pi.isDone(); pi.next()) {
            double[] coords = new double[6];
            Point2D.Double p;
            switch (pi.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO:
        		currentPoint = new Point2D.Double(coords[0],coords[1]);
                break;
            case PathIterator.SEG_LINETO:
        		p = new Point2D.Double(coords[0],coords[1]);
        		if (null == currentPoint)
        			throw new Error ("LINETO without MOVETO");
        		if (currentPoint.distance(p) > huge)
        			;//System.out.println(String.format(Locale.US, "Huge jump in path from %.3f,%.3f to %.3f,%.3f in %s", currentPoint.x, currentPoint.y, p.x, p.y, Planar.generalPathToString(path)));
        		currentPoint = p;
        		break;
            case PathIterator.SEG_CLOSE:
        		break;
        	default:
                throw new IllegalArgumentException("Path contains curves");
            }
        }
    }
    
    /**
     * Translate a Path2D from {@link nl.tudelft.otsim.GeoObjects.Network Network}, or 
	 * {@link nl.tudelft.otsim.Simulators.Simulator Simulator} space to the 
	 * coordinate system of the JPanel that displays the Network, or Simulator.  
     * @param path Path2D; the path to translate
     * @return GeneralPath; the translated path
     */
    public GeneralPath translatePath(Path2D path) {	
        double[] coords = new double[6];
        int numSubPaths = 0;
        GeneralPath drawPath = new GeneralPath(Path2D.WIND_EVEN_ODD, 3);
        for (PathIterator pi = path.getPathIterator(null); ! pi.isDone(); pi.next()) {
            switch (pi.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO:
        		Point2D.Double p = new Point2D.Double(coords[0],coords[1]);
        		Point2D.Double translatedPoint = translate(p);
        		drawPath.moveTo(translatedPoint.getX(), translatedPoint.getY());
                ++numSubPaths;
                break;
            case PathIterator.SEG_LINETO:
        		p = new Point2D.Double(coords[0],coords[1]);
        		translatedPoint = translate(p);
        		drawPath.lineTo(translatedPoint.getX(), translatedPoint.getY());
        		break;
            case PathIterator.SEG_CLOSE:
                if (numSubPaths > 1)
                    throw new IllegalArgumentException("Path contains multiple subpaths");
        		p = new Point2D.Double(coords[0],coords[1]);
        		translatedPoint = translate(p);
        		drawPath.closePath();
        		break;
        	default:
                throw new IllegalArgumentException("Path contains curves");
            }
        }
        return drawPath;
    }
    
	/**
	 * Highlight a set of points in the current Color and Stroke. The points
	 * are indicated by drawing a circle with radius 3 around the specified
	 * locations.
	 * @param points Array of Point2D.Double; the points to highlight.
     * These coordinates must be specified in 
     * {@link nl.tudelft.otsim.GeoObjects.Network Network}, or
     * {@link nl.tudelft.otsim.Simulators.Simulator Simulator} coordinates.
	 */
    public void drawPoints(Point2D.Double[] points) {
    	if (null == g2)
    		throw new Error("not painting");
        for (int pointNo = 0; pointNo < points.length; pointNo++) {
    		Point2D.Double translatedPoint = translate(points[pointNo]);
    		int x = (int) (translatedPoint.x);
    		int y = (int) (translatedPoint.y);
	    	g2.drawOval(x, y, 3, 3);
        }
    }
    
    /**
     * Draw an X at a series of points using the current Color and Stroke. The 
     * size of the X is 6 by 6 pixels.
     * @param points Array of Point2D.Double; the points to highlight.
     * These coordinates must be specified in 
     * {@link nl.tudelft.otsim.GeoObjects.Network Network}, or 
     * {@link nl.tudelft.otsim.Simulators.Simulator Simulator} coordinates.
     */
    public void drawX(Point2D.Double[] points) {
    	if (null == g2)
    		throw new Error("not painting");
        for (int pointNo = 0; pointNo < points.length; pointNo++) {
    		Point2D.Double translatedPoint = translate(points[pointNo]);
    		int x = (int) (translatedPoint.x);
    		int y = (int) (translatedPoint.y);
	    	g2.drawLine(x - 3, y - 3, x + 3, y + 3);
	    	g2.drawLine(x - 3, y + 3, x + 3, y - 3);
        }
    }
    
	/**
     * Set the color for subsequent line and text drawing operations.
     * @param color Color; color to use for subsequent line and text drawing
     */
    public void setColor(Color color) {
    	if (null == g2)
    		throw new Error("not painting");
    	g2.setColor(color);
    }
    
    /**
     * Fill the entire GraphicsPanel with a uniform color.
     * @param color Color; color to fill the GraphicsPanel with
     */
    public void fillPanel(Color color) {
    	if (null == g2)
    		throw new Error("not painting");
    	Color saveColor = g2.getColor();
    	g2.setColor(color);
		g2.fillRect(0, 0, getWidth(), getHeight());    	
    	g2.setColor(saveColor);
    }
    
    /** Value for the type argument of {@link #setStroke} */
    public static final int CONTINUOUS = 1;
    /** Value for the type argument of {@link #setStroke} */
    public static final int DASHED = 2; 
    /** Value for the type argument of {@link #setStroke} */
    public static final int SOLID = 3;
    /**
     * Sets the stroke for the Graphics object.
     * @param type 1=continuous, 2=dashed, 3=default <tt>BasicStroke</tt>
     * @param width Width of the line in meters
     * @param phase First x-coordinate to determine the phase for horizontal lines
     */
    public void setStroke(int type, float width, float phase) {
    	if (null == g2)
    		throw new Error("not painting");
    	float[] dash = null;
    	switch (type) {
    	case SOLID:
    		g2.setStroke(new java.awt.BasicStroke((float) (width * zoom)));
    		break;
    		
    	case DASHED:
            dash = new float[2];
            dash[0] = (float) (3 * zoom);
            dash[1] = (float) (9 * zoom);
            // no break; fall through into next case
    	case CONTINUOUS:
    		phase = (float) (phase - Math.floor((double) phase / 12) * 12);
            g2.setStroke(new java.awt.BasicStroke((float) (width * zoom), java.awt.BasicStroke.CAP_BUTT,
                    java.awt.BasicStroke.JOIN_MITER, 1.0f, dash, phase));
            break;
            
        default:
        	throw new Error("bad type");
    	}
    }
    
    /**
     * Set a BasicStroke for line drawing specifying only the width.
     * @param width Float; width used in subsequent line drawing
     */
    public void setStroke(float width) {
    	if (null == g2)
    		throw new Error("not painting");
    	g2.setStroke(new java.awt.BasicStroke((float) (width * zoom)));
    }
    
    /**
     * Draw a circle.
     * @param p Point2D.Double; position of the circle
     * @param color Color; color used to draw the circle
     * @param diameter Integer diameter (in pixels) of the circle (the 
     * diameter is <b>not</b> scaled by the zoom factor).
     */
    public void drawCircle(Point2D.Double p, Color color, int diameter) {
    	if (null == g2)
    		throw new Error("not painting");
    	g2.setColor(color);
    	p = translate(p);
    	g2.drawOval((int) (p.x - diameter / 2), (int) (p.y - diameter / 2), diameter, diameter);
    }
    
    /**
     * Draw a filled circle.
     * @param p Point2D.Double; center of the circle
     * @param color Color; color of the filled circle
     * @param diameter Integer; diameter of the filled circle (the diameter is
     * <b>not</b> scaled by the zoom factor).
     */
    public void drawDisc(Point2D.Double p, Color color, int diameter) {
    	if (null == g2)
    		throw new Error("not painting");
    	g2.setColor(color);
    	p = translate(p);
    	g2.fillOval((int) (p.x - diameter / 2), (int) (p.y - diameter / 2), diameter, diameter);
    }
   
    enum Anchor {
    	CENTER, LEFT, RIGHT, TOP, BOTTOM, TOPLEFT, TOPRIGHT, BOTTOMLEFT, BOTTOMRIGHT
    }
    
    /**
     * Draw a text. Size, font, style, etc. can not be specified (yet).
     * @param string String; text to draw
     * @param position Point2D.Double; location where the text is drawn
     */
    public void drawString(String string, Point2D.Double position/*, Anchor anchor*/) {
    	if (null == g2)
    		throw new Error("not painting");
    	position = translate(position);
    	FontMetrics fm = g2.getFontMetrics();
    	Rectangle2D r = fm.getStringBounds(string, g2);
    	g2.drawString(string, (int) (position.x - r.getWidth() / 2), (int) (position.y - r.getHeight() / 2 + fm.getAscent()));
    }

	@Override
	public void mouseDragged(MouseEvent arg0) {
		if (null != client)
			client.mouseDragged(this, arg0);	
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		if (null != client)
			client.mouseMoved(this, arg0);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// Ignored		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// Ignored		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// Ignored		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		if (null != client)
			client.mousePressed(this, arg0);
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (null != client)
			client.mouseReleased(this, arg0);
		repaint();
	}

} 