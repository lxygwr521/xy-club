package com.xyclub.wx.handler;

import java.util.Map;

/**
 * 微信消息处理器接口
 *
 * @author lxy
 * @date 2026-07-20
 */
public interface WxChatMsgHandler {

    WxChatMsgTypeEnum getMsgType();

    String dealMsg(Map<String, String> messageMap);

}
