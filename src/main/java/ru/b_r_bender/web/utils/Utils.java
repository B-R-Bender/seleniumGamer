package ru.b_r_bender.web.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
     * Method will calculate cool down time for specified <b>elementLocator</b> till it will be active again.
     * @param duelistDriver driver with specified page where <b>elementLocator</b> can be found
     * @param elementLocator to calculate cool down time for
     * @return cool down time in milliseconds
     */
    public static synchronized long calculateElementCoolDownTime(WebDriver duelistDriver, By elementLocator) {
        duelistDriver.navigate().refresh();
        duelistDriver.findElement(elementLocator);
        return 0;
    }

    /**
     * Return random short delay time to imitate human behavior. Delay range 500 - 3000 milliseconds.
     * @return random delay in milliseconds
     */
    public static long getShortDelay() {
        return random.nextInt(2_500) + 500;
    }

    /**
     * Return random medium delay time to imitate human behavior. Delay range 5000 - 25000 milliseconds.
     * @return random delay in milliseconds
     */
    public static long getMediumDelay() {
        return random.nextInt(20_000) + 5_000;
    }

    /**
     * Return random long delay time to imitate human behavior. Delay range 155000 - 425000 milliseconds.
     * @return random delay in milliseconds
     */
    public static long getLongDelay() {
        return random.nextInt(270_000) + 155_000;
    }
}
