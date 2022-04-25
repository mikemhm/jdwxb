
import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.http.HttpRequest;
import org.springframework.util.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 京东无线宝
 * Created by mikemhm 2022/04/21.
 */
public class JdwxbUtils {
    static String url = "https://gw.smart.jd.com/f/service/";
    static String accessKey = "b8f9c108c190a39760e1b4e373208af5cd75feb4";
    static String hmacKey = "706390cef611241d57573ca601eb3c061e174948";

    /**
     * 获取登录
     * @param body
     * @param accessKey
     * @param hmacKey
     * @return
     */
    private static String getAuthorization(String body,String accessKey,String hmacKey){
        Calendar cal = Calendar.getInstance();
        int totalDays = cal.get(Calendar.DAY_OF_YEAR);
        String deviceKey = "Android6.5.5MI 69:"+totalDays;
        deviceKey = DigestUtils.md5DigestAsHex(deviceKey.getBytes());
        String time = getJdNowDateString();
        String text = deviceKey+"postjson_body"+body+time+accessKey+deviceKey;
        HMac hMac = new HMac(HmacAlgorithm.HmacSHA1,hmacKey.getBytes());
        String decode = Base64.encode(hMac.digest(text));
        String authorization = "smart "+accessKey+":::"+decode+":::"+time;
        return authorization;
    }

    /**
     * 获取设备上次下载速度
     * @param feedId {'DCD87C48A3CC':{'device_name':'亚瑟','feed_id':265861649341072740},'DCD87C2C9E29':{'device_name':'鲁班','feed_id':401521648985258773}}
     * @param cmd
     * @return
     *
     * cmd
     * [
     *     "get_device_list",    # 获取设备列表 在线与离线的客户端状态
     *     "get_router_status_info",    # 获取路由器状态信息 上传与下载
     *     "get_router_status_detail",    # 获取路由器版本 mac  sn  上传  下载  cpu  路由在线时间(秒)  wanip  内存
     *     "jdcplugin_opt.get_pcdn_status",    # 获取路由器插件版本   缓存大小
     *     "reboot_system"     # 重启路由器
     * ]
     *
     */
    public static String getControlDevice(String feedId,String cmd){
        String sendUrl = url + "controlDevice?hard_platform=MI 6&app_version=6.5.5&plat_version=9&channel=jdCloud&plat=Android";
        String body = "{\"feed_id\":\""+feedId+"\",\"command\":[{\"current_value\":{\"cmd\":\""+cmd+"\"},\"stream_id\":\"SetParams\"}]}";
        String authorization = getAuthorization(body, accessKey, hmacKey);
        String result = HttpRequest.post(sendUrl)
                .header("tgt","AAJiT47PAED4wfpCyMYrWY5aHIahE-_rxstA_QPdpvucIPhCLtj0mXXjVZ9P9kUJV8SBxOE0J1qWjqazRpDwZgLAiJEgnx1M")
                .header("accessKey",accessKey)
                .header("pin","JDRouterPush")
                .header("appkey","996")
                .header("User-Agent","Android")
                .header("Host","gw.smart.jd.com")
                .header("Authorization",authorization)
                .body(body).timeout(4000).execute().body();
        return result;
    }

    /**
     * 获取今日，总收益
     * @return
     */
    public static String getIncome(){
        String sendUrl = "https://router-app-api.jdcloud.com/v1/regions/cn-north-1/todayPointDetail?sortField=today_point&sortDirection=DESC&pageSize=30&currentPage=1";
        String result = HttpRequest.get(sendUrl)
                .header("x-app-id", "996")
                .header("Content-Type", "application/json")
                .header("wskey", "AAJiT47PAED4wfpCyMYrWY5aHIahE-_rxstA_QPdpvucIPhCLtj0mXXjVZ9P9kUJV8SBxOE0J1qWjqazRpDwZgLAiJEgnx1M")
                .timeout(4000).execute().body();
        return result;
    }

    public static String getJdNowDateString() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
    }

}
