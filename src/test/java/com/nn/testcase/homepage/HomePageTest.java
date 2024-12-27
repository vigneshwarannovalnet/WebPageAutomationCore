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
        //checkAllLinks("DE");
        //checkSublinks("DE");
    }



    @Test(priority = 2, dataProvider ="siteMap_Url_EN",description = "Check whether the broken links are present or not in the homePage en site")
    public void brokenLink_en(String input) throws GeneralSecurityException, IOException {
        openURL(input);
        waitForTitleContains("XML Sitemap");
        //checkAllLinks("EN");
        //checkSublinks("EN");
        
    }

    @Test(priority = 3,dataProvider = "siteMap_Url_DE",description = "Check whether the broken image are present or not in the homePage de site")
    public void brokenImage_DE(String input) throws GeneralSecurityException, IOException {
        openURL(input);
        waitForTitleContains("XML Sitemap");
       // verifyBrokenImages("DE");
    }

   @Test(priority = 4,dataProvider = "siteMap_Url_EN",description = "Check whether the broken image are present or not in the homePage en site")
    public void brokenImage_EN(String input) throws GeneralSecurityException, IOException {
       openURL(input);
       waitForTitleContains("XML Sitemap");
       //verifyBrokenImages("EN");
    }

    @Test(priority = 5,dataProvider = "siteMap_Url_DE",description = "verify the more than one H1 tag in the Homepage DE site ")
    public void H1TagChecker_DE(String input) throws IOException, GeneralSecurityException {
        openURL(input);
        waitForTitleContains(("XML Sitemap"));
        verifyH1Tags_DE();
    }
    @Test(priority = 6,dataProvider = "siteMap_Url_EN",description = "verify the more than one H1 tag in the Homepage DE site ")
    public void H1TagChecker_EN(String input) throws IOException, GeneralSecurityException {
        openURL(input);
        waitForTitleContains(("XML Sitemap"));
        verifyH1Tags_EN();
    }


    @Test(priority = 7,dataProvider = "siteMap_Url_DE",description = "Verify images are mentioned in alt tag")
    public void imageAltTagChecker_DE(String input) throws GeneralSecurityException, IOException {
        openURL(input);
        waitForTitleContains(("XML Sitemap"));
        verifyImageAltAttributes_DE();
    }

    @Test(priority = 8,dataProvider = "siteMap_Url_EN",description = "Verify images are mentioned in alt tag")
    public void imageAltTagChecker_EN(String input) throws GeneralSecurityException, IOException {
        openURL(input);
        waitForTitleContains(("XML Sitemap"));
        verifyImageAltAttributes_EN();
    }

    @Test(priority = 9,dataProvider = "siteMap_Url_DE",description = "Verify meta tags for DE websites")
    public void verifyMetaData_DE(String input) throws IOException, GeneralSecurityException {
        openURL(input);
        waitForTitleContains(("XML Sitemap"));
        metaDataCheck_DE();
    }

    @Test(priority = 10,dataProvider = "siteMap_Url_EN",description = "Verify meta tags for EN websites")
    public void verifyMetaData_EN(String input) throws IOException, GeneralSecurityException {
        openURL(input);
        waitForTitleContains(("XML Sitemap"));
        metaDataCheck_EN();
    }
    @Test(priority = 11,dataProvider = "siteMap_Url_DE",description = "Verify canonical tags for DE websites")
    public void Canonicalckeck_DE(String input) throws IOException, GeneralSecurityException {
         openURL(input);
         waitForTitleContains(("XML Sitemap"));
         canonicalTags_DE();
    }

    @Test(priority = 12,dataProvider = "siteMap_Url_EN",description = "Verify canonical tags for EN websites")
    public void Canonicalckeck_EN(String input) throws IOException, GeneralSecurityException {
        openURL(input);
        waitForTitleContains(("XML Sitemap"));
        canonicalTags_EN();
    }




    @DataProvider()
    public Object[][] siteMap_Url_EN(){
        return new Object[][] {{"https://www.novalnet.com/post-sitemap.xml"},
                {"https://www.novalnet.com/page-sitemap.xml"},
                {"https://www.novalnet.com/integration-sitemap.xml"},
                {"https://www.novalnet.com/news-sitemap.xml"},
                {"https://www.novalnet.com/paymentsolution-sitemap.xml"},
                {"https://www.novalnet.com/glossary-sitemap.xml"},
                {"https://www.novalnet.com/careers-sitemap.xml"},
                {"https://www.novalnet.com/news_categories-sitemap.xml"},
                {"https://www.novalnet.com/glossary_categories-sitemap.xml"}

        };
    }
    
    @DataProvider()
    public Object[][] siteMap_Url_DE(){
        return new Object [][] {{"https://www.novalnet.de/post-sitemap.xml"},
                                {"https://www.novalnet.de/page-sitemap.xml"},
                                {"https://www.novalnet.de/karriere-sitemap.xml"},
                                {"https://www.novalnet.de/integration-sitemap.xml"},
                                {"https://www.novalnet.de/produkte-sitemap.xml"},
                                {"https://www.novalnet.de/solutions-sitemap.xml"},
                                {"https://www.novalnet.de/services-sitemap.xml"},
                                {"https://www.novalnet.de/mainp-sitemap.xml"},
                                {"https://www.novalnet.de/category-sitemap.xml"},
                                {"https://www.novalnet.de/post_tag-sitemap.xml"},
                                {"https://www.novalnet.de/author-sitemap.xml"}
    };

    }





}
