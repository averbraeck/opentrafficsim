//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.03.01 at 04:16:12 PM CET 
//


package org.opentrafficsim.xml.generated;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="GLOBALTIME" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *         &lt;element name="CATEGORY" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="DEMAND" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="LEVEL" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="ORIGIN" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="DESTINATION" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="CATEGORY" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="INTERPOLATION" type="{http://www.opentrafficsim.org/ots}INTERPOLATIONTYPE" /&gt;
 *                 &lt;attribute name="FACTOR" type="{http://www.opentrafficsim.org/ots}POSITIVEFACTOR" /&gt;
 *                 &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}base"/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="NAME" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="GLOBALINTERPOLATION" type="{http://www.opentrafficsim.org/ots}INTERPOLATIONTYPE" /&gt;
 *       &lt;attribute name="START" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="GLOBALFACTOR" type="{http://www.opentrafficsim.org/ots}POSITIVEFACTOR" /&gt;
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}base"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "globaltime",
    "category",
    "demand"
})
@XmlRootElement(name = "OD")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
public class OD {

    @XmlElement(name = "GLOBALTIME")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected Object globaltime;
    @XmlElement(name = "CATEGORY")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected List<Object> category;
    @XmlElement(name = "DEMAND")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected List<OD.DEMAND> demand;
    @XmlAttribute(name = "NAME")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected String name;
    @XmlAttribute(name = "GLOBALINTERPOLATION")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected String globalinterpolation;
    @XmlAttribute(name = "START")
    @XmlSchemaType(name = "dateTime")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected XMLGregorianCalendar start;
    @XmlAttribute(name = "GLOBALFACTOR")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected String globalfactor;
    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    protected String base;

    /**
     * Gets the value of the globaltime property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public Object getGLOBALTIME() {
        return globaltime;
    }

    /**
     * Sets the value of the globaltime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public void setGLOBALTIME(Object value) {
        this.globaltime = value;
    }

    /**
     * Gets the value of the category property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the category property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCATEGORY().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public List<Object> getCATEGORY() {
        if (category == null) {
            category = new ArrayList<Object>();
        }
        return this.category;
    }

    /**
     * Gets the value of the demand property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the demand property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDEMAND().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OD.DEMAND }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public List<OD.DEMAND> getDEMAND() {
        if (demand == null) {
            demand = new ArrayList<OD.DEMAND>();
        }
        return this.demand;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public String getNAME() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public void setNAME(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the globalinterpolation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public String getGLOBALINTERPOLATION() {
        return globalinterpolation;
    }

    /**
     * Sets the value of the globalinterpolation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public void setGLOBALINTERPOLATION(String value) {
        this.globalinterpolation = value;
    }

    /**
     * Gets the value of the start property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public XMLGregorianCalendar getSTART() {
        return start;
    }

    /**
     * Sets the value of the start property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public void setSTART(XMLGregorianCalendar value) {
        this.start = value;
    }

    /**
     * Gets the value of the globalfactor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public String getGLOBALFACTOR() {
        return globalfactor;
    }

    /**
     * Sets the value of the globalfactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public void setGLOBALFACTOR(String value) {
        this.globalfactor = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public String getBase() {
        return base;
    }

    /**
     * Sets the value of the base property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public void setBase(String value) {
        this.base = value;
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
     *       &lt;sequence&gt;
     *         &lt;element name="LEVEL" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="ORIGIN" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="DESTINATION" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="CATEGORY" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="INTERPOLATION" type="{http://www.opentrafficsim.org/ots}INTERPOLATIONTYPE" /&gt;
     *       &lt;attribute name="FACTOR" type="{http://www.opentrafficsim.org/ots}POSITIVEFACTOR" /&gt;
     *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}base"/&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "level"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
    public static class DEMAND {

        @XmlElement(name = "LEVEL")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        protected List<Object> level;
        @XmlAttribute(name = "ORIGIN", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        protected String origin;
        @XmlAttribute(name = "DESTINATION", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        protected String destination;
        @XmlAttribute(name = "CATEGORY")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        protected String category;
        @XmlAttribute(name = "INTERPOLATION")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        protected String interpolation;
        @XmlAttribute(name = "FACTOR")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        protected String factor;
        @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
        @XmlSchemaType(name = "anyURI")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        protected String base;

        /**
         * Gets the value of the level property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the level property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getLEVEL().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Object }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        public List<Object> getLEVEL() {
            if (level == null) {
                level = new ArrayList<Object>();
            }
            return this.level;
        }

        /**
         * Gets the value of the origin property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        public String getORIGIN() {
            return origin;
        }

        /**
         * Sets the value of the origin property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        public void setORIGIN(String value) {
            this.origin = value;
        }

        /**
         * Gets the value of the destination property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        public String getDESTINATION() {
            return destination;
        }

        /**
         * Sets the value of the destination property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        public void setDESTINATION(String value) {
            this.destination = value;
        }

        /**
         * Gets the value of the category property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        public String getCATEGORY() {
            return category;
        }

        /**
         * Sets the value of the category property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        public void setCATEGORY(String value) {
            this.category = value;
        }

        /**
         * Gets the value of the interpolation property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        public String getINTERPOLATION() {
            return interpolation;
        }

        /**
         * Sets the value of the interpolation property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        public void setINTERPOLATION(String value) {
            this.interpolation = value;
        }

        /**
         * Gets the value of the factor property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        public String getFACTOR() {
            return factor;
        }

        /**
         * Sets the value of the factor property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        public void setFACTOR(String value) {
            this.factor = value;
        }

        /**
         * Gets the value of the base property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        public String getBase() {
            return base;
        }

        /**
         * Sets the value of the base property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-01T04:16:12+01:00", comments = "JAXB RI v2.3.0")
        public void setBase(String value) {
            this.base = value;
        }

    }

}
