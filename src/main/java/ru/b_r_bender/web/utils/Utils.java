package ru.b_r_bender.web.utils;

import ru.b_r_bender.web.controller.Duelist;

import java.util.Random;

/**
 * Utility class containing methods to get random delays, to calculate different values so on.
 *
 * @author BRBender created on 28.02.2017.
 */
public class Utils {

    private Utils() {
    }

    private static Random random = new Random();

    /**
     * Method will calculate cool down time for specified <b>pageUri</b> till it will be active again.
     * by default method will return 2+ hours.
     * @return cool down time in milliseconds
     */
    public static long calculateElementCoolDownTime(String pageUri) {
        switch (pageUri) {
            case Duelist.DUEL_PAGE_URI:
                return 1_800_000 + getLongDelay();
            default:
                return 3_600_000 + getLongDelay();
        }
    }

    /**
     * Return random short delay time to imitate human behavior. Delay range 500 - 3000 milliseconds.
     *
     * @return random delay in milliseconds
     */
    public static long getShortDelay() {
        return random.nextInt(2_500) + 500;
    }

    /**
     * Return random medium delay time to imitate human behavior. Delay range 5000 - 25000 milliseconds.
     *
     * @return random delay in milliseconds
     */
    public static long getMediumDelay() {
        return random.nextInt(20_000) + 5_000;
    }

    /**
     * Return random long delay time to imitate human behavior. Delay range 155000 - 425000 milliseconds.
     *
     * @return random delay in milliseconds
     */
    public static long getLongDelay() {
        return random.nextInt(270_000) + 155_000;
    }

    /**
     * Return random integer in range 31-53 to be used as boundary of skipped opponents.
     *
     * @return random integer in range 31-53
     */
    public static int skippedOpponentsBoundary() {
        return random.nextInt(22) + 31;
    }

    /**
     * Calculates result of multiplying round to int
     *
     * @param value      vale to multiply
     * @param multiplier to apply on value
     * @return value * multiplier round to int
     */
    public static int calculateMultiplierResult(int value, double multiplier) {
        return new Long(Math.round(value * multiplier)).intValue();
    }

    public static double parseMultiplierValue(String multiplierText) {
        return Double.parseDouble(multiplierText.substring(2));
    }
}
