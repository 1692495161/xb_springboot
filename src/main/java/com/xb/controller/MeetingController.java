package com.xb.controller;

import com.xb.entity.*;
import com.xb.service.DeptService;
import com.xb.service.MeetingService;
import com.xb.service.UserService;
import com.xb.util.LoginUserUtil;
import com.xb.webSocket.XBWebSocket;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author cjj
 * @date 2020/8/31
 * @description
 */
@RestController
@RequestMapping("/meeting")
public class MeetingController {

    @Autowired
    MeetingService meetingService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    HttpSession session;

    @Autowired
    HttpServletRequest request;

    /*
    *@date 2020/9/3
    *@param [meeting]
    *@return com.xb.entity.Result
    *@description 发布会议
    */
    @PostMapping
    public Result save(@RequestBody Meeting meeting){
        meetingService.save(meeting);

        //给抄送人推送提醒参加会议的信息
        String[] makeUsers = meeting.getMakeUser().split(",");

        for (String makeUserId : makeUsers) {
            String message="您接收到一个会议提示,开始时间为:"+LoginUserUtil.dateToStr(meeting.getStartTime())+",记得准时参加哦！";

            XBWebSocket.sendMessage(Long.parseLong(makeUserId),message);

        }


        return new Result(true,"发布成功");
    }

    /*
    *@date 2020/9/3
    *@param [searchMap, page]
    *@return com.xb.entity.Result
    *@description  会议搜索+分页+倒叙(发布时间)
    */
    @PostMapping("/search/{page}")
    public Result search(@RequestBody Map searchMap,@PathVariable Integer page){

        Map returnMap=new HashMap();
        returnMap.put("title",searchMap.get("title"));

        //两个查询条件，做动态拼接
        Page<Meeting> pageData=meetingService.findPage(searchMap,page);

        PageResult<Meeting> pageResult = new PageResult<>(pageData.getTotalPages(), pageData.getContent());

        returnMap.put("pageResult",pageResult);

        return new Result(true,"查询成功",returnMap);
    }

    /*
    *@date 2020/9/3
    *@param [meetingId]
    *@return com.xb.entity.Result
    *@description 根据会议id查询会议详情
    */
    @GetMapping("/{meetingId}")
    public Result detail(@PathVariable Long meetingId){
       //查询会议详情
        Meeting meeting=meetingService.findByMeetingId(meetingId);

        //查询应到人数(将会议的makeUser根据“1”分割为数组，获取其长度)
        Integer shouldJoinCount = meeting.getMakeUser().split(",").length;

        //查询实到人数
        Integer joinCount=meetingService.countJoinMeeting(meetingId);

        //查询未到人数
        Integer noJoinCount=shouldJoinCount-joinCount;

        //查询登录用户是否已经参加过这个会议
        Boolean isJoinMeeting=meetingService.isJoinMeeting(LoginUserUtil.getId(),meetingId);

        Map returnMap=new HashMap();
        returnMap.put("meeting",meeting);
        returnMap.put("shouldJoinCount",shouldJoinCount);
        returnMap.put("joinCount",joinCount);
        returnMap.put("noJoinCount",noJoinCount);
        returnMap.put("isJoinMeeting",isJoinMeeting);

        return new Result(true, "查询成功", returnMap);
    }

    /*
    *@date 2020/9/3
    *@param [searchMap, page]
    *@return com.xb.entity.Result
    *@description 参加与取消会议
    */
    @PostMapping("/joinMeeting/{meetingId}")
    public Result joinMeeting(@PathVariable Long meetingId, Boolean isJoinMeeting){

        Meeting meeting = meetingService.findByMeetingId(meetingId);

        //会议必须是为开始状态
        if (meeting.getStatus().longValue()==1L){
            return new Result(false,"会议正在进行中！");
        }

        if (meeting.getStatus().longValue()==2L){
            return new Result(false,"会议已经结束！");
        }

        //必须在抄送人列表
        String[] makeUser = meeting.getMakeUser().split(",");

        //判断数组中是否包含登录用户的id
           //1、转成list判断
        if (!Arrays.asList(makeUser).contains(LoginUserUtil.getId().toString())){
            return new Result(false, "您不能操作此次会议");
        }
           //2、遍历
        /*for (String userId : makeUser) {
            if (!userId.equals(LoginUserUtil.getId().toString())){
                return new Result(false, "您不能操作此次会议");
            }
        }*/

        if (isJoinMeeting){
            //如果为true，表示已经参加了会议，点击取消
            meetingService.unJoinMeeting(LoginUserUtil.getId(),meetingId);
            return new Result(true, "取消会议成功");
        }else {
            //如果为false，点击参加会议
            meetingService.joinMeeting(LoginUserUtil.getId(),meetingId);
            return new Result(true, "参加会议成功");
        }
    }

}
