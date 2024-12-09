package com.nn.basetest;



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

public class BaseTest  {
     private static final Lock lock = new ReentrantLock();
    private static ThreadLocal<ChromeDriver> driver = new ThreadLocal<>();
    private static ThreadLocal<WebDriverWait> wait = new ThreadLocal<>();
    private Set<String> checked200UrlS = new CopyOnWriteArraySet<>();
    private Set<String> checkedURLs = new CopyOnWriteArraySet<>();
    public String linkchecksheetname = LocalDate.now()+"_BROKENLINK";
    public String imagechecksheetname =LocalDate.now()+"_BROKENIMAGE";
    public String H1tagchecksheetname = LocalDate.now()+"H1TAGS";

    public String imagealtchecksheetname = LocalDate.now()+"IMG_ALT_TAGS";
    private AtomicInteger count = new AtomicInteger(0);

    private static File DE_xl = new File(System.getProperty("user.dir"),"/src/test/resources/DE_HomePage.xlsx");
    private static File EN_xl = new File(System.getProperty("user.dir"),"/src/test/resources/EN_HomePage.xlsx");
    private static File skipped_URLs_xl = new File(System.getProperty("user.dir"),"/src/test/resources/EN_HomePage.xlsx");
    @BeforeMethod
    public void createDriver(){
        try {
            System.out.println("Launching Chrome Driver...");
            System.setProperty("webdriver.http.factory", "jdk-http-client");
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-infobars");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-popup-blocking");
            driver.set(new ChromeDriver(options));
            // Set a page load timeout and script timeout
           driver.get().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120)); // Page load timeout
            driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(120));  // Implicit wait
          driver.get().manage().timeouts().setScriptTimeout(Duration.ofSeconds(120)); // Script timeout
            wait.set(new WebDriverWait(driver.get(),Duration.ofSeconds(120)));
            assert driver != null : "Driver initialization failed!";
            driver.get().manage().window().maximize();
        }catch (Exception e){
            System.err.println("Driver initialization failed: " + e.getMessage());
            throw new RuntimeException("Driver initialization failed", e);
        }

    }


    @AfterMethod
    public void cleanUp() {
        if (driver.get() != null) {
            driver.get().quit();
        }
        driver.remove();
        wait.remove();
    }


    public void checkAllLinks(String lang) throws IOException, GeneralSecurityException {
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(120));
        waitForAllElementLocated(By.tagName("a"));
        List<WebElement> allLinks = getDriver().findElements(By.tagName("a"));
        for (WebElement link : allLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && href.contains("novalnet")) {
                verifyLink( "N/A", href,linkchecksheetname,lang);
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

    private void verifyLink(String sourceUrl, String url ,String sheetname,String lang) throws IOException, GeneralSecurityException {

        boolean result =false;
        for(String successURLs :checked200UrlS){
            result = successURLs.equals(url);
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
                    if(checked200UrlS.add(url)){
                        if(lang.equals("DE")){
                            writeDataToSheet_DE(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), DE_xl);
                        }else {
                            writeDataToSheet_EN(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), EN_xl);
                        }

                    }

                } else {
                    if(lang.equals("DE")){
                        writeDataToSheet_DE(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), DE_xl);
                    }else {
                        writeDataToSheet_EN(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), EN_xl);
                    }
                    System.err.println(currentCount + ": " + url + ": " + "Link is not 200 status code (HTTP response code: "
                            + statusCode + ")");
                }
            } catch (Exception e) {
                if (statusCode == 0) {
                    if(lang.equals("DE")){
                        writeDataToSheet_DE(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), DE_xl);
                    }else {
                        writeDataToSheet_EN(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), EN_xl);
                    }
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                } else {
                    if(lang.equals("DE")){
                        writeDataToSheet_DE(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), DE_xl);
                    }else {
                        writeDataToSheet_EN(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), EN_xl);
                    }
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                }
            }
        }

    }
    public void writeDataToSheet_DE(String sheetName, List<Object> data, File filePath) throws IOException {
        lock.lock(); // Ensure thread safety
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            // Get or create the sheet
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
            }

            // Define the headings for the first row
            String[] headings = {"URL", "Status Code", "Status Text", "Source Page URL"};

            // Add headings if the first row is empty
            if (sheet.getRow(0) == null) {
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headings.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headings[i]);
                    CellStyle style = workbook.createCellStyle();
                    Font font = workbook.createFont();
                    font.setBold(true); // Make the header bold
                    style.setFont(font);
                    cell.setCellStyle(style);
                }
            }

            // Check if data is null or empty
            if (data == null || data.isEmpty()) {
                System.err.println("No data provided to write to the sheet.");
                return; // Exit early if there's no data
            }

            // Find the next available row for data
            int nextRowNum = getNextRow(sheet);
            Row row = sheet.createRow(nextRowNum);
            if (row == null) {
                throw new IllegalStateException("Failed to create a new row at index: " + nextRowNum);
            }

            // Write data to the row
            int cellNum = 0;
            for (Object value : data) {
                Cell cell = row.createCell(cellNum++);
                if (cell == null) {
                    throw new IllegalStateException("Failed to create a new cell at column: " + (cellNum - 1));
                }
                if (value == null) {
                    System.out.println("Null value found in data at index: " + (cellNum - 1));
                    cell.setCellValue("NULL"); // Handle null values gracefully
                } else if (value instanceof String) {
                    cell.setCellValue((String) value);
                } else if (value instanceof Integer) {
                    cell.setCellValue((Integer) value);
                } else {
                    System.out.println("Sheet data is: " + value);
                    cell.setCellValue(value.toString());
                }
            }

            // Write changes back to the file
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }

        } finally {
            lock.unlock(); // Ensure the lock is released
        }
    }


    public void writeDataToSheet_EN(String sheetName, List<Object> data, File filePath) throws IOException {
        lock.lock(); // Ensure thread safety
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            // Get or create the sheet
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
            }

            // Define the headings for the first row
            String[] headings = {"URL", "Status Code", "Status Text", "Source Page URL"};

            // Add headings if the first row is empty
            if (sheet.getRow(0) == null) {
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headings.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headings[i]);
                    CellStyle style = workbook.createCellStyle();
                    Font font = workbook.createFont();
                    font.setBold(true); // Make the header bold
                    style.setFont(font);
                    cell.setCellStyle(style);
                }
            }

            // Check if data is null or empty
            if (data == null || data.isEmpty()) {
                System.err.println("No data provided to write to the sheet.");
                return; // Exit early if there's no data
            }

            // Find the next available row for data
            int nextRowNum = getNextRow(sheet);
            Row row = sheet.createRow(nextRowNum);
            if (row == null) {
                throw new IllegalStateException("Failed to create a new row at index: " + nextRowNum);
            }

            // Write data to the row
            int cellNum = 0;
            for (Object value : data) {
                Cell cell = row.createCell(cellNum++);
                if (cell == null) {
                    throw new IllegalStateException("Failed to create a new cell at column: " + (cellNum - 1));
                }
                if (value == null) {
                    System.out.println("Null value found in data at index: " + (cellNum - 1));
                    cell.setCellValue("NULL"); // Handle null values gracefully
                } else if (value instanceof String) {
                    cell.setCellValue((String) value);
                } else if (value instanceof Integer) {
                    cell.setCellValue((Integer) value);
                } else {
                    System.out.println("Sheet data is: " + value);
                    cell.setCellValue(value.toString());
                }
            }

            // Write changes back to the file
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        } finally {
            lock.unlock(); // Ensure the lock is released
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
    public void checkSublinks() throws IOException, GeneralSecurityException {
        List<String> novalnetLinks = getAllNovalnetLinks();
        for (String url : novalnetLinks) {
           openURL(url);
            waitForAllElementLocated(By.tagName("a"));
            List<WebElement> innerLinks = driver.get().findElements(By.tagName("a"));
            for (WebElement innerLink : innerLinks) {
                String subUrl = innerLink.getAttribute("href");
                if (subUrl != null && !subUrl.isEmpty()
                        && (subUrl.startsWith("https://") || subUrl.startsWith("http://"))) {
                    verifyLink(url, subUrl, linkchecksheetname,"Broken_Link");
                }

            }

        }
    }


    public void waitForAllElementLocated(By by){
        getWait().until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
    }

    private List<String> getAllNovalnetLinks() {
        driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(120));
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

    /*public void verifyCompleteLinks(String URL,String lang) throws GeneralSecurityException, IOException {
         List<String> siteMap_urls = get_siteMap_urls(URL);
         List<String> siteMap_inner_urls = getAllNovalnetLinks(URL);
        for (String url:siteMap_urls){
            openURL(url);
            waitForTitleContains(("XML Sitemap"));
            checkAllLinks(lang);
        }
        for (String url:siteMap_inner_urls){
            openURL(url);
            System.out.println("Novalnet link" + url);
            checkSublinks(url,lang);
        }
    }*/
    public List<String> get_siteMap_urls(String URL){
        List<String> siteMap_urls = new ArrayList<>();
        openURL(URL);
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
                    verifyLink(url, imageurl, imagechecksheetname,lang);

                }
            }

        }

    }
    public void verifyH1Tags(String lang) throws IOException, GeneralSecurityException {
        List<String> novalnetLinks = getAllNovalnetLinks();
        List<List<Object>> dataToWrite = new ArrayList<>();

        for (String url : novalnetLinks) {
            openURL(url);
            if (url != null && !url.isEmpty() && url.contains("novalnet") && checkedURLs.add(url)) {
                waitForAllElementLocated(By.xpath("//h1"));
                List<WebElement> h1Tags = driver.get().findElements(By.xpath("//h1"));
                String lengthCondition = (h1Tags.size()<=1) ? "Yes" : "No";
                if(lang.equals("DE")){
                    writeDataToSheet_DE(H1tagchecksheetname, new ArrayList<Object>(Arrays.asList(url, h1Tags.size(), lengthCondition)), DE_xl);
                }
               else {
                    writeDataToSheet_EN(H1tagchecksheetname, new ArrayList<Object>(Arrays.asList(url, h1Tags.size(), lengthCondition)), EN_xl);
                }
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
    public void verifyImageAltAttributes(String lang) throws GeneralSecurityException, IOException {
        Set<String> urlsToSkip = readSkipURLsFromExcel(skipped_URLs_xl);
        String altValue=null;
        List<String> novalnetLinks = getAllNovalnetLinks();
        List<List<Object>> dataToWrite = new ArrayList<>();
        for(String novalnetURL:novalnetLinks) {
            openURL(novalnetURL);
            if (checkedURLs.add(novalnetURL)) {
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
                   if (lang.equals("DE")){
                    writeDataToSheet_DE(imagealtchecksheetname, new ArrayList<Object>(Arrays.asList(novalnetURL, "YES", imageURL, altValue)), DE_xl);
                }
                   else {
                       writeDataToSheet_DE(imagealtchecksheetname, new ArrayList<Object>(Arrays.asList(novalnetURL, "YES", imageURL, altValue)), EN_xl);
                   }
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
}



