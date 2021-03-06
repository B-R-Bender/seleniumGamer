package ru.b_r_bender.web.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.utils.Utils;

import javax.security.auth.login.FailedLoginException;

/**
 * @author BRBender created on 28.02.2017.
 */
public class LoginPage extends AbstractPage {

    protected static final String LOGIN_PAGE_URI = "http://elem.mobi/start";

    private By userLoginLocator = By.name("plogin");
    private By userPasswordLocator = By.name("ppass");
    private String userName;
    private String userPassword;

    public LoginPage(WebDriver webDriver) {
        super(webDriver, LOGIN_PAGE_URI);
        initPage();
    }

    @Override
    //MYTODO [Homenko] перепилить на получение данных из файла с пропертями
    void initPage() {
        userName = Utils.getAppProperty("game.login");
        userPassword = Utils.getAppProperty("game.password");
    }

    public MainPage login() throws FailedLoginException {
        webDriver.findElement(userLoginLocator).sendKeys(userName);
        webDriver.findElement(userPasswordLocator).sendKeys(userPassword);
        webDriver.findElement(userPasswordLocator).submit();
        if (webDriver.getCurrentUrl().equals(MainPage.MAIN_PAGE_URI)) {
            authorizationComplete = true;
            return new MainPage(webDriver);
        } else {
            throw new FailedLoginException("Login failed somehow. FIDO");
        }
    }
}
