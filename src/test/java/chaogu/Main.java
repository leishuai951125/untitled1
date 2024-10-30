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
import org.springframework.util.StringUtils;
import 上证.Utils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main {
    enum RunMode {
        YuCe, JiaoYan;
    }

    RunMode runMode = RunMode.YuCe;

    static String lastDate = "2024-10-29";
    static String todayDate = "2024-10-30";

    static double lastDapanStar2EndDiff = -1.0 / 100.0;

    //25min整结束集合竞价，30分整开始交易

    static boolean needFilter = false;
    static boolean isSimpleMode = false;//简要模式

//    原则：1 min 涨越多越好  2 有反弹更好 3 早上涨幅不能太高  4 非科技板块*2
    //目前看归一化分数在 4～7 之间的表现最佳； 可能是调整阶段
    //最近几天上涨趋势


    @Test
    public void main() throws IOException {
//        getBankuaiWithData(new BanKuai("教育", "90.BK0740"));
        List<BanKuai> banKuaiList = parseAllBanKuai();
        long starMs = System.currentTimeMillis();
        List<BankuaiWithData> bankuaiWithDataList = banKuaiList.stream().parallel().map(e -> {
            BankuaiWithData bankuaiWithData = getBankuaiWithData(e.getName(), e.getCode());
            if (!StringUtils.isEmpty(e.getEtfCode())) {
                bankuaiWithData.etfBankuaiWithData = getBankuaiWithData(e.getEftName(), e.getEtfCode());
            }
            return bankuaiWithData;
        }).collect(Collectors.toList());
        //过滤
        bankuaiWithDataList = bankuaiWithDataList.stream().filter(Main::filter).collect(Collectors.toList());
        //填充归一化、比例
        bankuaiWithDataList.forEach(e -> e.setXiangDuiBiLi30Day(getXiangDuiBiLi30Day(e)));
        //填充归一化排名
        AtomicInteger sort = new AtomicInteger(0);
        bankuaiWithDataList.stream().sorted((a, b) -> (int) ((a.xiangDuiBiLi30Day.zuoRiGuiYiHua - b.xiangDuiBiLi30Day.zuoRiGuiYiHua) * 1000))
                .collect(Collectors.toList())
                .forEach(e -> e.xiangDuiBiLi30Day.guiyiHuaPaiMing = sort.incrementAndGet());
        //打印
        List<String> resultListt = bankuaiWithDataList.stream().filter(Main::filter).sorted((a, b) -> {
            return (int) ((getSortValue(b) - getSortValue(a)) * 10000);
        }).map(e -> {
            String ret = todayOneMinutteDesc(e);
            if (e.etfBankuaiWithData != null) {
                ret += todayOneMinutteDesc(e.etfBankuaiWithData);
            }
            return ret + getLastDayDesc(e);
        }).collect(Collectors.toList());
        long endMs = System.currentTimeMillis();
        if (isSimpleMode) {
            System.out.printf("开始时间：%s, 花费时间：%.2f s  \n" +
                            "昨日大盘涨跌：%.2f%% \n" +
                            "今日大盘开盘涨跌：%.2f%%\n" +
                            "归一化分数范围 0～10，分数在 4～7 分的值得购买 \n",
                    new Date(starMs).toLocaleString(), (endMs - starMs) / 1000.0,
                    lastDapanStar2EndDiff * 100,
                    hushen300BanKuaiData.last2StartDiff * 100);
        } else {
            System.out.printf("开始时间：%s, 花费时间：%.2f s  \n" +
                            "昨日大盘涨跌：%.2f%% \n" +
                            "今日大盘开盘涨跌：%.2f%%,今日大盘一分钟涨跌：%.2f%%\n" +
                            "归一化分数范围 0～10，分数在 4～7 分的值得购买 \n",
                    new Date(starMs).toLocaleString(), (endMs - starMs) / 1000.0,
                    lastDapanStar2EndDiff * 100,
                    hushen300BanKuaiData.last2StartDiff * 100, hushen300BanKuaiData.todayMinuteDataList.get(1).startEndDiff * 100);
        }
        System.out.println("===========");
        resultListt.forEach(System.out::println);

        System.out.println("===========");

        AtomicInteger sortCount = new AtomicInteger();

        System.out.println("===========");
        int groupSize = 10;
        bankuaiWithDataList.stream().collect(Collectors.groupingBy(a -> (a.xiangDuiBiLi30Day.guiyiHuaPaiMing - 1) / groupSize))
                .entrySet().stream().map(entry -> {
                    ShouYiTongJi guiYiHua2ShouYi = new ShouYiTongJi();
                    guiYiHua2ShouYi.setPaiMing(entry.getKey() * groupSize);
                    guiYiHua2ShouYi.setShouYiList(entry.getValue().stream()
                            .map(e -> e.getLast30DayInfoMap().get(todayDate).last2EndDiff).collect(Collectors.toList()));
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

    public static BankuaiWithData hushen300BanKuaiData = getBankuaiWithData("沪深300", "1.000300");

    double getSortValue(BankuaiWithData bankuaiWithData) {
        return bankuaiWithData.getTodayMinuteDataList().get(1).startEndDiff;
//        return getTodayDiffAfter1min(bankuaiWithData);
//        return bankuaiWithData.getLast30DayInfoMap().get(todayDate).getStartEndDiff();
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
        String color = ANSI_RESET;
        //归一化 35（涨幅80名）～80（涨幅47名）
        if (xiangDuiBiLi30Day.guiyiHuaPaiMing <= 50 && xiangDuiBiLi30Day.guiyiHuaPaiMing >= 10) {
            color = ANSI_RED;
        }
        if (isSimpleMode) {
            return String.format(color + "相对价格：%.1f " + ANSI_RESET, xiangDuiBiLi30Day.zuoRiGuiYiHua);
        }
        sb.append(String.format(color + "价格排名：%d  |  " + ANSI_RESET, xiangDuiBiLi30Day.guiyiHuaPaiMing));
        sb.append(String.format("相对价格：%.1f  |  ", xiangDuiBiLi30Day.zuoRiGuiYiHua));
        sb.append(String.format("昨日：%.2f  |  ", xiangDuiBiLi30Day.xiangDuiBiLiMap.get(lastDate) * 100 - 100));
        sb.append(String.format("过去最大：%.2f  |  ", xiangDuiBiLi30Day.maxXiangDuiBiLi * 100 - 100));
        sb.append(String.format("过去最小：%.2f  |  ", xiangDuiBiLi30Day.minXiangDuiBiLi * 100 - 100));
        sb.append(String.format("过去平均：%.2f  |  ", xiangDuiBiLi30Day.avgXiangDuiBiLi * 100 - 100));
        sb.append("\n");
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
        List<上证.Main.OneDayDataDetail> last30DayInfoWithoutTodyList = e.last30DayInfoList;

        if (last30DayInfoWithoutTodyList.get(last30DayInfoWithoutTodyList.size() - 1).date.equals(todayDate)) {
            //去掉今天
            last30DayInfoWithoutTodyList = last30DayInfoWithoutTodyList.subList(0, last30DayInfoWithoutTodyList.size() - 1);
        }
        //todo 取 x 天试试
        last30DayInfoWithoutTodyList = last30DayInfoWithoutTodyList.subList(last30DayInfoWithoutTodyList.size() - 10, last30DayInfoWithoutTodyList.size());

        List<Double> xiangDuiBiLiList = new ArrayList<>(last30DayInfoWithoutTodyList.size());
        Map<String, Double> xiangDuiBiLiMap = new HashMap<>(last30DayInfoWithoutTodyList.size());
        for (上证.Main.OneDayDataDetail dayDataDetail : last30DayInfoWithoutTodyList) {
            上证.Main.OneDayDataDetail hushenDayDataDetail = hushen300BanKuaiData.getLast30DayInfoMap().get(dayDataDetail.date);
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

    private static String todayOneMinutteDesc(BankuaiWithData e) {
        if (isSimpleMode) {
            return "板块： " + fillName(e.getBankuaiName()) + "  \t";
        }
        double todayMinuteXiangDui = e.todayMinuteDataList.get(1).startEndDiff * 100 - hushen300BanKuaiData.todayMinuteDataList.get(1).startEndDiff * 100;
        double kaipanXiangDui = e.last2StartDiff * 100 - hushen300BanKuaiData.last2StartDiff * 100;
        double zuoRiXiangDui = (e.lastDayDetail.startEndDiff - lastDapanStar2EndDiff) * 100;
        return String.format("板块：%-7s  " +
                        //今日一分钟
                        (todayMinuteXiangDui > 1 ? ANSI_RED : ANSI_GREEN) + "\t 今日一分钟相对涨跌：%.3f%% " +
                        "[即:%.3f%%]， \t  " + ANSI_RESET +
                        //今日开盘
                        (kaipanXiangDui < 0 ? ANSI_RED : "") + "今日开盘相对涨跌:%.3f%%" +
                        " [即:%.3f%%] \t  " + ANSI_RESET +
                        //昨日
                        (zuoRiXiangDui < 0 ? ANSI_RED : "") + " 上日相比大盘涨跌：%.2f%%" +
                        " [即:%.2f%%]， " + ANSI_RESET +
                        //今日
                        " 今日相比大盘涨跌：%.2f%%" +
                        " [即:%.2f%%]， " +
                        "   [一分钟后:%.2f%%]， " +
                        //时间
                        "\t  时间：%s" +
                        "\n",
                fillName(e.getBankuaiName()),
                //今日一分钟
                todayMinuteXiangDui,
                e.todayMinuteDataList.get(1).startEndDiff * 100,
                //今日开盘
                kaipanXiangDui,
                e.last2StartDiff * 100,
                //昨日
                zuoRiXiangDui,
                e.lastDayDetail.startEndDiff * 100,
                (e.getLast30DayInfoMap().get(todayDate).startEndDiff - hushen300BanKuaiData.getLast30DayInfoMap().get(todayDate).startEndDiff) * 100,
                e.getLast30DayInfoMap().get(todayDate).startEndDiff * 100,
                //一分钟后
                getTodayDiffAfter1min(e) * 100,
                //时间
                e.todayMinuteDataList.get(1).dateTime
        );
    }

    static String fillName(String name) {
        if (name.length() == 2) {
            return name.charAt(0) + "   " + name.charAt(1);
        }
        return name;
    }

    @NotNull
    public static BankuaiWithData getBankuaiWithData(String name, String code) {
        BankuaiWithData bankuaiWithData = new BankuaiWithData();
        bankuaiWithData.setBankuaiName(name);
        try {
            bankuaiWithData.setTodayMinuteDataList(getTodayMinuteDataList(code));
            bankuaiWithData.setLast30DayInfoList(getLast30DayData(code));
            bankuaiWithData.setLast30DayInfoMap(bankuaiWithData.getLast30DayInfoList().stream().collect(Collectors.toMap(kk -> kk.date, kk -> kk)));
            bankuaiWithData.setLastDayDetail(bankuaiWithData.getLast30DayInfoMap().get(lastDate));
            bankuaiWithData.setLast2StartDiff(bankuaiWithData.getTodayMinuteDataList().get(0).start / bankuaiWithData.getLastDayDetail().end - 1);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return bankuaiWithData;
    }


    //分钟级别
    private static List<OneData> getTodayMinuteDataList(String bankuaiName) throws IOException {
        String jqueryString = getMinuteData(bankuaiName);
        String arr[] = jqueryString.split("\\(|\\)");
        List<OneData> oneDataList = JSON.parseObject(arr[1]).getJSONObject("data").getJSONArray("trends").subList(0, 2) //前 6 分钟，第一分钟为开盘
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
    private static List<上证.Main.OneDayDataDetail> getLast30DayData(String bankuaiCode) throws IOException {
        JSONArray jsonArray = getDayData(bankuaiCode);
        List<String> list = jsonArray.stream().map(e -> (String) e).collect(Collectors.toList());
        List<上证.Main.OneDayDataDetail> detailList = Utils.parseDongFangCaiFuList(list);
        return detailList.subList(detailList.size() - 30, detailList.size());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BankuaiWithData {
        String bankuaiName;
        List<OneData> todayMinuteDataList;
        double last2StartDiff;//今日开盘涨跌
        List<上证.Main.OneDayDataDetail> last30DayInfoList;
        Map<String, 上证.Main.OneDayDataDetail> last30DayInfoMap;
        上证.Main.OneDayDataDetail lastDayDetail;
        XiangDuiBiLi30Day xiangDuiBiLi30Day;
        //板块对应的 etf 信息
        BankuaiWithData etfBankuaiWithData;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BanKuai {
        String name;
        String code;
        String eftName;
        String etfCode;
    }

    @NotNull
    private static List<BanKuai> parseAllBanKuai() {
//        https://data.eastmoney.com/bkzj/hy_5.html
        List<BanKuai> banKuaiList = JSON.parseArray(Utils.getDataByFileName("all_bankuai.json")).stream().map(e -> {
            JSONObject jsonObject = (JSONObject) e;
            BanKuai banKuai = new BanKuai();
            banKuai.setName(jsonObject.getString("f14"));
            banKuai.setCode(jsonObject.getString("f13") +
                    "." + jsonObject.getString("f12"));
            banKuai.setEftName(jsonObject.getString("etfName") != null ? jsonObject.getString("etfName") : "etf");
            banKuai.setEtfCode(jsonObject.getString("etfCode"));
            return banKuai;
        }).collect(Collectors.toList());
        return banKuaiList;
    }

    @Data
    public static class OneData {
        String dateTime;
        Double start;
        Double end;
        Double startEndDiff;//当日波动
    }

    private static String getMinuteData(String bankuaiCode) throws IOException {
        long ms = System.currentTimeMillis();
        String url = "https://push2his.eastmoney.com/api/qt/stock/trends2/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13&fields2=f51,f52,f53,f54,f55,f56,f57,f58&ut=fa5fd1943c7b386f172d6893dbfba10b&iscr=0&ndays=1&secid=" +
                bankuaiCode + "&cb=jQuery35109680847083872344_" +
                ms + "&_=" + (ms + 50);
        return getData(url);
    }

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

    @NotNull
    private static String getData(String url) throws IOException {
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

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
}
