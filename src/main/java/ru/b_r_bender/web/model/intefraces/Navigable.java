package ru.b_r_bender.web.model.intefraces;

import org.openqa.selenium.By;

/**
 * @author BRBender created on 28.02.2017.
 */
public interface Navigable<T> {

    T navigateTo(By navigableLocator);
}
