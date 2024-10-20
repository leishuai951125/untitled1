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
    static String lastDate = "2024-10-17";
    static double lastDapanStar2EndDiff = -1.05 / 100;//上日大盘涨跌

//    static String lastDate = "2024-10-18";
//    static double lastDapanStar2EndDiff = 2.91 / 100;
//    原则：1 min 涨越多越好  2 有反弹更好 3 早上涨幅不能太高

    @Test
    public void main() throws IOException {
//        System.out.println(getLastDayData("BK0474"));
        List<BanKuai> banKuaiList = parseAllBanKuai();
//        System.out.println(JSON.toJSONString(banKuaiList));
        long starMs = System.currentTimeMillis();
        List<String> resultListt = banKuaiList.stream().parallel().map(e -> {
            BankuaiWithData bankuaiWithData = new BankuaiWithData();
            bankuaiWithData.setBankuaiName(e.getName());
            try {
                bankuaiWithData.setTodayMinuteDataList(getTodayMinuteDataList(e.getCode()));
                bankuaiWithData.setLastDayData(getLastDayData(e.getCode()));
                bankuaiWithData.setLast2StartDiff(bankuaiWithData.getTodayMinuteDataList().get(0).start / bankuaiWithData.getLastDayData().end - 1);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return bankuaiWithData;
        }).sorted((a, b) -> {
            return (int) ((getSortValue(b) - getSortValue(a)) * 10000);
//            if ((a.getLastDayData().startEndDiff - lastDapanStar2EndDiff) * (b.getLastDayData().startEndDiff - lastDapanStar2EndDiff) < 0) {
//                return (int) ((a.getLastDayData().startEndDiff - lastDapanStar2EndDiff) * 10000);
//            }
//            return -(int) ((a.getTodayMinuteDataList().get(1).startEndDiff - b.getTodayMinuteDataList().get(1).startEndDiff) * 10000);
        }).map(e -> String.format("板块：%-7s,  \t 今日一分钟涨跌：%.3f%%， \t  " +
                        " 上日相比大盘涨跌：%.2f%%  [即:%.2f%%]，  \t  今日开盘涨跌:%.3f%% \t  ",
                e.getBankuaiName(), e.todayMinuteDataList.get(1).startEndDiff * 100,
                (e.lastDayData.startEndDiff - lastDapanStar2EndDiff) * 100, e.lastDayData.startEndDiff * 100,
                e.last2StartDiff * 100
        )).collect(Collectors.toList());
        long endMs = System.currentTimeMillis();
        System.out.printf("开始时间：%s, 花费时间：%.2f s\n", new Date(starMs).toLocaleString(), (endMs - starMs) / 1000.0);
        System.out.println("===========");
        resultListt.forEach(System.out::println);
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
        List<OneData> oneDataList = JSON.parseObject(arr[1]).getJSONObject("data").getJSONArray("trends").subList(0, 6) //前 6 分钟，第一分钟为开盘
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
    private static OneData getLastDayData(String bankuaiCode) throws IOException {
        String jqueryString = getDayData(bankuaiCode);
        String arr[] = jqueryString.split("\\(|\\)");
        JSONArray jsonArray = JSON.parseObject(arr[1]).getJSONObject("data").getJSONArray("klines");
        return jsonArray.subList(jsonArray.size() - 4, jsonArray.size()) //倒数4天（包括今天）
                .stream().map(e -> {
                    String one = (String) e;
                    String[] tmp = one.split(",");
                    OneData oneData = new OneData();
                    oneData.setDateTime(tmp[0]);
                    oneData.setStart(Double.parseDouble(tmp[1]));
                    oneData.setEnd(Double.parseDouble(tmp[2]));
                    oneData.setStartEndDiff(oneData.end / oneData.start - 1);
                    return oneData;
                }).filter(e -> e.dateTime.equals(lastDate)).findFirst().get();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BankuaiWithData {
        String bankuaiName;
        List<OneData> todayMinuteDataList;
        double last2StartDiff;//今日开盘涨跌
        OneData lastDayData;
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
            banKuai.setCode(jsonObject.getString("f12"));
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

    private static String getMinuteData(String bankuaiName) throws IOException {
        long ms = System.currentTimeMillis();
        String url = "https://push2his.eastmoney.com/api/qt/stock/trends2/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13&fields2=f51,f52,f53,f54,f55,f56,f57,f58&ut=fa5fd1943c7b386f172d6893dbfba10b&iscr=0&ndays=1&secid=90." +
                bankuaiName + "&cb=jQuery35109680847083872344_" +
                ms + "&_=" + (ms + 50);
        return getData(url);
    }

    private static String getDayData(String bankuaiName) throws IOException {
        long ms = System.currentTimeMillis();
        String url = "https://push2his.eastmoney.com/api/qt/stock/kline/get?cb=jQuery35105715717072793236_"
                + ms + "&secid=90." + bankuaiName +
                "&ut=fa5fd1943c7b386f172d6893dbfba10b&fields1=f1%2Cf2%2Cf3%2Cf4%2Cf5%2Cf6&fields2=f51%2Cf52%2Cf53%2Cf54%2Cf55%2Cf56%2Cf57%2Cf58%2Cf59%2Cf60%2Cf61&klt=101&fqt=1&end=20500101&lmt=120&_="
                + (ms + 50);
        return getData(url);
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
