package ru.b_r_bender.web;

import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.controller.Duelist;
import ru.b_r_bender.web.model.pages.EnterPage;
import ru.b_r_bender.web.model.pages.LoginPage;
import ru.b_r_bender.web.model.pages.MainPage;
import ru.b_r_bender.web.utils.SeleniumUtils;

import javax.security.auth.login.FailedLoginException;

/**
 * @author or Homenko created on 28.02.2017.
 */
public class TDD {

    public static void main(String[] args) {
        WebDriver webDriver = SeleniumUtils.getNewDriverInstance();

        try {
            EnterPage enterPage = new EnterPage(webDriver);
            LoginPage loginPage = enterPage.registeredUserEntry();
            MainPage mainPage = loginPage.login();
            mainPage.go();
        } catch (FailedLoginException e) {
            //MYTODO [Homenko] добавить обработку эксепшена
        }

        SeleniumUtils.driverDismiss(webDriver);
    }
}
