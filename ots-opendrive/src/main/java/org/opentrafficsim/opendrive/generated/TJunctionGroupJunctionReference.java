
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * References to existing junction elements.
 * 
 * <p>Java-Klasse für t_junctionGroup_junctionReference complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
     * Legt den Wert der junction-Eigenschaft fest.
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
