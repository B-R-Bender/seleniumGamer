package ru.b_r_bender.web.utils;

/**
 * @author Homenko created on 28.02.2017.
 */

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Utility class to interact with Selenium Web-Driver
 *
 * @author Homenko
 */
public class SeleniumUtils {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();;

    private static final By SERVER_TIME_LOCATOR = By.cssSelector("#server_time");

    private static volatile int screenShotCounter;
    private static List<WebDriver> createdDrivers = new ArrayList<>();

    private SeleniumUtils() {
    }

    public synchronized static WebDriver getNewDriverInstance() {
        PhantomJSDriver driver = new PhantomJSDriver();
        createdDrivers.add(driver);
        return driver;
    }

    public synchronized static WebDriver cloneDriverInstance(WebDriver driverToClone, String pageUri) {
        WebDriver newDriver = getNewDriverInstance();
        newDriver.get("http://elem.mobi/");
        for (Cookie cookie : driverToClone.manage().getCookies()) {
            String domain = cookie.getDomain();
            domain = domain.charAt(0) != '.' ? "." + domain : domain;
            newDriver.manage().addCookie(new Cookie(cookie.getName(), cookie.getValue(), domain, cookie.getPath(), cookie.getExpiry()));
        }
        createdDrivers.add(newDriver);
        newDriver.get(pageUri);
        return newDriver;
    }

    public synchronized static boolean driverDismiss(WebDriver webDriver) {
        if (webDriver != null) {
            webDriver.close();
            return createdDrivers.remove(webDriver);
        } else {
            return false;
        }
    }

    public static void refresh(WebDriver webDriver) {
        webDriver.navigate().refresh();
    }

    public static WebElement getWebElement(WebDriver webDriver, By elementLocator) {
        return getWebElement(webDriver, elementLocator, 0, false);
    }

    public static WebElement getWebElement(WebDriver webDriver, By elementLocator, boolean refreshRequired) {
        return getWebElement(webDriver, elementLocator, 0, refreshRequired);
    }

    public static WebElement getWebElement(WebDriver webDriver, By elementLocator, int elementIndex, boolean refreshRequired) {
        if (refreshRequired) {
            webDriver.navigate().refresh();
        }
        List<WebElement> elements = webDriver.findElements(elementLocator);
        if (elements.size() == 0) {
            return null;
        } else if (elementIndex > elements.size() - 1) {
            return null;
        } else {
            return elements.get(elementIndex);
        }
    }

    public static List<WebElement> getWebElements(WebDriver webDriver, By elementLocator) {
        return getWebElements(webDriver, elementLocator,false);
    }

    public static List<WebElement> getWebElements(WebDriver webDriver, By elementLocator, boolean refreshRequired) {
        if (refreshRequired) {
            webDriver.navigate().refresh();
        }
        return webDriver.findElements(elementLocator);
    }

    public static Integer getIntValueFromElement(WebDriver webDriver, By elementLocator) {
        return getIntValueFromElementByIndex(webDriver, elementLocator, 0);
    }

    public static Integer getIntValueFromElementByIndex(WebDriver webDriver, By elementLocator, int valuedIndex) {
        WebElement webElement = getWebElement(webDriver, elementLocator);
        return getIntValueFromElement(webElement, valuedIndex);
    }

    public static Integer getIntValueFromElement(WebElement webElement) {
        return getIntValueFromElement(webElement, 0);
    }

    public static Integer getIntValueFromElement(WebElement webElement, int valuedIndex) {
        Integer result = null;
        if (webElement != null) {
            String valueString = webElement.getText().split("\n")[valuedIndex];
            if (valueString.contains("K")) {
                Double parsedDouble = Double.valueOf(valueString.substring(0, valueString.length() - 1)) * 1_000;
                result = parsedDouble.intValue();
            } else {
                result = Integer.valueOf(valueString.replaceAll("[^\\d]", ""));
            }
        }
        return result;
    }

    public static Double getDoubleValueFromElementAttribute(WebDriver webDriver, By elementLocator, String attributeName) {
        WebElement webElement = getWebElement(webDriver, elementLocator);
        Double result = null;
        if (webElement != null) {
            String valueString = webElement.getAttribute(attributeName);
            if (valueString.contains("K")) {
                result = Double.valueOf(valueString.substring(0, valueString.length() - 1)) * 1_000;
            } else {
                result = Double.valueOf(valueString.replaceAll("[^\\d\\.]+", ""));
            }
        }
        return result;
    }

    public static synchronized String takeScreenShot(WebDriver webDriver) {
        return takeNamedScreenShot(webDriver, "screen");
    }

    public static synchronized String takeErrorScreenShot(WebDriver webDriver) {
        return takeNamedScreenShot(webDriver, "error");
    }

    public static synchronized String takeNamedScreenShot(WebDriver webDriver, String fileName) {
        String filePath;
        if (OS_NAME.contains("win")) {
            filePath = "D:\\tmp\\seleniumGamerScr\\" + fileName + screenShotCounter++ + ".png";
        } else {
            filePath = "/home/bender/Изображения/Screenshots/seleniumGamerScr/" + fileName + screenShotCounter++ + ".png";
        }
        takeScreenShotWithPath(webDriver, filePath);
        return filePath;
    }

    public static synchronized void takeScreenShotWithPath(WebDriver webDriver, String filePath) {
        try {
            File srcFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, new File(filePath));
        } catch (IOException e) {
            //MYTODO [Homenko] допилить обработку ошибок
            e.printStackTrace();
        }
    }

    public static Calendar getServerTime(WebDriver webDriver) {
        String serverTimeString = SeleniumUtils.getWebElement(webDriver, SERVER_TIME_LOCATOR).getText();
        String[] timeFragments = serverTimeString.split(":");

        int hours = timeFragments[0].equals("") ? 0 : Integer.parseInt(timeFragments[0]);
        int minutes = timeFragments[1].equals("") ? 0 : Integer.parseInt(timeFragments[1]);
        int seconds = timeFragments[2].equals("") ? 0 : Integer.parseInt(timeFragments[2]);

        Calendar serverTime = Calendar.getInstance();
        serverTime.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        serverTime.set(Calendar.HOUR_OF_DAY, hours);
        serverTime.set(Calendar.MINUTE, minutes);
        serverTime.set(Calendar.SECOND, seconds);

        return serverTime;
    }

    public static Calendar getTomorrow(WebDriver webDriver) {
        Calendar tomorrow = getServerTime(webDriver);

        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.set(Calendar.MILLISECOND, 0);

        return tomorrow;
    }
}