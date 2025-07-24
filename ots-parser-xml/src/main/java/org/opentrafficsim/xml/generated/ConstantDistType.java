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
import org.opentrafficsim.xml.bindings.DoubleAdapter;
import org.opentrafficsim.xml.bindings.DoublePositiveAdapter;
import org.opentrafficsim.xml.bindings.DoublePositiveInclusiveAdapter;
import org.opentrafficsim.xml.bindings.PositiveIntegerAdapter;
import org.opentrafficsim.xml.bindings.types.IntegerType;


/**
 * <p>Java-Klasse für ConstantDistType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ConstantDistType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="Constant"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="C" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Exponential"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Lambda" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Triangular"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                   &lt;attribute name="Mode" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                   &lt;attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Normal"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                   &lt;attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositiveInclusive" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="NormalTrunc"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                   &lt;attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositiveInclusive" /&gt;
 *                   &lt;attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                   &lt;attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Beta"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Alpha1" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
 *                   &lt;attribute name="Alpha2" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Erlang"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Mean" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                   &lt;attribute name="K" use="required" type="{http://www.opentrafficsim.org/ots}positiveInteger" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Gamma"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Alpha" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                   &lt;attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="LogNormal"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                   &lt;attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="LogNormalTrunc"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                   &lt;attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
 *                   &lt;attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                   &lt;attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Pearson5"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Alpha" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
 *                   &lt;attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Pearson6"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Alpha1" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
 *                   &lt;attribute name="Alpha2" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
 *                   &lt;attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Uniform"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                   &lt;attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="Weibull"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="Alpha" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
 *                   &lt;attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
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
@XmlType(name = "ConstantDistType", propOrder = {
    "constant",
    "exponential",
    "triangular",
    "normal",
    "normalTrunc",
    "beta",
    "erlang",
    "gamma",
    "logNormal",
    "logNormalTrunc",
    "pearson5",
    "pearson6",
    "uniform",
    "weibull",
    "randomStream"
})
@XmlSeeAlso({
    LengthDistType.class,
    SpeedDistType.class,
    AccelerationDistType.class,
    org.opentrafficsim.xml.generated.ModelType.ModelParameters.DoubleDist.class,
    PositionDistType.class,
    TimeDistType.class,
    DurationDistType.class,
    LinearDensityDistType.class,
    FrequencyDistType.class
})
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class ConstantDistType implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "Constant")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.Constant constant;
    @XmlElement(name = "Exponential")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.Exponential exponential;
    @XmlElement(name = "Triangular")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.Triangular triangular;
    @XmlElement(name = "Normal")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.Normal normal;
    @XmlElement(name = "NormalTrunc")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.NormalTrunc normalTrunc;
    @XmlElement(name = "Beta")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.Beta beta;
    @XmlElement(name = "Erlang")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.Erlang erlang;
    @XmlElement(name = "Gamma")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.Gamma gamma;
    @XmlElement(name = "LogNormal")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.LogNormal logNormal;
    @XmlElement(name = "LogNormalTrunc")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.LogNormalTrunc logNormalTrunc;
    @XmlElement(name = "Pearson5")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.Pearson5 pearson5;
    @XmlElement(name = "Pearson6")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.Pearson6 pearson6;
    @XmlElement(name = "Uniform")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.Uniform uniform;
    @XmlElement(name = "Weibull")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected ConstantDistType.Weibull weibull;
    @XmlElement(name = "RandomStream")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected RandomStreamSource randomStream;

    /**
     * Ruft den Wert der constant-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Constant }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.Constant getConstant() {
        return constant;
    }

    /**
     * Legt den Wert der constant-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Constant }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setConstant(ConstantDistType.Constant value) {
        this.constant = value;
    }

    /**
     * Ruft den Wert der exponential-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Exponential }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.Exponential getExponential() {
        return exponential;
    }

    /**
     * Legt den Wert der exponential-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Exponential }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setExponential(ConstantDistType.Exponential value) {
        this.exponential = value;
    }

    /**
     * Ruft den Wert der triangular-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Triangular }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.Triangular getTriangular() {
        return triangular;
    }

    /**
     * Legt den Wert der triangular-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Triangular }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setTriangular(ConstantDistType.Triangular value) {
        this.triangular = value;
    }

    /**
     * Ruft den Wert der normal-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Normal }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.Normal getNormal() {
        return normal;
    }

    /**
     * Legt den Wert der normal-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Normal }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setNormal(ConstantDistType.Normal value) {
        this.normal = value;
    }

    /**
     * Ruft den Wert der normalTrunc-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.NormalTrunc }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.NormalTrunc getNormalTrunc() {
        return normalTrunc;
    }

    /**
     * Legt den Wert der normalTrunc-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.NormalTrunc }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setNormalTrunc(ConstantDistType.NormalTrunc value) {
        this.normalTrunc = value;
    }

    /**
     * Ruft den Wert der beta-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Beta }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.Beta getBeta() {
        return beta;
    }

    /**
     * Legt den Wert der beta-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Beta }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setBeta(ConstantDistType.Beta value) {
        this.beta = value;
    }

    /**
     * Ruft den Wert der erlang-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Erlang }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.Erlang getErlang() {
        return erlang;
    }

    /**
     * Legt den Wert der erlang-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Erlang }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setErlang(ConstantDistType.Erlang value) {
        this.erlang = value;
    }

    /**
     * Ruft den Wert der gamma-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Gamma }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.Gamma getGamma() {
        return gamma;
    }

    /**
     * Legt den Wert der gamma-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Gamma }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setGamma(ConstantDistType.Gamma value) {
        this.gamma = value;
    }

    /**
     * Ruft den Wert der logNormal-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.LogNormal }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.LogNormal getLogNormal() {
        return logNormal;
    }

    /**
     * Legt den Wert der logNormal-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.LogNormal }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setLogNormal(ConstantDistType.LogNormal value) {
        this.logNormal = value;
    }

    /**
     * Ruft den Wert der logNormalTrunc-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.LogNormalTrunc }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.LogNormalTrunc getLogNormalTrunc() {
        return logNormalTrunc;
    }

    /**
     * Legt den Wert der logNormalTrunc-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.LogNormalTrunc }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setLogNormalTrunc(ConstantDistType.LogNormalTrunc value) {
        this.logNormalTrunc = value;
    }

    /**
     * Ruft den Wert der pearson5-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Pearson5 }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.Pearson5 getPearson5() {
        return pearson5;
    }

    /**
     * Legt den Wert der pearson5-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Pearson5 }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setPearson5(ConstantDistType.Pearson5 value) {
        this.pearson5 = value;
    }

    /**
     * Ruft den Wert der pearson6-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Pearson6 }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.Pearson6 getPearson6() {
        return pearson6;
    }

    /**
     * Legt den Wert der pearson6-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Pearson6 }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setPearson6(ConstantDistType.Pearson6 value) {
        this.pearson6 = value;
    }

    /**
     * Ruft den Wert der uniform-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Uniform }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.Uniform getUniform() {
        return uniform;
    }

    /**
     * Legt den Wert der uniform-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Uniform }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setUniform(ConstantDistType.Uniform value) {
        this.uniform = value;
    }

    /**
     * Ruft den Wert der weibull-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ConstantDistType.Weibull }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public ConstantDistType.Weibull getWeibull() {
        return weibull;
    }

    /**
     * Legt den Wert der weibull-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ConstantDistType.Weibull }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setWeibull(ConstantDistType.Weibull value) {
        this.weibull = value;
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
     *       &lt;attribute name="Alpha1" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
     *       &lt;attribute name="Alpha2" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
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
    public static class Beta
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "Alpha1", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha1;
        @XmlAttribute(name = "Alpha2", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha2;

        /**
         * Ruft den Wert der alpha1-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha1() {
            return alpha1;
        }

        /**
         * Legt den Wert der alpha1-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setAlpha1(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha1 = value;
        }

        /**
         * Ruft den Wert der alpha2-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha2() {
            return alpha2;
        }

        /**
         * Legt den Wert der alpha2-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setAlpha2(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha2 = value;
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
     *       &lt;attribute name="C" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
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
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType c;

        /**
         * Ruft den Wert der c-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getC() {
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
        public void setC(org.opentrafficsim.xml.bindings.types.DoubleType value) {
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
     *       &lt;attribute name="Mean" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
     *       &lt;attribute name="K" use="required" type="{http://www.opentrafficsim.org/ots}positiveInteger" /&gt;
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
    public static class Erlang
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "Mean", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType mean;
        @XmlAttribute(name = "K", required = true)
        @XmlJavaTypeAdapter(PositiveIntegerAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected IntegerType k;

        /**
         * Ruft den Wert der mean-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getMean() {
            return mean;
        }

        /**
         * Legt den Wert der mean-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setMean(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.mean = value;
        }

        /**
         * Ruft den Wert der k-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public IntegerType getK() {
            return k;
        }

        /**
         * Legt den Wert der k-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setK(IntegerType value) {
            this.k = value;
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
    public static class Exponential
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
     *       &lt;attribute name="Alpha" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
     *       &lt;attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
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
    public static class Gamma
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "Alpha", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha;
        @XmlAttribute(name = "Beta", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType beta;

        /**
         * Ruft den Wert der alpha-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha() {
            return alpha;
        }

        /**
         * Legt den Wert der alpha-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setAlpha(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha = value;
        }

        /**
         * Ruft den Wert der beta-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getBeta() {
            return beta;
        }

        /**
         * Legt den Wert der beta-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setBeta(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.beta = value;
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
     *       &lt;attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
     *       &lt;attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
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
    public static class LogNormal
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "Mu", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType mu;
        @XmlAttribute(name = "Sigma", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType sigma;

        /**
         * Ruft den Wert der mu-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getMu() {
            return mu;
        }

        /**
         * Legt den Wert der mu-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setMu(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.mu = value;
        }

        /**
         * Ruft den Wert der sigma-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getSigma() {
            return sigma;
        }

        /**
         * Legt den Wert der sigma-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setSigma(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.sigma = value;
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
     *       &lt;attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
     *       &lt;attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
     *       &lt;attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
     *       &lt;attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
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
    public static class LogNormalTrunc
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "Mu", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType mu;
        @XmlAttribute(name = "Sigma", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType sigma;
        @XmlAttribute(name = "Min", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType min;
        @XmlAttribute(name = "Max", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType max;

        /**
         * Ruft den Wert der mu-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getMu() {
            return mu;
        }

        /**
         * Legt den Wert der mu-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setMu(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.mu = value;
        }

        /**
         * Ruft den Wert der sigma-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getSigma() {
            return sigma;
        }

        /**
         * Legt den Wert der sigma-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setSigma(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.sigma = value;
        }

        /**
         * Ruft den Wert der min-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getMin() {
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
        public void setMin(org.opentrafficsim.xml.bindings.types.DoubleType value) {
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
        public org.opentrafficsim.xml.bindings.types.DoubleType getMax() {
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
        public void setMax(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.max = value;
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
     *       &lt;attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
     *       &lt;attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositiveInclusive" /&gt;
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
    public static class Normal
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "Mu", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType mu;
        @XmlAttribute(name = "Sigma", required = true)
        @XmlJavaTypeAdapter(DoublePositiveInclusiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType sigma;

        /**
         * Ruft den Wert der mu-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getMu() {
            return mu;
        }

        /**
         * Legt den Wert der mu-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setMu(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.mu = value;
        }

        /**
         * Ruft den Wert der sigma-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getSigma() {
            return sigma;
        }

        /**
         * Legt den Wert der sigma-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setSigma(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.sigma = value;
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
     *       &lt;attribute name="Mu" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
     *       &lt;attribute name="Sigma" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositiveInclusive" /&gt;
     *       &lt;attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
     *       &lt;attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
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
    public static class NormalTrunc
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "Mu", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType mu;
        @XmlAttribute(name = "Sigma", required = true)
        @XmlJavaTypeAdapter(DoublePositiveInclusiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType sigma;
        @XmlAttribute(name = "Min", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType min;
        @XmlAttribute(name = "Max", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType max;

        /**
         * Ruft den Wert der mu-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getMu() {
            return mu;
        }

        /**
         * Legt den Wert der mu-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setMu(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.mu = value;
        }

        /**
         * Ruft den Wert der sigma-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getSigma() {
            return sigma;
        }

        /**
         * Legt den Wert der sigma-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setSigma(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.sigma = value;
        }

        /**
         * Ruft den Wert der min-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getMin() {
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
        public void setMin(org.opentrafficsim.xml.bindings.types.DoubleType value) {
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
        public org.opentrafficsim.xml.bindings.types.DoubleType getMax() {
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
        public void setMax(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.max = value;
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
     *       &lt;attribute name="Alpha" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
     *       &lt;attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
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
    public static class Pearson5
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "Alpha", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha;
        @XmlAttribute(name = "Beta", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType beta;

        /**
         * Ruft den Wert der alpha-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha() {
            return alpha;
        }

        /**
         * Legt den Wert der alpha-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setAlpha(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha = value;
        }

        /**
         * Ruft den Wert der beta-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getBeta() {
            return beta;
        }

        /**
         * Legt den Wert der beta-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setBeta(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.beta = value;
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
     *       &lt;attribute name="Alpha1" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
     *       &lt;attribute name="Alpha2" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
     *       &lt;attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
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
    public static class Pearson6
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "Alpha1", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha1;
        @XmlAttribute(name = "Alpha2", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha2;
        @XmlAttribute(name = "Beta", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType beta;

        /**
         * Ruft den Wert der alpha1-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha1() {
            return alpha1;
        }

        /**
         * Legt den Wert der alpha1-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setAlpha1(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha1 = value;
        }

        /**
         * Ruft den Wert der alpha2-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha2() {
            return alpha2;
        }

        /**
         * Legt den Wert der alpha2-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setAlpha2(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha2 = value;
        }

        /**
         * Ruft den Wert der beta-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getBeta() {
            return beta;
        }

        /**
         * Legt den Wert der beta-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setBeta(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.beta = value;
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
     *       &lt;attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
     *       &lt;attribute name="Mode" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
     *       &lt;attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
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
    public static class Triangular
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "Min", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType min;
        @XmlAttribute(name = "Mode", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType mode;
        @XmlAttribute(name = "Max", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType max;

        /**
         * Ruft den Wert der min-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getMin() {
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
        public void setMin(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.min = value;
        }

        /**
         * Ruft den Wert der mode-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getMode() {
            return mode;
        }

        /**
         * Legt den Wert der mode-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setMode(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.mode = value;
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
        public org.opentrafficsim.xml.bindings.types.DoubleType getMax() {
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
        public void setMax(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.max = value;
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
     *       &lt;attribute name="Min" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
     *       &lt;attribute name="Max" use="required" type="{http://www.opentrafficsim.org/ots}double" /&gt;
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
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType min;
        @XmlAttribute(name = "Max", required = true)
        @XmlJavaTypeAdapter(DoubleAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType max;

        /**
         * Ruft den Wert der min-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getMin() {
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
        public void setMin(org.opentrafficsim.xml.bindings.types.DoubleType value) {
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
        public org.opentrafficsim.xml.bindings.types.DoubleType getMax() {
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
        public void setMax(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.max = value;
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
     *       &lt;attribute name="Alpha" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
     *       &lt;attribute name="Beta" use="required" type="{http://www.opentrafficsim.org/ots}DoublePositive" /&gt;
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
    public static class Weibull
        implements Serializable
    {

        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        private final static long serialVersionUID = 10102L;
        @XmlAttribute(name = "Alpha", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType alpha;
        @XmlAttribute(name = "Beta", required = true)
        @XmlJavaTypeAdapter(DoublePositiveAdapter.class)
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        protected org.opentrafficsim.xml.bindings.types.DoubleType beta;

        /**
         * Ruft den Wert der alpha-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getAlpha() {
            return alpha;
        }

        /**
         * Legt den Wert der alpha-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setAlpha(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.alpha = value;
        }

        /**
         * Ruft den Wert der beta-Eigenschaft ab.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public org.opentrafficsim.xml.bindings.types.DoubleType getBeta() {
            return beta;
        }

        /**
         * Legt den Wert der beta-Eigenschaft fest.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
        public void setBeta(org.opentrafficsim.xml.bindings.types.DoubleType value) {
            this.beta = value;
        }

    }

}
