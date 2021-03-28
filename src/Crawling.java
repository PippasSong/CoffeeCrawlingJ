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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Crawling {

    //WebDriver
    private WebDriver driver;
    private WebElement element;
    private String url;

    //Properties
    public static String WEB_DRIVER_ID = "webdriver.chrome.driver";
    public static String WEB_DRIVER_PATH = "C:/Users/tlmag/Desktop/workspace/CoffeeCrawlingJ/chromedriver.exe";

    //매장 정보들을 저장할 해시맵
    Map<String, ArrayList<String>> coffeeInfoms = new HashMap<>();

    public void SeleniumTest() throws InterruptedException {
        //System Property SetUp
        System.setProperty(WEB_DRIVER_ID, WEB_DRIVER_PATH);

        //Driver SetUp
        ChromeOptions options = new ChromeOptions();
        options.setCapability("ignoreProtectedModeSettings", true);
        driver = new ChromeDriver(options);
        
        
        //네이버 지도
        url = "https://map.naver.com/v5/";
        driver.get(url);
        Thread.sleep(15000);
        
        //네이버 지도 검색창에 원하는 동명 입력 후 엔터
        WebElement inputSearch = driver.findElement(By.className("input_search"));
        String input = "종암동";
        String inputKey = " 카페";
        inputSearch.sendKeys(input+inputKey);
        Thread.sleep(1000);
        inputSearch.sendKeys(Keys.ENTER);
        Thread.sleep(3000);


//         데이터가 iframe 안에 있는 경우(HTML 안 HTML) 이동
        driver.switchTo().frame("searchIframe");

        // 원하는 요소를 찾기(스크롤할 창)
        WebElement scrollBox = driver.findElement(By.id("_pcmap_list_scroll_container"));

        Actions builder = new Actions(driver);

        //커서를 올리고 스크롤을 내리는 액션 체인 생성, 실행
        for(int i = 0; i < 6; i++){
            Actions hoverOverRegistrar = builder.sendKeys(scrollBox, Keys.END);
            hoverOverRegistrar.perform(); // 1초 간격 6번 실행
            Thread.sleep(1500);
        }

        // 사이트에서 전체 매장을 찾은 뒤 코드를 읽는다
        List<WebElement> elements = driver.findElements(By.className("_3Yilt"));


        for (WebElement e : elements){
            System.out.println(e.getText());
        }

        //매장을 하나씩 클릭하고 주소를 읽는다
        for(WebElement e : elements){
            e.click();
            Thread.sleep(2000);
            driver.switchTo().parentFrame(); //부모 프레임으로 이동
            driver.switchTo().frame(driver.findElement(By.id("entryIframe"))); //옆 iframe으로 이동

            //주소
            WebElement addEle = driver.findElement(By.className("_2yqUQ"));
            System.out.println(addEle.getText());

            //전화번호 있는 경우
            try{
                WebElement callEle = driver.findElement(By.className("_3ZA0S"));
                System.out.println(callEle.getText());
            } catch (Exception ex){
                System.out.println(ex.toString());
                //전화번호 정보 없는 경우 null처리
            }



            driver.switchTo().parentFrame(); //부모 프레임으로 이동
            driver.switchTo().frame("searchIframe"); //원래 iframe으로 이동
        }
    }
    public static void main(String[] args) throws InterruptedException {
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
