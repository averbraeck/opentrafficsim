
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.opendrive.bindings.ContactPointAdapter;
import org.opentrafficsim.opendrive.bindings.RoadLinkTypeAdapter;


/**
 * For virtual and regular junctions, different attribute sets shall be used. @contactPoint shall be used for regular junctions; @elementS and @elementDir shall be used for virtual junctions.
 * 
 * <p>Java-Klasse für t_road_link_predecessorSuccessor complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_link_predecessorSuccessor">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="elementId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="elementType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="contactPoint" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="elementS" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="elementDir" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_link_predecessorSuccessor")
@SuppressWarnings("all") public class TRoadLinkPredecessorSuccessor
    extends OpenDriveElement
{

    /**
     * ID of the linked element
     * 
     */
    @XmlAttribute(name = "elementId", required = true)
    protected String elementId;
    /**
     * Type of the linked element
     * 
     */
    @XmlAttribute(name = "elementType")
    @XmlJavaTypeAdapter(RoadLinkTypeAdapter.class)
    protected ERoadLinkElementType elementType;
    /**
     * Contact point of link on the linked element
     * 
     */
    @XmlAttribute(name = "contactPoint")
    @XmlJavaTypeAdapter(ContactPointAdapter.class)
    protected EContactPoint contactPoint;
    /**
     * Alternative to contactPoint for virtual junctions. Indicates a connection within the predecessor, meaning not at the start or end of the predecessor. Shall only be used for elementType "road"
     * 
     */
    @XmlAttribute(name = "elementS")
    protected Double elementS;
    /**
     * To be provided when elementS is used for the connection definition. Indicates the direction on the predecessor from which the road is entered.
     * 
     */
    @XmlAttribute(name = "elementDir")
    protected String elementDir;

    /**
     * ID of the linked element
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElementId() {
        return elementId;
    }

    /**
     * Legt den Wert der elementId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getElementId()
     */
    public void setElementId(String value) {
        this.elementId = value;
    }

    /**
     * Type of the linked element
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public ERoadLinkElementType getElementType() {
        return elementType;
    }

    /**
     * Legt den Wert der elementType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getElementType()
     */
    public void setElementType(ERoadLinkElementType value) {
        this.elementType = value;
    }

    /**
     * Contact point of link on the linked element
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
     * Legt den Wert der contactPoint-Eigenschaft fest.
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

    /**
     * Alternative to contactPoint for virtual junctions. Indicates a connection within the predecessor, meaning not at the start or end of the predecessor. Shall only be used for elementType "road"
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getElementS() {
        return elementS;
    }

    /**
     * Legt den Wert der elementS-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     * @see #getElementS()
     */
    public void setElementS(Double value) {
        this.elementS = value;
    }

    /**
     * To be provided when elementS is used for the connection definition. Indicates the direction on the predecessor from which the road is entered.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElementDir() {
        return elementDir;
    }

    /**
     * Legt den Wert der elementDir-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getElementDir()
     */
    public void setElementDir(String value) {
        this.elementDir = value;
    }

}
