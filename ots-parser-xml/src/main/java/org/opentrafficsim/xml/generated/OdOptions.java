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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DoubleAdapter;
import org.opentrafficsim.xml.bindings.HeadwayDistributionAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.DoubleType;
import org.opentrafficsim.xml.bindings.types.HeadwayDistributionType;
import org.opentrafficsim.xml.bindings.types.LengthType;
import org.opentrafficsim.xml.bindings.types.StringType;


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
 *         &lt;element name="OdOptionsItem" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;choice&gt;
 *                     &lt;element name="Global" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
 *                     &lt;element name="LinkType" type="{http://www.opentrafficsim.org/ots}string"/&gt;
 *                     &lt;element name="Origin" type="{http://www.opentrafficsim.org/ots}string"/&gt;
 *                     &lt;element name="Lane" type="{http://www.opentrafficsim.org/ots}LaneLinkType"/&gt;
 *                   &lt;/choice&gt;
 *                   &lt;element name="DefaultModel" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;choice&gt;
 *                             &lt;element name="Id" type="{http://www.opentrafficsim.org/ots}string"/&gt;
 *                             &lt;element name="ModelIdReferral" type="{http://www.opentrafficsim.org/ots}string"/&gt;
 *                           &lt;/choice&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="Model" maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;choice&gt;
 *                             &lt;element name="Id" type="{http://www.opentrafficsim.org/ots}string"/&gt;
 *                             &lt;element name="ModelIdReferral" type="{http://www.opentrafficsim.org/ots}string"/&gt;
 *                           &lt;/choice&gt;
 *                           &lt;attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="NoLaneChange" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" minOccurs="0"/&gt;
 *                   &lt;element name="RoomChecker" type="{http://www.opentrafficsim.org/ots}RoomCheckerType" minOccurs="0"/&gt;
 *                   &lt;element name="HeadwayDist" minOccurs="0"&gt;
 *                     &lt;simpleType&gt;
 *                       &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
 *                         &lt;simpleType&gt;
 *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                             &lt;enumeration value="CONSTANT"/&gt;
 *                             &lt;enumeration value="EXPONENTIAL"/&gt;
 *                             &lt;enumeration value="UNIFORM"/&gt;
 *                             &lt;enumeration value="TRIANGULAR"/&gt;
 *                             &lt;enumeration value="TRI_EXP"/&gt;
 *                             &lt;enumeration value="LOGNORMAL"/&gt;
 *                           &lt;/restriction&gt;
 *                         &lt;/simpleType&gt;
 *                       &lt;/union&gt;
 *                     &lt;/simpleType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="Markov" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="State" maxOccurs="unbounded"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;complexContent&gt;
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                     &lt;attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                                     &lt;attribute name="Parent" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                                     &lt;attribute name="Correlation" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                                   &lt;/restriction&gt;
 *                                 &lt;/complexContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="LaneBiases" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *                             &lt;element ref="{http://www.opentrafficsim.org/ots}LaneBias"/&gt;
 *                             &lt;element name="DefinedLaneBias"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;complexContent&gt;
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                     &lt;attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *                                   &lt;/restriction&gt;
 *                                 &lt;/complexContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                           &lt;/choice&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Id" type="{http://www.opentrafficsim.org/ots}IdType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "odOptionsItem"
})
@XmlRootElement(name = "OdOptions")
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class OdOptions
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "OdOptionsItem", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<OdOptions.OdOptionsItem> odOptionsItem;
    @XmlAttribute(name = "Id")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected String id;

    /**
     * Gets the value of the odOptionsItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the odOptionsItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOdOptionsItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OdOptions.OdOptionsItem }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<OdOptions.OdOptionsItem> getOdOptionsItem() {
        if (odOptionsItem == null) {
            odOptionsItem = new ArrayList<OdOptions.OdOptionsItem>();
        }
        return this.odOptionsItem;
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
     *           &lt;element name="Global" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
     *           &lt;element name="LinkType" type="{http://www.opentrafficsim.org/ots}string"/&gt;
     *           &lt;element name="Origin" type="{http://www.opentrafficsim.org/ots}string"/&gt;
     *           &lt;element name="Lane" type="{http://www.opentrafficsim.org/ots}LaneLinkType"/&gt;
     *         &lt;/choice&gt;
     *         &lt;element name="DefaultModel" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;choice&gt;
     *                   &lt;element name="Id" type="{http://www.opentrafficsim.org/ots}string"/&gt;
     *                   &lt;element name="ModelIdReferral" type="{http://www.opentrafficsim.org/ots}string"/&gt;
     *                 &lt;/choice&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="Model" maxOccurs="unbounded" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;choice&gt;
     *                   &lt;element name="Id" type="{http://www.opentrafficsim.org/ots}string"/&gt;
     *                   &lt;element name="ModelIdReferral" type="{http://www.opentrafficsim.org/ots}string"/&gt;
     *                 &lt;/choice&gt;
     *                 &lt;attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="NoLaneChange" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" minOccurs="0"/&gt;
     *         &lt;element name="RoomChecker" type="{http://www.opentrafficsim.org/ots}RoomCheckerType" minOccurs="0"/&gt;
     *         &lt;element name="HeadwayDist" minOccurs="0"&gt;
     *           &lt;simpleType&gt;
     *             &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
     *               &lt;simpleType&gt;
     *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                   &lt;enumeration value="CONSTANT"/&gt;
     *                   &lt;enumeration value="EXPONENTIAL"/&gt;
     *                   &lt;enumeration value="UNIFORM"/&gt;
     *                   &lt;enumeration value="TRIANGULAR"/&gt;
     *                   &lt;enumeration value="TRI_EXP"/&gt;
     *                   &lt;enumeration value="LOGNORMAL"/&gt;
     *                 &lt;/restriction&gt;
     *               &lt;/simpleType&gt;
     *             &lt;/union&gt;
     *           &lt;/simpleType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="Markov" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="State" maxOccurs="unbounded"&gt;
     *                     &lt;complexType&gt;
     *                       &lt;complexContent&gt;
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                           &lt;attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                           &lt;attribute name="Parent" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                           &lt;attribute name="Correlation" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
     *                         &lt;/restriction&gt;
     *                       &lt;/complexContent&gt;
     *                     &lt;/complexType&gt;
     *                   &lt;/element&gt;
     *                 &lt;/sequence&gt;
     *               &lt;/restriction&gt;
     *             &lt;/complexContent&gt;
     *           &lt;/complexType&gt;
     *         &lt;/element&gt;
     *         &lt;element name="LaneBiases" minOccurs="0"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
     *                   &lt;element ref="{http://www.opentrafficsim.org/ots}LaneBias"/&gt;
     *                   &lt;element name="DefinedLaneBias"&gt;
     *                     &lt;complexType&gt;
     *                       &lt;complexContent&gt;
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                           &lt;attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
     *                         &lt;/restriction&gt;
     *                       &lt;/complexContent&gt;
     *                     &lt;/complexType&gt;
     *                   &lt;/element&gt;
     *                 &lt;/choice&gt;
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
        "global",
        "linkType",
        "origin",
        "lane",
        "defaultModel",
        "model",
        "noLaneChange",
        "roomChecker",
        "headwayDist",
        "markov",
        "laneBiases"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class OdOptionsItem
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "Global")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected EmptyType global;
        @XmlElement(name = "LinkType", type = String.class)
        @XmlJavaTypeAdapter(StringAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected StringType linkType;
        @XmlElement(name = "Origin", type = String.class)
        @XmlJavaTypeAdapter(StringAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected StringType origin;
        @XmlElement(name = "Lane")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected LaneLinkType lane;
        @XmlElement(name = "DefaultModel")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected OdOptions.OdOptionsItem.DefaultModel defaultModel;
        @XmlElement(name = "Model")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected List<OdOptions.OdOptionsItem.Model> model;
        @XmlElement(name = "NoLaneChange", type = String.class)
        @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected LengthType noLaneChange;
        @XmlElement(name = "RoomChecker")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected RoomCheckerType roomChecker;
        @XmlElement(name = "HeadwayDist", type = String.class)
        @XmlJavaTypeAdapter(HeadwayDistributionAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected HeadwayDistributionType headwayDist;
        @XmlElement(name = "Markov")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected OdOptions.OdOptionsItem.Markov markov;
        @XmlElement(name = "LaneBiases")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected OdOptions.OdOptionsItem.LaneBiases laneBiases;

        /**
         * Ruft den Wert der global-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public EmptyType getGlobal() {
            return global;
        }

        /**
         * Legt den Wert der global-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setGlobal(EmptyType value) {
            this.global = value;
        }

        /**
         * Ruft den Wert der linkType-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public StringType getLinkType() {
            return linkType;
        }

        /**
         * Legt den Wert der linkType-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setLinkType(StringType value) {
            this.linkType = value;
        }

        /**
         * Ruft den Wert der origin-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public StringType getOrigin() {
            return origin;
        }

        /**
         * Legt den Wert der origin-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setOrigin(StringType value) {
            this.origin = value;
        }

        /**
         * Ruft den Wert der lane-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link LaneLinkType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public LaneLinkType getLane() {
            return lane;
        }

        /**
         * Legt den Wert der lane-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link LaneLinkType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setLane(LaneLinkType value) {
            this.lane = value;
        }

        /**
         * Ruft den Wert der defaultModel-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link OdOptions.OdOptionsItem.DefaultModel }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public OdOptions.OdOptionsItem.DefaultModel getDefaultModel() {
            return defaultModel;
        }

        /**
         * Legt den Wert der defaultModel-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link OdOptions.OdOptionsItem.DefaultModel }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setDefaultModel(OdOptions.OdOptionsItem.DefaultModel value) {
            this.defaultModel = value;
        }

        /**
         * Gets the value of the model property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the model property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getModel().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link OdOptions.OdOptionsItem.Model }
         * 
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public List<OdOptions.OdOptionsItem.Model> getModel() {
            if (model == null) {
                model = new ArrayList<OdOptions.OdOptionsItem.Model>();
            }
            return this.model;
        }

        /**
         * Ruft den Wert der noLaneChange-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public LengthType getNoLaneChange() {
            return noLaneChange;
        }

        /**
         * Legt den Wert der noLaneChange-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setNoLaneChange(LengthType value) {
            this.noLaneChange = value;
        }

        /**
         * Ruft den Wert der roomChecker-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link RoomCheckerType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public RoomCheckerType getRoomChecker() {
            return roomChecker;
        }

        /**
         * Legt den Wert der roomChecker-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link RoomCheckerType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setRoomChecker(RoomCheckerType value) {
            this.roomChecker = value;
        }

        /**
         * Ruft den Wert der headwayDist-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public HeadwayDistributionType getHeadwayDist() {
            return headwayDist;
        }

        /**
         * Legt den Wert der headwayDist-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setHeadwayDist(HeadwayDistributionType value) {
            this.headwayDist = value;
        }

        /**
         * Ruft den Wert der markov-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link OdOptions.OdOptionsItem.Markov }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public OdOptions.OdOptionsItem.Markov getMarkov() {
            return markov;
        }

        /**
         * Legt den Wert der markov-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link OdOptions.OdOptionsItem.Markov }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setMarkov(OdOptions.OdOptionsItem.Markov value) {
            this.markov = value;
        }

        /**
         * Ruft den Wert der laneBiases-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link OdOptions.OdOptionsItem.LaneBiases }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public OdOptions.OdOptionsItem.LaneBiases getLaneBiases() {
            return laneBiases;
        }

        /**
         * Legt den Wert der laneBiases-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link OdOptions.OdOptionsItem.LaneBiases }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setLaneBiases(OdOptions.OdOptionsItem.LaneBiases value) {
            this.laneBiases = value;
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
         *         &lt;element name="Id" type="{http://www.opentrafficsim.org/ots}string"/&gt;
         *         &lt;element name="ModelIdReferral" type="{http://www.opentrafficsim.org/ots}string"/&gt;
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
            "id",
            "modelIdReferral"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class DefaultModel
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlElement(name = "Id", type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;
            @XmlElement(name = "ModelIdReferral", type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType modelIdReferral;

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
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
            public void setId(StringType value) {
                this.id = value;
            }

            /**
             * Ruft den Wert der modelIdReferral-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getModelIdReferral() {
                return modelIdReferral;
            }

            /**
             * Legt den Wert der modelIdReferral-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setModelIdReferral(StringType value) {
                this.modelIdReferral = value;
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
         *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
         *         &lt;element ref="{http://www.opentrafficsim.org/ots}LaneBias"/&gt;
         *         &lt;element name="DefinedLaneBias"&gt;
         *           &lt;complexType&gt;
         *             &lt;complexContent&gt;
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *                 &lt;attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *               &lt;/restriction&gt;
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
            "laneBias",
            "definedLaneBias"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class LaneBiases
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlElement(name = "LaneBias")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected List<LaneBias> laneBias;
            @XmlElement(name = "DefinedLaneBias")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected List<OdOptions.OdOptionsItem.LaneBiases.DefinedLaneBias> definedLaneBias;

            /**
             * Gets the value of the laneBias property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the laneBias property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getLaneBias().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link LaneBias }
             * 
             * 
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public List<LaneBias> getLaneBias() {
                if (laneBias == null) {
                    laneBias = new ArrayList<LaneBias>();
                }
                return this.laneBias;
            }

            /**
             * Gets the value of the definedLaneBias property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the definedLaneBias property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getDefinedLaneBias().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link OdOptions.OdOptionsItem.LaneBiases.DefinedLaneBias }
             * 
             * 
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public List<OdOptions.OdOptionsItem.LaneBiases.DefinedLaneBias> getDefinedLaneBias() {
                if (definedLaneBias == null) {
                    definedLaneBias = new ArrayList<OdOptions.OdOptionsItem.LaneBiases.DefinedLaneBias>();
                }
                return this.definedLaneBias;
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
             *       &lt;attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
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
            public static class DefinedLaneBias
                implements Serializable
            {

                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                private final static long serialVersionUID = 10102L;
                @XmlAttribute(name = "GtuType", required = true)
                @XmlJavaTypeAdapter(StringAdapter.class)
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected StringType gtuType;

                /**
                 * Ruft den Wert der gtuType-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public StringType getGtuType() {
                    return gtuType;
                }

                /**
                 * Legt den Wert der gtuType-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setGtuType(StringType value) {
                    this.gtuType = value;
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
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;sequence&gt;
         *         &lt;element name="State" maxOccurs="unbounded"&gt;
         *           &lt;complexType&gt;
         *             &lt;complexContent&gt;
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *                 &lt;attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *                 &lt;attribute name="Parent" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *                 &lt;attribute name="Correlation" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
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
            "state"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Markov
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlElement(name = "State", required = true)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected List<OdOptions.OdOptionsItem.Markov.State> state;

            /**
             * Gets the value of the state property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the state property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getState().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link OdOptions.OdOptionsItem.Markov.State }
             * 
             * 
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public List<OdOptions.OdOptionsItem.Markov.State> getState() {
                if (state == null) {
                    state = new ArrayList<OdOptions.OdOptionsItem.Markov.State>();
                }
                return this.state;
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
             *       &lt;attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
             *       &lt;attribute name="Parent" type="{http://www.opentrafficsim.org/ots}string" /&gt;
             *       &lt;attribute name="Correlation" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
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
            public static class State
                implements Serializable
            {

                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                private final static long serialVersionUID = 10102L;
                @XmlAttribute(name = "GtuType", required = true)
                @XmlJavaTypeAdapter(StringAdapter.class)
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected StringType gtuType;
                @XmlAttribute(name = "Parent")
                @XmlJavaTypeAdapter(StringAdapter.class)
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected StringType parent;
                @XmlAttribute(name = "Correlation", required = true)
                @XmlJavaTypeAdapter(DoubleAdapter.class)
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected DoubleType correlation;

                /**
                 * Ruft den Wert der gtuType-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public StringType getGtuType() {
                    return gtuType;
                }

                /**
                 * Legt den Wert der gtuType-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setGtuType(StringType value) {
                    this.gtuType = value;
                }

                /**
                 * Ruft den Wert der parent-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public StringType getParent() {
                    return parent;
                }

                /**
                 * Legt den Wert der parent-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setParent(StringType value) {
                    this.parent = value;
                }

                /**
                 * Ruft den Wert der correlation-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public DoubleType getCorrelation() {
                    return correlation;
                }

                /**
                 * Legt den Wert der correlation-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setCorrelation(DoubleType value) {
                    this.correlation = value;
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
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *       &lt;choice&gt;
         *         &lt;element name="Id" type="{http://www.opentrafficsim.org/ots}string"/&gt;
         *         &lt;element name="ModelIdReferral" type="{http://www.opentrafficsim.org/ots}string"/&gt;
         *       &lt;/choice&gt;
         *       &lt;attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
         *     &lt;/restriction&gt;
         *   &lt;/complexContent&gt;
         * &lt;/complexType&gt;
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "id",
            "modelIdReferral"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Model
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlElement(name = "Id", type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType id;
            @XmlElement(name = "ModelIdReferral", type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType modelIdReferral;
            @XmlAttribute(name = "GtuType", required = true)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType gtuType;

            /**
             * Ruft den Wert der id-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getId() {
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
            public void setId(StringType value) {
                this.id = value;
            }

            /**
             * Ruft den Wert der modelIdReferral-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getModelIdReferral() {
                return modelIdReferral;
            }

            /**
             * Legt den Wert der modelIdReferral-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setModelIdReferral(StringType value) {
                this.modelIdReferral = value;
            }

            /**
             * Ruft den Wert der gtuType-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getGtuType() {
                return gtuType;
            }

            /**
             * Legt den Wert der gtuType-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setGtuType(StringType value) {
                this.gtuType = value;
            }

        }

    }

}
