
package org.opentrafficsim.opendrive.generated;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Used to describe complex forms of objects. Defines a corner point on the object outline relative to the object pivot point in local u/v-coordinates. The pivot point and the orientation of the object are given by the s/t/heading arguments of the <object> element.
 * 
 * <p>Java class for t_road_objects_object_outlines_outline_cornerLocal complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_objects_object_outlines_outline_cornerLocal">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="u" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="v" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="z" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="height" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="id" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_objects_object_outlines_outline_cornerLocal")
@SuppressWarnings("all") public class TRoadObjectsObjectOutlinesOutlineCornerLocal
    extends OpenDriveElement
{

    /**
     * Local u-coordinate of the corner
     * 
     */
    @XmlAttribute(name = "u", required = true)
    protected double u;
    /**
     * Local v-coordinate of the corner
     * 
     */
    @XmlAttribute(name = "v", required = true)
    protected double v;
    /**
     * Local z-coordinate of the corner
     * 
     */
    @XmlAttribute(name = "z", required = true)
    protected double z;
    /**
     * Height of the object at this corner, along the z-axis
     * 
     */
    @XmlAttribute(name = "height", required = true)
    protected double height;
    /**
     * ID of the outline point. Shall be unique within one outline.
     * 
     */
    @XmlAttribute(name = "id")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger id;

    /**
     * Local u-coordinate of the corner
     * 
     */
    public double getU() {
        return u;
    }

    /**
     * Sets the value of the u property.
     * 
     */
    public void setU(double value) {
        this.u = value;
    }

    /**
     * Local v-coordinate of the corner
     * 
     */
    public double getV() {
        return v;
    }

    /**
     * Sets the value of the v property.
     * 
     */
    public void setV(double value) {
        this.v = value;
    }

    /**
     * Local z-coordinate of the corner
     * 
     */
    public double getZ() {
        return z;
    }

    /**
     * Sets the value of the z property.
     * 
     */
    public void setZ(double value) {
        this.z = value;
    }

    /**
     * Height of the object at this corner, along the z-axis
     * 
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     */
    public void setHeight(double value) {
        this.height = value;
    }

    /**
     * ID of the outline point. Shall be unique within one outline.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     * @see #getId()
     */
    public void setId(BigInteger value) {
        this.id = value;
    }

}
