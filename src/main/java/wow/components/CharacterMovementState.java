package wow.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cargeh
 */
public enum CharacterMovementState {
    IDLE_STANDING(0),
    RUNNING_FORWARD(1),
    UNKNOWN(-1);

    private static final Map<Short, CharacterMovementState> states;

    static {
        Map<Short, CharacterMovementState> tempStates = new HashMap<>();
        for (CharacterMovementState state : CharacterMovementState.values()) {
            tempStates.put(state.stateValue, state);
        }
        states = Collections.unmodifiableMap(tempStates);
    }

    private final short stateValue;

    CharacterMovementState(int stateValue) {
        this.stateValue = (short) stateValue;
    }

    public static CharacterMovementState getState(short stateValue) {
        CharacterMovementState state = states.get(stateValue);
        if (state == null)
            state = UNKNOWN;
        return state;
    }
}
