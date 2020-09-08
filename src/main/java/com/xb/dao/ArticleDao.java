package com.xb.dao;

import com.xb.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


/**
 * @author cjj
 * @date 2020/8/31
 * @description
 */
public interface ArticleDao extends JpaRepository<Article,Long>, JpaSpecificationExecutor<Article> {


    Page<Article> findByTitleLikeOrderByPublishDateDesc(String title, Pageable of);

    @Query("select count(1) from Favorite f where f.aId=?1")
    Integer countFavorite(Long articleId);

    @Query("select count (1) from Favorite f where f.uId=?1 and f.aId=?2")
    Integer isFavorite(Long id, Long articleId);

    @Query("update Article a set a.browseCount=a.browseCount+1 where a.id=?1")
    @Modifying
    void updateBrowserCount(Long articleId);

    @Query("delete from Favorite f where f.uId=?1 and f.aId=?2")
    @Modifying
    void deleteFavorite(Long id, Long articleId);

    @Query(value = "insert into favorite values (?1,?2)",nativeQuery = true)
    @Modifying
    void insertFavorite(Long id, Long articleId);

    @Query("select a from Favorite f left join Article a on f.aId=a.id where f.uId=?1 and a.title like ?2 order by a.publishDate desc ")
    Page<Article> favoritePage(Long id, String title, Pageable of);
}
