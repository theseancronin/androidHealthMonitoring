package com.android.shnellers.heartrate.weight;

/**
 * Created by Sean on 16/02/2017.
 */

public class Convert {

    private static final double KG_IN_POUNDS = 2.20462;

    private static final double POUND_IN_KG = 0.453592;

    private static final double STONE_IN_KG = 6.35029;

    private static final int POUNDS_IN_STONE = 14;

    private Convert(){}

    /**
     * Convert kg to stone.
     *
     * @param kg
     * @return
     */
    public static int convertKgsToPounds(final int kg) {

        double pounds = kg / POUND_IN_KG;

        return (int) pounds;

    }

    /**
     * Convert pounds to kg.
     *
     * @param pounds
     * @return
     */
    public static int convertPoundsToKgs(final int pounds) {
        double kgs = pounds / KG_IN_POUNDS;
        return (int) kgs;
    }

    /**
     * Convert stone to KG.
     *
     * @param stone
     * @param pounds
     * @return
     */
    public static int convertStoneToKg(final int stone, final int pounds) {

        int totalPounds = (stone * POUNDS_IN_STONE) + pounds;

        return convertPoundsToKgs(totalPounds);
    }
}
