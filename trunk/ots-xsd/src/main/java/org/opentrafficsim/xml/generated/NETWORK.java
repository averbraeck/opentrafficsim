//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.04.10 at 07:36:20 PM CEST 
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
 *         &lt;element ref="{http://www.w3.org/2001/XInclude}include" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}NODE" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}CONNECTOR" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}LINK" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}ROUTE" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}ROUTEMIX" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}SHORTESTROUTE" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.opentrafficsim.org/ots}SHORTESTROUTEMIX" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
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
    "include",
    "node",
    "connector",
    "link",
    "route",
    "routemix",
    "shortestroute",
    "shortestroutemix"
})
@XmlRootElement(name = "NETWORK")
@Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
public class NETWORK
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    private final static long serialVersionUID = 10102L;
    @XmlElement(namespace = "http://www.w3.org/2001/XInclude")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    protected List<IncludeType> include;
    @XmlElement(name = "NODE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    protected List<NODE> node;
    @XmlElement(name = "CONNECTOR")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    protected List<CONNECTOR> connector;
    @XmlElement(name = "LINK")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    protected List<LINK> link;
    @XmlElement(name = "ROUTE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    protected List<ROUTE> route;
    @XmlElement(name = "ROUTEMIX")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    protected List<ROUTEMIX> routemix;
    @XmlElement(name = "SHORTESTROUTE")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    protected List<SHORTESTROUTE> shortestroute;
    @XmlElement(name = "SHORTESTROUTEMIX")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    protected List<SHORTESTROUTEMIX> shortestroutemix;
    @XmlAttribute(name = "base", namespace = "http://www.w3.org/XML/1998/namespace")
    @XmlSchemaType(name = "anyURI")
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    protected String base;

    /**
     * Gets the value of the include property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the include property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInclude().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IncludeType }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    public List<IncludeType> getInclude() {
        if (include == null) {
            include = new ArrayList<IncludeType>();
        }
        return this.include;
    }

    /**
     * Gets the value of the node property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the node property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNODE().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NODE }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    public List<NODE> getNODE() {
        if (node == null) {
            node = new ArrayList<NODE>();
        }
        return this.node;
    }

    /**
     * Gets the value of the connector property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the connector property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCONNECTOR().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CONNECTOR }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    public List<CONNECTOR> getCONNECTOR() {
        if (connector == null) {
            connector = new ArrayList<CONNECTOR>();
        }
        return this.connector;
    }

    /**
     * Gets the value of the link property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the link property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLINK().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LINK }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    public List<LINK> getLINK() {
        if (link == null) {
            link = new ArrayList<LINK>();
        }
        return this.link;
    }

    /**
     * Gets the value of the route property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the route property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getROUTE().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ROUTE }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    public List<ROUTE> getROUTE() {
        if (route == null) {
            route = new ArrayList<ROUTE>();
        }
        return this.route;
    }

    /**
     * Gets the value of the routemix property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the routemix property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getROUTEMIX().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ROUTEMIX }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    public List<ROUTEMIX> getROUTEMIX() {
        if (routemix == null) {
            routemix = new ArrayList<ROUTEMIX>();
        }
        return this.routemix;
    }

    /**
     * Gets the value of the shortestroute property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shortestroute property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSHORTESTROUTE().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SHORTESTROUTE }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    public List<SHORTESTROUTE> getSHORTESTROUTE() {
        if (shortestroute == null) {
            shortestroute = new ArrayList<SHORTESTROUTE>();
        }
        return this.shortestroute;
    }

    /**
     * Gets the value of the shortestroutemix property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shortestroutemix property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSHORTESTROUTEMIX().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SHORTESTROUTEMIX }
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    public List<SHORTESTROUTEMIX> getSHORTESTROUTEMIX() {
        if (shortestroutemix == null) {
            shortestroutemix = new ArrayList<SHORTESTROUTEMIX>();
        }
        return this.shortestroutemix;
    }

    /**
     * Gets the value of the base property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
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
    @Generated(value = "com.sun.tools.xjc.Driver", date = "2019-04-10T07:36:19+02:00", comments = "JAXB RI v2.3.0")
    public void setBase(String value) {
        this.base = value;
    }

}
