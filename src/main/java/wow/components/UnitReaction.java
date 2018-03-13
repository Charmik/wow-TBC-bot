package wow.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Cargeh
 */
public enum UnitReaction {

    NEUTRAL,
    FRIENDLY,
    HOSTILE;

    private static Logger logger = LoggerFactory.getLogger(UnitReaction.class);

    public static UnitReaction getReaction(
        Faction first,
        Faction second)
    {
        boolean friendly = isFriendly(first, second);
        boolean hostile = isHostile(first, second);

        if (!friendly && !hostile)
            return NEUTRAL;
        else if (friendly)
            return FRIENDLY;
        else
            return HOSTILE;
    }

    /**
     * @return true if faction A is friendly to B
     */
    private static boolean isFriendly(
        Faction first,
        Faction second)
    {
        //TODO: it throws NPE sometimes
        if (first == null || second == null) {
            logger.debug("method isFrienly have nullable params, first=" + first + ", second=" + second);
            return true;
        }
        return (first.ourMask & second.friendlyMask) != 0;
    }

    /**
     * @return true if faction A is hostile to B
     */
    private static boolean isHostile(
        Faction first,
        Faction second)
    {
        if (first == null || second == null) {
            logger.debug("method isHostile have nullable params, first=" + first + ", second=" + second);
            return true;
        }
        return (first.ourMask & second.hostileMask) != 0;
    }

    public boolean canAttack() {
        return (this == UnitReaction.HOSTILE) || (this == UnitReaction.NEUTRAL);
    }
}
