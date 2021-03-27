import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.io.IOException;
import java.util.List;

public class Crawling {

    //WebDriver
    private WebDriver driver;
    private WebElement element;
    private String url;

    //Properties
    public static String WEB_DRIVER_ID = "webdriver.chrome.driver";
    public static String WEB_DRIVER_PATH = "C:/Users/tlmag/Desktop/workspace/CoffeeCrawlingJ/chromedriver.exe";

    public void SeleniumTest() throws InterruptedException, IOException {
        //System Property SetUp
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

        //Driver SetUp
        ChromeOptions options = new ChromeOptions();
        options.setCapability("ignoreProtectedModeSettings", true);
        driver = new ChromeDriver(options);

        url = "https://map.naver.com/v5/search/%EC%B9%B4%ED%8E%98?c=14140243.5138456,4523958.9602580,14,0,0,0,dh";
        driver.get(url);
        Thread.sleep(4000);

        // 데이터가 iframe 안에 있는 경우(HTML 안 HTML) 이동
        driver.switchTo().frame("searchIframe");

        // 원하는 요소를 찾기(스크롤할 창)
        WebElement scrollBox = driver.findElement(By.id("_pcmap_list_scroll_container"));

        Actions builder = new Actions(driver);

        //커서를 올리고 스크롤을 내리는 액션 체인 생성, 실행
        for(int i = 0; i < 5; i++){
            Actions hoverOverRegistrar = builder.sendKeys(scrollBox, Keys.END);
            hoverOverRegistrar.perform(); // 1초 간격 6번 실행
            Thread.sleep(1000);
        }

        // 사이트에서 전체 매장을 찾은 뒤 코드를 읽는다
        List<WebElement> elements = driver.findElements(By.className("_3Yilt"));


        for (WebElement e : elements){
            System.out.println(e.getText());
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Crawling test = new Crawling();
        test.SeleniumTest();

//        try {
//
////웹에서 내용을 가져온다.
//
//            Document doc = Jsoup.connect("http://jobc.tistory.com/").get();
//
////내용 중에서 원하는 부분을 가져온다.
//
//            Elements contents = doc.select(".class #id");
//
////원하는 부분은 Elements형태로 되어 있으므로 이를 String 형태로 바꾸어 준다.
//
//            String text = contents.text();
//
//            System.out.println(text);
//
//        } catch (IOException e) { //Jsoup의 connect 부분에서 IOException 오류가 날 수 있으므로 사용한다.
//
//            e.printStackTrace();
//
//        }

    }



}
