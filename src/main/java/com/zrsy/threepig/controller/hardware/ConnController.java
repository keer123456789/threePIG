package com.zrsy.threepig.controller.hardware;


import com.zrsy.threepig.BigchainDB.KeyPairHolder;
import com.zrsy.threepig.Util.HttpUtil;
import com.zrsy.threepig.domain.ParserResult;
import com.zrsy.threepig.service.hardware.IConnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * 接收树莓派和nodemcu的连接请求
 */
@RestController
public class ConnController {
    protected static final Logger logger = LoggerFactory.getLogger(ConnController.class);
    //树莓派的ip地址
    private String raspberryIP = null;
    //树莓派的MAC地址
    private String raspberryMac = null;
    //nodemcu的IP地址
    private HashSet<String> nodeMcuIPs = new HashSet<>();

    @Autowired
    IConnService connService;
    //树莓派连接的最大个数
    @Value("${raspberryMaxConn}")
    private int size;

    /**
     * 树莓派发来请求，设置ip
     *
     * @param request
     * @return
     */
    @GetMapping("/setRaspberryIP/{mac}")
    public boolean setRaspberryIP(HttpServletRequest request, @PathVariable String mac) {
        raspberryIP = getIpAddr(request);
        raspberryMac = mac;
        if (!raspberryIP.equals(null)) {
            logger.info(raspberryIP);
            return true;
        } else {
            return false;
        }
    }

    /**
     * nodemcu请求服务器，获取ip
     *
     * @param request
     * @return
     */
    @GetMapping("/setNodeMCUIP")
    public boolean setNodeMCUIP(HttpServletRequest request) {
        if (nodeMcuIPs.size() >= this.size) {
            nodeMcuIPs.clear();
            return false;
        } else {
            nodeMcuIPs.add(getIpAddr(request));
            logger.info(nodeMcuIPs.toString());
            return true;
        }

    }


    /**
     * nodemcu获取树莓派ip
     *
     * @return
     */
    @GetMapping("/getRaspberryIP")
    public String getRaspberryIP() {
        return raspberryIP;
    }

    /**
     * 树莓派获取BigchainDB的密钥
     * @return 返回字符串 为./keypair.txt中的内容
     */
    @GetMapping("/getDataKey")
    public String getDataKey(){
        return KeyPairHolder.getKeyPairFormTXT();
    }


    /**
     * 前端请求BigchainDB和变量中的树莓派信息
     *
     * @return
     */
    @GetMapping("/getAllRaspberry")
    public ParserResult getAllRaspberry() {
        List<Map> list = connService.getAllRaspberry();
        if (raspberryIP!=null && raspberryMac!=null) {
            Map map = new HashMap();
            map.put("PiMac", raspberryMac);
            map.put("PiIp", raspberryIP);
            map.put("PiStatus", "未注册");
            list.add(map);
        }

        ParserResult result = new ParserResult();
        result.setStatus(ParserResult.SUCCESS);
        result.setMessage("success");
        result.setData(list);

        return result;
    }

    /**
     * 前端获得当前内存中已经发来的nodemcuIP
     *
     * @return
     */
    @GetMapping("/getNodeMcuIp")
    public ParserResult getNodeMcuIp() {
        logger.info("获得nodemcuIp的请求………………");
        ParserResult result = new ParserResult();
        result.setData(nodeMcuIPs);
        result.setMessage("success");
        result.setStatus(ParserResult.SUCCESS);
        return result;
    }

    /**
     * 前端发来请求，将资产信息发来，树莓派表 Raspberry
     *
     * @param map
     * @return
     */
    @PostMapping("/createRaspberryAsset")
    public ParserResult createRaspberryAsset(@RequestBody Map map) {
        logger.info(map.toString());
        map = (Map) map.get("data");
        ParserResult parserResult=connService.createRaspberryAsset(map);
        String resp=HttpUtil.httpGet("http://"+map.get("PiIp").toString()+":8080/isCreate/"+parserResult.getData().toString()+"/"+map.get("pigSty").toString());
        if(resp.equals("success")){
            logger.info("资产ID给树莓派发送成功");
        }else {
            logger.error("资产ID给树莓派发送失败！！ result："+resp);
        }
        raspberryMac = null;
        raspberryIP =null;
        return parserResult;
    }


    /**
     * 获取请求IP
     *
     * @param request
     * @return
     */
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1")) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ip = inet.getHostAddress();
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;
    }

}
