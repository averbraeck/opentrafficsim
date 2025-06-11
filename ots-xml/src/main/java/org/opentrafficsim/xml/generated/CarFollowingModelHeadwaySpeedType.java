
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für CarFollowingModelHeadwaySpeedType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="CarFollowingModelHeadwaySpeedType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="DesiredHeadwayModel" type="{http://www.opentrafficsim.org/ots}DesiredHeadwayModelType" minOccurs="0"/>
 *         <element name="DesiredSpeedModel" type="{http://www.opentrafficsim.org/ots}DesiredSpeedModelType" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CarFollowingModelHeadwaySpeedType", propOrder = {
    "desiredHeadwayModel",
    "desiredSpeedModel"
})
@SuppressWarnings("all") public class CarFollowingModelHeadwaySpeedType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "DesiredHeadwayModel")
    protected DesiredHeadwayModelType desiredHeadwayModel;
    @XmlElement(name = "DesiredSpeedModel")
    protected DesiredSpeedModelType desiredSpeedModel;

    /**
     * Ruft den Wert der desiredHeadwayModel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DesiredHeadwayModelType }
     *     
     */
    public DesiredHeadwayModelType getDesiredHeadwayModel() {
        return desiredHeadwayModel;
    }

    /**
     * Legt den Wert der desiredHeadwayModel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DesiredHeadwayModelType }
     *     
     */
    public void setDesiredHeadwayModel(DesiredHeadwayModelType value) {
        this.desiredHeadwayModel = value;
    }

    /**
     * Ruft den Wert der desiredSpeedModel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DesiredSpeedModelType }
     *     
     */
    public DesiredSpeedModelType getDesiredSpeedModel() {
        return desiredSpeedModel;
    }

    /**
     * Legt den Wert der desiredSpeedModel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DesiredSpeedModelType }
     *     
     */
    public void setDesiredSpeedModel(DesiredSpeedModelType value) {
        this.desiredSpeedModel = value;
    }

}
