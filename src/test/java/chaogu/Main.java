package chaogu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import shangZheng.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main {
    enum RunMode {
        YuCe, JiaoYan;
    }

    RunMode runMode = RunMode.YuCe;

    static String lastDate = "2024-11-22";
    static String todayDate = "2024-11-25";
    static boolean readDataByFile = false;
    static boolean needFilterChongFuBankuai = true;//一分钟后的机会中去重
    static boolean testJiHui = false;//测试机会模式
    static double lastDapanStar2EndDiff = -3 / 100.0;

    //25min整结束集合竞价，30分整开始交易

    static boolean needFilter = false;
    static boolean isSimpleMode = false;//简要模式
    static boolean filterNoEtf = true;//过滤没有etf的板块
    static boolean needLogZhuLi = false;//是否打印主力信息

    static int testStartTimeIndex = 1;//当前时间是多少分钟
    static int testEndTimeIndex = 120;//当前时间是多少分钟
    static double shangZhangGaiLv = 0.5;


    static double getSortValue(BankuaiWithData bankuaiWithData) {
//        return bankuaiWithData.testMinuteShouYiSum;
//        return bankuaiWithData.getLast30DayInfoMap().get(todayDate).getStartEndDiff() - bankuaiWithData.test0_EndIndexShouyim;
//        return bankuaiWithData.getTodayMinuteDataList().get(1).startEndDiff * Math.abs(bankuaiWithData.getTodayMinuteDataList().get(1).startEndDiff / bankuaiWithData.getLast30DayInfoMap().get(todayDate).last10dayEndAvg);
//常用的两个
//        return bankuaiWithData.getTodayMinuteDataList().get(1).startEndDiff / Math.pow(bankuaiWithData.getBoDong(), 0.3);
//常用的两个除系数，日常使用排序：todo **********
        return getDeFen(bankuaiWithData) * Math.abs(bankuaiWithData.getTodayMinuteDataList().get(1).startEndDiff) / Math.pow(bankuaiWithData.getBoDong(), 0.3);//得分排序
//        return bankuaiWithData.getTodayMinuteDataList().get(1).startEndDiff / Math.pow(bankuaiWithData.getBoDong(), 0.3);//pow 第二个参数取值 0.1～-1 ;取值越小，波动大的越有优势
        //实际收益排序； todo 考虑增加胜率的收益排序
//        return getTodayDiffAfter1min(bankuaiWithData) / bankuaiWithData.getBoDong();//1分钟后收益统计
//        return getTodayDiffAfter1min(bankuaiWithData) / Math.pow(bankuaiWithData.getTodayBoDong(), 0.5);//1分钟后收益统计
//        return bankuaiWithData.getTodayShengLv();//数学期望排序
//        return bankuaiWithData.test0_EndIndexShouyim / bankuaiWithData.getBoDong();//区间收益统计
//        return getDeFen(bankuaiWithData)  ;//得分排序
//        return bankuaiWithData.getBoDong();
        //前2分钟已有收益
//        return (bankuaiWithData.getTodayMinuteDataList().get(1).startEndDiff - bankuaiWithData.last2StartDiff) / Math.pow(bankuaiWithData.getBoDong(), 0.3);
//        return (bankuaiWithData.getTodayMinuteDataList().get(1).startEndDiff - bankuaiWithData.last2StartDiff / 2) / bankuaiWithData.getBoDong();
    }

    private static int getDeFen(BankuaiWithData e) {
        int deFen =
                e.todayMinuteSort * 2 - e.lastDayZhangFuSort - e.getXiangDuiBiLi30Day().guiyiHuaPaiMing - e.last2StartDiffSort / 5;
        if (ANSI_GREEN.equals(getLastDayZhangFuColor(e))) {
            deFen -= 20;
        }
        if (ANSI_GREEN.equals(getJiaGePaiMingColor(e.getXiangDuiBiLi30Day()))) {
            deFen -= 20;
        }
        if (ANSI_GREEN.equals(getOneMinuteZhangFuColor(e))) {
            deFen -= 20;
        }
        if (ANSI_GREEN.equals(getKaiPanZhangFuColor(e))) {
            deFen -= 20;
        }
        if (ANSI_GREEN.equals(getEtfXiangDuiBanKuaiColor(e.getEtfBankuaiWithData(), e))) {
            deFen -= 10;
        } else if (ANSI_RED.equals(getEtfXiangDuiBanKuaiColor(e.getEtfBankuaiWithData(), e))) {
            deFen += 10;
        }
        if (!CollectionUtils.isEmpty(e.zhuLiList) && e.zhuLiList.get(0).getZhuLi() < -1 && e.zhuLiList.get(0).getChaoDaDan() < -1) {
            deFen -= 10;
        }
        return deFen;
    }

    static Set<String> printedJiHuiSet = new HashSet<>(100);//避免重复打印

    private static void testAfterOneMinuteJiHui(List<BankuaiWithData> bankuaiWithDataList, int offsetForTest) {
        Map<String, List<String>> time2desc = new HashMap<>();
        Set<String> allFitBanKuai = new HashSet<>(10);

//        daZengDaJiang();

        bankuaiWithDataList.forEach(e -> {
//            if (e.bankuaiName.contains("汽车服务")) { //打断点查问题
//                System.out.print("");
//            }
            int startIndex = 2;
            int maxSpan = 2;
            for (int i = startIndex; i < e.todayMinuteDataList.size() && i <= offsetForTest; i++) {
                System.out.print("");
                for (int t = 1; t <= maxSpan; t++) {
                    if (i - t < startIndex - 1) {
                        continue;
                    }
                    OneData bankuaiOneData = e.todayMinuteDataList.get(i);
//                OneData hushen300OneData = hushen300BanKuaiData.todayMinuteDataList.get(i);
                    double bankuaiDiff = e.todayMinuteDataList.get(i).end / e.todayMinuteDataList.get(i - t).end - 1;
                    double hushen300Diff = hushen300BanKuaiData.todayMinuteDataList.get(i).end / hushen300BanKuaiData.todayMinuteDataList.get(i - t).end - 1;
                    double kechuang50Diff = KeChuang50BanKuaiData.todayMinuteDataList.get(i).end / KeChuang50BanKuaiData.todayMinuteDataList.get(i - t).end - 1;
                    hushen300Diff = (hushen300Diff + kechuang50Diff) / 2;
                    String fitDesc = null;
//                    double yuzhi = 0.006;
                    double yuzhi = e.getBoDong() * (t <= 1 ? 0.25 : 0.35);
                    if (hushen300Diff >= -0.00 && bankuaiDiff < -yuzhi) {
                        fitDesc = ANSI_GREEN + "-条件1" + ANSI_RESET;
                    } else if (hushen300Diff <= 0.00 && bankuaiDiff > yuzhi) {
                        fitDesc = ANSI_RED + "+条件1" + ANSI_RESET;
                    } else if (hushen300Diff <= 0.000 && bankuaiDiff > 0 && bankuaiDiff - hushen300Diff > yuzhi) {
//                        fitDesc = ANSI_GREEN + "+条件2" + ANSI_RESET;
                    } else if (hushen300Diff > 0 && bankuaiDiff - hushen300Diff > yuzhi &&
                            bankuaiDiff / hushen300Diff >= 3) {
//                        fitDesc = "+条件3";//todo 合理的加入数据，考虑倍数; 目前测试发现该分支反例较多
                    }
                    if (fitDesc != null) {
                        if (needFilterChongFuBankuai && allFitBanKuai.contains(e.bankuaiName + fitDesc.charAt(0))) {
                            return;
                        }
                        allFitBanKuai.add(e.bankuaiName + fitDesc.charAt(0));//把符号带上

                        //------颜色------
                        double bankuaiDangQianShouYi = e.todayMinuteDataList.get(i).end / e.last30DayInfoMap.get(todayDate).start - 1;
                        double dapanDangQianShouYi = hushen300BanKuaiData.todayMinuteDataList.get(i).end / hushen300BanKuaiData.last30DayInfoMap.get(todayDate).start * 0.5 +
                                KeChuang50BanKuaiData.todayMinuteDataList.get(i).end / KeChuang50BanKuaiData.last30DayInfoMap.get(todayDate).start * 0.5 - 1;
                        String bankuaiShouYiColor = ANSI_RESET;
                        if (bankuaiDiff > 0 && (
                                bankuaiDangQianShouYi - dapanDangQianShouYi > 2.5 * bankuaiDiff //涨太多
                                        || bankuaiDangQianShouYi - dapanDangQianShouYi < -1.5 * bankuaiDiff //跌太多，反弹不可信
                        )) {
                            bankuaiShouYiColor = ANSI_GREEN;
                        }
                        String yifenzhongShouYiColor = ANSI_RESET;
                        if (bankuaiDiff > 0 && bankuaiDiff > e.getBoDong() * 0.6) {
                            yifenzhongShouYiColor = ANSI_GREEN;
                        }
                        String yifenDapanColor = ANSI_RESET;
                        if (bankuaiDiff > 0 && hushen300Diff < -hushen300BanKuaiData.getBoDong() * 0.15) {
                            yifenDapanColor = ANSI_GREEN;
                        }
                        String banKuaiColor = ANSI_RESET;
                        if (bankuaiDiff > 0 && !Objects.equals(bankuaiShouYiColor, ANSI_GREEN)
                                && !Objects.equals(yifenzhongShouYiColor, ANSI_GREEN) && !Objects.equals(yifenDapanColor, ANSI_GREEN)) {
                            banKuaiColor = ANSI_RED;
                        }
                        //----颜色end----
                        fitDesc += String.format("，时间:%s,统计 %d 分钟," +
                                        banKuaiColor + "板块:%s , etf:%s" + ANSI_RESET
                                        + ",波动:%.2f%%,| " +
                                        "涨幅：" + yifenzhongShouYiColor + "%.2f%%，" + yifenDapanColor + "%.2f%%" + ANSI_RESET + "，板块收盘涨幅：%.2f%% |" +
                                        "大盘收盘涨幅：%.2f%% ,50收盘涨跌:%.2f%%，" +
                                        "\n        板块开盘：%.2f%%，板块一分钟：%.2f%%, " +
                                        bankuaiShouYiColor + "截止当前板块收益：%.2f%%" + ANSI_RESET +
                                        ",截止当前大盘收益：%.2f%% \n",
                                bankuaiOneData.dateTime.substring(11), t,
                                //板块名，波动
                                e.bankuaiName.substring(0, Math.min(5, e.bankuaiName.length())), e.banKuai.eftName,
                                e.getBoDong() * 100,
                                //板块
                                bankuaiDiff * 100, hushen300Diff * 100, e.last30DayInfoMap.get(todayDate).end / e.todayMinuteDataList.get(i).end * 100 - 100,
                                //大盘收盘
                                hushen300BanKuaiData.last30DayInfoMap.get(todayDate).end / hushen300BanKuaiData.todayMinuteDataList.get(i).end * 100 - 100,
                                KeChuang50BanKuaiData.last30DayInfoMap.get(todayDate).end / KeChuang50BanKuaiData.todayMinuteDataList.get(i).end * 100 - 100,
                                //板块
                                e.last2StartDiff * 100, e.todayMinuteDataList.get(1).startEndDiff * 100,
                                //截止收益,todo 截止收益差距过大可能要排除，截止收益的定义需要清晰（是否包含开盘本身）
                                bankuaiDangQianShouYi * 100,
                                dapanDangQianShouYi * 100
                        );
                        time2desc.putIfAbsent(bankuaiOneData.dateTime, new ArrayList<>());
                        time2desc.get(bankuaiOneData.dateTime).add(fitDesc);
                        break;
                    }
                }
            }
        });

        if (time2desc.size() != 0) {
            time2desc.keySet().stream().sorted().forEach(key -> {
                if (!printedJiHuiSet.contains(key)) {
                    System.out.println(key);
                    printedJiHuiSet.add(key);
                }
                time2desc.get(key).forEach(desc -> {
                    if (!printedJiHuiSet.contains(desc)) {
                        System.out.printf(" %s", desc);
                        printedJiHuiSet.add(desc);
                    }
                });
            });
        }
    }

    private static void daZengDaJiang() {
        System.out.println("---------");
        System.out.println("可能无用的结论");
        for (int i = 3; i < hushen300BanKuaiData.todayMinuteDataList.size(); i++) {
            for (int t = 1; t <= 3; t++) {
                double hushen300Diff = hushen300BanKuaiData.todayMinuteDataList.get(i).end / hushen300BanKuaiData.todayMinuteDataList.get(i - t).end - 1;
                double kechuang50Diff = KeChuang50BanKuaiData.todayMinuteDataList.get(i).end / KeChuang50BanKuaiData.todayMinuteDataList.get(i - t).end - 1;
                hushen300Diff = (hushen300Diff + kechuang50Diff) / 2;
                double yuzhi = hushen300BanKuaiData.getBoDong() * (t <= 1 ? 0.3 : 0.35);
                OneData oneData = hushen300BanKuaiData.todayMinuteDataList.get(i);
                if (hushen300Diff > yuzhi) {
                    System.out.printf("时间:%s,统计 %d 分钟, 大盘暴涨 ,涨幅：%.2f%%\n",
                            oneData.dateTime, t, hushen300Diff * 100);
                    break;
                } else if (hushen300Diff < -yuzhi) {
                    System.out.printf("时间:%s,统计 %d 分钟, 大盘暴跌 ,涨幅：%.2f%%\n",
                            oneData.dateTime, t, hushen300Diff * 100);
                    break;
                }
            }
        }
    }


    static ExecutorService executorService = Executors.newFixedThreadPool(300);
    public static BankuaiWithData KeChuang50BanKuaiData;
    public static BankuaiWithData hushen300BanKuaiData;
    private static int totalLength = 0;


//    原则：1 min 涨越多越好  2 有反弹更好 3 早上涨幅不能太高  4 非科技板块*2

    //判断开盘：昨日涨跌+尾盘+昨晚中概股 ， 上午是否有利好利空消息
    //判断后续行情，看美元汇率、a50（0.6置信度较高）、消息
    //选股：板块涨幅大、价格排名 <60 、etf 相对涨幅小 、昨日和开盘为负数

    /**
     * 猜想：
     * 1 开盘涨后续跌的；可能有原因：1 行情不好时，开盘涨的太猛的更容易跌； 2 涨到顶了，套现
     * 2 只有<2个板块能涨超过 1%，尽量不买；
     * 3 大盘不景气，买上证 50 （可能无用）
     */

    //优先级： 1分钟涨幅 > 昨日排名 > 今日开盘 > etf 相对涨幅
    //不允许：昨日排名过高 、开盘涨幅过高、etf 溢价>0.5%

    //上午开盘3分钟和收盘3分钟不可信
    //谨慎购买涨幅反常的头部股票、开盘前有利好消息的不买


    //盘中选股：排名 10～60 ，归一化为正，已有涨幅较小
    @Test
    public void main() throws IOException {
        long starMs = System.currentTimeMillis();
        List<BankuaiWithData> bankuaiWithDataList = getBankuaiWithData(readDataByFile);
        if (hushen300BanKuaiData.todayMinuteDataList.size() >= testEndTimeIndex) {
            fillGuiYihuaShouyi(bankuaiWithDataList);
        }
        totalLength = bankuaiWithDataList.size();
        //填充归一化、比例
        bankuaiWithDataList.forEach(e -> {
            try {
                e.setXiangDuiBiLi30Day(getXiangDuiBiLi30Day(e));
            } catch (Exception exception) {
                System.out.println("!!! 失败:" + e.getBankuaiName());
            }
        });
        System.out.printf("总板块个数：%d\t", totalLength);
        //填充归一化排名
        fillGuiYiHuaPaiMing(bankuaiWithDataList);
        //填充开盘涨幅排名
        fillKapPanZhangFuPaiMing(bankuaiWithDataList);
        //填充开盘涨幅排名
        fillZuoRiZhangFuPaiMing(bankuaiWithDataList);
        //一分钟涨幅排名
        fillTodayMinuteSort(bankuaiWithDataList);
        //过滤
        bankuaiWithDataList = bankuaiWithDataList.stream().filter(e -> filter(e) && !e.banKuai.isSkipLog()).collect(Collectors.toList());
        //过滤
        bankuaiWithDataList = bankuaiWithDataList.stream()
                .filter(e -> !filterNoEtf || !StringUtils.isEmpty(e.banKuai.etfCode)).collect(Collectors.toList());
        //打印
        List<String> resultListt = bankuaiWithDataList.stream().filter(Main::filter).sorted((a, b) -> {
            return getSortValue(b) > getSortValue(a) ? 1 : -1;
        }).map(e -> {
            String ret = todayOneMinutteDesc(e);
            if (e.etfBankuaiWithData != null) {
                try {
                    String etfDesc = etfTodayOneMinutteDesc(e.etfBankuaiWithData, e);
                    ret += etfDesc;
                } catch (Exception exception) {
                    ret += "etf:" + e.etfBankuaiWithData.bankuaiName + "\n";
//                    exception.printStackTrace();
                }
            }
            return ret + getLastDayDesc(e);
        }).collect(Collectors.toList());
        long endMs = System.currentTimeMillis();

//        System.out.println("\n---------");
//        testAfterOneMinuteJiHui(bankuaiWithDataList);
//        System.out.println("---------");

        if (isSimpleMode) {
            System.out.printf("开始时间：%s, 花费时间：%.2f s  \n" +
                            "昨日大盘涨跌：%.2f%% \n" +
                            "今日大盘开盘涨跌：%.2f%%\n",
                    new Date(starMs).toLocaleString(), (endMs - starMs) / 1000.0,
                    lastDapanStar2EndDiff * 100,
                    hushen300BanKuaiData.last2StartDiff * 100);
        } else {
            System.out.printf("开始时间：%s, 花费时间：%.2f s  \n" +
                            "昨日大盘涨跌：%.2f%% \n" +
                            "今日大盘开盘涨跌：%.2f%%,今日大盘一分钟涨跌：%.2f%%, 一分钟后的收益：%.2f%%， 波动：%.2f%% \n",
                    new Date(starMs).toLocaleString(), (endMs - starMs) / 1000.0,
                    lastDapanStar2EndDiff * 100,
                    hushen300BanKuaiData.last2StartDiff * 100, hushen300BanKuaiData.todayMinuteDataList.get(1).startEndDiff * 100,
                    getTodayDiffAfter1min(hushen300BanKuaiData) * 100,
                    hushen300BanKuaiData.getBoDong() * 100
            );
        }
        System.out.printf("kechuang50开盘涨跌：%.2f%%,今日大盘一分钟涨跌：%.2f%%, 一分钟后的收益：%.2f%%， 波动：%.2f%% \n",
                KeChuang50BanKuaiData.last2StartDiff * 100, KeChuang50BanKuaiData.todayMinuteDataList.get(1).startEndDiff * 100,
                getTodayDiffAfter1min(KeChuang50BanKuaiData) * 100,
                KeChuang50BanKuaiData.getBoDong() * 100
        );
        System.out.printf("时间区间：[%d~%d],大盘归一化收益：%.2f%% , 大盘从[2~%d] 的收益：%.2f%% \n",
                testStartTimeIndex, testEndTimeIndex, hushen300BanKuaiData.testMinuteShouYiSum * 100,
                testEndTimeIndex, hushen300BanKuaiData.test0_EndIndexShouyim * 100
        );
        System.out.printf("一分钟后平均收益：%.2f \t", sumTodayDiffAfter1min.stream().mapToDouble(e -> e).average().getAsDouble());
        if (etfSumTodayDiffAfter1min.size() > 0) {
            System.out.printf("一分钟后etf平均收益：%.2f\n", etfSumTodayDiffAfter1min.stream().mapToDouble(e -> e).average().getAsDouble());
        }
        System.out.printf("===========\t                             %s\t                %s\n", jiaGePaiMingColorTips, lastDayZhangFuColorTips);
        resultListt.forEach(System.out::println);
        tongji(bankuaiWithDataList);

        loopJiHui(bankuaiWithDataList);//机会

        executorService.shutdown();
        System.out.println("结束");
    }

    private static void loopJiHui(List<BankuaiWithData> bankuaiWithDataList) {
        System.out.println("\n---------");
        long shiZhongSecond = 30 / 3;//每秒分母次
        for (int kk = 2; kk < 1000000; kk++) {
            long beMs = System.currentTimeMillis();
            if (beMs / 1000 % 60 <= 30 && beMs / 1000 % 60 >= 5) {
                sleep(2 * 1000);
                continue;
            }
            System.out.println("  ...." + new Date().toLocaleString());
            testAfterOneMinuteJiHui(bankuaiWithDataList, testJiHui ? kk : 100000);
            bankuaiWithDataList = getBankuaiWithData(readDataByFile);
            if (hushen300BanKuaiData.todayMinuteDataList.size() >= testEndTimeIndex) {
                fillGuiYihuaShouyi(bankuaiWithDataList);
            }
            if (!testJiHui && hushen300BanKuaiData.todayMinuteDataList.size() >= 240) {
                //结束
                break;
            }
            sleep(shiZhongSecond * 1000 - (System.currentTimeMillis() - beMs));
        }
        System.out.println("---------");
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //这些都是波动浮动小的大盘指标
    static Set<String> neeSkipCode = Arrays.stream(new String[]{
            "1.510310", "1.510500", "1.512010", "1.512040", "1.512090", "1.512120", "1.512160", "1.512190", "1.512260", "1.512280", "1.512290", "1.512360", "1.512380", "1.512390", "1.512400", "1.512510", "1.512520", "1.512530", "1.512550", "1.512640", "1.512650", "1.512700", "1.512730", "1.512750", "1.512770", "1.512800", "1.512820", "1.512890", "1.512910", "1.512950", "1.512960", "1.512970", "1.512990", "1.515020", "1.515080", "1.515100", "1.515110", "1.515120", "1.515130", "1.515150", "1.515160", "1.515180", "1.515190", "1.515200", "1.515220", "1.515290", "1.515300", "1.515310", "1.515330", "1.515350", "1.515360", "1.515380", "1.515450", "1.515530", "1.515550", "1.515580", "1.515590", "1.515600", "1.515650", "1.515660", "1.515680", "1.515760", "1.515770", "1.515800", "1.515810", "1.515860", "1.515890", "1.515900", "1.515910", "1.515920", "1.515950", "1.515960", "1.515990", "1.516020", "1.516060", "1.516080", "1.516110", "1.516120", "1.516130", "1.516210", "1.516220", "1.516300", "1.516310", "1.516500", "1.516520", "1.516530", "1.516550", "1.516560", "1.516570", "1.516600", "1.516650", "1.516670", "1.516720", "1.516750", "1.516760", "1.516810", "1.516830", "1.516910", "1.516930", "1.516950", "1.516960", "1.516970", "1.560020", "1.560030", "1.560050", "1.560060", "1.560070", "1.560080", "1.560100", "1.560150", "1.560180", "1.560280", "1.560330", "1.560350", "1.560500", "1.560520", "1.560550", "1.560560", "1.560580", "1.560650", "1.560700", "1.560860", "1.560880", "1.560950", "1.560960", "1.560990", "1.561000", "1.561060", "1.561120", "1.561130", "1.561170", "1.561180", "1.561190", "1.561320", "1.561330", "1.561350", "1.561360", "1.561500", "1.561510", "1.561550", "1.561560", "1.561580", "1.561600", "1.561700", "1.561760", "1.561790", "1.561900", "1.561920", "1.561950", "1.561960", "1.561990", "1.562000", "1.562060", "1.562310", "1.562320", "1.562330", "1.562340", "1.562350", "1.562390", "1.562510", "1.562530", "1.562550", "1.562580", "1.562600", "1.562700", "1.562850", "1.562890", "1.562900", "1.562910", "1.562960", "1.562990", "1.563000", "1.563020", "1.563030", "1.563080", "1.563090", "1.563150", "1.563180", "1.563350"
    }).collect(Collectors.toSet());

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SaveFileData {
        BankuaiWithData KeChuang50BanKuaiData;
        BankuaiWithData hushen300BanKuaiData;
        List<BankuaiWithData> bankuaiWithDataList;
    }

    private static List<BankuaiWithData> getBankuaiWithData(boolean readDataByFile) {
        if (readDataByFile) {
            SaveFileData getByFile = getFile();
            KeChuang50BanKuaiData = getByFile.KeChuang50BanKuaiData;
            hushen300BanKuaiData = getByFile.hushen300BanKuaiData;
            return getByFile.bankuaiWithDataList;
        }
        waitAll(() -> {
            KeChuang50BanKuaiData = getBankuaiWithData("科技创新50", "1.588000");
        }, () -> {
            hushen300BanKuaiData = getBankuaiWithData("沪深300", "1.000300");
        });
        List<BanKuai> banKuaiList = parseAllBanKuai();
        //过滤波动小的
        banKuaiList = banKuaiList.stream().filter(e -> !neeSkipCode.contains(e.getCode())).collect(Collectors.toList());
        //组装信息
        List<BankuaiWithData> bankuaiWithDataList = banKuaiList.stream()
                .parallel()
                .map(e -> {
                    try {
                        BankuaiWithData bankuaiWithData = getBankuaiWithData(e.getName(), e.getCode());
                        bankuaiWithData.banKuai = e;
                        if (!StringUtils.isEmpty(e.getEtfCode())) {
                            bankuaiWithData.etfBankuaiWithData = getBankuaiWithData(e.getEftName(), e.getEtfCode());
                        }
                        return bankuaiWithData;
                    } catch (Exception exception) {
                        System.out.println("！！！获取板块信息失败，板块:" + e.name);
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
        saveFile(new SaveFileData(KeChuang50BanKuaiData, hushen300BanKuaiData, bankuaiWithDataList));
        return bankuaiWithDataList;
    }


    public static void saveFile(SaveFileData saveFileData) {
        String fileName = "/Users/leishuai/IdeaProjects/untitled1/src/test/java/chaogu/beifen/" + todayDate + ".txt";
        String fileName_Full = "/Users/leishuai/IdeaProjects/untitled1/src/test/java/chaogu/beifen/" + todayDate + "_full.txt";
        try {
            if (new File(fileName_Full).exists()) {
                return;
            }
            if ((System.currentTimeMillis() / 1000 / 3600 % 24) > 17 || (System.currentTimeMillis() / 1000 / 3600 % 24) < 6) {
                //是今天的数据
                if (hushen300BanKuaiData.last30DayInfoList.get(hushen300BanKuaiData.last30DayInfoList.size() - 1).date.equals(todayDate)) {
                    //非股市交易时间，记录完整数据
                    FileWriter fileWriter2 = new FileWriter(fileName_Full);
                    fileWriter2.write((JSON.toJSONString(saveFileData)));
                    fileWriter2.flush();
                    fileWriter2.close();
                    return;
                }
            }
            //记录非完整数据
            List<shangZheng.Main.OneDayDataDetail> last30DayInfoList = saveFileData.hushen300BanKuaiData.last30DayInfoList;
            if (!new File(fileName).exists() && saveFileData.hushen300BanKuaiData.todayMinuteDataList.size() >= 4
                    && last30DayInfoList.get(last30DayInfoList.size() - 1).date.equals(todayDate)) {
                FileWriter fileWriter2 = new FileWriter(fileName);
                fileWriter2.write((JSON.toJSONString(saveFileData)));
                fileWriter2.flush();
                fileWriter2.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SaveFileData getFile() {
        String fileName = "/Users/leishuai/IdeaProjects/untitled1/src/test/java/chaogu/beifen/" + todayDate + ".txt";
        String fileName_Full = "/Users/leishuai/IdeaProjects/untitled1/src/test/java/chaogu/beifen/" + todayDate + "_full.txt";
        try {
            long start = System.currentTimeMillis();
            String body = null;
            if (new File(fileName_Full).exists()) {
                body = new String(Files.readAllBytes(Paths.get(fileName_Full)));
            } else {
                body = new String(Files.readAllBytes(Paths.get(fileName)));
            }
//            System.out.println("cost:" + (System.currentTimeMillis() - start));
            return JSON.parseObject(body, SaveFileData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void fillGuiYiHuaPaiMing(List<BankuaiWithData> bankuaiWithDataList) {
        AtomicInteger sort = new AtomicInteger(0);
        bankuaiWithDataList.stream().sorted((a, b) -> (int) ((a.xiangDuiBiLi30Day.zuoRiGuiYiHua - b.xiangDuiBiLi30Day.zuoRiGuiYiHua) * 10000))
                .collect(Collectors.toList())
                .forEach(e -> e.xiangDuiBiLi30Day.guiyiHuaPaiMing = sort.incrementAndGet());
    }

    private static void fillKapPanZhangFuPaiMing(List<BankuaiWithData> bankuaiWithDataList) {
        AtomicInteger sort = new AtomicInteger(0);
        bankuaiWithDataList.stream().sorted((a, b) -> (int) ((
                        a.last2StartDiff / a.getBoDong() -
                                b.last2StartDiff / b.getBoDong()
                ) * 10000))
                .collect(Collectors.toList())
                .forEach(e -> e.last2StartDiffSort = sort.incrementAndGet());
    }

    private static void fillTodayMinuteSort(List<BankuaiWithData> bankuaiWithDataList) {
        AtomicInteger sort = new AtomicInteger(0);
        bankuaiWithDataList.stream().sorted((a, b) -> (int) ((
                        a.getTodayMinuteDataList().get(1).startEndDiff / a.getBoDong()
                                - b.getTodayMinuteDataList().get(1).startEndDiff / b.getBoDong()
                ) * 10000))
                .collect(Collectors.toList())
                .forEach(e -> e.todayMinuteSort = sort.incrementAndGet());
    }

    private static void fillZuoRiZhangFuPaiMing(List<BankuaiWithData> bankuaiWithDataList) {
        //填充归一化排名
        AtomicInteger sort = new AtomicInteger(0);
        bankuaiWithDataList.stream().sorted((a, b) -> (int) ((a.lastDayDetail.startEndDiff - b.lastDayDetail.startEndDiff) * 1000))
                .collect(Collectors.toList())
                .forEach(e -> e.lastDayZhangFuSort = sort.incrementAndGet());
    }


    private static void fillGuiYihuaShouyi(List<BankuaiWithData> bankuaiWithDataList) {
        for (BankuaiWithData bankuai : bankuaiWithDataList) {
            //-----已有收益
            double hushen0_EndIndexShouyi = 0.0;
            double bankuai0_EndIndexShouyi = 0.0;
            double etf0_EndIndexShouyi = 0.0;
            for (int i = 2; i <= testEndTimeIndex; i++) {
                OneData hushenOneData = hushen300BanKuaiData.getTodayMinuteDataList().get(i);
                OneData banuaiOneData = bankuai.getTodayMinuteDataList().get(i);
                hushen0_EndIndexShouyi += hushenOneData.startEndDiff;
                bankuai0_EndIndexShouyi += banuaiOneData.startEndDiff;
                if (bankuai.etfBankuaiWithData != null) {
                    etf0_EndIndexShouyi += bankuai.etfBankuaiWithData.getTodayMinuteDataList().get(i).startEndDiff;
                }
            }
            for (int i = 1; i < hushen300BanKuaiData.getTodayMinuteDataList().size(); i++) {
                OneData banuaiOneData = bankuai.getTodayMinuteDataList().get(i);
                //板块最大最小
                bankuai.todayMaxPrice = Math.max(bankuai.todayMaxPrice, banuaiOneData.end);
                bankuai.todayMinPrice = Math.min(bankuai.todayMinPrice, banuaiOneData.end);
            }
            bankuai.test0_EndIndexShouyim = bankuai0_EndIndexShouyi;
            hushen300BanKuaiData.test0_EndIndexShouyim = hushen0_EndIndexShouyi;
            if (bankuai.etfBankuaiWithData != null) {
                bankuai.etfBankuaiWithData.test0_EndIndexShouyim = etf0_EndIndexShouyi;
            }
            //-------归一化收益
            double hushenFuSum = 0.0;
            double hushenZhengSum = 0.0;
            double bankuaiFuSum = 0.0;
            double bankuaiZhengSum = 0.0;
            double etfFuSum = 0.0;
            double etfZhengSum = 0.0;
            int fuCount = 0;
            int zhengCount = 0;
            for (int i = testStartTimeIndex; i <= testEndTimeIndex; i++) {
                OneData hushenOneData = hushen300BanKuaiData.getTodayMinuteDataList().get(i);
                OneData banuaiOneData = bankuai.getTodayMinuteDataList().get(i);
                if (hushenOneData.startEndDiff < 0.0) {
                    fuCount++;
                    hushenFuSum += hushenOneData.startEndDiff;
                    bankuaiFuSum += banuaiOneData.startEndDiff;
                    if (bankuai.etfBankuaiWithData != null) {
                        etfFuSum += bankuai.etfBankuaiWithData.getTodayMinuteDataList().get(i).startEndDiff;
                    }
                } else if (hushenOneData.startEndDiff > 0.0) {
                    zhengCount++;
                    hushenZhengSum += hushenOneData.startEndDiff;
                    bankuaiZhengSum += banuaiOneData.startEndDiff;
                    if (bankuai.etfBankuaiWithData != null) {
                        etfZhengSum += bankuai.etfBankuaiWithData.getTodayMinuteDataList().get(i).startEndDiff;
                    }
                }
            }
//            double timeCount = (240.0 - testEndTimeIndex) / 2;
            double shangZhangCount = (240.0 - testEndTimeIndex) * shangZhangGaiLv;
            double xiajiangCount = (240.0 - testEndTimeIndex) * (1 - shangZhangGaiLv);
            double hushenSum = (xiajiangCount / fuCount * hushenFuSum + shangZhangCount / zhengCount * hushenZhengSum)
//                    / Math.abs(240.0 / testEndTimeIndex * hushen0_EndIndexShouyi);
//                    - 60.0 / testEndTimeIndex * hushen0_EndIndexShouyi;
//                    - hushen0_EndIndexShouyi;
                    ;
            double bankuaiSum = (xiajiangCount / fuCount * bankuaiFuSum + shangZhangCount / zhengCount * bankuaiZhengSum)
//                    / Math.abs(240.0 / testEndTimeIndex * bankuai0_EndIndexShouyi);
//                    - 60.0 / testEndTimeIndex * bankuai0_EndIndexShouyi;
//                    - bankuai0_EndIndexShouyi;
                    ;
            if (bankuai.etfBankuaiWithData != null) {
                bankuai.etfBankuaiWithData.testMinuteShouYiSum = xiajiangCount / fuCount * etfFuSum + shangZhangCount / zhengCount * etfZhengSum
//                        - timeCount / testEndTimeIndex * etf0_EndIndexShouyi;
                ;
            }

            bankuai.testMinuteShouYiSum = bankuaiSum;
            hushen300BanKuaiData.testMinuteShouYiSum = hushenSum;
        }
    }


    private static void tongji(List<BankuaiWithData> bankuaiWithDataList) {
        System.out.println("===========");
        System.out.println("===========");
        int groupSize = 10;
        bankuaiWithDataList.stream().collect(Collectors.groupingBy(a -> (a.xiangDuiBiLi30Day.guiyiHuaPaiMing - 1) / groupSize))
                .entrySet().stream().map(entry -> {
                    ShouYiTongJi guiYiHua2ShouYi = new ShouYiTongJi();
                    guiYiHua2ShouYi.setPaiMing(entry.getKey() * groupSize);
                    guiYiHua2ShouYi.setShouYiList(entry.getValue().stream()
                            .map(e -> {
//                               return e.getLast30DayInfoMap().get(todayDate).last2EndDiff;
                                //一分钟后收益
                                return e.getLast30DayInfoMap().get(todayDate).last2EndDiff - e.getTodayMinuteDataList().get(1).startEndDiff;
                            }).collect(Collectors.toList()));
                    guiYiHua2ShouYi.setAvgShouYi(guiYiHua2ShouYi.getShouYiList().stream().mapToDouble(e -> e).average().getAsDouble());
                    guiYiHua2ShouYi.setCount(entry.getValue().size());
                    return guiYiHua2ShouYi;
                }).sorted((a, b) -> (int) ((b.avgShouYi * 1000 - a.avgShouYi * 1000)))
                .forEach(entry -> System.out.printf("排名：%d~%d,count:%d,平均收益：%.2f,所有收益:%s\n",
                        entry.getPaiMing() + 1, entry.getPaiMing() + groupSize, entry.getCount(), entry.getAvgShouYi() * 100,
                        entry.getShouYiList().stream().map(e -> String.format("%.2f", e * 100)).collect(Collectors.joining(","))
                ));
    }

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    public static class ShouYiTongJi {
        int paiMing;
        List<Double> shouYiList = new ArrayList<>(5);
        double avgShouYi;
        int count;
    }


    //一分钟后的涨跌
    static double getTodayDiffAfter1min(BankuaiWithData bankuaiWithData) {
        return bankuaiWithData.last30DayInfoMap.get(todayDate).getStartEndDiff()
                - bankuaiWithData.getTodayMinuteDataList().get(1).startEndDiff;
    }

    static double zuoRiGuiYiHua(double zuoRi, double max, double min, double avg) {
        return (zuoRi - min) / (max - min) * 100;
//        return (zuoRi - min) / (max - min) * 100;
//        return (zuoRi - avg) / (max - avg) * 100;
//        return (zuoRi - min) / (avg - min) * 100;
    }

    private static boolean filter(BankuaiWithData e) {
        if (!needFilter) {
            return true;
        }
        if (e.todayMinuteDataList.get(1).startEndDiff * 100 - hushen300BanKuaiData.todayMinuteDataList.get(1).startEndDiff * 100 <= 0
                && e.todayMinuteDataList.get(1).startEndDiff * 100 <= 0) {
            //过滤掉上涨不如大盘的
            return false;
        }
        return true;
    }


    @NotNull
    private static String getLastDayDesc(BankuaiWithData e) {
        //30 天比例，最大比例，最小比例，昨日比例
        XiangDuiBiLi30Day xiangDuiBiLi30Day = e.getXiangDuiBiLi30Day();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("过去十天：%s  |", xiangDuiBiLi30Day.xiangDuiBiLiList10Day.stream().map(v -> String.format("%.2f", v * 100 - 100)).collect(Collectors.toList())));
        String color = getJiaGePaiMingColor(xiangDuiBiLi30Day);
        if (isSimpleMode) {
            return String.format(color + "相对价格：%.1f " + ANSI_RESET, xiangDuiBiLi30Day.zuoRiGuiYiHua);
        }
        sb.append(String.format(color + "【价格排名：%d】  |  " + ANSI_RESET, xiangDuiBiLi30Day.guiyiHuaPaiMing));
        sb.append(String.format("相对价格：%.1f  |  ", xiangDuiBiLi30Day.zuoRiGuiYiHua));
        sb.append(String.format("昨日：%.2f  |  ", xiangDuiBiLi30Day.xiangDuiBiLiMap.get(lastDate) * 100 - 100));
        sb.append(String.format("过去最大：%.2f  |  ", xiangDuiBiLi30Day.maxXiangDuiBiLi * 100 - 100));
        sb.append(String.format("过去最小：%.2f  |  ", xiangDuiBiLi30Day.minXiangDuiBiLi * 100 - 100));
        sb.append(String.format("过去平均：%.2f  |  ", xiangDuiBiLi30Day.avgXiangDuiBiLi * 100 - 100));
        sb.append(getZiJinDesc(e));
        sb.append("\n");
        return sb.toString();
    }

    static String getZiJinDesc(BankuaiWithData e) {
        if (!needLogZhuLi) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String sub3 = "\nhttps://data.eastmoney.com/bkzj/" + e.banKuai.code.split("\\.")[1] + ".html";
        sb.append(sub3);
        if (CollectionUtils.isEmpty(e.zhuLiList)) {
            return sb.toString();
        }
        sb.append(String.format("      [超大单，主力] 开始时间：%s  [%.2f，%.2f]",
                e.zhuLiList.get(0).time,
                e.zhuLiList.get(0).chaoDaDan, e.zhuLiList.get(0).getZhuLi()));
        for (int i = 1; i < e.zhuLiList.size() && i < 5; i++) {
            ZiJin i0ZiJin = e.zhuLiList.get(i);
            ZiJin i_1ZiJin = e.zhuLiList.get(i - 1);
            sb.append(String.format("  [%.2f，%.2f]",
                    i0ZiJin.getChaoDaDan() - i_1ZiJin.getChaoDaDan(),
                    i0ZiJin.getZhuLi() - i_1ZiJin.getZhuLi()));
        }
        return sb.toString();
    }


    @Data
    public static class XiangDuiBiLi30Day {
        double maxXiangDuiBiLi = -1000;
        double minXiangDuiBiLi = 1000;
        double avgXiangDuiBiLi;
        Map<String, Double> xiangDuiBiLiMap;
        List<Double> xiangDuiBiLiList10Day;
        double zuoRiGuiYiHua;
        int guiyiHuaPaiMing;
    }


    //基于 10月25数据：
    //取最近 15 天，排名 1～30 比较好
    //取最近 10 天，排名 11～50 比较好
    //

    static XiangDuiBiLi30Day getXiangDuiBiLi30Day(BankuaiWithData e) {
        XiangDuiBiLi30Day xiangDuiBiLi30Day = new XiangDuiBiLi30Day();
        List<shangZheng.Main.OneDayDataDetail> last30DayInfoWithoutTodyList = e.last30DayInfoList;

        if (last30DayInfoWithoutTodyList.get(last30DayInfoWithoutTodyList.size() - 1).date.equals(todayDate)) {
            //去掉今天
            last30DayInfoWithoutTodyList = last30DayInfoWithoutTodyList.subList(0, last30DayInfoWithoutTodyList.size() - 1);
        }
        //todo 取 x 天试试
        last30DayInfoWithoutTodyList = last30DayInfoWithoutTodyList.subList(last30DayInfoWithoutTodyList.size() - 15, last30DayInfoWithoutTodyList.size());

        List<Double> xiangDuiBiLiList = new ArrayList<>(last30DayInfoWithoutTodyList.size());
        Map<String, Double> xiangDuiBiLiMap = new HashMap<>(last30DayInfoWithoutTodyList.size());
        for (shangZheng.Main.OneDayDataDetail dayDataDetail : last30DayInfoWithoutTodyList) {
            shangZheng.Main.OneDayDataDetail hushenDayDataDetail = hushen300BanKuaiData.getLast30DayInfoMap().get(dayDataDetail.date);
            Double xiangDuiBiLi = dayDataDetail.todayEndDiv30Avg / hushenDayDataDetail.todayEndDiv30Avg;
            xiangDuiBiLiMap.put(dayDataDetail.date, xiangDuiBiLi);
            xiangDuiBiLiList.add(xiangDuiBiLi);
            if (dayDataDetail.date.equals(lastDate)) {
                continue;
            }
            if (xiangDuiBiLi > xiangDuiBiLi30Day.maxXiangDuiBiLi) {
                xiangDuiBiLi30Day.maxXiangDuiBiLi = xiangDuiBiLi;
            }
            if (xiangDuiBiLi < xiangDuiBiLi30Day.minXiangDuiBiLi) {
                xiangDuiBiLi30Day.minXiangDuiBiLi = xiangDuiBiLi;
            }
        }
        xiangDuiBiLi30Day.xiangDuiBiLiMap = xiangDuiBiLiMap;
        xiangDuiBiLi30Day.avgXiangDuiBiLi = xiangDuiBiLiList.subList(0, xiangDuiBiLiList.size() - 1).stream().mapToDouble(d -> d).average().getAsDouble();
        xiangDuiBiLi30Day.xiangDuiBiLiList10Day = xiangDuiBiLiList.subList(xiangDuiBiLiList.size() - 10, xiangDuiBiLiList.size());
        xiangDuiBiLi30Day.zuoRiGuiYiHua = zuoRiGuiYiHua(xiangDuiBiLiMap.get(lastDate), xiangDuiBiLi30Day.maxXiangDuiBiLi, xiangDuiBiLi30Day.minXiangDuiBiLi, xiangDuiBiLi30Day.avgXiangDuiBiLi);
        return xiangDuiBiLi30Day;
    }

    public static List<Double> sumTodayDiffAfter1min = new ArrayList<>(100);
    public static List<Double> etfSumTodayDiffAfter1min = new ArrayList<>(100);

    static String lastDayZhangFuColorTips = "";
    static String jiaGePaiMingColorTips = "";

    static String getLastDayZhangFuColor(BankuaiWithData e) {
        lastDayZhangFuColorTips = String.format("最佳昨日涨幅:" + ANSI_RED + "( %d ~ %d ]" + ANSI_RESET, (int) (totalLength * 0.06), (int) (totalLength * 0.6));
//        return (e.lastDayZhangFuSort > 5 && e.lastDayZhangFuSort <= 50 ? ANSI_RED : ANSI_GREEN);
        return (e.lastDayZhangFuSort > totalLength * 0.06 && e.lastDayZhangFuSort <= totalLength * 0.6 ? ANSI_RED : ANSI_GREEN);
    }

    @NotNull
    private static String getJiaGePaiMingColor(XiangDuiBiLi30Day xiangDuiBiLi30Day) {
        jiaGePaiMingColorTips = String.format("最佳价格排名:" + ANSI_RED + "[ %d ~ %d ]" + ANSI_RESET, (int) (totalLength * 0.11), (int) (totalLength * 0.7));
        String color = ANSI_GREEN;
        //归一化 35（涨幅80名）～80（涨幅47名）
//        if (xiangDuiBiLi30Day.guiyiHuaPaiMing <= 60 && xiangDuiBiLi30Day.guiyiHuaPaiMing >= 10) {
        if (xiangDuiBiLi30Day.guiyiHuaPaiMing <= totalLength * 0.7 && xiangDuiBiLi30Day.guiyiHuaPaiMing >= totalLength * 0.11) {
            color = ANSI_RED;
        }
        return color;
    }

    private static String getKaiPanZhangFuColor(BankuaiWithData e) {
//        if (Math.abs(e.last2StartDiff) < e.getBoDong() * 0.1) {
//            //值太小，不算
//            return ANSI_RESET;
//        }
        if (e.last2StartDiffSort <= totalLength * 0.03) {
            //太差
            return ANSI_GREEN;
        }
        if (e.last2StartDiffSort <= totalLength * 0.4) {
            //跌幅刚好
            return ANSI_RED;
        }
        if (e.last2StartDiffSort >= totalLength * 0.9 && e.last2StartDiff >= e.getBoDong() * 0.3) {
            //开盘 last2StartDiff 压根不应该为正，正的太多问题更大
            //涨幅太高, todo 根据相对值是否断层来判断是否过高
            return ANSI_GREEN;
        }
        return "";
    }

    private static String getOneMinuteZhangFuColor(BankuaiWithData e) {
        if (e.bankuaiName.contains("风电设备")) {
//            System.out.println(e);
        }
        if (e.todayMinuteDataList.get(1).startEndDiff < e.last2StartDiff) {
            //一分钟后涨幅不如开盘
            return ANSI_GREEN;
        }
        if (e.todayMinuteDataList.get(1).startEndDiff >= e.getBoDong() * 0.25
                && e.todayMinuteDataList.get(1).startEndDiff <= e.getBoDong() * 0.5) {
            //涨幅合适，todo 这种涨幅的板块太少可能也不可信；因为此时行情大概了下跌；
            //todo 第一名如果跟后面没有代差其实也可行信任，即使涨幅高
            return ANSI_RED;
        }
        if (e.todayMinuteSort - e.last2StartDiffSort > 0.2 * totalLength ||
                e.todayMinuteDataList.get(1).startEndDiff - e.last2StartDiff > e.getBoDong() * 0.2) {
            //一分钟后涨幅增加
            return ANSI_RESET;
        }
        if (e.todayMinuteSort < totalLength * 0.2) {
            //跌太狠
            return ANSI_GREEN;
        }
        return ANSI_RESET;
    }


    private static String todayOneMinutteDesc(BankuaiWithData e) {
        if (isSimpleMode) {
            return "板块： " + fillName(e.getBankuaiName()) + "  \t";
        }
        double todayMinuteXiangDui = e.todayMinuteDataList.get(1).startEndDiff * 100 - hushen300BanKuaiData.todayMinuteDataList.get(1).startEndDiff * 100;
        double kaipanXiangDui = e.last2StartDiff * 100 - hushen300BanKuaiData.last2StartDiff * 100;
        double zuoRiXiangDui = (e.lastDayDetail.startEndDiff - lastDapanStar2EndDiff) * 100;
        try {
            sumTodayDiffAfter1min.add(getTodayDiffAfter1min(e) * 100);
        } catch (Exception exception) {
            System.out.println("！！！失败，" + e.bankuaiName);
        }
        int deFen = getDeFen(e);
        String sub1 = String.format("板块：%-7s \t" +
                        //今日一分钟
                        getOneMinuteZhangFuColor(e) + "今日一分钟涨跌:%.3f%% " + ANSI_RESET +
//                        (deFen > 80 ? ANSI_RED : (deFen < 0 ? ANSI_GREEN : "")) + "得分【%d】\t" + ANSI_RESET +
                        (deFen > totalLength * 0.93 ? ANSI_RED : (deFen < 0 ? ANSI_GREEN : "")) + "得分【%d】\t" + ANSI_RESET +
                        //今日开盘
                        getKaiPanZhangFuColor(e) + "今日开盘相对涨跌:%.3f%%" +
                        " [即:%.3f%%] %d，\t" + ANSI_RESET +
                        //昨日
                        getLastDayZhangFuColor(e) + "上日相比大盘涨跌:%.2f%%" +
                        " [即:%.2f%%] %d，" + ANSI_RESET,
                fillName(e),
                //今日一分钟
                e.todayMinuteDataList.get(1).startEndDiff * 100,
                deFen,
                //今日开盘
                kaipanXiangDui,
                e.last2StartDiff * 100, e.last2StartDiffSort,
                //昨日
                zuoRiXiangDui,
                e.lastDayDetail.startEndDiff * 100, e.lastDayZhangFuSort
        );
        String sub2 = String.format(
                //今日
//                        " 今日相比大盘涨跌：%.2f%%" +
//                        " [即:%.2f%%]， " +
                " [一分钟后:%.2f%%] " +
                        "收益数学期望 %.1f%%， " +
                        "往日波动:%.2f%%  " +
                        "今日波动:%.2f%%  " +
                        //时间
                        "\t | 时间：%s " +
                        //已有收益
                        "\t[已有收益:%.3f%%]" +
                        //归一化收益
                        "\t归一化相对收益:%.3f%%" +
                        " [即:%.3f%%]\t" + ANSI_RESET +
                        "\n",//昨日
                //今日
//                (e.getLast30DayInfoMap().get(todayDate).startEndDiff - hushen300BanKuaiData.getLast30DayInfoMap().get(todayDate).startEndDiff) * 100,
//                e.getLast30DayInfoMap().get(todayDate).startEndDiff * 100,
                //一分钟后
                getTodayDiffAfter1min(e) * 100,
                e.getTodayShengLv() * 100,//为正就是可以的
                e.getBoDong() * 100,
                e.getTodayBoDong() * 100,
                //时间
                e.todayMinuteDataList.get(1).dateTime,
                //已有收益
                e.test0_EndIndexShouyim * 100,
                //归一化收益
                e.testMinuteShouYiSum * 100 - hushen300BanKuaiData.testMinuteShouYiSum * 100,
                e.testMinuteShouYiSum * 100);
        return sub1 + sub2;
    }


    //这个指标可能不重要
    static String getEtfXiangDuiBanKuaiColor(BankuaiWithData etf, BankuaiWithData bankuai) {
        if (etf == null) {
            return "";
        }
        double todayMinuteXiangDui = etf.todayMinuteDataList.get(1).startEndDiff * 100 - hushen300BanKuaiData.todayMinuteDataList.get(1).startEndDiff * 100;
        double kaipanXiangDui = etf.last2StartDiff * 100 - hushen300BanKuaiData.last2StartDiff * 100;

        double bankuaiTodayMinuteXiangDui = bankuai.todayMinuteDataList.get(1).startEndDiff * 100 - hushen300BanKuaiData.todayMinuteDataList.get(1).startEndDiff * 100;
        double bankuaiKaipanXiangDui = bankuai.last2StartDiff * 100 - hushen300BanKuaiData.last2StartDiff * 100;

        double etfXiangDuiBanKuai = (todayMinuteXiangDui + kaipanXiangDui) - (bankuaiTodayMinuteXiangDui + bankuaiKaipanXiangDui);
        return (etfXiangDuiBanKuai < -0.5 ? ANSI_RED : (etfXiangDuiBanKuai > 0.5 ? ANSI_GREEN : ""));
    }

    private static String etfTodayOneMinutteDesc(BankuaiWithData etf, BankuaiWithData bankuai) {
        if (etf == null || isSimpleMode) {
            return "";
        }
        double todayMinuteXiangDui = etf.todayMinuteDataList.get(1).startEndDiff * 100 - hushen300BanKuaiData.todayMinuteDataList.get(1).startEndDiff * 100;
        double kaipanXiangDui = etf.last2StartDiff * 100 - hushen300BanKuaiData.last2StartDiff * 100;

        double bankuaiTodayMinuteXiangDui = bankuai.todayMinuteDataList.get(1).startEndDiff * 100 - hushen300BanKuaiData.todayMinuteDataList.get(1).startEndDiff * 100;
        double bankuaiKaipanXiangDui = bankuai.last2StartDiff * 100 - hushen300BanKuaiData.last2StartDiff * 100;

        double etfXiangDuiBanKuai = (todayMinuteXiangDui + kaipanXiangDui) - (bankuaiTodayMinuteXiangDui + bankuaiKaipanXiangDui);
        etfSumTodayDiffAfter1min.add(getTodayDiffAfter1min(etf) * 100);
        return String.format("板块：%-7s  \t" +
                        //今日一分钟
                        "今日一分钟涨跌：%.3f%% \t" + ANSI_RESET +
                        //今日开盘
                        "今日开盘相对涨跌:%.3f%%" +
                        " [即:%.3f%%] \t  " + ANSI_RESET +
                        //etf 比 板块
                        getEtfXiangDuiBanKuaiColor(etf, bankuai) + "【截止开盘一分钟etf相对板块:%.3f%%】" + ANSI_RESET +
//                        //今日
//                        " 今日相比大盘涨跌：%.2f%%" +
//                        " [即:%.2f%%]， " +
                        "   [一分钟后:%.2f%%]， " +
                        "波动:%.2f%%  " +
                        //时间
                        "\t | 时间：%s  " +
                        //已有收益
                        "\t[已有收益:%.3f%%] " +
                        //归一化收益
                        "\t归一化相对收益:%.3f%%" +
                        " [即:%.3f%%] \t  " + ANSI_RESET +
                        "\n",
                fillName(etf.getBankuaiName()),
                //今日一分钟
                etf.todayMinuteDataList.get(1).startEndDiff * 100,
                //今日开盘
                kaipanXiangDui,
                etf.last2StartDiff * 100,
                //etf 比 板块
                etfXiangDuiBanKuai,
//                //今日
//                (etf.getLast30DayInfoMap().get(todayDate).startEndDiff - hushen300BanKuaiData.getLast30DayInfoMap().get(todayDate).startEndDiff) * 100,
//                etf.getLast30DayInfoMap().get(todayDate).startEndDiff * 100,
                //一分钟后
                getTodayDiffAfter1min(etf) * 100,
                etf.getBoDong() * 100,
                //时间
                etf.todayMinuteDataList.get(1).dateTime,
                //已有收益
                etf.test0_EndIndexShouyim * 100,
                //归一化收益
                etf.testMinuteShouYiSum * 100 - hushen300BanKuaiData.testMinuteShouYiSum * 100,
                etf.testMinuteShouYiSum * 100
        );
    }

    static String fillName(String name) {
        if (name.length() == 2) {
            return name.charAt(0) + "   " + name.charAt(1);
        }
        return name;
    }

    static String fillName(BankuaiWithData bankuai) {
        String name = bankuai.bankuaiName;
        if (bankuai.getBoDong() >= KeChuang50BanKuaiData.getBoDong() * 1.2) {
            name += " 3*";
        } else if (bankuai.getBoDong() >= KeChuang50BanKuaiData.getBoDong()) {
            name += " 2*";
        } else if (bankuai.getBoDong() >= KeChuang50BanKuaiData.getBoDong() * 0.8) {
            name += " 1*";
        } else {
            name += " 0*";
        }
        return name;
    }

    @NotNull
    public static BankuaiWithData getBankuaiWithData(String name, String code) {
        BankuaiWithData bankuaiWithData = new BankuaiWithData();
        bankuaiWithData.setBankuaiName(name);
        try {
            waitAll(
                    () -> {
                        bankuaiWithData.setTodayMinuteDataList(getTodayMinuteDataList(code));
                    },
                    () -> {
                        bankuaiWithData.setZhuLiList(getZiJin(code));
                    },
                    () -> {
                        bankuaiWithData.setLast30DayInfoList(getLast30DayData(code));
                    }
            );
            bankuaiWithData.setLast30DayInfoMap(bankuaiWithData.getLast30DayInfoList().stream().collect(Collectors.toMap(kk -> kk.date, kk -> kk)));
            bankuaiWithData.setLastDayDetail(bankuaiWithData.getLast30DayInfoMap().get(lastDate));
            bankuaiWithData.setLast2StartDiff(bankuaiWithData.getTodayMinuteDataList().get(0).start / bankuaiWithData.getLastDayDetail().end - 1);
        } catch (Exception e) {
            throw e;
        }
        return bankuaiWithData;
    }


    //分钟级别
    @SneakyThrows
    private static List<OneData> getTodayMinuteDataList(String bankuaiName) {
        String jqueryString = getMinuteData(bankuaiName);
        String arr[] = jqueryString.split("\\(|\\)");
        List<OneData> oneDataList = JSON.parseObject(arr[1]).getJSONObject("data").getJSONArray("trends") //前 6 分钟，第一分钟为开盘
                .stream().map(e -> {
                    String one = (String) e;
                    String[] tmp = one.split(",");
                    OneData oneData = new OneData();
                    oneData.setDateTime(tmp[0]);
                    oneData.setStart(Double.parseDouble(tmp[1]));
                    oneData.setEnd(Double.parseDouble(tmp[2]));
                    oneData.setStartEndDiff(oneData.end / oneData.start - 1);
                    return oneData;
                }).collect(Collectors.toList());
        return oneDataList;
    }

    //30天
    @SneakyThrows
    private static List<shangZheng.Main.OneDayDataDetail> getLast30DayData(String bankuaiCode) {
        JSONArray jsonArray = getDayData(bankuaiCode);
        List<String> list = jsonArray.stream().map(e -> (String) e).collect(Collectors.toList());
        List<shangZheng.Main.OneDayDataDetail> detailList = Utils.parseDongFangCaiFuList(list);
        return detailList.subList(detailList.size() - 30, detailList.size());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BankuaiWithData {
        BanKuai banKuai;
        String bankuaiName;
        List<OneData> todayMinuteDataList;
        int todayMinuteSort;
        double todayMaxPrice = -10000000;
        double todayMinPrice = 100000000;
        List<ZiJin> zhuLiList;
        double testMinuteShouYiSum = 0.0;
        double test0_EndIndexShouyim = 0.0;
        double last2StartDiff;//今日开盘涨跌
        int last2StartDiffSort;//开盘涨跌排名，值越小排名越小
        List<shangZheng.Main.OneDayDataDetail> last30DayInfoList;
        Map<String, shangZheng.Main.OneDayDataDetail> last30DayInfoMap;
        shangZheng.Main.OneDayDataDetail lastDayDetail;
        int lastDayZhangFuSort;//昨日日内涨跌排名，值越小排名越小
        XiangDuiBiLi30Day xiangDuiBiLi30Day;
        //板块对应的 etf 信息
        BankuaiWithData etfBankuaiWithData;

        public double getBoDong() {
            return lastDayDetail.last20dayBoDong;
        }

        public double getTodayBoDong() {
            return todayMaxPrice / todayMinPrice - 1;
        }

        //一分钟后的胜率
        public double getTodayShengLv() {
            return ((todayMaxPrice + todayMinPrice) / 2 - todayMinuteDataList.get(1).end) //中位线收益
                    / (todayMaxPrice - todayMinPrice); //日内波动
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BanKuai {
        String name;
        String code;
        String eftName;
        String etfCode;
        boolean isSkipLog;
    }

    /*
    查板块对应的股票
    https://quote.eastmoney.com/center/boardlist.html#boards2-90.BK1015
    https://48.push2.eastmoney.com/api/qt/clist/get?cb=jQuery112407552430561468451_1732041877603&pn=1&pz=5&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&fltt=2&invt=2&dect=1&wbp2u=|0|0|0|web&fid=f62&fs=b:BK1015&fields=f12,f13,f14,f62&_=1732041877609
     */

    @NotNull
    private static List<BanKuai> parseAllBanKuai() {
//        https://data.eastmoney.com/bkzj/hy_5.html
        List<BanKuai> banKuaiList = JSON.parseArray(Utils.getDataByFileName("all_bankuai.json")).stream()
                .map(e -> {
                    JSONObject jsonObject = (JSONObject) e;
                    BanKuai banKuai = new BanKuai();
                    banKuai.setName(jsonObject.getString("f14"));
                    String code = jsonObject.getString("f12");
                    if (!code.contains(".")) {
                        code = jsonObject.getString("f13") + "." + code;
                    }
                    banKuai.setCode(code);
                    banKuai.setEftName(jsonObject.getString("etfName") != null ? jsonObject.getString("etfName") : "etf");
                    banKuai.setEtfCode(jsonObject.getString("etfCode"));
                    banKuai.setSkipLog(Objects.equals(jsonObject.getBoolean("isSkip"), true));
                    return banKuai;
                }).collect(Collectors.toList());
//        banKuaiList = banKuaiList.stream().filter(e -> !StringUtils.isEmpty(e.etfCode)).map(e -> {
//            return new BanKuai(e.eftName, e.etfCode, e.name, e.code, e.isSkipLog);
//        }).collect(Collectors.toList());
        return banKuaiList;
    }

    @Data
    public static class OneData {
        String dateTime;
        Double start;
        Double end;
        Double startEndDiff;//当日波动
    }

    @SneakyThrows
    private static String getMinuteData(String bankuaiCode) throws IOException {
        long ms = System.currentTimeMillis();
        String url = "https://push2his.eastmoney.com/api/qt/stock/trends2/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13&fields2=f51,f52,f53,f54,f55,f56,f57,f58&ut=fa5fd1943c7b386f172d6893dbfba10b&iscr=0&ndays=1&secid=" +
                bankuaiCode + "&cb=jQuery35109680847083872344_" +
                ms + "&_=" + (ms + 50);
        return getData(url);
    }

    @SneakyThrows
    private static JSONArray getDayData(String bankuaiCode) throws IOException {
        long ms = System.currentTimeMillis();
        String url = "https://push2his.eastmoney.com/api/qt/stock/kline/get?cb=jQuery35105715717072793236_"
                + ms + "&secid=" + bankuaiCode +
                "&ut=fa5fd1943c7b386f172d6893dbfba10b&fields1=f1%2Cf2%2Cf3%2Cf4%2Cf5%2Cf6&fields2=f51%2Cf52%2Cf53%2Cf54%2Cf55%2Cf56%2Cf57%2Cf58%2Cf59%2Cf60%2Cf61&klt=101&fqt=1&end=20500101&lmt=120&_="
                + (ms + 50);
        String jqueryString = getData(url);
        String arr[] = jqueryString.split("\\(|\\)");
        JSONArray jsonArray = JSON.parseObject(arr[1]).getJSONObject("data").getJSONArray("klines");
        return jsonArray;
    }

    @SneakyThrows
    private static List<ZiJin> getZiJin(String bankuaiCode) {
        try {
            long ms = System.currentTimeMillis();
            String url = "https://push2.eastmoney.com/api/qt/stock/fflow/kline/get?cb=jQuery1123007371597491709281_" +
                    +ms + "&lmt=0&klt=1&fields1=f1%2Cf2%2Cf3%2Cf7&fields2=f51%2Cf52%2Cf53%2Cf54%2Cf55%2Cf56%2Cf57%2Cf58%2Cf59%2Cf60%2Cf61%2Cf62%2Cf63%2Cf64%2Cf65&ut=b2884a393a59ad64002292a3e90d46a5&secid=" +
                    bankuaiCode + "&_=" + (ms + 50);
            String jqueryString = getData(url);
            String arr[] = jqueryString.split("\\(|\\)");
            JSONArray jsonArray = JSON.parseObject(arr[1]).getJSONObject("data").getJSONArray("klines");
            if (jsonArray != null && jsonArray.size() != 0) {
                return jsonArray.stream().map(e -> {
                    String oneLine = (String) (e);
                    String[] arr2 = oneLine.split(",");
                    String time = arr2[0];
                    double dadan = Double.parseDouble(arr2[4]) / 1e8;
                    double chaodadan = Double.parseDouble(arr2[5]) / 1e8;
                    return new ZiJin(time, dadan, chaodadan);
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.out.println(bankuaiCode + "获取主力资金出错");
        }
        return new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ZiJin {
        String time;
        //两者之和表示主力
        double daDan;
        double chaoDaDan;

        double getZhuLi() {
            return daDan + chaoDaDan;
        }
    }

    @NotNull
    @SneakyThrows
    private static String getData(String url) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("authority", "stock.xueqiu.com")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("accept-language", "zh-CN,zh;q=0.9")
                .addHeader("sec-ch-ua-platform", "\"macOS\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-site")
                .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    @SneakyThrows
    public static void waitAll(Runnable... task) {
        List<Future> futureList = Arrays.stream(task).map(t -> executorService.submit(t)).collect(Collectors.toList());
        waitAll(futureList);
    }

    private static void waitAll(List<Future> futureList) {
        futureList.forEach(e -> {
            try {
                e.get();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            } catch (ExecutionException ex) {
                if (ex.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) (ex.getCause());
                }
                throw new RuntimeException(ex.getCause());
            }
        });
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
}

/**
 * 猜想：
 * 1 美元降息 ； 美股+a股涨； 但是美股可能吸收a股的资金，a股不升反降； 如果人民币跟着降息，则a股升；
 */


//今日一分钟涨跌：0.680% 	今日开盘相对涨跌:0.858% [即:0.912%] 一分钟主流流入 1.45 :1 ;2分钟 0.8:1.1
//中药：0.4:0.04  0.7:0.08


//https://query.sse.com.cn/commonSoaQuery.do?jsonCallBack=jsonpCallback7107801&isPagination=true&pageHelp.pageSize=500&pageHelp.pageNo=1&pageHelp.beginPage=1&pageHelp.cacheSize=1&pageHelp.endPage=1&pagecache=false&sqlId=FUND_LIST&fundType=00&subClass=03&_=1732125094515
//https://www.sse.com.cn/assortment/fund/etf/list/ 所有沪基etf