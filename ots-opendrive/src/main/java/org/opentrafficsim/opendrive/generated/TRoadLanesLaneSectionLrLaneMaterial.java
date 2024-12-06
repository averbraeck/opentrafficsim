
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Stores information about the material of lanes. Each element is valid until a new element is defined. If multiple elements are defined, they must be listed in increasing order.
 * 
 * <p>Java class for t_road_lanes_laneSection_lr_lane_material complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes_laneSection_lr_lane_material">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="sOffset" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="surface" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="friction" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="roughness" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes_laneSection_lr_lane_material")
@SuppressWarnings("all") public class TRoadLanesLaneSectionLrLaneMaterial
    extends OpenDriveElement
{

    /**
     * s-coordinate of start position, relative to the position of the preceding <laneSection> element
     * 
     */
    @XmlAttribute(name = "sOffset", required = true)
    protected double sOffset;
    /**
     * Surface material code, depending on application
     * 
     */
    @XmlAttribute(name = "surface")
    protected String surface;
    /**
     * Friction coefficient
     * 
     */
    @XmlAttribute(name = "friction", required = true)
    protected double friction;
    /**
     * Roughness, for example, for sound and motion systems
     * 
     */
    @XmlAttribute(name = "roughness")
    protected Double roughness;

    /**
     * s-coordinate of start position, relative to the position of the preceding <laneSection> element
     * 
     */
    public double getSOffset() {
        return sOffset;
    }

    /**
     * Sets the value of the sOffset property.
     * 
     */
    public void setSOffset(double value) {
        this.sOffset = value;
    }

    /**
     * Surface material code, depending on application
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSurface() {
        return surface;
    }

    /**
     * Sets the value of the surface property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getSurface()
     */
    public void setSurface(String value) {
        this.surface = value;
    }

    /**
     * Friction coefficient
     * 
     */
    public double getFriction() {
        return friction;
    }

    /**
     * Sets the value of the friction property.
     * 
     */
    public void setFriction(double value) {
        this.friction = value;
    }

    /**
     * Roughness, for example, for sound and motion systems
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRoughness() {
        return roughness;
    }

    /**
     * Sets the value of the roughness property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getRoughness()
     */
    public void setRoughness(Double value) {
        this.roughness = value;
    }

}
