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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.AnticipationAdapter;
import org.opentrafficsim.xml.bindings.ClassAdapter;
import org.opentrafficsim.xml.bindings.EstimationAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.AnticipationType;
import org.opentrafficsim.xml.bindings.types.ClassType;
import org.opentrafficsim.xml.bindings.types.EstimationType;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für PerceptionType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="PerceptionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Categories" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;all minOccurs="0"&gt;
 *                   &lt;element name="Ego" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                   &lt;element name="Infrastructure" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                   &lt;element name="Neighbors" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                   &lt;element name="Intersection" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                   &lt;element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                   &lt;element name="Traffic" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                 &lt;/all&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="HeadwayGtuType" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="Wrap" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
 *                   &lt;element name="Perceived"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="Estimation"&gt;
 *                               &lt;simpleType&gt;
 *                                 &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
 *                                   &lt;simpleType&gt;
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                       &lt;enumeration value="NONE"/&gt;
 *                                       &lt;enumeration value="UNDERESTIMATION"/&gt;
 *                                       &lt;enumeration value="OVERESTIMATION"/&gt;
 *                                     &lt;/restriction&gt;
 *                                   &lt;/simpleType&gt;
 *                                 &lt;/union&gt;
 *                               &lt;/simpleType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="Anticipation"&gt;
 *                               &lt;simpleType&gt;
 *                                 &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
 *                                   &lt;simpleType&gt;
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                       &lt;enumeration value="NONE"/&gt;
 *                                       &lt;enumeration value="CONSTANT_SPEED"/&gt;
 *                                       &lt;enumeration value="CONSTANT_ACCELERATION"/&gt;
 *                                     &lt;/restriction&gt;
 *                                   &lt;/simpleType&gt;
 *                                 &lt;/union&gt;
 *                               &lt;/simpleType&gt;
 *                             &lt;/element&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/choice&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Mental" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="Fuller"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="Task" type="{http://www.opentrafficsim.org/ots}ClassNameType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                             &lt;element name="BehavioralAdaptations" minOccurs="0"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;complexContent&gt;
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                     &lt;all minOccurs="0"&gt;
 *                                       &lt;element name="SituationalAwareness" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                       &lt;element name="Headway" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                       &lt;element name="Speed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
 *                                     &lt;/all&gt;
 *                                   &lt;/restriction&gt;
 *                                 &lt;/complexContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                             &lt;element name="TaskManager" minOccurs="0"&gt;
 *                               &lt;simpleType&gt;
 *                                 &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
 *                                   &lt;simpleType&gt;
 *                                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                                       &lt;enumeration value="SUMMATIVE"/&gt;
 *                                       &lt;enumeration value="ANTICIPATION_RELIANCE"/&gt;
 *                                     &lt;/restriction&gt;
 *                                   &lt;/simpleType&gt;
 *                                 &lt;/union&gt;
 *                               &lt;/simpleType&gt;
 *                             &lt;/element&gt;
 *                           &lt;/sequence&gt;
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
@XmlType(name = "PerceptionType", propOrder = {
    "categories",
    "headwayGtuType",
    "mental"
})
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class PerceptionType
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "Categories")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected PerceptionType.Categories categories;
    @XmlElement(name = "HeadwayGtuType")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected PerceptionType.HeadwayGtuType headwayGtuType;
    @XmlElement(name = "Mental")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected PerceptionType.Mental mental;

    /**
     * Ruft den Wert der categories-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PerceptionType.Categories }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public PerceptionType.Categories getCategories() {
        return categories;
    }

    /**
     * Legt den Wert der categories-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PerceptionType.Categories }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setCategories(PerceptionType.Categories value) {
        this.categories = value;
    }

    /**
     * Ruft den Wert der headwayGtuType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PerceptionType.HeadwayGtuType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public PerceptionType.HeadwayGtuType getHeadwayGtuType() {
        return headwayGtuType;
    }

    /**
     * Legt den Wert der headwayGtuType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PerceptionType.HeadwayGtuType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setHeadwayGtuType(PerceptionType.HeadwayGtuType value) {
        this.headwayGtuType = value;
    }

    /**
     * Ruft den Wert der mental-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link PerceptionType.Mental }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public PerceptionType.Mental getMental() {
        return mental;
    }

    /**
     * Legt den Wert der mental-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link PerceptionType.Mental }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setMental(PerceptionType.Mental value) {
        this.mental = value;
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
     *       &lt;all minOccurs="0"&gt;
     *         &lt;element name="Ego" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *         &lt;element name="Infrastructure" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *         &lt;element name="Neighbors" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *         &lt;element name="Intersection" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *         &lt;element name="BusStop" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *         &lt;element name="Traffic" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *       &lt;/all&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {

    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class Categories
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "Ego")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected EmptyType ego;
        @XmlElement(name = "Infrastructure")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected EmptyType infrastructure;
        @XmlElement(name = "Neighbors")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected EmptyType neighbors;
        @XmlElement(name = "Intersection")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected EmptyType intersection;
        @XmlElement(name = "BusStop")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected EmptyType busStop;
        @XmlElement(name = "Traffic")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected EmptyType traffic;

        /**
         * Ruft den Wert der ego-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public EmptyType getEgo() {
            return ego;
        }

        /**
         * Legt den Wert der ego-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setEgo(EmptyType value) {
            this.ego = value;
        }

        /**
         * Ruft den Wert der infrastructure-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public EmptyType getInfrastructure() {
            return infrastructure;
        }

        /**
         * Legt den Wert der infrastructure-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setInfrastructure(EmptyType value) {
            this.infrastructure = value;
        }

        /**
         * Ruft den Wert der neighbors-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public EmptyType getNeighbors() {
            return neighbors;
        }

        /**
         * Legt den Wert der neighbors-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setNeighbors(EmptyType value) {
            this.neighbors = value;
        }

        /**
         * Ruft den Wert der intersection-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public EmptyType getIntersection() {
            return intersection;
        }

        /**
         * Legt den Wert der intersection-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setIntersection(EmptyType value) {
            this.intersection = value;
        }

        /**
         * Ruft den Wert der busStop-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public EmptyType getBusStop() {
            return busStop;
        }

        /**
         * Legt den Wert der busStop-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setBusStop(EmptyType value) {
            this.busStop = value;
        }

        /**
         * Ruft den Wert der traffic-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public EmptyType getTraffic() {
            return traffic;
        }

        /**
         * Legt den Wert der traffic-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setTraffic(EmptyType value) {
            this.traffic = value;
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
     *         &lt;element name="Wrap" type="{http://www.opentrafficsim.org/ots}EmptyType"/&gt;
     *         &lt;element name="Perceived"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="Estimation"&gt;
     *                     &lt;simpleType&gt;
     *                       &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
     *                         &lt;simpleType&gt;
     *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                             &lt;enumeration value="NONE"/&gt;
     *                             &lt;enumeration value="UNDERESTIMATION"/&gt;
     *                             &lt;enumeration value="OVERESTIMATION"/&gt;
     *                           &lt;/restriction&gt;
     *                         &lt;/simpleType&gt;
     *                       &lt;/union&gt;
     *                     &lt;/simpleType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="Anticipation"&gt;
     *                     &lt;simpleType&gt;
     *                       &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
     *                         &lt;simpleType&gt;
     *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                             &lt;enumeration value="NONE"/&gt;
     *                             &lt;enumeration value="CONSTANT_SPEED"/&gt;
     *                             &lt;enumeration value="CONSTANT_ACCELERATION"/&gt;
     *                           &lt;/restriction&gt;
     *                         &lt;/simpleType&gt;
     *                       &lt;/union&gt;
     *                     &lt;/simpleType&gt;
     *                   &lt;/element&gt;
     *                 &lt;/sequence&gt;
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
        "wrap",
        "perceived"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class HeadwayGtuType
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "Wrap")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected EmptyType wrap;
        @XmlElement(name = "Perceived")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected PerceptionType.HeadwayGtuType.Perceived perceived;

        /**
         * Ruft den Wert der wrap-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public EmptyType getWrap() {
            return wrap;
        }

        /**
         * Legt den Wert der wrap-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link EmptyType }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setWrap(EmptyType value) {
            this.wrap = value;
        }

        /**
         * Ruft den Wert der perceived-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link PerceptionType.HeadwayGtuType.Perceived }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public PerceptionType.HeadwayGtuType.Perceived getPerceived() {
            return perceived;
        }

        /**
         * Legt den Wert der perceived-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link PerceptionType.HeadwayGtuType.Perceived }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setPerceived(PerceptionType.HeadwayGtuType.Perceived value) {
            this.perceived = value;
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
         *         &lt;element name="Estimation"&gt;
         *           &lt;simpleType&gt;
         *             &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
         *               &lt;simpleType&gt;
         *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
         *                   &lt;enumeration value="NONE"/&gt;
         *                   &lt;enumeration value="UNDERESTIMATION"/&gt;
         *                   &lt;enumeration value="OVERESTIMATION"/&gt;
         *                 &lt;/restriction&gt;
         *               &lt;/simpleType&gt;
         *             &lt;/union&gt;
         *           &lt;/simpleType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="Anticipation"&gt;
         *           &lt;simpleType&gt;
         *             &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
         *               &lt;simpleType&gt;
         *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
         *                   &lt;enumeration value="NONE"/&gt;
         *                   &lt;enumeration value="CONSTANT_SPEED"/&gt;
         *                   &lt;enumeration value="CONSTANT_ACCELERATION"/&gt;
         *                 &lt;/restriction&gt;
         *               &lt;/simpleType&gt;
         *             &lt;/union&gt;
         *           &lt;/simpleType&gt;
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
            "estimation",
            "anticipation"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Perceived
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlElement(name = "Estimation", required = true, type = String.class)
            @XmlJavaTypeAdapter(EstimationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected EstimationType estimation;
            @XmlElement(name = "Anticipation", required = true, type = String.class)
            @XmlJavaTypeAdapter(AnticipationAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected AnticipationType anticipation;

            /**
             * Ruft den Wert der estimation-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public EstimationType getEstimation() {
                return estimation;
            }

            /**
             * Legt den Wert der estimation-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setEstimation(EstimationType value) {
                this.estimation = value;
            }

            /**
             * Ruft den Wert der anticipation-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public AnticipationType getAnticipation() {
                return anticipation;
            }

            /**
             * Legt den Wert der anticipation-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setAnticipation(AnticipationType value) {
                this.anticipation = value;
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
     *         &lt;element name="Fuller"&gt;
     *           &lt;complexType&gt;
     *             &lt;complexContent&gt;
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                 &lt;sequence&gt;
     *                   &lt;element name="Task" type="{http://www.opentrafficsim.org/ots}ClassNameType" maxOccurs="unbounded" minOccurs="0"/&gt;
     *                   &lt;element name="BehavioralAdaptations" minOccurs="0"&gt;
     *                     &lt;complexType&gt;
     *                       &lt;complexContent&gt;
     *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *                           &lt;all minOccurs="0"&gt;
     *                             &lt;element name="SituationalAwareness" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                             &lt;element name="Headway" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                             &lt;element name="Speed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
     *                           &lt;/all&gt;
     *                         &lt;/restriction&gt;
     *                       &lt;/complexContent&gt;
     *                     &lt;/complexType&gt;
     *                   &lt;/element&gt;
     *                   &lt;element name="TaskManager" minOccurs="0"&gt;
     *                     &lt;simpleType&gt;
     *                       &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
     *                         &lt;simpleType&gt;
     *                           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
     *                             &lt;enumeration value="SUMMATIVE"/&gt;
     *                             &lt;enumeration value="ANTICIPATION_RELIANCE"/&gt;
     *                           &lt;/restriction&gt;
     *                         &lt;/simpleType&gt;
     *                       &lt;/union&gt;
     *                     &lt;/simpleType&gt;
     *                   &lt;/element&gt;
     *                 &lt;/sequence&gt;
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
        "fuller"
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public static class Mental
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlElement(name = "Fuller")
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected PerceptionType.Mental.Fuller fuller;

        /**
         * Ruft den Wert der fuller-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link PerceptionType.Mental.Fuller }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public PerceptionType.Mental.Fuller getFuller() {
            return fuller;
        }

        /**
         * Legt den Wert der fuller-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link PerceptionType.Mental.Fuller }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setFuller(PerceptionType.Mental.Fuller value) {
            this.fuller = value;
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
         *         &lt;element name="Task" type="{http://www.opentrafficsim.org/ots}ClassNameType" maxOccurs="unbounded" minOccurs="0"/&gt;
         *         &lt;element name="BehavioralAdaptations" minOccurs="0"&gt;
         *           &lt;complexType&gt;
         *             &lt;complexContent&gt;
         *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
         *                 &lt;all minOccurs="0"&gt;
         *                   &lt;element name="SituationalAwareness" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                   &lt;element name="Headway" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                   &lt;element name="Speed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
         *                 &lt;/all&gt;
         *               &lt;/restriction&gt;
         *             &lt;/complexContent&gt;
         *           &lt;/complexType&gt;
         *         &lt;/element&gt;
         *         &lt;element name="TaskManager" minOccurs="0"&gt;
         *           &lt;simpleType&gt;
         *             &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
         *               &lt;simpleType&gt;
         *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
         *                   &lt;enumeration value="SUMMATIVE"/&gt;
         *                   &lt;enumeration value="ANTICIPATION_RELIANCE"/&gt;
         *                 &lt;/restriction&gt;
         *               &lt;/simpleType&gt;
         *             &lt;/union&gt;
         *           &lt;/simpleType&gt;
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
            "task",
            "behavioralAdaptations",
            "taskManager"
        })
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public static class Fuller
            implements Serializable
        {

            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            private final static long serialVersionUID = 10102L;
            @XmlElement(name = "Task", type = String.class)
            @XmlJavaTypeAdapter(ClassAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected List<ClassType> task;
            @XmlElement(name = "BehavioralAdaptations")
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected PerceptionType.Mental.Fuller.BehavioralAdaptations behavioralAdaptations;
            @XmlElement(name = "TaskManager", type = String.class)
            @XmlJavaTypeAdapter(StringAdapter.class)
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            protected StringType taskManager;

            /**
             * Gets the value of the task property.
             * 
             * <p>
             * This accessor method returns a reference to the live list,
             * not a snapshot. Therefore any modification you make to the
             * returned list will be present inside the JAXB object.
             * This is why there is not a <CODE>set</CODE> method for the task property.
             * 
             * <p>
             * For example, to add a new item, do as follows:
             * <pre>
             *    getTask().add(newItem);
             * </pre>
             * 
             * 
             * <p>
             * Objects of the following type(s) are allowed in the list
             * {@link String }
             * 
             * 
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public List<ClassType> getTask() {
                if (task == null) {
                    task = new ArrayList<ClassType>();
                }
                return this.task;
            }

            /**
             * Ruft den Wert der behavioralAdaptations-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link PerceptionType.Mental.Fuller.BehavioralAdaptations }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public PerceptionType.Mental.Fuller.BehavioralAdaptations getBehavioralAdaptations() {
                return behavioralAdaptations;
            }

            /**
             * Legt den Wert der behavioralAdaptations-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link PerceptionType.Mental.Fuller.BehavioralAdaptations }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setBehavioralAdaptations(PerceptionType.Mental.Fuller.BehavioralAdaptations value) {
                this.behavioralAdaptations = value;
            }

            /**
             * Ruft den Wert der taskManager-Eigenschaft ab.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public StringType getTaskManager() {
                return taskManager;
            }

            /**
             * Legt den Wert der taskManager-Eigenschaft fest.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public void setTaskManager(StringType value) {
                this.taskManager = value;
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
             *       &lt;all minOccurs="0"&gt;
             *         &lt;element name="SituationalAwareness" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *         &lt;element name="Headway" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *         &lt;element name="Speed" type="{http://www.opentrafficsim.org/ots}EmptyType" minOccurs="0"/&gt;
             *       &lt;/all&gt;
             *     &lt;/restriction&gt;
             *   &lt;/complexContent&gt;
             * &lt;/complexType&gt;
             * </pre>
             * 
             * 
             */
            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "", propOrder = {

            })
            @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
            public static class BehavioralAdaptations
                implements Serializable
            {

                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                private final static long serialVersionUID = 10102L;
                @XmlElement(name = "SituationalAwareness")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType situationalAwareness;
                @XmlElement(name = "Headway")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType headway;
                @XmlElement(name = "Speed")
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                protected EmptyType speed;

                /**
                 * Ruft den Wert der situationalAwareness-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getSituationalAwareness() {
                    return situationalAwareness;
                }

                /**
                 * Legt den Wert der situationalAwareness-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setSituationalAwareness(EmptyType value) {
                    this.situationalAwareness = value;
                }

                /**
                 * Ruft den Wert der headway-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getHeadway() {
                    return headway;
                }

                /**
                 * Legt den Wert der headway-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setHeadway(EmptyType value) {
                    this.headway = value;
                }

                /**
                 * Ruft den Wert der speed-Eigenschaft ab.
                 * 
                 * @return
                 *     possible object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public EmptyType getSpeed() {
                    return speed;
                }

                /**
                 * Legt den Wert der speed-Eigenschaft fest.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link EmptyType }
                 *     
                 */
                @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
                public void setSpeed(EmptyType value) {
                    this.speed = value;
                }

            }

        }

    }

}
