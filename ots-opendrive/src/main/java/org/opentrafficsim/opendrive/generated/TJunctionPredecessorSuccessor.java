
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Provides detailed information about the predecessor / successor road of a virtual connection. Currently, only the @elementType “road” is allowed.
 * 
 * <p>Java-Klasse für t_junction_predecessorSuccessor complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="t_junction_predecessorSuccessor">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="elementType" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="road" />
 *       <attribute name="elementId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="elementS" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grZero" />
 *       <attribute name="elementDir" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_elementDir" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_junction_predecessorSuccessor")
@SuppressWarnings("all") public class TJunctionPredecessorSuccessor
    extends OpenDriveElement
{

    /**
     * Type of the linked element Currently only "road" is allowed.
     * 
     */
    @XmlAttribute(name = "elementType", required = true)
    protected String elementType;
    /**
     * ID of the linked element
     * 
     */
    @XmlAttribute(name = "elementId", required = true)
    protected String elementId;
    /**
     * s-coordinate where the connection meets the preceding / succeding road.
     * 
     */
    @XmlAttribute(name = "elementS", required = true)
    protected double elementS;
    /**
     * Direction, relative to the s-direction, of the connection on the preceding / succeding road
     * 
     */
    @XmlAttribute(name = "elementDir", required = true)
    protected String elementDir;

    /**
     * Type of the linked element Currently only "road" is allowed.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getElementType() {
        if (elementType == null) {
            return "road";
        } else {
            return elementType;
        }
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
    public void setElementType(String value) {
        this.elementType = value;
    }

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
     * s-coordinate where the connection meets the preceding / succeding road.
     * 
     */
    public double getElementS() {
        return elementS;
    }

    /**
     * Legt den Wert der elementS-Eigenschaft fest.
     * 
     */
    public void setElementS(double value) {
        this.elementS = value;
    }

    /**
     * Direction, relative to the s-direction, of the connection on the preceding / succeding road
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
