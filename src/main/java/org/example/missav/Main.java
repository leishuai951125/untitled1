package org.example.missav;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 22321323121
 * 13545981618
 */
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    static RestTemplate restTemplate = new RestTemplate();

    public static void main2(String[] args) throws UnsupportedEncodingException {

    }

    //    public static void main(String[] args) throws FileNotFoundException {
//        HttpHeaders httpHeaders = new HttpHeaders();
//        Scanner scanner = new Scanner(new FileReader("/Users/leishuai/IdeaProjects/untitled1/src/main/java/org/example/missav/tmp.tmp"));
//        while (scanner.hasNextLine()) {
//            String[] line = scanner.nextLine().split(":\\s");
//            httpHeaders.add(line[0], line[1]);
//        }
//        String url = "https://orion-http.gw.postman.co/v1/request";
//        HttpEntity<String> requestEntity = new HttpEntity<>(null, httpHeaders);
//        ResponseEntity<String> resEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
//        System.out.println(resEntity.getBody());
//    }

    public static class Person {
        static int count = 10000000;
        int age;
        String name;

        public Person() {
        }

        public Person(int age2, String name) {
            /*age = age;*/
//            System.out.println("this.age:" + this.age);
            this.age = age2;
////            System.out.println("this.age:" + this.age);
            this.name = name;
        }

        public int getAge() {
            return this.age;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "age=" + age +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public static void ff(int a, int b) {
        //
        int temp = a;
        a = b;
        b = temp;
//        System.out.println(c);
        System.out.println(a);
        System.out.println(b);
        //
    }

    public static void main(String[] args) {
        int a = 1;
        int b = 2;
        int c = 3;
//        System.out.println(a);
//        System.out.println(b);
//        int temp = a;
//        a = b;
//        b = temp;
        ff(a, b);
//        System.out.println(a);
//        System.out.println(b);

//        int a = 1;
//        a = 2;
//        a = 3;
//        System.out.println(a);

//        isChengNianRen(18);
//        isChengNianRen(20);
//
        Person person1 = new Person(10, "zhangsan");
////
//        System.out.println(person1);
////
        Person person2 = new Person(28, "lisi");

        int ageSum = person1.age + person2.age;

//        System.out.println(ageSum);
////
//        System.out.println(person2);

        //        person.age = 10;
//        Person person1 = new Person();
//        person1.age = 10;
//        person1.name = "xxx";

//        int a = 1;
//        int age = person.getAge();

//        int age2 = person2.getAge();

//        int age = 0;
//        System.out.println("age:" + age);
////       age=10;
//        Student student = new Student();
////        Student.age=90;
//        Student.count = 1000000;
//        student.count = 1000;
//        student.age = 90;
//        student.name = "zahngsan";
//        student.score = 100;

        System.out.println();

//        Student student1=new Student(90,"zahngsan",100);
//
//        System.out.println(student.getAge());

//        Student[] students = new Student[50];

//        String[] arr = new String[]{"22321323121", "13545981618", "2378324"};
//        String[] arr = new String[3];
//        arr[0] = "22321323121";
//        arr[1] = "13545981618";
//        arr[2] = "2378324";
//
//        int[] arrInt = new int[]{12, 34, 34};
//        for (int i = 0; i < 3; i++) {
//            String s = arr[i];
//            boolean isPoneNumber = Pattern.compile("1\\d{10}").matcher(s).find();
//            if (isPoneNumber) {
//                System.out.println(s);
//            }
//        }

//        Pattern.compile("1\\d{10}").matcher("13545981618").find();

//
//        Scanner scanner = new Scanner(System.in);
//        while (true) {
//            int a = scanner.nextInt();
//            int b = scanner.nextInt();
//            int c = scanner.nextInt();
//            int p = (a + b + c) / 2;
//            Double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));
//            System.out.println("面积:" + s);
//        }
//        "123".length();
//        int test = 2023;
//        for (int i = 2; i <= 2023 / 2; i++) {
//            if (test % i == 0) {
//                System.out.println("是合数,i:" + i);
////                break;
//            }
//        }
//        String name = "leishuai";
//        System.out.println(name.length());
//        System.out.println("sdfldjlfs".length());
//        int i = 1;
//        while (i <= 100) {
//            sum = sum + i;//sum+=i;
//            i  ++;  //i++
//        }
//
//        int sum = 0;
//        for (int i = 1; i <= 100; i++) {
//            sum += i;
//            if (i > 10) {
//                continue;
//            }
//            System.out.println("i: " + i);
//        }

//        System.out.println(sum);


//        String ageS = "1111111";
//        int age2 = Integer.parseInt(ageS);
//        char c = 'l';
//        String name = "leishuai";
//        String isTiancai = " shi tian cai";
//        String s = name + isTiancai;
//        name = name + isTiancai;
//        name += isTiancai;
//        System.out.println(s);
//        boolean is = true;
//        if (age > 18) {
//            System.out.println("age:" + age);
//        }


//        isChengNianRen(20);
//        isChengNianRen(20);
//        isChengNianRen(15);

//        int sum = add(1, 3);
//        int sum2 = add(4, 3);
//

//        System.out.println(result);
//        System.out.println("=====");
//        int a = rand();
//        System.out.println(a);
//        System.out.println(rand());
//        int a=1,b=2;
//        boolean cc= a>b && !is ;
    }

    public static void isChengNianRen(int age) { //入参
        boolean isWeiChenng = age < 18;
        if (!isWeiChenng) {
            System.out.println("是成年人");
        } else {
            System.out.println("不是成年人");
        }
    }

    public static int add(int a, int b) {
        return 0;
//        return a + b;
    }

    public static int rand() {
        return new Random().nextInt();
    }


    public static class Student extends Person {
        static int sumScore;
        int score;

        public Student() {
            age = 1;
        }

        public Student(int age, String name, int score) {
            this.age = age;
            this.name = name;
            this.score = score;

            super.age = age;
            this.age = age;
        }


    }

    public static String kkkk(int a) {
        if (a % 2 == 0) {
            return "是偶数";
        } else {
            return "是奇数";
        }
    }
}
