package chaogu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.util.StringUtils;
import shangZheng.Utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
        List<GuPiaoData> list2list = code2Name.keySet().parallelStream().map(code -> {
            return getLast30DayData(successCodePrefixMap.get(code.substring(0, 3)) + "." + code);
        }).filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println(list2list.size());

        //1.512200 房地产 ， 1.561600 消费电子 ,  1.515790 光伏 , 1.516020 化工 ， 食品 1.515170 , 1.515880 通信 ， 1.516010 游戏
        // 90.BK1036 半导体
        // 90.BK1031 光伏
        GuPiaoData etfList = getLast30DayData("90.BK1036");
        GuPiaoData etf300List = getLast30DayData("90.BK1031");

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
                List<String> kechuang = Arrays.asList("1.688", "1.689", "1.300", "1.301");
                if (kechuang.contains(list2list.get(gupiaoIndex).code.substring(0, 5))) {
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


    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";


    static List<String> dateList = getLast30DayData("1.000300").detailList.stream().map(e -> e.getDate()).collect(Collectors.toList());

    @Test
    public void ff2() {

        GuPiaoData kechuang50 = getLast30DayData("1.515790");
        List<Main.BanKuai> allBanKuai = Main.parseAllBanKuai();
        List<BanKuaiWithGuPiao> banKuaiWithGuPiaoList = new ArrayList<>(allBanKuai.size());
//        banKuaiWithGuPiaoList.addAll(
//                allBanKuai.subList(0, allBanKuai.size() / 2)
//                        .parallelStream().map(banKuai -> {
//                            return getBanKuaiWithGuPiao(banKuai);
//                        }).collect(Collectors.toList()));
        banKuaiWithGuPiaoList.addAll(
                allBanKuai.subList(allBanKuai.size() / 2, allBanKuai.size())
                        .parallelStream().map(banKuai -> {
                            return getBanKuaiWithGuPiao(banKuai);
                        }).collect(Collectors.toList()));

        System.out.println("================");
        for (int i = 0; i < 10; i++) {
            test(kechuang50, banKuaiWithGuPiaoList);
            System.out.println("end ================");
        }
    }

    private static void test(GuPiaoData kechuang50, List<BanKuaiWithGuPiao> banKuaiWithGuPiaoList) {
        double shouyiSum = 1;
        double shouyiSum2 = 1;
        double shouyiSum3 = 1;
        Random random = new Random();
        for (int i = 1; i < dateList.size() - 1; i++) {
            String lastDate = dateList.get(i);
            String date = dateList.get(i);
            String nextDate = dateList.get(i + 1);
//            double avgZhangDie = banKuaiWithGuPiaoList.stream().map(e -> {
//                shangZheng.Main.OneDayDataDetail dayDataDetail = e.getBankuaiData().dayDataDetailMap.get(date);
//                if (dayDataDetail == null) {
//                    return null;
//                }
//                return dayDataDetail.last2EndDiff / dayDataDetail.getLast10dayBoDong();
//            }).filter(e -> e != null).mapToDouble(k -> k).average().orElse(1);
            //按策略计算的最优板块
            BanKuaiWithGuPiao zuiYouBanKuai = banKuaiWithGuPiaoList.get(0);
            for (BanKuaiWithGuPiao tmp : banKuaiWithGuPiaoList) {
                shangZheng.Main.OneDayDataDetail tmpDateDetail = tmp.getBankuaiData().dayDataDetailMap.get(date);
                if (tmp.getZhangTingLv(date) > zuiYouBanKuai.getZhangTingLv(date)
//                        && tmpDateDetail.last2EndDiff / tmpDateDetail.getLast10dayBoDong() > avgZhangDie
//                        && tmp.getZhangTingLv(date) >= tmp.getZhangTingLv(lastDate)
                ) {
                    zuiYouBanKuai = tmp;
                }
            }
            double mingRiShouYi = zuiYouBanKuai.getBankuaiData().dayDataDetailMap.get(nextDate).last2EndDiff;
            double bodong = zuiYouBanKuai.getBankuaiData().dayDataDetailMap.get(date).getLast10dayBoDong();
            shouyiSum *= 1 + mingRiShouYi;
            //随机选择板块
            BanKuaiWithGuPiao zuiYouBanKuai2 = banKuaiWithGuPiaoList.get(random.nextInt(banKuaiWithGuPiaoList.size() - 1));
            double mingRiShouYi2 = zuiYouBanKuai2.getBankuaiData().dayDataDetailMap.get(nextDate).last2EndDiff;
            double bodong2 = zuiYouBanKuai2.getBankuaiData().dayDataDetailMap.get(date).getLast10dayBoDong();
            shouyiSum2 *= 1 + mingRiShouYi2 / bodong2 * bodong;
            //科创 50
            double mingRiShouYi3 = kechuang50.dayDataDetailMap.get(nextDate).last2EndDiff;
            double bodong3 = kechuang50.dayDataDetailMap.get(nextDate).getLast10dayBoDong();
            shouyiSum3 *= 1 + mingRiShouYi3 / bodong3 * bodong;
            System.out.printf("日期：%s ,策略1板块：%s,涨停率:%.2f%%, 策略1明日涨幅：%.2f%%, 策略1累积涨幅：%.2f%% ； 策略1板块：%s,涨停率:%.2f%%, 策略2明日涨幅：%.2f%%, 策略2累积涨幅：%.2f%% ; 科创50 累积涨幅：%.2f%% \n", date,
                    zuiYouBanKuai.banKuai.name, zuiYouBanKuai.getZhangTingLv(date), mingRiShouYi * 100, shouyiSum * 100,
                    zuiYouBanKuai2.banKuai.name, zuiYouBanKuai2.getZhangTingLv(date), mingRiShouYi2 * 100, shouyiSum2 * 100,
                    shouyiSum3 * 100
            );
        }
    }

    public BanKuaiWithGuPiao getBanKuaiWithGuPiao(Main.BanKuai banKuai) {
        GuPiaoData bankuaiData = getLast30DayData(banKuai.code);
        List<Main.BanKuai> allGeGuList = getAllGeGu(banKuai.code.split("\\.")[1]);
        Map<String/*code*/, GuPiaoData> geguCode2Data = allGeGuList.parallelStream().map(gegu -> {
            return getLast30DayData(gegu.code);
        }).filter(Objects::nonNull).collect(Collectors.toMap(e -> e.code, e -> e));
        Map<String/*date*/, BanKuaiWithGuPiao.ZhangDieTing> date2ZhangDieTing = new HashMap<>(dateList.size());
        for (int i = 0; i < dateList.size(); i++) {
            String date = dateList.get(i);
            AtomicInteger zhangTing = new AtomicInteger();
            AtomicInteger dieTing = new AtomicInteger();
            geguCode2Data.values().stream().forEach(gegu -> {
                shangZheng.Main.OneDayDataDetail dayDataDetail = gegu.dayDataDetailMap.get(date);
                if (dayDataDetail == null) {
                    return;
                }
                if (isKeChuang(gegu.code)) {
                    if (dayDataDetail.last2EndDiff > 0.195) {
                        zhangTing.incrementAndGet();
                    } else if (dayDataDetail.last2EndDiff < -0.195) {
                        dieTing.incrementAndGet();
                    }
                } else {
                    if (dayDataDetail.last2EndDiff > 0.095) {
                        zhangTing.incrementAndGet();
                    } else if (dayDataDetail.last2EndDiff < -0.095) {
                        dieTing.incrementAndGet();
                    }
                }
            });
            BanKuaiWithGuPiao.ZhangDieTing zhangDieTing = new BanKuaiWithGuPiao.ZhangDieTing(zhangTing.get(), dieTing.get());
            date2ZhangDieTing.put(date, zhangDieTing);
        }
        return new BanKuaiWithGuPiao(banKuai, bankuaiData, allGeGuList, geguCode2Data, date2ZhangDieTing);
    }

    static boolean isKeChuang(String geguCode) {
        return kechuang.contains(geguCode.substring(0, 5));
    }

    static Set<String> kechuang = Arrays.asList("1.688", "1.689", "1.300", "1.301").stream().collect(Collectors.toSet());


    @Data
    @AllArgsConstructor
    public static class BanKuaiWithGuPiao {
        Main.BanKuai banKuai;//板块基础信息
        GuPiaoData bankuaiData;//板块涨跌幅信息
        List<Main.BanKuai> allGeGuList;//所有个股；可能有过滤
        Map<String/*code*/, GuPiaoData> geguCode2Data;//所有股票的详细数据，已经过滤了获取数据失败的股票；
        Map<String/*date*/, ZhangDieTing> date2ZhangDieTing;

        @Data
        @AllArgsConstructor
        public static class ZhangDieTing {
            int zhangTing;
            int dieTing;
        }

        public double getZhangTingLv(String date) {
            ZhangDieTing zhangDieTing = date2ZhangDieTing.get(date);
            if (zhangDieTing == null) {
                return 0;
            }
            return (zhangDieTing.zhangTing - zhangDieTing.dieTing) * 1.0 / geguCode2Data.size();
        }
    }

    @AllArgsConstructor
    public static class GuPiaoData {
        String code;//带前缀
        String name;
        List<shangZheng.Main.OneDayDataDetail> detailList;
        Map<String, shangZheng.Main.OneDayDataDetail> dayDataDetailMap;
    }

    //带前缀
    //    https://push2his.eastmoney.com/api/qt/stock/kline/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61&beg=0&end=20500101&ut=fa5fd1943c7b386f172d6893dbfba10b&rtntype=6&secid=1.000558&klt=101&fqt=1&cb=jsonp1732998219242
//    https://push2his.eastmoney.com/api/qt/stock/kline/get?cb=jQuery35105715717072793236_1732997335203&secid=1.000558&ut=fa5fd1943c7b386f172d6893dbfba10b&fields1=f1%2Cf2%2Cf3%2Cf4%2Cf5%2Cf6&fields2=f51%2Cf52%2Cf53%2Cf54%2Cf55%2Cf56%2Cf57%2Cf58%2Cf59%2Cf60%2Cf61&klt=101&fqt=1&end=20500101&lmt=120&_=1732997335253
    private static GuPiaoData getLast30DayData(String bankuaiCode) {
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
        detailList = detailList.subList(detailList.size() - 100, detailList.size() - 1);//todo
        Map<String, shangZheng.Main.OneDayDataDetail> dayDataDetailMap = detailList.stream().collect(Collectors.toMap(e -> e.getDate(), e -> e));
        return new GuPiaoData(bankuaiCode, "", detailList, dayDataDetailMap);
    }

    //bankuaiCode 不带前缀
    @SneakyThrows
    private static List<Main.BanKuai> getAllGeGu(String bankuaiCode) {
        try {
            long ms = System.currentTimeMillis();
            String url = "https://33.push2.eastmoney.com/api/qt/clist/get?cb=jQuery112404930637717184252_" +
                    ms + "&pn=1&pz=300&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&fltt=2&invt=2&dect=1&wbp2u=|0|0|0|web&fid=f3&fs=b:"
                    + bankuaiCode + "+f:!50&fields=f12,f13,f14&_=" + (ms + 10);
            String jqueryString = Main.getData(url);
            String arr[] = jqueryString.split("\\(|\\)");
            JSONArray jsonArray = JSON.parseObject(arr[1]).getJSONObject("data").getJSONArray("diff");
            if (jsonArray != null && jsonArray.size() != 0) {
                return jsonArray.stream().map(e -> {
                    JSONObject jsonObject = (JSONObject) e;
                    Main.BanKuai banKuai = new Main.BanKuai();
                    banKuai.setName(jsonObject.getString("f14"));
                    String code = jsonObject.getString("f12");
                    if (!code.contains(".")) {
                        code = jsonObject.getString("f13") + "." + code;
                    }
                    banKuai.setCode(code);
                    return banKuai;
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.out.println(bankuaiCode + "查all个股失败");
        }
        return new ArrayList<>();
    }
}