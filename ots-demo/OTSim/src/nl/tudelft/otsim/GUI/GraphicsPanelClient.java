package nl.tudelft.otsim.GUI;

import java.awt.event.MouseEvent;

/**
 * Abstract class that specifies the minimum graphical functionality that must
 * be implemented by a graphical editor or simulator in Open Traffic. 
 * 
 * @author Peter Knoppers
 */
public interface GraphicsPanelClient {
	/**
	 * Indicate that the GraphicsPanelClient was modified (and the user might
	 * wish to save changes before closing).
	 */
	abstract public void setModified();
	/**
	 * Redraw the contents of the editor or simulator a GraphicsPanel.
	 * @param graphicsPanel GraphicsPanel; graphicsPanel to draw the state of
	 * the editor or simulator on
	 */
	abstract public void repaintGraph(GraphicsPanel graphicsPanel);
	/**
	 * Handle a mouse pressed event.
	 * @param graphicsPanel GraphicsPanel; graphicsPanel on which the mouse
	 * pressed event occurred 
	 * @param evt MouseEvent; the mouse pressed event
	 */
	abstract public void mousePressed(GraphicsPanel graphicsPanel, MouseEvent evt);
	/**
	 * Handle a mouse dragged event.
	 * @param graphicsPanel GraphicsPanel; graphicsPanel on which the mouse
	 * dragged event occurred
	 * @param evt MouseEvent; the mouse dragged event
	 */
	abstract public void mouseDragged(GraphicsPanel graphicsPanel, MouseEvent evt);
	/**
	 * Handle a mouse released event.
	 * @param graphicsPanel GraphicsPanel; graphicsPanel on which the mouse
	 * released event occurred
	 * @param evt MouseEvent; the mouse released event
	 */
	abstract public void mouseReleased(GraphicsPanel graphicsPanel, MouseEvent evt);
	/**
	 * Handle a mouse moved event.
	 * @param graphicsPanel GraphicsPanel; graphicsPanel on which the mouse
	 * moved event occurred
	 * @param evt MouseEvent; the mouse moved event
	 */
	abstract public void mouseMoved(GraphicsPanel graphicsPanel, MouseEvent evt);
	
} 