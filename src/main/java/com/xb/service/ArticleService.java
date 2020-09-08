package com.xb.service;

import com.xb.dao.ArticleDao;
import com.xb.entity.Article;
import com.xb.entity.PageResult;
import com.xb.entity.User;
import com.xb.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjj
 * @date 2020/9/2
 * @description
 */
@Service
public class ArticleService {

    @Autowired
    ArticleDao articleDao;

    @Autowired
    private ArticleMapper articleMapper;

    //发布文章
    public void save(Article article) {
        //初始浏览量和发布日期
        article.setBrowseCount(0L);
        article.setPublishDate(new Date());

        articleDao.save(article);
    }

    //搜索+分页+倒序
    public Page<Article> findPage(String title, Integer page) {
        return articleDao.findByTitleLikeOrderByPublishDateDesc(title, PageRequest.of(page-1, PageResult.PAGE_SIZE));
    }

    //通过id查询文章
    public Article findByArticleId(Long articleId) {
        return articleDao.findById(articleId).get();
    }

    //根据文章id查询收藏数
    public Integer countFavorite(Long articleId) {
        return articleDao.countFavorite(articleId);
    }

    //查询userId用户是否收藏过articleId这篇文章
    public Boolean isFavorite(Long id, Long articleId) {
        return articleDao.isFavorite(id,articleId)>0?true:false;
    }

    //查询我关注的好友列表中也收藏这篇文章的用户
    public List<User> findFavoriteList(Long id, Long articleId) {
        Map param=new HashMap();
        param.put("userId",id);
        param.put("articleId",articleId);

        return articleMapper.findFavoriteList(param);
    }

    //更新浏览量
    @Transactional
    public void updateBrowserCount(Long articleId) {
        articleDao.updateBrowserCount(articleId);
    }

    //删除收藏
    @Transactional
    public void unFavorite(Long id, Long articleId) {
        articleDao.deleteFavorite(id,articleId);
    }

    //增加收藏
    @Transactional
    public void favorite(Long id, Long articleId) {
        articleDao.insertFavorite(id,articleId);
    }

    public Page<Article> favoritePage(Long id, String title, Integer page) {
        return articleDao.favoritePage(id,title,PageRequest.of(page-1,PageResult.PAGE_SIZE));
    }
}
