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

    private static List<WebDriver> createdDrivers = new ArrayList<WebDriver>();

    private SeleniumUtils() {
    }

    public synchronized static WebDriver getNewDriverInstance() {
        PhantomJSDriver driver = new PhantomJSDriver();
        createdDrivers.add(driver);
        return driver;
    }

    public synchronized static WebDriver cloneDriverInstance(WebDriver driverToClone, String duelPageUri) {
        WebDriver newDriver = getNewDriverInstance();
        newDriver.get("http://elem.mobi/");
        for (Cookie cookie : driverToClone.manage().getCookies()) {
            String domain = cookie.getDomain();
            domain = domain.charAt(0) != '.' ? "." + domain : domain;
            newDriver.manage().addCookie(new Cookie(cookie.getName(), cookie.getValue(), domain, cookie.getPath(), cookie.getExpiry()));
        }
        newDriver.get(duelPageUri);
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
        if (webElement != null) {
            return Integer.valueOf(webElement.getText().replaceAll("[^\\d]", "" ));
        } else {
            return null;
        }
    }

    public static void takeScreenShot(WebDriver webDriver) {
        takeNamedScreenShot(webDriver, "D:\\Java\\screenshot_.png");
    }

    public static void takeNamedScreenShot(WebDriver webDriver, String filePath) {
        try {
            File srcFile = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, new File(filePath));
        } catch (IOException e) {
            //MYTODO [Homenko] допилить обработку ошибок
            e.printStackTrace();
        }
    }
}