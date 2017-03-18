package ru.b_r_bender.web.model.entities;

import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.utils.Utils;

/**
 * Class describes attack choice at http://elem.mobi/duel/. <br>
 * It consist opponent attack strength damage multiplier and hero attack strength.<br>
 * It allows to compare one attack option to another to determine best outcome.
 * @author BRBender created on 01.03.2017.
 */
public class DuelAttackOption implements Comparable<DuelAttackOption> {

    private WebElement actionElement;
    private int opponentStrength;
    private double attackDamageMultiplier;
    private int heroStrength;

    /**
     * Default constructor to get a {@link DuelAttackOption}
     * @param opponentStrength strength of opponent attack
     * @param attackDamageMultiplier attack multiplier
     * @param heroStrength strength of hero attack
     */
    public DuelAttackOption(WebElement actionElement, int opponentStrength, double attackDamageMultiplier, int heroStrength) {
        this.actionElement = actionElement;
        this.opponentStrength = opponentStrength;
        this.attackDamageMultiplier = attackDamageMultiplier;
        this.heroStrength = heroStrength;
    }

    /**
     * Default constructor to get a {@link DuelAttackOption} if attackDamageMultiplier unknown.<br>
     * By default attackDamageMultiplier = 1d;
     * @param opponentStrength strength of opponent attack
     * @param heroStrength strength of hero attack
     */
    public DuelAttackOption(WebElement actionElement, int opponentStrength, int heroStrength) {
        this(actionElement, opponentStrength, 1d, heroStrength);
    }

    /**
     * Perform attack with <b>this</b> DuelAttackOption
     */
    public void attack() {
        actionElement.click();
    }

    /**
     * Calculates this DuelAttackOption attack strength using next formula:<br>
     * hero strength * attack multiplier - opponent strength * (2 - attack multiplier)
     * @return this DuelAttackOption attack strength
     */
    public int getAttackStrength() {
        int heroTotalDamage = Utils.calculateMultiplierResult(heroStrength, attackDamageMultiplier);
        int opponentTotalDamage = Utils.calculateMultiplierResult(opponentStrength, 2 - attackDamageMultiplier);
        return heroTotalDamage - opponentTotalDamage;
    }

    @Override
    public String toString() {
        return "DuelAttackOption{" +
                "opp=" + opponentStrength +
                ", multiplier=" + attackDamageMultiplier +
                ", hero=" + heroStrength +
                ", total=" + getAttackStrength() +
                '}';
    }

    @Override
    public int compareTo(DuelAttackOption that) {
        if (that == null) {
            throw new IllegalArgumentException("DuelAttackOption can not be compared to null");
        }
        return this.getAttackStrength() - that.getAttackStrength();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DuelAttackOption that = (DuelAttackOption) o;

        return this.getAttackStrength() == that.getAttackStrength();
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = opponentStrength;
        temp = Double.doubleToLongBits(attackDamageMultiplier);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + heroStrength;
        return result;
    }
}
