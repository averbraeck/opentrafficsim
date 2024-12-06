
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
 * Contains information about all possible connections between roads meeting at a physical junction.
 * 
 * <p>Java class for t_junction complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_junction">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="connection" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_junction_connection" maxOccurs="unbounded"/>
 *         <element name="priority" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_junction_priority" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="controller" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_junction_controller" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="surface" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_junction_surface" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="type" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_junction_type" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_junction", propOrder = {
    "connection",
    "priority",
    "controller",
    "surface",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TJunction
    extends OpenDriveElement
{

    @XmlElement(required = true)
    protected List<TJunctionConnection> connection;
    protected List<TJunctionPriority> priority;
    protected List<TJunctionController> controller;
    protected TJunctionSurface surface;
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
     * Name of the junction. May be chosen freely.
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
     * Type of the junction; regular junctions are of type "default". The attribute is mandatory for virtual junctions
     * 
     */
    @XmlAttribute(name = "type")
    protected EJunctionType type;

    /**
     * Gets the value of the connection property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the connection property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getConnection().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TJunctionConnection }
     * </p>
     * 
     * 
     * @return
     *     The value of the connection property.
     */
    public List<TJunctionConnection> getConnection() {
        if (connection == null) {
            connection = new ArrayList<>();
        }
        return this.connection;
    }

    /**
     * Gets the value of the priority property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the priority property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getPriority().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TJunctionPriority }
     * </p>
     * 
     * 
     * @return
     *     The value of the priority property.
     */
    public List<TJunctionPriority> getPriority() {
        if (priority == null) {
            priority = new ArrayList<>();
        }
        return this.priority;
    }

    /**
     * Gets the value of the controller property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the controller property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getController().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TJunctionController }
     * </p>
     * 
     * 
     * @return
     *     The value of the controller property.
     */
    public List<TJunctionController> getController() {
        if (controller == null) {
            controller = new ArrayList<>();
        }
        return this.controller;
    }

    /**
     * Gets the value of the surface property.
     * 
     * @return
     *     possible object is
     *     {@link TJunctionSurface }
     *     
     */
    public TJunctionSurface getSurface() {
        return surface;
    }

    /**
     * Sets the value of the surface property.
     * 
     * @param value
     *     allowed object is
     *     {@link TJunctionSurface }
     *     
     */
    public void setSurface(TJunctionSurface value) {
        this.surface = value;
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
     * Name of the junction. May be chosen freely.
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
     * Type of the junction; regular junctions are of type "default". The attribute is mandatory for virtual junctions
     * 
     * @return
     *     possible object is
     *     {@link EJunctionType }
     *     
     */
    public EJunctionType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link EJunctionType }
     *     
     * @see #getType()
     */
    public void setType(EJunctionType value) {
        this.type = value;
    }

}
