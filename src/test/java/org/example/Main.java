package org.example;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {
    //    RestTemplate restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    RestTemplate restTemplate = new RestTemplate();

//    public static OkHttpClient okHttpClient() {
//        return new OkHttpClient.Builder()
//                //.sslSocketFactory(sslSocketFactory(), x509TrustManager())
//                .retryOnConnectionFailure(false)
//                .connectionPool(new ConnectionPool(200, 5, TimeUnit.MINUTES))
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .readTimeout(30, TimeUnit.SECONDS)
//                .writeTimeout(30, TimeUnit.SECONDS)
//                .build();
//    }

    @Test
    public void main2() throws UnsupportedEncodingException {
        HttpHeaders httpHeaders = new HttpHeaders();
//        String realUrl = "https://missav.com/genres/%E8%88%94%E9%99%B0";
        String realUrl = "https://missav.com/dsd-877";
        String url = "http://localhost:8888/?p=" + realUrl;
//        String url = "http://localhost:8888/?p=" + URLEncoder.encode(realUrl, "UTF-8");
        HttpEntity<String> requestEntity = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<String> resEntity = restTemplate.getForEntity(url, String.class);
        System.out.println(resEntity.getBody());
    }
//
//    @Test
//    public void main() throws FileNotFoundException {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        Scanner scanner = new Scanner(new FileReader("/Users/leishuai/IdeaProjects/untitled1/src/main/java/org/example/missav/tmp.tmp"));
//        while (scanner.hasNextLine()) {
//            String[] line = scanner.nextLine().split(":\\s");
//            httpHeaders.add(line[0], line[1]);
//        }
//        String url = "https://missav.com/makers/Crystal-Eizou2";
//        HttpEntity<String> requestEntity = new HttpEntity<>(null, httpHeaders);
//        ResponseEntity<String> resEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
//        System.out.println(resEntity.getBody());
//    }
//
//
//    @Test
//    public void main3() throws FileNotFoundException {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        Scanner scanner = new Scanner(new FileReader("/Users/leishuai/IdeaProjects/untitled1/src/main/java/org/example/missav/postman.sh"));
//        String url = "https://orion-http.gw.postman.co/v1/request";
//        HttpMethod method = HttpMethod.POST;
//        while (scanner.hasNextLine()) {
//            String newLine = scanner.nextLine();
//            if (newLine.contains("curl")) {
//                url = newLine.split("'")[1];
//                continue;
//            }
//            if (newLine.contains("-X")) {
//                method = HttpMethod.valueOf(newLine.split("'")[1]);
//                continue;
//            }
//            if (newLine.contains("--")) {
//                continue;
//            }
//            newLine = newLine.replaceAll("' \\\\", "").replaceAll("  -H '", "");
//            String[] line = newLine.split(":\\s");
//            httpHeaders.add(line[0], line[1]);
//        }
//        HttpEntity<String> requestEntity = new HttpEntity<>(null, httpHeaders);
//        ResponseEntity<String> resEntity = restTemplate.exchange(url, method, requestEntity, String.class);
//        System.out.println(resEntity.getBody());
//    }

    @Test
    public void ff() {
        List<String> set1 = getAllLine("/Users/leishuai/IdeaProjects/untitled1/src/main/java/org/example/missav/cookie.txt");
        List<String> set2 = getAllLine("/Users/leishuai/IdeaProjects/untitled1/src/main/java/org/example/missav/cookie2.txt");
        set1.stream().filter(set2::contains).forEach(System.out::println);
    }

    List<String> getAllLine(String file) {
        Scanner scanner = null;
        List<String> set = new ArrayList<>();
        try {
            scanner = new Scanner(new FileReader(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while (scanner.hasNextLine()) {
            set.add(scanner.nextLine());
        }
        return set;
    }
}
