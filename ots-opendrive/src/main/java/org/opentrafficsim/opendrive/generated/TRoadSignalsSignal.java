
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
 * Used to provide information about signals along a road. Consists of a main element and an optional lane validity element.
 * 
 * <p>Java class for t_road_signals_signal complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_signals_signal">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="validity" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_laneValidity" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="dependency" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_signals_signal_dependency" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="reference" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_signals_signal_reference" maxOccurs="unbounded" minOccurs="0"/>
 *         <choice minOccurs="0">
 *           <element name="positionRoad" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_signals_signal_positionRoad"/>
 *           <element name="positionInertial" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_signals_signal_positionInertial"/>
 *         </choice>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="s" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="t" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="dynamic" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_yesNo" />
 *       <attribute name="orientation" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_orientation" />
 *       <attribute name="zOffset" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="country" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_countryCode" />
 *       <attribute name="countryRevision" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="subtype" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="value" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="unit" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_unit" />
 *       <attribute name="height" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="width" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="text" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="hOffset" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="pitch" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="roll" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_signals_signal", propOrder = {
    "validity",
    "dependency",
    "reference",
    "positionRoad",
    "positionInertial",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadSignalsSignal
    extends OpenDriveElement
{

    protected List<TRoadObjectsObjectLaneValidity> validity;
    protected List<TRoadSignalsSignalDependency> dependency;
    protected List<TRoadSignalsSignalReference2> reference;
    protected TRoadSignalsSignalPositionRoad positionRoad;
    protected TRoadSignalsSignalPositionInertial positionInertial;
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
     * s-coordinate
     * 
     */
    @XmlAttribute(name = "s", required = true)
    protected double s;
    /**
     * t-coordinate
     * 
     */
    @XmlAttribute(name = "t", required = true)
    protected double t;
    /**
     * Unique ID of the signal within the OpenDRIVE file
     * 
     */
    @XmlAttribute(name = "id", required = true)
    protected String id;
    /**
     * Name of the signal. May be chosen freely.
     * 
     */
    @XmlAttribute(name = "name")
    protected String name;
    /**
     * Indicates whether the signal is dynamic or static. Example: traffic light is dynamic
     * 
     */
    @XmlAttribute(name = "dynamic", required = true)
    protected TYesNo dynamic;
    /**
     * "+" = valid in positive s- direction
     * "-" = valid in negative s- direction
     * "none" = valid in both directions
     * 
     */
    @XmlAttribute(name = "orientation", required = true)
    protected String orientation;
    /**
     * z offset from the road to bottom edge of the signal. This represents the vertical clearance of the object. Relative to the reference line.
     * 
     */
    @XmlAttribute(name = "zOffset", required = true)
    protected double zOffset;
    /**
     * Country code of the road, see ISO 3166-1, alpha-2 codes.
     * 
     */
    @XmlAttribute(name = "country")
    protected String country;
    @XmlAttribute(name = "countryRevision")
    protected String countryRevision;
    /**
     * Type identifier according to country code 
     * or "-1" / "none". See extra document.
     * 
     */
    @XmlAttribute(name = "type", required = true)
    protected String type;
    /**
     * Subtype identifier according to country code or "-1" / "none"
     * 
     */
    @XmlAttribute(name = "subtype", required = true)
    protected String subtype;
    /**
     * Value of the signal, if value is given, unit is mandatory
     * 
     */
    @XmlAttribute(name = "value")
    protected Double value;
    /**
     * Unit of @value
     * 
     */
    @XmlAttribute(name = "unit")
    protected String unit;
    /**
     * Height of the signal, measured from bottom edge of the signal
     * 
     */
    @XmlAttribute(name = "height")
    protected Double height;
    /**
     * Width of the signal
     * 
     */
    @XmlAttribute(name = "width")
    protected Double width;
    /**
     * Additional text associated with the signal, for example, text on city limit "City\nBadAibling"
     * 
     */
    @XmlAttribute(name = "text")
    protected String text;
    /**
     * Heading offset of the signal (relative to @orientation, if orientation is equal to “+” or “-“)
     * Heading offset of the signal (relative to reference line, if orientation is equal to “none” )
     * 
     */
    @XmlAttribute(name = "hOffset")
    protected Double hOffset;
    /**
     * Pitch angle of the signal, relative to the inertial system (xy-plane)
     * 
     */
    @XmlAttribute(name = "pitch")
    protected Double pitch;
    /**
     * Roll angle of the signal after applying pitch, relative to the inertial system (x’’y’’-plane)
     * 
     */
    @XmlAttribute(name = "roll")
    protected Double roll;

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
     * Gets the value of the dependency property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dependency property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getDependency().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadSignalsSignalDependency }
     * </p>
     * 
     * 
     * @return
     *     The value of the dependency property.
     */
    public List<TRoadSignalsSignalDependency> getDependency() {
        if (dependency == null) {
            dependency = new ArrayList<>();
        }
        return this.dependency;
    }

    /**
     * Gets the value of the reference property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reference property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getReference().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadSignalsSignalReference2 }
     * </p>
     * 
     * 
     * @return
     *     The value of the reference property.
     */
    public List<TRoadSignalsSignalReference2> getReference() {
        if (reference == null) {
            reference = new ArrayList<>();
        }
        return this.reference;
    }

    /**
     * Gets the value of the positionRoad property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadSignalsSignalPositionRoad }
     *     
     */
    public TRoadSignalsSignalPositionRoad getPositionRoad() {
        return positionRoad;
    }

    /**
     * Sets the value of the positionRoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadSignalsSignalPositionRoad }
     *     
     */
    public void setPositionRoad(TRoadSignalsSignalPositionRoad value) {
        this.positionRoad = value;
    }

    /**
     * Gets the value of the positionInertial property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadSignalsSignalPositionInertial }
     *     
     */
    public TRoadSignalsSignalPositionInertial getPositionInertial() {
        return positionInertial;
    }

    /**
     * Sets the value of the positionInertial property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadSignalsSignalPositionInertial }
     *     
     */
    public void setPositionInertial(TRoadSignalsSignalPositionInertial value) {
        this.positionInertial = value;
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
     * s-coordinate
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
     * t-coordinate
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
     * Unique ID of the signal within the OpenDRIVE file
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
     * Name of the signal. May be chosen freely.
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
     * Indicates whether the signal is dynamic or static. Example: traffic light is dynamic
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
     * "+" = valid in positive s- direction
     * "-" = valid in negative s- direction
     * "none" = valid in both directions
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
     * z offset from the road to bottom edge of the signal. This represents the vertical clearance of the object. Relative to the reference line.
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
     * Country code of the road, see ISO 3166-1, alpha-2 codes.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getCountry()
     */
    public void setCountry(String value) {
        this.country = value;
    }

    /**
     * Gets the value of the countryRevision property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountryRevision() {
        return countryRevision;
    }

    /**
     * Sets the value of the countryRevision property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountryRevision(String value) {
        this.countryRevision = value;
    }

    /**
     * Type identifier according to country code 
     * or "-1" / "none". See extra document.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getType()
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Subtype identifier according to country code or "-1" / "none"
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
     * Value of the signal, if value is given, unit is mandatory
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getValue()
     */
    public void setValue(Double value) {
        this.value = value;
    }

    /**
     * Unit of @value
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getUnit()
     */
    public void setUnit(String value) {
        this.unit = value;
    }

    /**
     * Height of the signal, measured from bottom edge of the signal
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
     * Width of the signal
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
     * Additional text associated with the signal, for example, text on city limit "City\nBadAibling"
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getText()
     */
    public void setText(String value) {
        this.text = value;
    }

    /**
     * Heading offset of the signal (relative to @orientation, if orientation is equal to “+” or “-“)
     * Heading offset of the signal (relative to reference line, if orientation is equal to “none” )
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

    /**
     * Pitch angle of the signal, relative to the inertial system (xy-plane)
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
     * Roll angle of the signal after applying pitch, relative to the inertial system (x’’y’’-plane)
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

}
