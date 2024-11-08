package org.example;

import org.openqa.selenium.chrome.ChromeDriver;

public class Main {
    public static void main(String[] args) {
        ChromeDriver driver = new ChromeDriver();
        driver.get("https://github.com/");
        driver.quit();
    }
}