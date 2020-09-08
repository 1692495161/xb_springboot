package com.xb.controller;

import com.xb.entity.Article;
import com.xb.entity.PageResult;
import com.xb.entity.Result;
import com.xb.entity.User;
import com.xb.service.ArticleService;
import com.xb.util.LoginUserUtil;
import com.xb.webSocket.XBWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjj
 * @date 2020/9/2
 * @description
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /*
     *@date 2020/9/2
     *@param [article]
     *@return com.xb.entity.Result
     *@description  发布文章
     */
    @PostMapping
    public Result save(@RequestBody Article article) {

        //发布人的id和真实名字
        article.setUserId(LoginUserUtil.getId());
        article.setPublishRealName(LoginUserUtil.getLoginUser().getRealName());

        articleService.save(article);

        return new Result(true, "发布成功");
    }

    /*
     *@date 2020/9/2
     *@param [article]
     *@return com.xb.entity.Result
     *@description 文章搜索+分页+倒序
     */
    @PostMapping("/search/{page}")
    public Result search(@RequestBody Map searchMap, @PathVariable Integer page) {

        if (searchMap.get("title") == null) {
            searchMap.put("title", "");
        }

        Page<Article> pageData = articleService.findPage("%" + searchMap.get("title").toString() + "%", page);

        PageResult<Article> pageResult = new PageResult<>(pageData.getTotalPages(), pageData.getContent());

        Map returnMap = new HashMap();
        returnMap.put("pageResult", pageResult);
        returnMap.put("title", searchMap.get("title"));

        return new Result(true, "查询成功", returnMap);
    }

    /*
     *@date 2020/9/2
     *@param [articleId]
     *@return com.xb.entity.Result
     *@description  查看文章详情
     */
    @GetMapping("/{articleId}")
    public Result search(@PathVariable Long articleId) {

        //通过id获取文章的详情
        Article article = articleService.findByArticleId(articleId);

        //该文章的收藏数(通过id获取)
        Integer favoriteCount = articleService.countFavorite(articleId);

        //登录用户是否已经收藏过该文章
        Boolean isFavorite = articleService.isFavorite(LoginUserUtil.getId(), articleId);

        //登录用户关注的好友列表中，是否也收藏该文章
        List<User> favoriteList = articleService.findFavoriteList(LoginUserUtil.getId(), articleId);

        //浏览数+1,(浏览自己的文章不会)
        if (LoginUserUtil.getId() != article.getUserId().longValue()) {
            //表示不是自己发布的，浏览数+1
            articleService.updateBrowserCount(articleId);
        }

        //封装成map返回前端
        Map returnMap = new HashMap();
        returnMap.put("article", article);
        returnMap.put("favoriteCount", favoriteCount);
        returnMap.put("isFavorite", isFavorite);
        returnMap.put("favoriteList", favoriteList);

        return new Result(true, "查询成功", returnMap);
    }

    /*
     *@date 2020/9/2
     *@param [articleId, isFavorite]
     *@return com.xb.entity.Result
     *@description 点击收藏和取消收藏
     */
    @PostMapping("/favorite/{articleId}")
    public Result favorite(@PathVariable Long articleId, Boolean isFavorite) {
        if (isFavorite) {
            //如果为true，表示已收藏，取消收藏
            articleService.unFavorite(LoginUserUtil.getId(), articleId);

            //更新收藏数，收藏数-1
            Integer favoriteCount = articleService.countFavorite(articleId);

            return new Result(true, "取消收藏成功", favoriteCount);
        } else {
            //如果为false，表示点击收藏
            articleService.favorite(LoginUserUtil.getId(), articleId);

            //更新收藏数，收藏数+1
            Integer favoriteCount = articleService.countFavorite(articleId);

            //查询该文章是谁发布的
            Article article = articleService.findByArticleId(articleId);
            Long userId = article.getUserId();
            //收藏文章是给发布者推送信息
            XBWebSocket.sendMessage(userId,LoginUserUtil.getLoginUser().getRealName()+"刚刚收藏了您的文章!文章名："+article.getTitle());

            return new Result(true, "收藏成功", favoriteCount);
        }
    }

    /*
    *@date 2020/9/2
    *@param [searchMap, page]
    *@return com.xb.entity.Result
    *@description 我的收藏列表+分页
    */
    @PostMapping("/favoritePage/{page}")
    public Result favoritePage(@RequestBody Map searchMap,@PathVariable Integer page) {

        if (searchMap.get("title") == null) {
            searchMap.put("title", "");
        }

        Page<Article> pageData = articleService.favoritePage(LoginUserUtil.getId(),
                "%" + searchMap.get("title").toString() + "%", page);

        PageResult<Article> pageResult = new PageResult<>(pageData.getTotalPages(), pageData.getContent());

        Map returnMap = new HashMap();
        returnMap.put("pageResult", pageResult);
        //搜索条件回显
        returnMap.put("title", searchMap.get("title"));

        return new Result(true, "收藏成功",returnMap);
    }

}
