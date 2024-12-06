
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Provides a means to link a signal to a series of other elements (for example, objects and signals).
 * 
 * <p>Java class for t_road_signals_signal_reference complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_signals_signal_reference">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="elementType" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_road_signals_signal_reference_elementType" />
 *       <attribute name="elementId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="type" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_signals_signal_reference")
@SuppressWarnings("all") public class TRoadSignalsSignalReference2
    extends OpenDriveElement
{

    /**
     * Type of the linked element, For values see UML Model
     * 
     */
    @XmlAttribute(name = "elementType", required = true)
    protected ERoadSignalsSignalReferenceElementType elementType;
    /**
     * Unique ID of the linked element
     * 
     */
    @XmlAttribute(name = "elementId", required = true)
    protected String elementId;
    /**
     * Type of the linkage 
     * Free text, depending on application
     * 
     */
    @XmlAttribute(name = "type")
    protected String type;

    /**
     * Type of the linked element, For values see UML Model
     * 
     * @return
     *     possible object is
     *     {@link ERoadSignalsSignalReferenceElementType }
     *     
     */
    public ERoadSignalsSignalReferenceElementType getElementType() {
        return elementType;
    }

    /**
     * Sets the value of the elementType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERoadSignalsSignalReferenceElementType }
     *     
     * @see #getElementType()
     */
    public void setElementType(ERoadSignalsSignalReferenceElementType value) {
        this.elementType = value;
    }

    /**
     * Unique ID of the linked element
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
     * Type of the linkage 
     * Free text, depending on application
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

}
