package com.xb.dao;

import com.xb.entity.Dept;
import com.xb.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


/**
 * @author cjj
 * @date 2020/8/31
 * @description
 */
public interface MeetingDao extends JpaRepository<Meeting,Long>, JpaSpecificationExecutor<Meeting> {


    @Query("select count(1) from MeetingJoin mj where mj.mId=?1")
    Integer countJoinMeeting(Long meetingId);

    @Query("select count(1) from MeetingJoin mj where mj.mId=?2 and mj.uId=?1")
    Integer isJoinMeeting(Long userId, Long meetingId);

    @Query("delete from MeetingJoin mj where mj.uId=?1 and mj.mId=?2")
    @Modifying
    void deleteJoinMeeting(Long userId, Long meetingId);

    @Query(value = "insert into meeting_join values (?1,?2)",nativeQuery = true)
    @Modifying
    void insertJoinMeeting(Long userId, Long meetingId);

    List<Meeting> findByStatusNot(long status);

    @Query("update Meeting m set m.status=?2 where m.id=?1")
    @Modifying
    void updateMeetingStatus(Long meetingId, Long status);
}
