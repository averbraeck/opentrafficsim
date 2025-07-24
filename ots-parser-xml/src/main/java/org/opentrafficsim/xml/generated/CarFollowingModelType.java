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
 * <p>Java-Klasse für CarFollowingModelType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="CarFollowingModelType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="Idm" type="{http://www.opentrafficsim.org/ots}CarFollowingModelHeadwaySpeedType"/&gt;
 *         &lt;element name="IdmPlus" type="{http://www.opentrafficsim.org/ots}CarFollowingModelHeadwaySpeedType"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CarFollowingModelType", propOrder = {
    "idm",
    "idmPlus"
})
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class CarFollowingModelType
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "Idm")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected CarFollowingModelHeadwaySpeedType idm;
    @XmlElement(name = "IdmPlus")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected CarFollowingModelHeadwaySpeedType idmPlus;

    /**
     * Ruft den Wert der idm-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link CarFollowingModelHeadwaySpeedType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setIdmPlus(CarFollowingModelHeadwaySpeedType value) {
        this.idmPlus = value;
    }

}
