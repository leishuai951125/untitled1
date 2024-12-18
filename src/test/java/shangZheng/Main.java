package shangZheng;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    @Data
    public static class OneDayDataDetail {
        public String date;
        public Double start;
        public Double end;
        public Double startEndDiff;//当日波动
        public Double last2StartDiff;//开盘涨幅
        public Double last2EndDiff;//收盘涨幅

        public Double todayEndDiv30Avg;//上一日收盘除以30日 avg ; 得到今日相对比例

        public OneDayDataDetail lasOneDayDataDetail;//上一日

        //------avg-----
        public Double last5dayEndAvg;
        public Double last5dayBoDong;
        public Double last10dayEndAvg;
        public Double last10dayBoDong;
        public Double last20dayEndAvg;
        public Double last20dayBoDong;
        public Double last30dayEndAvg;
        public Double last30dayBoDong2;
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    static double shouxufei = 0.0003;//正常 0.001
    //    static double shouxufei = 0.0025;//正常 0.001
    static boolean qufan = false;//不能用 true
    static Map<String, OneDayDataDetail> testDataInfoMap = null;

    //当日阴阳线
//    static Function<OneData, Boolean> yijuFunc = (oneData) -> oneData.getStartEndDiff() > 0;
    //当日收盘价
//    static Function<OneData, Boolean> yijuFunc = (oneData) -> oneData.getLast2EndDiff() > 0;

    //当日收盘价
//    static Function<OneData, Boolean> yijuFunc = (oneData) -> oneData.getLast2StartDiff() > 0;

    static Map<String, Utils.QiInfo> qihuoMap = Utils.parseA50QiHuo();

    static AtomicInteger compareCount = new AtomicInteger(0);
    static AtomicInteger compareEqualsCount = new AtomicInteger(0);
    static AtomicInteger compareBuZhiXinCount = new AtomicInteger(0);

    static Function<OneDayDataDetail, Boolean> yijuFunc = (oneDayDataDetail) -> {
        Utils.QiInfo qiInfo = qihuoMap.get(oneDayDataDetail.date);
        if (qiInfo != null) {
//            boolean zhixin = Math.abs(qiInfo.zhangdie) >= 0.006;//更准
            boolean zhixin = Math.abs(qiInfo.zhangdie) >= 0.006000;
            if (zhixin) {
                String color = qiInfo.zhangdie * oneDayDataDetail.startEndDiff < 0 ? ANSI_RED : ANSI_GREEN;
                System.out.printf(color + "置信：日期：%s,预测值:%.2f%%,整日涨跌: %.2f%% , 日内涨跌:%.2f%% \n" + ANSI_RESET,
                        oneDayDataDetail.date, qiInfo.zhangdie * 100, oneDayDataDetail.last2EndDiff * 100, oneDayDataDetail.startEndDiff * 100);
                compareCount.incrementAndGet();
                if (qiInfo.zhangdie * oneDayDataDetail.startEndDiff > 0) {
                    compareEqualsCount.incrementAndGet();
                }
                return qiInfo.zhangdie > 0;
            } else {
                String color = qiInfo.zhangdie * oneDayDataDetail.startEndDiff < 0 ? ANSI_RED : ANSI_YELLOW;
                System.out.printf(color + "不置信：日期：%s,预测值:%.2f%%,整日涨跌: %.2f%% , 日内涨跌:%.2f%% \n" + ANSI_RESET,
                        oneDayDataDetail.date, qiInfo.zhangdie * 100, oneDayDataDetail.last2EndDiff * 100, oneDayDataDetail.startEndDiff * 100);
                compareBuZhiXinCount.incrementAndGet();
                return true;//不置信就不卖
            }
//            return qiInfo.zhangdie > 0;
        }
        return null;
    };

//    final static String ali_gang = Utils.getDataByFileName("ali_gang");
//    final static String ali_mei = Utils.getDataByFileName("ali_mei");
//    final static String zhonggaiUs = Utils.getDataByFileName("zhonggai_us");

    //https://push2his.eastmoney.com/api/qt/stock/kline/get?cb=jQuery35106172707985323247_1728913794805&secid=1.000001&ut=fa5fd1943c7b386f172d6893dbfba10b&fields1=f1%2Cf2%2Cf3%2Cf4%2Cf5%2Cf6&fields2=f51%2Cf52%2Cf53%2Cf54%2Cf55%2Cf56%2Cf57%2Cf58%2Cf59%2Cf60%2Cf61&klt=101&fqt=1&end=20500101&lmt=120&_=1728913794810


    final static String shangZhengZhishu = Utils.getDataByFileName("shangZhengZhishu");

    final static String keChuang50ZhiShu = Utils.getDataByFileName("keChuang50ZhiShu");

//    final static String hushen300 = Utils.getDataByFileName("hushen300");

    @Test
    public void test2() {

//        Map<String/*dayOffset */, Utils.QiInfo> o = Utils.parseA50QiHuo();
//        o = new TreeMap<>(o);

//        System.out.println(JSON.toJSONString(o));

//        System.out.println(new Date(1728667800000L));

        testZhangDie(shangZhengZhishu);


//        testZhangDie(keChuang50ZhiShu);

//        System.out.println("大盘");
//        test(dapan);
//        System.out.println("白酒");
//        test(baijiu);
//        System.out.println("科创");
//        test(kechuang);
    }

    @Test
    public void ff2() {
//        bili(keChuang50ZhiShu, shangZhengZhishu);
        System.out.println("=========");
//        bili(Utils.getDataByFileName("fangdichan"), shangZhengZhishu);
//        bili(Utils.getDataByFileName("huazai"), shangZhengZhishu);
//        bili(Utils.getDataByFileName("shengwu"), shangZhengZhishu);
//        bili(Utils.getDataByFileName("dianli"), shangZhengZhishu);
//        bili(Utils.getDataByFileName("xiangcun_zhenxing"), shangZhengZhishu);
//        bili(Utils.getDataByFileName("xiangcun_zhenxing"), shangZhengZhishu);
//        bili(Utils.getDataByFileName("jiudian"), shangZhengZhishu);
        bili(Utils.getDataByFileName("jiaoyu"), shangZhengZhishu);
    }


    //https://push2his.eastmoney.com/api/qt/stock/kline/get?cb=jQuery35105388667892147085_1729285290503&secid=1.000001&ut=fa5fd1943c7b386f172d6893dbfba10b&fields1=f1%2Cf2%2Cf3%2Cf4%2Cf5%2Cf6&fields2=f51%2Cf52%2Cf53%2Cf54%2Cf55%2Cf56%2Cf57%2Cf58%2Cf59%2Cf60%2Cf61&klt=101&fqt=1&end=20500101&lmt=1360&_=1729285290508
    public void bili(String test, String base) {
        List<OneDayDataDetail> kechuangList = Utils.parseDongFangCaiFuList(JSON.parseArray(test, String.class));
        List<OneDayDataDetail> baseList = Utils.parseDongFangCaiFuList(JSON.parseArray(base, String.class));
        Map<String, OneDayDataDetail> baseMap = baseList.stream().collect(Collectors.toMap(e -> e.date, e -> e));
        System.out.printf("波动幅度比例：%.2f:1\n", Utils.avgBoDong(kechuangList) / Utils.avgBoDong(baseList));
        System.out.printf("波动幅度比例：%.2f:1\n", Utils.avgBoDongV2(kechuangList) / Utils.avgBoDongV2(baseList));
        System.out.println("========");
        double testShouyi = 0;
        double dapanShouyi = 0;
        for (OneDayDataDetail e : kechuangList) {

            if (e.last30dayEndAvg != null) {
                double billi = e.getLasOneDayDataDetail().end / e.last30dayEndAvg;

                OneDayDataDetail baseOneDayDataDetail = baseMap.get(e.date);
                double baseBilli = baseOneDayDataDetail.getLasOneDayDataDetail().end / baseOneDayDataDetail.last30dayEndAvg;
                String colordiffDapan = ANSI_RESET;

                if (billi < 1) {//亏钱
                    //比大盘亏的多
                    colordiffDapan = Math.abs(billi - 1) / e.last30dayBoDong2 >= 30 ? ANSI_RED : ANSI_RESET; //红色表示盈利方向反向
                    if (colordiffDapan == ANSI_RED) {
                        testShouyi += e.getLast2EndDiff();
                        dapanShouyi += baseOneDayDataDetail.getLast2EndDiff();
                        System.out.printf(ANSI_RESET + "计算收益,beishu:%.2f,beishu:%.2f,testShouyi :%.2f%%,dapanShouyi %.2f%%\n",
                                e.last30dayBoDong2 / baseOneDayDataDetail.last30dayBoDong2, (billi - 1) / e.last30dayBoDong2,
                                testShouyi * 100, dapanShouyi * 100);
                    }
                }

                //昨日多涨
                double diffDapanLast2EndDiff = (e.last2EndDiff - baseOneDayDataDetail.last2EndDiff) * 100;//todo
//                double diffDapanLast2EndDiff = (e.startEndDiff - baseOneData.startEndDiff) * 100;

                System.out.printf(colordiffDapan + "日期：%s,20日，上日大盘比例:%.3f，  上日板块比例:%.3f，     " +
                                " \t上日板块比大盘比值:%.3f,  " +
//                                " 上日板块多余倍数:%.1f   ,比波动倍数:%.2f " +
                                "  \t今日大盘涨跌:%.3f，今日日内大盘涨跌:%.3f，今日板块日内涨跌:%.3f,今日板块涨跌:%.3f，\t今日板块比大盘多涨:%.3f,\n",
                        e.date, baseBilli * 100 - 100, billi * 100 - 100,
                        billi / baseBilli * 100 - 100,
//                        beishu, Math.abs(billi - 1) / e.last30dayBoDong,
                        baseOneDayDataDetail.last2EndDiff * 100, baseOneDayDataDetail.startEndDiff * 100, e.startEndDiff * 100, e.last2EndDiff * 100, diffDapanLast2EndDiff);
            }

        }
//        System.out.println(testShouyi * 100);
//        System.out.println(dapanShouyi * 100);
    }


    void testZhangDie(String testJSON) {
        testDataInfoMap = Utils.parseDongFangCaiFuMap(JSON.parseArray(testJSON, String.class));
        test(testJSON);
    }
//    14 收  13811
//    盘前 最高 13860  最低 13751
//    开盘

    private static void test(String json) {
        List<String> jsonArray = JSON.parseArray(json, String.class);
//        jsonArray = jsonArray.subList(0, jsonArray.size() - 10);
//        jsonArray = jsonArray.subList(0, 111);
//        jsonArray = jsonArray.subList(20, 50);
        if (qufan) {
            List<String> jsonArray2 = new ArrayList<>(jsonArray);
            Collections.reverse(jsonArray2);
            jsonArray.addAll(jsonArray2);
        }
        testShouyi(jsonArray);
        System.out.println("==================================");
        System.out.println();
    }


    private static void testShouyi(List<String> jsonArray) {
        List<OneDayDataDetail> list = new ArrayList<>(jsonArray.size());
        List<Double> shouyi = new ArrayList<>();
        List<Double> shouyi2 = new ArrayList<>();
        List<Double> shouyi3 = new ArrayList<>();
        List<Double> shouyi4 = new ArrayList<>();
        List<Double> shouyi5 = new ArrayList<>();
        List<Double> shouyi6 = new ArrayList<>();
        List<Double> shouyi7 = new ArrayList<>();
        List<Double> shouyiDuiZhao1 = new ArrayList<>();
        List<Double> shouyiDuiZhao2 = new ArrayList<>();
        AtomicInteger zhengqueAutomic = new AtomicInteger();
        AtomicInteger cuouwuAutomic = new AtomicInteger();
//        public class
        jsonArray.forEach(e -> {
            String arr[] = e.split(",");
            OneDayDataDetail oneDayDataDetail = new OneDayDataDetail();
            oneDayDataDetail.date = arr[0];
            oneDayDataDetail.start = Double.parseDouble(arr[1]);
            oneDayDataDetail.end = Double.parseDouble(arr[2]);
            oneDayDataDetail.startEndDiff = (oneDayDataDetail.end - oneDayDataDetail.start) / oneDayDataDetail.start;

            if (list.size() != 0) {
                OneDayDataDetail lastOneDayDataDetail = list.get(list.size() - 1);
                oneDayDataDetail.last2StartDiff = (oneDayDataDetail.start - lastOneDayDataDetail.end) / lastOneDayDataDetail.end;
                oneDayDataDetail.last2EndDiff = (oneDayDataDetail.end - lastOneDayDataDetail.end) / lastOneDayDataDetail.end;
//                System.out.printf(ANSI_YELLOW + "日期：%s,上日涨跌:%.2f%%,整日涨跌: %.2f%% , 日内涨跌:%.2f%% \n" + ANSI_RESET,
//                        oneData.date, lastOneData.startEndDiff * 100, oneData.last2EndDiff * 100, oneData.startEndDiff * 100);
//                if (Math.abs(oneData.startEndDiff) > 0 &&
//                        (oneData.getDate().contains("2024-08")
//                                || oneData.getDate().contains("2024-09")
//                                || oneData.getDate().contains("2024-07")
//                        )
////                            && qihuoMap.containsKey(oneData.getDate())
//                ) {
//                    if (lastOneData.startEndDiff * oneData.startEndDiff > 0.0) {
//                        System.out.println("正确");
//                        zhengqueAutomic.incrementAndGet();
//                    } else {
//                        System.out.println("错误");
//                        cuouwuAutomic.incrementAndGet();
//                    }
//                }

                if (list.size() > 1) {

                    //当的开盘涨跌
                    Boolean yiJu = yijuFunc.apply(oneDayDataDetail);

                    if (yiJu != null) { //有买入

                        //无脑买入，有脑卖出
                        if (yiJu) { //当日整体涨幅
                            shouyi.add(oneDayDataDetail.last2EndDiff);//预计涨就持有
                        } else {
                            shouyi.add(oneDayDataDetail.last2StartDiff - shouxufei);//预计跌就卖出 , todo 考虑手续费
                        }

                        //上一日有收益买入，有脑卖出
                        if (lastOneDayDataDetail.last2EndDiff > 0) {
                            if (yiJu) { //当日整体涨幅
                                shouyi2.add(oneDayDataDetail.last2EndDiff);//预计涨就持有
                            } else {
                                shouyi2.add(oneDayDataDetail.last2StartDiff - shouxufei);//预计跌就卖出 , todo 考虑手续费
                            }
                        } else {
                            if (yiJu) { //当日整体涨幅
                                shouyi4.add(oneDayDataDetail.last2EndDiff);//预计涨就持有
                            } else {
                                shouyi4.add(oneDayDataDetail.last2StartDiff - shouxufei);//预计跌就卖出 , todo 考虑手续费
                            }
                        }

                        //上一日有收益买入，有脑卖出
                        if (lastOneDayDataDetail.last2EndDiff >= 0 && lastOneDayDataDetail.startEndDiff >= 0) {
                            if (yiJu) { //当日整体涨幅
                                shouyi3.add(oneDayDataDetail.last2EndDiff);//预计涨就持有
                            } else {
                                shouyi3.add(oneDayDataDetail.last2StartDiff - shouxufei);//预计跌就卖出 , todo 考虑手续费
                            }
                            shouyi7.add(oneDayDataDetail.last2EndDiff - shouxufei);//无脑卖
                        } else {
                            if (yiJu) { //当日整体涨幅
                                shouyi5.add(oneDayDataDetail.last2EndDiff);//预计涨就持有
                            } else {
                                shouyi5.add(oneDayDataDetail.last2StartDiff - shouxufei);//预计跌就卖出 , todo 考虑手续费
                            }
                        }

                        if (lastOneDayDataDetail.last2EndDiff < 0 && lastOneDayDataDetail.startEndDiff < 0) {
                            if (yiJu) { //当日整体涨幅
                                shouyi6.add(oneDayDataDetail.last2EndDiff);//预计涨就持有
                            } else {
                                shouyi6.add(oneDayDataDetail.last2StartDiff - shouxufei);//预计跌就卖出 , todo 考虑手续费
                            }
                        }
                        //无脑买入，无脑收盘卖出
                        shouyiDuiZhao1.add(oneDayDataDetail.last2EndDiff - shouxufei);
                        //无脑买入，无脑开盘卖出
                        shouyiDuiZhao2.add(oneDayDataDetail.last2StartDiff - shouxufei);
                    }
                }
            }
            list.add(oneDayDataDetail);
        });

        System.out.printf("zhengque:%d,cuowu:%d,zhengque/cuowu:%.2f\n",
                zhengqueAutomic.get(), cuouwuAutomic.get(), zhengqueAutomic.get() * 1.0 / cuouwuAutomic.get() * 100);

        String spletPrefix = "";

        List<ShouyiSumTmp> shouyiSumTmpList = new ArrayList<>();
        getShouyiSum(shouyi, "***实验1，无脑买入，依据卖" + spletPrefix, shouyiSumTmpList);//1
        getShouyiSum(shouyi2, "实验2，上日涨买入，依据卖" + spletPrefix, shouyiSumTmpList); //依据卖：决定收盘还是开盘卖
        getShouyiSum(shouyiDuiZhao1, "对照1，无脑买，收盘卖" + spletPrefix, shouyiSumTmpList);

        getShouyiSum(shouyiDuiZhao2, "对照2，无脑买，开盘卖" + spletPrefix, shouyiSumTmpList);
        getShouyiSum(shouyi4, "实验4，上日跌买入，依据卖" + spletPrefix, shouyiSumTmpList);
        getShouyiSum(shouyi3, "实验3，上日涨且阳买入，依据卖" + spletPrefix, shouyiSumTmpList);
        getShouyiSum(shouyi7, "实验7，上日涨且阳买入，收盘卖" + spletPrefix, shouyiSumTmpList);
        getShouyiSum(shouyi5, "实验5，非【上日涨且阳买入】，依据卖" + spletPrefix, shouyiSumTmpList);
        getShouyiSum(shouyi6, "实验6，上日跌且阴买入，依据卖" + spletPrefix, shouyiSumTmpList);
        shouyiSumTmpList.sort((a, b) -> (int) ((a.shouyi - b.shouyi) * -10000));

        System.out.printf("整体涨跌：%.2f%%\n", (list.get(list.size() - 1).end - list.get(2).start) / list.get(2).start * 100);
        System.out.printf("最大收益：%.2f%%\n", shouyiSumTmpList.get(0).shouyi * 100);
        System.out.printf("平均年化收益：%.2f%%\n", Math.pow(shouyiSumTmpList.get(0).shouyi + 1, 240.0 / shouyiSumTmpList.get(0).jiaoyicishu) * 100 - 100);
        System.out.printf("不置信次数:%d,count:%d,success:%d,预测正确率:%.2f %%  \n",
                compareBuZhiXinCount.get(),
                compareCount.get(),
                compareEqualsCount.get(),
                compareEqualsCount.get() * 1.0 / compareCount.get() * 100);
        System.out.println("------------------");
        shouyiSumTmpList.forEach(e -> System.out.println(e.log));
    }

    @AllArgsConstructor
    public static class ShouyiSumTmp {
        String fangAn;
        double shouyi;
        String log;
        double shouxufeiSum;
        long shouxufeiCishu;
        int jiaoyicishu;
    }

    private static void getShouyiSum(List<Double> shouyi, String prefix, List<ShouyiSumTmp> shouyiSumTmpList) {
        double shouyiSum = 1.0;
        for (double sy : shouyi) {
            shouyiSum *= (1 + sy);
        }
        shouyiSum -= 1;
        prefix = (shouyiSum > (0.003 - shouxufei) * shouyi.size() ? ANSI_GREEN : ANSI_RESET) + "【" + prefix + "】";
        String log = "";
        long shouxufeicishu = shouyi.stream().filter(e -> e < 0).count();
        double shouxufeiSum = 1 - (Math.pow(1 - shouxufei, shouxufeicishu));
        log = log + String.format("总买卖次数：%d，手续费次数 %d，手续费总计：%.2f%%,单次手续费:%.2f%% \n", shouyi.size(), shouxufeicishu, shouxufeiSum * 100, shouxufei * 100);
        log = log + String.format(prefix + " , 累积%d次，   累计收益:%.2f%%,   平均收益：%.4f%%\n" + ANSI_RESET, shouyi.size(), shouyiSum * 100, shouyiSum * 100 / shouyi.size());
        shouyiSumTmpList.add(new ShouyiSumTmp(prefix, shouyiSum, log, shouxufeiSum, shouxufeicishu, shouyi.size()));
    }


}

//猜测：处于均价历史低位、前5分钟涨势凶