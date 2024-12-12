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


    @Test(priority = 1, dataProvider = "siteMap_Url_DE",description = "Check whether the broken links  are present or not in the homePage de site")
    public void brokenLink_de(String input) throws GeneralSecurityException, IOException {
        openURL(input);
        waitForTitleContains("XML Sitemap");
        checkAllLinks("DE");
        checkSublinks("DE");
    }


    @Test(priority = 2, dataProvider ="siteMap_Url_EN",description = "Check whether the broken links are present or not in the homePage en site")
    public void brokenLink_en(String input) throws GeneralSecurityException, IOException {
        openURL(input);
        waitForTitleContains("XML Sitemap");
        checkAllLinks("EN");
        checkSublinks("EN");
    }

   /* @Test(priority = 3,description = "Check whether the broken image are present or not in the homePage de site")
    public void brokenImage_DE() throws GeneralSecurityException, IOException {
       verifyBrokenImages("https://www.novalnet.de/sitemap_index.xml","DE");
    }

   @Test(priority = 4,description = "Check whether the broken image are present or not in the homePage en site")
    public void brokenImage_EN() throws GeneralSecurityException, IOException {
       verifyBrokenImages("https://www.novalnet.com/sitemap_index.xml","EN");
    }*/

    //@Test(priority = 5,dataProvider = "siteMap_Url_DE",description = "verify the more than one H1 tag in the Homepage DE site ")
    public void H1TagChecker_DE(String input) throws IOException, GeneralSecurityException {
        openURL(input);
        waitForTitleContains(("XML Sitemap"));
     //   verifyH1Tags("DE");
    }

   // @Test(priority = 6,dataProvider = "siteMap_Url_EN",description = "verify the more than one H1 tag in the Homepage DE site ")
    public void H1TagChecker_EN(String input) throws IOException, GeneralSecurityException {
        openURL(input);
        waitForTitleContains(("XML Sitemap"));
     //   verifyH1Tags("EN");
    }

    //@Test(priority = 7,dataProvider = "siteMap_Url_DE",description = "Verify images are mentioned in alt tag")
    public void imageAltTagChecker_DE(String input) throws GeneralSecurityException, IOException {
        openURL(input);
        waitForTitleContains(("XML Sitemap"));
       // verifyImageAltAttributes("DE");
    }

    //@Test(priority = 8,dataProvider = "siteMap_Url_EN",description = "Verify images are mentioned in alt tag")
    public void imageAltTagChecker_EN(String input) throws GeneralSecurityException, IOException {
        openURL(input);
        waitForTitleContains(("XML Sitemap"));
       // verifyImageAltAttributes("EN");
    }

   // @Test(priority = 4,dataProvider = "siteMap_Url",description = "gffdgd")
    public void verifyMetaData(String input) throws IOException, GeneralSecurityException {
       // DriverActions.openURL(input);
      //  metaDataCheck();
    }
   // @Test(priority = 5,dataProvider = "siteMap_Url",description = "gffdgd")
    public void Canonicalckeck(String input) throws IOException, GeneralSecurityException {
       // DriverActions.openURL(input);
       // DriverActions.waitForTitleContains(("XML Sitemap"));
       // canonicalTags();
    }




    @DataProvider()
    public Object[][] siteMap_Url_DE(){
        return new Object [][] {{"https://www.novalnet.de/post-sitemap.xml"},
                {"https://www.novalnet.de/page-sitemap.xml"}};
    }

       @DataProvider()
       public Object[][] siteMap_Url_EN(){
        return new Object [][]
                 
                 {
                 {"https://www.novalnet.com/post-sitemap.xml"},
                 {"https://www.novalnet.com/page-sitemap.xml"}

        };
    }
    /* @DataProvider()
        public Object[][] siteMap_Url_EN () {
            return convertListToDataProvider(get_siteMap_urls("https://www.novalnet.com/sitemap_index.xml"));
        }
        */



}
