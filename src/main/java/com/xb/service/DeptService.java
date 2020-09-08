package com.xb.service;


import com.xb.dao.DeptDao;
import com.xb.entity.Dept;
import com.xb.mapper.DeptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author cjj
 * @date 2020/8/31
 * @description
 */
@Service
public class DeptService {

    @Autowired
    DeptDao deptDao;

    @Autowired
    DeptMapper deptMapper;

    public List<Dept> findAll() {
        return deptDao.findAll();
    }

    public Dept findById(Long deptId) {
        return deptDao.findById(deptId).get();
    }

    //查询全部部门信息  部门信息+部门人数
    public List<Map> findAllDept() {
        return deptMapper.findAllDept();
    }
}
