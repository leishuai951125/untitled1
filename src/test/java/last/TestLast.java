package last;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import sun.security.x509.GeneralName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestLast {
    static boolean shouxuei = false;

    public static void main(String[] args) throws IOException {
        String jsonBody = new String(Files.readAllBytes(Paths.get("/Users/leishuai/IdeaProjects/untitled1/src/test/java/last/tmp.txt")));
        List<Double> doubleList = JSON.parseArray(jsonBody, Double.class);
//        doubleList = doubleList.stream().map(e -> e / 100).collect(Collectors.toList());
//        doubleList = Lists.partition(doubleList, 5).stream().map(sub -> sub.stream().mapToDouble(e -> e).sum()).collect(Collectors.toList());
        double maxShouYi = 0;
        for (int i = 1; i <= 10; i++) {
            for (int j = -15; j <= -5; j++) {
                double shouyi = getShouYi(doubleList, 9 + i, j * 0.3, false);
                if (shouyi > maxShouYi) {
                    maxShouYi = shouyi;
                }
                if (shouyi > 2) {
                    System.out.printf("%d,%d,%.2f%%\n", i, j, shouyi * 100 - 100);
                }
            }
        }
        System.out.println(maxShouYi * 100 - 100);
        getShouYi(doubleList, 15, -2.4, true);
    }

    private static double getShouYi(List<Double> doubleList, int testCounnt, double testBiLi, boolean print) {
        double sum1 = 1;
        int cnt1 = 0;
        double sum2 = 1;
        int cnt2 = 0;
        double sum3 = 1;
        int lastBuy = -1;
        for (int i = 1; i < doubleList.size(); i++) {
            sum1 *= doubleList.get(i) + 1;
            cnt1++;
            if (doubleList.get(i - 1) > 0 && canBuy(doubleList, i, testCounnt, testBiLi)) {
                if (lastBuy == i - 1 || !shouxuei) {
                    sum2 *= doubleList.get(i) + 1;
                } else {
                    sum2 *= doubleList.get(i) + 1 - 0.0003;
                }
                lastBuy = i;
                cnt2++;
            }
            if (doubleList.get(i) > 0) {
                sum3 *= doubleList.get(i) + 1;
            }
            if (print) {
                System.out.printf("策略1交易次数:%d，收益:%.2f%% ; 无脑买策略交易次数:%d，收益:%.2f%% ; 当日收益：%.2f%% ,比值：%.2f%% \n",
                        cnt2, (sum2 - 1) * 100, cnt1, (sum1 - 1) * 100, doubleList.get(i) * 100, sum2 / sum1 * 100);
            }
        }
        if (print) {
            System.out.printf("策略1交易次数:%d，收益:%.2f%% ; 无脑买策略交易次数:%d，收益:%.2f%% ,全对的收益：%.2f%%; \n",
                    cnt2, (sum2 - 1) * 100, cnt1, (sum1 - 1) * 100, (sum3 - 1) * 100);
        }
        return sum2;
    }

    //看看策略是否还可用，可用为1，不可用-1
    static boolean canBuy(List<Double> doubleList, int i, int testCounnt, double testBiLi) {
        return true;
//        if (i < 10) {
//            return true;
//        }
//        int count = testCounnt;
//        int start = Math.max(1, i - count);
//        int end = i;
//        double bodong = doubleList.subList(start, end).stream().mapToDouble(e -> Math.abs(e)).average().getAsDouble();
//        double sum = doubleList.subList(start, end).stream().mapToDouble(e -> e).sum();
//        return sum > testBiLi * bodong;
    }

}
