package com.xb.dao;

import com.xb.entity.Dept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


/**
 * @author cjj
 * @date 2020/8/31
 * @description
 */
public interface DeptDao extends JpaRepository<Dept,Long>, JpaSpecificationExecutor<Dept> {


}
