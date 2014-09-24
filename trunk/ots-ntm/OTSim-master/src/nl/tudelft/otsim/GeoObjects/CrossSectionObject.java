package nl.tudelft.otsim.GeoObjects;

import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.GUI.GraphicsPanel;

/**
 * This class is the base for all road markings, detectors, traffic lights.
 * @author Peter Knoppers
 */
public abstract class CrossSectionObject implements XML_IO {
	final static int LateralReferenceEntireWidth = 4;
	protected double longitudinalPosition = Double.NaN;
	protected double longitudinalLength = Double.NaN;
	protected double longitudinalRepeat = Double.NaN;
	protected double lateralPosition = Double.NaN;
	protected double lateralWidth = Double.NaN;
	protected double lateralRepeat = Double.NaN;
	protected int lateralReference = -1;
	protected CrossSectionElement crossSectionElement;
	
	/**
	 * Retrieve the longitudinal position of this CrossSectionObject. Negative 
	 * values mean that the position is measured from the end of the
	 * {@link CrossSectionElement} that owns this StopLine). The value NaN
	 * indicates that this CrossSectionObject does not have a longitudinal
	 * position.
	 * <br /> If this CrossSectionObject has a valid 
	 * {@link #longitudinalRepeat}, the longitudinal position returned applies
	 * to the first location where this CrossSectionObject is painted.
	 * Additional copies will be painted at longitudinalRepeat intervals until
	 * the end of this CrossSectionElement.
	 * @return Double; the longitudinal position of this CrossSectionObject
	 */
	public double getLongitudinalPosition() {
		return longitudinalPosition;
	}
	
	/** 
	 * Retrieve the longitudinal length of this CrossSectionObject.
	 * <br /> If this CrossSectionObject does not have a longitudinal length,
	 * this method returns NaN.
	 * @return Double; the longitudinal length, or NaN
	 */
	public double getLongitudinalLength() {
		return longitudinalLength;
	}
	
	/**
	 * Retrieve the longitudinal repeat interval of this CrossSectionObject.
	 * If this CrossSectionObject does not have a longitudinal position, or
	 * is not repeated longitudinally, this method returns NaN.
	 * @return Double; the longitudinal repeat interval, or NaN
	 */
	public double getLongitudinalRepeat() {
		return longitudinalRepeat;
	}
	
	/**
	 * Retrieve the lateral position of this CrossSectionObject. Lateral
	 * position is relative to the {@link #lateralReference} of this
	 * CrossSectionObject. The value NaN indicates that this CrossSectionObject
	 * has not lateral position.
	 * <br /> If this CrossSectionObject has a valid {@link #lateralRepeat},
	 * the lateral position refers to the left-most location where this
	 * CrossSectionObject is painted. Additional copies will be painted at
	 * lateralRepeat intervals until the right edge of this CrossSectionElement.
	 * @return Double; the lateral position of this CrossSectionObject
	 */
	public double getLateralPosition() {
		return lateralPosition;
	}
	
	/**
	 * Retrieve the lateral width of this CrossSectionObject. The value NaN
	 * indicates that this CrossSectionObject has no lateral width, or always
	 * has the width of the parent {@link CrossSectionElement}.
	 * @return Double; the lateral width or NaN
	 */
	public double getLateralWidth() {
		return lateralWidth;
	}

	/**
	 * Retrieve the lateral repeat interval of this CrossSectionObject.
	 * If this CrossSectionObject does not have a lateral position, or
	 * is not repeated laterally, this method returns NaN.
	 * @return Double; the lateral repeat interval, or NaN
	 */
	public double getLateralRepeat() {
		return lateralRepeat;
	}
	
	/**
	 * Retrieve the lateral reference of this CrossSectionObject.
	 * <br />The lateral reference is one of the values
	 * <ul>
	 * <li>CrossSectionElement.LateralReferenceLeft</li>
	 * <li>CrossSectionElement.LateralReferenceCenter</li>
	 * <li>CrossSectionElement.LateralReferenceRight</li>
	 * <li>CrossSectionObject.LateralReferenceEntireWidth</li>
	 * <li>-1 if this CrossSectionElement does not have a lateral position</li>
	 * </ul>
	 * @return Integer
	 */
	public int getLateralReference () {
		return lateralReference;
	}
	
	/**
	 * Set the {@link CrossSectionElement} of this CrossSectionObject.
	 * <br /> This can only be done if the current value is null!
	 * @param crossSectionElement {@link CrossSectionElement}; the new owner
	 * of this CrossSectionObject 
	 */
	public void setCrossSectionElement(CrossSectionElement crossSectionElement) {
		if ((null != this.crossSectionElement) && (this.crossSectionElement != crossSectionElement))
			throw new Error ("This CrossSectionObject alread has crossSectionElement set");
		this.crossSectionElement = crossSectionElement;		
	}
	
	/**
	 * Draw this CrossSectionObject onto a {@link GraphicsPanel}.
	 * @param graphicsPanel {@link GraphicsPanel}; drawing target
	 */
	public abstract void paint(GraphicsPanel graphicsPanel);
	
	/**
	 * Write this CrossSectionObject to an XML file.
	 * <br /> Automatically generated objects should do nothing in this method
	 * and always return true.
	 * @param staxWriter {@link StaXWriter}; writer for the XML file
	 * @return Boolean; true on success; false on failure
	 */
	@Override
	public abstract boolean writeXML(StaXWriter staxWriter);
	
}