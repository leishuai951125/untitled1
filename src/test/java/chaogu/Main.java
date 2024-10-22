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
import 上证.Utils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    enum RunMode {
        YuCe, JiaoYan;
    }

    RunMode runMode = RunMode.YuCe;

    static String lastDate = "2024-10-21";
    static String todayDate = "2024-10-22";
    static double lastDapanStar2EndDiff = -0.25 / 100;

    static boolean needFilter = false;
    static boolean isSimpleMode = false;//简要模式

//    原则：1 min 涨越多越好  2 有反弹更好 3 早上涨幅不能太高  4 非科技板块*2
    //目前看归一化分数在 4～7 之间的表现最佳； 可能是调整阶段


    @Test
    public void main() throws IOException {
//        getBankuaiWithData(new BanKuai("教育", "90.BK0740"));
        List<BanKuai> banKuaiList = parseAllBanKuai();
        long starMs = System.currentTimeMillis();
        List<String> resultListt = banKuaiList.stream().parallel().map(e -> {
            return getBankuaiWithData(e);
        }).filter(Main::filter).sorted((a, b) -> {
            return (int) ((getSortValue(b) - getSortValue(a)) * 10000);
        }).map(e -> {
            String ret = todayOneMinutteDesc(e);
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
                            "归一化分数范围 0～10，分数越高越值得购买\n",
                    new Date(starMs).toLocaleString(), (endMs - starMs) / 1000.0,
                    lastDapanStar2EndDiff * 100,
                    hushen300BanKuaiData.last2StartDiff * 100, hushen300BanKuaiData.todayMinuteDataList.get(1).startEndDiff * 100);
        }
        System.out.println("===========");
        resultListt.forEach(System.out::println);
    }

    public static BankuaiWithData hushen300BanKuaiData = getBankuaiWithData(new BanKuai("沪深300", "1.000300"));

    @NotNull
    private static String getLastDayDesc(BankuaiWithData e) {
        //30 天比例，最大比例，最小比例，昨日比例
        double maxXiangDuiBiLi = -1000;
        double minXiangDuiBiLi = 1000;
        double sumXiangDuiBiLi = 0;
        double avgXiangDuiBiLi = 0;
        List<Double> xiangDuiBiLiList = new ArrayList<>(e.last30DayInfoList.size());
        Map<String, Double> xiangDuiBiLiMap = new HashMap<>(e.last30DayInfoList.size());
        for (上证.Main.OneDayDataDetail dayDataDetail : e.last30DayInfoList) {
            上证.Main.OneDayDataDetail hushenDayDataDetail = hushen300BanKuaiData.getLast30DayInfoMap().get(dayDataDetail.date);
            Double xiangDuiBiLi = dayDataDetail.todayEndDiv30Avg / hushenDayDataDetail.todayEndDiv30Avg;
            if (xiangDuiBiLi > maxXiangDuiBiLi) {
                maxXiangDuiBiLi = xiangDuiBiLi;
            }
            if (xiangDuiBiLi < minXiangDuiBiLi) {
                minXiangDuiBiLi = xiangDuiBiLi;
            }
            sumXiangDuiBiLi += xiangDuiBiLi;
            xiangDuiBiLiMap.put(dayDataDetail.date, xiangDuiBiLi);
            xiangDuiBiLiList.add(xiangDuiBiLi);
        }
        avgXiangDuiBiLi = sumXiangDuiBiLi / xiangDuiBiLiMap.size();
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        List<Double> xiangDuiBiLiList10Day = xiangDuiBiLiList.subList(xiangDuiBiLiList.size() - 10, xiangDuiBiLiList.size());
        sb.append(String.format("过去十天：%s  |", xiangDuiBiLiList10Day.stream().map(v -> String.format("%.2f", v * 100 - 100)).collect(Collectors.toList())));
        double zuoRiGuiYiHua = zuoRiGuiYiHua(xiangDuiBiLiMap.get(lastDate), maxXiangDuiBiLi, minXiangDuiBiLi);
        String color = ANSI_RESET;
        if (zuoRiGuiYiHua <= 7 && zuoRiGuiYiHua >= 4) {
            color = ANSI_RED;
        }
        if (isSimpleMode) {
            return String.format(color + "归一化分数：%.1f " + ANSI_RESET, zuoRiGuiYiHua);
        }
        sb.append(String.format(color + "归一化分数：%.1f  |  ", zuoRiGuiYiHua));
        sb.append(String.format("昨日：%.2f  |  " + ANSI_RESET, xiangDuiBiLiMap.get(lastDate) * 100 - 100));
        sb.append(String.format("过去最大：%.2f  |  ", maxXiangDuiBiLi * 100 - 100));
        sb.append(String.format("过去最小：%.2f  |  ", minXiangDuiBiLi * 100 - 100));
        sb.append(String.format("过去平均：%.2f  |  ", avgXiangDuiBiLi * 100 - 100));
        sb.append("\n");
        return sb.toString();
    }

    //归一化 0～10 ，越大越值得买
    static double zuoRiGuiYiHua(double zuoRi, double max, double min) {
        return 10 - (zuoRi - min) / (max - min) * 10;
    }

    private static boolean filter(BankuaiWithData e) {
        if (!needFilter) {
            return true;
        }
        if (e.todayMinuteDataList.get(1).startEndDiff * 100 - hushen300BanKuaiData.todayMinuteDataList.get(1).startEndDiff * 100 < 0) {
            //过滤掉上涨不如大盘的
            return false;
        }
        return true;
    }

    private static String todayOneMinutteDesc(BankuaiWithData e) {
        if (isSimpleMode) {
            return "板块： " + fillName(e.getBankuaiName()) + "  \t";
        }
        return String.format("板块：%-7s  " +
                        //今日一分钟
                        ANSI_GREEN + "\t 今日一分钟相对涨跌：%.3f%% " +
                        "[即:%.3f%%]， \t  " +
                        //今日开盘
                        "今日开盘相对涨跌:%.3f%%" +
                        " [即:%.3f%%] \t  " +
                        //昨日
                        " 上日相比大盘涨跌：%.2f%%" +
                        " [即:%.2f%%]， " + ANSI_RESET +
                        //时间
                        "\t  时间：%s",
                fillName(e.getBankuaiName()),
                //今日一分钟
                e.todayMinuteDataList.get(1).startEndDiff * 100 - hushen300BanKuaiData.todayMinuteDataList.get(1).startEndDiff * 100,
                e.todayMinuteDataList.get(1).startEndDiff * 100,
                //今日开盘
                e.last2StartDiff * 100 - hushen300BanKuaiData.last2StartDiff * 100,
                e.last2StartDiff * 100,
                //昨日
                (e.lastDayDetail.startEndDiff - lastDapanStar2EndDiff) * 100,
                e.lastDayDetail.startEndDiff * 100,
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
    public static BankuaiWithData getBankuaiWithData(BanKuai e) {
        BankuaiWithData bankuaiWithData = new BankuaiWithData();
        bankuaiWithData.setBankuaiName(e.getName());
        try {
            bankuaiWithData.setTodayMinuteDataList(getTodayMinuteDataList(e.getCode()));
            bankuaiWithData.setLast30DayInfoList(getLast30DayData(e.getCode()));
            bankuaiWithData.setLast30DayInfoMap(bankuaiWithData.getLast30DayInfoList().stream().collect(Collectors.toMap(kk -> kk.date, kk -> kk)));
            bankuaiWithData.setLastDayDetail(bankuaiWithData.getLast30DayInfoMap().get(lastDate));
            bankuaiWithData.setLast2StartDiff(bankuaiWithData.getTodayMinuteDataList().get(0).start / bankuaiWithData.getLastDayDetail().end - 1);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return bankuaiWithData;
    }

    double getSortValue(BankuaiWithData bankuaiWithData) {
//        return bankuaiWithData.getTodayMinuteDataList().get(1).startEndDiff;
        return bankuaiWithData.getLast30DayInfoMap().get(todayDate).getStartEndDiff();
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
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BanKuai {
        String name;
        String code;
    }

    @NotNull
    private static List<BanKuai> parseAllBanKuai() {
//        https://data.eastmoney.com/bkzj/hy_5.html
        List<BanKuai> banKuaiList = JSON.parseArray(Utils.getDataByFileName("all_bankuai")).stream().map(e -> {
            JSONObject jsonObject = (JSONObject) e;
            BanKuai banKuai = new BanKuai();
            banKuai.setName(jsonObject.getString("f14"));
            banKuai.setCode(jsonObject.getString("f13") +
                    "." + jsonObject.getString("f12"));
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
