
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Provides information about a single signal controlled by the corresponding controller.
 * 
 * <p>Java-Klasse für t_controller_control complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="t_controller_control">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="signalId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_controller_control")
@SuppressWarnings("all") public class TControllerControl
    extends OpenDriveElement
{

    /**
     * ID of the controlled signal
     * 
     */
    @XmlAttribute(name = "signalId", required = true)
    protected String signalId;
    /**
     * Type of control. 
     * Free Text, depends on the application.
     * 
     */
    @XmlAttribute(name = "type")
    protected String type;

    /**
     * ID of the controlled signal
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSignalId() {
        return signalId;
    }

    /**
     * Legt den Wert der signalId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getSignalId()
     */
    public void setSignalId(String value) {
        this.signalId = value;
    }

    /**
     * Type of control. 
     * Free Text, depends on the application.
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
     * Legt den Wert der type-Eigenschaft fest.
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

}
