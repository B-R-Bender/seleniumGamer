package ru.b_r_bender.web.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.controller.DeckManager;
import ru.b_r_bender.web.controller.Duelist;
import ru.b_r_bender.web.controller.Shopper;
import ru.b_r_bender.web.utils.SeleniumUtils;

/**
 * @author Homenko created on 28.02.2017.
 */
public class MainPage extends AbstractPage {

    public static final String MAIN_PAGE_URI = "http://elem.mobi/";
    private static final String MAIN_PAGE_DISABLE_FLAG = "disable";

    private static final By DUEL_LOCATOR = By.cssSelector(".bttn.duels");
    private static final By DUNGEON_LOCATOR = By.cssSelector(".bttn.campaign");
    private static final By URFIN_LOCATOR = By.cssSelector(".bttn.urfin");
    private static final By TOURNAMENT_LOCATOR = By.cssSelector(".bttn.urfin");

    private boolean duelAvailable;
    private boolean dungeonAvailable;
    private boolean urfinAvailable;
    private boolean tournamentAvailable;

    MainPage(WebDriver webDriver) {
        super(webDriver, MAIN_PAGE_URI);
        initPage();
    }

    @Override
    void initPage() {
        reloadPage();
        duelAvailable = checkElementAvailability(DUEL_LOCATOR);
        dungeonAvailable = checkElementAvailability(DUNGEON_LOCATOR);
        urfinAvailable = checkElementAvailability(URFIN_LOCATOR);
        tournamentAvailable = checkElementAvailability(TOURNAMENT_LOCATOR);
    }

    private boolean checkElementAvailability(By elementLocator) {
        return !SeleniumUtils.getWebElement(webDriver, elementLocator).getAttribute("class").contains(MAIN_PAGE_DISABLE_FLAG);
    }

    public void go() {
        Thread duelThread = new Thread(new Duelist(webDriver, duelAvailable), "Duelist Thread");
        duelThread.start();
        Thread shopThread = new Thread(new Shopper(webDriver), "Shopper Thread");
        shopThread.start();
        Thread deckManagerThread = new Thread(new DeckManager(webDriver), "DeckManager Thread");
        deckManagerThread.start();
    }
}
