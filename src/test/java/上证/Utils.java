package 上证;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {
    public static String getDataByFileName(String fileName) {
        String filePath = "/Users/leishuai/IdeaProjects/untitled1/src/test/java/上证/data/" + fileName;
        StringBuffer sb = new StringBuffer();
        Scanner scanner;
        try {
            File file = new File(filePath);
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                sb.append(line);
            }
            scanner.close();
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static String getLastDate(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return formatter.format(new Date(formatter.parse(date).getTime() - 24 * 3600 * 1000));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    static long getTs(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return formatter.parse(date).getTime() / 1000 / (24 * 3600);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    static class QiInfo {
        String dateFormmat;
        String timeFormat;
        long kaipan;
        long shoupan;
        double zhangdie;
    }

    public static Map<String/*dayOffset */, QiInfo> parseA50QiHuo() {
        return JSON.parseObject(Utils.getDataByFileName("a50_qihuo"))
                .getJSONObject("data")
                .getJSONObject("candle")
                .getJSONObject("CNA50F.OTC")
                .getJSONArray("lines")
                .stream().map(o -> {
                    JSONArray jsonArray = (JSONArray) o;
                    long ts = jsonArray.getLong(9);
                    String timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ts * 1000));
                    String dateFormmat = new SimpleDateFormat("yyyy-MM-dd").format(new Date(ts * 1000));
                    long kaipan = jsonArray.getLong(0);
                    long shoupan = jsonArray.getLong(1);
                    return new QiInfo(dateFormmat, timeFormat, kaipan, shoupan, (shoupan - kaipan) * 1.0 / kaipan);
                }).filter(e -> e.timeFormat.endsWith("09:00:00")).collect(Collectors.toMap(e -> e.dateFormmat, e -> e));
    }

    // 东方财富
    // https://quote.eastmoney.com/gb/zsXIN9.html
    public static Map<String, Main.OneDayDataDetail> parseDongFangCaiFuMap(List<String> jsonArray) {
        List<Main.OneDayDataDetail> list = new ArrayList<>(jsonArray.size());
        jsonArray.forEach(e -> {
            String arr[] = e.split(",");
            Main.OneDayDataDetail oneDayDataDetail = new Main.OneDayDataDetail();
            oneDayDataDetail.date = arr[0];
            oneDayDataDetail.start = Double.parseDouble(arr[1]);
            oneDayDataDetail.end = Double.parseDouble(arr[2]);
            oneDayDataDetail.startEndDiff = (oneDayDataDetail.end - oneDayDataDetail.start) / oneDayDataDetail.start;
            if (list.size() != 0) {
                Main.OneDayDataDetail lastOneDayDataDetail = list.get(list.size() - 1);
                oneDayDataDetail.last2StartDiff = (oneDayDataDetail.start - lastOneDayDataDetail.end) / lastOneDayDataDetail.end;
                oneDayDataDetail.last2EndDiff = (oneDayDataDetail.end - lastOneDayDataDetail.end) / lastOneDayDataDetail.end;
            }
            list.add(oneDayDataDetail);//jisuan
        });
        return list.stream().collect(Collectors.toMap(e -> e.getDate(), e -> e));
    }

    public static List<Main.OneDayDataDetail> parseDongFangCaiFuList(List<String> jsonArray) {
        List<Main.OneDayDataDetail> list = new ArrayList<>(jsonArray.size());
        jsonArray.forEach(e -> {
            String arr[] = e.split(",");
            Main.OneDayDataDetail oneDayDataDetail = new Main.OneDayDataDetail();
            oneDayDataDetail.date = arr[0];
            oneDayDataDetail.start = Double.parseDouble(arr[1]);
            oneDayDataDetail.end = Double.parseDouble(arr[2]);
            oneDayDataDetail.startEndDiff = (oneDayDataDetail.end - oneDayDataDetail.start) / oneDayDataDetail.start;
            if (list.size() != 0) {
                Main.OneDayDataDetail lastOneDayDataDetail = list.get(list.size() - 1);
                oneDayDataDetail.last2StartDiff = (oneDayDataDetail.start - lastOneDayDataDetail.end) / lastOneDayDataDetail.end;
                oneDayDataDetail.last2EndDiff = (oneDayDataDetail.end - lastOneDayDataDetail.end) / lastOneDayDataDetail.end;
                oneDayDataDetail.setLasOneDayDataDetail(lastOneDayDataDetail);
            }
            list.add(oneDayDataDetail);//jisuan
        });
        fillAvg(list);

        list.forEach(e -> {
            e.todayEndDiv30Avg = e.end / e.last30dayEndAvg;
        });

        return list;
    }

    private static void fillAvg(List<Main.OneDayDataDetail> kechuangList) {
        for (int i = 0; i < kechuangList.size(); i++) {
            Main.OneDayDataDetail kechuangOneDayDataDetail = kechuangList.get(i);
            if (i >= 5) {
                kechuangOneDayDataDetail.last5dayEndAvg = getLastAvg(kechuangList, i, 5);
            }
            if (i >= 10) {
                kechuangOneDayDataDetail.last10dayEndAvg = getLastAvg(kechuangList, i, 10);
            }
            if (i >= 20) {
                kechuangOneDayDataDetail.last20dayEndAvg = getLastAvg(kechuangList, i, 20);
            }
            if (i >= 30) {
                kechuangOneDayDataDetail.last30dayEndAvg = getLastAvg(kechuangList, i, 30);
                kechuangOneDayDataDetail.last30dayBoDong = avgBoDongV2(kechuangList.subList(i - 30, i));
            }
        }
    }

    static Double getLastAvg(List<Main.OneDayDataDetail> oneDayDataDetailList, int currentIndex, int lastDays) {
        if (currentIndex >= lastDays) {
            return oneDayDataDetailList.subList(currentIndex - lastDays, currentIndex).stream()
                    .mapToDouble(k -> k.getEnd()).average().getAsDouble();
        }
        return null;
    }

    public static double avgBoDongV2(List<Main.OneDayDataDetail> list) {
        return list.stream().mapToDouble(e -> e.last2EndDiff == null ? 0 : Math.abs(e.last2StartDiff)).sum() / list.size();
    }

    public static double avgBoDong(List<Main.OneDayDataDetail> list) {
        return list.stream().mapToDouble(e -> Math.abs(e.startEndDiff)).sum() / list.size();
    }

}
