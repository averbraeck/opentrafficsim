//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v2.3.7 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2025.07.24 um 01:18:43 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für CarFollowingModelHeadwaySpeedType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="CarFollowingModelHeadwaySpeedType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="DesiredHeadwayModel" type="{http://www.opentrafficsim.org/ots}DesiredHeadwayModelType" minOccurs="0"/&gt;
 *         &lt;element name="DesiredSpeedModel" type="{http://www.opentrafficsim.org/ots}DesiredSpeedModelType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CarFollowingModelHeadwaySpeedType", propOrder = {
    "desiredHeadwayModel",
    "desiredSpeedModel"
})
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class CarFollowingModelHeadwaySpeedType
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "DesiredHeadwayModel")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected DesiredHeadwayModelType desiredHeadwayModel;
    @XmlElement(name = "DesiredSpeedModel")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected DesiredSpeedModelType desiredSpeedModel;

    /**
     * Ruft den Wert der desiredHeadwayModel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DesiredHeadwayModelType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setDesiredSpeedModel(DesiredSpeedModelType value) {
        this.desiredSpeedModel = value;
    }

}
