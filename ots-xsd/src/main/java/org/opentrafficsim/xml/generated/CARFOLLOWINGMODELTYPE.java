//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.01.23 at 11:07:38 AM CET 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CARFOLLOWINGMODELTYPE complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CARFOLLOWINGMODELTYPE"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="IDM" type="{http://www.opentrafficsim.org/ots}CARFOLLOWINGMODELHEADWAYSPEEDTYPE"/&gt;
 *         &lt;element name="IDMPLUS" type="{http://www.opentrafficsim.org/ots}CARFOLLOWINGMODELHEADWAYSPEEDTYPE"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CARFOLLOWINGMODELTYPE", propOrder = {
    "idm",
    "idmplus"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T11:07:38+01:00", comments = "JAXB RI v2.3.0")
public class CARFOLLOWINGMODELTYPE
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T11:07:38+01:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "IDM")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T11:07:38+01:00", comments = "JAXB RI v2.3.0")
    protected CARFOLLOWINGMODELHEADWAYSPEEDTYPE idm;
    @XmlElement(name = "IDMPLUS")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T11:07:38+01:00", comments = "JAXB RI v2.3.0")
    protected CARFOLLOWINGMODELHEADWAYSPEEDTYPE idmplus;

    /**
     * Gets the value of the idm property.
     * 
     * @return
     *     possible object is
     *     {@link CARFOLLOWINGMODELHEADWAYSPEEDTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T11:07:38+01:00", comments = "JAXB RI v2.3.0")
    public CARFOLLOWINGMODELHEADWAYSPEEDTYPE getIDM() {
        return idm;
    }

    /**
     * Sets the value of the idm property.
     * 
     * @param value
     *     allowed object is
     *     {@link CARFOLLOWINGMODELHEADWAYSPEEDTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T11:07:38+01:00", comments = "JAXB RI v2.3.0")
    public void setIDM(CARFOLLOWINGMODELHEADWAYSPEEDTYPE value) {
        this.idm = value;
    }

    /**
     * Gets the value of the idmplus property.
     * 
     * @return
     *     possible object is
     *     {@link CARFOLLOWINGMODELHEADWAYSPEEDTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T11:07:38+01:00", comments = "JAXB RI v2.3.0")
    public CARFOLLOWINGMODELHEADWAYSPEEDTYPE getIDMPLUS() {
        return idmplus;
    }

    /**
     * Sets the value of the idmplus property.
     * 
     * @param value
     *     allowed object is
     *     {@link CARFOLLOWINGMODELHEADWAYSPEEDTYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2020-01-23T11:07:38+01:00", comments = "JAXB RI v2.3.0")
    public void setIDMPLUS(CARFOLLOWINGMODELHEADWAYSPEEDTYPE value) {
        this.idmplus = value;
    }

}
