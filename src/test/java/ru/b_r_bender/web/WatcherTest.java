package ru.b_r_bender.web;

import org.junit.Test;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.controller.Watcher;
import ru.b_r_bender.web.model.pages.EnterPage;
import ru.b_r_bender.web.model.pages.LoginPage;
import ru.b_r_bender.web.utils.SeleniumUtils;

import javax.security.auth.login.FailedLoginException;

/**
 * @author Homenko created on 29.03.2017.
 */
public class WatcherTest {

    @Test
    public void watcherTest() {
        WebDriver webDriver = SeleniumUtils.getNewDriverInstance();
        try {
            EnterPage enterPage = new EnterPage(webDriver);
            LoginPage loginPage = enterPage.registeredUserEntry();
            loginPage.login();

            new Watcher(webDriver).run();
        } catch (FailedLoginException e) {
            //MYTODO [Homenko] добавить обработку эксепшена
        } finally {
            SeleniumUtils.driverDismiss(webDriver);
        }
    }
}
