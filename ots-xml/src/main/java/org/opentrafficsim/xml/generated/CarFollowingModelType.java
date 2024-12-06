
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CarFollowingModelType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="CarFollowingModelType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice>
 *         <element name="Idm" type="{http://www.opentrafficsim.org/ots}CarFollowingModelHeadwaySpeedType"/>
 *         <element name="IdmPlus" type="{http://www.opentrafficsim.org/ots}CarFollowingModelHeadwaySpeedType"/>
 *       </choice>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CarFollowingModelType", propOrder = {
    "idm",
    "idmPlus"
})
@SuppressWarnings("all") public class CarFollowingModelType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Idm")
    protected CarFollowingModelHeadwaySpeedType idm;
    @XmlElement(name = "IdmPlus")
    protected CarFollowingModelHeadwaySpeedType idmPlus;

    /**
     * Gets the value of the idm property.
     * 
     * @return
     *     possible object is
     *     {@link CarFollowingModelHeadwaySpeedType }
     *     
     */
    public CarFollowingModelHeadwaySpeedType getIdm() {
        return idm;
    }

    /**
     * Sets the value of the idm property.
     * 
     * @param value
     *     allowed object is
     *     {@link CarFollowingModelHeadwaySpeedType }
     *     
     */
    public void setIdm(CarFollowingModelHeadwaySpeedType value) {
        this.idm = value;
    }

    /**
     * Gets the value of the idmPlus property.
     * 
     * @return
     *     possible object is
     *     {@link CarFollowingModelHeadwaySpeedType }
     *     
     */
    public CarFollowingModelHeadwaySpeedType getIdmPlus() {
        return idmPlus;
    }

    /**
     * Sets the value of the idmPlus property.
     * 
     * @param value
     *     allowed object is
     *     {@link CarFollowingModelHeadwaySpeedType }
     *     
     */
    public void setIdmPlus(CarFollowingModelHeadwaySpeedType value) {
        this.idmPlus = value;
    }

}
