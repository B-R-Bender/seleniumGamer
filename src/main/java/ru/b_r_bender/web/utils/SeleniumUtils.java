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
import java.util.List;

/**
 * Utility class to interact with Selenium Web-Driver
 *
 * @author Homenko
 */
public class SeleniumUtils {

    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();;

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
        List<WebElement> elements = webDriver.findElements(elementLocator);
        if (elements.size() == 0) {
            return null;
        } else {
            return elements;
        }
    }

    public static Integer getIntValueFromElement(WebDriver webDriver, By elementLocator) {
        WebElement webElement = getWebElement(webDriver, elementLocator);
        Integer result = null;
        if (webElement != null) {
            String valueString = webElement.getText();
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

    public static synchronized void takeScreenShot(WebDriver webDriver) {
        String filePath;
        if (OS_NAME.contains("win")) {
            filePath = "D:\\tmp\\seleniumGamerScr\\screenshot" + screenShotCounter++ + ".png";
        } else {
            filePath = "/home/bender/Изображения/Screenshots/seleniumGamerScr/screen" + screenShotCounter++ + ".png";
        }
        takeNamedScreenShot(webDriver, filePath);
    }

    public static synchronized void takeNamedScreenShot(WebDriver webDriver, String filePath) {
        try {
            File srcFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, new File(filePath));
        } catch (IOException e) {
            //MYTODO [Homenko] допилить обработку ошибок
            e.printStackTrace();
        }
    }
}