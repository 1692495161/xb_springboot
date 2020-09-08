package com.xb.service;


import com.xb.dao.DeptDao;
import com.xb.dao.MeetingDao;
import com.xb.entity.Dept;
import com.xb.entity.Meeting;
import com.xb.entity.PageResult;
import com.xb.mapper.DeptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author cjj
 * @date 2020/8/31
 * @description
 */
@Service
public class MeetingService {

    @Autowired
    MeetingDao meetingDao;

    @Autowired
    DeptDao deptDao;

    //发布会议
    public void save(Meeting meeting) {

        Long deptId = meeting.getDeptId();

        String deptName = deptDao.findById(deptId).get().getName();

        meeting.setDeptName(deptName);
        meeting.setStatus(0L);          //默认状态为0,未开始
        meeting.setPublishDate(new Date());     //发布日期为当前时间

        meetingDao.save(meeting);
    }

    //条件动态拼接
    private Specification<Meeting> creatSpec(Map searchMap){
        return new Specification<Meeting>() {
            @Override
            public Predicate toPredicate(Root<Meeting> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                //声明一个list集合来接收
                List<Predicate> predicateList=new ArrayList<>();

                if (searchMap.get("title")!=null &&!"".equals(searchMap.get("title"))){
                    //会议标题模糊查找
                    predicateList.add(cb.like(root.get("title").as(String.class),
                            "%"+searchMap.get("title").toString()+"%"));
                }

                if (searchMap.get("status")!=null &&!"".equals(searchMap.get("status"))){
                    //会议状态的精确查询
                    predicateList.add(cb.equal(root.get("status").as(Long.class),
                            Long.parseLong(searchMap.get("status").toString())));
                }

                Predicate[] predicates = new Predicate[predicateList.size()];

                //用and拼接
                return cb.and(predicateList.toArray(predicates));
            }
        };
    }

    //会议搜索+分页+倒序
    public Page<Meeting> findPage(Map searchMap, Integer page) {
        //动态条件拼接
        Specification<Meeting> spec = creatSpec(searchMap);

        Page<Meeting> pageData = meetingDao.findAll(spec, PageRequest.of(page - 1, PageResult.PAGE_SIZE, Sort.by("publishDate").descending()));

        return pageData;
    }

    //根据会议id查询会议详情
    public Meeting findByMeetingId(Long meetingId) {
        return meetingDao.findById(meetingId).get();
    }

    //根据会议id查询已经在参加会议的人数（实到人数）
    public Integer countJoinMeeting(Long meetingId) {
        return meetingDao.countJoinMeeting(meetingId);
    }

    // 查询登录用户的id(userId)是否有参加过这次会议(meetingId)
    public Boolean isJoinMeeting(Long userId, Long meetingId) {
        return meetingDao.isJoinMeeting(userId,meetingId)>0?true:false;
    }

    //取消会议
    @Transactional
    public void unJoinMeeting(Long userId, Long meetingId) {
        meetingDao.deleteJoinMeeting(userId,meetingId);
    }

    //加入会议
    @Transactional
    public void joinMeeting(Long userId, Long meetingId) {
        meetingDao.insertJoinMeeting(userId,meetingId);
    }

    //获取会议状态status不为2的会议
    public List<Meeting> findByStatusNot(Long status) {
        return meetingDao.findByStatusNot(status);
    }

    //试实时修改会议状态
    @Transactional
    public void updateMeetingStatus(Long meetingId, Long status) {
        meetingDao.updateMeetingStatus(meetingId,status);
    }
}
