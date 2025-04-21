
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java class for e_objectType</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * <pre>{@code
 * <simpleType name="e_objectType">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     <enumeration value="none"/>
 *     <enumeration value="obstacle"/>
 *     <enumeration value="car"/>
 *     <enumeration value="pole"/>
 *     <enumeration value="tree"/>
 *     <enumeration value="vegetation"/>
 *     <enumeration value="barrier"/>
 *     <enumeration value="building"/>
 *     <enumeration value="parkingSpace"/>
 *     <enumeration value="patch"/>
 *     <enumeration value="railing"/>
 *     <enumeration value="trafficIsland"/>
 *     <enumeration value="crosswalk"/>
 *     <enumeration value="streetLamp"/>
 *     <enumeration value="gantry"/>
 *     <enumeration value="soundBarrier"/>
 *     <enumeration value="van"/>
 *     <enumeration value="bus"/>
 *     <enumeration value="trailer"/>
 *     <enumeration value="bike"/>
 *     <enumeration value="motorbike"/>
 *     <enumeration value="tram"/>
 *     <enumeration value="train"/>
 *     <enumeration value="pedestrian"/>
 *     <enumeration value="wind"/>
 *     <enumeration value="roadMark"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "e_objectType")
@XmlEnum
@SuppressWarnings("all") public enum EObjectType {


    /**
     * i.e. unknown
     * 
     */
    @XmlEnumValue("none")
    NONE("none"),

    /**
     * for anything that is not further categorized
     * 
     */
    @XmlEnumValue("obstacle")
    OBSTACLE("obstacle"),

    /**
     * deprecated
     * 
     */
    @XmlEnumValue("car")
    CAR("car"),
    @XmlEnumValue("pole")
    POLE("pole"),
    @XmlEnumValue("tree")
    TREE("tree"),
    @XmlEnumValue("vegetation")
    VEGETATION("vegetation"),
    @XmlEnumValue("barrier")
    BARRIER("barrier"),
    @XmlEnumValue("building")
    BUILDING("building"),
    @XmlEnumValue("parkingSpace")
    PARKING_SPACE("parkingSpace"),
    @XmlEnumValue("patch")
    PATCH("patch"),
    @XmlEnumValue("railing")
    RAILING("railing"),
    @XmlEnumValue("trafficIsland")
    TRAFFIC_ISLAND("trafficIsland"),
    @XmlEnumValue("crosswalk")
    CROSSWALK("crosswalk"),
    @XmlEnumValue("streetLamp")
    STREET_LAMP("streetLamp"),
    @XmlEnumValue("gantry")
    GANTRY("gantry"),
    @XmlEnumValue("soundBarrier")
    SOUND_BARRIER("soundBarrier"),

    /**
     * deprecated
     * 
     */
    @XmlEnumValue("van")
    VAN("van"),

    /**
     * deprecated
     * 
     */
    @XmlEnumValue("bus")
    BUS("bus"),

    /**
     * deprecated
     * 
     */
    @XmlEnumValue("trailer")
    TRAILER("trailer"),

    /**
     * deprecated
     * 
     */
    @XmlEnumValue("bike")
    BIKE("bike"),

    /**
     * deprecated
     * 
     */
    @XmlEnumValue("motorbike")
    MOTORBIKE("motorbike"),

    /**
     * deprecated
     * 
     */
    @XmlEnumValue("tram")
    TRAM("tram"),

    /**
     * deprecated
     * 
     */
    @XmlEnumValue("train")
    TRAIN("train"),

    /**
     * deprecated
     * 
     */
    @XmlEnumValue("pedestrian")
    PEDESTRIAN("pedestrian"),

    /**
     * deprecated
     * 
     */
    @XmlEnumValue("wind")
    WIND("wind"),
    @XmlEnumValue("roadMark")
    ROAD_MARK("roadMark");
    private final String value;

    EObjectType(String v) {
        value = v;
    }

    /**
     * Gets the value associated to the enum constant.
     * 
     * @return
     *     The value linked to the enum.
     */
    public String value() {
        return value;
    }

    /**
     * Gets the enum associated to the value passed as parameter.
     * 
     * @param v
     *     The value to get the enum from.
     * @return
     *     The enum which corresponds to the value, if it exists.
     * @throws IllegalArgumentException
     *     If no value matches in the enum declaration.
     */
    public static EObjectType fromValue(String v) {
        for (EObjectType c: EObjectType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
