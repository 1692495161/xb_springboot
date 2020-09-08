package com.xb.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface HomeMapper {

    Map<String, Object> findHomeCount();

    List<Map<String, Object>> findHomeDetail();
}
