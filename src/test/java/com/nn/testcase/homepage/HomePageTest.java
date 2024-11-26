package com.nn.testcase.homepage;
import com.nn.basetest.BaseTest;
import org.testng.annotations.Test;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class HomePageTest extends BaseTest {

   //@Test(priority = 1, description = "Check whether the broken links and broken images are present or not in the homePage de site")
    public void brokenLink_de() throws GeneralSecurityException, IOException {
        verifyCompleteLinks("https://www.novalnet.de/sitemap_index.xml", "DE");
    }

    @Test(priority = 2, description = "Check whether the broken links and broken images are present or not in the homePage en site")
    public void brokenLink_en() throws GeneralSecurityException, IOException {
       // verifyCompleteLinks("https://www.novalnet.com/sitemap_index.xml", "EN");
        System.out.println("Hello world");
    }

  //  @Test(priority = 3,description = "Check whether the broken image are present or not in the homePage de site")
    public void brokenImage_DE() throws GeneralSecurityException, IOException {
          verifyBrokenImages("https://www.novalnet.de/sitemap_index.xml","DE");
    }

}
