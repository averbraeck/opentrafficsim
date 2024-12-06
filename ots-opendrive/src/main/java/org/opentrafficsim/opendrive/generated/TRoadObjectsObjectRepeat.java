
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * To avoid lengthy XML code, objects of the same type may be repeated. The attributes of the repeated object may be changed. Attributes of the repeated object shall overrule the attributes from the original object. If attributes are omitted in the repeated objects, the attributes from the original object apply.
 * 
 * <p>Java class for t_road_objects_object_repeat complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
     * Sets the value of the s property.
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
     * Sets the value of the length property.
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
     * Sets the value of the distance property.
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
     * Sets the value of the tStart property.
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
     * Sets the value of the tEnd property.
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
     * Sets the value of the heightStart property.
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
     * Sets the value of the heightEnd property.
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
     * Sets the value of the zOffsetStart property.
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
     * Sets the value of the zOffsetEnd property.
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
     * Sets the value of the widthStart property.
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
     * Sets the value of the widthEnd property.
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
     * Sets the value of the lengthStart property.
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
     * Sets the value of the lengthEnd property.
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
     * Sets the value of the radiusStart property.
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
     * Sets the value of the radiusEnd property.
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
