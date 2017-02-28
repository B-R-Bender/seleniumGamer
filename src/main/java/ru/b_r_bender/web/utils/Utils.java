package ru.b_r_bender.web.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author BRBender created on 28.02.2017.
 */
public class Utils {
    public static synchronized long calculateElementCoolDownTime(WebDriver duelistDriver, By elementLocator) {
        duelistDriver.navigate().refresh();
        duelistDriver.findElement(elementLocator);
        return 0;
    }
}
