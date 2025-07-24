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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.LengthBeginEndAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für ResponsiveControlType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ResponsiveControlType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.opentrafficsim.org/ots}ControlType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Detector" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="MultipleLane"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="EntryLink" type="{http://www.opentrafficsim.org/ots}string"/&gt;
 *                             &lt;element name="EntryLane" type="{http://www.opentrafficsim.org/ots}string"/&gt;
 *                             &lt;element name="EntryPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/&gt;
 *                             &lt;element name="IntermediateLanes" type="{http://www.opentrafficsim.org/ots}LaneLinkType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                             &lt;element name="ExitLink" type="{http://www.opentrafficsim.org/ots}string"/&gt;
 *                             &lt;element name="ExitLane" type="{http://www.opentrafficsim.org/ots}string"/&gt;
 *                             &lt;element name="ExitPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="SingleLane"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="Link" type="{http://www.opentrafficsim.org/ots}string"/&gt;
 *                             &lt;element name="Lane" type="{http://www.opentrafficsim.org/ots}string"/&gt;
 *                             &lt;element name="EntryPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/&gt;
 *                             &lt;element name="ExitPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/choice&gt;
 *                 &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" /&gt;
 *                 &lt;attribute name="Type" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
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
@XmlType(name = "ResponsiveControlType", propOrder = {
    "detector"
})
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.Control.TrafCod.class
})
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class ResponsiveControlType
    extends ControlType
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "Detector", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<ResponsiveControlType.Detector> detector;

    /**
     * Gets the value of the detector property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the detector property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDetector().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResponsiveControlType.Detector }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<ResponsiveControlType.Detector> getDetector() {
        if (detector == null) {
            detector = new ArrayList<ResponsiveControlType.Detector>();
        }
        return this.detector;
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
     *       &lt;choice&gt;
     *         &lt;element name="MultipleLane"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="EntryLink" type="{http://www.opentrafficsim.org/ots}string"/&gt;
     *                   &lt;element name="EntryLane" type="{http://www.opentrafficsim.org/ots}string"/&gt;
     *                   &lt;element name="EntryPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/&gt;
     *                   &lt;element name="IntermediateLanes" type="{http://www.opentrafficsim.org/ots}LaneLinkType" maxOccurs="unbounded" minOccurs="0"/&gt;
     *                   &lt;element name="ExitLink" type="{http://www.opentrafficsim.org/ots}string"/&gt;
     *                   &lt;element name="ExitLane" type="{http://www.opentrafficsim.org/ots}string"/&gt;
     *                   &lt;element name="ExitPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="SingleLane"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="Link" type="{http://www.opentrafficsim.org/ots}string"/&gt;
     *                   &lt;element name="Lane" type="{http://www.opentrafficsim.org/ots}string"/&gt;
     *                   &lt;element name="EntryPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/&gt;
     *                   &lt;element name="ExitPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *       &lt;/choice&gt;
     *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" /&gt;
     *       &lt;attribute name="Type" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "multipleLane",
        "singleLane"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class Detector
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "MultipleLane")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected ResponsiveControlType.Detector.MultipleLane multipleLane;
        @XmlElement(name = "SingleLane")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected ResponsiveControlType.Detector.SingleLane singleLane;
        @XmlAttribute(name = "Id", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected String id;
        @XmlAttribute(name = "Type", required = true)
        @XmlJavaTypeAdapter(StringAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected StringType type;

        /**
         * Ruft den Wert der multipleLane-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ResponsiveControlType.Detector.MultipleLane }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public ResponsiveControlType.Detector.MultipleLane getMultipleLane() {
            return multipleLane;
        }

        /**
         * Legt den Wert der multipleLane-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ResponsiveControlType.Detector.MultipleLane }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setMultipleLane(ResponsiveControlType.Detector.MultipleLane value) {
            this.multipleLane = value;
        }

        /**
         * Ruft den Wert der singleLane-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link ResponsiveControlType.Detector.SingleLane }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public ResponsiveControlType.Detector.SingleLane getSingleLane() {
            return singleLane;
        }

        /**
         * Legt den Wert der singleLane-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link ResponsiveControlType.Detector.SingleLane }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setSingleLane(ResponsiveControlType.Detector.SingleLane value) {
            this.singleLane = value;
        }

        /**
         * Ruft den Wert der id-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public String getId() {
            return id;
        }

        /**
         * Legt den Wert der id-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setId(String value) {
            this.id = value;
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
        public StringType getType() {
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
        public void setType(StringType value) {
            this.type = value;
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
         *         &lt;element name="EntryLink" type="{http://www.opentrafficsim.org/ots}string"/&gt;
         *         &lt;element name="EntryLane" type="{http://www.opentrafficsim.org/ots}string"/&gt;
         *         &lt;element name="EntryPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/&gt;
         *         &lt;element name="IntermediateLanes" type="{http://www.opentrafficsim.org/ots}LaneLinkType" maxOccurs="unbounded" minOccurs="0"/&gt;
         *         &lt;element name="ExitLink" type="{http://www.opentrafficsim.org/ots}string"/&gt;
         *         &lt;element name="ExitLane" type="{http://www.opentrafficsim.org/ots}string"/&gt;
         *         &lt;element name="ExitPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/&gt;
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
            "entryLink",
            "entryLane",
            "entryPosition",
            "intermediateLanes",
            "exitLink",
            "exitLane",
            "exitPosition"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class MultipleLane
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlElement(name = "EntryLink", required = true, type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType entryLink;
            @XmlElement(name = "EntryLane", required = true, type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType entryLane;
            @XmlElement(name = "EntryPosition", required = true, type = String.class)
            @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected LengthBeginEndType entryPosition;
            @XmlElement(name = "IntermediateLanes")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected List<LaneLinkType> intermediateLanes;
            @XmlElement(name = "ExitLink", required = true, type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType exitLink;
            @XmlElement(name = "ExitLane", required = true, type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType exitLane;
            @XmlElement(name = "ExitPosition", required = true, type = String.class)
            @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected LengthBeginEndType exitPosition;

            /**
             * Ruft den Wert der entryLink-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getEntryLink() {
                return entryLink;
            }

            /**
             * Legt den Wert der entryLink-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setEntryLink(StringType value) {
                this.entryLink = value;
            }

            /**
             * Ruft den Wert der entryLane-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getEntryLane() {
                return entryLane;
            }

            /**
             * Legt den Wert der entryLane-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setEntryLane(StringType value) {
                this.entryLane = value;
            }

            /**
             * Ruft den Wert der entryPosition-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public LengthBeginEndType getEntryPosition() {
                return entryPosition;
            }

            /**
             * Legt den Wert der entryPosition-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setEntryPosition(LengthBeginEndType value) {
                this.entryPosition = value;
            }

            /**
             * Gets the value of the intermediateLanes property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the intermediateLanes property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getIntermediateLanes().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link LaneLinkType }
             * 
             * 
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public List<LaneLinkType> getIntermediateLanes() {
                if (intermediateLanes == null) {
                    intermediateLanes = new ArrayList<LaneLinkType>();
                }
                return this.intermediateLanes;
            }

            /**
             * Ruft den Wert der exitLink-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getExitLink() {
                return exitLink;
            }

            /**
             * Legt den Wert der exitLink-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setExitLink(StringType value) {
                this.exitLink = value;
            }

            /**
             * Ruft den Wert der exitLane-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getExitLane() {
                return exitLane;
            }

            /**
             * Legt den Wert der exitLane-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setExitLane(StringType value) {
                this.exitLane = value;
            }

            /**
             * Ruft den Wert der exitPosition-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public LengthBeginEndType getExitPosition() {
                return exitPosition;
            }

            /**
             * Legt den Wert der exitPosition-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setExitPosition(LengthBeginEndType value) {
                this.exitPosition = value;
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
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="Link" type="{http://www.opentrafficsim.org/ots}string"/&gt;
         *         &lt;element name="Lane" type="{http://www.opentrafficsim.org/ots}string"/&gt;
         *         &lt;element name="EntryPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/&gt;
         *         &lt;element name="ExitPosition" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType"/&gt;
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
            "link",
            "lane",
            "entryPosition",
            "exitPosition"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class SingleLane
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlElement(name = "Link", required = true, type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType link;
            @XmlElement(name = "Lane", required = true, type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType lane;
            @XmlElement(name = "EntryPosition", required = true, type = String.class)
            @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected LengthBeginEndType entryPosition;
            @XmlElement(name = "ExitPosition", required = true, type = String.class)
            @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected LengthBeginEndType exitPosition;

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
             * Ruft den Wert der entryPosition-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public LengthBeginEndType getEntryPosition() {
                return entryPosition;
            }

            /**
             * Legt den Wert der entryPosition-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setEntryPosition(LengthBeginEndType value) {
                this.entryPosition = value;
            }

            /**
             * Ruft den Wert der exitPosition-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public LengthBeginEndType getExitPosition() {
                return exitPosition;
            }

            /**
             * Legt den Wert der exitPosition-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setExitPosition(LengthBeginEndType value) {
                this.exitPosition = value;
            }

        }

    }

}
