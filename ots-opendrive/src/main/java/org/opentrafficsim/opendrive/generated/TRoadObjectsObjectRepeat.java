
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * To avoid lengthy XML code, objects of the same type may be repeated. The attributes of the repeated object may be changed. Attributes of the repeated object shall overrule the attributes from the original object. If attributes are omitted in the repeated objects, the attributes from the original object apply.
 * 
 * <p>Java-Klasse für t_road_objects_object_repeat complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_objects_object_repeat">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="s" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="length" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="distance" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="tStart" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="tEnd" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="heightStart" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="heightEnd" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="zOffsetStart" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="zOffsetEnd" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="widthStart" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="widthEnd" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="lengthStart" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="lengthEnd" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="radiusStart" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="radiusEnd" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_objects_object_repeat")
@SuppressWarnings("all") public class TRoadObjectsObjectRepeat
    extends OpenDriveElement
{

    /**
     * s-coordinate of start position, overrides the corresponding argument in the original <object> record
     * 
     */
    @XmlAttribute(name = "s", required = true)
    protected double s;
    /**
     * Length of the repeat area, along the reference line in s-direction.
     * 
     */
    @XmlAttribute(name = "length", required = true)
    protected double length;
    /**
     * Distance between two instances of the object;
     * If this value is zero, then the object is treated like a continuous feature, for example, a guard rail, a wall, etc.
     * 
     */
    @XmlAttribute(name = "distance", required = true)
    protected double distance;
    /**
     * Lateral offset of objects reference point at @s
     * 
     */
    @XmlAttribute(name = "tStart", required = true)
    protected double tStart;
    /**
     * Lateral offset of object's reference point at @s + @length
     * 
     */
    @XmlAttribute(name = "tEnd", required = true)
    protected double tEnd;
    /**
     * Height of the object at @s
     * 
     */
    @XmlAttribute(name = "heightStart", required = true)
    protected double heightStart;
    /**
     * Height of the object at @s + @length
     * 
     */
    @XmlAttribute(name = "heightEnd", required = true)
    protected double heightEnd;
    /**
     * z-offset of the object at @s, relative to the elevation of the reference line
     * 
     */
    @XmlAttribute(name = "zOffsetStart", required = true)
    protected double zOffsetStart;
    /**
     * z-offset of the object at @s + @length, relative to the elevation of the reference line
     * 
     */
    @XmlAttribute(name = "zOffsetEnd", required = true)
    protected double zOffsetEnd;
    /**
     * Width of the object at @s
     * 
     */
    @XmlAttribute(name = "widthStart")
    protected Double widthStart;
    /**
     * Width of the object at @s + @length
     * 
     */
    @XmlAttribute(name = "widthEnd")
    protected Double widthEnd;
    /**
     * Length of the object at @sh
     * 
     */
    @XmlAttribute(name = "lengthStart")
    protected Double lengthStart;
    /**
     * Length of the object at @s + @length
     * 
     */
    @XmlAttribute(name = "lengthEnd")
    protected Double lengthEnd;
    /**
     * Radius of the object at @s
     * 
     */
    @XmlAttribute(name = "radiusStart")
    protected Double radiusStart;
    /**
     * Radius of the object at @s + @length
     * 
     */
    @XmlAttribute(name = "radiusEnd")
    protected Double radiusEnd;

    /**
     * s-coordinate of start position, overrides the corresponding argument in the original <object> record
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
     * Length of the repeat area, along the reference line in s-direction.
     * 
     */
    public double getLength() {
        return length;
    }

    /**
     * Legt den Wert der length-Eigenschaft fest.
     * 
     */
    public void setLength(double value) {
        this.length = value;
    }

    /**
     * Distance between two instances of the object;
     * If this value is zero, then the object is treated like a continuous feature, for example, a guard rail, a wall, etc.
     * 
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Legt den Wert der distance-Eigenschaft fest.
     * 
     */
    public void setDistance(double value) {
        this.distance = value;
    }

    /**
     * Lateral offset of objects reference point at @s
     * 
     */
    public double getTStart() {
        return tStart;
    }

    /**
     * Legt den Wert der tStart-Eigenschaft fest.
     * 
     */
    public void setTStart(double value) {
        this.tStart = value;
    }

    /**
     * Lateral offset of object's reference point at @s + @length
     * 
     */
    public double getTEnd() {
        return tEnd;
    }

    /**
     * Legt den Wert der tEnd-Eigenschaft fest.
     * 
     */
    public void setTEnd(double value) {
        this.tEnd = value;
    }

    /**
     * Height of the object at @s
     * 
     */
    public double getHeightStart() {
        return heightStart;
    }

    /**
     * Legt den Wert der heightStart-Eigenschaft fest.
     * 
     */
    public void setHeightStart(double value) {
        this.heightStart = value;
    }

    /**
     * Height of the object at @s + @length
     * 
     */
    public double getHeightEnd() {
        return heightEnd;
    }

    /**
     * Legt den Wert der heightEnd-Eigenschaft fest.
     * 
     */
    public void setHeightEnd(double value) {
        this.heightEnd = value;
    }

    /**
     * z-offset of the object at @s, relative to the elevation of the reference line
     * 
     */
    public double getZOffsetStart() {
        return zOffsetStart;
    }

    /**
     * Legt den Wert der zOffsetStart-Eigenschaft fest.
     * 
     */
    public void setZOffsetStart(double value) {
        this.zOffsetStart = value;
    }

    /**
     * z-offset of the object at @s + @length, relative to the elevation of the reference line
     * 
     */
    public double getZOffsetEnd() {
        return zOffsetEnd;
    }

    /**
     * Legt den Wert der zOffsetEnd-Eigenschaft fest.
     * 
     */
    public void setZOffsetEnd(double value) {
        this.zOffsetEnd = value;
    }

    /**
     * Width of the object at @s
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWidthStart() {
        return widthStart;
    }

    /**
     * Legt den Wert der widthStart-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getWidthStart()
     */
    public void setWidthStart(Double value) {
        this.widthStart = value;
    }

    /**
     * Width of the object at @s + @length
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWidthEnd() {
        return widthEnd;
    }

    /**
     * Legt den Wert der widthEnd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getWidthEnd()
     */
    public void setWidthEnd(Double value) {
        this.widthEnd = value;
    }

    /**
     * Length of the object at @sh
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLengthStart() {
        return lengthStart;
    }

    /**
     * Legt den Wert der lengthStart-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getLengthStart()
     */
    public void setLengthStart(Double value) {
        this.lengthStart = value;
    }

    /**
     * Length of the object at @s + @length
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLengthEnd() {
        return lengthEnd;
    }

    /**
     * Legt den Wert der lengthEnd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getLengthEnd()
     */
    public void setLengthEnd(Double value) {
        this.lengthEnd = value;
    }

    /**
     * Radius of the object at @s
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRadiusStart() {
        return radiusStart;
    }

    /**
     * Legt den Wert der radiusStart-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getRadiusStart()
     */
    public void setRadiusStart(Double value) {
        this.radiusStart = value;
    }

    /**
     * Radius of the object at @s + @length
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRadiusEnd() {
        return radiusEnd;
    }

    /**
     * Legt den Wert der radiusEnd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getRadiusEnd()
     */
    public void setRadiusEnd(Double value) {
        this.radiusEnd = value;
    }

}
