
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für CarFollowingModelType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
     * Ruft den Wert der idm-Eigenschaft ab.
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
     * Legt den Wert der idm-Eigenschaft fest.
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
     * Ruft den Wert der idmPlus-Eigenschaft ab.
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
     * Legt den Wert der idmPlus-Eigenschaft fest.
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
