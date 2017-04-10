package ru.b_r_bender.web.model.pages;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.controller.*;
import ru.b_r_bender.web.utils.SeleniumUtils;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


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
    private static Set<WebDriver> appWebDrivers;
    private static ThreadGroup mainPageThreads;

    static {
        appWebDrivers = new HashSet<>();
        mainPageThreads = new ThreadGroup("Main Page Thread Group");
    }

    MainPage(WebDriver webDriver) {
        super(webDriver, MAIN_PAGE_URI);
        initPage();
    }

    @Override
    void initPage() {
        reloadPage();
        mainPageWebDriver = webDriver;
        appWebDrivers.add(webDriver);
    }

    public void go() {
        try {
            Thread rewardCollectorThread = new Thread(mainPageThreads, new RewardCollector(webDriver), RewardCollector.class.getName());
            rewardCollectorThread.start();
            Thread.sleep(5_000);
            Thread watcherThread = new Thread(mainPageThreads, new Watcher(webDriver), Watcher.class.getName());
            watcherThread.start();
            Thread.sleep(60_000);
            Thread duelThread = new Thread(mainPageThreads, new Duelist(webDriver), Duelist.class.getName());
            duelThread.start();
            Thread.sleep(360_000);
            Thread shopThread = new Thread(mainPageThreads, new Shopper(webDriver), Shopper.class.getName());
            shopThread.start();
            Thread.sleep(120_000);
            Thread deckManagerThread = new Thread(mainPageThreads, new DeckManager(webDriver), DeckManager.class.getName());
            deckManagerThread.start();
            Thread.sleep(180_000);
            Thread gladiatorThread = new Thread(mainPageThreads, new Gladiator(webDriver), Gladiator.class.getName());
            gladiatorThread.start();
            Thread.sleep(300_000);
            Thread dungeonKeeperThread = new Thread(mainPageThreads, new DungeonKeeper(webDriver), DungeonKeeper.class.getName());
            dungeonKeeperThread.start();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static synchronized void stop() {
        try {
            Thread[] threads = new Thread[mainPageThreads.activeCount()];
            mainPageThreads.enumerate(threads);
            for (Thread thread : threads) {
                thread.interrupt();
            }
            Thread.sleep(30_000);
            appWebDrivers.forEach(WebDriver::quit);
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
                stop();
                System.exit(-1);
            }
            millisWhenFirstThreadDied = now;
        }
        bringItToLife(deadOne);
        numberOfThreadsResurrected++;
    }

    private static <T extends Runnable> void bringItToLife(Class<T> deadOne) {
        try {
            SeleniumUtils.refresh(mainPageWebDriver);
            Constructor<T> constructor = deadOne.getConstructor(WebDriver.class);
            T instance = constructor.newInstance(mainPageWebDriver);
            new Thread(mainPageThreads, instance, instance.getClass().getSimpleName()).start();
        } catch (ReflectiveOperationException e) {
            LOG.error("Unable to resurrect " + deadOne, e);
        }
    }

    private boolean checkElementAvailability(By elementLocator) {
        return !SeleniumUtils.getWebElement(webDriver, elementLocator).getAttribute("class").contains(MAIN_PAGE_DISABLE_FLAG);
    }

    public static void addDriver(WebDriver webDriver) {
        appWebDrivers.add(webDriver);
    }
}
