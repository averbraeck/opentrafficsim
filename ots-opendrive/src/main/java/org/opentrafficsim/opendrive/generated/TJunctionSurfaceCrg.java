
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Data described in OpenCRG are represented by the <CRG> element within the <surface> element.
 * 
 * <p>Java class for t_junction_surface_CRG complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_junction_surface_CRG">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="file" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="mode" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_road_surface_CRG_mode" fixed="global" />
 *       <attribute name="purpose" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_road_surface_CRG_purpose" />
 *       <attribute name="zOffset" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="zScale" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_junction_surface_CRG")
@SuppressWarnings("all") public class TJunctionSurfaceCrg
    extends OpenDriveElement
{

    /**
     * Name of the file containing the CRG data
     * 
     */
    @XmlAttribute(name = "file", required = true)
    protected String file;
    /**
     * Attachment mode for the surface data.
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
     * z offset between CRG center line and inertial xy-plane
     * (default = 0.0)
     * 
     */
    @XmlAttribute(name = "zOffset")
    protected Double zOffset;
    /**
     * z scale factor for the surface description (default = 1.0)
     * 
     */
    @XmlAttribute(name = "zScale")
    protected Double zScale;

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
     * Attachment mode for the surface data.
     * 
     * @return
     *     possible object is
     *     {@link ERoadSurfaceCrgMode }
     *     
     */
    public ERoadSurfaceCrgMode getMode() {
        if (mode == null) {
            return ERoadSurfaceCrgMode.GLOBAL;
        } else {
            return mode;
        }
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
     * z offset between CRG center line and inertial xy-plane
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
     * z scale factor for the surface description (default = 1.0)
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

}
