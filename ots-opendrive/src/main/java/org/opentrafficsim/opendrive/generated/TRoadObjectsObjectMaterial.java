
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Describes the material properties of objects, for example, patches that are part of the road surface but deviate from the standard road material. Supersedes the material specified in the &lt;road material&gt; element and is valid only within the outline of the parent road object.
 * 
 * <p>Java class for t_road_objects_object_material complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_objects_object_material">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="surface" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="friction" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="roughness" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_objects_object_material")
@SuppressWarnings("all") public class TRoadObjectsObjectMaterial
    extends OpenDriveElement
{

    /**
     * Surface material code, depending on application
     * 
     */
    @XmlAttribute(name = "surface")
    protected String surface;
    /**
     * Friction value, depending on application
     * 
     */
    @XmlAttribute(name = "friction")
    protected Double friction;
    /**
     * Roughness, for example, for sound and motion systems, depending on application
     * 
     */
    @XmlAttribute(name = "roughness")
    protected Double roughness;

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
     * Friction value, depending on application
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getFriction() {
        return friction;
    }

    /**
     * Sets the value of the friction property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getFriction()
     */
    public void setFriction(Double value) {
        this.friction = value;
    }

    /**
     * Roughness, for example, for sound and motion systems, depending on application
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
