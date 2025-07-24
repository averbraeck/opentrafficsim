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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für LaneLinkType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="LaneLinkType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="Lane" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *       &lt;attribute name="Link" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LaneLinkType")
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class LaneLinkType
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlAttribute(name = "Lane")
    @XmlJavaTypeAdapter(StringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected StringType lane;
    @XmlAttribute(name = "Link")
    @XmlJavaTypeAdapter(StringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected StringType link;

    /**
     * Ruft den Wert der lane-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public StringType getLane() {
        return lane;
    }

    /**
     * Legt den Wert der lane-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setLane(StringType value) {
        this.lane = value;
    }

    /**
     * Ruft den Wert der link-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public StringType getLink() {
        return link;
    }

    /**
     * Legt den Wert der link-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setLink(StringType value) {
        this.link = value;
    }

}
