
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
 * It is possible to link an object with one or more roads, signals or other objects using a <objectReference> element. The referenced objects require a unique ID.
 * The object reference element consists of a main element and an optional lane validity element.
 * 
 * <p>Java class for t_road_objects_objectReference complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_objects_objectReference">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="validity" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_laneValidity" maxOccurs="unbounded" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="s" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="t" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="zOffset" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="validLength" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="orientation" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_orientation" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_objects_objectReference", propOrder = {
    "validity",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadObjectsObjectReference
    extends OpenDriveElement
{

    protected List<TRoadObjectsObjectLaneValidity> validity;
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
     * Unique ID of the referred object within the database
     * 
     */
    @XmlAttribute(name = "id", required = true)
    protected String id;
    /**
     * z offset relative to the elevation of the reference line
     * 
     */
    @XmlAttribute(name = "zOffset")
    protected Double zOffset;
    /**
     * Validity of the object along s-axis
     * (0.0 for point object)
     * 
     */
    @XmlAttribute(name = "validLength")
    protected Double validLength;
    /**
     * "+" = valid in positive s-direction
     * "-" = valid in negative s-direction
     * "none" = valid in both directions
     * 
     */
    @XmlAttribute(name = "orientation", required = true)
    protected String orientation;

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
     * Unique ID of the referred object within the database
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
     * z offset relative to the elevation of the reference line
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
     * Validity of the object along s-axis
     * (0.0 for point object)
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

}
