package com.xyclub.wx.utils;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信消息解析工具
 *
 * @author lxy
 * @date 2026-07-20
 */
public class MessageUtil {

    /**
     * 解析微信发来的请求（XML）
     *
     * @param msg 消息
     * @return map
     */
    public static Map<String, String> parseXml(final String msg) {
        Map<String, String> map = new HashMap<String, String>();

        try (InputStream inputStream = new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8.name()))) {
            SAXReader reader = new SAXReader();
            Document document = reader.read(inputStream);
            Element root = document.getRootElement();
            List<Element> elementList = root.elements();

            for (Element e : elementList) {
                map.put(e.getName(), e.getText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

}
