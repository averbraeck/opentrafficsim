//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2022.02.24 at 11:10:08 PM CET 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.math.BigInteger;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.FractionAdapter;


/**
 * <p>Java class for DISCRETEDISTTYPE complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DISCRETEDISTTYPE"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="CONSTANT"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="C" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
 *                 &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="BERNOULLI"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FRACTIONTYPE" /&gt;
 *                 &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="BINOMIAL"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="N" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
 *                 &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FRACTIONTYPE" /&gt;
 *                 &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="UNIFORM"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="MIN" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *                 &lt;attribute name="MAX" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *                 &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="GEOMETRIC"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FRACTIONTYPE" /&gt;
 *                 &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="NEGBINOMIAL"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="N" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
 *                 &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FRACTIONTYPE" /&gt;
 *                 &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="POISSON"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;attribute name="LAMBDA" use="required" type="{http://www.opentrafficsim.org/ots}DOUBLEPOSITIVE" /&gt;
 *                 &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
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
@XmlType(name = "DISCRETEDISTTYPE", propOrder = {
    "constant",
    "bernoulli",
    "binomial",
    "uniform",
    "geometric",
    "negbinomial",
    "poisson"
})
@XmlSeeAlso({
    PARAMETERLONGDIST.class,
    PARAMETERINTEGERDIST.class
})
@Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
public class DISCRETEDISTTYPE implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "CONSTANT")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    protected DISCRETEDISTTYPE.CONSTANT constant;
    @XmlElement(name = "BERNOULLI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    protected DISCRETEDISTTYPE.BERNOULLI bernoulli;
    @XmlElement(name = "BINOMIAL")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    protected DISCRETEDISTTYPE.BINOMIAL binomial;
    @XmlElement(name = "UNIFORM")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    protected DISCRETEDISTTYPE.UNIFORM uniform;
    @XmlElement(name = "GEOMETRIC")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    protected DISCRETEDISTTYPE.GEOMETRIC geometric;
    @XmlElement(name = "NEGBINOMIAL")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    protected DISCRETEDISTTYPE.NEGBINOMIAL negbinomial;
    @XmlElement(name = "POISSON")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    protected DISCRETEDISTTYPE.POISSON poisson;

    /**
     * Gets the value of the constant property.
     * 
     * @return
     *     possible object is
     *     {@link DISCRETEDISTTYPE.CONSTANT }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public DISCRETEDISTTYPE.CONSTANT getCONSTANT() {
        return constant;
    }

    /**
     * Sets the value of the constant property.
     * 
     * @param value
     *     allowed object is
     *     {@link DISCRETEDISTTYPE.CONSTANT }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public void setCONSTANT(DISCRETEDISTTYPE.CONSTANT value) {
        this.constant = value;
    }

    /**
     * Gets the value of the bernoulli property.
     * 
     * @return
     *     possible object is
     *     {@link DISCRETEDISTTYPE.BERNOULLI }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public DISCRETEDISTTYPE.BERNOULLI getBERNOULLI() {
        return bernoulli;
    }

    /**
     * Sets the value of the bernoulli property.
     * 
     * @param value
     *     allowed object is
     *     {@link DISCRETEDISTTYPE.BERNOULLI }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public void setBERNOULLI(DISCRETEDISTTYPE.BERNOULLI value) {
        this.bernoulli = value;
    }

    /**
     * Gets the value of the binomial property.
     * 
     * @return
     *     possible object is
     *     {@link DISCRETEDISTTYPE.BINOMIAL }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public DISCRETEDISTTYPE.BINOMIAL getBINOMIAL() {
        return binomial;
    }

    /**
     * Sets the value of the binomial property.
     * 
     * @param value
     *     allowed object is
     *     {@link DISCRETEDISTTYPE.BINOMIAL }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public void setBINOMIAL(DISCRETEDISTTYPE.BINOMIAL value) {
        this.binomial = value;
    }

    /**
     * Gets the value of the uniform property.
     * 
     * @return
     *     possible object is
     *     {@link DISCRETEDISTTYPE.UNIFORM }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public DISCRETEDISTTYPE.UNIFORM getUNIFORM() {
        return uniform;
    }

    /**
     * Sets the value of the uniform property.
     * 
     * @param value
     *     allowed object is
     *     {@link DISCRETEDISTTYPE.UNIFORM }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public void setUNIFORM(DISCRETEDISTTYPE.UNIFORM value) {
        this.uniform = value;
    }

    /**
     * Gets the value of the geometric property.
     * 
     * @return
     *     possible object is
     *     {@link DISCRETEDISTTYPE.GEOMETRIC }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public DISCRETEDISTTYPE.GEOMETRIC getGEOMETRIC() {
        return geometric;
    }

    /**
     * Sets the value of the geometric property.
     * 
     * @param value
     *     allowed object is
     *     {@link DISCRETEDISTTYPE.GEOMETRIC }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public void setGEOMETRIC(DISCRETEDISTTYPE.GEOMETRIC value) {
        this.geometric = value;
    }

    /**
     * Gets the value of the negbinomial property.
     * 
     * @return
     *     possible object is
     *     {@link DISCRETEDISTTYPE.NEGBINOMIAL }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public DISCRETEDISTTYPE.NEGBINOMIAL getNEGBINOMIAL() {
        return negbinomial;
    }

    /**
     * Sets the value of the negbinomial property.
     * 
     * @param value
     *     allowed object is
     *     {@link DISCRETEDISTTYPE.NEGBINOMIAL }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public void setNEGBINOMIAL(DISCRETEDISTTYPE.NEGBINOMIAL value) {
        this.negbinomial = value;
    }

    /**
     * Gets the value of the poisson property.
     * 
     * @return
     *     possible object is
     *     {@link DISCRETEDISTTYPE.POISSON }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public DISCRETEDISTTYPE.POISSON getPOISSON() {
        return poisson;
    }

    /**
     * Sets the value of the poisson property.
     * 
     * @param value
     *     allowed object is
     *     {@link DISCRETEDISTTYPE.POISSON }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public void setPOISSON(DISCRETEDISTTYPE.POISSON value) {
        this.poisson = value;
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
     *       &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FRACTIONTYPE" /&gt;
     *       &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public static class BERNOULLI
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "P", required = true)
        @XmlJavaTypeAdapter(FractionAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected Double p;
        @XmlAttribute(name = "RANDOMSTREAM")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected String randomstream;

        /**
         * Gets the value of the p property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public Double getP() {
            return p;
        }

        /**
         * Sets the value of the p property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setP(Double value) {
            this.p = value;
        }

        /**
         * Gets the value of the randomstream property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public String getRANDOMSTREAM() {
            if (randomstream == null) {
                return "default";
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setRANDOMSTREAM(String value) {
            this.randomstream = value;
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
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="N" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
     *       &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FRACTIONTYPE" /&gt;
     *       &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public static class BINOMIAL
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "N", required = true)
        @XmlSchemaType(name = "positiveInteger")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected BigInteger n;
        @XmlAttribute(name = "P", required = true)
        @XmlJavaTypeAdapter(FractionAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected Double p;
        @XmlAttribute(name = "RANDOMSTREAM")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected String randomstream;

        /**
         * Gets the value of the n property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public BigInteger getN() {
            return n;
        }

        /**
         * Sets the value of the n property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setN(BigInteger value) {
            this.n = value;
        }

        /**
         * Gets the value of the p property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public Double getP() {
            return p;
        }

        /**
         * Sets the value of the p property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setP(Double value) {
            this.p = value;
        }

        /**
         * Gets the value of the randomstream property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public String getRANDOMSTREAM() {
            if (randomstream == null) {
                return "default";
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setRANDOMSTREAM(String value) {
            this.randomstream = value;
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
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="C" use="required" type="{http://www.w3.org/2001/XMLSchema}long" /&gt;
     *       &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public static class CONSTANT
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "C", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected long c;
        @XmlAttribute(name = "RANDOMSTREAM")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected String randomstream;

        /**
         * Gets the value of the c property.
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public long getC() {
            return c;
        }

        /**
         * Sets the value of the c property.
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setC(long value) {
            this.c = value;
        }

        /**
         * Gets the value of the randomstream property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public String getRANDOMSTREAM() {
            if (randomstream == null) {
                return "default";
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setRANDOMSTREAM(String value) {
            this.randomstream = value;
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
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FRACTIONTYPE" /&gt;
     *       &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public static class GEOMETRIC
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "P", required = true)
        @XmlJavaTypeAdapter(FractionAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected Double p;
        @XmlAttribute(name = "RANDOMSTREAM")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected String randomstream;

        /**
         * Gets the value of the p property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public Double getP() {
            return p;
        }

        /**
         * Sets the value of the p property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setP(Double value) {
            this.p = value;
        }

        /**
         * Gets the value of the randomstream property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public String getRANDOMSTREAM() {
            if (randomstream == null) {
                return "default";
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setRANDOMSTREAM(String value) {
            this.randomstream = value;
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
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="N" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
     *       &lt;attribute name="P" use="required" type="{http://www.opentrafficsim.org/ots}FRACTIONTYPE" /&gt;
     *       &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public static class NEGBINOMIAL
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "N", required = true)
        @XmlSchemaType(name = "positiveInteger")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected BigInteger n;
        @XmlAttribute(name = "P", required = true)
        @XmlJavaTypeAdapter(FractionAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected Double p;
        @XmlAttribute(name = "RANDOMSTREAM")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected String randomstream;

        /**
         * Gets the value of the n property.
         * 
         * @return
         *     possible object is
         *     {@link BigInteger }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public BigInteger getN() {
            return n;
        }

        /**
         * Sets the value of the n property.
         * 
         * @param value
         *     allowed object is
         *     {@link BigInteger }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setN(BigInteger value) {
            this.n = value;
        }

        /**
         * Gets the value of the p property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public Double getP() {
            return p;
        }

        /**
         * Sets the value of the p property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setP(Double value) {
            this.p = value;
        }

        /**
         * Gets the value of the randomstream property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public String getRANDOMSTREAM() {
            if (randomstream == null) {
                return "default";
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setRANDOMSTREAM(String value) {
            this.randomstream = value;
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
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="LAMBDA" use="required" type="{http://www.opentrafficsim.org/ots}DOUBLEPOSITIVE" /&gt;
     *       &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public static class POISSON
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "LAMBDA", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected double lambda;
        @XmlAttribute(name = "RANDOMSTREAM")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected String randomstream;

        /**
         * Gets the value of the lambda property.
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public double getLAMBDA() {
            return lambda;
        }

        /**
         * Sets the value of the lambda property.
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setLAMBDA(double value) {
            this.lambda = value;
        }

        /**
         * Gets the value of the randomstream property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public String getRANDOMSTREAM() {
            if (randomstream == null) {
                return "default";
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setRANDOMSTREAM(String value) {
            this.randomstream = value;
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
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;attribute name="MIN" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
     *       &lt;attribute name="MAX" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
     *       &lt;attribute name="RANDOMSTREAM" type="{http://www.w3.org/2001/XMLSchema}string" default="default" /&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
    public static class UNIFORM
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "MIN", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected int min;
        @XmlAttribute(name = "MAX", required = true)
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected int max;
        @XmlAttribute(name = "RANDOMSTREAM")
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        protected String randomstream;

        /**
         * Gets the value of the min property.
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public int getMIN() {
            return min;
        }

        /**
         * Sets the value of the min property.
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setMIN(int value) {
            this.min = value;
        }

        /**
         * Gets the value of the max property.
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public int getMAX() {
            return max;
        }

        /**
         * Sets the value of the max property.
         * 
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setMAX(int value) {
            this.max = value;
        }

        /**
         * Gets the value of the randomstream property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public String getRANDOMSTREAM() {
            if (randomstream == null) {
                return "default";
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
        @Generated(value = "com.sun.tools.xjc.Driver", date = "2022-02-24T11:10:08+01:00", comments = "JAXB RI v2.3.0")
        public void setRANDOMSTREAM(String value) {
            this.randomstream = value;
        }

    }

}
