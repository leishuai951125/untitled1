package chaogu;

import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
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
            String arr[] = line.split("\\s+");
            code2Name.put(arr[0], arr[1]);
        }
        System.out.println(code2Name);
        List<BankuaiData> list2list = code2Name.keySet().parallelStream().map(code -> {
            return getLast30DayData(successCodePrefixMap.get(code.substring(0, 3)) + "." + code);
        }).filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println(list2list.size());

        //1.512200 房地产 ， 1.561600 消费电子 ,  1.515790 光伏 , 1.516020 化工 ， 食品 1.515170 , 1.515880 通信 ， 1.516010 游戏
        BankuaiData etfList = getLast30DayData("1.512200");
        BankuaiData etf300List = getLast30DayData("1.515790");

        int lastZhangDieTingCount = 999;
        double celue1ShouYiSum = 1;
        double allShiYanShouYiSum = 1;
        double celue2ShouYiSum = 1;
        double allDuiZhaoShouYiSum = 1;
        for (int dayIndex = 1; dayIndex < etfList.detailList.size() - 1; dayIndex++) {
            double bankuaiMingRiShouYi = etfList.detailList.get(dayIndex + 1).end / etfList.detailList.get(dayIndex).end - 1;
            double bankuai300MingRiShouYi = etf300List.detailList.get(dayIndex + 1).end / etf300List.detailList.get(dayIndex).end - 1;
            bankuai300MingRiShouYi *= 1;
            int zhangting = 0;
            int dieting = 0;
            Set<String> dateSet = new HashSet<>();
            String date = list2list.get(1).detailList.get(dayIndex).date;
            dateSet.add(date);
            for (int gupiaoIndex = 0; gupiaoIndex < list2list.size(); gupiaoIndex++) {
                shangZheng.Main.OneDayDataDetail oneDay = list2list.get(gupiaoIndex).detailList.get(dayIndex);
                if (!dateSet.contains(oneDay.date)) {
                    System.out.println("日期有问题:" + gupiaoIndex);
                    continue;
                }
                dateSet.add(oneDay.date);
                shangZheng.Main.OneDayDataDetail lastDay = list2list.get(gupiaoIndex).detailList.get(dayIndex - 1);
                if (list2list.get(gupiaoIndex).code.startsWith("0")) {
                    if (oneDay.end / lastDay.end > 1.19) {
                        zhangting++;
                    } else if (oneDay.end / lastDay.end < 0.81) {
                        dieting++;
                    }
                } else {
                    if (oneDay.end / lastDay.end > 1.095) {
                        zhangting++;
                    } else if (oneDay.end / lastDay.end < 0.905) {
                        dieting++;
                    }
                }
            }

            int zhangdieTingCount = zhangting - dieting;
            String color;
            String desc;
            if (zhangdieTingCount >= lastZhangDieTingCount) {
                color = ANSI_RED;
                desc = "买入";
                celue1ShouYiSum *= 1 + bankuaiMingRiShouYi;
            } else {
                color = ANSI_GREEN;
                desc = "不买";
                celue1ShouYiSum *= 1 + bankuai300MingRiShouYi;
            }
            allShiYanShouYiSum *= 1 + bankuaiMingRiShouYi;
            allDuiZhaoShouYiSum *= 1 + bankuai300MingRiShouYi;
            if (zhangdieTingCount >= 0) {
                celue2ShouYiSum *= 1 + bankuaiMingRiShouYi;
            } else {
                celue2ShouYiSum *= 1 + bankuai300MingRiShouYi;
            }
            lastZhangDieTingCount = zhangdieTingCount;
            System.out.printf(color + "日期：%s,涨停: %d ,跌停: %d %s， 明日收益：%.2f%% ," +
                            "策略1总收益：%.2f%% ,全实验组总收益：%.2f%% ,策略2总收益：%.2f%% ,全对照组总收益：%.2f%% \n" + ANSI_RESET,
                    date, zhangting, dieting, desc, bankuaiMingRiShouYi * 100, celue1ShouYiSum * 100 - 100,
                    allShiYanShouYiSum * 100 - 100, celue2ShouYiSum * 100 - 100, allDuiZhaoShouYiSum * 100 - 100);
        }
        System.out.println("成功:" + successSet);
        System.out.println("失败：" + failSet);
    }

    static Set<String> successSet = new HashSet<>();
    static Set<String> failSet = new HashSet<>();

    static Map<String, String> successCodePrefixMap = new HashMap<>();

    static {
        String successCodeJSONArr = "1.600, 1.688, 1.512, 1.601, 1.603, 1.000, 1.561, 0.300, 0.301, 90.BK0, 90.BK1";
        Arrays.stream(successCodeJSONArr.split(", ")).forEach(sub -> {
            String[] sub2 = sub.split("\\.");
            successCodePrefixMap.put(sub2[1], sub2[0]);
        });
    }

    @AllArgsConstructor
    public static class BankuaiData {
        String code;
        String name;
        List<shangZheng.Main.OneDayDataDetail> detailList;
    }

    //    https://push2his.eastmoney.com/api/qt/stock/kline/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61&beg=0&end=20500101&ut=fa5fd1943c7b386f172d6893dbfba10b&rtntype=6&secid=1.000558&klt=101&fqt=1&cb=jsonp1732998219242
//    https://push2his.eastmoney.com/api/qt/stock/kline/get?cb=jQuery35105715717072793236_1732997335203&secid=1.000558&ut=fa5fd1943c7b386f172d6893dbfba10b&fields1=f1%2Cf2%2Cf3%2Cf4%2Cf5%2Cf6&fields2=f51%2Cf52%2Cf53%2Cf54%2Cf55%2Cf56%2Cf57%2Cf58%2Cf59%2Cf60%2Cf61&klt=101&fqt=1&end=20500101&lmt=120&_=1732997335253
    private static BankuaiData getLast30DayData(String bankuaiCode) {
        JSONArray jsonArray = null;
        try {
            jsonArray = Main.getDayData(bankuaiCode);
            successSet.add(bankuaiCode.substring(0, 5));
//            System.out.println("成功" + bankuaiCode);
            if (jsonArray.size() < 100) {
                System.out.println("<100 " + bankuaiCode);
                return null;
            }
        } catch (Exception e) {
            failSet.add(bankuaiCode.substring(0, 5));
//            System.out.println("失败" + bankuaiCode);
//            throw new RuntimeException(e);
            return null;
        }
//        System.out.println("成功" + bankuaiCode);
        List<String> list = jsonArray.stream().map(e -> (String) e).collect(Collectors.toList());
        List<shangZheng.Main.OneDayDataDetail> detailList = Utils.parseDongFangCaiFuList(list);
        return new BankuaiData(bankuaiCode, "", detailList.subList(detailList.size() - 50, detailList.size() - 1));
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
}

//查板块个股
//https://33.push2.eastmoney.com/api/qt/clist/get?cb=jQuery112404930637717184252_1733073559136&pn=1&pz=20&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&fltt=2&invt=2&dect=1&wbp2u=|0|0|0|web&fid=f3&fs=b:BK1036+f:!50&fields=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f22,f11,f62,f128,f136,f115,f152,f45&_=1733073559152