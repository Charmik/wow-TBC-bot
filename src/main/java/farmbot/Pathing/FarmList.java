package farmbot.Pathing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.memory.objects.Player;

/**
 * Created by charm on 24.08.2017.
 */
public enum FarmList {

    /*
    ALLIANCE
    START_NIGHT_ELF_VILLAGE(1, 6, "1-3_start_night_elfs", "5-6_teldrassil", null),
    DOLANAAR(7, 9, "5-6_teldrassil", null, null),
    AUBERDINE_MARLOCKS(10, 12, "9-11", "11-13_bears", null),
    AUBERDINE_BEARS_AND_OUSTRICH(13, 16, "11-13_bears", "13-16", null),
    DARKSHORT_AROUND_RIVER(17, 19, "13-16", null, null),
    WETLANDS_MARLOCS(20, 24, "20-21_WETLANDS_MURLOCS", "21-26_WETLANDS", null),
    WETLANDS_RAPTORS(25, 30, "21-26_WETLANDS", "30-33_arathi", null),
    ARATHI_RAPTORS(31, 35, "30-33_arathi", "32-36_arathi", null),
    ARAPHI_RAPTORS_AND_SPIDERS(36, 39, "32-36_arathi", "40-42_hinterlands", null),
    HINTERLANDS_WOLFS(40, 48, "40-42_hinterlands", null, null),
    FELWOOD_WOLFS(49, 56, "49-51_felwood", "54-56_Winter", null),
    WINTERSPRING(57, 63, "54-56_Winter", null, null),
    BLADE_SALVANAR_ANIMALS(64, 70, "salvamar_amimals_a_few_humanoids", null, "repair_salvanar"),
    TOSHLEY_STATOPN(64, 70, "toshley_station", null, "repair_toshley_station"),
    */
    //HORDE
    START_TAURENS(1, 7, "1-8_mulgore_start", "BV8-14", null, true),
    BLOODHOOF_VILLAGE(8, 13, "BV8-14", null, null, true),
    CROSSROAD_ZHEVRAS(14, 19, "14-20_barrens", null, null, true),
    //    MULGORE_KODO(14, 19, "10-18_mulgore", null, null, true),
    CAMP_TOURAJO(20, 23, "20-23_camp", "23_end_barrens", null, true),
    END_BARRENS(24, 27, "20-23_end_barrens", null, null, true),
    NEEDLES_BOTTON_SPIRIT(28, 35, "30-32_needles", "34-35_needles", null, true),
    NEEDLES_BOTTOM(36, 40, "34-35_needles", null, null, false),
    GADGEDZAN(41, 44, "41_gadgetzan", "45-47_tanaris", null, true),
    TANARIS_SECOND_SPIRIT(45, 50, "45-47_tanaris", null, null, true),
    SALVANAR(64, 70, "sylvanar_farm", null, "salvanar_repair", false),
    ANY(1, 70, "", null, null, false);

    private static final Logger log = LoggerFactory.getLogger(FarmList.class);
    private static final FarmList[] values = values();
    private final int lowLevel;
    private final int highLevel;
    private final String nextFileName;
    private final String repairFileName;
    private final boolean runFreely;
    private String fileName;

    FarmList(
        int lowLevel,
        int highLevel,
        String fileName,
        String nextFileName,
        String repairFileName,
        boolean runFreely)
    {
        this.lowLevel = lowLevel;
        this.highLevel = highLevel;
        this.fileName = fileName;
        this.nextFileName = nextFileName;
        this.repairFileName = repairFileName;
        this.runFreely = runFreely;
    }

    public static FarmList getFarmListByFileName(String fileName) {
        for (FarmList current : values()) {
            if (current.fileName.equals(fileName)) {
                return current;
            }
        }
        log.info("FarmList wasn't found, using ANY");
        ANY.fileName = fileName;
        return ANY;
    }

    public static FarmList getFarmListByLevel(Player player) {
        for (FarmList farmList : values()) {
            if (farmList.lowLevel <= player.getLevel() && farmList.highLevel >= player.getLevel()) {
                return farmList;
            }
        }
        throw new IllegalArgumentException("can't find farmList with level=" + player.getLevel());
    }

    public String getNextFileName() {
        return nextFileName;
    }

    public int getLowLevel() {
        return lowLevel;
    }

    public FarmList getNextFarmList() {
        if (this.ordinal() + 1 == values.length) {
            return this;
        }
        return values[(this.ordinal() + 1) % values.length];
    }

    public String getCurrentFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "FarmList{" +
            "lowLevel=" + lowLevel +
            ", highLevel=" + highLevel +
            ", fileName='" + fileName + '\'' +
            ", nextFileName='" + nextFileName + '\'' +
            ", repairFileName='" + repairFileName + '\'' +
            '}';
    }

    public String getRepairFileName() {
        return repairFileName;
    }

    public boolean isRunFreely() {
        return runFreely;
    }
}
