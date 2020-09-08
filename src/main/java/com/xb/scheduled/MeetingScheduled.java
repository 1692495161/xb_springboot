package com.xb.scheduled;

import com.xb.entity.Meeting;
import com.xb.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * @author cjj
 * @date 2020/9/3
 * @description
 */
@Component //加入容器管理
public class MeetingScheduled {

    @Autowired
    private MeetingService meetingService;

    //设置定时规则：每年的每月每日每小时的每分钟的每一秒刷新一次
    @Scheduled(cron = "1 * * * * ?")
    public void test() {

        //查询所有会议状态不为2的，结束的会议不用管理

        List<Meeting> meetingList = meetingService.findByStatusNot(2L);

        for (Meeting meeting : meetingList) {
            //当前的时间戳
            long currTime = new Date().getTime();
            //会议开始的时间戳
            long startTime = meeting.getStartTime().getTime();
            //会议结束的时间戳
            long endTime = meeting.getEndTime().getTime();

            if (currTime > startTime) {
                if (currTime < endTime) {
                    //当前时间超过开始时间，小于结束，表示会议正在进行
                    meetingService.updateMeetingStatus(meeting.getId(), 1L);
                } else {
                    //说明会议结束了
                    meetingService.updateMeetingStatus(meeting.getId(), 2L);
                }
            }
        }
    }
}
