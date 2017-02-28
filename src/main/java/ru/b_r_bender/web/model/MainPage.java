package ru.b_r_bender.web.model;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.controller.Duelist;

import java.util.Date;

/**
 * @author Homenko created on 28.02.2017.
 */
public class MainPage extends AbstractPage {

    protected static final String MAIN_PAGE_URI = "http://elem.mobi/";
    protected static final String MAIN_PAGE_DISABLE_FLAG = "disable";

    private By duelLocator = By.cssSelector(".bttn.duels");
    private By dungeonLocator = By.cssSelector(".bttn.campaign");
    private By urfinLocator = By.cssSelector(".bttn.urfin");
    private By tournamentLocator = By.cssSelector(".bttn.urfin");
    private By serverTimeClockLocator = By.id("server_time");

    private boolean duelEnable;
    private boolean dungeonEnable;
    private boolean urfinEnable;
    private boolean tournamentEnable;

    private Date enterTime;

    MainPage(WebDriver webDriver) {
        super(webDriver, MAIN_PAGE_URI);
        initPage();
    }

    @Override
    void initPage() {
        enterTime = new Date();

        checkForTransitionsAvailability();

        System.out.println("stop");
    }

    private void checkForTransitionsAvailability() {
        reloadPage();
        duelEnable = !webDriver.findElement(duelLocator).getAttribute("class").contains(MAIN_PAGE_DISABLE_FLAG);
        dungeonEnable = !webDriver.findElement(dungeonLocator).getAttribute("class").contains(MAIN_PAGE_DISABLE_FLAG);
        urfinEnable = !webDriver.findElement(urfinLocator).getAttribute("class").contains(MAIN_PAGE_DISABLE_FLAG);
        tournamentEnable = !webDriver.findElement(tournamentLocator).getAttribute("class").contains(MAIN_PAGE_DISABLE_FLAG);
    }

    public void go() {
        Thread duelThread = new Thread(new Duelist(duelEnable));
        duelThread.start();
//        Thread dungeonThread = new Thread();
//        dungeonThread.start();
    }
}
