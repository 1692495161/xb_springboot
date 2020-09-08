package com.xb.mapper;

import com.xb.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ArticleMapper {


    List<User> findFavoriteList(Map param);
}
