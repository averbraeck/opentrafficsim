
package org.opentrafficsim.opendrive.generated;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.opendrive.bindings.ContactPointAdapter;


/**
 * Provides information about a single connection within a junction.
 * 
 * <p>Java class for t_junction_connection complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_junction_connection">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="predecessor" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_junction_predecessorSuccessor" minOccurs="0"/>
 *         <element name="successor" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_junction_predecessorSuccessor" minOccurs="0"/>
 *         <element name="laneLink" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_junction_connection_laneLink" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="type" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_junction_type" />
 *       <attribute name="incomingRoad" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="connectingRoad" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="contactPoint" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_junction_connection", propOrder = {
    "predecessor",
    "successor",
    "laneLink"
})
@SuppressWarnings("all") public class TJunctionConnection
    extends OpenDriveElement
{

    protected TJunctionPredecessorSuccessor predecessor;
    protected TJunctionPredecessorSuccessor successor;
    protected List<TJunctionConnectionLaneLink> laneLink;
    /**
     * Unique ID within the junction
     * 
     */
    @XmlAttribute(name = "id", required = true)
    protected String id;
    /**
     * Type of the connection, regular connections are type “default” 
     * mandatory attribute for virtual connections
     * 
     */
    @XmlAttribute(name = "type")
    protected EJunctionType type;
    /**
     * ID of the incoming road
     * 
     */
    @XmlAttribute(name = "incomingRoad")
    protected String incomingRoad;
    /**
     * ID of the connecting road
     * 
     */
    @XmlAttribute(name = "connectingRoad")
    protected String connectingRoad;
    /**
     * Contact point on the connecting road. For values, see UML Model
     * 
     */
    @XmlAttribute(name = "contactPoint")
    @XmlJavaTypeAdapter(ContactPointAdapter.class)
    protected EContactPoint contactPoint;

    /**
     * Gets the value of the predecessor property.
     * 
     * @return
     *     possible object is
     *     {@link TJunctionPredecessorSuccessor }
     *     
     */
    public TJunctionPredecessorSuccessor getPredecessor() {
        return predecessor;
    }

    /**
     * Sets the value of the predecessor property.
     * 
     * @param value
     *     allowed object is
     *     {@link TJunctionPredecessorSuccessor }
     *     
     */
    public void setPredecessor(TJunctionPredecessorSuccessor value) {
        this.predecessor = value;
    }

    /**
     * Gets the value of the successor property.
     * 
     * @return
     *     possible object is
     *     {@link TJunctionPredecessorSuccessor }
     *     
     */
    public TJunctionPredecessorSuccessor getSuccessor() {
        return successor;
    }

    /**
     * Sets the value of the successor property.
     * 
     * @param value
     *     allowed object is
     *     {@link TJunctionPredecessorSuccessor }
     *     
     */
    public void setSuccessor(TJunctionPredecessorSuccessor value) {
        this.successor = value;
    }

    /**
     * Gets the value of the laneLink property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the laneLink property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLaneLink().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TJunctionConnectionLaneLink }
     * </p>
     * 
     * 
     * @return
     *     The value of the laneLink property.
     */
    public List<TJunctionConnectionLaneLink> getLaneLink() {
        if (laneLink == null) {
            laneLink = new ArrayList<>();
        }
        return this.laneLink;
    }

    /**
     * Unique ID within the junction
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
     * Type of the connection, regular connections are type “default” 
     * mandatory attribute for virtual connections
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

    /**
     * ID of the incoming road
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIncomingRoad() {
        return incomingRoad;
    }

    /**
     * Sets the value of the incomingRoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getIncomingRoad()
     */
    public void setIncomingRoad(String value) {
        this.incomingRoad = value;
    }

    /**
     * ID of the connecting road
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConnectingRoad() {
        return connectingRoad;
    }

    /**
     * Sets the value of the connectingRoad property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getConnectingRoad()
     */
    public void setConnectingRoad(String value) {
        this.connectingRoad = value;
    }

    /**
     * Contact point on the connecting road. For values, see UML Model
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public EContactPoint getContactPoint() {
        return contactPoint;
    }

    /**
     * Sets the value of the contactPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getContactPoint()
     */
    public void setContactPoint(EContactPoint value) {
        this.contactPoint = value;
    }

}
