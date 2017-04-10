package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.model.pages.MainPage;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

import javax.mail.MessagingException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author BRBender created on 29.03.2017.
 */
public class Watcher implements Runnable {

    private static final Logger LOG = Logger.getLogger(Watcher.class);

    public static final String REFUSING_GUY_PAGE_URI = MainPage.MAIN_PAGE_URI;

    private static final By GUILD_INVITE_MESSAGE_LOCATOR = By.cssSelector(".clan-arms.mlra");
    private static final By GUILD_INVITING_PERSON_NAME_LOCATOR = By.cssSelector("a[href*='/user/'][class*='c_lblue4']");
    private static final By GUILD_NAME_LOCATOR = By.cssSelector("a[href*='/guild/'][class='tdn']");
    private static final By GUILD_DECLINE_BUTTON_LOCATOR = By.cssSelector("a[href*='/guild/decline/']");
    private static final By GOLD_SALE_BUTTON_LOCATOR = By.cssSelector("a[href*='/forum/'][class='c_lgreen4']");
    private static final By UNUSUAL_BUTTON_LOCATOR = By.cssSelector(".bbtn.bbtn-yel");

    private WebDriver watcherDriver;
    private Set<String> unusualSet;
    private int tomorrow;

    public Watcher(WebDriver webDriver) {
        unusualSet = new HashSet<>();
        watcherDriver = SeleniumUtils.cloneDriverInstance(webDriver, REFUSING_GUY_PAGE_URI);
        MainPage.addDriver(watcherDriver);

        Calendar serverTime = SeleniumUtils.getServerTime(webDriver);
        serverTime.add(Calendar.DAY_OF_WEEK, 1);
        tomorrow = serverTime.get(Calendar.DAY_OF_WEEK);

        LOG.info(Utils.getMessage("watcher.info.created"));
    }

    @Override
    public void run() {
        LOG.info(Utils.getMessage("watcher.info.thread.start"));
        try {
            while (true) {
                isThereAnythingUnusual();
                tellThemToFuckOff();
                waitTillNextPatrol();
            }
        } catch (WebDriverException e) {
            LOG.error("Trying to restart thread because there was an error in WebDriver: ", e);
            MainPage.resurrectMe(Watcher.class);
        } catch (Exception e) {
            String screenName = SeleniumUtils.takeErrorScreenShot(watcherDriver);
            LOG.error("Screen shot taken and saved in " + screenName + " for error:\n" + e.getMessage(), e);
        }
    }

    private void isThereAnythingUnusual() {
        List<WebElement> unusualButtons = SeleniumUtils.getWebElements(watcherDriver, UNUSUAL_BUTTON_LOCATOR);
        String screenShot = null;
        if (unusualButtons.size() > 0) {
            screenShot = SeleniumUtils.takeNamedScreenShot(watcherDriver, "somethingUnusual");
        }

        try {
            for (int i = 0; i < unusualButtons.size(); i++) {
                String unusualThingText = SeleniumUtils.getWebElements(watcherDriver, UNUSUAL_BUTTON_LOCATOR)
                        .get(i).getText();
                String clearedText = unusualThingText.replaceAll("[0-9]", "");
                boolean thisStrangeThingWasNotAlreadyNoticed = unusualSet.add(clearedText);
                if (thisStrangeThingWasNotAlreadyNoticed) {
                    Utils.sendEmail("Selenium Gamer unusual thing report", unusualThingText, screenShot);
                }
            }
        } catch (MessagingException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void tellThemToFuckOff() {
        LOG.info(Utils.getMessage("watcher.info.fuckOff"));
        String hero = Utils.getAppProperty("game.login");

        try {
            WebElement guildFuckOffButton;
            while ((guildFuckOffButton = SeleniumUtils.getWebElement(watcherDriver, GUILD_DECLINE_BUTTON_LOCATOR)) != null) {
                String name = SeleniumUtils.getWebElement(watcherDriver, GUILD_INVITING_PERSON_NAME_LOCATOR).getText();
                String guild = SeleniumUtils.getWebElement(watcherDriver, GUILD_NAME_LOCATOR).getText();
                LOG.info(Utils.getMessage("watcher.info.fuckOffGuild", name, guild, hero));
                guildFuckOffButton.click();
                Thread.sleep(Utils.getShortDelay());
            }
            WebElement goldSaleFuckOffButton;
            while ((goldSaleFuckOffButton = SeleniumUtils.getWebElement(watcherDriver, GOLD_SALE_BUTTON_LOCATOR)) != null) {
                LOG.info(Utils.getMessage("watcher.info.fuckOffGold"));
                goldSaleFuckOffButton.click();
                Thread.sleep(Utils.getShortDelay());
                watcherDriver.navigate().back();
                Thread.sleep(Utils.getShortDelay());
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }

        LOG.info(Utils.getMessage("watcher.info.noMoreAnnoyingThings", hero));
    }

    private void waitTillNextPatrol() {
        try {
            Calendar now = SeleniumUtils.getServerTime(watcherDriver);
            int today = now.get(Calendar.DAY_OF_WEEK);
            if (today == tomorrow) {
                unusualSet.clear();
                now.add(Calendar.DAY_OF_WEEK, 1);
                tomorrow = now.get(Calendar.DAY_OF_WEEK);
            }
            long coolDownTime = Utils.calculateElementCoolDownTime(REFUSING_GUY_PAGE_URI);
            LOG.info(Utils.getMessage("watcher.info.wait", Utils.millisecondsToTimeString(coolDownTime)));
            Thread.sleep(coolDownTime);
            SeleniumUtils.refresh(watcherDriver);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
