package org.example;

import org.junit.Test;

import java.util.*;
import java.util.function.Function;

public class Jifen {
    @Test
    public void ff() {
        int testCount = 100000;
        int result = 0;
        for (int i = 0; i < testCount; i++) {
            result += f();
        }
        System.out.println(result);
        System.out.println(result / (testCount * 100.0) * 100);
    }

    int f() {
        int arr[] = new int[100];
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            int index = 0;
            if (random.nextBoolean()) {
                index = (i + 100 - 1) % 100;
            } else {
                index = (i + 100 + 1) % 100;
            }
            arr[index]++;
        }
        int count = 0;
        for (int i = 0; i < 100; i++) {
            if (arr[i] == 0) {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (in.hasNext()) { // 注意 while 处理多个 case
            String s = in.next();
            int n = in.nextInt();
            for (int i = 0; i < n; i++) {
                int start = in.nextInt();
                int length = in.nextInt();
                String beforeSub = s.substring(0, start);
                String sub = s.substring(start, start + length);
                String afterSub = s.substring(start + length);
                StringBuffer stringBuffer = new StringBuffer();
                append(stringBuffer, beforeSub, sub, revert(sub), afterSub);
                s = stringBuffer.toString();
            }
            System.out.println(s);
        }
    }

    static String revert(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        char c[] = new char[s.length()];
        for (int i = 0; i < s.length(); i++) {
            c[i] = s.charAt(s.length() - 1 - i);
        }
        return new String(c);
    }

    static void append(StringBuffer stringBuffer, String... s) {
        for (String s1 : s) {
            if (s1 != null) {
                stringBuffer.append(s1);
            }
        }
    }

    @Test
    public void ff2() {
        System.out.println(
                jiFen(
                        -1,
                        1,
                        x -> Math.sqrt(1 - x * x),
                        0.0000001
                )
        );
        //x2+y2=1
    }

    double jiFen(double xStart, double xEnd, Function<Double, Double> func, double jinDu) {
        double result = 0;
        for (double x = xStart; x < xEnd; x += jinDu) {
            double y = func.apply(x);
            result += y * jinDu;
        }
        return result;
    }
}
