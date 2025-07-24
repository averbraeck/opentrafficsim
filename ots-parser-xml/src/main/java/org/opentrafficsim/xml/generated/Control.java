//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v2.3.7 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2025.07.24 um 01:18:43 PM CEST 
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
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DurationAdapter;
import org.opentrafficsim.xml.bindings.EncodingAdapter;
import org.opentrafficsim.xml.bindings.GraphicsTypeAdapter;
import org.opentrafficsim.xml.bindings.SpaceAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.DurationType;
import org.opentrafficsim.xml.bindings.types.EncodingType;
import org.opentrafficsim.xml.bindings.types.GraphicsTypeType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="FixedTime" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;extension base="{http://www.opentrafficsim.org/ots}ControlType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Cycle" maxOccurs="unbounded"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;attribute name="SignalGroupId" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                           &lt;attribute name="Offset" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
 *                           &lt;attribute name="PreGreen" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
 *                           &lt;attribute name="Green" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
 *                           &lt;attribute name="Yellow" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="CycleTime" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
 *                 &lt;attribute name="Offset" type="{http://www.opentrafficsim.org/ots}DurationType" default="0.0 s" /&gt;
 *               &lt;/extension&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="TrafCod" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;extension base="{http://www.opentrafficsim.org/ots}ResponsiveControlType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;choice&gt;
 *                     &lt;element name="Program"&gt;
 *                       &lt;complexType&gt;
 *                         &lt;simpleContent&gt;
 *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;MultiLineString"&gt;
 *                             &lt;attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" /&gt;
 *                           &lt;/extension&gt;
 *                         &lt;/simpleContent&gt;
 *                       &lt;/complexType&gt;
 *                     &lt;/element&gt;
 *                     &lt;element name="ProgramFile" type="{http://www.opentrafficsim.org/ots}anyURI"/&gt;
 *                   &lt;/choice&gt;
 *                   &lt;element name="Console"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;choice&gt;
 *                               &lt;element name="Map"&gt;
 *                                 &lt;complexType&gt;
 *                                   &lt;simpleContent&gt;
 *                                     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;MultiLineString"&gt;
 *                                       &lt;attribute name="Type" type="{http://www.opentrafficsim.org/ots}GraphicsType" /&gt;
 *                                       &lt;attribute name="Encoding" type="{http://www.opentrafficsim.org/ots}EncodingType" /&gt;
 *                                       &lt;attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" /&gt;
 *                                     &lt;/extension&gt;
 *                                   &lt;/simpleContent&gt;
 *                                 &lt;/complexType&gt;
 *                               &lt;/element&gt;
 *                               &lt;element name="MapFile" type="{http://www.opentrafficsim.org/ots}anyURI"/&gt;
 *                             &lt;/choice&gt;
 *                             &lt;choice&gt;
 *                               &lt;element name="Coordinates" type="{http://www.opentrafficsim.org/ots}TrafCodCoordinatesType"/&gt;
 *                               &lt;element name="CoordinatesFile" type="{http://www.opentrafficsim.org/ots}anyURI"/&gt;
 *                             &lt;/choice&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
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
    "fixedTime",
    "trafCod"
})
@XmlRootElement(name = "Control")
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class Control
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "FixedTime")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<Control.FixedTime> fixedTime;
    @XmlElement(name = "TrafCod")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<Control.TrafCod> trafCod;

    /**
     * Gets the value of the fixedTime property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fixedTime property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFixedTime().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Control.FixedTime }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<Control.FixedTime> getFixedTime() {
        if (fixedTime == null) {
            fixedTime = new ArrayList<Control.FixedTime>();
        }
        return this.fixedTime;
    }

    /**
     * Gets the value of the trafCod property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trafCod property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrafCod().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Control.TrafCod }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<Control.TrafCod> getTrafCod() {
        if (trafCod == null) {
            trafCod = new ArrayList<Control.TrafCod>();
        }
        return this.trafCod;
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;extension base="{http://www.opentrafficsim.org/ots}ControlType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element name="Cycle" maxOccurs="unbounded"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;attribute name="SignalGroupId" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                 &lt;attribute name="Offset" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
     *                 &lt;attribute name="PreGreen" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
     *                 &lt;attribute name="Green" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
     *                 &lt;attribute name="Yellow" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
     *       &lt;attribute name="CycleTime" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
     *       &lt;attribute name="Offset" type="{http://www.opentrafficsim.org/ots}DurationType" default="0.0 s" /&gt;
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class FixedTime
        extends ControlType
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "Cycle", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected List<Control.FixedTime.Cycle> cycle;
        @XmlAttribute(name = "CycleTime", required = true)
        @XmlJavaTypeAdapter(DurationAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected DurationType cycleTime;
        @XmlAttribute(name = "Offset")
        @XmlJavaTypeAdapter(DurationAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected DurationType offset;

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
         *    getCycle().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Control.FixedTime.Cycle }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public List<Control.FixedTime.Cycle> getCycle() {
            if (cycle == null) {
                cycle = new ArrayList<Control.FixedTime.Cycle>();
            }
            return this.cycle;
        }

        /**
         * Ruft den Wert der cycleTime-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public DurationType getCycleTime() {
            return cycleTime;
        }

        /**
         * Legt den Wert der cycleTime-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setCycleTime(DurationType value) {
            this.cycleTime = value;
        }

        /**
         * Ruft den Wert der offset-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public DurationType getOffset() {
            if (offset == null) {
                return new DurationAdapter().unmarshal("0.0 s");
            } else {
                return offset;
            }
        }

        /**
         * Legt den Wert der offset-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setOffset(DurationType value) {
            this.offset = value;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;attribute name="SignalGroupId" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *       &lt;attribute name="Offset" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
         *       &lt;attribute name="PreGreen" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
         *       &lt;attribute name="Green" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
         *       &lt;attribute name="Yellow" use="required" type="{http://www.opentrafficsim.org/ots}DurationType" /&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Cycle
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlAttribute(name = "SignalGroupId", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected org.opentrafficsim.xml.bindings.types.StringType signalGroupId;
            @XmlAttribute(name = "Offset", required = true)
            @XmlJavaTypeAdapter(DurationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected DurationType offset;
            @XmlAttribute(name = "PreGreen")
            @XmlJavaTypeAdapter(DurationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected DurationType preGreen;
            @XmlAttribute(name = "Green", required = true)
            @XmlJavaTypeAdapter(DurationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected DurationType green;
            @XmlAttribute(name = "Yellow", required = true)
            @XmlJavaTypeAdapter(DurationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected DurationType yellow;

            /**
             * Ruft den Wert der signalGroupId-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public org.opentrafficsim.xml.bindings.types.StringType getSignalGroupId() {
                return signalGroupId;
            }

            /**
             * Legt den Wert der signalGroupId-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setSignalGroupId(org.opentrafficsim.xml.bindings.types.StringType value) {
                this.signalGroupId = value;
            }

            /**
             * Ruft den Wert der offset-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public DurationType getOffset() {
                return offset;
            }

            /**
             * Legt den Wert der offset-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setOffset(DurationType value) {
                this.offset = value;
            }

            /**
             * Ruft den Wert der preGreen-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public DurationType getPreGreen() {
                return preGreen;
            }

            /**
             * Legt den Wert der preGreen-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setPreGreen(DurationType value) {
                this.preGreen = value;
            }

            /**
             * Ruft den Wert der green-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public DurationType getGreen() {
                return green;
            }

            /**
             * Legt den Wert der green-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setGreen(DurationType value) {
                this.green = value;
            }

            /**
             * Ruft den Wert der yellow-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public DurationType getYellow() {
                return yellow;
            }

            /**
             * Legt den Wert der yellow-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setYellow(DurationType value) {
                this.yellow = value;
            }

        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     * 
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;extension base="{http://www.opentrafficsim.org/ots}ResponsiveControlType"&gt;
     *       &lt;sequence&gt;
     *         &lt;choice&gt;
     *           &lt;element name="Program"&gt;
     *             &lt;complexType&gt;
     *               &lt;simpleContent&gt;
     *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;MultiLineString"&gt;
     *                   &lt;attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" /&gt;
     *                 &lt;/extension&gt;
     *               &lt;/simpleContent&gt;
     *             &lt;/complexType&gt;
     *           &lt;/element&gt;
     *           &lt;element name="ProgramFile" type="{http://www.opentrafficsim.org/ots}anyURI"/&gt;
     *         &lt;/choice&gt;
     *         &lt;element name="Console"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;choice&gt;
     *                     &lt;element name="Map"&gt;
     *                       &lt;complexType&gt;
     *                         &lt;simpleContent&gt;
     *                           &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;MultiLineString"&gt;
     *                             &lt;attribute name="Type" type="{http://www.opentrafficsim.org/ots}GraphicsType" /&gt;
     *                             &lt;attribute name="Encoding" type="{http://www.opentrafficsim.org/ots}EncodingType" /&gt;
     *                             &lt;attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" /&gt;
     *                           &lt;/extension&gt;
     *                         &lt;/simpleContent&gt;
     *                       &lt;/complexType&gt;
     *                     &lt;/element&gt;
     *                     &lt;element name="MapFile" type="{http://www.opentrafficsim.org/ots}anyURI"/&gt;
     *                   &lt;/choice&gt;
     *                   &lt;choice&gt;
     *                     &lt;element name="Coordinates" type="{http://www.opentrafficsim.org/ots}TrafCodCoordinatesType"/&gt;
     *                     &lt;element name="CoordinatesFile" type="{http://www.opentrafficsim.org/ots}anyURI"/&gt;
     *                   &lt;/choice&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/sequence&gt;
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
        "programFile",
        "console"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class TrafCod
        extends ResponsiveControlType
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "Program")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected Control.TrafCod.Program program;
        @XmlElement(name = "ProgramFile")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected String programFile;
        @XmlElement(name = "Console", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected Control.TrafCod.Console console;

        /**
         * Ruft den Wert der program-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Control.TrafCod.Program }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public Control.TrafCod.Program getProgram() {
            return program;
        }

        /**
         * Legt den Wert der program-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Control.TrafCod.Program }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setProgram(Control.TrafCod.Program value) {
            this.program = value;
        }

        /**
         * Ruft den Wert der programFile-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public String getProgramFile() {
            return programFile;
        }

        /**
         * Legt den Wert der programFile-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setProgramFile(String value) {
            this.programFile = value;
        }

        /**
         * Ruft den Wert der console-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link Control.TrafCod.Console }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public Control.TrafCod.Console getConsole() {
            return console;
        }

        /**
         * Legt den Wert der console-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link Control.TrafCod.Console }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setConsole(Control.TrafCod.Console value) {
            this.console = value;
        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;complexContent&gt;
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;choice&gt;
         *           &lt;element name="Map"&gt;
         *             &lt;complexType&gt;
         *               &lt;simpleContent&gt;
         *                 &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;MultiLineString"&gt;
         *                   &lt;attribute name="Type" type="{http://www.opentrafficsim.org/ots}GraphicsType" /&gt;
         *                   &lt;attribute name="Encoding" type="{http://www.opentrafficsim.org/ots}EncodingType" /&gt;
         *                   &lt;attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" /&gt;
         *                 &lt;/extension&gt;
         *               &lt;/simpleContent&gt;
         *             &lt;/complexType&gt;
         *           &lt;/element&gt;
         *           &lt;element name="MapFile" type="{http://www.opentrafficsim.org/ots}anyURI"/&gt;
         *         &lt;/choice&gt;
         *         &lt;choice&gt;
         *           &lt;element name="Coordinates" type="{http://www.opentrafficsim.org/ots}TrafCodCoordinatesType"/&gt;
         *           &lt;element name="CoordinatesFile" type="{http://www.opentrafficsim.org/ots}anyURI"/&gt;
         *         &lt;/choice&gt;
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
            "map",
            "mapFile",
            "coordinates",
            "coordinatesFile"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Console
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlElement(name = "Map")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected Control.TrafCod.Console.Map map;
            @XmlElement(name = "MapFile")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected String mapFile;
            @XmlElement(name = "Coordinates")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected TrafCodCoordinatesType coordinates;
            @XmlElement(name = "CoordinatesFile")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected String coordinatesFile;

            /**
             * Ruft den Wert der map-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link Control.TrafCod.Console.Map }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public Control.TrafCod.Console.Map getMap() {
                return map;
            }

            /**
             * Legt den Wert der map-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link Control.TrafCod.Console.Map }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setMap(Control.TrafCod.Console.Map value) {
                this.map = value;
            }

            /**
             * Ruft den Wert der mapFile-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public String getMapFile() {
                return mapFile;
            }

            /**
             * Legt den Wert der mapFile-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setMapFile(String value) {
                this.mapFile = value;
            }

            /**
             * Ruft den Wert der coordinates-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link TrafCodCoordinatesType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public TrafCodCoordinatesType getCoordinates() {
                return coordinates;
            }

            /**
             * Legt den Wert der coordinates-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link TrafCodCoordinatesType }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setCoordinates(TrafCodCoordinatesType value) {
                this.coordinates = value;
            }

            /**
             * Ruft den Wert der coordinatesFile-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public String getCoordinatesFile() {
                return coordinatesFile;
            }

            /**
             * Legt den Wert der coordinatesFile-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setCoordinatesFile(String value) {
                this.coordinatesFile = value;
            }


            /**
             * <p>Java-Klasse für anonymous complex type.
             * 
             * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
             * 
             * <pre>
             * &lt;complexType&gt;
             *   &lt;simpleContent&gt;
             *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;MultiLineString"&gt;
             *       &lt;attribute name="Type" type="{http://www.opentrafficsim.org/ots}GraphicsType" /&gt;
             *       &lt;attribute name="Encoding" type="{http://www.opentrafficsim.org/ots}EncodingType" /&gt;
             *       &lt;attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" /&gt;
             *     &lt;/extension&gt;
             *   &lt;/simpleContent&gt;
             * &lt;/complexType&gt;
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {
                "value"
            })
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public static class Map
                implements Serializable
            {

                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                private final static long serialVersionUID = 10102L;
                @XmlValue
                @XmlJavaTypeAdapter(StringAdapter.class)
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected org.opentrafficsim.xml.bindings.types.StringType value;
                @XmlAttribute(name = "Type")
                @XmlJavaTypeAdapter(GraphicsTypeAdapter.class)
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected GraphicsTypeType type;
                @XmlAttribute(name = "Encoding")
                @XmlJavaTypeAdapter(EncodingAdapter.class)
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EncodingType encoding;
                @XmlAttribute(name = "Space")
                @XmlJavaTypeAdapter(SpaceAdapter.class)
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected org.opentrafficsim.xml.bindings.types.StringType space;

                /**
                 * Ruft den Wert der value-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public org.opentrafficsim.xml.bindings.types.StringType getValue() {
                    return value;
                }

                /**
                 * Legt den Wert der value-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setValue(org.opentrafficsim.xml.bindings.types.StringType value) {
                    this.value = value;
                }

                /**
                 * Ruft den Wert der type-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public GraphicsTypeType getType() {
                    return type;
                }

                /**
                 * Legt den Wert der type-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setType(GraphicsTypeType value) {
                    this.type = value;
                }

                /**
                 * Ruft den Wert der encoding-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EncodingType getEncoding() {
                    return encoding;
                }

                /**
                 * Legt den Wert der encoding-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setEncoding(EncodingType value) {
                    this.encoding = value;
                }

                /**
                 * Ruft den Wert der space-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public org.opentrafficsim.xml.bindings.types.StringType getSpace() {
                    if (space == null) {
                        return new SpaceAdapter().unmarshal("preserve");
                    } else {
                        return space;
                    }
                }

                /**
                 * Legt den Wert der space-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setSpace(org.opentrafficsim.xml.bindings.types.StringType value) {
                    this.space = value;
                }

            }

        }


        /**
         * <p>Java-Klasse für anonymous complex type.
         * 
         * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
         * 
         * <pre>
         * &lt;complexType&gt;
         *   &lt;simpleContent&gt;
         *     &lt;extension base="&lt;http://www.opentrafficsim.org/ots&gt;MultiLineString"&gt;
         *       &lt;attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" /&gt;
         *     &lt;/extension&gt;
         *   &lt;/simpleContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Program
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlValue
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected org.opentrafficsim.xml.bindings.types.StringType value;
            @XmlAttribute(name = "Space")
            @XmlJavaTypeAdapter(SpaceAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected org.opentrafficsim.xml.bindings.types.StringType space;

            /**
             * Ruft den Wert der value-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public org.opentrafficsim.xml.bindings.types.StringType getValue() {
                return value;
            }

            /**
             * Legt den Wert der value-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setValue(org.opentrafficsim.xml.bindings.types.StringType value) {
                this.value = value;
            }

            /**
             * Ruft den Wert der space-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public org.opentrafficsim.xml.bindings.types.StringType getSpace() {
                if (space == null) {
                    return new SpaceAdapter().unmarshal("preserve");
                } else {
                    return space;
                }
            }

            /**
             * Legt den Wert der space-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setSpace(org.opentrafficsim.xml.bindings.types.StringType value) {
                this.space = value;
            }

        }

    }

}
