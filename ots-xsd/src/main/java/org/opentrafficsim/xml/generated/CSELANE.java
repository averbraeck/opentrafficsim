//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.03.10 at 04:40:02 AM CET 
//


package org.opentrafficsim.xml.generated;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.xml.bindings.DrivingDirectionAdapter;
import org.opentrafficsim.xml.bindings.SpeedAdapter;
import org.opentrafficsim.xml.bindings.types.DrivingDirectionType;


/**
 * <p>Java class for CSELANE complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CSELANE"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opentrafficsim.org/ots}CROSSSECTIONELEMENT"&gt;
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="SPEEDLIMIT" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="GTUTYPE" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="LEGALSPEEDLIMIT" type="{http://www.opentrafficsim.org/ots}SPEEDTYPE" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="LANETYPE" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="DIRECTION" use="required" type="{http://www.opentrafficsim.org/ots}DRIVINGDIRECTIONTYPE" /&gt;
 *       &lt;attribute name="OVERTAKING" type="{http://www.opentrafficsim.org/ots}OVERTAKINGTYPE" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CSELANE", propOrder = {
    "speedlimit"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
public class CSELANE
    extends CROSSSECTIONELEMENT
{

    @XmlElement(name = "SPEEDLIMIT")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
    protected List<CSELANE.SPEEDLIMIT> speedlimit;
    @XmlAttribute(name = "LANETYPE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
    protected String lanetype;
    @XmlAttribute(name = "DIRECTION", required = true)
    @XmlJavaTypeAdapter(DrivingDirectionAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
    protected DrivingDirectionType direction;
    @XmlAttribute(name = "OVERTAKING")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
    protected String overtaking;

    /**
     * Gets the value of the speedlimit property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the speedlimit property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSPEEDLIMIT().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CSELANE.SPEEDLIMIT }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
    public List<CSELANE.SPEEDLIMIT> getSPEEDLIMIT() {
        if (speedlimit == null) {
            speedlimit = new ArrayList<CSELANE.SPEEDLIMIT>();
        }
        return this.speedlimit;
    }

    /**
     * Gets the value of the lanetype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
    public String getLANETYPE() {
        return lanetype;
    }

    /**
     * Sets the value of the lanetype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
    public void setLANETYPE(String value) {
        this.lanetype = value;
    }

    /**
     * Gets the value of the direction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
    public DrivingDirectionType getDIRECTION() {
        return direction;
    }

    /**
     * Sets the value of the direction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
    public void setDIRECTION(DrivingDirectionType value) {
        this.direction = value;
    }

    /**
     * Gets the value of the overtaking property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
    public String getOVERTAKING() {
        return overtaking;
    }

    /**
     * Sets the value of the overtaking property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
    public void setOVERTAKING(String value) {
        this.overtaking = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="GTUTYPE" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="LEGALSPEEDLIMIT" type="{http://www.opentrafficsim.org/ots}SPEEDTYPE" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
    public static class SPEEDLIMIT {

        @XmlAttribute(name = "GTUTYPE", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
        protected String gtutype;
        @XmlAttribute(name = "LEGALSPEEDLIMIT")
        @XmlJavaTypeAdapter(SpeedAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
        protected Speed legalspeedlimit;

        /**
         * Gets the value of the gtutype property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
        public String getGTUTYPE() {
            return gtutype;
        }

        /**
         * Sets the value of the gtutype property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
        public void setGTUTYPE(String value) {
            this.gtutype = value;
        }

        /**
         * Gets the value of the legalspeedlimit property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
        public Speed getLEGALSPEEDLIMIT() {
            return legalspeedlimit;
        }

        /**
         * Sets the value of the legalspeedlimit property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-10T04:40:02+01:00", comments = "JAXB RI v2.3.0")
        public void setLEGALSPEEDLIMIT(Speed value) {
            this.legalspeedlimit = value;
        }

    }

}
