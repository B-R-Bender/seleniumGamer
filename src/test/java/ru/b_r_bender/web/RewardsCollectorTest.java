package ru.b_r_bender.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.controller.DeckManager;
import ru.b_r_bender.web.controller.RewardCollector;
import ru.b_r_bender.web.model.pages.EnterPage;
import ru.b_r_bender.web.model.pages.LoginPage;
import ru.b_r_bender.web.utils.SeleniumUtils;

import javax.security.auth.login.FailedLoginException;

/**
 * @author BRBender created on 16.03.2017.
 */
public class RewardsCollectorTest {

    private static WebDriver webDriver;

    @Before
    public void init() {
        webDriver = SeleniumUtils.getNewDriverInstance();

        try {
            EnterPage enterPage = new EnterPage(webDriver);
            LoginPage loginPage = enterPage.registeredUserEntry();
            loginPage.login();
        } catch (FailedLoginException e) {
            System.out.println(e);
        }
    }

    @After
    public void destroy() {
        SeleniumUtils.driverDismiss(webDriver);
    }

    @Test
    public void rewardsCollectorTest() {
        new RewardCollector(webDriver).run();
    }

}