package ru.b_r_bender.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.controller.*;
import ru.b_r_bender.web.model.pages.EnterPage;
import ru.b_r_bender.web.model.pages.LoginPage;
import ru.b_r_bender.web.model.pages.MainPage;
import ru.b_r_bender.web.utils.SeleniumUtils;

import javax.security.auth.login.FailedLoginException;

/**
 * @author BRBender created on 04.04.2017.
 */
public class MainPageTest {

    WebDriver webDriver;
    MainPage mainPage;

    @Before
    public void init() {
        webDriver = SeleniumUtils.getNewDriverInstance();

        try {
            EnterPage enterPage = new EnterPage(webDriver);
            LoginPage loginPage = enterPage.registeredUserEntry();
            mainPage = loginPage.login();
            System.out.println("Login complete");
        } catch (FailedLoginException e) {
            e.printStackTrace();
        }
    }

    @After
    public void clear() {
        MainPage.stop();
    }

    @Test
    public void resurrectionTest() {
//            MainPage.resurrectMe(RewardCollector.class);
//            MainPage.resurrectMe(DeckManager.class);
//            MainPage.resurrectMe(Duelist.class);
//            MainPage.resurrectMe(DungeonKeeper.class);
//            MainPage.resurrectMe(Gladiator.class);
//            MainPage.resurrectMe(Shopper.class);
//            MainPage.resurrectMe(Watcher.class);
    }

}
