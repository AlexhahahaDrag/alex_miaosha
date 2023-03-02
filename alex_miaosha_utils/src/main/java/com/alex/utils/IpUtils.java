package com.alex.utils;

import com.alex.common.utils.string.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.lionsoul.ip2region.xdb.Searcher;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *description:  ip工具类
 * 因为使用静态块，如果初始化的时候报错，系统就找不到这个类，后续就会一直报java.lang.NoClassDefFoundError: Could not initialize class com.alex.blog.utils.utils.IpUtils错
 *author:       alex
 *createDate:   2021/7/17 21:02
 *version:      1.0.0
 */
@Slf4j
public class IpUtils {

    private static String dbPath;

    public static Searcher searcher = null;

    static {
        dbPath = createFtlFileByFtlArray();
        try {
            searcher = Searcher.newWithFileOnly(dbPath);
        } catch (IOException e) {
            System.out.printf("failed to create searcher with `%s`: %s\n", dbPath, e);
        }
    }


    /**
     * @param request
     * @description:  根据请求获取ip地址
     * @author:       alex
     * @return:       java.lang.String
     */
    public static String getIpAddr(HttpServletRequest request) {
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
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                    ipAddress = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    log.error("查不到本机ip,", e);
                    e.printStackTrace();
                }

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
     * @param request
     * @description:  获取真实ip
     * @author:       alex
     * @return:       java.lang.String
     */
    public static String getRealIp(HttpServletRequest request) {
        String ip;
        return checkNotIp(ip = request.getHeader("x-forwarded-for")) ?
                (checkNotIp(ip = request.getHeader("Proxy-Client-IP")) ?
                        (checkNotIp(ip = request.getHeader("WL-Proxy-Client-IP")) ? request.getRemoteAddr(): ip): ip) : ip;
    }

    /**
     * @param ip
     * @description:  校验ip
     * @author:       alex
     * @return:       boolean
     */
    private static boolean checkNotIp(String ip) {
        return ip == null || StringUtils.isEmpty(ip) ||
                "unknown".equalsIgnoreCase(ip) || "0:0:0:0:0:0:0:1".equals(ip);
    }

    /**
     * @param request
     * @description:  根据请求获取系统和浏览器信息
     * @author:       alex
     * @return:       java.util.Map<java.lang.String,java.lang.String>
     */
    public static Map<String, String> getOsAndBrowserInfo(HttpServletRequest request) throws Exception {
        String userAgent = request.getHeader("User-Agent");
        String user = userAgent.toLowerCase();
        String os = "";
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
        } else  {
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
                String[] split2 = userAgent.substring(userAgent.indexOf("Version")).split(" ");
                if (user.contains("safari") && user.contains("version")) {
                    browser = (userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0]
                            + "-" + (split2[0]).split("/")[1];
                } else if (user.contains("opr") || user.contains("opera")) {
                    if (user.contains("opera")) {
                        String[] split = split2;
                        browser = (userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0]
                                + "-" + (split[0]).split("/")[1];
                    } else if (user.contains("opr")) {
                        browser = ((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-"))
                                .replace("OPR", "Opera");
                    }
                } else {
                    browser = "UnKnown";
                }
            }
        } catch (Exception e) {
            log.error("获取浏览器版本失败");
            log.error(e.getMessage());
            browser = "UnKnown";
        }
        Map<String, String> result = new HashMap<>(2);
        result.put("OS", os);
        result.put("BROWSER", browser);
        String cityInfo = getCityInfo(getIpAddr(request));
        result.put("LOCATION", cityInfo);
        return result;
    }

    /**
     * 判断是否是内网IP
     *
     * @param ip
     * @return
     */
    public static boolean isInner(String ip) {
        String reg = "(10|172|192)\\.([0-1][0-9]{0,2}|[2][0-5]{0,2}|[3-9][0-9]{0,1})\\.([0-1][0-9]{0,2}|[2][0-5]{0,2}|[3-9][0-9]{0,1})\\.([0-1][0-9]{0,2}|[2][0-5]{0,2}|[3-9][0-9]{0,1})";
        Pattern p = Pattern.compile(reg);
        Matcher matcher = p.matcher(ip);
        return matcher.find();
    }

    /**
     * @param content
     * @param encodingString
     * @description:  根据ip地址获取城市信息
     * @author:       alex
     * @return:       java.lang.String
     */
    public static String getAddresses(String content, String encodingString) throws Exception {
        String ip = content.substring(3);
        String cityInfo = getCityInfo(ip);
        log.info("根据ip返回城市信息：{}", cityInfo);
        return cityInfo;

    }

    /**
     * @description:  创建ip2region.db文件
     * @author:       alex
     * @return:       java.lang.String
     */
    public static String createFtlFileByFtlArray() {
        String ftlPath = "city/";
        return createFtlFile(ftlPath, "ip2region.xdb");
    }

    /**
     * @param ftlPath
     * @param ftlName
     * @description:  创建ip2region文件
     * @author:       alex
     * @return:       java.lang.String
     */
    private static String createFtlFile(String ftlPath, String ftlName) {
        InputStream certStream = null;
        try {
            //获取当前下项目所在的绝对路径
            String proFilePath = System.getProperty("user.dir");
            String newFilePath = proFilePath + File.separator + ftlPath;
            newFilePath = newFilePath.replace("/", File.separator);
            //检查对应目录下是否存在文件
            File newFile = new File(newFilePath + ftlName);
            if (newFile.isFile() && newFile.exists()) {
                return newFile.getPath();
            }
            certStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(ftlPath + ftlName);
            assert certStream != null;
            byte[] certData = IOUtils.toByteArray(certStream);
            FileUtils.writeByteArrayToFile(newFile, certData);
            return newFilePath;
        } catch (Exception e) {
            log.info(e.getMessage());
        } finally {
            try {
                if (certStream != null) {
                    certStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @param ip
     * @description:  根据ip获取城市信息
     * @author:       alex
     * @return:       java.lang.String
     */
    private static String getCityInfo(String ip) throws Exception {
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

    /**
     * @description:  获取主机ip
     * @author:       alex
     * @return:       java.lang.String
    */
    public static String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error(e.getMessage());
        }
        return "127.0.0.1";
    }

    /**
     * @description:  获取主机名称
     * @author:       alex
     * @return:       java.lang.String
    */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "未知";
    }

    public static void main(String args[]) throws Exception {
        String ip = "116.2.203.240";
        String cityIpString = getCityInfo(ip);
        System.out.println(cityIpString);
    }
}
