package gupiao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Data {


    public static void main(String[] args) throws IOException {
//        System.out.println(ff3("BABA"));
        allStock();
    }

    private static void allStock() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://stock.xueqiu.com/v5/stock/batch/quote.json?symbol=BABA%2CBEKE%2CDAO%2CDDL%2CEDU%2CDADA%2CDOYU%2CEH%2CTCOM%2CTC%2CTAOP%2CTANH%2CTAL%2CSY%2CSXTC%2CSVM%2CSVA%2CSTG%2CSPI%2CSOS%2CSOL%2CSOHU%2CSNTG%2CSJ%2CSISI%2CSGLY%2CSEED%2CRLX%2CRETO%2CRERE%2CNAAS%2CRCON%2CBGM%2CQH%2CQFIN%2CQD%2CPT%2CPLAG%2CPETZ%2CPDD%2COPRA%2COCG%2COCFT%2CNTES%2CNOAH%2CNIU%2CNISN%2CNIO%2CNCTY%2CMVST%2CMTC%2CMSC%2CMOMO%2CMOGU%2CMNSO%2CMLCO%2CMHUA%2CMFH%2CMDJH%2CLXEH%2CLX%2CLU%2CLKCO%2CLITB%2CLI%2CKXIN%2CKUKE%2CKRKR%2CKNDI%2CKC%2CJZXN%2CJWEL%2CJKS%2CJG%2CJFU%2CJFIN%2CJD%2CITP%2CIQ%2CIMAB%2CIH%2CIFBD%2CICLK%2CSTEC%2CHUYA%2CHUIZ%2CHUDI%2CHTHT%2CHIHO%2CHCM%2CGURE%2CGTEC%2CGOTU%2CGHG%2CGDS%2CFUTU%2CFTFT%2CFINV%2CFENG%2CFEDU%2CFANH%2CFAMI%2CYMM%2CYJ%2CYI%2CYGMZ%2CXYF%2CXPEV%2CXNET%2CXIN%2CWNW%2CWIMI%2CWDH%2CWB%2CWAFU%2CVNET%2CVIPS%2CVIOT%2CUXIN%2CUTSI%2CUPC%2CUK%2CTUYA%2CTROO%2CTOUR%2CTME%2CTIRX%2CTIGR%2CZTO%2CZLAB%2CZKIN%2CZH%2CZEPP%2CZCMD%2CYY%2CYUMC%2CYSG%2CYRD%2CYQ%2CTSM%2CHKD%2CHSAI")
                .method("GET", null)
                .addHeader("authority", "stock.xueqiu.com")
                .addHeader("accept", "*/*")
                .addHeader("accept-language", "zh-CN,zh;q=0.9")
                .addHeader("cookie", "xq_a_token=dbc1dc6d13bd101dd06f18c5b7f2fb2eb276fb5a; xqat=dbc1dc6d13bd101dd06f18c5b7f2fb2eb276fb5a; xq_r_token=8009cc86908134cef1e05f27b0fbea84bea0abb7; xq_id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOi0xLCJpc3MiOiJ1YyIsImV4cCI6MTczMDUwODgwMCwiY3RtIjoxNzI4NDg5NDE0Mjk1LCJjaWQiOiJkOWQwbjRBWnVwIn0.of1GaaFquvjuvlkKojobRfov6d5BXxBjHDyA8H1ewLMCwfmJb37WaT03f6jh5JZRJxp0P9WLXQ-y9fcjBOyh5qUrXmC0QHGBzpVIs8pZVJr6TJdebb8bs-r9Adf2ETm1_4BeBsZlF60enEhb6Nac0TUl0qLpTMa-63UZvaSKwpYPjfRmJGJPLjrt0AbQVRuN5_mFqI458tjBoRrJxc0xtrvpbrYBYN1siJJ_boIekktADp6xO4rzrFB9FxpnpQKvEWOygAl1HkFI9qCQh7opVxLtDEhpbstfT7kNZZLmufLSbSABCpDPABVo6doVn5ILDexx6Klcx89ivfWPLGopFQ; cookiesu=521728489443811; u=521728489443811; Hm_lvt_1db88642e346389874251b5a1eded6e3=1728489445; HMACCOUNT=123D7CD2EEF5B445; device_id=e81b056ae1921590b8ea1eaaade7fa73; s=aq129oe62b; is_overseas=0; ssxmod_itna=Yq0x2DuDgD00G=KD=DXKDH7yxcDUEibO80qAIqf3qD/QQEDnqD=GFDK40of8o4KdQK7+hoTjY+AuwsU3R0o+F5G/DxLxD=ixiTD4q07Db4GkDAqiOD7Lq5Dxpq0rD74irDDxD3pTL/zO0UDDF/MjcRqDEDYpMDA3Di4D+UdtDmudDGkKDbqQDIMtL7GiDz4TnYwUMmB4qWmpBDB67xBQciL0DcwXcRZ4P6i4jfDA4ABAeQGDPmGxezA4e8FxNCzx4ADx4l2wUaY+tADA2xD3pnbD; ssxmod_itna2=Yq0x2DuDgD00G=KD=DXKDH7yxcDUEibO80qAIq3oG9F5iiDBwigx7PqTqMHgPkD6A0K+5uUxC005qFx7QK2DjKD27YD=; Hm_lpvt_1db88642e346389874251b5a1eded6e3=1728491428")
                .addHeader("origin", "https://xueqiu.com")
                .addHeader("referer", "https://xueqiu.com/S/BK1526")
                .addHeader("sec-ch-ua", "\"Chromium\";v=\"116\", \"Not)A;Brand\";v=\"24\", \"Google Chrome\";v=\"116\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"macOS\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-site")
                .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36")
                .build();
        Response response = client.newCall(request).execute();
        JSONObject resp = JSON.parseObject(response.body().string());
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Map<String, List<Stock>> allStock = resp.getJSONObject("data").getJSONArray("items").stream().map(e -> {
            Stock stock = new Stock();
            JSONObject quote = ((JSONObject) e).getJSONObject("quote");
            stock.setName(quote.getString("name"));
            stock.setCode(quote.getString("code"));
//            stock.setSubType(Optional.ofNullable(quote.getString("sub_type")).orElse("-1"));
//            if (atomicInteger.incrementAndGet() < 10) {
            try {
                stock.setSubType("");
//                stock.setSubType(ff3(stock.getCode()));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
//            }
            return stock;
        }).collect(Collectors.groupingBy(e -> e.getSubType()));
        System.out.println(JSON.toJSONString(allStock));
        System.out.println(Optional.ofNullable(allStock.get("")).map(e -> e.size()).orElse(0));
    }

    @lombok.Data
    public static class Stock {
        String name;
        String code;
        String subType;
    }

    //https://emweb.eastmoney.com/PC_USF10/pages/index.html?code=BABA&type=web&color=w#/gsgk
    //所属行业
    private static String ff3(String code) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://datacenter.eastmoney.com/securities/api/data/v1/get?reportName=RPT_USF10_INFO_ORGPROFILE&columns=SECUCODE%2CSECURITY_CODE%2CORG_CODE%2CSECURITY_INNER_CODE%2CORG_NAME%2CORG_EN_ABBR%2CBELONG_INDUSTRY%2CFOUND_DATE%2CCHAIRMAN%2CREG_PLACE%2CADDRESS%2CEMP_NUM%2CORG_TEL%2CORG_FAX%2CORG_EMAIL%2CORG_WEB%2CORG_PROFILE&quoteColumns=&filter=(SECURITY_CODE%3D%22" + code + "%22)&pageNumber=1&pageSize=200&sortTypes=&sortColumns=&source=SECURITIES&client=PC&v=019269436504287296")
//                .method("GET", body)
                .addHeader("Accept", "*/*")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
                .addHeader("Connection", "keep-alive")
                .addHeader("Origin", "https://emweb.eastmoney.com")
                .addHeader("Referer", "https://emweb.eastmoney.com/")
                .addHeader("Sec-Fetch-Dest", "empty")
                .addHeader("Sec-Fetch-Mode", "cors")
                .addHeader("Sec-Fetch-Site", "same-site")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36")
                .addHeader("sec-ch-ua", "\"Chromium\";v=\"116\", \"Not)A;Brand\";v=\"24\", \"Google Chrome\";v=\"116\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"macOS\"")
                .build();
        Response response = client.newCall(request).execute();
        JSONObject resp = JSON.parseObject(response.body().string());
        return resp.getJSONObject("result").getJSONArray("data").getJSONObject(0).getString("BELONG_INDUSTRY");
    }


    //中概股当日走势：https://xueqiu.com/S/KXIN
    private static void ff1() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
//        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://stock.xueqiu.com/v5/stock/chart/minute.json?symbol=BK1526&period=1d")
                .method("GET", null)
                .addHeader("authority", "stock.xueqiu.com")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("accept-language", "zh-CN,zh;q=0.9")
                .addHeader("cookie", "xq_a_token=dbc1dc6d13bd101dd06f18c5b7f2fb2eb276fb5a; xqat=dbc1dc6d13bd101dd06f18c5b7f2fb2eb276fb5a; xq_r_token=8009cc86908134cef1e05f27b0fbea84bea0abb7; xq_id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOi0xLCJpc3MiOiJ1YyIsImV4cCI6MTczMDUwODgwMCwiY3RtIjoxNzI4NDg5NDE0Mjk1LCJjaWQiOiJkOWQwbjRBWnVwIn0.of1GaaFquvjuvlkKojobRfov6d5BXxBjHDyA8H1ewLMCwfmJb37WaT03f6jh5JZRJxp0P9WLXQ-y9fcjBOyh5qUrXmC0QHGBzpVIs8pZVJr6TJdebb8bs-r9Adf2ETm1_4BeBsZlF60enEhb6Nac0TUl0qLpTMa-63UZvaSKwpYPjfRmJGJPLjrt0AbQVRuN5_mFqI458tjBoRrJxc0xtrvpbrYBYN1siJJ_boIekktADp6xO4rzrFB9FxpnpQKvEWOygAl1HkFI9qCQh7opVxLtDEhpbstfT7kNZZLmufLSbSABCpDPABVo6doVn5ILDexx6Klcx89ivfWPLGopFQ; cookiesu=521728489443811; u=521728489443811; Hm_lvt_1db88642e346389874251b5a1eded6e3=1728489445; HMACCOUNT=123D7CD2EEF5B445; device_id=e81b056ae1921590b8ea1eaaade7fa73; s=aq129oe62b; Hm_lpvt_1db88642e346389874251b5a1eded6e3=1728490115; ssxmod_itna=eqjOY57K4moh87DX7DHD0Wvq0=QDtDnl1e8z1Co=DUx0y0weGzDAxn40iDto1Nf+2PKNUlBhL+oFrBuP7KsF0ELNozS0pfxbDneG0DQKGmDBKDSDWKD9F=xiiMDCeDIDWeDiDG+OcL2kF7aDDdD+KkSxGWDm4zDWPDYxDrBo5DRZPD0aqDEPKDuh5xHaYDNpO8pa5zhW2DdS+CDlIjDC98EOAGMc5kSF2+We+dQe+exZheY=i4qibYHjG2oDZxCe145mC4+lx5ssGxLj4HC1xDi=0Ppupi4D; ssxmod_itna2=eqjOY57K4moh87DX7DHD0Wvq0=QDtDnl1e8z1Co=DRDnIdquPDsQwDL0ilGs0PS+i8D8hxUdB4bkDbCsK0e52I592QnWeBb0GgvEh8IYx=4T=0=3czeom6k2eooKS7Cf5nzydavq/AKwR+f2l35+DTIijg+jz0ikzDjDgRLq83meWuIIK0u53ktCSohA/DQ+GevtYBG+SiXOBC0=LMje7mfdBYjMeee6SPN4yjw5SmaZYwIeS=mhZ8LeF9dwLFtFgq9zKYolzBjAd+wp6jY4S6nc7Ay2oSOU3LEm3GwIsSlSVUjmDuW8WNghXYrNtO0se0VY4EWXxQ4u03tcd/rq4=IA700ht1CUe2PN9w6gvXgpmBXNA0mj2T+wncis0Dm2DZl6QKQwQiOzKX1UxlGpZNXB3UWb8nwUxnIpP2CtkouIGfNnvOjjbc5DpmGQ0SDI4iLNEeF2ia1ewFYCgr+R4NUwlrjtK59S5IPFSKpzryqgeD4KwK92QNuzU8SW/8bvlKPSEdD44+rkGw2EaZp71p3Bn5ZW5NUgYgIqHmxvdaRpWITZI6C+5kZgrwavjmF/KHDG2Dx5RAqkSYDTYIewDlquef7a=KPqK0IYGEn2xDSPbOuT/Rkzx7bbUrX9rE/0mbO7cUtBh22UtSgtR+LzYT8bRu/kz+gyEt4p2v8K1sBfVIzPSVStjoMiqDDLxD+aqRnxlxyuv0YT+LGQiG=0xEnqeCkWYiDIhyiqmiumizv=CPe0KUDiq5Y7xb=041DTm+TaOQaeGDD=")
                .addHeader("origin", "https://xueqiu.com")
                .addHeader("referer", "https://xueqiu.com/S/BK1526")
                .addHeader("sec-ch-ua", "\"Chromium\";v=\"116\", \"Not)A;Brand\";v=\"24\", \"Google Chrome\";v=\"116\"")
                .addHeader("sec-ch-ua-mobile", "?0")
                .addHeader("sec-ch-ua-platform", "\"macOS\"")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-site", "same-site")
                .addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36")
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
    }
}
