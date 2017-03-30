package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.model.pages.MainPage;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

import java.util.List;

/**
 * @author BRBender created on 29.03.2017.
 */
public class RefusingGuy implements Runnable {

    private static final Logger LOG = Logger.getLogger(RefusingGuy.class);

    public static final String REFUSING_GUY_PAGE_URI = MainPage.MAIN_PAGE_URI;

    private static final By GUILD_INVITE_MESSAGE_LOCATOR = By.cssSelector(".clan-arms.mlra");
    private static final By GUILD_INVITING_PERSON_NAME_LOCATOR = By.cssSelector("a[href*='/user/'][class*='c_lblue4']");
    private static final By GUILD_NAME_LOCATOR = By.cssSelector("a[href*='/guild/'][class='tdn']");
    private static final By GUILD_DECLINE_BUTTON_LOCATOR = By.cssSelector("a[href*='/guild/decline/']");
    private static final By GOLD_SALE_BUTTON_LOCATOR = By.cssSelector("a[href*='/forum/'][class='c_lgreen4']");

    private WebDriver refuseDriver;

    public RefusingGuy(WebDriver webDriver) {
        refuseDriver = webDriver;
        refuseDriver.get(REFUSING_GUY_PAGE_URI);
        LOG.info(Utils.getMessage("refusingGuy.info.created"));
    }

    @Override
    public void run() {
        LOG.info(Utils.getMessage("refusingGuy.info.thread.start"));
        try {
            while (true) {
                tellThemToFuckOff();
                waitTillSomeOneCome();
            }
        } catch (WebDriverException e) {
            LOG.error("Trying to restart thread because there was an error in WebDriver: ", e);
            MainPage.resurrectMe(RefusingGuy.class);
        } catch (Exception e) {
            String screenName = SeleniumUtils.takeErrorScreenShot(refuseDriver);
            LOG.error("Screen shot taken and saved in " + screenName + " for error:\n" + e.getMessage(), e);
        } finally {
            refuseDriver.close();
        }
    }

    private void waitTillSomeOneCome() {
        try {
            long coolDownTime = Utils.calculateElementCoolDownTime(REFUSING_GUY_PAGE_URI);
            LOG.info(Utils.getMessage("refusingGuy.info.wait", Utils.millisecondsToTimeString(coolDownTime)));
            Thread.sleep(coolDownTime);
            SeleniumUtils.refresh(refuseDriver);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void tellThemToFuckOff() {
        LOG.info(Utils.getMessage("refusingGuy.info.fuckOff"));
        WebElement guildFuckOffButton;
        while ((guildFuckOffButton = SeleniumUtils.getWebElement(refuseDriver, GUILD_DECLINE_BUTTON_LOCATOR)) != null) {
            String name = SeleniumUtils.getWebElement(refuseDriver, GUILD_INVITING_PERSON_NAME_LOCATOR).getText();
            String guild = SeleniumUtils.getWebElement(refuseDriver, GUILD_NAME_LOCATOR).getText();
            LOG.info(Utils.getMessage("refusingGuy.info.fuckOffGuild", name, guild));
            guildFuckOffButton.click();
        }
        WebElement goldSaleFuckOffButton;
        while ((goldSaleFuckOffButton = SeleniumUtils.getWebElement(refuseDriver, GOLD_SALE_BUTTON_LOCATOR)) != null) {
            LOG.info(Utils.getMessage("refusingGuy.info.fuckOffGold"));
            goldSaleFuckOffButton.click();
            refuseDriver.navigate().back();
        }
        LOG.info(Utils.getMessage("refusingGuy.info.noMoreAnnoyingThings"));
    }
}
