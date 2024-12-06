
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Data described in OpenCRG is represented by the <CRG> element within the <surface> element.
 * 
 * <p>Java class for t_road_surface_CRG complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_surface_CRG">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="file" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="sStart" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="sEnd" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="orientation" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_direction" />
 *       <attribute name="mode" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_road_surface_CRG_mode" />
 *       <attribute name="purpose" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_road_surface_CRG_purpose" />
 *       <attribute name="sOffset" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="tOffset" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="zOffset" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="zScale" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="hOffset" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_surface_CRG")
@SuppressWarnings("all") public class TRoadSurfaceCrg
    extends OpenDriveElement
{

    /**
     * Name of the file containing the CRG data
     * 
     */
    @XmlAttribute(name = "file", required = true)
    protected String file;
    /**
     * Start of the application of CRG data
     * (s-coordinate)
     * 
     */
    @XmlAttribute(name = "sStart", required = true)
    protected double sStart;
    /**
     * End of the application of CRG
     * (s-coordinate)
     * 
     */
    @XmlAttribute(name = "sEnd", required = true)
    protected double sEnd;
    /**
     * Orientation of the CRG data set relative to the parent <road> element
     * 
     */
    @XmlAttribute(name = "orientation", required = true)
    protected EDirection orientation;
    /**
     * Attachment mode for the surface data, see specification.
     * 
     */
    @XmlAttribute(name = "mode", required = true)
    protected ERoadSurfaceCrgMode mode;
    /**
     * Physical purpose of the data contained in the CRG file; if the attribute is missing, data will be interpreted as elevation data.
     * 
     */
    @XmlAttribute(name = "purpose")
    protected ERoadSurfaceCrgPurpose purpose;
    /**
     * s-offset between CRG center line and reference line of the road 
     * (default = 0.0)
     * 
     */
    @XmlAttribute(name = "sOffset")
    protected Double sOffset;
    /**
     * t-offset between CRG center line and reference line of the road
     *  (default = 0.0)
     * 
     */
    @XmlAttribute(name = "tOffset")
    protected Double tOffset;
    /**
     * z-offset between CRG center line and reference line of the road 
     * (default = 0.0)
     * 
     */
    @XmlAttribute(name = "zOffset")
    protected Double zOffset;
    /**
     * z-scale factor for the surface description (default = 1.0)
     * 
     */
    @XmlAttribute(name = "zScale")
    protected Double zScale;
    /**
     * Heading offset between CRG center line and reference line of the road (required for mode genuine only, default = 0.0)
     * 
     */
    @XmlAttribute(name = "hOffset")
    protected Double hOffset;

    /**
     * Name of the file containing the CRG data
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getFile()
     */
    public void setFile(String value) {
        this.file = value;
    }

    /**
     * Start of the application of CRG data
     * (s-coordinate)
     * 
     */
    public double getSStart() {
        return sStart;
    }

    /**
     * Sets the value of the sStart property.
     * 
     */
    public void setSStart(double value) {
        this.sStart = value;
    }

    /**
     * End of the application of CRG
     * (s-coordinate)
     * 
     */
    public double getSEnd() {
        return sEnd;
    }

    /**
     * Sets the value of the sEnd property.
     * 
     */
    public void setSEnd(double value) {
        this.sEnd = value;
    }

    /**
     * Orientation of the CRG data set relative to the parent <road> element
     * 
     * @return
     *     possible object is
     *     {@link EDirection }
     *     
     */
    public EDirection getOrientation() {
        return orientation;
    }

    /**
     * Sets the value of the orientation property.
     * 
     * @param value
     *     allowed object is
     *     {@link EDirection }
     *     
     * @see #getOrientation()
     */
    public void setOrientation(EDirection value) {
        this.orientation = value;
    }

    /**
     * Attachment mode for the surface data, see specification.
     * 
     * @return
     *     possible object is
     *     {@link ERoadSurfaceCrgMode }
     *     
     */
    public ERoadSurfaceCrgMode getMode() {
        return mode;
    }

    /**
     * Sets the value of the mode property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERoadSurfaceCrgMode }
     *     
     * @see #getMode()
     */
    public void setMode(ERoadSurfaceCrgMode value) {
        this.mode = value;
    }

    /**
     * Physical purpose of the data contained in the CRG file; if the attribute is missing, data will be interpreted as elevation data.
     * 
     * @return
     *     possible object is
     *     {@link ERoadSurfaceCrgPurpose }
     *     
     */
    public ERoadSurfaceCrgPurpose getPurpose() {
        return purpose;
    }

    /**
     * Sets the value of the purpose property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERoadSurfaceCrgPurpose }
     *     
     * @see #getPurpose()
     */
    public void setPurpose(ERoadSurfaceCrgPurpose value) {
        this.purpose = value;
    }

    /**
     * s-offset between CRG center line and reference line of the road 
     * (default = 0.0)
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getSOffset() {
        return sOffset;
    }

    /**
     * Sets the value of the sOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getSOffset()
     */
    public void setSOffset(Double value) {
        this.sOffset = value;
    }

    /**
     * t-offset between CRG center line and reference line of the road
     *  (default = 0.0)
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getTOffset() {
        return tOffset;
    }

    /**
     * Sets the value of the tOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getTOffset()
     */
    public void setTOffset(Double value) {
        this.tOffset = value;
    }

    /**
     * z-offset between CRG center line and reference line of the road 
     * (default = 0.0)
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getZOffset() {
        return zOffset;
    }

    /**
     * Sets the value of the zOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getZOffset()
     */
    public void setZOffset(Double value) {
        this.zOffset = value;
    }

    /**
     * z-scale factor for the surface description (default = 1.0)
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getZScale() {
        return zScale;
    }

    /**
     * Sets the value of the zScale property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getZScale()
     */
    public void setZScale(Double value) {
        this.zScale = value;
    }

    /**
     * Heading offset between CRG center line and reference line of the road (required for mode genuine only, default = 0.0)
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getHOffset() {
        return hOffset;
    }

    /**
     * Sets the value of the hOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getHOffset()
     */
    public void setHOffset(Double value) {
        this.hOffset = value;
    }

}
