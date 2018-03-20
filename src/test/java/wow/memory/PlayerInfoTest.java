package wow.memory;

import wow.BaseTest;


/**
 * @author Cargeh
 */
public class PlayerInfoTest extends BaseTest {


    private static void printf(
        String format,
        Object... values)
    {
        System.out.printf(format + System.lineSeparator(), values);
    }

    /**
     * Mouse cursor states:
     * - 29: hovering over hostile mob, out of range
     * - 4: hovering over hostile mob, in range
     */
    /*
    @Test
    public void testStaticValues() {
        printf("-------- STATIC VALUES ---------");
        printf("Account name: %s", player.getAccountName());
        printf("Is in game: %b", player.isInGame());
        printf("Name: %s", player.getName());
        printf("Zone id: %d", player.getZoneId());
        printf("Level: %d", player.getLevel());
        printf("Angle: %f", player.getAngle());
        printf("Mouse cursor state: %d", player.getMouseCursorState());
        player.getTarget().ifPresent(wowObject -> printf("Target GUID: %d", wowObject.getGuid()));
        printf("Vendor opened window ID: %d", player.getVendorWindowId());
    }
    */

    /**
     * Movement states:
     * - 1: running forward
     * - 2: running backwards
     */
    /*
    @Test
    public void testDymanic() {
        printf("-------- DYNAMIC VALUES ---------");
        printf("Health: %d", player.getHealth());
        printf("Health%: %d", player.getHealthPercent());
        printf("Guid: %d", player.getGuid());

        printf("Mana: %d", player.getMana());
        printf("Mana%: %d", player.getManaPercent());
        printf("Energy: %d", player.getEnergy());
        printf("Rage: %d", player.getRage());

        printf("Is casting: %b", player.isCasting());
        printf("Faction: %s", player.getFaction());
        printf("Casting spell id: %d", player.getCastingSpellId());
        printf("Movement state: %s", player.getMovementState());
        printf("State: %s", player.getState());

        Point2D.Double coordinates = player.getMapCoordinates();
        printf("Player X position: %f", coordinates.getX());
        printf("Player Y position: %f", coordinates.getY());

        printf("Player WORLD X position: %f", player.getX());
        printf("Player WORLD Y position: %f", player.getY());
        printf("Player WORLD Z position: %f", player.getZ());
    }
    */
}
