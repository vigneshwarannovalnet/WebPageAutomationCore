package com.nn.testcase.homepage;
import com.nn.basetest.BaseTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;

public class HomePageTest extends BaseTest {
    private static File DE_xl = new File(System.getProperty("user.dir") + "/src/test/resources/DE_HomePage.xlsx");

    @Test(priority = 1, dataProvider = "siteMap_Url_DE",description = "Check whether the broken links  are present or not in the homePage de site")
    public void brokenLink_de(String input) throws GeneralSecurityException, IOException {
       System.out.println("Starter: brokenLink_de: " + Thread.currentThread().getName());
       System.out.println("Thread ID: " + Thread.currentThread().getId());
        openURL(input);
        checkAllLinks("DE");
        checkSublinks();
    }

 // @Test(priority = 2, dataProvider ="siteMap_Url_EN",description = "Check whether the broken links are present or not in the homePage en site")
    public void brokenLink_en(String input) throws GeneralSecurityException, IOException {
       System.out.println("Starter: brokenLink_de: " + Thread.currentThread().getName());
       System.out.println("Thread ID: " + Thread.currentThread().getId());
       openURL(input);
       checkAllLinks("EN");
       checkSublinks();
    }

   //@Test(priority = 3,dataProvider = "siteMap_Url_DE",description = "Check whether the broken image are present or not in the homePage de site")
    public void brokenImage_DE(String input) throws GeneralSecurityException, IOException {
       System.out.println("Starter: brokenLink_de: " + Thread.currentThread().getName());
       System.out.println("Thread ID: " + Thread.currentThread().getId());
       openURL(input);
       verifyBrokenImages("DE");

    }

   //@Test(priority = 4,dataProvider = "siteMap_Url_EN",description = "Check whether the broken image are present or not in the homePage en site")
    public void brokenImage_EN(String input) throws GeneralSecurityException, IOException {
       System.out.println("Starter: brokenLink_de: " + Thread.currentThread().getName());
       System.out.println("Thread ID: " + Thread.currentThread().getId());
       openURL(input);
       verifyBrokenImages("EN");
    }

    @DataProvider()
    public Object[][] siteMap_Url_DE() {
        return new Object[][] {/*{"https://www.novalnet.de/post-sitemap.xml"},
                {"https://www.novalnet.de/page-sitemap.xml"},
                {"https://www.novalnet.de/karriere-sitemap.xml"},
                {"https://www.novalnet.de/integration-sitemap.xml"},
                {"https://www.novalnet.de/produkte-sitemap.xml"},
                {"https://www.novalnet.de/solutions-sitemap.xml"},
                {"https://www.novalnet.de/services-sitemap.xml"},
                {"https://www.novalnet.de/mainp-sitemap.xml"},
                {"https://www.novalnet.de/category-sitemap.xml"},
                {"https://www.novalnet.de/post_tag-sitemap.xml"},*/
                {"https://www.novalnet.de/solutions-sitemap.xml"}};
    }

        @DataProvider()
        public Object[][] siteMap_Url_EN () {
            return new Object[][]{{"https://www.novalnet.com/post-sitemap.xml"},
                    {"https://www.novalnet.com/page-sitemap.xml"},
                    {"https://www.novalnet.com/integration-sitemap.xml"},
                    {"https://www.novalnet.com/news-sitemap.xml"},
                    {"https://www.novalnet.com/paymentsolution-sitemap.xml"},
                    {"https://www.novalnet.com/glossary-sitemap.xml"},
                    {"https://www.novalnet.com/news_categories-sitemap.xml"},
                    {"https://www.novalnet.com/glossary_categories-sitemap.xml"},
                    {"https://www.novalnet.com/careers-sitemap.xml"}
            };
        }


    }
