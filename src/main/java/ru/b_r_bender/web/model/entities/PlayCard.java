package ru.b_r_bender.web.model.entities;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.utils.SeleniumUtils;

import java.util.List;

import static ru.b_r_bender.web.controller.utils.CardsUtils.*;

/**
 * @author BRBender created on 02.03.2017.
 */
public class PlayCard implements Comparable<PlayCard> {

    private static final By CARD_NAME_LOCATOR = By.cssSelector(".fttl.mt5");
    private static final By CARD_STRENGTH_LOCATOR = By.cssSelector(".pt10.small");
    private static final By CARD_LEVEL_LOCATOR = By.cssSelector(".pt5.small");
    private static final By CARD_ATTRIBUTES_LOCATOR = By.cssSelector(".pt5.small");
    private static final By LEVEL_PROGRESS_LOCATOR = By.cssSelector(".rate.blue");
    private static final By ABSORB_WEAK_LOCATOR = By.cssSelector("a[href*='mergeall']");
    private static final By UPGRADE_CARD_LOCATOR = By.cssSelector("a[class*='w180px'][href*='improve']");
    private static final By UPGRADE_CARD_CONFIRM_LOCATOR = By.cssSelector("a[class*='w100px'][href*='confirmed']");
    private static final By UPGRADE_CARD_SUCCESS_LOCATOR = By.cssSelector(".msg.green.mt5");
    private static final By GOLD_LOCATOR = By.cssSelector(".c_gold");
    private static final By UPGRADE_COST_LOCATOR = By.xpath("//*[text()[contains(.,'Цена')]]");

    private static final String CARD_ATTRIBUTES_IN_PLAY_DECK = "В колоде";
    private static final String CARD_ATTRIBUTES_PROTECTED = "Защищена";
    private static final String LEVEL_PROGRESS_ATTRIBUTE_NAME = "style";

    private String cardUrl;
    private String cardName;
    private int cardStrength;
    private int cardLevel;
    private boolean isInPlayDeck;
    private boolean isProtected;
    private double levelProgress;
    private int cardStrengthAt40;
    private int lastUpgradeAttemptSuccessCount;

    private PlayCard() {
    }

    public PlayCard(WebDriver managerDriver) {
        //TODO: добавить проверку урла на корректность
        List<WebElement> webElements = SeleniumUtils.getWebElements(managerDriver, CARD_ATTRIBUTES_LOCATOR);

        this.cardUrl = managerDriver.getCurrentUrl();
        this.cardName = SeleniumUtils.getWebElement(managerDriver, CARD_NAME_LOCATOR).getText();
        this.cardStrength = SeleniumUtils.getIntValueFromElement(managerDriver, CARD_STRENGTH_LOCATOR);
        this.cardLevel = SeleniumUtils.getIntValueFromElement(managerDriver, CARD_LEVEL_LOCATOR);
        this.isInPlayDeck = webElements.get(1).getText().equals(CARD_ATTRIBUTES_IN_PLAY_DECK);
        this.isProtected = webElements.get(2).getText().equals(CARD_ATTRIBUTES_PROTECTED);
        this.levelProgress = SeleniumUtils.getDoubleValueFromElementAttribute(managerDriver, LEVEL_PROGRESS_LOCATOR, LEVEL_PROGRESS_ATTRIBUTE_NAME);
        this.cardStrengthAt40 = calculateBasicStrength();
    }

    public boolean isAbsorptionAvailable(WebDriver managerDriver) {
        WebElement absorbElement = SeleniumUtils.getWebElement(managerDriver, ABSORB_WEAK_LOCATOR);
        return absorbElement != null;
    }

    public boolean absorbWeakCards(WebDriver managerDriver) {
        if (managerDriver.getCurrentUrl().equals(this.cardUrl)) {
            return performAbsorption(managerDriver);
        } else {
            String urlToGoBackTo = managerDriver.getCurrentUrl();
            managerDriver.get(this.cardUrl);
            boolean result = performAbsorption(managerDriver);
            managerDriver.get(urlToGoBackTo);
            return result;
        }
    }

    private boolean performAbsorption(WebDriver managerDriver) {
        WebElement absorbElement = SeleniumUtils.getWebElement(managerDriver, ABSORB_WEAK_LOCATOR);
        if (absorbElement == null) {
            return false;
        } else {
            absorbElement.click();
            this.levelProgress = SeleniumUtils.getDoubleValueFromElementAttribute(managerDriver, LEVEL_PROGRESS_LOCATOR, LEVEL_PROGRESS_ATTRIBUTE_NAME);
            return true;
        }
    }

    public int upgrade(WebDriver managerDriver) {
        lastUpgradeAttemptSuccessCount = 0;

        if (managerDriver.getCurrentUrl().equals(this.cardUrl)) {
            return performUpgrade(managerDriver) ? lastUpgradeAttemptSuccessCount : 0;
        } else {
            String urlToGoBackTo = managerDriver.getCurrentUrl();
            managerDriver.get(this.cardUrl);
            boolean result = performUpgrade(managerDriver);
            managerDriver.get(urlToGoBackTo);
            return result ? lastUpgradeAttemptSuccessCount : 0;
        }
    }

    private boolean performUpgrade(WebDriver managerDriver) {
        boolean upgradeSuccess = false;
        while (SeleniumUtils.getWebElement(managerDriver, UPGRADE_CARD_SUCCESS_LOCATOR) == null
                && SeleniumUtils.getWebElement(managerDriver, UPGRADE_CARD_CONFIRM_LOCATOR) == null){
            SeleniumUtils.getWebElement(managerDriver, UPGRADE_CARD_LOCATOR).click();
            if (SeleniumUtils.getIntValueFromElement(managerDriver, CARD_LEVEL_LOCATOR) - this.cardLevel == 1) {
                upgradeSuccess = true;
                lastUpgradeAttemptSuccessCount++;
            }
        }
        if ((cardLevel + 1) % 5 == 0 && sufficientFounds(managerDriver)) {
            SeleniumUtils.getWebElement(managerDriver, UPGRADE_CARD_CONFIRM_LOCATOR).click();
            if (SeleniumUtils.getIntValueFromElement(managerDriver, CARD_LEVEL_LOCATOR) - this.cardLevel == 1) {
                upgradeSuccess = true;
                lastUpgradeAttemptSuccessCount++;
            }
        }
        this.cardLevel = SeleniumUtils.getIntValueFromElement(managerDriver, CARD_LEVEL_LOCATOR);
        this.levelProgress = SeleniumUtils.getDoubleValueFromElementAttribute(managerDriver, LEVEL_PROGRESS_LOCATOR, LEVEL_PROGRESS_ATTRIBUTE_NAME);
        this.cardStrength = SeleniumUtils.getIntValueFromElement(managerDriver, CARD_STRENGTH_LOCATOR);
        if (upgradeSuccess && this.levelProgress == 100d) {
            SeleniumUtils.refresh(managerDriver);
            performUpgrade(managerDriver);
        }
        return upgradeSuccess;
    }

    private boolean sufficientFounds(WebDriver managerDriver) {
        Integer heroGold = SeleniumUtils.getIntValueFromElement(managerDriver, GOLD_LOCATOR);
        Integer upgradeCost = SeleniumUtils.getIntValueFromElementByIndex(managerDriver, UPGRADE_COST_LOCATOR, 1);
        return heroGold >= upgradeCost;
    }

    private int calculateBasicStrength(){
        return downgradeTo40(this.clone());
    }

    private int downgradeTo40(PlayCard card) {
        if (card.cardLevel <= 40) {
            return card.cardStrength;
        }

        card.cardStrength = card.cardStrength - getStrengthAtSpecifiedLevel(card.cardLevel);
        card.cardLevel--;
        return downgradeTo40(card);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayCard that = (PlayCard) o;

        return this.cardStrength == that.cardStrength && this.cardLevel == that.cardLevel;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = cardUrl != null ? cardUrl.hashCode() : 0;
        result = 31 * result + cardStrength;
        result = 31 * result + cardLevel;
        result = 31 * result + (isInPlayDeck ? 1 : 0);
        result = 31 * result + (isProtected ? 1 : 0);
        temp = Double.doubleToLongBits(levelProgress);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int compareTo(PlayCard that) {
        return that.cardStrengthAt40 - this.cardStrengthAt40;
    }

    @Override
    public String toString() {
        return "PlayCard{" +
                "name=" + cardName +
                ", strength=" + cardStrength +
                ", level=" + cardLevel +
                ", levelProgress=" + levelProgress +
                ", strengthAt40=" + cardStrengthAt40 +
                '}';
    }

    @Override
    protected PlayCard clone() {
        PlayCard playCard = new PlayCard();
        playCard.cardUrl = this.cardUrl;
        playCard.cardStrength = this.cardStrength;
        playCard.cardLevel = this.cardLevel;
        playCard.isInPlayDeck = this.isInPlayDeck;
        playCard.isProtected = this.isProtected;
        playCard.levelProgress = this.levelProgress;
        return playCard;
    }

    //геттеры и сеттеры
    public String getCardUrl() {
        return cardUrl;
    }

    public double getLevelProgress() {
        return levelProgress;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean aProtected) {
        isProtected = aProtected;
    }
}
