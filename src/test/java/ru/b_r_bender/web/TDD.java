package ru.b_r_bender.web;

import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.controller.Duelist;
import ru.b_r_bender.web.model.EnterPage;
import ru.b_r_bender.web.model.LoginPage;
import ru.b_r_bender.web.model.MainPage;
import ru.b_r_bender.web.utils.SeleniumUtils;

import javax.security.auth.login.FailedLoginException;

/**
 * @author or Homenko created on 28.02.2017.
 */
public class TDD {

    public static void main(String[] args) {
        WebDriver webDriver = SeleniumUtils.getNewDriverInstance();
        MainPage mainPage;

        try {
            EnterPage enterPage = new EnterPage(webDriver);
            LoginPage loginPage = enterPage.registeredUserEntry();
            mainPage = loginPage.login();
        } catch (FailedLoginException e) {
            //MYTODO [Homenko] добавить обработку эксепшена
        }

        Duelist duelist = new Duelist(webDriver,false);
        duelist.checkDuelAvailable();
        duelist.killEmAll();

        SeleniumUtils.driverDismiss(webDriver);
    }
}
