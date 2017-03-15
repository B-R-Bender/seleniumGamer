package ru.b_r_bender.web;

import org.junit.Test;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.controller.DeckManager;
import ru.b_r_bender.web.model.pages.EnterPage;
import ru.b_r_bender.web.model.pages.LoginPage;
import ru.b_r_bender.web.utils.SeleniumUtils;

import javax.security.auth.login.FailedLoginException;
import java.util.Arrays;

/**
 * @author Homenko created on 15.03.2017.
 */
public class DeckManagerTest {

    @Test
    public void deckManagerTest() {
        WebDriver webDriver = SeleniumUtils.getNewDriverInstance();

        try {
            EnterPage enterPage = new EnterPage(webDriver);
            LoginPage loginPage = enterPage.registeredUserEntry();
            loginPage.login();

            new DeckManager(webDriver).run();
        } catch (FailedLoginException e) {
            System.out.println(e.getMessage() + "\n" + e.getStackTrace());
        }

        SeleniumUtils.driverDismiss(webDriver);
    }

}
