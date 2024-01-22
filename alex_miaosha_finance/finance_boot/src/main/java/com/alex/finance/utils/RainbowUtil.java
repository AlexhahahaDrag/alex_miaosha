package com.alex.finance.utils;

/**
 * 彩虹屁接口调用
 * 对接天行数据（彩虹屁）api的工具类
 * Author:木芒果
 */
public class RainbowUtil {
    public static String getRainbow() {
//        String httpUrl = "http://api.tianapi.com/caihongpi/index?key=" + PushConfigure.getRainbowKey();
//        BufferedReader reader = null;
//        String result = null;
//        StringBuilder stringBuilder = new StringBuilder();
//
//        try {
//            URL url = new URL(httpUrl);
//            HttpURLConnection connection = (HttpURLConnection) url
//                    .openConnection();
//            connection.setRequestMethod("GET");
//            InputStream is = connection.getInputStream();
//            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
//            String strRead = null;
//            while ((strRead = reader.readLine()) != null) {
//                stringBuilder.append(strRead);
//                stringBuilder.append("\r\n");
//            }
//            reader.close();
//            result = stringBuilder.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        JSONObject jsonObject = JSONObject.parseObject(result);
//        if (jsonObject == null) {
//            return "";
//        }
//        JSONArray newslist = jsonObject.getJSONArray("newslist");
//        return "\n" + newslist.getJSONObject(0).getString("content");
        return null;
    }

}