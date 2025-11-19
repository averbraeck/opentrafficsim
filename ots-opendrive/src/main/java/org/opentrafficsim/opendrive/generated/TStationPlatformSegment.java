
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Each platform element is valid on one or more track segments. The &lt;segment&gt; element must be specified.
 * 
 * <p>Java class for t_station_platform_segment complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_station_platform_segment">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="roadId" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="sStart" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="sEnd" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_grEqZero" />
 *       <attribute name="side" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_station_platform_segment_side" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_station_platform_segment")
@SuppressWarnings("all") public class TStationPlatformSegment
    extends OpenDriveElement
{

    /**
     * Unique ID of the <road> element (track) that accompanies the platform
     * 
     */
    @XmlAttribute(name = "roadId", required = true)
    protected String roadId;
    /**
     * Minimum s-coordinate on <road> element that has an adjacent platform
     * 
     */
    @XmlAttribute(name = "sStart", required = true)
    protected double sStart;
    /**
     * Maximum s-coordiante on <road> element that has an adjacent platform
     * 
     */
    @XmlAttribute(name = "sEnd", required = true)
    protected double sEnd;
    /**
     * Side of track on which the platform is situated when going from sStart to sEnd. For values see UML Model
     * 
     */
    @XmlAttribute(name = "side", required = true)
    protected EStationPlatformSegmentSide side;

    /**
     * Unique ID of the <road> element (track) that accompanies the platform
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRoadId() {
        return roadId;
    }

    /**
     * Sets the value of the roadId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getRoadId()
     */
    public void setRoadId(String value) {
        this.roadId = value;
    }

    /**
     * Minimum s-coordinate on <road> element that has an adjacent platform
     * 
     */
    public double getSStart() {
        return sStart;
    }

    /**
     * Sets the value of the sStart property.
     * 
     */
    public void setSStart(double value) {
        this.sStart = value;
    }

    /**
     * Maximum s-coordiante on <road> element that has an adjacent platform
     * 
     */
    public double getSEnd() {
        return sEnd;
    }

    /**
     * Sets the value of the sEnd property.
     * 
     */
    public void setSEnd(double value) {
        this.sEnd = value;
    }

    /**
     * Side of track on which the platform is situated when going from sStart to sEnd. For values see UML Model
     * 
     * @return
     *     possible object is
     *     {@link EStationPlatformSegmentSide }
     *     
     */
    public EStationPlatformSegmentSide getSide() {
        return side;
    }

    /**
     * Sets the value of the side property.
     * 
     * @param value
     *     allowed object is
     *     {@link EStationPlatformSegmentSide }
     *     
     * @see #getSide()
     */
    public void setSide(EStationPlatformSegmentSide value) {
        this.side = value;
    }

}
