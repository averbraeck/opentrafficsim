
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
 * Tunnels are modeled as objects in OpenDRIVE. Tunnels apply to the entire cross section of the road within the given range unless a lane validity element with further restrictions is provided as child element.
 * 
 * <p>Java class for t_road_objects_tunnel complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_objects_tunnel">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="validity" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object_laneValidity" maxOccurs="unbounded" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="s" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="length" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="type" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_tunnelType" />
 *       <attribute name="lighting" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_zeroOne" />
 *       <attribute name="daylight" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_zeroOne" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_objects_tunnel", propOrder = {
    "validity",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadObjectsTunnel
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
     * Length of the tunnel (in s-direction)
     * 
     */
    @XmlAttribute(name = "length", required = true)
    protected double length;
    /**
     * Name of the tunnel. May be chosen freely.
     * 
     */
    @XmlAttribute(name = "name")
    protected String name;
    /**
     * Unique ID within database
     * 
     */
    @XmlAttribute(name = "id", required = true)
    protected String id;
    /**
     * Type of tunnel. For values see UML Model.
     * 
     */
    @XmlAttribute(name = "type", required = true)
    protected ETunnelType type;
    /**
     * Degree of artificial tunnel lighting. Depends on the application.
     * 
     */
    @XmlAttribute(name = "lighting")
    protected Double lighting;
    /**
     * Degree of daylight intruding the tunnel. Depends on the application.
     * 
     */
    @XmlAttribute(name = "daylight")
    protected Double daylight;

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
     * Length of the tunnel (in s-direction)
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
     * Name of the tunnel. May be chosen freely.
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
     * Type of tunnel. For values see UML Model.
     * 
     * @return
     *     possible object is
     *     {@link ETunnelType }
     *     
     */
    public ETunnelType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link ETunnelType }
     *     
     * @see #getType()
     */
    public void setType(ETunnelType value) {
        this.type = value;
    }

    /**
     * Degree of artificial tunnel lighting. Depends on the application.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLighting() {
        return lighting;
    }

    /**
     * Sets the value of the lighting property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getLighting()
     */
    public void setLighting(Double value) {
        this.lighting = value;
    }

    /**
     * Degree of daylight intruding the tunnel. Depends on the application.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getDaylight() {
        return daylight;
    }

    /**
     * Sets the value of the daylight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getDaylight()
     */
    public void setDaylight(Double value) {
        this.daylight = value;
    }

}
