
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Provides detailed information about the predecessor / successor road of a virtual connection. Currently, only the @elementType “road” is allowed.
 * 
 * <p>Java class for t_junction_predecessorSuccessor complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
     * Sets the value of the elementType property.
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
     * Sets the value of the elementId property.
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
     * Sets the value of the elementS property.
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
     * Sets the value of the elementDir property.
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
