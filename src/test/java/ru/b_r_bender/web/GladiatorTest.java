package ru.b_r_bender.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.controller.Gladiator;
import ru.b_r_bender.web.model.pages.EnterPage;
import ru.b_r_bender.web.model.pages.LoginPage;
import ru.b_r_bender.web.utils.SeleniumUtils;

import javax.security.auth.login.FailedLoginException;

/**
 * @author BRBender created on 14.03.2017.
 */
public class GladiatorTest {

    WebDriver webDriver;

    @Before
    public void init() {
        webDriver = SeleniumUtils.getNewDriverInstance();

        try {
            EnterPage enterPage = new EnterPage(webDriver);
            LoginPage loginPage = enterPage.registeredUserEntry();
            loginPage.login();
            System.out.println("Login complete");
        } catch (FailedLoginException e) {
            e.printStackTrace();
        }
    }

    @After
    public void clear() {
        SeleniumUtils.driverDismiss(webDriver);
    }


    @Test
    public void gladiatorTest() {
        new Gladiator(webDriver).run();
    }


}
