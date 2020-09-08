package com.xb.service;

import com.xb.mapper.HomeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author cjj
 * @date 2020/9/4
 * @description
 */
@Service
public class HomeService {

    @Autowired
    HomeMapper homeMapper;

    //查询当天新增的用户，文章和会议数
    public Map<String, Object> findHomeCount(){
        return homeMapper.findHomeCount();
    }

    //查询最近7天新增的用户，文章和会议数
    public List<Map<String, Object>> findHomeDetail(){
        return homeMapper.findHomeDetail();
    }
}
