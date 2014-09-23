package nl.tudelft.otsim.GUI;

import java.awt.geom.Point2D;

/**
 * GraphicsPanel without any visible components.
 * 
 * @author Peter Knoppers
 */
public class FakeGraphicsPanel extends GraphicsPanel {

	private static final long serialVersionUID = 1L;
	
	private GraphicsPanelClient client = null;

	/**
	 * Create a fake GraphicsPanel.
	 */
	public FakeGraphicsPanel() {
		// NOthing to do
	}
	
    @Override
	public void repaint(boolean clearPaintComplete) {
    }
    
    @Override
	public boolean paintComplete() {
    	return true;
    }
    
	@Override
	public void setZoom(double zoom, Point2D.Double center) {
	}
	
	@Override
	public void setClient(GraphicsPanelClient client) {
		this.client = client;
	}

	/**
	 * Retrieve the GraphicsPanelClient of this GraphicsPanel
	 * @return GraphicsPanelClient of this GraphicsPanel
	 */
	@Override
	public GraphicsPanelClient getClient() {
		return client;
	}
	
	@Override
	public void setPan(double panX, double panY) {
	}

}
