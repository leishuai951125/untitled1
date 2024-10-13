package org.example;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://jsonplaceholder.typicode.com/posts/1";
        String str = restTemplate.getForObject(url, String.class);
        System.out.println(JSON.parseObject(str,Temp.class));
        new Temp().getBody();
    }
    public static int add(int a,int b){
        return a+b;
    }
}
