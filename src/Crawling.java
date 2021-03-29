import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.io.IOException;
import java.lang.reflect.Array;
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
        Thread.sleep(5000);
        
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
//        WebElement scrollBox = driver.findElement(By.className("_2lx2y"));

        Actions builder = new Actions(driver);
//        Actions hoverOverRegistrar = builder.sendKeys(scrollBox, Keys.PAGE_DOWN);
        //커서를 올리고 스크롤을 내리는 액션 체인 생성, 실행
        for(int i = 0; i < 1; i++){
////            hoverOverRegistrar.perform(); // 1초 간격 6번 실행
            builder.sendKeys(scrollBox, Keys.END).build().perform();
            Thread.sleep(2000);
////            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", scrollBox);

        }


        // 사이트에서 전체 매장을 찾은 뒤 코드를 읽는다
        List<WebElement> elements = driver.findElements(By.className("_3Yilt"));
//        List<WebElement> elements = driver.findElements(By.xpath("//*[@id=\"baseMap\"]/div/div[1]/div[4]/*/salt-marker/div/div/div[2]/div[1]/strong"));

        for (WebElement e : elements){
            System.out.println(e.getText());
            //매장명을 키값으로 해시맵 생성
            coffeeInfoms.put(e.getText(), new ArrayList<>());
        }

        //매장을 하나씩 클릭하고 주소를 읽는다
        for(WebElement e : elements){
            e.click();
            String key = e.getText();
            Thread.sleep(2000);
            driver.switchTo().parentFrame(); //부모 프레임으로 이동
            driver.switchTo().frame(driver.findElement(By.id("entryIframe"))); //옆 iframe으로 이동

            //주소
            WebElement addEle = driver.findElement(By.className("_2yqUQ"));
//            System.out.println(addEle.getText());
            ArrayList<String> addTemp = coffeeInfoms.get(key);
            addTemp.add(0, addEle.getText());
            coffeeInfoms.put(key, addTemp);

            //전화번호 있는 경우
            try{
                WebElement callEle = driver.findElement(By.className("_3ZA0S"));
//                System.out.println(callEle.getText());
                ArrayList<String> callTemp = coffeeInfoms.get(key);
                callTemp.add(1, callEle.getText());
                coffeeInfoms.put(key, callTemp);
            } catch (Exception ex){
                System.out.println(ex.toString());
                //전화번호 정보 없는 경우 null처리
                ArrayList<String> callTemp = coffeeInfoms.get(key);
                callTemp.add(1, null);
                coffeeInfoms.put(key, callTemp);
            }

            //메뉴정보를 저장할 문자열
            //메뉴와 가격은 ':', 메뉴 간은 ';'로 구분
            String menuInfo = new String();

            try{
                List<WebElement> menuEles = driver.findElements(By.className("_1q3GD"));
                List<WebElement> priceEles = driver.findElements(By.className("_2nGYH"));
                for(int i = 0; i < menuEles.size(); i++){
                    String temp = menuEles.get(i).getText() + ":" + priceEles.get(i).getText() + ";";
                    menuInfo = menuInfo + temp;
                }
            } catch (Exception ex){
                System.out.println("인기 메뉴정보 없음");
            }
            try{
                List<WebElement> menuEles = driver.findElements(By.className("_3K2xG"));
                List<WebElement> priceEles = driver.findElements(By.className("_3GJcI"));
                for(int i = 0; i < menuEles.size(); i++){
                    String temp = menuEles.get(i).getText() + "," + priceEles.get(i).getText() + ";";
                    menuInfo = menuInfo + temp;
                }
            } catch (Exception ex){
                System.out.println("일반 메뉴정보 없음");
            }
            ArrayList<String> menuTemp = coffeeInfoms.get(key);
            menuTemp.add(2, menuInfo);
            coffeeInfoms.put(key, menuTemp);
//            System.out.println(menuInfo);



            driver.switchTo().parentFrame(); //부모 프레임으로 이동
            driver.switchTo().frame("searchIframe"); //원래 iframe으로 이동
        }
    }
    public static void main(String[] args) throws InterruptedException {
        Crawling test = new Crawling();
        
        //크롤링
        test.SeleniumTest();
        System.out.println(test.coffeeInfoms.get("스타벅스 종암DT점"));

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
