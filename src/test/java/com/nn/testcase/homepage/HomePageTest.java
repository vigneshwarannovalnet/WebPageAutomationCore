package com.nn.testcase.homepage;

import com.nn.basetest.BaseTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class HomePageTest extends BaseTest {

    @Test(priority = 1,dataProvider = "siteMap_Url",description = "sdsdsdssd")
    public void brokenLink(String input) throws GeneralSecurityException, IOException {
        getDriver().get(input);
        waitForTitleContains(("XML Sitemap"));
        checkAllLinks();
        checkSublinks();
    }

    @DataProvider()
    public Object[][] siteMap_Url(){
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
                {"https://www.novalnet.com/glossary_categories-sitemap.xml"}};
    }
}
