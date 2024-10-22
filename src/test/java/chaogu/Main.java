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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    enum RunMode {
        YuCe, JiaoYan;
    }

    RunMode runMode = RunMode.YuCe;

    static String lastDate = "2024-10-21";
    static String todayDate = "2024-10-23";
    static double lastDapanStar2EndDiff = -0.25 / 100;

//    原则：1 min 涨越多越好  2 有反弹更好 3 早上涨幅不能太高  4 非科技板块*2

    @Test
    public void main() throws IOException {
//        getBankuaiWithData(new BanKuai("教育", "90.BK0740"));
        List<BanKuai> banKuaiList = parseAllBanKuai();
        long starMs = System.currentTimeMillis();
        BankuaiWithData hushen300TodayWithData = getBankuaiWithData(new BanKuai("沪深300", "1.000300"));
        List<String> resultListt = banKuaiList.stream().parallel().map(e -> {
            return getBankuaiWithData(e);
        }).sorted((a, b) -> {
            return (int) ((getSortValue(b) - getSortValue(a)) * 10000);
        }).map(e -> {
            String ret = todayOneMinutteDesc(hushen300TodayWithData, e);
            e.last30DayInfo.detailList.subList(e.last30DayInfo.detailList.size() - 30, e.last30DayInfo.detailList.size());
            //30 天比例，最大比例，最小比例，昨日比例
            return ret;
        }).collect(Collectors.toList());
        long endMs = System.currentTimeMillis();
        System.out.printf("开始时间：%s, 花费时间：%.2f s  \n" +
                        "昨日大盘涨跌：%.2f%% \n" +
                        "今日大盘开盘涨跌：%.2f%%,今日大盘一分钟涨跌：%.2f%%\n",
                new Date(starMs).toLocaleString(), (endMs - starMs) / 1000.0,
                lastDapanStar2EndDiff * 100,
                hushen300TodayWithData.last2StartDiff * 100, hushen300TodayWithData.todayMinuteDataList.get(1).startEndDiff * 100);
        System.out.println("===========");
        resultListt.forEach(System.out::println);
    }

    private static String todayOneMinutteDesc(BankuaiWithData hushen300TodayWithData, BankuaiWithData e) {
        return String.format("板块：%-7s  " +
                        //今日一分钟
                        "\t 今日一分钟相对涨跌：%.3f%% " +
                        "[即:%.3f%%]， \t  " +
                        //今日开盘
                        "今日开盘相对涨跌:%.3f%%" +
                        " [即:%.3f%%] \t  " +
                        //昨日
                        " 上日相比大盘涨跌：%.2f%%" +
                        " [即:%.2f%%]， " +
                        //时间
                        "\t  时间：%s",
                fillName(e.getBankuaiName()),
                //今日一分钟
                e.todayMinuteDataList.get(1).startEndDiff * 100 - hushen300TodayWithData.todayMinuteDataList.get(1).startEndDiff * 100,
                e.todayMinuteDataList.get(1).startEndDiff * 100,
                //今日开盘
                e.last2StartDiff * 100 - hushen300TodayWithData.last2StartDiff * 100,
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
            bankuaiWithData.setLast30DayInfo(getLastDayData(e.getCode()));
            bankuaiWithData.setLastDayDetail(bankuaiWithData.getLast30DayInfo().getDetailMap().get(lastDate));
            bankuaiWithData.setLast2StartDiff(bankuaiWithData.getTodayMinuteDataList().get(0).start / bankuaiWithData.getLastDayDetail().end - 1);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return bankuaiWithData;
    }

    double getSortValue(BankuaiWithData bankuaiWithData) {
//        double lastGuiYiHua = bankuaiWithData.lastDayData.startEndDiff * (0.1 / Math.abs(lastDapanStar2EndDiff));//昨日涨跌归一化
//        double kaiPanGuiYiHua = bankuaiWithData.getLast2StartDiff() * (0.1 / Math.abs(lastDapanStar2EndDiff));//开盘归一化
//        double todayGuiYiHua = bankuaiWithData.getTodayMinuteDataList().get(1).startEndDiff * 20;
//        return todayGuiYiHua - (lastGuiYiHua + kaiPanGuiYiHua);
        return bankuaiWithData.getTodayMinuteDataList().get(1).startEndDiff;
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

    //天
    private static Last30DayInfoUtils.Last30DayInfo getLastDayData(String bankuaiCode) throws IOException {
        JSONArray jsonArray = getDayData(bankuaiCode);
        return Last30DayInfoUtils.getLast30DayInfo(jsonArray);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BankuaiWithData {
        String bankuaiName;
        List<OneData> todayMinuteDataList;
        double last2StartDiff;//今日开盘涨跌
        Last30DayInfoUtils.Last30DayInfo last30DayInfo;
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
}
