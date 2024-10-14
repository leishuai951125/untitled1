package 上证;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;

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
    static class QiInfo {
        long ts;
        String dateFormmat;
        String timeFormat;
        long kaipan;
        long shoupan;
        double zhangdie;
    }

    public static Map<String/*dayOffset */, QiInfo> parseA50QiHuo() {
        return JSON.parseArray(Utils.getDataByFileName("a50_qihuo")).stream().map(o -> {
            JSONArray jsonArray = (JSONArray) o;
            long ts = jsonArray.getLong(9);
            String timeFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date(ts * 1000));
            String dateFormmat = new SimpleDateFormat("yyyy-MM-dd").format(new Date(ts * 1000));
            long kaipan = jsonArray.getLong(0);
            long shoupan = jsonArray.getLong(1);
            return new QiInfo(ts, dateFormmat, timeFormat, kaipan, shoupan, (shoupan - kaipan) * 1.0 / kaipan);
        }).filter(e -> e.timeFormat.endsWith("09-00-00")).collect(Collectors.toMap(e -> e.dateFormmat, e -> e));
    }

    // 东方财富
    // https://quote.eastmoney.com/gb/zsXIN9.html
    public static Map<String, Main.OneData> parseDongFangCaiFuMap(List<String> jsonArray) {
        List<Main.OneData> list = new ArrayList<>(jsonArray.size());
        jsonArray.forEach(e -> {
            String arr[] = e.split(",");
            Main.OneData oneData = new Main.OneData();
            oneData.date = arr[0];
            oneData.start = Double.parseDouble(arr[1]);
            oneData.end = Double.parseDouble(arr[2]);
            oneData.startEndDiff = (oneData.end - oneData.start) / oneData.start;
            if (list.size() != 0) {
                Main.OneData lastOneData = list.get(list.size() - 1);
                oneData.last2StartDiff = (oneData.start - lastOneData.end) / lastOneData.end;
                oneData.last2EndDiff = (oneData.end - lastOneData.end) / lastOneData.end;
            }
            list.add(oneData);//jisuan
        });
        return list.stream().collect(Collectors.toMap(e -> e.getDate(), e -> e));
    }
}
