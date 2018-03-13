package wow.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cargeh
 */
public enum CharacterState {
    LOOTING(1032),
    STATELESS(8), // seems to be the default state for when nothing happens
    UNKNOWN(-1);

    private static final Map<Integer, CharacterState> states;

    static {
        Map<Integer, CharacterState> tempStates = new HashMap<>();
        for (CharacterState state : CharacterState.values()) {
            tempStates.put(state.stateValue, state);
        }
        states = Collections.unmodifiableMap(tempStates);
    }

    private final int stateValue;

    CharacterState(int stateValue) {
        this.stateValue = stateValue;
    }

    public static CharacterState getState(int stateValue) {
        CharacterState state = states.get(stateValue);
        if (state == null) {
            state = UNKNOWN;
            System.err.println("Unknown state: " + stateValue);
        }
        return state;
    }
}
