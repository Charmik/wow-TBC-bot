package farmbot.Pathing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.memory.objects.Player;

/**
 * Created by charm on 24.08.2017.
 */
public enum FarmList {


    //ALLIANCE
    START_ELF(1, 7, "startTelb", "fromStartElfTo2", null,true),
    DOLANAR(8, 11, "dolanar", null, null,true),
    IMPS_HELLFIRE(60, 70, "58_imps_HonorHold", null, null,true),

    /*
    START_NIGHT_ELF_VILLAGE(1, 6, "1-3_start_night_elfs", "5-6_teldrassil", null,false),
    DOLANAAR(7, 9, "5-6_teldrassil", null, null,false),
    AUBERDINE_MARLOCKS(10, 12, "9-11", "11-13_bears", null,false),
    AUBERDINE_BEARS_AND_OUSTRICH(13, 16, "11-13_bears", "13-16", null,false),
    DARKSHORT_AROUND_RIVER(17, 19, "13-16", null, null,false),
    WETLANDS_MARLOCS(20, 24, "20-21_WETLANDS_MURLOCS", "21-26_WETLANDS", null,false),
    WETLANDS_RAPTORS(25, 30, "21-26_WETLANDS", "30-33_arathi", null,false),
    ARATHI_RAPTORS(31, 35, "30-33_arathi", "32-36_arathi", null,false),
    ARAPHI_RAPTORS_AND_SPIDERS(36, 39, "32-36_arathi", "40-42_hinterlands", null,false),
    HINTERLANDS_WOLFS(40, 48, "40-42_hinterlands", null, null,false),
    FELWOOD_WOLFS(49, 56, "49-51_felwood", "54-56_Winter", null,false),
    //WINTERSPRING(57, 63, "54-56_Winter", null, null,false),
    HELLFIRE_IMPS_HONOR_HOLD(58, 66, "58_imps_HonorHold.txt", null, null, true),
    NAGRAND_SUNSPRING_POST(67, 70, "nagrandSunspringPost.txt", null, null, true),
//    BLADE_SALVANAR_ANIMALS(64, 70, "salvamar_amimals_a_few_humanoids", null, "repair_salvanar",false),
    TOSHLEY_STATOPN(64, 70, "toshley_station", null, "repair_toshley_station",false),
    */

    //HORDE
    /*
    START_TAURENS(1, 7, "1-8_mulgore_start", "8-14BV", null, true),
    BLOODHOOF_VILLAGE(8, 13, "8-14BV", null, null, true),
    CROSSROAD_ZHEVRAS(14, 19, "14-20_barrens", null, null, true),
    CAMP_TOURAJO(20, 23, "20-23_camp", "23_end_barrens", null, true),
    END_BARRENS(24, 27, "20-23_end_barrens", "27-28_needles", null, true),
    NEEDLES_BOTTON(28, 31, "27-28_needles", "30-32_needles", null, true),
    NEEDLES_BOTTON_SPIRIT(32, 35, "30-32_needles", "34-35_needles", null, true),
    NEEDLES_BOTTOM(36, 40, "34-35_needles", "41_gadgetzan", null, true), //edit it a little, stuck sometimes in water
    GADGEDZAN(41, 44, "41_gadgetzan", "45-47_tanaris", null, true),
    TANARIS_SECOND_SPIRIT(45, 49, "45-47_tanaris", "48-50_ungoro", null, true),
    UNGORO(50, 55, "48-50_ungoro", null, null, true),
    SILITIUS(56, 61, "57-58_silit", null, null, true),
    HELLFIRE_IMPS(60, 66, "hellfire_58", null, null, true),
    ORGES_BLADE(67, 70, "65-66_blade_orges", null, null, true),
    //FALCON_WATCH_HUMANOIDS(67, 70, "63_falcon_watch_humanoids", null, null, true),
    //SALVANAR(64, 70, "sylvanar_farm", null, "salvanar_repair", false),
    */
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
