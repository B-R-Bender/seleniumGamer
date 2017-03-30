package ru.b_r_bender.web.model.pages;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.controller.*;
import ru.b_r_bender.web.utils.SeleniumUtils;

import java.lang.reflect.Constructor;
import java.util.Date;


/**
 * @author BRBender created on 28.02.2017.
 */
public class MainPage extends AbstractPage {
    private static final Logger LOG = Logger.getLogger(MainPage.class);

    public static final String MAIN_PAGE_URI = "http://elem.mobi/";

    private static final By DUEL_LOCATOR = By.cssSelector(".bttn.duels");
    private static final By DUNGEON_LOCATOR = By.cssSelector(".bttn.campaign");
    private static final By URFIN_LOCATOR = By.cssSelector(".bttn.urfin");
    private static final By TOURNAMENT_LOCATOR = By.cssSelector(".bttn.urfin");

    private static final String MAIN_PAGE_DISABLE_FLAG = "disable";

    private boolean duelAvailable;
    private boolean dungeonAvailable;
    private boolean urfinAvailable;
    private boolean tournamentAvailable;

    private static int numberOfThreadsResurrected;
    private static long millisWhenFirstThreadDied;
    private static WebDriver mainPageWebDriver;

    MainPage(WebDriver webDriver) {
        super(webDriver, MAIN_PAGE_URI);
    }

    @Override
    void initPage() {
        reloadPage();
        mainPageWebDriver = webDriver;
        duelAvailable = checkElementAvailability(DUEL_LOCATOR);
        dungeonAvailable = checkElementAvailability(DUNGEON_LOCATOR);
        urfinAvailable = checkElementAvailability(URFIN_LOCATOR);
        tournamentAvailable = checkElementAvailability(TOURNAMENT_LOCATOR);
    }

    private boolean checkElementAvailability(By elementLocator) {
        return !SeleniumUtils.getWebElement(webDriver, elementLocator).getAttribute("class").contains(MAIN_PAGE_DISABLE_FLAG);
    }

    public void go() {
        try {
            Thread rewardCollectorThread = new Thread(new RewardCollector(webDriver), "Reward Collector Thread");
            rewardCollectorThread.start();
            Thread.sleep(5_000);
            initPage();
            Thread duelThread = new Thread(new Duelist(webDriver), "Duelist Thread");
            duelThread.start();
            Thread.sleep(360_000);
            Thread shopThread = new Thread(new Shopper(webDriver), "Shopper Thread");
            shopThread.start();
            Thread.sleep(120_000);
            Thread deckManagerThread = new Thread(new DeckManager(webDriver), "Deck Manager Thread");
            deckManagerThread.start();
            Thread.sleep(180_000);
            Thread gladiatorThread = new Thread(new Gladiator(webDriver), "Gladiator Thread");
            gladiatorThread.start();
            Thread.sleep(300_000);
            Thread dungeonKeeperThread = new Thread(new DungeonKeeper(webDriver), "Dungeon Keeper Thread");
            dungeonKeeperThread.start();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static synchronized <T extends Runnable> void resurrectMe(Class<T> deadOne) {
        long now = new Date().getTime();
        if (numberOfThreadsResurrected % 10 == 0) {
            if (now - millisWhenFirstThreadDied <= 60_000) {
                LOG.error("Something went very wrong - more then 10 threads resurrections in 60 seconds, " +
                                                                                        "system will shutdown now.");
                System.exit(-1);
            }
            millisWhenFirstThreadDied = now;
        }
        bringItToLife(deadOne);
        numberOfThreadsResurrected++;
    }

    private static <T extends Runnable> void bringItToLife(Class<T> deadOne) {
        try {
            Constructor<T> constructor = deadOne.getConstructor(WebDriver.class);
            T instance = constructor.newInstance(mainPageWebDriver);
            new Thread(instance).run();
        } catch (ReflectiveOperationException e) {
            LOG.error("Unable to resurrect " + deadOne, e);
        }
    }
}
