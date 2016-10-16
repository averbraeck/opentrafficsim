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

import nl.tudelft.simulation.language.Throw;

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

    /** The tokenised rules. */
    final List<Object[]> tokenisedRules = new ArrayList<>();

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

    /** Maximum number of evaluation loops. */
    private int maxLoopCount = 10;

    /** Position in current expression. */
    private int currentToken;

    /** The expression evaluation stack. */
    private List<Integer> stack = new ArrayList<Integer>();

    /** Rule that is currently being evaluated. */
    private Object[] currentRule;

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
                NameAndStream nameAndStream = new NameAndStream(fields[0], locationDescription);
                installVariable(nameAndStream.getName(), nameAndStream.getStream(), EnumSet.noneOf(Flags.class),
                        locationDescription);
                // The supplied initial value is ignored (in this version of the TrafCOD interpreter)!
                continue;
            }
            if (stringBeginsWithIgnoreCase(TIME_PREFIX, trimmedLine))
            {
                String timerNameAndMaximumValue = trimmedLine.substring(INIT_PREFIX.length()).trim().replaceAll("[ \t]+", " ");
                String[] fields = timerNameAndMaximumValue.split(" ");
                NameAndStream nameAndStream = new NameAndStream(fields[0], locationDescription);
                Variable variable =
                        installVariable(nameAndStream.getName(), nameAndStream.getStream(), EnumSet.noneOf(Flags.class),
                                locationDescription);
                int value10 = Integer.parseInt(fields[1]);
                variable.setTimerMax(value10);
                continue;
            }
            if (stringBeginsWithIgnoreCase(EXPORT_PREFIX, trimmedLine))
            {
                String varNameAndOutputValue = trimmedLine.substring(EXPORT_PREFIX.length()).trim().replaceAll("[ \t]+", " ");
                String[] fields = varNameAndOutputValue.split(" ");
                NameAndStream nameAndStream = new NameAndStream(fields[0], locationDescription);
                Variable variable =
                        installVariable(nameAndStream.getName(), nameAndStream.getStream(), EnumSet.noneOf(Flags.class),
                                locationDescription);
                int value = Integer.parseInt(fields[1]);
                variable.setOutput(value);
                continue;
            }
            this.trafcodRules.add(trimmedLine);
            Object[] tokenisedRule = parse(trimmedLine, locationDescription);
            if (null != tokenisedRule)
            {
                this.tokenisedRules.add(tokenisedRule);
                // for (Object o : tokenisedRule)
                // {
                // System.out.print(o + " ");
                // }
                // System.out.println("");
                String untokenised = printRule(tokenisedRule, false);
                System.out.println(untokenised);
            }
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
     * Evaluate all expressions until no more changes occur.
     * @return int; number of iteration loops performed
     * @throws Exception when evalution of a fule fails
     */
    private int evalExprs() throws Exception
    {
        for (int loop = 0; loop < this.maxLoopCount; loop++)
        {
            if (evalExpressionsOnce() == 0)
            {
                return loop;
            }
        }
        return this.maxLoopCount;
    }

    /**
     * Evaluate all expressions and return the number of changed variables.
     * @return int; the number of changed variables
     * @throws Exception when evaluation of a rule fails
     */
    private int evalExpressionsOnce() throws Exception
    {
        for (Variable variable : this.variables.values())
        {
            variable.clearChangedFlag();
        }
        int changeCount = 0;
        for (Object[] rule : this.tokenisedRules)
        {
            if (evalRule(rule))
            {
                changeCount++;
            }
        }
        return changeCount;
    }

    /**
     * Evaluate a rule.
     * @param rule Object[]; the tokenised rule
     * @return boolean; true if the variable that is affected by the rule has changed; false if no variable was changed
     * @throws Exception when evaluation of the rule fails
     */
    private boolean evalRule(final Object[] rule) throws Exception
    {
        Token ruleType = (Token) rule[0];
        Variable destination = (Variable) rule[1];
        if (destination.isTimer())
        {
            if (destination.getFlags().contains(Flags.TIMEREXPIRED))
            {
                destination.clearFlag(Flags.TIMEREXPIRED);
                destination.setFlag(Flags.END);
            }
            else if (destination.getFlags().contains(Flags.START) || destination.getFlags().contains(Flags.END))
            {
                destination.clearFlag(Flags.START);
                destination.clearFlag(Flags.END);
                destination.setFlag(Flags.CHANGED);
            }
        }
        else
        {
            // Normal Variable or detector
            if (Token.START_RULE == ruleType)
            {
                destination.clearFlag(Flags.START);
            }
            else if (Token.END_RULE == ruleType)
            {
                destination.clearFlag(Flags.END);
            }
            else
            {
                destination.clearFlag(Flags.START);
                destination.clearFlag(Flags.END);
            }
        }

        int currentValue = destination.getValue();
        if (Token.START_RULE == ruleType && currentValue != 0 || Token.END == ruleType && currentValue == 0
                || Token.INIT_TIMER == ruleType && currentValue != 0)
        {
            return false; // Value cannot change from zero to nonzero or vice versa due to evaluating the expression
        }
        this.currentRule = rule;
        this.currentToken = 2; // Point to first token of the RHS
        this.stack.clear();
        evalExpr(0);

        return false;
    }

    /** Binding strength of unary minus. */
    private static int BIND_UNARY_MINUS = 4;

    /**
     * Evaluate an expression.
     * @param bindingStrength int; the binding strength of a not yet applied binary operator (higher value must be applied
     *            first)
     * @throws Exception when the expression is not valid
     */
    private void evalExpr(final int bindingStrength) throws Exception
    {
        if (this.currentToken >= this.currentRule.length)
        {
            throw new Exception("Missing operand at end of expression " + printRule(this.currentRule, false));
        }
        Token token = (Token) this.currentRule[this.currentToken++];
        Object nextToken = null;
        if (this.currentToken < this.currentRule.length)
        {
            nextToken = this.currentRule[this.currentToken];
        }
        switch (token)
        {
            case UNARY_MINUS:
                if (Token.OPEN_PAREN != nextToken && Token.VARIABLE != nextToken && Token.NEG_VARIABLE != nextToken
                        && Token.CONSTANT != nextToken && Token.START != nextToken && Token.END != nextToken)
                {
                    throw new Exception("Operand expected after unary minus");
                }
                evalExpr(BIND_UNARY_MINUS);
                push(-pop());
                break;

            case OPEN_PAREN:
                evalExpr(0);
                if (Token.CLOSE_PAREN != this.currentRule[this.currentToken])
                {
                    throw new Exception("Missing closing parenthesis");
                }
                this.currentToken++;
                break;

            case START:
                if (!(nextToken instanceof Variable))
                {
                    throw new Exception("Missing variable after S");
                }
                push(((Variable) nextToken).getFlags().contains(Flags.START) ? 1 : 0);
                this.currentToken++;
                break;

            case END:
                if (!(nextToken instanceof Variable))
                {
                    throw new Exception("Missing variable after E");
                }
                push(((Variable) nextToken).getFlags().contains(Flags.END) ? 1 : 0);
                this.currentToken++;
                break;

            case VARIABLE:
            {
                Variable operand = (Variable) nextToken;
                if (operand.isTimer())
                {
                    push(operand.getValue() == 0 ? 0 : 1);
                }
                else
                {
                    push(operand.getValue());
                }
                this.currentToken++;
                break;
            }

            case CONSTANT:
                push((Integer) nextToken);
                this.currentToken++;
                break;

            case NEG_VARIABLE:
                push(-((Variable) nextToken).getValue());
                this.currentToken++;
                break;

            default:
                throw new Exception("Operand missing");
        }
        evalRHS(bindingStrength);
    }
    
    private void evalRHS(final int bindingStrength)
    {
        
    }

    /**
     * Push a value on the evaluation stack.
     * @param value int; the value to push on the evaluation stack
     */
    private void push(final int value)
    {
        this.stack.add(value);
    }

    /**
     * Remove the last not-yet-removed value from the evaluation stack and return it.
     * @return int; the last non-yet-removed value on the evaluation stack
     * @throws Exception when the stack is empty
     */
    private int pop() throws Exception
    {
        if (this.stack.size() < 1)
        {
            throw new Exception("Stack empty");
        }
        return this.stack.remove(this.stack.size() - 1);
    }

    /**
     * Print a tokenised rule.
     * @param tokens Object[]; the tokens
     * @param printValues boolean; if true; print the values of all encountered variable; if false; do not print the values of
     *            all encountered variable
     * @return String; a textual approximation of the original rule
     * @throws Exception when <cite>tokens</cite> does not match the expected grammar
     */
    private String printRule(Object[] tokens, final boolean printValues) throws Exception
    {
        StringBuilder result = new StringBuilder();
        for (int inPos = 0; inPos < tokens.length; inPos++)
        {
            Object token = tokens[inPos];
            if (token instanceof Token)
            {
                switch ((Token) token)
                {
                    case EQUALS_RULE:
                        result.append(((Variable) tokens[++inPos]).selectedFieldsToString(EnumSet.of(PrintFlags.ID)));
                        result.append("=");
                        break;
                    case NEG_EQUALS_RULE:
                        result.append(((Variable) tokens[++inPos]).selectedFieldsToString(EnumSet.of(PrintFlags.ID,
                                PrintFlags.NEGATED)));
                        result.append("=");
                        break;
                    case START_RULE:
                        result.append(((Variable) tokens[++inPos]).selectedFieldsToString(EnumSet.of(PrintFlags.ID)));
                        result.append(".=");
                        break;
                    case END_RULE:
                        result.append(((Variable) tokens[++inPos]).selectedFieldsToString(EnumSet.of(PrintFlags.ID,
                                PrintFlags.NEGATED)));
                        result.append(".=");
                        break;
                    case INIT_TIMER:
                        result.append(((Variable) tokens[++inPos]).selectedFieldsToString(EnumSet.of(PrintFlags.ID,
                                PrintFlags.INITTIMER)));
                        result.append(".=");
                        break;
                    case REINIT_TIMER:
                        result.append(((Variable) tokens[++inPos]).selectedFieldsToString(EnumSet.of(PrintFlags.ID,
                                PrintFlags.REINITTIMER)));
                        result.append(".=");
                        break;
                    case START:
                        result.append("S");
                        break;
                    case END:
                        result.append("E");
                        break;
                    case VARIABLE:
                        result.append(((Variable) tokens[++inPos]).selectedFieldsToString(EnumSet.of(PrintFlags.ID)));
                        break;
                    case NEG_VARIABLE:
                        result.append(((Variable) tokens[++inPos]).selectedFieldsToString(EnumSet.of(PrintFlags.ID,
                                PrintFlags.NEGATED)));
                        break;
                    case CONSTANT:
                        result.append(tokens[++inPos]).toString();
                        break;
                    case UNARY_MINUS:
                    case MINUS:
                        result.append("-");
                        break;
                    case PLUS:
                        result.append("+");
                        break;
                    case TIMES:
                        result.append(".");
                        break;
                    case EQ:
                        result.append("=");
                        break;
                    case NOTEQ:
                        result.append("<>");
                        break;
                    case GT:
                        result.append(">");
                        break;
                    case GTEQ:
                        result.append(">=");
                        break;
                    case LE:
                        result.append("<");
                        break;
                    case LEEQ:
                        result.append("<=");
                        break;
                    case OPEN_PAREN:
                        result.append("(");
                        break;
                    case CLOSE_PAREN:
                        result.append(")");
                        break;
                    default:
                        System.out.println("<<<ERROR>>> encountered a non-Token object: " + token + " after "
                                + result.toString());
                        throw new Exception("Unknown token");

                }
            }
            else
            {
                System.out.println("<<<ERROR>>> encountered a non-Token object: " + token + " after " + result.toString());
                throw new Exception("Not a token");
            }
        }
        return result.toString();
    }

    /**
     * States of the rule parser.
     * <p>
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

    /**
     * Types of TrafCOD tokens.
     * <p>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    enum Token
    {
        /** Equals rule. */
        EQUALS_RULE,
        /** Not equals rule. */
        NEG_EQUALS_RULE,
        /** Assignment rule. */
        ASSIGNMENT,
        /** Start rule. */
        START_RULE,
        /** End rule. */
        END_RULE,
        /** Timer initialize rule. */
        INIT_TIMER,
        /** Timer re-initialize rule. */
        REINIT_TIMER,
        /** Unary minus operator. */
        UNARY_MINUS,
        /** Less than or equal to ("<="). */
        LEEQ,
        /** Not equal to ("<>"). */
        NOTEQ,
/** Less than ("<"). */
        LE,
        /** Greater than or equal to (">="). */
        GTEQ,
        /** Greater than (">"). */
        GT,
        /** Equals to ("="). */
        EQ,
        /** True if following variable has just started. */
        START,
        /** True if following variable has just ended. */
        END,
        /** Variable follows. */
        VARIABLE,
        /** Variable that follows must be logically negated. */
        NEG_VARIABLE,
        /** Integer follows. */
        CONSTANT,
        /** Addition operator. */
        PLUS,
        /** Subtraction operator. */
        MINUS,
        /** Multiplication operator. */
        TIMES,
        /** Opening parenthesis. */
        OPEN_PAREN,
        /** Closing parenthesis. */
        CLOSE_PAREN,
    }

    /**
     * Parse one TrafCOD rule.
     * @param rawRule String; the TrafCOD rule
     * @param locationDescription String; description of the location (file, line) where the rule was found
     * @return Object[]; array filled with the tokenised rule
     * @throws Exception when the rule is not a valid TrafCOD rule
     */
    private Object[] parse(final String rawRule, final String locationDescription) throws Exception
    {
        if (rawRule.length() == 0)
        {
            throw new Exception("empty rule at " + locationDescription);
        }
        ParserState state = ParserState.FIND_LHS;
        String rule = rawRule.toUpperCase(Locale.US);
        Token ruleType = Token.ASSIGNMENT;
        int inPos = 0;
        NameAndStream lhsNameAndStream = null;
        List<Object> tokens = new ArrayList<>();
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
                {
                    if ('S' == character)
                    {
                        ruleType = Token.START_RULE;
                        inPos++;
                        lhsNameAndStream = new NameAndStream(rule.substring(inPos), locationDescription);
                        inPos += lhsNameAndStream.getNumberOfChars();
                    }
                    else if ('E' == character)
                    {
                        ruleType = Token.END_RULE;
                        inPos++;
                        lhsNameAndStream = new NameAndStream(rule.substring(inPos), locationDescription);
                        inPos += lhsNameAndStream.getNumberOfChars();
                    }
                    else if ('I' == character && 'T' == rule.charAt(inPos + 1))
                    {
                        ruleType = Token.INIT_TIMER;
                        inPos++; // The 'T' is part of the name of the time; do not consume it
                        lhsNameAndStream = new NameAndStream(rule.substring(inPos), locationDescription);
                        inPos += lhsNameAndStream.getNumberOfChars();
                    }
                    else if ('R' == character && 'I' == rule.charAt(inPos + 1) && 'T' == rule.charAt(inPos + 2))
                    {
                        ruleType = Token.REINIT_TIMER;
                        inPos += 2; // The 'T' is part of the name of the time; do not consume it
                        lhsNameAndStream = new NameAndStream(rule.substring(inPos), locationDescription);
                        inPos += lhsNameAndStream.getNumberOfChars();
                    }
                    else if ('T' == character && rule.indexOf('=') >= 0
                            && (rule.indexOf('N') < 0 || rule.indexOf('N') > rule.indexOf('=')))
                    {
                        throw new Exception("Bad time initialization at " + locationDescription);
                    }
                    else
                    {
                        ruleType = Token.EQUALS_RULE;
                        lhsNameAndStream = new NameAndStream(rule.substring(inPos), locationDescription);
                        inPos += lhsNameAndStream.getNumberOfChars();
                        if (lhsNameAndStream.isNegated())
                        {
                            ruleType = Token.NEG_EQUALS_RULE;
                        }
                    }
                    state = ParserState.FIND_ASSIGN;
                    break;
                }
                case FIND_ASSIGN:
                {
                    if ('.' == character && '=' == rule.charAt(inPos + 1))
                    {
                        if (Token.EQUALS_RULE == ruleType)
                        {
                            ruleType = Token.START_RULE;
                        }
                        else if (Token.NEG_EQUALS_RULE == ruleType)
                        {
                            ruleType = Token.END_RULE;
                        }
                        inPos += 2;
                    }
                    else if ('=' == character)
                    {
                        if (Token.START_RULE == ruleType || Token.END_RULE == ruleType || Token.INIT_TIMER == ruleType
                                || Token.REINIT_TIMER == ruleType)
                        {
                            throw new Exception("Bad assignment at " + locationDescription);
                        }
                        inPos++;
                    }
                    tokens.add(ruleType);
                    EnumSet<Flags> lhsFlags = EnumSet.noneOf(Flags.class);
                    if (Token.START_RULE == ruleType || Token.EQUALS_RULE == ruleType || Token.NEG_EQUALS_RULE == ruleType
                            || Token.INIT_TIMER == ruleType || Token.REINIT_TIMER == ruleType)
                    {
                        lhsFlags.add(Flags.HAS_START_RULE);
                    }
                    if (Token.END_RULE == ruleType || Token.EQUALS_RULE == ruleType || Token.NEG_EQUALS_RULE == ruleType)
                    {
                        lhsFlags.add(Flags.HAS_END_RULE);
                    }
                    Variable lhsVariable =
                            installVariable(lhsNameAndStream.getName(), lhsNameAndStream.getStream(), lhsFlags,
                                    locationDescription);
                    tokens.add(lhsVariable);
                    state = ParserState.MAY_UMINUS;
                    break;
                }
                case MAY_UMINUS:
                    if ('-' == character)
                    {
                        tokens.add(Token.UNARY_MINUS);
                        inPos++;
                    }
                    state = ParserState.FIND_EXPR;
                    break;

                case FIND_EXPR:
                {
                    if (Character.isDigit(character))
                    {
                        int constValue = 0;
                        while (inPos < rule.length() && Character.isDigit(rule.charAt(inPos)))
                        {
                            int digit = rule.charAt(inPos) - '0';
                            if (constValue >= (Integer.MAX_VALUE - digit) / 10)
                            {
                                throw new Exception("Number too large at " + locationDescription);
                            }
                            constValue = 10 * constValue + digit;
                            inPos++;
                        }
                        tokens.add(Token.CONSTANT);
                        tokens.add(new Integer(constValue));
                    }
                    if (inPos >= rule.length())
                    {
                        return tokens.toArray();
                    }
                    character = rule.charAt(inPos);
                    switch (character)
                    {
                        case '+':
                            tokens.add(Token.PLUS);
                            inPos++;
                            break;
                        case '-':
                            tokens.add(Token.MINUS);
                            inPos++;
                            break;
                        case '.':
                            tokens.add(Token.TIMES);
                            inPos++;
                            break;
                        case ')':
                            tokens.add(Token.CLOSE_PAREN);
                            inPos++;
                            break;

                        case '<':
                        {
                            Character nextChar = rule.charAt(++inPos);
                            if ('=' == nextChar)
                            {
                                tokens.add(Token.LEEQ);
                                inPos++;
                            }
                            else if ('>' == nextChar)
                            {
                                tokens.add(Token.NOTEQ);
                                inPos++;
                            }
                            else
                            {
                                tokens.add(Token.LE);
                            }
                            break;
                        }
                        case '>':
                        {
                            Character nextChar = rule.charAt(++inPos);
                            if ('=' == nextChar)
                            {
                                tokens.add(Token.GTEQ);
                                inPos++;
                            }
                            else if ('<' == nextChar)
                            {
                                tokens.add(Token.NOTEQ);
                                inPos++;
                            }
                            else
                            {
                                tokens.add(Token.GT);
                            }
                            break;
                        }
                        case '=':
                        {
                            Character nextChar = rule.charAt(++inPos);
                            if ('<' == nextChar)
                            {
                                tokens.add(Token.LEEQ);
                                inPos++;
                            }
                            else if ('>' == nextChar)
                            {
                                tokens.add(Token.GTEQ);
                                inPos++;
                            }
                            else
                            {
                                tokens.add(Token.EQ);
                            }
                            break;
                        }
                        case '(':
                        {
                            inPos++;
                            tokens.add(Token.OPEN_PAREN);
                            state = ParserState.MAY_UMINUS;
                            break;
                        }
                        default:
                        {
                            if ('S' == character)
                            {
                                tokens.add(Token.START);
                                inPos++;
                            }
                            else if ('E' == character)
                            {
                                tokens.add(Token.END);
                                inPos++;
                            }
                            NameAndStream nas = new NameAndStream(rule.substring(inPos), locationDescription);
                            inPos += nas.getNumberOfChars();
                            if (nas.isNegated())
                            {
                                tokens.add(Token.NEG_VARIABLE);
                            }
                            else
                            {
                                tokens.add(Token.VARIABLE);
                            }
                            Variable variable =
                                    installVariable(nas.getName(), nas.getStream(), EnumSet.noneOf(Flags.class),
                                            locationDescription);
                            variable.incrementReferenceCount();
                            tokens.add(variable);
                        }
                    }
                    break;
                }
                default:
                    throw new Exception("Error: bad switch; case " + state + " should not happen");
            }
        }
        return tokens.toArray();

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
     * @param flags EnumSet&lt;Flags&gt;; some (possibly empty) combination of Flags.HAS_START_RULE and Flags.HAS_END_RULE; no
     *            other flags are allowed
     * @param location String; description of the location in the TrafCOD file that triggered the call to this method
     * @return Variable; the new (or already existing) variable
     * @throws Exception if the variable already exists and already has (one of) the specified flag(s)
     */
    private Variable installVariable(String name, short stream, EnumSet<Flags> flags, String location) throws Exception
    {
        EnumSet<Flags> forbidden = EnumSet.complementOf(EnumSet.of(Flags.HAS_START_RULE, Flags.HAS_END_RULE));
        EnumSet<Flags> badFlags = EnumSet.copyOf(forbidden);
        badFlags.retainAll(flags);
        if (badFlags.size() > 0)
        {
            throw new Exception("installVariable was called with wrong flag(s): " + badFlags);
        }
        String key = variableKey(name, stream);
        Variable variable = this.variables.get(key);
        if (null == variable)
        {
            // Create and install a new variable
            variable = new Variable(name, stream);
            this.variables.put(key, variable);
        }
        if (flags.contains(Flags.HAS_START_RULE))
        {
            variable.setStartSource(location);
        }
        if (flags.contains(Flags.HAS_END_RULE))
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
    private final String name;

    /** The stream number. */
    private short stream = -1;

    /** Number characters parsed. */
    private int numberOfChars = 0;

    /** Was a letter N consumed while parsing the name?. */
    private boolean negated = false;

    /**
     * Parse a name and stream.
     * @param text String; the name and stream
     * @param locationDescription String; description of the location in the input file
     * @throws Exception when <cite>text</cite> is not a valid TrafCOD variable name
     */
    public NameAndStream(final String text, final String locationDescription) throws Exception
    {
        int pos = 0;
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos)))
        {
            pos++;
        }
        while (pos < text.length())
        {
            char character = text.charAt(pos);
            if (!Character.isLetterOrDigit(character))
            {
                break;
            }
            // if (Character.isWhitespace(character) || '.' == character || '=' == character)
            // {
            // break;
            // }
            pos++;
        }
        this.numberOfChars = pos;
        String trimmed = text.substring(0, pos).replaceAll(" ", "");
        if (trimmed.length() == 0)
        {
            throw new Exception("missing variable at " + locationDescription);
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
        // else if (trimmed.matches("^T"))
        // {
        // trimmed = trimmed.substring(1);
        // }
        StringBuilder nameBuilder = new StringBuilder();
        for (pos = 0; pos < trimmed.length(); pos++)
        {
            char nextChar = trimmed.charAt(pos);
            if (pos < trimmed.length() - 1 && Character.isDigit(nextChar) && Character.isDigit(trimmed.charAt(pos + 1))
                    && -1 == this.stream)
            {
                if (0 == pos || (1 == pos && trimmed.startsWith("N")))
                {
                    throw new Exception("Bad variable name: " + trimmed + " at " + locationDescription);
                }
                if (trimmed.charAt(pos - 1) == 'N')
                {
                    // Previous N was NOT part of the name
                    nameBuilder.deleteCharAt(nameBuilder.length() - 1);
                    // Move the 'N' after the digits
                    trimmed =
                            trimmed.substring(0, pos - 2) + trimmed.substring(pos, pos + 2) + "N" + trimmed.substring(pos + 2);
                    pos -= 2;
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
            this.negated = true;
        }
        this.name = nameBuilder.toString();
    }

    /**
     * Was a negation operator ('N') embedded in the name?
     * @return boolean
     */
    public boolean isNegated()
    {
        return this.negated;
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

    /**
     * Retrieve the number of characters consumed from the input.
     * @return int; the number of characters consumed from the input
     */
    public int getNumberOfChars()
    {
        return this.numberOfChars;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "NameAndStream [name=" + this.name + ", stream=" + this.stream + ", numberOfChars=" + this.numberOfChars
                + ", negated=" + this.negated + "]";
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
        if (this.name.length() == 2 && this.name.startsWith("D") && Character.isDigit(this.name.charAt(1)))
        {
            this.flags.add(Flags.IS_DETECTOR);
        }
    }

    /**
     * Retrieve the current value of this Variable.
     * @return int; the value of this Variable
     */
    public int getValue()
    {
        return this.value;
    }

    /**
     * Set one flag.
     * @param flag Flags
     */
    public void setFlag(final Flags flag)
    {
        this.flags.add(flag);
    }

    /**
     * Clear one flag.
     * @param flag Flags; the flag to clear
     */
    public void clearFlag(final Flags flag)
    {
        this.flags.remove(flag);
    }

    /**
     * Report whether this Variable is a timer.
     * @return boolean; true if this Variable is a timer; false if this variable is not a timer
     */
    public boolean isTimer()
    {
        return this.flags.contains(Flags.IS_TIMER);
    }

    /**
     * Clear the CHANGED flag of this Variable.
     */
    public void clearChangedFlag()
    {
        this.flags.remove(Flags.CHANGED);
    }

    /**
     * Increment the reference counter of this variable. The reference counter counts the number of rules where this variable
     * occurs on the right hand side of the assignment operator.
     */
    public void incrementReferenceCount()
    {
        this.refCount++;
    }

    /**
     * Return a safe copy of the flags.
     * @return EnumSet&lt;Flags&gt;
     */
    public EnumSet<Flags> getFlags()
    {
        return EnumSet.copyOf(this.flags);
    }

    /**
     * Set a flag of this Variable.
     * @param flag Flags; the flag to set
     */
    public void addFlag(final Flags flag)
    {
        this.flags.add(flag);
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
        if (!this.flags.contains(Flags.IS_TIMER))
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
     * @throws Exception when a start source has already been set
     */
    public void setStartSource(String startSource) throws Exception
    {
        if (null != this.startSource)
        {
            throw new Exception("Conflicting rules: " + this.startSource + " vs " + startSource);
        }
        this.startSource = startSource;
        this.flags.add(Flags.HAS_START_RULE);
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
     * @throws Exception when an end source has already been set
     */
    public void setEndSource(String endSource) throws Exception
    {
        if (null != this.endSource)
        {
            throw new Exception("Conflicting rules: " + this.startSource + " vs " + endSource);
        }
        this.endSource = endSource;
        this.flags.add(Flags.HAS_END_RULE);
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
