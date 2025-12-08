
package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CarFollowingModelHeadwaySpeedType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
     * Gets the value of the desiredHeadwayModel property.
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
     * Sets the value of the desiredHeadwayModel property.
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
     * Gets the value of the desiredSpeedModel property.
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
     * Sets the value of the desiredSpeedModel property.
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
