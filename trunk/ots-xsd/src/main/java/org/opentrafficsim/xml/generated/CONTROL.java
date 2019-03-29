//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.03.29 at 04:36:04 PM CET 
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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.xml.bindings.DurationAdapter;
import org.opentrafficsim.xml.bindings.TimeAdapter;


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
 *         &lt;element name="FIXEDTIME" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="SIGNALGROUP" maxOccurs="unbounded"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="TRAFFICLIGHT" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *                           &lt;/sequence&gt;
 *                           &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                           &lt;attribute name="OFFSET" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
 *                           &lt;attribute name="PREGREEN" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
 *                           &lt;attribute name="GREEN" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
 *                           &lt;attribute name="YELLOW" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                 &lt;attribute name="CYCLETIME" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
 *                 &lt;attribute name="OFFSET" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
 *                 &lt;attribute name="STARTTIME" type="{http://www.opentrafficsim.org/ots}TIMETYPE" /&gt;
 *                 &lt;attribute name="ENDTIME" type="{http://www.opentrafficsim.org/ots}TIMETYPE" /&gt;
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
@XmlType(name = "", propOrder = {
    "fixedtime"
})
@XmlRootElement(name = "CONTROL")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
public class CONTROL
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "FIXEDTIME")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    protected List<CONTROL.FIXEDTIME> fixedtime;

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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    public List<CONTROL.FIXEDTIME> getFIXEDTIME() {
        if (fixedtime == null) {
            fixedtime = new ArrayList<CONTROL.FIXEDTIME>();
        }
        return this.fixedtime;
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
     *         &lt;element name="SIGNALGROUP" maxOccurs="unbounded"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="TRAFFICLIGHT" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
     *                 &lt;/sequence&gt;
     *                 &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *                 &lt;attribute name="OFFSET" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
     *                 &lt;attribute name="PREGREEN" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
     *                 &lt;attribute name="GREEN" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
     *                 &lt;attribute name="YELLOW" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
     *       &lt;attribute name="CYCLETIME" use="required" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
     *       &lt;attribute name="OFFSET" type="{http://www.opentrafficsim.org/ots}DURATIONTYPE" /&gt;
     *       &lt;attribute name="STARTTIME" type="{http://www.opentrafficsim.org/ots}TIMETYPE" /&gt;
     *       &lt;attribute name="ENDTIME" type="{http://www.opentrafficsim.org/ots}TIMETYPE" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "signalgroup"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
    public static class FIXEDTIME
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "SIGNALGROUP", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        protected List<CONTROL.FIXEDTIME.SIGNALGROUP> signalgroup;
        @XmlAttribute(name = "ID", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        protected String id;
        @XmlAttribute(name = "CYCLETIME", required = true)
        @XmlJavaTypeAdapter(DurationAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        protected Duration cycletime;
        @XmlAttribute(name = "OFFSET")
        @XmlJavaTypeAdapter(DurationAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        protected Duration offset;
        @XmlAttribute(name = "STARTTIME")
        @XmlJavaTypeAdapter(TimeAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        protected Time starttime;
        @XmlAttribute(name = "ENDTIME")
        @XmlJavaTypeAdapter(TimeAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        protected Time endtime;

        /**
         * Gets the value of the signalgroup property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the signalgroup property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSIGNALGROUP().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link CONTROL.FIXEDTIME.SIGNALGROUP }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        public List<CONTROL.FIXEDTIME.SIGNALGROUP> getSIGNALGROUP() {
            if (signalgroup == null) {
                signalgroup = new ArrayList<CONTROL.FIXEDTIME.SIGNALGROUP>();
            }
            return this.signalgroup;
        }

        /**
         * Gets the value of the id property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        public void setID(String value) {
            this.id = value;
        }

        /**
         * Gets the value of the cycletime property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        public void setOFFSET(Duration value) {
            this.offset = value;
        }

        /**
         * Gets the value of the starttime property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        public Time getSTARTTIME() {
            return starttime;
        }

        /**
         * Sets the value of the starttime property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        public void setSTARTTIME(Time value) {
            this.starttime = value;
        }

        /**
         * Gets the value of the endtime property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        public Time getENDTIME() {
            return endtime;
        }

        /**
         * Sets the value of the endtime property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        public void setENDTIME(Time value) {
            this.endtime = value;
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
         *         &lt;element name="TRAFFICLIGHT" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
         *       &lt;/sequence&gt;
         *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
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
        @XmlType(name = "", propOrder = {
            "trafficlight"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
        public static class SIGNALGROUP
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
            private final static long serialVersionUID = 10102L;
            @XmlElement(name = "TRAFFICLIGHT", required = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
            protected List<String> trafficlight;
            @XmlAttribute(name = "ID", required = true)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
            protected String id;
            @XmlAttribute(name = "OFFSET", required = true)
            @XmlJavaTypeAdapter(DurationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
            protected Duration offset;
            @XmlAttribute(name = "PREGREEN")
            @XmlJavaTypeAdapter(DurationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
            protected Duration pregreen;
            @XmlAttribute(name = "GREEN", required = true)
            @XmlJavaTypeAdapter(DurationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
            protected Duration green;
            @XmlAttribute(name = "YELLOW", required = true)
            @XmlJavaTypeAdapter(DurationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
            protected Duration yellow;

            /**
             * Gets the value of the trafficlight property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the trafficlight property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getTRAFFICLIGHT().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             * 
             * 
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
            public List<String> getTRAFFICLIGHT() {
                if (trafficlight == null) {
                    trafficlight = new ArrayList<String>();
                }
                return this.trafficlight;
            }

            /**
             * Gets the value of the id property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
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
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
            public void setID(String value) {
                this.id = value;
            }

            /**
             * Gets the value of the offset property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
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
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
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
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
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
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
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
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
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
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
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
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
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
            @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-03-29T04:36:04+01:00", comments = "JAXB RI v2.3.0")
            public void setYELLOW(Duration value) {
                this.yellow = value;
            }

        }

    }

}
