package org.opentrafficsim.trafficcontrol.trafcod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.opentrafficsim.core.Throw;

/**
 * TrafCOD evaluator.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 5, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Evaluator
{
    /** Version of the supported TrafCOD files. */
    final static int TRAFCOD_VERSION = 100;

    /** Text leading up to the TrafCOD version number. */
    private final static String VERSION_PREFIX = "trafcod-version=";

    /** Text on line before the sequence line. */
    private final static String SEQUENCE_KEY = "Sequence";

    /** Text leading up to the control program structure. */
    private final static String STRUCTURE_PREFIX = "Structure:";

    /** The original rules. */
    final List<String> trafcodRules = new ArrayList<>();

    /** The TrafCOD variables. */
    final Map<String, Variable> variables = new HashMap<>();

    /** Comment starter in TrafCOD. */
    final static String COMMENT_START = "#";

    /** Prefix for initialization rules. */
    private final static String INIT_PREFIX = "%init ";
    
    /** Prefix for time initializer rules. */
    private final static String TIME_PREFIX = "%time ";
    
    /** Prefix for export rules. */
    private final static String EXPORT_PREFIX = "%export ";

    /** Sequence information; number of conflict groups. */
    private int conflictGroups = -1;

    /** Sequence information; size of conflict group. */
    private int conflictGroupSize = -1;

    /** Chosen structure number (as assigned by VRIGen). */
    private int structureNumber = -1;

    /** The conflict groups in order that they will be served. */
    private List<List<Short>> conflictgroups = new ArrayList<>();
    
    /**
     * @param trafCodURL String; the URL of the TrafCOD rules
     * @throws Exception when a rule cannot be parsed
     */
    public Evaluator(final String trafCodURL) throws Exception
    {
        Throw.whenNull(trafCodURL, "trafCodURL may not be null");
        URL url = new URL(trafCodURL);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String inputLine;
        int lineno = 0;
        while ((inputLine = in.readLine()) != null)
        {
            ++lineno;
            System.out.println(lineno + ":\t" + inputLine);
            String trimmedLine = inputLine.trim();
            if (trimmedLine.length() == 0)
            {
                continue;
            }
            String locationDescription = trafCodURL + "(" + lineno + ") ";
            if (trimmedLine.startsWith(COMMENT_START))
            {
                String commentStripped = trimmedLine.substring(1).trim();
                if (stringBeginsWithIgnoreCase(VERSION_PREFIX, commentStripped))
                {
                    String versionString = commentStripped.substring(VERSION_PREFIX.length());
                    try
                    {
                        int observedVersion = Integer.parseInt(versionString);
                        if (TRAFCOD_VERSION != observedVersion)
                        {
                            throw new Exception("Wrong TrafCOD version (expected " + TRAFCOD_VERSION + ", got "
                                    + observedVersion + ")");
                        }
                    }
                    catch (NumberFormatException nfe)
                    {
                        nfe.printStackTrace();
                        throw new Exception("Could not parse TrafCOD version (got \"" + versionString + ")");
                    }
                }
                else if (stringBeginsWithIgnoreCase(SEQUENCE_KEY, commentStripped))
                {
                    while (trimmedLine.startsWith(COMMENT_START))
                    {
                        inputLine = in.readLine();
                        if (null == inputLine)
                        {
                            throw new Exception("Unexpected EOF (reading sequence key at " + locationDescription + ")");
                        }
                        ++lineno;
                        trimmedLine = inputLine.trim();
                    }
                    String[] fields = inputLine.split("\t");
                    if (fields.length != 2)
                    {
                        throw new Exception("Wrong number of fields in Sequence information");
                    }
                    try
                    {
                        this.conflictGroups = Integer.parseInt(fields[0]);
                        this.conflictGroupSize = Integer.parseInt(fields[1]);
                    }
                    catch (NumberFormatException nfe)
                    {
                        nfe.printStackTrace();
                        throw new Exception("Bad number of conflict groups or bad conflict group size");
                    }
                }
                else if (stringBeginsWithIgnoreCase(STRUCTURE_PREFIX, commentStripped))
                {
                    String structureNumberString = commentStripped.substring(STRUCTURE_PREFIX.length()).trim();
                    try
                    {
                        this.structureNumber = Integer.parseInt(structureNumberString);
                    }
                    catch (NumberFormatException nfe)
                    {
                        nfe.printStackTrace();
                        throw new Exception("Bad structure number (got \"" + structureNumberString + "\" at "
                                + locationDescription + ")");
                    }
                    for (int conflictMemberLine = 0; conflictMemberLine < this.conflictGroups; conflictMemberLine++)
                    {
                        while (trimmedLine.startsWith(COMMENT_START))
                        {
                            inputLine = in.readLine();
                            if (null == inputLine)
                            {
                                throw new Exception("Unexpected EOF (reading sequence key at " + locationDescription + ")");
                            }
                            ++lineno;
                            trimmedLine = inputLine.trim();
                        }
                        String[] fields = inputLine.split("\t");
                        if (fields.length != this.conflictGroupSize)
                        {
                            throw new Exception("Wrong number of conflict groups in Structure information");
                        }
                        List<Short> row = new ArrayList<>(this.conflictGroupSize);
                        for (int col = 0; col < this.conflictGroupSize; col++)
                        {
                            try
                            {
                                Short stream = Short.parseShort(fields[col]);
                                row.add(stream);
                            }
                            catch (NumberFormatException nfe)
                            {
                                nfe.printStackTrace();
                                throw new Exception("Wrong number of streams in conflict group " + trimmedLine);
                            }
                        }
                    }
                }
                continue;
            }
            if (stringBeginsWithIgnoreCase(INIT_PREFIX, trimmedLine))
            {
                String varNameAndInitialValue = trimmedLine.substring(INIT_PREFIX.length()).trim().replaceAll("[ \t]+", " ");
                String[] fields = varNameAndInitialValue.split(" ");
                NameAndStream nameAndStream = new NameAndStream(fields[0]);
                installVariable(nameAndStream.name, nameAndStream.getStream(), EnumSet.noneOf(Flags.class), locationDescription);
                // The supplied initial value is ignored (in this version of the TrafCOD interpreter)!
                continue;
            }
            if (stringBeginsWithIgnoreCase(TIME_PREFIX, trimmedLine))
            {
                String timerNameAndMaximumValue = trimmedLine.substring(INIT_PREFIX.length()).trim().replaceAll("[ \t]+", " ");
                String[] fields = timerNameAndMaximumValue.split(" ");
                NameAndStream nameAndStream = new NameAndStream(fields[0]);
                Variable variable = installVariable(nameAndStream.name, nameAndStream.getStream(), EnumSet.noneOf(Flags.class), locationDescription);
                int value10 = Integer.parseInt(fields[1]);
                variable.setTimerMax(value10);
                continue;
            }
            if (stringBeginsWithIgnoreCase(EXPORT_PREFIX, trimmedLine))
            {
                String varNameAndOutputValue = trimmedLine.substring(INIT_PREFIX.length()).trim().replaceAll("[ \t]+", " ");
                String[] fields = varNameAndOutputValue.split(" ");
                NameAndStream nameAndStream = new NameAndStream(fields[0]);
                Variable variable = installVariable(nameAndStream.name, nameAndStream.getStream(), EnumSet.noneOf(Flags.class), locationDescription);
                int value = Integer.parseInt(fields[1]);
                variable.setOutput(value);
                continue;
            }
            this.trafcodRules.add(trimmedLine);
            parse(trimmedLine, locationDescription);

            
            
            
        }
        in.close();
        System.out.println("Installed " + this.variables.size() + " variables");
        for (String key : this.variables.keySet())
        {
            System.out.println(key
                    + ":\t"
                    + this.variables.get(key).selectedFieldsToString(
                            EnumSet.of(PrintFlags.ID, PrintFlags.VALUE, PrintFlags.INITTIMER, PrintFlags.REINITTIMER,
                                    PrintFlags.S, PrintFlags.E)));
        }
    }
    
    /**
     * States of the rule parser.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$,
     *          initial version Oct 7, 2016 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    enum ParserState
    {
        /** Looking for the left hand side of an assignment. */
        FIND_LHS,
        /** Looking for an assignment operator. */
        FIND_ASSIGN,
        /** Looking for the right hand side of an assignment. */
        FIND_RHS,
        /** Looking for an optional unary minus. */
        MAY_UMINUS,
        /** Looking for an expression. */
        FIND_EXPR,
    }
    
    enum RuleType
    {
        /** Assignment rule. */
        ASSIGNMENT,
        /** Start rule. */
        START,
        /** End rule. */
        END,
    }
    
    /**
     * Parse one TrafCOD rule.
     * @param rawRule String; the TrafCOD rule
     * @param locationDescription String; description of the location (file, line) where the rule was found
     * @throws Exception when the rule is not a valid TrafCOD rule
     */
    private void parse(final String rawRule, final String locationDescription) throws Exception
    {
        if (rawRule.length() == 0)
        {
            throw new Exception("empty rule at " + locationDescription);
        }
        ParserState state = ParserState.FIND_LHS;
        String rule = rawRule.toUpperCase(Locale.US);
        RuleType ruleType = RuleType.ASSIGNMENT;
        int inPos = 0;
        while (inPos < rule.length())
        {
            char character = rule.charAt(inPos);
            if (Character.isWhitespace(character))
            {
                inPos++;
                continue;
            }
            switch (state)
            {
                case FIND_LHS:
                    if ('S' == character)
                    {
                        ruleType = RuleType.START;
                        inPos++;
                    }
                case FIND_ASSIGN:
                    break;
                case FIND_EXPR:
                    break;
                case FIND_RHS:
                    break;
                case MAY_UMINUS:
                    break;
                default:
                    break;
                    
            }
        }
        
    }
    
    

    /**
     * Check if a String begins with the text of a supplied String (ignoring case).
     * @param sought String; the sought pattern (NOT a regular expression)
     * @param supplied String; the String that might start with the <cite>sought</cite> string
     * @return boolean; true if the supplied String begins with the sought String (case insensitive)
     */
    private boolean stringBeginsWithIgnoreCase(final String sought, final String supplied)
    {
        if (sought.length() > supplied.length())
        {
            return false;
        }
        return (sought.equalsIgnoreCase(supplied.substring(0, sought.length())));
    }

    /**
     * Generate the key for a variable name and stream for use in this.variables.
     * @param name String; name of the variable
     * @param stream short; stream of the variable
     * @return String
     */
    private String variableKey(final String name, final short stream)
    {
        if (name.contains("\t"))
        {
            System.out.println("Whoops");
        }
        return String.format("%s%02d", name, stream);
    }

    /**
     * Lookup or create a new Variable.
     * @param name String; name of the variable
     * @param stream short; stream number of the variable
     * @param flags EnumSet&lt;Flags&gt;; flags of the variable
     * @param location String; description of the location in the TrafCOD file that triggered the call to this method
     * @return Variable; the new (or already existing) variable
     */
    private Variable installVariable(String name, short stream, EnumSet<Flags> flags, String location)
    {
        String key = variableKey(name, stream);
        Variable variable = this.variables.get(key);
        if (null == variable)
        {
            // Create and install a new variable
            variable = new Variable(name, stream);
            this.variables.put(key, variable);
        }
        if (flags.contains(Flags.START))
        {
            variable.setStartSource(location);
        }
        if (flags.contains(Flags.END))
        {
            variable.setEndSource(location);
        }
        return variable;
    }

    /**
     * Test code
     * @param args String; the command line arguments (not used)
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception
    {
        Evaluator evaluator = new Evaluator("file:///d:/cppb/trafcod/otsim/simpel.tfc");
    }

}

/**
 * Store a variable name, stream and isTimer status.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 6, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class NameAndStream
{
    /** The name. */
    final String name;

    /** The stream number. */
    short stream = 0;
    
    /** Number characters parsed. */
    int numberOfChars = 0;

    /**
     * Parse a name and stream.
     * @param text String; the name and stream
     * @throws Exception when <cite>text</cite> is not a valid TrafCOD variable name
     */
    public NameAndStream(final String text) throws Exception
    {
        int pos = 0;
        while(pos < text.length() && Character.isWhitespace(text.charAt(pos)))
        {
            pos++;
        }
        while (pos < text.length())
        {
            char character = text.charAt(pos);
            if (Character.isWhitespace(character) || '.' == character || '=' == character)
            {
                break;
            }
            pos++;
        }
        if (pos >= text.length())
        {
            throw new Exception("missing variable");
        }
        this.numberOfChars = pos;
        String trimmed = text.substring(0, pos).replaceAll(" ", "");
        if (trimmed.length() == 0)
        {
            throw new Exception("missing variable");
        }
        if (trimmed.matches("^D([Nn]?\\d\\d\\d)|(\\d\\d\\d[Nn])"))
        {
            // Handle a detector
            if (trimmed.charAt(1) == 'N' || trimmed.charAt(1) == 'n')
            {
                // Move the 'N' to the end
                trimmed = "D" + trimmed.substring(1, 3) + "N" + trimmed.substring(5);
            }
            this.name = "D" + trimmed.charAt(3);
            this.stream = (short) (10 * (trimmed.charAt(1) - '0') + trimmed.charAt(2) - '0');
            return;
        }
        else if (trimmed.matches("^T"))
        {
            trimmed = trimmed.substring(1);
        }
        StringBuilder nameBuilder = new StringBuilder();
        for (pos = 0; pos < trimmed.length(); pos++)
        {
            char nextChar = trimmed.charAt(pos);
            if (pos < trimmed.length() - 1 && Character.isDigit(nextChar) && Character.isDigit(trimmed.charAt(pos + 1)))
            {
                if (0 == pos || (1 == pos && trimmed.startsWith("N")))
                {
                    throw new Exception("Bad variable name: " + trimmed);
                }
                if (trimmed.charAt(pos - 1) == 'N')
                {
                    // Previous N was NOT part of the name
                    nameBuilder.deleteCharAt(nameBuilder.length() - 1);
                    // Move the 'N' after the digits
                    trimmed = trimmed.substring(0, pos - 2) + trimmed.substring(pos, 2) + "N" + trimmed.substring(pos + 2);
                }
                this.stream = (short) (10 * (trimmed.charAt(pos) - '0') + trimmed.charAt(pos + 1) - '0');
                pos++;
            }
            else
            {
                nameBuilder.append(nextChar);
            }
        }
        if (trimmed.endsWith("N"))
        {
            nameBuilder.deleteCharAt(nameBuilder.length() - 1);
        }
        this.name = nameBuilder.toString();
    }

    /**
     * Retrieve the stream number.
     * @return short; the stream number
     */
    public short getStream()
    {
        return this.stream;
    }

    /**
     * Retrieve the name.
     * @return String; the name (without the stream number)
     */
    public String getName()
    {
        return this.name;
    }

}

/**
 * Storage for a TrafCOD variable.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 5, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class Variable
{
    /** Flags. */
    EnumSet<Flags> flags = EnumSet.noneOf(Flags.class);

    /** The current value. */
    int value;

    /** Limit value (if this is a timer variable). */
    int timerMax10;

    /** Output color (if this is an export variable). */
    int newColor;

    /** Name of this variable (without the traffic stream). */
    final String name;

    /** Position in the debugging list. */
    char listPos;

    /** Traffic stream number */
    final short stream;

    /** Number of rules that refer to this variable. */
    int refCount;

    /** Time of last update in tenth of second. */
    int updateTime10;

    /** Source of start rule. */
    String startSource;

    /** Source of end rule. */
    String endSource;

    /**
     * Construct a new Variable.
     * @param name String; name of the new variable (without the stream number)
     * @param stream short; stream number to which the new Variable is associated
     */
    public Variable(final String name, final short stream)
    {
        this.name = name;
        this.stream = stream;
        if (name.startsWith("T"))
        {
            this.flags.add(Flags.IS_TIMER);
        }
        if(this.name.length() == 2 && this.name.startsWith("D") && Character.isDigit(this.name.charAt(1)))
        {
            this.flags.add(Flags.IS_DETECTOR);
        }
    }

    /**
     * Make this variable an output variable and set the output value.
     * @param outputValue int; the output value
     */
    public void setOutput(int outputValue)
    {
        this.newColor = outputValue;
        this.flags.add(Flags.IS_OUTPUT);
    }

    /**
     * Set the maximum time of this timer.
     * @param value10 int; the maximum time in 0.1 s
     * @throws Exception when this Variable is not a timer
     */
    public void setTimerMax(int value10) throws Exception
    {
        if (! this.flags.contains(Flags.IS_TIMER))
        {
            throw new Exception("Cannot set maximum timer value of " + selectedFieldsToString(EnumSet.of(PrintFlags.ID)));
        }
        this.timerMax10 = value10;
    }

    /**
     * Describe the rule that starts this variable.
     * @return String
     */
    public String getStartSource()
    {
        return this.startSource;
    }

    /**
     * Set the description of the rule that starts this variable.
     * @param startSource String; description of the rule that starts this variable
     */
    public void setStartSource(String startSource)
    {
        this.startSource = startSource;
    }

    /**
     * Describe the rule that ends this variable.
     * @return String
     */
    public String getEndSource()
    {
        return this.endSource;
    }

    /**
     * Set the description of the rule that ends this variable.
     * @param endSource String; description of the rule that ends this variable
     */
    public void setEndSource(String endSource)
    {
        this.endSource = endSource;
    }

    /**
     * Retrieve the stream to which this variable belongs.
     * @return short; the stream to which this variable belongs
     */
    public short getStream()
    {
        return this.stream;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Variable [" + selectedFieldsToString(EnumSet.of(PrintFlags.ID, PrintFlags.VALUE)) + "]";
    }

    /**
     * Print selected fields.
     * @param printFlags EnumSet&lt;PrintFlags&gt;; the set of fields to print
     * @return String
     */
    public String selectedFieldsToString(EnumSet<PrintFlags> printFlags)
    {
        StringBuilder result = new StringBuilder();
        if (printFlags.contains(PrintFlags.ID))
        {
            if (this.flags.contains(Flags.IS_DETECTOR))
            {
                result.append("D");
            }
            else if (printFlags.contains(PrintFlags.INITTIMER))
            {
                result.append("I");
                result.append(this.name);
            }
            else if (printFlags.contains(PrintFlags.REINITTIMER))
            {
                result.append("RI");
                result.append(this.name);
            }
            else
            {
                result.append(this.name);
            }
            if (this.stream > 0)
            {
                // Insert the stream BEFORE the first digit in the name (if any); otherwise append
                int pos;
                for (pos = 0; pos < result.length(); pos++)
                {
                    if (Character.isDigit(result.charAt(pos)))
                    {
                        break;
                    }
                }
                result.insert(pos, String.format("%02d", this.stream));
            }
            if (this.flags.contains(Flags.IS_DETECTOR))
            {
                result.append(this.name.substring(1));
            }
            if (printFlags.contains(PrintFlags.NEGATED))
            {
                result.append("N");
            }
        }
        int printValue = Integer.MIN_VALUE; // That should stand out if not changed by the code below this line.
        if (printFlags.contains(PrintFlags.VALUE))
        {
            if (printFlags.contains(PrintFlags.NEGATED))
            {
                printValue = 0 == this.value ? 1 : 0;
            }
            else
            {
                printValue = this.value;
            }
            if (printFlags.contains(PrintFlags.S))
            {
                if (this.flags.contains(Flags.START))
                {
                    printValue = 1;
                }
                else
                {
                    printValue = 0;
                }
            }
            if (printFlags.contains(PrintFlags.E))
            {
                if (this.flags.contains(Flags.END))
                {
                    printValue = 1;
                }
                else
                {
                    printValue = 0;
                }
            }
        }
        if (printFlags.contains(PrintFlags.VALUE) || printFlags.contains(PrintFlags.S) || printFlags.contains(PrintFlags.E)
                || printFlags.contains(PrintFlags.FLAGS))
        {
            result.append("<");
            if (printFlags.contains(PrintFlags.VALUE) || printFlags.contains(PrintFlags.S) || printFlags.contains(PrintFlags.E))
            {
                result.append(printValue);
            }
            if (printFlags.contains(PrintFlags.FLAGS))
            {
                if (this.flags.contains(Flags.START))
                {
                    result.append("S");
                }
                if (this.flags.contains(Flags.END))
                {
                    result.append("E");
                }
            }
            result.append(">");
        }
        if (printFlags.contains(PrintFlags.MODIFY_TIME))
        {
            result.append(String.format(" (%d.%d)", this.updateTime10 / 10, this.updateTime10 % 10));
        }
        return result.toString();
    }

}

/**
 * Flags for toString method of a Variable.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 6, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
enum PrintFlags
{
    /** The name and stream of the Variable. */
    ID,
    /** The current Variable. */
    VALUE,
    /** Print "I" before the name (to indicate that a timer is initialized). */
    INITTIMER,
    /** Print "RI" before the name (to indicate that a timer is re-initialized). */
    REINITTIMER,
    /** Print "1" if just set, else print "0". */
    S,
    /** Print "1" if just reset, else print "0". */
    E,
    /** Print the negated Variable. */
    NEGATED,
    /** Print the flags of the Variable. */
    FLAGS,
    /** Print the time of last modification of the Variable. */
    MODIFY_TIME,
}

/**
 * Flags of a TrafCOD variable.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 6, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
enum Flags
{
    /** Variable becomes active. */
    START,
    /** Variable becomes inactive. */
    END,
    /** Timer has just expired. */
    TIMEREXPIRED,
    /** Variable has just changed value. */
    CHANGED,
    /** Variable is a timer. */
    IS_TIMER,
    /** Variable is a detector. */
    IS_DETECTOR,
    /** Variable has a start rule. */
    HAS_START_RULE,
    /** Variable has an end rule. */
    HAS_END_RULE,
    /** Variable is an output. */
    IS_OUTPUT,
    /** Variable must be initialized to 1 at start of control program. */
    INITED,
}
