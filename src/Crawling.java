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
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

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
    ArrayList<String> temp = new ArrayList<>();


    public void crawlMap(String location) throws InterruptedException {
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
        String inputKey = " 카페";
        inputSearch.sendKeys(location +inputKey);
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
            String menuInfo = "";

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
                    String temp = menuEles.get(i).getText() + ":" + priceEles.get(i).getText() + ";";
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

    //db에 해시맵의 정보 업데이트
    public void uploadDB(){
        String driver = "oracle.jdbc.driver.OracleDriver";
        String url = "jdbc:oracle:thin:@//localhost/xe";
        Connection con = null;
        PreparedStatement pstmt = null;
        String query = "MERGE INTO T_CAFE A USING(SELECT NVL(MAX(CAFE_NAME), ' ') CAFE_NAME, NVL(MAX(CAFE_ADDRESS), ' ') CAFE_ADDRESS FROM T_CAFE WHERE CAFE_NAME=? AND CAFE_ADDRESS=?) B ON (A.CAFE_NAME = B.CAFE_NAME AND A.CAFE_ADDRESS = B.CAFE_ADDRESS) WHEN MATCHED THEN UPDATE SET CONTACT_NUMBER=?, MENU_INFO=?,"
        +"UPDATED_DATETIME = SYSDATE, UPDATOR_ID=?, DELETED_YN=? WHEN NOT MATCHED THEN INSERT (CAFE_NAME, CAFE_ADDRESS, CONTACT_NUMBER, MENU_INFO, CREATED_DATETIME, CREATOR_ID, DELETED_YN) VALUES (?,?,?,?,SYSDATE,?,?)";


        try{
            Class.forName(driver); //드라이버 로딩
            con = DriverManager.getConnection(url, "CAFE_MAN", "1234"); //DB연결
            pstmt = con.prepareStatement(query);

            Set set = coffeeInfoms.keySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()){

                String key = (String)iterator.next();
                pstmt.setString(1, key);
                pstmt.setString(2, coffeeInfoms.get(key).get(0));
                pstmt.setString(3, coffeeInfoms.get(key).get(1));
                pstmt.setString(4, coffeeInfoms.get(key).get(2));
                pstmt.setString(5, "admin");
                pstmt.setString(6, "N");
                pstmt.setString(7, key);
                pstmt.setString(8, coffeeInfoms.get(key).get(0));
                pstmt.setString(9, coffeeInfoms.get(key).get(1));
                pstmt.setString(10, coffeeInfoms.get(key).get(2));
                pstmt.setString(11, "admin");
                pstmt.setString(12, "N");

                int cnt = pstmt.executeUpdate();
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
        } finally {
            try{
                pstmt.close();
                con.close();
            } catch (Exception e2){
                System.out.println(e2.getMessage());
            }
        }
    }

   public static void main(String[] args) throws InterruptedException {
        Crawling test = new Crawling();
        
        //크롤링
        test.crawlMap("종암동");
//        System.out.println(test.coffeeInfoms.get("스타벅스 종암DT점"));

        //DB업데이트
        test.uploadDB();

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
