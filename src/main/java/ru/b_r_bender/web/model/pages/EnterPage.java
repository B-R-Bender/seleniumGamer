package ru.b_r_bender.web.model.pages;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.utils.SeleniumUtils;

/**
 * @author BRBender created on 28.02.2017.
 */
public class EnterPage extends AbstractPage {
    private static final Logger LOG = Logger.getLogger(EnterPage.class);

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
        SeleniumUtils.getWebElement(webDriver, enterButtonLocator).click();
        return new LoginPage(webDriver);
    }

    public AbstractPage registration() {
        throw new UnsupportedOperationException ("Do not implemented yet.");
    }
}
