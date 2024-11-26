package com.nn.basetest;



import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
    public String linkchecksheetname = LocalDate.now()+"_BROKENLINK";
    public String imagechecksheetname =LocalDate.now()+"_BROKENIMAGE";
    private AtomicInteger count = new AtomicInteger(0);

    private static File DE_xl = new File(System.getProperty("user.dir"),"/src/test/resources/DE_HomePage.xlsx");
    private static File EN_xl = new File(System.getProperty("user.dir"),"/src/test/resources/EN_HomePage.xlsx");
    @BeforeMethod
    public void createDriver(){
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            driver.set(new ChromeDriver(options));

            // Set a page load timeout and script timeout
           // driver.get().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60)); // Page load timeout
           // driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(30));  // Implicit wait
          //  driver.get().manage().timeouts().setScriptTimeout(Duration.ofSeconds(60)); // Script timeout
            wait.set(new WebDriverWait(driver.get(),Duration.ofSeconds(30)));
            assert driver != null : "Driver initialization failed!";
            driver.get().manage().window().maximize();
        }catch (Exception e){
            System.err.println("Driver initialization failed: " + e.getMessage());
            throw new RuntimeException("Driver initialization failed", e);
        }
        deleteSheets();

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
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(60));
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
        if(checked200UrlS.add(url)){
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
                System.out.println("url: " + url + ", statusCode: " + statusCode + ", statusMessage: " + statusMessage + ", sourceUrl: " + sourceUrl);
                if (statusCode == 200) {
                    System.out.println(currentCount + ": " + url + ": " + "Link is valid(HTTP response code: " + statusCode + ")");
                        if(lang.equals("DE")){
                            writeDataToSheet_DE(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), DE_xl);
                        }else {
                            writeDataToSheet_EN(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl)), EN_xl);
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
    public void checkSublinks(String source_URL,String lang) throws IOException, GeneralSecurityException {
        driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(120));
            List<WebElement> innerLinks = driver.get().findElements(By.tagName("a"));
            for (WebElement innerLink : innerLinks) {
                String subUrl = innerLink.getAttribute("href");
                if (subUrl != null && !subUrl.isEmpty()
                        && (subUrl.startsWith("https://") || subUrl.startsWith("http://"))) {
                    verifyLink(source_URL, subUrl, linkchecksheetname,lang);
                }

            }
        }


    public void waitForAllElementLocated(By by){
        getWait().until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
    }

    private List<String> getAllNovalnetLinks(String URL) {
        List<String> novalnetLinks = new ArrayList<>();
        List<String> site_map_urls = get_siteMap_urls(URL);
        for(String novalnetLink: site_map_urls){
            openURL(novalnetLink);
            driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(60));
            waitForAllElementLocated(By.tagName("a"));
            List<WebElement> allLinks = driver.get().findElements(By.tagName("a"));
            for (WebElement link : allLinks) {
                String href = link.getAttribute("href");
                if (href != null && href.contains("novalnet")) {
                    novalnetLinks.add(href);
                }
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

    public void verifyCompleteLinks(String URL,String lang) throws GeneralSecurityException, IOException {
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
    }
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

    public void openURL(String url){
       getDriver().get(url);
        System.out.println("Open URL: " + url);
    }

    public void verifyBrokenImages(String URL,String lang) throws GeneralSecurityException, IOException {
        List<String> siteMap_inner_urls = getAllNovalnetLinks(URL);
        for (String url:siteMap_inner_urls){
            openURL(url);
            System.out.println("Novalnet link" + url);
            verifyBrokenImages_(url,lang);
        }

    }

    public void verifyBrokenImages_(String source_URL,String lang) throws GeneralSecurityException, IOException {
        waitForAllElementLocated(By.tagName("img"));
        List<WebElement> imgTags = driver.get().findElements(By.tagName("img"));
        for (WebElement value : imgTags) {
            String imageurl = value.getAttribute("src");
            if (imageurl != null && (imageurl.contains("https") || imageurl.contains("http"))) {
                verifyLink(source_URL, imageurl, imagechecksheetname,lang);

            }
        }
    }
}
