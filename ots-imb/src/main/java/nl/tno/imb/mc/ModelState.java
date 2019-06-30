package nl.tno.imb.mc;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The possible states of a model.
 */
public enum ModelState
{

    /** Ready. */
    READY(0),
    /** Calculating. */
    CALCULATING(1),
    /** Busy. */
    BUSY(2),
    /** Model is available for use. */
    IDLE(3),
    /** Claim model for use in this session. */
    LOCK(4),
    /** Free model to be used in other session. */
    UNLOCK(5),
    /** Force termination of model. */
    // TODO TERMINATE(6),
    /** Model is no longer available. */
    REMOVED(-1);

    /** Map to translate numeric value to enum. */
    protected static Map<Integer, ModelState> commandMap = new LinkedHashMap<>();

    static
    {
        for (ModelState modelState : values())
        {
            commandMap.put(modelState.getValue(), modelState);
        }
    }

    /**
     * Construct a ModelState.
     * @param value int; the result of the getValue method
     */
    private ModelState(final int value)
    {
        this.value = value;
    }

    /**
     * Retrieve the integer value that represents this ModelState for transmission over IMB.
     * @return int; the value that represents this ModelState in transmission over IMB
     */
    public int getValue()
    {
        return this.value;
    }

    /** The value that represents this ModalState when being transmitted over IMB. */
    private final int value;

    /**
     * Lookup the ModelState that corresponds to the value.
     * @param value int; the value to look up
     * @return ModelState; the ModelState that corresponds to the value, or null if no ModelState with the specified value is
     *         defined
     */
    protected static ModelState byValue(final int value)
    {
        return commandMap.get(value);
    }

}
