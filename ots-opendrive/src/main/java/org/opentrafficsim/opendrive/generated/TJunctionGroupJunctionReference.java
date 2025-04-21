
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * References to existing junction elements.
 * 
 * <p>Java class for t_junctionGroup_junctionReference complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_junctionGroup_junctionReference">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="junction" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_junctionGroup_junctionReference")
@SuppressWarnings("all") public class TJunctionGroupJunctionReference
    extends OpenDriveElement
{

    /**
     * ID of the junction
     * 
     */
    @XmlAttribute(name = "junction", required = true)
    protected String junction;

    /**
     * ID of the junction
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJunction() {
        return junction;
    }

    /**
     * Sets the value of the junction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getJunction()
     */
    public void setJunction(String value) {
        this.junction = value;
    }

}
