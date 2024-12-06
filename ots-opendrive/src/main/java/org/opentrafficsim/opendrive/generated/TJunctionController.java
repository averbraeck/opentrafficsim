
package org.opentrafficsim.opendrive.generated;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Lists the controllers that are used for the management of a junction.
 * 
 * <p>Java class for t_junction_controller complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_junction_controller">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="sequence" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_junction_controller")
@SuppressWarnings("all") public class TJunctionController
    extends OpenDriveElement
{

    /**
     * ID of the controller
     * 
     */
    @XmlAttribute(name = "id", required = true)
    protected String id;
    /**
     * Type of control for this junction. Free text, depending on the application.
     * 
     */
    @XmlAttribute(name = "type")
    protected String type;
    /**
     * Sequence number (priority) of this controller with respect to other controllers in the same junction
     * 
     */
    @XmlAttribute(name = "sequence")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger sequence;

    /**
     * ID of the controller
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
     * Type of control for this junction. Free text, depending on the application.
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
     * Sequence number (priority) of this controller with respect to other controllers in the same junction
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSequence() {
        return sequence;
    }

    /**
     * Sets the value of the sequence property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     * @see #getSequence()
     */
    public void setSequence(BigInteger value) {
        this.sequence = value;
    }

}
