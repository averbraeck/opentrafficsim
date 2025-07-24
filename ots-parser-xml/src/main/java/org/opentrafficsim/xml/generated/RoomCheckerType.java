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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DurationAdapter;
import org.opentrafficsim.xml.bindings.types.DurationType;


/**
 * 
 *         Cf uses equilibrium car-following, CfBa uses non-equilibrium car-following to
 *         increase flow based on
 *         bounded-acceleration, Ttc uses time-to-collision.
 *       
 * 
 * <p>Java-Klasse für RoomCheckerType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="RoomCheckerType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="Cf" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
 *         &lt;element name="CfBa" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
 *         &lt;element name="Ttc" type="{http://www.opentrafficsim.org/ots}DurationType"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RoomCheckerType", propOrder = {
    "cf",
    "cfBa",
    "ttc"
})
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class RoomCheckerType
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "Cf")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected EmptyType cf;
    @XmlElement(name = "CfBa")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected EmptyType cfBa;
    @XmlElement(name = "Ttc", type = String.class)
    @XmlJavaTypeAdapter(DurationAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected DurationType ttc;

    /**
     * Ruft den Wert der cf-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public EmptyType getCf() {
        return cf;
    }

    /**
     * Legt den Wert der cf-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setCf(EmptyType value) {
        this.cf = value;
    }

    /**
     * Ruft den Wert der cfBa-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EmptyType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public EmptyType getCfBa() {
        return cfBa;
    }

    /**
     * Legt den Wert der cfBa-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EmptyType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setCfBa(EmptyType value) {
        this.cfBa = value;
    }

    /**
     * Ruft den Wert der ttc-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public DurationType getTtc() {
        return ttc;
    }

    /**
     * Legt den Wert der ttc-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setTtc(DurationType value) {
        this.ttc = value;
    }

}
