package chaogu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import shangZheng.Main;
import shangZheng.Utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Last30DayInfoUtils {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Last30DayInfo {
        List<Main.OneDayDataDetail> detailList;//120 天数据
        Map<String, Main.OneDayDataDetail> detailMap;//120 天数据
    }

    public static Last30DayInfo getLast30DayInfo(String test) {
        return getLast30DayInfo(JSON.parseArray(test, String.class));
    }

    public static Last30DayInfo getLast30DayInfo(JSONArray jsonArray) {
        List<String> list = jsonArray.stream().map(e -> (String) e).collect(Collectors.toList());
        return getLast30DayInfo(list);
    }

    public static Last30DayInfo getLast30DayInfo(List<String> jsonArray) {
        List<Main.OneDayDataDetail> detailList = Utils.parseDongFangCaiFuList(jsonArray);
        detailList = detailList.subList(detailList.size() - 30, detailList.size());//只取最近30天
        Map<String, Main.OneDayDataDetail> detailMap = detailList.stream().collect(Collectors.toMap(e -> e.date, e -> e));
        return new Last30DayInfo(detailList, detailMap);
    }
}
