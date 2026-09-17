package com.alex.utils;

import com.alex.common.utils.string.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * description:  ip工具类
 * 因为使用静态块，如果初始化的时候报错，系统就找不到这个类，后续就会一直报java.lang.NoClassDefFoundError: Could not initialize class com.alex.blog.utils.utils.IpUtils错
 * author:       alex
 * createDate:   2021/7/17 21:02
 * version:      1.0.0
 */
@Slf4j
public class IpUtils {

    private static final String dbPath = "city/ip2region.xdb";

    private static volatile String cachedLocalHostIp = null;

    public static Searcher searcher;

    static {
        try (InputStream inputStream = IpUtils.class.getClassLoader().getResourceAsStream(dbPath)) {
            if (inputStream == null) {
                log.error("Failed to find ip2region database at path: {}", dbPath);
            } else {
                byte[] cBuff = toByteArray(inputStream);
                searcher = Searcher.newWithBuffer(cBuff);
                log.info("Successfully loaded ip2region database from: {}", dbPath);
            }
        } catch (Exception e) {
            log.error("Failed to initialize IpUtils with database `{}`: {}", dbPath, e.getMessage(), e);
        }
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int n;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    /**
     * @param request description: 根据请求获取ip地址
     *                author: alex
     *                return: java.lang.String
     */
    public static String getIpAddr(HttpServletRequest request) throws Exception {
        if (request == null) {
            return "";
        }
        String ipAddress = request.getHeader("x-forwarded-for");
        if (checkNotIp(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (checkNotIp(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (checkNotIp(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress)) {
                if (cachedLocalHostIp == null) {
                    synchronized (IpUtils.class) {
                        if (cachedLocalHostIp == null) {
                            try {
                                InetAddress inet = InetAddress.getLocalHost();
                                cachedLocalHostIp = inet.getHostAddress();
                            } catch (UnknownHostException e) {
                                log.error("查不到本机ip,", e);
                                cachedLocalHostIp = "127.0.0.1";
                            }
                        }
                    }
                }
                ipAddress = cachedLocalHostIp;
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照
        if (ipAddress != null && ipAddress.length() > 15) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

    /**
     * @param ip description: 校验ip
     *           author: alex
     *           return: boolean
     */
    private static boolean checkNotIp(String ip) {
        return ip == null || StringUtils.isEmpty(ip) ||
                "unknown".equalsIgnoreCase(ip) || "0:0:0:0:0:0:0:1".equals(ip);
    }

    /**
     * @param request description: 根据请求获取系统和浏览器信息
     *                author: alex
     *                return: java.util.Map<java.lang.String, java.lang.String>
     */
    public static Map<String, String> getOsAndBrowserInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String user = userAgent.toLowerCase();
        String os;
        String browser = "";

        //os info
        if (user.contains("windows")) {
            os = "Windows";
        } else if (user.contains(("mac"))) {
            os = "Mac";
        } else if (user.contains("x11")) {
            os = "Unix";
        } else if (user.contains("android")) {
            os = "Android";
        } else if (user.contains("iphone")) {
            os = "IPhone";
        } else {
            os = "Unknown, More-Inof:" + userAgent;
        }
        //browser
        try {
            if (user.contains("edge")) {
                browser = userAgent.substring(userAgent.indexOf("Edge")).split(" ")[0].replace("/", "-");
            } else if (user.contains("msie")) {
                String substring = userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
                browser = substring.split(";")[0].split(" ")[0].replace("MSIE", "IE") + "-" + substring.split(" ")[1];
            } else if (user.contains("chrome")) {
                browser = (userAgent.substring(userAgent.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
            } else if ((user.contains("mozilla/7.0")) || (user.contains("netscape6")) ||
                    (user.contains("mozilla/4.7")) || (user.contains("mozilla/4.78")) ||
                    (user.contains("mozilla/4.08")) || (user.contains("mozilla/3"))) {
                browser = "Netscape-?";
            } else if (user.contains("firefox")) {
                browser = (userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
            } else if (user.contains("rv")) {
                String IEVersion = (userAgent.substring(userAgent.indexOf("rv")).split(" ")[0]).replace("rv:", "-");
                browser = "IE" + IEVersion.substring(0, IEVersion.length() - 1);
            } else {
                if (user.contains("safari") && user.contains("version")) {
                    int versionIdx = userAgent.indexOf("Version");
                    if (versionIdx != -1) {
                        String[] split2 = userAgent.substring(versionIdx).split(" ");
                        browser = (userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0]
                                + "-" + (split2[0]).split("/")[1];
                    } else {
                        browser = "Safari-Unknown";
                    }
                } else if (user.contains("opr") || user.contains("opera")) {
                    if (user.contains("opera")) {
                        int versionIdx = userAgent.indexOf("Version");
                        if (versionIdx != -1) {
                            String[] split2 = userAgent.substring(versionIdx).split(" ");
                            browser = (userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0]
                                    + "-" + (split2[0]).split("/")[1];
                        } else {
                            browser = "Opera-Unknown";
                        }
                    } else if (user.contains("opr")) {
                        browser = ((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-"))
                                .replace("OPR", "Opera");
                    }
                } else {
                    browser = "Unknown";
                }
            }
        } catch (Exception e) {
            log.error("获取浏览器版本失败");
            log.error(e.getMessage());
            browser = "Unknown";
        }
        Map<String, String> result = new HashMap<>(2);
        result.put("OS", os);
        result.put("BROWSER", browser);
        return result;
    }

    /**
     * param content
     */
    public static String getAddresses(String content) throws Exception {
        String ip = content.substring(3);
        String cityInfo = getCityInfo(ip);
        log.info("根据ip返回城市信息：{}", cityInfo);
        return cityInfo;

    }

    /**
     * @param ip description: 根据ip获取城市信息
     *           author: alex
     *           return: java.lang.String
     */
    public static String getCityInfo(String ip) throws Exception {
        if (StringUtils.isEmpty(dbPath)) {
            log.error("Error: Invalid ip2orgin.db file");
            return null;
        }
        if (searcher == null) {
            log.error("Error: DbConfig or DbSearcher is null");
            return null;
        }
        return searcher.search(ip);
    }

    public static void main(String[] args) throws Exception {
        String ip = "175.164.89.163";
        String cityIpString = getCityInfo(ip);
        System.out.println(cityIpString);
    }
}
