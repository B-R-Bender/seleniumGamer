package ru.b_r_bender.web.model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.model.intefraces.Navigable;

import java.net.URISyntaxException;

/**
 * @author BRBender created on 28.02.2017.
 */
public class EnterPage extends AbstractPage {

    protected static final String ENTER_PAGE_URI = "http://elem.mobi/start";

    private By enterButtonLocator = By.className("entrance");

    public EnterPage(WebDriver webDriver) {
        super(webDriver, ENTER_PAGE_URI);
        initPage();
    }

    @Override
    void initPage() {
        webDriver.navigate().to(ENTER_PAGE_URI);
    }

    public LoginPage registeredUserEntry() {
        webDriver.findElement(enterButtonLocator).click();
        return new LoginPage(webDriver);
    }

    public AbstractPage registration() {
        throw new UnsupportedOperationException ("Do not implemented yet.");
    }
}
