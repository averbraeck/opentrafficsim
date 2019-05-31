//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.22 at 09:43:59 PM CEST 
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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.xml.bindings.DurationAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="FIXEDTIME" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;extension base="{http://www.opentrafficsim.org/ots}CONTROLTYPE"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="CYCLE" maxOccurs="unbounded"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;attribute name="SIGNALGROUPID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="OFFSET" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
 *                           &lt;attribute name="PREGREEN" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
 *                           &lt;attribute name="GREEN" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
 *                           &lt;attribute name="YELLOW" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="CYCLETIME" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
 *                 &lt;attribute name="OFFSET" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" default="0.0 s" /&gt;
 *               &lt;/extension&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="TRAFCOD" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;extension base="{http://www.opentrafficsim.org/ots}RESPONSIVECONTROLTYPE"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="PROGRAM" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="PROGRAMFILE" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
 *                 &lt;/choice&gt;
 *               &lt;/extension&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "fixedtime",
    "trafcod"
})
@XmlRootElement(name = "CONTROL")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
public class CONTROL
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "FIXEDTIME")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    protected List<CONTROL.FIXEDTIME> fixedtime;
    @XmlElement(name = "TRAFCOD")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    protected List<CONTROL.TRAFCOD> trafcod;

    /**
     * Gets the value of the fixedtime property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixedtime property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFIXEDTIME().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CONTROL.FIXEDTIME }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public List<CONTROL.FIXEDTIME> getFIXEDTIME() {
        if (fixedtime == null) {
            fixedtime = new ArrayList<CONTROL.FIXEDTIME>();
        }
        return this.fixedtime;
    }

    /**
     * Gets the value of the trafcod property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trafcod property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTRAFCOD().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CONTROL.TRAFCOD }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public List<CONTROL.TRAFCOD> getTRAFCOD() {
        if (trafcod == null) {
            trafcod = new ArrayList<CONTROL.TRAFCOD>();
        }
        return this.trafcod;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;extension base="{http://www.opentrafficsim.org/ots}CONTROLTYPE"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="CYCLE" maxOccurs="unbounded"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;attribute name="SIGNALGROUPID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="OFFSET" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
     *                 &lt;attribute name="PREGREEN" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
     *                 &lt;attribute name="GREEN" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
     *                 &lt;attribute name="YELLOW" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="CYCLETIME" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
     *       &lt;attribute name="OFFSET" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" default="0.0 s" /&gt;
     *     &lt;/extension&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "cycle"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public static class FIXEDTIME
        extends CONTROLTYPE
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "CYCLE", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        protected List<CONTROL.FIXEDTIME.CYCLE> cycle;
        @XmlAttribute(name = "CYCLETIME", required = true)
        @XmlJavaTypeAdapter(DurationAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        protected Duration cycletime;
        @XmlAttribute(name = "OFFSET")
        @XmlJavaTypeAdapter(DurationAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        protected Duration offset;

        /**
         * Gets the value of the cycle property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the cycle property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getCYCLE().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link CONTROL.FIXEDTIME.CYCLE }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public List<CONTROL.FIXEDTIME.CYCLE> getCYCLE() {
            if (cycle == null) {
                cycle = new ArrayList<CONTROL.FIXEDTIME.CYCLE>();
            }
            return this.cycle;
        }

        /**
         * Gets the value of the cycletime property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public Duration getCYCLETIME() {
            return cycletime;
        }

        /**
         * Sets the value of the cycletime property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public void setCYCLETIME(Duration value) {
            this.cycletime = value;
        }

        /**
         * Gets the value of the offset property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public Duration getOFFSET() {
            if (offset == null) {
                return new DurationAdapter().unmarshal("0.0 s");
            } else {
                return offset;
            }
        }

        /**
         * Sets the value of the offset property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public void setOFFSET(Duration value) {
            this.offset = value;
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
         *       &lt;attribute name="SIGNALGROUPID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
         *       &lt;attribute name="OFFSET" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
         *       &lt;attribute name="PREGREEN" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
         *       &lt;attribute name="GREEN" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
         *       &lt;attribute name="YELLOW" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public static class CYCLE
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            private final static long serialVersionUID = 10102L;
            @XmlAttribute(name = "SIGNALGROUPID", required = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            protected String signalgroupid;
            @XmlAttribute(name = "OFFSET", required = true)
            @XmlJavaTypeAdapter(DurationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            protected Duration offset;
            @XmlAttribute(name = "PREGREEN")
            @XmlJavaTypeAdapter(DurationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            protected Duration pregreen;
            @XmlAttribute(name = "GREEN", required = true)
            @XmlJavaTypeAdapter(DurationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            protected Duration green;
            @XmlAttribute(name = "YELLOW", required = true)
            @XmlJavaTypeAdapter(DurationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            protected Duration yellow;

            /**
             * Gets the value of the signalgroupid property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            public String getSIGNALGROUPID() {
                return signalgroupid;
            }

            /**
             * Sets the value of the signalgroupid property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            public void setSIGNALGROUPID(String value) {
                this.signalgroupid = value;
            }

            /**
             * Gets the value of the offset property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            public Duration getOFFSET() {
                return offset;
            }

            /**
             * Sets the value of the offset property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            public void setOFFSET(Duration value) {
                this.offset = value;
            }

            /**
             * Gets the value of the pregreen property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            public Duration getPREGREEN() {
                return pregreen;
            }

            /**
             * Sets the value of the pregreen property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            public void setPREGREEN(Duration value) {
                this.pregreen = value;
            }

            /**
             * Gets the value of the green property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            public Duration getGREEN() {
                return green;
            }

            /**
             * Sets the value of the green property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            public void setGREEN(Duration value) {
                this.green = value;
            }

            /**
             * Gets the value of the yellow property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            public Duration getYELLOW() {
                return yellow;
            }

            /**
             * Sets the value of the yellow property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
            public void setYELLOW(Duration value) {
                this.yellow = value;
            }

        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;extension base="{http://www.opentrafficsim.org/ots}RESPONSIVECONTROLTYPE"&gt;
     *       &lt;choice&gt;
     *         &lt;element name="PROGRAM" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="PROGRAMFILE" type="{http://www.w3.org/2001/XMLSchema}anyURI"/&gt;
     *       &lt;/choice&gt;
     *     &lt;/extension&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "program",
        "programfile"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
    public static class TRAFCOD
        extends RESPONSIVECONTROLTYPE
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "PROGRAM")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        protected String program;
        @XmlElement(name = "PROGRAMFILE")
        @XmlSchemaType(name = "anyURI")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        protected String programfile;

        /**
         * Gets the value of the program property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public String getPROGRAM() {
            return program;
        }

        /**
         * Sets the value of the program property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public void setPROGRAM(String value) {
            this.program = value;
        }

        /**
         * Gets the value of the programfile property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public String getPROGRAMFILE() {
            return programfile;
        }

        /**
         * Sets the value of the programfile property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-22T09:43:59+02:00", comments = "JAXB RI v2.3.0")
        public void setPROGRAMFILE(String value) {
            this.programfile = value;
        }

    }

}