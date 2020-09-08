package com.xb.webSocket;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cjj
 * @date 2020/9/5
 * @description
 */
@Component
@ServerEndpoint("/xb_socket/{userId}")
public class XBWebSocket {

    //当前在线人数
    private static Integer onlineCount = 0;

    //存储当前所有的会话
    private static final Map<Long, Session> sessions = new ConcurrentHashMap<>();

    //与客户端建立连接
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        //保存当前会话
        sessions.put(userId, session);

        //在线人数+1
        addOnlineCount();

        System.out.println("当前会议人数: " + getOnlineCount());

    }

    //与客户端断开连接
    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        //删除当前会话
        sessions.remove(userId);

        //在线人数-1
        subOnlineCount();

        System.out.println("当前会议人数: " + getOnlineCount());
    }

    //群发信息
    public static void sendMessage(String message) {

        for (Long userId : sessions.keySet()) {
            //获取所有的会话
            Session session = sessions.get(userId);

            if (session != null) {
                session.getAsyncRemote().sendText(message);
            }
        }

    }

    //单发信息
    public static void sendMessage(Long userId,String message){
        Session session = sessions.get(userId);
        if (session != null) {
            session.getAsyncRemote().sendText(message);
        }
    }

    //指定某人不发
    public static void sendMessageNotUser(Long userId,String message){

        for (Long oneId : sessions.keySet()) {

            if (oneId!=userId){

                Session session = sessions.get(oneId);

                session.getAsyncRemote().sendText(message);

            }

        }

    }


    //获取当前在线人数
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    //在线人数+1
    public static synchronized void addOnlineCount() {
        XBWebSocket.onlineCount++;
    }

    //在线人数-1
    public static synchronized void subOnlineCount() {
        XBWebSocket.onlineCount--;
    }
}
