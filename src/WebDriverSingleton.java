import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class WebDriverSingleton {
    private static WebDriver driverInstance;

    public static WebDriver getWebDriverInstance() {
        if (driverInstance == null) {
            // Set up WebDriver (if not already instantiated)
            System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            driverInstance = new ChromeDriver(options);
        }
        return driverInstance;
    }

    public static void quitWebDriverInstance() {
        if (driverInstance != null) {
            driverInstance.quit();
            driverInstance = null;
        }
    }
}
