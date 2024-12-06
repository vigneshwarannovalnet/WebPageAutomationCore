package com.nn.testcase.homepage;
import com.nn.basetest.BaseTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;

public class HomePageTest extends BaseTest {
    private static File DE_xl = new File(System.getProperty("user.dir") + "/src/test/resources/DE_HomePage.xlsx");

    //@Test(priority = 1, description = "Check whether the broken links  are present or not in the homePage de site")
    public void brokenLink_de() throws GeneralSecurityException, IOException {
       System.out.println("Starter: brokenLink_de: " + Thread.currentThread().getName());
       System.out.println("Thread ID: " + Thread.currentThread().getId());
       verifyCompleteLinks("https://www.novalnet.de/sitemap_index.xml", "DE");
    }

   // @Test(priority = 2, description = "Check whether the broken links are present or not in the homePage en site")
    public void brokenLink_en() throws GeneralSecurityException, IOException {
        System.out.println("Starter: brokenLink_en: " + Thread.currentThread().getName());
        System.out.println("Thread ID: " + Thread.currentThread().getId());
     verifyCompleteLinks("https://www.novalnet.com/sitemap_index.xml", "EN");
    }

   //@Test(priority = 3,description = "Check whether the broken image are present or not in the homePage de site")
    public void brokenImage_DE() throws GeneralSecurityException, IOException {
       System.out.println("Starter: brokenImage_DE: " + Thread.currentThread().getName());
       System.out.println("Thread ID: " + Thread.currentThread().getId());
         verifyBrokenImages("https://www.novalnet.de/sitemap_index.xml","DE");
    }

  //@Test(priority = 4,description = "Check whether the broken image are present or not in the homePage en site")
    public void brokenImage_EN() throws GeneralSecurityException, IOException {
        System.out.println("Starter: brokenImage_EN: " + Thread.currentThread().getName());
        System.out.println("Thread ID: " + Thread.currentThread().getId());
      verifyBrokenImages("https://www.novalnet.com/sitemap_index.xml","DE");
    }

    @Test
    public void test() throws IOException {
        System.out.println("File path is: " + DE_xl.getAbsolutePath());
        writeDataToSheet_DE("anvar", new ArrayList<Object>(Arrays.asList("url", "statusCode", "statusMessage", "sourceUrl")), DE_xl);

    }

}
