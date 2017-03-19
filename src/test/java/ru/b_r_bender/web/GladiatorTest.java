package ru.b_r_bender.web;

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

    @Test
    public void gladiatorTest() {
        WebDriver webDriver = SeleniumUtils.getNewDriverInstance();

        try {
            EnterPage enterPage = new EnterPage(webDriver);
            LoginPage loginPage = enterPage.registeredUserEntry();
            loginPage.login();

            new Gladiator(webDriver).run();
        } catch (FailedLoginException e) {
            //MYTODO [Homenko] добавить обработку эксепшена
        }

        SeleniumUtils.driverDismiss(webDriver);
    }


}
