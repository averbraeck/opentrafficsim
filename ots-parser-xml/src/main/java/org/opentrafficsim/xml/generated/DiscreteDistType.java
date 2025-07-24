//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v2.3.7 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2025.07.24 um 01:18:43 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DoublePositiveAdapter;
import org.opentrafficsim.xml.bindings.FractionAdapter;
import org.opentrafficsim.xml.bindings.IntegerAdapter;
import org.opentrafficsim.xml.bindings.LongAdapter;
import org.opentrafficsim.xml.bindings.PositiveIntegerAdapter;
import org.opentrafficsim.xml.bindings.types.LongType;


/**
 * <p>Java-Klasse für DiscreteDistType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="DiscreteDistType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="Constant"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="C" use="required" type="{http://www.opentrafficsim.org/ots}long" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="BernoulliI"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Binomial"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="N" use="required" type="{http://www.opentrafficsim.org/ots}positiveInteger" /&gt;
 *                   &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Uniform"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}int" /&gt;
 *                   &lt;attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}int" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Geometric"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="NegBinomial"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="N" use="required" type="{http://www.opentrafficsim.org/ots}positiveInteger" /&gt;
 *                   &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Poisson"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Lambda" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="RandomStream" type="{http://www.opentrafficsim.org/ots}RandomStreamSource" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DiscreteDistType", propOrder = {
    "constant",
    "bernoulliI",
    "binomial",
    "uniform",
    "geometric",
    "negBinomial",
    "poisson",
    "randomStream"
})
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.ModelType.ModelParameters.IntegerDist.class
})
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class DiscreteDistType implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "Constant")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected DiscreteDistType.Constant constant;
    @XmlElement(name = "BernoulliI")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected DiscreteDistType.BernoulliI bernoulliI;
    @XmlElement(name = "Binomial")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected DiscreteDistType.Binomial binomial;
    @XmlElement(name = "Uniform")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected DiscreteDistType.Uniform uniform;
    @XmlElement(name = "Geometric")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected DiscreteDistType.Geometric geometric;
    @XmlElement(name = "NegBinomial")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected DiscreteDistType.NegBinomial negBinomial;
    @XmlElement(name = "Poisson")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected DiscreteDistType.Poisson poisson;
    @XmlElement(name = "RandomStream")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected RandomStreamSource randomStream;

    /**
     * Ruft den Wert der constant-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.Constant }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public DiscreteDistType.Constant getConstant() {
        return constant;
    }

    /**
     * Legt den Wert der constant-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.Constant }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setConstant(DiscreteDistType.Constant value) {
        this.constant = value;
    }

    /**
     * Ruft den Wert der bernoulliI-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.BernoulliI }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public DiscreteDistType.BernoulliI getBernoulliI() {
        return bernoulliI;
    }

    /**
     * Legt den Wert der bernoulliI-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.BernoulliI }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setBernoulliI(DiscreteDistType.BernoulliI value) {
        this.bernoulliI = value;
    }

    /**
     * Ruft den Wert der binomial-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.Binomial }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public DiscreteDistType.Binomial getBinomial() {
        return binomial;
    }

    /**
     * Legt den Wert der binomial-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.Binomial }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setBinomial(DiscreteDistType.Binomial value) {
        this.binomial = value;
    }

    /**
     * Ruft den Wert der uniform-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.Uniform }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public DiscreteDistType.Uniform getUniform() {
        return uniform;
    }

    /**
     * Legt den Wert der uniform-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.Uniform }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setUniform(DiscreteDistType.Uniform value) {
        this.uniform = value;
    }

    /**
     * Ruft den Wert der geometric-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.Geometric }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public DiscreteDistType.Geometric getGeometric() {
        return geometric;
    }

    /**
     * Legt den Wert der geometric-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.Geometric }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setGeometric(DiscreteDistType.Geometric value) {
        this.geometric = value;
    }

    /**
     * Ruft den Wert der negBinomial-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.NegBinomial }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public DiscreteDistType.NegBinomial getNegBinomial() {
        return negBinomial;
    }

    /**
     * Legt den Wert der negBinomial-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.NegBinomial }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setNegBinomial(DiscreteDistType.NegBinomial value) {
        this.negBinomial = value;
    }

    /**
     * Ruft den Wert der poisson-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link DiscreteDistType.Poisson }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public DiscreteDistType.Poisson getPoisson() {
        return poisson;
    }

    /**
     * Legt den Wert der poisson-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link DiscreteDistType.Poisson }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setPoisson(DiscreteDistType.Poisson value) {
        this.poisson = value;
    }

    /**
     * Ruft den Wert der randomStream-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link RandomStreamSource }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public RandomStreamSource getRandomStream() {
        return randomStream;
    }

    /**
     * Legt den Wert der randomStream-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link RandomStreamSource }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setRandomStream(RandomStreamSource value) {
        this.randomStream = value;
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
     *       &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" /&gt;
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
    public static class BernoulliI
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "P", required = true)
        @XmlJavaTypeAdapter(FractionAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType p;

        /**
         * Ruft den Wert der p-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getP() {
            return p;
        }

        /**
         * Legt den Wert der p-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setP(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.p = value;
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
     *       &lt;attribute name="N" use="required" type="{http://www.opentrafficsim.org/ots}positiveInteger" /&gt;
     *       &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" /&gt;
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
    public static class Binomial
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "N", required = true)
        @XmlJavaTypeAdapter(PositiveIntegerAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.IntegerType n;
        @XmlAttribute(name = "P", required = true)
        @XmlJavaTypeAdapter(FractionAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType p;

        /**
         * Ruft den Wert der n-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.IntegerType getN() {
            return n;
        }

        /**
         * Legt den Wert der n-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setN(org.opentrafficsim.xml.bindings.types.IntegerType value) {
            this.n = value;
        }

        /**
         * Ruft den Wert der p-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getP() {
            return p;
        }

        /**
         * Legt den Wert der p-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setP(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.p = value;
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
     *       &lt;attribute name="C" use="required" type="{http://www.opentrafficsim.org/ots}long" /&gt;
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
    public static class Constant
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "C", required = true)
        @XmlJavaTypeAdapter(LongAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected LongType c;

        /**
         * Ruft den Wert der c-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public LongType getC() {
            return c;
        }

        /**
         * Legt den Wert der c-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setC(LongType value) {
            this.c = value;
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
     *       &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" /&gt;
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
    public static class Geometric
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "P", required = true)
        @XmlJavaTypeAdapter(FractionAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType p;

        /**
         * Ruft den Wert der p-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getP() {
            return p;
        }

        /**
         * Legt den Wert der p-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setP(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.p = value;
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
     *       &lt;attribute name="N" use="required" type="{http://www.opentrafficsim.org/ots}positiveInteger" /&gt;
     *       &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FractionType" /&gt;
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
    public static class NegBinomial
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "N", required = true)
        @XmlJavaTypeAdapter(PositiveIntegerAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.IntegerType n;
        @XmlAttribute(name = "P", required = true)
        @XmlJavaTypeAdapter(FractionAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType p;

        /**
         * Ruft den Wert der n-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.IntegerType getN() {
            return n;
        }

        /**
         * Legt den Wert der n-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setN(org.opentrafficsim.xml.bindings.types.IntegerType value) {
            this.n = value;
        }

        /**
         * Ruft den Wert der p-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getP() {
            return p;
        }

        /**
         * Legt den Wert der p-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setP(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.p = value;
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
     *       &lt;attribute name="Lambda" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
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
    public static class Poisson
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "Lambda", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType lambda;

        /**
         * Ruft den Wert der lambda-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getLambda() {
            return lambda;
        }

        /**
         * Legt den Wert der lambda-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setLambda(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.lambda = value;
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
     *       &lt;attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}int" /&gt;
     *       &lt;attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}int" /&gt;
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
    public static class Uniform
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "Min", required = true)
        @XmlJavaTypeAdapter(IntegerAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.IntegerType min;
        @XmlAttribute(name = "Max", required = true)
        @XmlJavaTypeAdapter(IntegerAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.IntegerType max;

        /**
         * Ruft den Wert der min-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.IntegerType getMin() {
            return min;
        }

        /**
         * Legt den Wert der min-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setMin(org.opentrafficsim.xml.bindings.types.IntegerType value) {
            this.min = value;
        }

        /**
         * Ruft den Wert der max-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.IntegerType getMax() {
            return max;
        }

        /**
         * Legt den Wert der max-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setMax(org.opentrafficsim.xml.bindings.types.IntegerType value) {
            this.max = value;
        }

    }

}
