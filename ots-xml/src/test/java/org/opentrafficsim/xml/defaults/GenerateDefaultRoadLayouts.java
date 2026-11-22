package org.opentrafficsim.xml.defaults;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates {@code default_roadlayouts.xml} from hard-coded recipes. Each recipe describes the road layout from left to right;
 * the generated XML lists the cross-section elements from right to left, as required by the XSD. Running the main method writes
 * the file to {@code ots-xml/src/main/resources/xsd/defaults/}.
 * <p>
 * Copyright (c) 2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public final class GenerateDefaultRoadLayouts
{

    /** Highway lane type and link type. */
    private static final String HIGHWAY = "NL.HIGHWAY";

    /** Urban lane type and link type. */
    private static final String URBAN = "NL.URBAN";

    /** Solid stripe type. */
    private static final String SOLID = "NL.SOLID";

    /** Dashed stripe type. */
    private static final String DASHED = "NL.DASHED";

    /** Urban dashed stripe type. */
    private static final String DASHED_URBAN = "NL.DASHED_URBAN";

    /** Turn dashed stripe type. */
    private static final String DASHED_TURN = "NL.DASHED_TURN";

    /** Block stripe type. */
    private static final String BLOCK = "NL.BLOCK";

    /** Width of all lanes and shoulders. */
    private static final String WIDTH = "3.5m";

    /** Center offset of the single lane that has one. */
    private static final String CENTER_OFFSET = "0.0m";

    /** Recipes, in the order in which the road layouts are generated. */
    /* {@formatter:off} */
    private static final List<String> RECIPES = List.of(
            "::",
            ":::",
            "::::",
            "||",
            "|.|",
            "|..|",
            "|...|",
            "|....|",
            "|.....|",
            "||||",
            "||.||",
            "||.:||",
            "||.:.||",
            "||.:..||",
            "||..||",
            "||..:||",
            "||..:.||",
            "||..:..||",
            "||...||",
            "||...:||",
            "||...:.||",
            "||....||",
            "||.....||",
            "||.:.\\\\",
            "||..:.\\\\",
            "||...:.\\\\",
            "||':'//",
            "||'':'//",
            "||''':'//"
    );
    /* {@formatter:on} */

    /**
     * Constructor.
     */
    private GenerateDefaultRoadLayouts()
    {
        //
    }

    /**
     * Main method. Writes the generated XML to the defaults folder in the main resources.
     * @param args args (not used)
     * @throws IOException exception when the file cannot be written
     * @throws URISyntaxException exception when the defaults folder cannot be located
     */
    public static void main(final String[] args) throws IOException, URISyntaxException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<ots:RoadLayouts xmlns:ots=\"http://www.opentrafficsim.org/ots\"\n");
        sb.append("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        sb.append("  xsi:schemaLocation=\"http://www.opentrafficsim.org/ots ../ots-definitions.xsd\">\n");
        sb.append("\n");
        for (String linkType : List.of(URBAN, HIGHWAY))
        {
            for (String recipe : RECIPES)
            {
                boolean highway = linkType.equals(HIGHWAY);
                if (highway == isHighway(recipe))
                {
                    appendLayout(sb, recipe, highway, linkType);
                }
            }
        }
        sb.append("</ots:RoadLayouts>\n");

        Path output = Path.of("src").resolve("main").resolve("resources").resolve("xsd").resolve("defaults")
                .resolve("default_roadlayouts.xml").toAbsolutePath();
        Files.writeString(output, sb.toString(), StandardCharsets.UTF_8);
        System.out.println("Written " + output);
    }

    /**
     * Determines whether a recipe describes a highway road layout.
     * @param recipe recipe
     * @return whether the recipe is a highway recipe
     */
    private static boolean isHighway(final String recipe)
    {
        String r = recipe.trim();
        if (r.equals("||"))
        {
            return false;
        }
        return r.startsWith("||") && (r.endsWith("||") || r.endsWith("//") || r.endsWith("\\\\"));
    }

    /**
     * Generates one RoadLayout tag.
     * @param sb builder to append to
     * @param recipe recipe
     * @param highway whether this is a highway recipe
     * @param linkType link type used to compose the road layout id
     */
    private static void appendLayout(final StringBuilder sb, final String recipe, final boolean highway, final String linkType)
    {
        String r = recipe.trim();
        List<String> stripeTypes = new ArrayList<>();
        for (int i = 0; i < r.length(); i++)
        {
            stripeTypes.add(stripeType(r.charAt(i), highway));
        }

        // Gap i lies between stripe i and stripe i + 1. A pair of solid lines only bounds a shoulder at the left-most or
        // right-most edge of a highway layout; any inner pair (recipe "||||") bounds a Lane instead.
        List<Boolean> shoulder = new ArrayList<>();
        for (int i = 0; i < stripeTypes.size() - 1; i++)
        {
            boolean edge = i == 0 || i == stripeTypes.size() - 2;
            shoulder.add(highway && edge && SOLID.equals(stripeTypes.get(i)) && SOLID.equals(stripeTypes.get(i + 1)));
        }
        List<Integer> lanes = new ArrayList<>();
        for (int i = 0; i < shoulder.size(); i++)
        {
            if (!shoulder.get(i))
            {
                lanes.add(i);
            }
        }

        int offsetLane = -1;
        if (!lanes.isEmpty())
        {
            // A ":|" puts the offset on the lane directly left of the block stripe, otherwise on the right-most lane.
            offsetLane = r.contains(":|") || (r.contains(":") && (r.endsWith("/") || r.endsWith("\\")))
                    ? lanes.indexOf(r.indexOf(":") - 1) : lanes.size() - 1;
        }
        int taperLane = -1;
        boolean taperStart = r.contains("/");
        if (highway && (taperStart || r.contains("\\")))
        {
            taperLane = lanes.indexOf(r.indexOf(':'));
        }

        int left = 0;
        int right = 0;
        if (highway && r.contains(":"))
        {
            int colon = r.indexOf(':');
            for (int lane : lanes)
            {
                if (lane < colon)
                {
                    left++;
                }
                else
                {
                    right++;
                }
            }
        }

        String id = roadLayoutId(r, highway, linkType, lanes.size(), left, right);
        sb.append("  <ots:RoadLayout Id=\"").append(id).append("\" Builtin=\"true\">\n");

        List<String> elements = new ArrayList<>();
        int stripeNo = 0;
        int laneNo = 0;
        for (int i = 0; i < r.length(); i++)
        {
            // The outer solid line of a highway layout is the edge of the road, not a Stripe.
            if (!(highway && (i == 0 || i == r.length() - 1)))
            {
                appendStripe(elements, ++stripeNo, stripeTypes.get(i));
            }
            if (i < shoulder.size())
            {
                if (shoulder.get(i))
                {
                    appendShoulder(elements, i == 0 ? "LeftShoulder" : "RightShoulder");
                }
                else
                {
                    int recipeLane = lanes.indexOf(i);
                    appendLane(elements, ++laneNo, recipeLane == offsetLane, recipeLane == taperLane, taperStart);
                }
            }
        }
        for (int i = elements.size() - 1; i >= 0; i--)
        {
            sb.append(elements.get(i));
        }
        sb.append("  </ots:RoadLayout>\n");
        sb.append("\n");
    }

    /**
     * Composes the RoadLayout Id.
     * @param recipe recipe
     * @param highway whether this is a highway recipe
     * @param linkType lane type and link type of this road layout
     * @param lanes number of lanes
     * @param left number of lanes to the left of the block stripe
     * @param right number of lanes to the right of the block stripe
     * @return RoadLayout Id
     */
    private static String roadLayoutId(final String recipe, final boolean highway, final String linkType, final int lanes,
            final int left, final int right)
    {
        String id;
        if (!highway)
        {
            id = (linkType.equals(URBAN) ? "URBAN" : "PROVINCIAL") + (recipe.contains("|") ? " " : " TURN ") + lanes;
        }
        else if (!recipe.contains(":"))
        {
            id = "HIGHWAY " + lanes;
        }
        else
        {
            id = "HIGHWAY " + left + "+" + right;
        }
        if (recipe.endsWith("//"))
        {
            return id + "DT";
        }
        if (recipe.endsWith("\\\\"))
        {
            return id + "MT";
        }
        return id;
    }

    /**
     * Maps a recipe character to a stripe type.
     * @param c recipe character
     * @param highway whether this is a highway recipe
     * @return stripe type
     */
    private static String stripeType(final char c, final boolean highway)
    {
        if (highway)
        {
            switch (c)
            {
                case '|', '/', '\\':
                    return SOLID;
                case '.', '\'':
                    return DASHED;
                case ':':
                    return BLOCK;
                default:
                    throw new IllegalArgumentException("Unknown highway recipe character: " + c);
            }
        }
        switch (c)
        {
            case '|':
                return SOLID;
            case '.':
                return DASHED_URBAN;
            case ':':
                return DASHED_TURN;
            default:
                throw new IllegalArgumentException("Unknown urban recipe character: " + c);
        }
    }

    /**
     * Appends a Stripe tag.
     * @param elements elements in recipe order
     * @param number stripe number
     * @param type stripe type
     */
    private static void appendStripe(final List<String> elements, final int number, final String type)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("    <ots:Stripe Id=\"Stripe").append(number).append("\" StripeType=\"").append(type).append("\">\n");
        sb.append("    </ots:Stripe>\n");
        elements.add(sb.toString());
    }

    /**
     * Appends a Shoulder tag.
     * @param elements elements in recipe order
     * @param id shoulder Id
     */
    private static void appendShoulder(final List<String> elements, final String id)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("    <ots:Shoulder Id=\"").append(id).append("\">\n");
        sb.append("      <ots:Width>").append(WIDTH).append("</ots:Width>\n");
        sb.append("    </ots:Shoulder>\n");
        elements.add(sb.toString());
    }

    /**
     * Appends a Lane tag.
     * @param elements elements in recipe order
     * @param number lane number
     * @param center whether this lane receives the center offset
     * @param taper whether this lane tapers
     * @param taperStart whether the taper is at the start of the link
     */
    private static void appendLane(final List<String> elements, final int number, final boolean center, final boolean taper,
            final boolean taperStart)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("    <ots:Lane Id=\"Lane").append(number).append("\">\n");
        if (center)
        {
            sb.append("      <ots:CenterOffset>").append(CENTER_OFFSET).append("</ots:CenterOffset>\n");
        }
        if (taper)
        {
            sb.append("      <ots:WidthStart>").append(taperStart ? "0.0m" : WIDTH).append("</ots:WidthStart>\n");
            sb.append("      <ots:WidthEnd>").append(taperStart ? WIDTH : "0.0m").append("</ots:WidthEnd>\n");
        }
        else
        {
            sb.append("      <ots:Width>").append(WIDTH).append("</ots:Width>\n");
        }
        sb.append("    </ots:Lane>\n");
        elements.add(sb.toString());
    }

}
