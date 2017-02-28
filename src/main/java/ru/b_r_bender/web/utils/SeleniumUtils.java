package ru.b_r_bender.web.utils;

/**
 * @author Homenko created on 28.02.2017.
 */

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to interact with Selenium Web-Driver
 *
 * @author Homenko
 */
public class SeleniumUtils {

    private static List<WebDriver> createdDrivers = new ArrayList<WebDriver>();

    private SeleniumUtils() {
    }

    public synchronized static WebDriver getNewDriverInstance() {
        PhantomJSDriver driver = new PhantomJSDriver();
        createdDrivers.add(driver);
        return driver;
    }

    public synchronized static boolean driverDismiss(WebDriver webDriver) {
        if (webDriver != null) {
            webDriver.close();
            return createdDrivers.remove(webDriver);
        } else {
            return false;
        }
    }

}