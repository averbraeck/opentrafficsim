//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.22 at 03:02:02 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.xml.bindings.TimeAdapter;


/**
 * <p>Java class for GLOBALTIMETYPE complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GLOBALTIMETYPE"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="TIME" maxOccurs="unbounded" minOccurs="2"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="VALUE" use="required" type="{http://www.opentrafficsim.org/ots}TIMETYPE" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GLOBALTIMETYPE", propOrder = {
    "time"
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
public class GLOBALTIMETYPE
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "TIME", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
    protected List<GLOBALTIMETYPE.TIME> time;

    /**
     * Gets the value of the time property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the time property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTIME().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GLOBALTIMETYPE.TIME }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
    public List<GLOBALTIMETYPE.TIME> getTIME() {
        if (time == null) {
            time = new ArrayList<GLOBALTIMETYPE.TIME>();
        }
        return this.time;
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
     *       &lt;attribute name="VALUE" use="required" type="{http://www.opentrafficsim.org/ots}TIMETYPE" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
    public static class TIME
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "VALUE", required = true)
        @XmlJavaTypeAdapter(TimeAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
        protected Time value;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
        public Time getVALUE() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T03:02:02+02:00", comments = "JAXB RI v2.3.0")
        public void setVALUE(Time value) {
            this.value = value;
        }

    }

}
