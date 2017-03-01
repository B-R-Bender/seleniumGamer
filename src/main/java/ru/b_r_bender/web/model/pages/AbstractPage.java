package ru.b_r_bender.web.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Abstract class to represent game page. All ingame pages must inherit this class.
 *
 * @author by BRBender on 27.02.2017.
 */
public abstract class AbstractPage {

    protected final WebDriver webDriver;
    protected String pageUriStringValue;
    protected static boolean authorizationComplete;

    AbstractPage(WebDriver webDriver, String pageUriStringValue) {
        this.pageUriStringValue = pageUriStringValue;
        this.webDriver = webDriver;
    }

    abstract void initPage();

    protected boolean reloadPage() {
        if (webDriver != null) {
            webDriver.navigate().refresh();
            return true;
        } else {
            return false;
        }
    }

    //getters and setters
    public URI getPageURI() throws URISyntaxException {
        return new URI(pageUriStringValue);
    }

    public String getPageUriStringValue() {
        return pageUriStringValue;
    }
}
