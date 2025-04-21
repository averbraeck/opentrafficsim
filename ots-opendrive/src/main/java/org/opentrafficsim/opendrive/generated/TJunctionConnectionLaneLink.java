
package org.opentrafficsim.opendrive.generated;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Provides information about the lanes that are linked between an incoming road and a connecting road. It is strongly recommended to provide this element. It is deprecated to omit the <laneLink> element.
 * 
 * <p>Java class for t_junction_connection_laneLink complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_junction_connection_laneLink">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="from" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       <attribute name="to" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_junction_connection_laneLink")
@SuppressWarnings("all") public class TJunctionConnectionLaneLink
    extends OpenDriveElement
{

    /**
     * ID of the incoming lane
     * 
     */
    @XmlAttribute(name = "from", required = true)
    protected BigInteger from;
    /**
     * ID of the connection lane
     * 
     */
    @XmlAttribute(name = "to", required = true)
    protected BigInteger to;

    /**
     * ID of the incoming lane
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     * @see #getFrom()
     */
    public void setFrom(BigInteger value) {
        this.from = value;
    }

    /**
     * ID of the connection lane
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     * @see #getTo()
     */
    public void setTo(BigInteger value) {
        this.to = value;
    }

}
