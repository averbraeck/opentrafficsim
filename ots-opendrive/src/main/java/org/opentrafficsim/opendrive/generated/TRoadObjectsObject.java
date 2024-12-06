
package org.opentrafficsim.opendrive.generated;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Describes common 3D objects that have a reference to a given road. Objects are items that influence a road by expanding, delimiting, and supplementing its course. The most common examples are parking spaces, crosswalks, and traffic barriers.
 * There are two ways to describe the bounding box of objects.
 * 	- For an angular object: definition of the width, length and height.
 * 	- For a circular object: definition of the radius and height.
 * 
 * <p>Java class for t_road_objects_object complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_objects_object">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="repeat" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_repeat" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="outline" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_outlines_outline" minOccurs="0"/>
 *         <element name="outlines" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_outlines" minOccurs="0"/>
 *         <element name="material" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_material" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="validity" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_laneValidity" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="parkingSpace" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_parkingSpace" minOccurs="0"/>
 *         <element name="markings" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_markings" minOccurs="0"/>
 *         <element name="borders" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_borders" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="t" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="zOffset" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="type" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_objectType" />
 *       <attribute name="validLength" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="orientation" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_orientation" />
 *       <attribute name="subtype" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="dynamic" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_yesNo" />
 *       <attribute name="hdg" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="pitch" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="roll" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="height" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="s" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="length" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="width" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="radius" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_objects_object", propOrder = {
    "repeat",
    "outline",
    "outlines",
    "material",
    "validity",
    "parkingSpace",
    "markings",
    "borders",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadObjectsObject
    extends OpenDriveElement
{

    protected List<TRoadObjectsObjectRepeat> repeat;
    protected TRoadObjectsObjectOutlinesOutline outline;
    protected TRoadObjectsObjectOutlines outlines;
    protected List<TRoadObjectsObjectMaterial> material;
    protected List<TRoadObjectsObjectLaneValidity> validity;
    protected TRoadObjectsObjectParkingSpace parkingSpace;
    protected TRoadObjectsObjectMarkings markings;
    protected TRoadObjectsObjectBorders borders;
    /**
     * OpenDRIVE offers the possibility to include external data. The processing of this data depends on the application.
     * Additional data may be placed at any position in OpenDRIVE.
     * 
     */
    @XmlElements({
        @XmlElement(name = "include", type = TInclude.class),
        @XmlElement(name = "userData", type = TUserData.class),
        @XmlElement(name = "dataQuality", type = TDataQuality.class)
    })
    protected List<Object> gAdditionalData;
    /**
     * t-coordinate of object's origin
     * 
     */
    @XmlAttribute(name = "t", required = true)
    protected double t;
    /**
     * z-offset of object's origin relative to the elevation of the reference line
     * 
     */
    @XmlAttribute(name = "zOffset", required = true)
    protected double zOffset;
    /**
     * Type of object. For values, see UML.
     * For a parking space, the <parkingSpace> element may be used additionally.
     * 
     */
    @XmlAttribute(name = "type")
    protected EObjectType type;
    /**
     * Validity of object along s-axis (0.0 for point object)
     * 
     */
    @XmlAttribute(name = "validLength")
    protected Double validLength;
    /**
     * "+" = valid in positive s-direction
     * "-" = valid in negative s-direction
     * "none" = valid in both directions
     * (does not affect the heading)
     * 
     */
    @XmlAttribute(name = "orientation")
    protected String orientation;
    /**
     * Variant of a type
     * 
     */
    @XmlAttribute(name = "subtype")
    protected String subtype;
    /**
     * Indicates whether the object is dynamic or static, default value is “no” (static). Dynamic object cannot change its position.
     * 
     */
    @XmlAttribute(name = "dynamic")
    protected TYesNo dynamic;
    /**
     * Heading angle of the object relative to road direction
     * 
     */
    @XmlAttribute(name = "hdg")
    protected Double hdg;
    /**
     * Name of the object. May be chosen freely.
     * 
     */
    @XmlAttribute(name = "name")
    protected String name;
    /**
     * Pitch angle relative to the x/y-plane
     * 
     */
    @XmlAttribute(name = "pitch")
    protected Double pitch;
    /**
     * Unique ID within database
     * 
     */
    @XmlAttribute(name = "id", required = true)
    protected String id;
    /**
     * Roll angle relative to the x/y-plane
     * 
     */
    @XmlAttribute(name = "roll")
    protected Double roll;
    /**
     * Height of the object's bounding box. @height is defined in the local coordinate system u/v along the z-axis
     * 
     */
    @XmlAttribute(name = "height")
    protected Double height;
    /**
     * s-coordinate of object's origin
     * 
     */
    @XmlAttribute(name = "s", required = true)
    protected double s;
    /**
     * Length of the object's bounding box, alternative to @radius.
     * @length is defined in the local coordinate system u/v along the v-axis
     * 
     */
    @XmlAttribute(name = "length")
    protected Double length;
    /**
     * Width of the object's bounding box, alternative to @radius.
     * @width is defined in the local coordinate system u/v along the u-axis
     * 
     */
    @XmlAttribute(name = "width")
    protected Double width;
    /**
     * radius of the circular object's bounding box, alternative to @length and @width. @radius is defined in the local coordinate system u/v
     * 
     */
    @XmlAttribute(name = "radius")
    protected Double radius;

    /**
     * Gets the value of the repeat property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the repeat property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getRepeat().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadObjectsObjectRepeat }
     * </p>
     * 
     * 
     * @return
     *     The value of the repeat property.
     */
    public List<TRoadObjectsObjectRepeat> getRepeat() {
        if (repeat == null) {
            repeat = new ArrayList<>();
        }
        return this.repeat;
    }

    /**
     * Gets the value of the outline property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadObjectsObjectOutlinesOutline }
     *     
     */
    public TRoadObjectsObjectOutlinesOutline getOutline() {
        return outline;
    }

    /**
     * Sets the value of the outline property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadObjectsObjectOutlinesOutline }
     *     
     */
    public void setOutline(TRoadObjectsObjectOutlinesOutline value) {
        this.outline = value;
    }

    /**
     * Gets the value of the outlines property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadObjectsObjectOutlines }
     *     
     */
    public TRoadObjectsObjectOutlines getOutlines() {
        return outlines;
    }

    /**
     * Sets the value of the outlines property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadObjectsObjectOutlines }
     *     
     */
    public void setOutlines(TRoadObjectsObjectOutlines value) {
        this.outlines = value;
    }

    /**
     * Gets the value of the material property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the material property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getMaterial().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadObjectsObjectMaterial }
     * </p>
     * 
     * 
     * @return
     *     The value of the material property.
     */
    public List<TRoadObjectsObjectMaterial> getMaterial() {
        if (material == null) {
            material = new ArrayList<>();
        }
        return this.material;
    }

    /**
     * Gets the value of the validity property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the validity property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getValidity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadObjectsObjectLaneValidity }
     * </p>
     * 
     * 
     * @return
     *     The value of the validity property.
     */
    public List<TRoadObjectsObjectLaneValidity> getValidity() {
        if (validity == null) {
            validity = new ArrayList<>();
        }
        return this.validity;
    }

    /**
     * Gets the value of the parkingSpace property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadObjectsObjectParkingSpace }
     *     
     */
    public TRoadObjectsObjectParkingSpace getParkingSpace() {
        return parkingSpace;
    }

    /**
     * Sets the value of the parkingSpace property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadObjectsObjectParkingSpace }
     *     
     */
    public void setParkingSpace(TRoadObjectsObjectParkingSpace value) {
        this.parkingSpace = value;
    }

    /**
     * Gets the value of the markings property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadObjectsObjectMarkings }
     *     
     */
    public TRoadObjectsObjectMarkings getMarkings() {
        return markings;
    }

    /**
     * Sets the value of the markings property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadObjectsObjectMarkings }
     *     
     */
    public void setMarkings(TRoadObjectsObjectMarkings value) {
        this.markings = value;
    }

    /**
     * Gets the value of the borders property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadObjectsObjectBorders }
     *     
     */
    public TRoadObjectsObjectBorders getBorders() {
        return borders;
    }

    /**
     * Sets the value of the borders property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadObjectsObjectBorders }
     *     
     */
    public void setBorders(TRoadObjectsObjectBorders value) {
        this.borders = value;
    }

    /**
     * OpenDRIVE offers the possibility to include external data. The processing of this data depends on the application.
     * Additional data may be placed at any position in OpenDRIVE.
     * 
     * Gets the value of the gAdditionalData property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gAdditionalData property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getGAdditionalData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TDataQuality }
     * {@link TInclude }
     * {@link TUserData }
     * </p>
     * 
     * 
     * @return
     *     The value of the gAdditionalData property.
     */
    public List<Object> getGAdditionalData() {
        if (gAdditionalData == null) {
            gAdditionalData = new ArrayList<>();
        }
        return this.gAdditionalData;
    }

    /**
     * t-coordinate of object's origin
     * 
     */
    public double getT() {
        return t;
    }

    /**
     * Sets the value of the t property.
     * 
     */
    public void setT(double value) {
        this.t = value;
    }

    /**
     * z-offset of object's origin relative to the elevation of the reference line
     * 
     */
    public double getZOffset() {
        return zOffset;
    }

    /**
     * Sets the value of the zOffset property.
     * 
     */
    public void setZOffset(double value) {
        this.zOffset = value;
    }

    /**
     * Type of object. For values, see UML.
     * For a parking space, the <parkingSpace> element may be used additionally.
     * 
     * @return
     *     possible object is
     *     {@link EObjectType }
     *     
     */
    public EObjectType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link EObjectType }
     *     
     * @see #getType()
     */
    public void setType(EObjectType value) {
        this.type = value;
    }

    /**
     * Validity of object along s-axis (0.0 for point object)
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getValidLength() {
        return validLength;
    }

    /**
     * Sets the value of the validLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getValidLength()
     */
    public void setValidLength(Double value) {
        this.validLength = value;
    }

    /**
     * "+" = valid in positive s-direction
     * "-" = valid in negative s-direction
     * "none" = valid in both directions
     * (does not affect the heading)
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrientation() {
        return orientation;
    }

    /**
     * Sets the value of the orientation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getOrientation()
     */
    public void setOrientation(String value) {
        this.orientation = value;
    }

    /**
     * Variant of a type
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * Sets the value of the subtype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getSubtype()
     */
    public void setSubtype(String value) {
        this.subtype = value;
    }

    /**
     * Indicates whether the object is dynamic or static, default value is “no” (static). Dynamic object cannot change its position.
     * 
     * @return
     *     possible object is
     *     {@link TYesNo }
     *     
     */
    public TYesNo getDynamic() {
        return dynamic;
    }

    /**
     * Sets the value of the dynamic property.
     * 
     * @param value
     *     allowed object is
     *     {@link TYesNo }
     *     
     * @see #getDynamic()
     */
    public void setDynamic(TYesNo value) {
        this.dynamic = value;
    }

    /**
     * Heading angle of the object relative to road direction
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getHdg() {
        return hdg;
    }

    /**
     * Sets the value of the hdg property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getHdg()
     */
    public void setHdg(Double value) {
        this.hdg = value;
    }

    /**
     * Name of the object. May be chosen freely.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getName()
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Pitch angle relative to the x/y-plane
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getPitch() {
        return pitch;
    }

    /**
     * Sets the value of the pitch property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getPitch()
     */
    public void setPitch(Double value) {
        this.pitch = value;
    }

    /**
     * Unique ID within database
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getId()
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Roll angle relative to the x/y-plane
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRoll() {
        return roll;
    }

    /**
     * Sets the value of the roll property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getRoll()
     */
    public void setRoll(Double value) {
        this.roll = value;
    }

    /**
     * Height of the object's bounding box. @height is defined in the local coordinate system u/v along the z-axis
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getHeight()
     */
    public void setHeight(Double value) {
        this.height = value;
    }

    /**
     * s-coordinate of object's origin
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
     * Length of the object's bounding box, alternative to @radius.
     * @length is defined in the local coordinate system u/v along the v-axis
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getLength()
     */
    public void setLength(Double value) {
        this.length = value;
    }

    /**
     * Width of the object's bounding box, alternative to @radius.
     * @width is defined in the local coordinate system u/v along the u-axis
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getWidth()
     */
    public void setWidth(Double value) {
        this.width = value;
    }

    /**
     * radius of the circular object's bounding box, alternative to @length and @width. @radius is defined in the local coordinate system u/v
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRadius() {
        return radius;
    }

    /**
     * Sets the value of the radius property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getRadius()
     */
    public void setRadius(Double value) {
        this.radius = value;
    }

}
