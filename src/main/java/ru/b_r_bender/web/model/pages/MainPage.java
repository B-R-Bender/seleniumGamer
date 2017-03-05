package ru.b_r_bender.web.model.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.controller.Duelist;
import ru.b_r_bender.web.controller.Shopper;
import ru.b_r_bender.web.utils.SeleniumUtils;

import java.util.Date;

/**
 * @author Homenko created on 28.02.2017.
 */
public class MainPage extends AbstractPage {

    public static final String MAIN_PAGE_URI = "http://elem.mobi/";
    private static final String MAIN_PAGE_DISABLE_FLAG = "disable";

    private static By duelLocator = By.cssSelector(".bttn.duels");
    private static By dungeonLocator = By.cssSelector(".bttn.campaign");
    private static By urfinLocator = By.cssSelector(".bttn.urfin");
    private static By tournamentLocator = By.cssSelector(".bttn.urfin");
    private static By serverTimeClockLocator = By.id("server_time");

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
        duelAvailable = checkElementAvailability(duelLocator);
        dungeonAvailable = checkElementAvailability(duelLocator);
        urfinAvailable = checkElementAvailability(urfinLocator);
        tournamentAvailable = checkElementAvailability(tournamentLocator);
    }

    private boolean checkElementAvailability(By elementLocator) {
        return !SeleniumUtils.getWebElement(webDriver, elementLocator).getAttribute("class").contains(MAIN_PAGE_DISABLE_FLAG);
    }

    public void go() {
//        Thread duelThread = new Thread(new Duelist(webDriver, duelAvailable), "DuelistThread");
//        duelThread.start();
//        Thread shopThread = new Thread(new Shopper(webDriver), "ShopperThread");
//        shopThread.start();
//        Thread dungeonThread = new Thread();
//        dungeonThread.start();
    }
}
