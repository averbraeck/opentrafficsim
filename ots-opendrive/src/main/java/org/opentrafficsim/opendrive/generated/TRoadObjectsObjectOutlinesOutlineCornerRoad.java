
package org.opentrafficsim.opendrive.generated;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Defines a corner point on the object’s outline in road coordinates.
 * 
 * <p>Java-Klasse für t_road_objects_object_outlines_outline_cornerRoad complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_objects_object_outlines_outline_cornerRoad">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="s" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="t" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="dz" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
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
@XmlType(name = "t_road_objects_object_outlines_outline_cornerRoad")
@SuppressWarnings("all") public class TRoadObjectsObjectOutlinesOutlineCornerRoad
    extends OpenDriveElement
{

    /**
     * s-coordinate of the corner
     * 
     */
    @XmlAttribute(name = "s", required = true)
    protected double s;
    /**
     * t-coordinate of the corner
     * 
     */
    @XmlAttribute(name = "t", required = true)
    protected double t;
    /**
     * dz of the corner relative to road reference line
     * 
     */
    @XmlAttribute(name = "dz", required = true)
    protected double dz;
    /**
     * Height of the object at this corner, along the z-axis
     * 
     */
    @XmlAttribute(name = "height", required = true)
    protected double height;
    /**
     * ID of the outline point. Must be unique within one outline
     * 
     */
    @XmlAttribute(name = "id")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger id;

    /**
     * s-coordinate of the corner
     * 
     */
    public double getS() {
        return s;
    }

    /**
     * Legt den Wert der s-Eigenschaft fest.
     * 
     */
    public void setS(double value) {
        this.s = value;
    }

    /**
     * t-coordinate of the corner
     * 
     */
    public double getT() {
        return t;
    }

    /**
     * Legt den Wert der t-Eigenschaft fest.
     * 
     */
    public void setT(double value) {
        this.t = value;
    }

    /**
     * dz of the corner relative to road reference line
     * 
     */
    public double getDz() {
        return dz;
    }

    /**
     * Legt den Wert der dz-Eigenschaft fest.
     * 
     */
    public void setDz(double value) {
        this.dz = value;
    }

    /**
     * Height of the object at this corner, along the z-axis
     * 
     */
    public double getHeight() {
        return height;
    }

    /**
     * Legt den Wert der height-Eigenschaft fest.
     * 
     */
    public void setHeight(double value) {
        this.height = value;
    }

    /**
     * ID of the outline point. Must be unique within one outline
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
     * Legt den Wert der id-Eigenschaft fest.
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
