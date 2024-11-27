package com.nn.testcase.homepage;
import com.nn.basetest.BaseTest;
import org.testng.annotations.Test;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class HomePageTest extends BaseTest {

   @Test(priority = 1, description = "Check whether the broken links  are present or not in the homePage de site")
    public void brokenLink_de() throws GeneralSecurityException, IOException {
       System.out.println("Starter: brokenLink_de");
       // verifyCompleteLinks("https://www.novalnet.de/sitemap_index.xml", "DE");
    }

    @Test(priority = 2, description = "Check whether the broken links are present or not in the homePage en site")
    public void brokenLink_en() throws GeneralSecurityException, IOException {
        System.out.println("Starter: brokenLink_en");
      // verifyCompleteLinks("https://www.novalnet.com/sitemap_index.xml", "EN");
    }

   @Test(priority = 3,description = "Check whether the broken image are present or not in the homePage de site")
    public void brokenImage_DE() throws GeneralSecurityException, IOException {
       System.out.println("Starter: brokenImage_DE");
         // verifyBrokenImages("https://www.novalnet.de/sitemap_index.xml","DE");
    }

    @Test(priority = 4,description = "Check whether the broken image are present or not in the homePage en site")
    public void brokenImage_EN() throws GeneralSecurityException, IOException {
        System.out.println("Starter: brokenImage_EN");
       // verifyBrokenImages("https://www.novalnet.com/sitemap_index.xml","DE");
    }

}
