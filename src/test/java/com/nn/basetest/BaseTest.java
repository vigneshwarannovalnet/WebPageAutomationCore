package com.nn.basetest;


import com.nn.testcase.homepage.Test;
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
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseTest extends Test {


    private ChromeDriver driver;
    private Set<String> checked200UrlS = new HashSet<>();
    private  WebDriverWait wait ;



    public String linkchecksheetname ="BROKENLINK";
    public String imagechecksheetname ="BROKENIMAGE";
    public String metachecksheetname ="META-CHECK";
    public String H1tagchecksheetname ="H1TAG-CHECK";
    public String imagealtchecksheetname ="IMAGEALT-CHECK";
    public String canonicalTagssheetname ="canonical-CHECK_DE";

    private AtomicInteger count = new AtomicInteger(0);

    private static File xl = new File(System.getProperty("user.dir"),"/src/test/resources/HomePage.xlsx");
    @BeforeSuite
    public void createDriver(){
        System.out.println("Inside createdriver method staritng point");
        ChromeOptions options = new ChromeOptions();
      options.addArguments("--headless");
         driver = new ChromeDriver(options);
         wait = new WebDriverWait(driver,Duration.ofSeconds(30));
        assert driver != null : "Driver initialization failed!";
        System.out.println("Inside createdriver method ending point");
        // driver.manage().window().maximize();
    }

    @AfterSuite
    public void quitDriver(){
        if(driver!=null){
            driver.quit();;
        }
    }

    @BeforeTest
    public ChromeDriver getDriver(){

        if(driver==null) {
        throw new IllegalStateException("Driver is not initialized!");
        }
        return driver;
    }

    public void checkAllLinks() throws IOException, GeneralSecurityException {


        List<WebElement> allLinks = driver.findElements(By.tagName("a"));
        for (WebElement link : allLinks) {
            String href = link.getAttribute("href");
            if (href != null && !href.isEmpty() && href.contains("novalnet")) {
                verifyLink( "N/A", href,linkchecksheetname ,"Broken_Link");
            }
        }

    }

    public void waitForTitleContains(String title){

        wait.until(ExpectedConditions.titleContains(title));
    }

    private void verifyLink(String sourceUrl, String url ,String sheetname ,String purpose) throws IOException, GeneralSecurityException {

        boolean result =false;
        for(String successURs :checked200UrlS){
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
                    if(checked200UrlS.add(url)){
                        writeDataToSheet(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl,purpose)), xl);
                    }

                } else {
                    writeDataToSheet(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl,purpose)), xl);
                    System.err.println(currentCount + ": " + url + ": " + "Link is broken (HTTP response code: "
                            + statusCode + ")");
                }
            } catch (Exception e) {
                if (statusCode == 0) {
                    writeDataToSheet(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, "null", sourceUrl,purpose)), xl);
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                } else {
                    writeDataToSheet(sheetname, new ArrayList<Object>(Arrays.asList(url, statusCode, statusMessage, sourceUrl,purpose)), xl);
                    System.err.println(currentCount + ": " + url + ": " + "Exception occurred: " + statusMessage);
                }
            }
        }

    }
    public synchronized void writeDataToSheet(String sheetName, List<Object> data, File filePath) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
            }

            int nextRowNum = getNextRow(sheet);
            Row row = sheet.createRow(nextRowNum);

            int cellNum = 0;
            for (Object value : data) {
                Cell cell = row.createCell(cellNum++);
                if (value instanceof String) {
                    cell.setCellValue((String) value);
                } else if (value instanceof Integer) {
                    cell.setCellValue((Integer) value);
                } else {
                    cell.setCellValue(value.toString());
                }
            }

            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
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
            driver.get(url);
            waitForAllElementLocated(By.tagName("a"));
            List<WebElement> innerLinks = driver.findElements(By.tagName("a"));
            waitForAllElementLocated(By.tagName("img"));
            List<WebElement> imgTags = driver.findElements(By.tagName("img"));
            for (WebElement innerLink : innerLinks) {
                String subUrl = innerLink.getAttribute("href");
                if (subUrl != null && !subUrl.isEmpty()
                        && (subUrl.startsWith("https://") || subUrl.startsWith("http://"))) {
                    verifyLink(url, subUrl, linkchecksheetname,"Broken_Link");
                }

            }
            for (WebElement value : imgTags) {
                String imageurl = value.getAttribute("src");
                if (imageurl != null && (imageurl.contains("https") || imageurl.contains("http"))) {
                    verifyLink(url, imageurl, imagechecksheetname,"Broken_Image");

                }
            }

        }
    }

    public void waitForAllElementLocated(By by){
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
    }

    private List<String> getAllNovalnetLinks() {
       waitForAllElementLocated(By.tagName("a"));
        List<WebElement> allLinks = driver.findElements(By.tagName("a"));
        List<String> novalnetLinks = new ArrayList<>();

        for (WebElement link : allLinks) {
            String href = link.getAttribute("href");
            if (href != null && href.contains("novalnet")) {
                novalnetLinks.add(href);
            }
        }

        return novalnetLinks;
    }
}
