package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.utils.SeleniumUtils;

import java.util.List;

/**
 * @author BRBender on 20.03.17.
 */
public class DungeonKeeper implements Runnable {

    private static final Logger LOG = Logger.getLogger(DungeonKeeper.class);

    public static final String DUNGEON_PAGE_URI = "http://elem.mobi/dungeon/";

    private static final By DUNGEONS_ENTRANCE_LOCATOR = By.cssSelector("a[href*='/dungeon/']");
    private static final By HERO_CARDS_BUTTON_LOCATOR = By.cssSelector("a[href*='/dungeon/'][class='card']");
    private static By DUNGEON_EXIT_LOCATOR = By.cssSelector("a[href*='/dungeon/']");

    private WebDriver keeperDriver;

    public DungeonKeeper(WebDriver webDriver) {
        this.keeperDriver = SeleniumUtils.cloneDriverInstance(webDriver, DUNGEON_PAGE_URI);
    }

    @Override
    public void run() {
        while (true) {
            downInTheDungeon();
            breatheSomeFreshAir();
        }
    }

    private void downInTheDungeon() {
        WebElement entranceElement;
        while ((entranceElement = SeleniumUtils.getWebElement(keeperDriver, DUNGEONS_ENTRANCE_LOCATOR)) != null) {
            entranceElement.click();
            defeatDungeonCreature();
        }

    }

    private void defeatDungeonCreature() {
        while (SeleniumUtils.getWebElement(keeperDriver, DUNGEON_EXIT_LOCATOR) == null) {
            List<WebElement> heroCards = SeleniumUtils.getWebElements(keeperDriver, HERO_CARDS_BUTTON_LOCATOR);

        }
    }

    private void breatheSomeFreshAir() {

    }
}
