
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * If an incoming road is linked to an outgoing road with multiple connection roads to represent several possible lane connections, then one of these connections may be prioritized. Assigning a priority is only required if the application is unable to derive priorities from signals before or inside a junction or from the lanes leading to a junction. At least one attribute must be given.
 * 
 * <p>Java class for t_junction_priority complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_junction_priority">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="high" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="low" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_junction_priority")
@SuppressWarnings("all") public class TJunctionPriority
    extends OpenDriveElement
{

    /**
     * ID of the prioritized connecting road
     * 
     */
    @XmlAttribute(name = "high")
    protected String high;
    /**
     * ID of the connecting road with lower priority
     * 
     */
    @XmlAttribute(name = "low")
    protected String low;

    /**
     * ID of the prioritized connecting road
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHigh() {
        return high;
    }

    /**
     * Sets the value of the high property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getHigh()
     */
    public void setHigh(String value) {
        this.high = value;
    }

    /**
     * ID of the connecting road with lower priority
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLow() {
        return low;
    }

    /**
     * Sets the value of the low property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getLow()
     */
    public void setLow(String value) {
        this.low = value;
    }

}
