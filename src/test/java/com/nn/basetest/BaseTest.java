package com.nn.basetest;

import org.openqa.selenium.HasCapabilities;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.openqa.selenium.manager.SeleniumManager;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.manager.SeleniumManager;

public class BaseTest  {
     private static final Lock lock = new ReentrantLock();
    private static ThreadLocal<ChromeDriver> driver = new ThreadLocal<>();
    private static ThreadLocal<WebDriverWait> wait = new ThreadLocal<>();
    private Set<String> checked200UrlS_DE = new HashSet<>();
    private Set<String> checkedImage200UrlS_DE = new HashSet<>();
    private Set<String> checkedImage200UrlS_EN = new HashSet<>();
    private Set<String> checked200UrlS_EN = new HashSet<>();

    private Set<String> H1Tag_checkedURLs_DE = new HashSet<>();
    private Set<String> H1Tag_checkedURLs_EN = new HashSet<>();
    private Set<String>  imgALT_DE_checkedURLs = new HashSet<>();
    private Set<String>  imgALT_EN_checkedURLs = new HashSet<>();
    public String linkchecksheetname = LocalDate.now()+"_BROKENLINK";
    public String imagechecksheetname =LocalDate.now()+"_BROKENIMAGE";
    public String H1tagchecksheetname = LocalDate.now()+"H1TAGS";
    public String metachecksheetname ="META-CHECK";
    public String imagealtchecksheetname = LocalDate.now()+"IMG_ALT_TAGS";
    public String canonicalTagssheetname ="canonical-CHECK_DE";
    private AtomicInteger count = new AtomicInteger(0);

    private static File DE_xl = new File(System.getProperty("user.dir"),"/src/test/resources/DE_HomePage.xlsx");
    private static File EN_xl = new File(System.getProperty("user.dir"),"/src/test/resources/EN_HomePage.xlsx");
    private static File skipped_URLs_xl = new File(System.getProperty("user.dir"),"/src/test/resources/EN_HomePage.xlsx");
    public static final boolean GITHUB = Boolean.parseBoolean(System.getProperty("GITHUB"));
    public static final boolean HEADLESS = Boolean.parseBoolean(System.getProperty("HEADLESS"));
    @BeforeTest
    public void createDriver(){
        try {
            ChromeOptions options = new ChromeOptions();
            System.out.println("Launching Chrome Driver...");
            System.setProperty("webdriver.http.factory", "jdk-http-client");
            if(GITHUB){
                System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
                System.out.println("Script run via github");
            }
            if(HEADLESS){
                options.addArguments("--headless");
            }
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-infobars");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-popup-blocking");
             options.addArguments("--no-proxy-server");
      options.addArguments("--disable-dev-shm-usage");
         options.addArguments("--disable-gpu");
             //options.setPageLoadStrategy(PageLoadStrategy.EAGER);
             System.setProperty("webdriver.chrome.verboseLogging", "true");
            driver.set(new ChromeDriver(options));
             
            // Set a page load timeout and script timeout
         driver.get().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120)); // Page load timeout
            //driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(120));  // Implicit wait
        driver.get().manage().timeouts().setScriptTimeout(Duration.ofSeconds(120)); // Script timeout
            wait.set(new WebDriverWait(driver.get(),Duration.ofSeconds(60)));
            assert driver != null : "Driver initialization failed!";
            driver.get().manage().window().maximize();
            System.out.println("Browser launched successfully");
        }catch (Exception e){
            System.err.println("Driver initialization failed: " + e.getMessage());
            throw new RuntimeException("Driver initialization failed", e);
        }

         
    }


    @AfterTest
    public void cleanUp() {
        if (driver.get() != null) {
            driver.get().quit();
        }
        driver.remove();
        wait.remove();
    }


    public void checkAllLinks(String lang) throws IOException, GeneralSecurityException {
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(60));
        waitForAllElementLocated(By.tagName("a"));
        List<WebElement> allLinks = getDriver().findElements(By.tagName("a"));
        for (WebElement link : allLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && href.contains("novalnet")) {
                if(lang.equals("DE")){
                    verifyLink_DE("N/A", href,linkchecksheetname);
                }else {
                    verifyLink_EN("N/A", href,linkchecksheetname);
                }

            }
        }

    }
    public static ChromeDriver getDriver() {
        return driver.get();
    }

    public static WebDriverWait getWait() {
        return wait.get();
    }
    public void waitForTitleContains(String title){
        getWait().until(ExpectedConditions.titleContains(title));;
    }

    private void verifyLink_DE(String sourceUrl, String url ,String sheetname) throws IOException, GeneralSecurityException {

        boolean result =false;
        for(String successURs :checked200UrlS_DE){
            result = successURs.equals(url);
            if (result==true)
                break;
        }
        if(result!=true){
            int statusCode = 0;
            String statusMessage = null;
            int currentCount = count.incrementAndGet();

            try {
                RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000) // 30 seconds connect
                        // timeout
                        .setSocketTimeout(30 * 1000) // 30 seconds socket timeout
                        .build();

                HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
                HttpGet request = new HttpGet(url);
                HttpResponse response = httpClient.execute(request);
                statusCode = response.getStatusLine().getStatusCode();
                StatusLine statusLine = response.getStatusLine();
                statusMessage = statusLine.getReasonPhrase();

                if (statusCode == 200) {
                    System.out.println(currentCount + ": " + url + ": " + "Link is valid(HTTP response code: " + statusCode + ")");
                    if(checked200UrlS_DE.add(url)){
                        writeDataToSheet_DE(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), DE_xl);
                    }

                } else {
                    writeDataToSheet_DE(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), DE_xl);
                    System.err.println(currentCount + ": " + url + ": " + "Link is broken (HTTP response code: "
                            + statusCode + ")");
                }
            } catch (Exception e) {
                if (statusCode == 0) {
                    writeDataToSheet_DE(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, "null", sourceUrl)), DE_xl);
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                } else {
                    writeDataToSheet_DE(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), DE_xl);
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                }
            }
        }

    }
    private void verifyImageLink_DE(String sourceUrl, String url ,String sheetname) throws IOException, GeneralSecurityException {

        boolean result =false;
        for(String successURs :checkedImage200UrlS_DE){
            result = successURs.equals(url);
            if (result==true)
                break;
        }
        if(result!=true){
            int statusCode = 0;
            String statusMessage = null;
            int currentCount = count.incrementAndGet();

            try {
                RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000) // 30 seconds connect
                        // timeout
                        .setSocketTimeout(30 * 1000) // 30 seconds socket timeout
                        .build();

                HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
                HttpGet request = new HttpGet(url);
                HttpResponse response = httpClient.execute(request);
                statusCode = response.getStatusLine().getStatusCode();
                StatusLine statusLine = response.getStatusLine();
                statusMessage = statusLine.getReasonPhrase();

                if (statusCode == 200) {
                    System.out.println(currentCount + ": " + url + ": " + "Link is valid(HTTP response code: " + statusCode + ")");
                    if(checkedImage200UrlS_DE.add(url)){
                        writeDataToSheet_DE(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), DE_xl);
                    }

                } else {
                    writeDataToSheet_DE(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), DE_xl);
                    System.err.println(currentCount + ": " + url + ": " + "Link is broken (HTTP response code: "
                            + statusCode + ")");
                }
            } catch (Exception e) {
                if (statusCode == 0) {
                    writeDataToSheet_DE(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, "null", sourceUrl)), DE_xl);
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                } else {
                    writeDataToSheet_DE(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), DE_xl);
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                }
            }
        }

    }
    private void verifyImageLink_EN(String sourceUrl, String url ,String sheetname) throws IOException, GeneralSecurityException {

        boolean result =false;
        for(String successURs :checkedImage200UrlS_EN){
            result = successURs.equals(url);
            if (result==true)
                break;
        }
        if(result!=true){
            int statusCode = 0;
            String statusMessage = null;
            int currentCount = count.incrementAndGet();

            try {
                RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000) // 30 seconds connect
                        // timeout
                        .setSocketTimeout(30 * 1000) // 30 seconds socket timeout
                        .build();

                HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
                HttpGet request = new HttpGet(url);
                HttpResponse response = httpClient.execute(request);
                statusCode = response.getStatusLine().getStatusCode();
                StatusLine statusLine = response.getStatusLine();
                statusMessage = statusLine.getReasonPhrase();

                if (statusCode == 200) {
                    System.out.println(currentCount + ": " + url + ": " + "Link is valid(HTTP response code: " + statusCode + ")");
                    if(checkedImage200UrlS_EN.add(url)){
                        writeDataToSheet_EN(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), EN_xl);
                    }

                } else {
                    writeDataToSheet_EN(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), EN_xl);
                    System.err.println(currentCount + ": " + url + ": " + "Link is broken (HTTP response code: "
                            + statusCode + ")");
                }
            } catch (Exception e) {
                if (statusCode == 0) {
                    writeDataToSheet_EN(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, "null", sourceUrl)), EN_xl);
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                } else {
                    writeDataToSheet_EN(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), EN_xl);
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                }
            }
        }

    }
    private void verifyLink_EN(String sourceUrl, String url ,String sheetname) throws IOException, GeneralSecurityException {

        boolean result =false;
        for(String successURs :checked200UrlS_EN){
            result = successURs.equals(url);
            if (result==true)
                break;
        }
        if(result!=true){
            int statusCode = 0;
            String statusMessage = null;
            int currentCount = count.incrementAndGet();

            try {
                RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000) // 30 seconds connect
                        // timeout
                        .setSocketTimeout(30 * 1000) // 30 seconds socket timeout
                        .build();

                HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
                HttpGet request = new HttpGet(url);
                HttpResponse response = httpClient.execute(request);
                statusCode = response.getStatusLine().getStatusCode();
                StatusLine statusLine = response.getStatusLine();
                statusMessage = statusLine.getReasonPhrase();

                if (statusCode == 200) {
                    System.out.println(currentCount + ": " + url + ": " + "Link is valid(HTTP response code: " + statusCode + ")");
                    if(checked200UrlS_EN.add(url)){
                        writeDataToSheet_EN(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), EN_xl);
                    }

                } else {
                    writeDataToSheet_EN(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), EN_xl);
                    System.err.println(currentCount + ": " + url + ": " + "Link is broken (HTTP response code: "
                            + statusCode + ")");
                }
            } catch (Exception e) {
                if (statusCode == 0) {
                    writeDataToSheet_EN(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, "null", sourceUrl)), EN_xl);
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                } else {
                    writeDataToSheet_EN(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), EN_xl);
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                }
            }
        }

    }
    public void writeDataToSheet_DE(String sheetName, List<Object> data, File filePath) throws IOException {
        lock.lock();  // Lock the block to prevent concurrent writes
        try {
            // Load the existing workbook
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            // Get the sheet, or create it if it doesn't exist
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
            }

            // Get the next available row number
            int nextRowNum = getNextRow(sheet);
            Row row = sheet.createRow(nextRowNum);

            // Write the data into the cells
            int cellNum = 0;
            for (Object value : data) {
                Cell cell = row.createCell(cellNum++);
                // Check the type of value before setting the cell value
                if (value instanceof String) {
                    cell.setCellValue((String) value);
                } else if (value instanceof Integer) {
                    cell.setCellValue((Integer) value);
                } else if (value instanceof Double) {
                    cell.setCellValue((Double) value);
                } else {
                    cell.setCellValue(value.toString());  // Default to string conversion
                }
            }

            // Close the input stream
            fileInputStream.close();

            // Write the changes back to the file
            FileOutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);

            // Close the workbook and output stream
            workbook.close();
            outputStream.close();
        } finally {
            lock.unlock();  // Ensure the lock is released even if an exception occurs
        }
    }


    public void writeDataToSheet_EN(String sheetName, List<Object> data, File filePath) throws IOException {
        lock.lock();  // Lock the block to prevent concurrent writes
        try {
            // Load the existing workbook
            FileInputStream fileInputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            // Get the sheet, or create it if it doesn't exist
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
            }

            // Get the next available row number
            int nextRowNum = getNextRow(sheet);
            Row row = sheet.createRow(nextRowNum);

            // Write the data into the cells
            int cellNum = 0;
            for (Object value : data) {
                Cell cell = row.createCell(cellNum++);
                // Check the type of value before setting the cell value
                if (value instanceof String) {
                    cell.setCellValue((String) value);
                } else if (value instanceof Integer) {
                    cell.setCellValue((Integer) value);
                } else if (value instanceof Double) {
                    cell.setCellValue((Double) value);
                } else {
                    cell.setCellValue(value.toString());  // Default to string conversion
                }
            }

            // Close the input stream
            fileInputStream.close();

            // Write the changes back to the file
            FileOutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);

            // Close the workbook and output stream
            workbook.close();
            outputStream.close();
        } finally {
            lock.unlock();  // Ensure the lock is released even if an exception occurs
        }
    }



    private int getNextRow(Sheet sheet) {
        // Iterate over each row to find the first completely empty row
        for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null || isRowEmpty(row)) {
                return rowNum;
            }
        }
        return sheet.getPhysicalNumberOfRows(); // If all rows are filled, return the next physical row number
    }

    private boolean isRowEmpty(Row row) {
        for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    public void checkSublinks(String lang) throws IOException, GeneralSecurityException {
        List<String> novalnetLinks = getAllNovalnetLinks();
        for (String url : novalnetLinks) {
            openURL(url);
            waitForAllElementLocated(By.tagName("a"));
            List<WebElement> innerLinks = driver.get().findElements(By.tagName("a"));
            for (WebElement innerLink : innerLinks) {
                String subUrl = innerLink.getAttribute("href");
                if (subUrl != null && !subUrl.isEmpty()
                        && (subUrl.startsWith("https://") || subUrl.startsWith("http://"))) {
                    if(lang.equals("DE")){
                        verifyLink_DE(url, subUrl, linkchecksheetname);
                    }else {
                        verifyLink_EN(url, subUrl, linkchecksheetname);
                    }

                }

            }

        }
    }

    public void waitForAllElementLocated(By by){
        getWait().until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
    }

    private List<String> getAllNovalnetLinks() {
        //driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(60));
        waitForAllElementLocated(By.tagName("a"));
        List<WebElement> allLinks = driver.get().findElements(By.tagName("a"));
        List<String> novalnetLinks = new ArrayList<>();

        for (WebElement link : allLinks) {
            String href = link.getAttribute("href");
            if (href != null && href.contains("novalnet")) {
                novalnetLinks.add(href);
            }
        }

        return novalnetLinks;
    }
    public void deleteSheets() {
        // Check if today is the 1st day of the month
        if (LocalDate.now().getDayOfMonth() == 1) {
            try (FileInputStream fis = new FileInputStream(DE_xl);
                 Workbook workbook = WorkbookFactory.create(fis)) {

                int sheetCounts = workbook.getNumberOfSheets();

                // Proceed to delete all sheets only if sheets exist
                if (sheetCounts > 0) {
                    // Loop through all sheets in reverse order to remove them
                    for (int i = sheetCounts - 1; i >= 0; i--) {
                        workbook.removeSheetAt(i);
                    }

                    // Write the changes to the file
                    try (FileOutputStream fos = new FileOutputStream(DE_xl)) {
                        workbook.write(fos);
                    }

                    System.out.println("All sheets have been deleted as today is the 1st of the month.");
                } else {
                    System.out.println("No sheets to delete.");
                }

            } catch (IOException e) {
                throw new RuntimeException("Error while deleting sheets", e);
            }
        } else {
            System.out.println("Today is not the 1st of the month. No sheets were deleted.");
        }
    }

   /* public void verifyCompleteLinks(String URL,String lang) throws GeneralSecurityException, IOException {
         List<String> siteMap_urls = get_siteMap_urls(URL);
         List<String> siteMap_inner_urls = getAllNovalnetLinks(URL);
        for (String url:siteMap_urls){
            openURL(url);
            waitForTitleContains(("XML Sitemap"));
            checkAllLinks(lang);
        }
        for (String url:siteMap_inner_urls){
            openURL(url);
            checkSublinks(url,lang);
        }
    }*/
    public List<String> get_siteMap_urls(String URL){
        List<String> siteMap_urls = new ArrayList<>();
        openURL(URL);
        waitForTitleContains("XML Sitemap");
        driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(60));
        waitForAllElementLocated(By.xpath("//a[contains(@href,'novalnet')]"));
        List<WebElement> siteMapLinks = driver.get().findElements(By.xpath("//a[contains(@href,'novalnet')]"));
        for (WebElement siteMapURLS : siteMapLinks) {
            String siteMapURL = siteMapURLS.getText();
            siteMap_urls.add(siteMapURL);
        }
        return siteMap_urls;
    }

    public Object[][] convertListToDataProvider(List<String> urls) {
        Object[][] data = new Object[urls.size()][1];
        for (int i = 0; i < urls.size(); i++) {
            data[i][0] = urls.get(i);
        }
        return data;
    }

    public void openURL(String url){
        getDriver().get(url);
         
        System.out.println("Open URL: " + url);
    }

    public void verifyBrokenImages(String lang) throws GeneralSecurityException, IOException {
        List<String> novalnetLinks = getAllNovalnetLinks();
        for (String url : novalnetLinks) {
            openURL(url);
            waitForAllElementLocated(By.tagName("img"));
            List<WebElement> imgTags =  driver.get().findElements(By.tagName("img"));
            for (WebElement value : imgTags) {
                String imageurl = value.getAttribute("src");
                if (imageurl != null && (imageurl.contains("https") || imageurl.contains("http"))) {

                    if (lang.equals("DE")){
                        verifyImageLink_DE(url, imageurl, imagechecksheetname);
                    }else{
                        verifyImageLink_EN(url,imageurl,imagechecksheetname);
                    }

                    }


                }
            }

        }

    public void verifyH1Tags_EN() throws IOException, GeneralSecurityException {
        List<String> novalnetLinks = getAllNovalnetLinks();

        for (String url : novalnetLinks) {
            openURL(url);
            if (url != null && !url.isEmpty() && url.contains("novalnet") && H1Tag_checkedURLs_EN.add(url)) {
                waitForAllElementLocated(By.xpath("//h1"));
                List<WebElement> h1Tags = driver.get().findElements(By.xpath("//h1"));
                String lengthCondition = (h1Tags.size()<=1) ? "Yes" : "No";
                writeDataToSheet_EN(H1tagchecksheetname, new ArrayList<Object>(Arrays.asList(url, h1Tags.size(), lengthCondition)), EN_xl);
                }
        }

    }

    public void verifyH1Tags_DE() throws IOException, GeneralSecurityException {
        List<String> novalnetLinks = getAllNovalnetLinks();

        for (String url : novalnetLinks) {
            openURL(url);
            if (url != null && !url.isEmpty() && url.contains("novalnet") && H1Tag_checkedURLs_DE.add(url)) {
                waitForAllElementLocated(By.xpath("//h1"));
                List<WebElement> h1Tags = driver.get().findElements(By.xpath("//h1"));
                String lengthCondition = (h1Tags.size()<=1) ? "Yes" : "No";
                    writeDataToSheet_DE(H1tagchecksheetname, new ArrayList<Object>(Arrays.asList(url, h1Tags.size(), lengthCondition)), DE_xl);
                }
            }

    }

    public static void waitForPageLoad() {
        WebDriverWait wait = new WebDriverWait(driver.get(), Duration.ofSeconds(120));

        ExpectedCondition<Boolean> pageLoad = new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                return getJsExecutor().executeScript("return document.readyState").equals("complete");
            }
        };

        boolean pageLoaded = getJsExecutor().executeScript("return document.readyState").equals("complete");

        if(!pageLoaded) {
            System.out.println("Javascript is not loaded ");
            try {
                wait.until(pageLoad);
            }catch(Throwable error) {
                error.printStackTrace();
                Assert.fail("Timeout waiting for page load (Javascript). (" + 120 + "s)");
            }
        }
    }
    public static JavascriptExecutor getJsExecutor() {
        return (JavascriptExecutor)driver.get();
    }
    public void verifyImageAltAttributes_DE() throws GeneralSecurityException, IOException {
        Set<String> urlsToSkip = readSkipURLsFromExcel(skipped_URLs_xl);
        String altValue=null;
        List<String> novalnetLinks = getAllNovalnetLinks();
        List<List<Object>> dataToWrite = new ArrayList<>();
        for(String novalnetURL:novalnetLinks) {
            openURL(novalnetURL);
            if (imgALT_DE_checkedURLs.add(novalnetURL)) {
                waitForAllElementLocated(By.xpath("//img"));
                List<WebElement> images = driver.get().findElements(By.xpath("//img"));
                for (WebElement image : images) {
                    String imageURL = image.getAttribute("src");
                    altValue = image.getAttribute("alt");

                    if (urlsToSkip.contains(imageURL)) {
                        continue;
                    }
                    if(altValue.isEmpty()){
                        altValue="NIL";
                    }
                       writeDataToSheet_DE(imagealtchecksheetname, new ArrayList<Object>(Arrays.asList(novalnetURL, "YES", imageURL, altValue)), DE_xl);

            }
        }
    }
    }

    public void verifyImageAltAttributes_EN() throws GeneralSecurityException, IOException {
        Set<String> urlsToSkip = readSkipURLsFromExcel(skipped_URLs_xl);
        String altValue=null;
        List<String> novalnetLinks = getAllNovalnetLinks();
        List<List<Object>> dataToWrite = new ArrayList<>();
        for(String novalnetURL:novalnetLinks) {
            openURL(novalnetURL);
            if (imgALT_EN_checkedURLs.add(novalnetURL)) {
                waitForAllElementLocated(By.xpath("//img"));
                List<WebElement> images = driver.get().findElements(By.xpath("//img"));
                for (WebElement image : images) {
                    String imageURL = image.getAttribute("src");
                    altValue = image.getAttribute("alt");

                    if (urlsToSkip.contains(imageURL)) {
                        continue;
                    }
                    if(altValue.isEmpty()){
                        altValue="NIL";
                    }
                    writeDataToSheet_DE(imagealtchecksheetname, new ArrayList<Object>(Arrays.asList(novalnetURL, "YES", imageURL, altValue)), EN_xl);

                }
            }
        }
    }
    // Method to read URLs to skip from an Excel file
    private static Set<String> readSkipURLsFromExcel(File file) throws IOException {
        Set<String> skipURLs = new HashSet<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet
            for (Row row : sheet) {
                Cell cell = row.getCell(0); // Assuming URLs are in the first column
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String url = cell.getStringCellValue().trim();
                    if (!url.isEmpty()) {
                        skipURLs.add(url);
                    }
                }
            }
        }
        return skipURLs;
    }
    public void metaDataCheck_DE() throws IOException, GeneralSecurityException {
        List<String> urls = getAllNovalnetLinks();
        for(String url:urls) {
            openURL(url);
            String value = null;
            List<WebElement> metaTags = driver.get().findElements(By.tagName("meta"));
            WebElement lastIndexTag =metaTags.get(metaTags.size() - 1);
            String lastIndexTagValue=lastIndexTag.getAttribute("name").replaceAll("\\s", "");
            for (WebElement metTag : metaTags) {
                value = metTag.getAttribute("name").replaceAll("\\s", "");
                if (value.equalsIgnoreCase("description")) {
                    String xpath = value.equals("description") ? "//meta[@name='description']" : "//meta[@name='Description']";
                    WebElement descriptionMetaTag = driver.get().findElement(By.xpath(xpath));
                    String description = descriptionMetaTag.getAttribute("content");
                    String lengthCondition = (description.length()<=155) ? "Yes" : "No";
                    writeDataToSheet_DE(metachecksheetname, new ArrayList<Object>(Arrays.asList(url, "YES", description, value, description.length(), lengthCondition)), DE_xl);
                    break;

                } else if (value.equals(lastIndexTagValue)) {
                    writeDataToSheet_DE(metachecksheetname, new ArrayList<Object>(Arrays.asList(url, "NO", "N/A", "N/A", "N/A")), DE_xl);
                }
            }
        }
    }
    public void metaDataCheck_EN() throws IOException, GeneralSecurityException {
        List<String> urls = getAllNovalnetLinks();
        for(String url:urls) {
            openURL(url);
            String value = null;
            List<WebElement> metaTags = driver.get().findElements(By.tagName("meta"));
            WebElement lastIndexTag =metaTags.get(metaTags.size() - 1);
            String lastIndexTagValue=lastIndexTag.getAttribute("name").replaceAll("\\s", "");
            for (WebElement metTag : metaTags) {
                value = metTag.getAttribute("name").replaceAll("\\s", "");
                if (value.equalsIgnoreCase("description")) {
                    String xpath = value.equals("description") ? "//meta[@name='description']" : "//meta[@name='Description']";
                    WebElement descriptionMetaTag = driver.get().findElement(By.xpath(xpath));
                    String description = descriptionMetaTag.getAttribute("content");
                    String lengthCondition = (description.length()<=155) ? "Yes" : "No";
                    writeDataToSheet_DE(metachecksheetname, new ArrayList<Object>(Arrays.asList(url, "YES", description, value, description.length(), lengthCondition)), EN_xl);
                    break;

                } else if (value.equals(lastIndexTagValue)) {
                    writeDataToSheet_DE(metachecksheetname, new ArrayList<Object>(Arrays.asList(url, "NO", "N/A", "N/A", "N/A")), EN_xl);
                }
            }
        }
    }
    public void canonicalTags_DE() throws IOException {
        List<String> novalLinks = getAllNovalnetLinks();
        for (String novalneturl : novalLinks) {
            int currentCount = count.incrementAndGet();
            openURL(novalneturl);
            waitForAllElementLocated(By.xpath("//link[@rel='canonical']"));
            List<WebElement> canonicalTags = driver.get().findElements((By.xpath("//link[@rel='canonical']")));
            int a  =canonicalTags.size() ;
            if (a==1){
                String value = canonicalTags.get(0).getAttribute("href");
                String valuecondition = value.equals(novalneturl) ? "Self referrer tag = Yes" : "Self referrer tag = No";
                writeDataToSheet_DE(canonicalTagssheetname, Arrays.asList(novalneturl, "Yes", valuecondition), DE_xl);
            }
            else{
                String valuecondition = (a==0) ? "This page does not have a canonical tag " : "This page have more than 1 canonical tag";
                writeDataToSheet_DE(canonicalTagssheetname, Arrays.asList(novalneturl, "abnormal", valuecondition), DE_xl);
            }


        }


    }
    public void canonicalTags_EN() throws IOException {
        List<String> novalLinks = getAllNovalnetLinks();
        for (String novalneturl : novalLinks) {
            int currentCount = count.incrementAndGet();
            openURL(novalneturl);
            waitForAllElementLocated(By.xpath("//link[@rel='canonical']"));
            List<WebElement> canonicalTags = driver.get().findElements((By.xpath("//link[@rel='canonical']")));
            int a  =canonicalTags.size() ;
            if (a==1){
                String value = canonicalTags.get(0).getAttribute("href");
                String valuecondition = value.equals(novalneturl) ? "Self referrer tag = Yes" : "Self referrer tag = No";
                writeDataToSheet_DE(canonicalTagssheetname, Arrays.asList(novalneturl, "Yes", valuecondition), EN_xl);
            }
            else{
                String valuecondition = (a==0) ? "This page does not have a canonical tag " : "This page have more than 1 canonical tag";
                writeDataToSheet_DE(canonicalTagssheetname, Arrays.asList(novalneturl, "abnormal", valuecondition), EN_xl);
            }


        }


    }
}



