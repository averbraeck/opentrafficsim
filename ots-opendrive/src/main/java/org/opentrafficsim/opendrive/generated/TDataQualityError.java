
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * The absolute or relative errors of road data are described by <error> elements within the <dataQuality> element.
 * 
 * <p>Java class for t_dataQuality_Error complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_dataQuality_Error">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *       </sequence>
 *       <attribute name="xyAbsolute" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="zAbsolute" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="xyRelative" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       <attribute name="zRelative" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_dataQuality_Error")
@SuppressWarnings("all") public class TDataQualityError {

    /**
     * Absolute error of the road data in x/y direction
     * 
     */
    @XmlAttribute(name = "xyAbsolute", required = true)
    protected double xyAbsolute;
    /**
     * Absolute error of the road data in z direction
     * 
     */
    @XmlAttribute(name = "zAbsolute", required = true)
    protected double zAbsolute;
    /**
     * Relative error of the road data in x/y direction
     * 
     */
    @XmlAttribute(name = "xyRelative", required = true)
    protected double xyRelative;
    /**
     * Relative error of the road data in z direction
     * 
     */
    @XmlAttribute(name = "zRelative", required = true)
    protected double zRelative;

    /**
     * Absolute error of the road data in x/y direction
     * 
     */
    public double getXyAbsolute() {
        return xyAbsolute;
    }

    /**
     * Sets the value of the xyAbsolute property.
     * 
     */
    public void setXyAbsolute(double value) {
        this.xyAbsolute = value;
    }

    /**
     * Absolute error of the road data in z direction
     * 
     */
    public double getZAbsolute() {
        return zAbsolute;
    }

    /**
     * Sets the value of the zAbsolute property.
     * 
     */
    public void setZAbsolute(double value) {
        this.zAbsolute = value;
    }

    /**
     * Relative error of the road data in x/y direction
     * 
     */
    public double getXyRelative() {
        return xyRelative;
    }

    /**
     * Sets the value of the xyRelative property.
     * 
     */
    public void setXyRelative(double value) {
        this.xyRelative = value;
    }

    /**
     * Relative error of the road data in z direction
     * 
     */
    public double getZRelative() {
        return zRelative;
    }

    /**
     * Sets the value of the zRelative property.
     * 
     */
    public void setZRelative(double value) {
        this.zRelative = value;
    }

}
