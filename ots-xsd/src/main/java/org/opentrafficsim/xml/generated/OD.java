//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.22 at 08:30:33 PM CEST 
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="GLOBALTIME" type="{http://www.opentrafficsim.org/ots}GLOBALTIMETYPE" minOccurs="0"/&gt;
 *         &lt;element name="CATEGORY" type="{http://www.opentrafficsim.org/ots}CATEGORYTYPE" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="DEMAND" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="LEVEL" type="{http://www.opentrafficsim.org/ots}LEVELTIMETYPE" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="ORIGIN" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="DESTINATION" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="CATEGORY" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="INTERPOLATION" type="{http://www.opentrafficsim.org/ots}INTERPOLATIONTYPE" default="LINEAR" /&gt;
 *                 &lt;attribute name="FACTOR" type="{http://www.opentrafficsim.org/ots}POSITIVEFACTOR" default="1.0" /&gt;
 *                 &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}base"/&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="GLOBALINTERPOLATION" type="{http://www.opentrafficsim.org/ots}INTERPOLATIONTYPE" default="LINEAR" /&gt;
 *       &lt;attribute name="GLOBALFACTOR" type="{http://www.opentrafficsim.org/ots}POSITIVEFACTOR" default="1.0" /&gt;
 *       &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="generation" /&gt;
 *       &lt;attribute name="OPTIONS" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
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
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
public class OD
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "GLOBALTIME")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected GLOBALTIMETYPE globaltime;
    @XmlElement(name = "CATEGORY")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected List<CATEGORYTYPE> category;
    @XmlElement(name = "DEMAND")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected List<OD.DEMAND> demand;
    @XmlAttribute(name = "ID", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected String id;
    @XmlAttribute(name = "GLOBALINTERPOLATION")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected String globalinterpolation;
    @XmlAttribute(name = "GLOBALFACTOR")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected String globalfactor;
    @XmlAttribute(name = "RANDOMSTREAM")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected String randomstream;
    @XmlAttribute(name = "OPTIONS")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected String options;
    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    protected String base;

    /**
     * Gets the value of the globaltime property.
     * 
     * @return
     *     possible object is
     *     {@link GLOBALTIMETYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public GLOBALTIMETYPE getGLOBALTIME() {
        return globaltime;
    }

    /**
     * Sets the value of the globaltime property.
     * 
     * @param value
     *     allowed object is
     *     {@link GLOBALTIMETYPE }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setGLOBALTIME(GLOBALTIMETYPE value) {
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
     * {@link CATEGORYTYPE }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public List<CATEGORYTYPE> getCATEGORY() {
        if (category == null) {
            category = new ArrayList<CATEGORYTYPE>();
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public List<OD.DEMAND> getDEMAND() {
        if (demand == null) {
            demand = new ArrayList<OD.DEMAND>();
        }
        return this.demand;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public String getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setID(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the globalinterpolation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public String getGLOBALINTERPOLATION() {
        if (globalinterpolation == null) {
            return "LINEAR";
        } else {
            return globalinterpolation;
        }
    }

    /**
     * Sets the value of the globalinterpolation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setGLOBALINTERPOLATION(String value) {
        this.globalinterpolation = value;
    }

    /**
     * Gets the value of the globalfactor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public String getGLOBALFACTOR() {
        if (globalfactor == null) {
            return "1.0";
        } else {
            return globalfactor;
        }
    }

    /**
     * Sets the value of the globalfactor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setGLOBALFACTOR(String value) {
        this.globalfactor = value;
    }

    /**
     * Gets the value of the randomstream property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public String getRANDOMSTREAM() {
        if (randomstream == null) {
            return "generation";
        } else {
            return randomstream;
        }
    }

    /**
     * Sets the value of the randomstream property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setRANDOMSTREAM(String value) {
        this.randomstream = value;
    }

    /**
     * Gets the value of the options property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public String getOPTIONS() {
        return options;
    }

    /**
     * Sets the value of the options property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public void setOPTIONS(String value) {
        this.options = value;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
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
     *         &lt;element name="LEVEL" type="{http://www.opentrafficsim.org/ots}LEVELTIMETYPE" maxOccurs="unbounded" minOccurs="0"/&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="ORIGIN" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="DESTINATION" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="CATEGORY" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="INTERPOLATION" type="{http://www.opentrafficsim.org/ots}INTERPOLATIONTYPE" default="LINEAR" /&gt;
     *       &lt;attribute name="FACTOR" type="{http://www.opentrafficsim.org/ots}POSITIVEFACTOR" default="1.0" /&gt;
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
    public static class DEMAND
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "LEVEL")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        protected List<LEVELTIMETYPE> level;
        @XmlAttribute(name = "ORIGIN", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        protected String origin;
        @XmlAttribute(name = "DESTINATION", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        protected String destination;
        @XmlAttribute(name = "CATEGORY")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        protected String category;
        @XmlAttribute(name = "INTERPOLATION")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        protected String interpolation;
        @XmlAttribute(name = "FACTOR")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        protected String factor;
        @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
        @XmlSchemaType(name = "anyURI")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
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
         * {@link LEVELTIMETYPE }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        public List<LEVELTIMETYPE> getLEVEL() {
            if (level == null) {
                level = new ArrayList<LEVELTIMETYPE>();
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        public String getINTERPOLATION() {
            if (interpolation == null) {
                return "LINEAR";
            } else {
                return interpolation;
            }
        }

        /**
         * Sets the value of the interpolation property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        public String getFACTOR() {
            if (factor == null) {
                return "1.0";
            } else {
                return factor;
            }
        }

        /**
         * Sets the value of the factor property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T08:30:33+02:00", comments = "JAXB RI v2.3.0")
        public void setBase(String value) {
            this.base = value;
        }

    }

}
