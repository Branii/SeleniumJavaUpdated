import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.safari.ConnectionClosedException;

public class CanadaKeno implements Runnable{
	ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private static final Logger LOGGER = Logger.getLogger(CanadaKeno.class.getName());
	
    private static WebDriver driver;
    private static int counter = 0;
    private static int networkCount = 0;
    private static volatile boolean isDisplayDrawInfoCalled = false;
    static PreparedStatement pstmt;
    
    @Override
	public void run() {
		
    	executor.scheduleAtFixedRate(() -> {
			
    		DateFormat CanadaTime = new SimpleDateFormat("HH:mm:ss");
            CanadaTime.setTimeZone(TimeZone.getTimeZone("America/Vancouver"));
            Date zoneTime = new Date();
            String currentTime = CanadaTime.format(zoneTime);
            
//            LocalTime time = LocalTime.parse(currentTime, DateTimeFormatter.ofPattern("HH:mm:ss")); convert to 24h
//            String time24Hour = time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
//            System.out.println("Time in 24-hour format: " + time24Hour);
        
           
            try {
                
                runKeno(currentTime);
                if (isDisplayDrawInfoCalled) {
                	System.out.println("Sleeping ...");
                    Thread.sleep(10); // Sleep the executor for 5 seconds
                    isDisplayDrawInfoCalled = false; // Reset the flag
                }
                
            } catch (InterruptedException e) {
                System.out.println("Invalid time format: " + e.getMessage());
            } 
            
           
            }, 0, 1, TimeUnit.SECONDS);
		
	}
    
    public static void runKeno(String currentTime) throws InterruptedException,ConnectionClosedException {
        Map<String, String> timeValueMap = createTimeValueMap();
        String canadaTime = timeValueMap.get(currentTime);
        
        if (canadaTime != null) {
        	 String drawcount = timeValueMap.get(currentTime);

        	  String formattedTime = LocalTime.parse(currentTime, DateTimeFormatter.ofPattern("HH:mm:ss"))
                      .format(DateTimeFormatter.ofPattern("hh:mm:ss"));
           
        	 
            try {
                
                setupWebDriver(currentTime);
                System.out.println("Request new data: => [ SystemTime = " + formattedTime + " | SiteTime = " + getDrawTime() + " ]");

                while (true) {
                    String drawTime = getDrawTime();

                    if (drawTime.trim().equals(formattedTime.trim())) {
                        displayDrawInfo(drawcount);
                        break;
                    } else {
                        if (counter >= 5) {  // 5 attempt for retry
                            System.err.println("Timed out!!!");
                            counter = 0;
                            break;
                        } else {
                            retryLogic(currentTime);
                        }
                    }
                }
            } catch (Throwable th) {
            	//LOGGER.log(Level.SEVERE, "Exception occurred", th.getMessage());
            } finally {
                closeWebDriver();
            }
        }
    }

    private static Map<String, String> createTimeValueMap() {
        Map<String, String> timeValueMap = new HashMap<>();
        for (String[] pair : new TimeSet().CanadaTime) {
            timeValueMap.put(pair[0], pair[1]);
        }
        return timeValueMap;
    }

    @SuppressWarnings("deprecation")
	private static void setupWebDriver(String currentTime) throws InterruptedException {
    	
    	try {
    	
            System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--remote-allow-origins=*");

            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
            driver.manage().timeouts().pageLoadTimeout(100, TimeUnit.SECONDS);
            driver.get("https://www.playnow.com/keno/winning-numbers/");
            driver.findElement(By.className("keno__winning-numbers__controls__view__list")).click();
       
            
        } catch (org.openqa.selenium.WebDriverException e) {
            if (e.getMessage().contains("Connection reset")) {
            	networkCount++;
            	 if (networkCount >= 5) {
            		 System.out.println("Could not connect to host check your internet!");
                     networkCount = 0;
                     return;
                    
                 } else {
                	 System.out.println("Connection reset occurred. Retrying...");
                     retryLogic(currentTime);
                 }
            }
        }

    }

    private static String getDrawTime() {
        List<WebElement> parent = driver.findElements(By.className("keno__winning-numbers__results__draws__draw-result"));
        WebElement childElement = parent.get(1);
        WebElement drawTimeX = childElement.findElement(By.className("keno__winning-numbers__results__draws__draw-result__draw-time"));
        return drawTimeX.getAttribute("textContent").split(" ")[2];
    }

    private static void displayDrawInfo(String drawcount) throws IOException, ParseException {
    	
         List<WebElement> parent = driver.findElements(By.className("keno__winning-numbers__results__draws__draw-result"));
         WebElement childElement = parent.get(1);
         WebElement drawNumberX =  childElement.findElement(By.className("keno__winning-numbers__results__draws__draw-result__list-numbers"));
         WebElement drawPeriod =  childElement.findElement(By.className("keno__winning-numbers__results__draws__draw-result__draw-number"));
         WebElement drawTimeX   =  childElement.findElement(By.className("keno__winning-numbers__results__draws__draw-result__draw-time"));
         String[] splitData = drawNumberX.getText().split(" ");
         String Number= String.join(",", splitData);
         
         DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         LocalDate today = LocalDate.now();
         DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("HH:mm:ss");
         LocalDateTime currTime = LocalDateTime.now();
         
         String drawDate = drawPeriod.getAttribute("textContent").split(" ")[3];
         String drawTime = drawTimeX.getAttribute("textContent").split(" ")[2];;
         String drawNumber = Number;
         String drawCount = drawcount;
         String DateCreated = today.format(formatDate);
         String drawClient = "box";
         String drawGet = formatTime.format(currTime);
         String DrawTable = "draws_tbl";
         
         InsertNewData(DrawTable, drawDate, drawTime, drawNumber, drawCount, DateCreated, drawClient, drawGet);
         isDisplayDrawInfoCalled = true;
         counter = 0;
      
    }

    private static void retryLogic(String currentTime) throws InterruptedException {
        System.out.println("Retrying...");
        counter++;
        closeWebDriver();
        runKeno(currentTime); // Recursive call replaced with loop/conditional retry
    }

    private static void closeWebDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    public static void InsertNewData(String Tablename, String drawDate,String drawTime,String drawNumber,String drawCount,String DateCreated, String drawClient, String drawGet) throws IOException, ParseException{

	try (Connection connection = Config.getConnection()){
	    
	String insertSQL  = "INSERT INTO " + Tablename + " (draw_date,draw_time,draw_number,draw_count,date_created,client,get_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
	pstmt = connection.prepareStatement(insertSQL);
	pstmt.setString(1,drawDate);
	pstmt.setString(2,drawTime);
	pstmt.setString(3,drawNumber);
	pstmt.setString(4,drawCount);
	pstmt.setString(5,DateCreated);
	pstmt.setString(6,drawClient);
	pstmt.setString(7,drawGet);
	pstmt.executeUpdate();
	System.out.println("**** Inserted **** => Time: " + drawTime);
	pstmt.close();
	connection.close();
	  
	} catch (SQLException e) {
		LOGGER.log(Level.INFO, "Exception occurred", e.getMessage());
  }
 }
}
