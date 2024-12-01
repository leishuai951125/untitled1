package chaogu;

import com.alibaba.fastjson.JSONArray;
import org.springframework.util.StringUtils;
import shangZheng.Utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class ZhangTing {
    public static void main(String[] args) throws FileNotFoundException {
        Map<String, String> code2Name = new HashMap<>();
        Scanner sc = new Scanner(new FileReader("/Users/leishuai/IdeaProjects/untitled1/src/test/java/gupiao/tmp.txt"));
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            String arr[] = line.split("	");
            code2Name.put(arr[0], arr[1]);
        }
        System.out.println(code2Name);
        List<List<shangZheng.Main.OneDayDataDetail>> list2list = code2Name.keySet().parallelStream().map(code -> {
            return getLast30DayData("1." + code);
        }).filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println(list2list.size());

        List<shangZheng.Main.OneDayDataDetail> etfList = getLast30DayData("1.512200");
        List<shangZheng.Main.OneDayDataDetail> etf300List = getLast30DayData("1.561600");

        int lastZhangDieTingCount = 999;
        double shouYiSum1 = 0;
        double shouYiSum2 = 0;
        double shouYiSum3 = 0;
        double shouYiSum4 = 0;
        for (int dayIndex = 1; dayIndex < list2list.get(0).size() - 1; dayIndex++) {
            double bankuaiMingRiShouYi = etfList.get(dayIndex + 1).end / etfList.get(dayIndex).end - 1;
            double bankuai300MingRiShouYi = etf300List.get(dayIndex + 1).end / etf300List.get(dayIndex).end - 1;
            bankuai300MingRiShouYi *= 1;
            int zhangting = 0;
            int dieting = 0;
            Set<String> dateSet = new HashSet<>();
            String date = list2list.get(1).get(dayIndex).date;
            dateSet.add(date);
            for (int gupiaoIndex = 0; gupiaoIndex < list2list.size(); gupiaoIndex++) {
                shangZheng.Main.OneDayDataDetail oneDay = list2list.get(gupiaoIndex).get(dayIndex);
                if (!dateSet.contains(oneDay.date)) {
                    System.out.println("日期有问题:" + gupiaoIndex);
                }
                dateSet.add(oneDay.date);
                shangZheng.Main.OneDayDataDetail lastDay = list2list.get(gupiaoIndex).get(dayIndex - 1);
                if (oneDay.end / lastDay.end > 1.095) {
                    zhangting++;
                } else if (oneDay.end / lastDay.end < 0.905) {
                    dieting++;
                }
            }
            int zhangdieTingCount = zhangting - dieting;
            String color = ANSI_RESET;
            if (zhangdieTingCount >= lastZhangDieTingCount) {
                color = ANSI_RED;
                shouYiSum1 += bankuaiMingRiShouYi;
            } else {
                shouYiSum1 += bankuai300MingRiShouYi;
                color = ANSI_GREEN;
            }
            shouYiSum2 += bankuaiMingRiShouYi;
            shouYiSum4 += bankuai300MingRiShouYi;
            if (zhangdieTingCount >= 0) {
                shouYiSum3 += bankuaiMingRiShouYi;
            } else {
                shouYiSum1 += bankuai300MingRiShouYi;
            }
            String desc = zhangdieTingCount > lastZhangDieTingCount ? "买入" : "不买";
            lastZhangDieTingCount = zhangdieTingCount;
            System.out.printf(color + "日期：%s,涨停: %d ,跌停: %d %s， 明日收益：%.2f%% ," +
                            "策略总收益：%.2f%% ,无脑总收益：%.2f%%,策略2总收益：%.2f%%,300 总收益：%.2f%% \n" + ANSI_RESET,
                    date, zhangting, dieting, desc, bankuaiMingRiShouYi * 100, shouYiSum1 * 100, shouYiSum2 * 100, shouYiSum3 * 100, shouYiSum4 * 100);
        }
    }

    //    https://push2his.eastmoney.com/api/qt/stock/kline/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61&beg=0&end=20500101&ut=fa5fd1943c7b386f172d6893dbfba10b&rtntype=6&secid=1.000558&klt=101&fqt=1&cb=jsonp1732998219242
//    https://push2his.eastmoney.com/api/qt/stock/kline/get?cb=jQuery35105715717072793236_1732997335203&secid=1.000558&ut=fa5fd1943c7b386f172d6893dbfba10b&fields1=f1%2Cf2%2Cf3%2Cf4%2Cf5%2Cf6&fields2=f51%2Cf52%2Cf53%2Cf54%2Cf55%2Cf56%2Cf57%2Cf58%2Cf59%2Cf60%2Cf61&klt=101&fqt=1&end=20500101&lmt=120&_=1732997335253
    private static List<shangZheng.Main.OneDayDataDetail> getLast30DayData(String bankuaiCode) {
        JSONArray jsonArray = null;
        try {
            jsonArray = Main.getDayData(bankuaiCode);
            if (jsonArray.size() < 100) {
                System.out.println("<100 " + bankuaiCode);
                return null;
            }
        } catch (Exception e) {
//            System.out.println("失败" + bankuaiCode);
//            throw new RuntimeException(e);
            return null;
        }
//        System.out.println("成功" + bankuaiCode);
        List<String> list = jsonArray.stream().map(e -> (String) e).collect(Collectors.toList());
        List<shangZheng.Main.OneDayDataDetail> detailList = Utils.parseDongFangCaiFuList(list);
        return detailList.subList(detailList.size() - 90, detailList.size() - 10);
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
}
