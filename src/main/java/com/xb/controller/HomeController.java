package com.xb.controller;

import com.xb.entity.Result;
import com.xb.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjj
 * @date 2020/9/4
 * @description
 */
@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private HomeService homeService;


    /*
    *@date 2020/9/4
    *@param []
    *@return com.xb.entity.Result
    *@description 主页的数据查询
    */
    @GetMapping
    public Result findCount(){

        //查询当天新增的用户，文章和会议数
        Map<String, Object> allCount = homeService.findHomeCount();

        //查询最近7天新增的用户，文章和会议数
        List<Map<String, Object>> homeDetail = homeService.findHomeDetail();
        /*
         * 把map数据转换为集合数据，方便前端折线图的数据填充
         * Map:{day1:0 day2:3 day3:1 day4:2 day5:3 day6:1 day7:1}
         * List: [0,3,1,2,3,1,1]
         */
        List<List> countList=new ArrayList<>();

        for (Map<String,Object> map : homeDetail) {

            List mapList=new ArrayList();

           /* for (Object key : map.keySet()) {
                mapList.add(map.get(key));
            }*/
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object val = map.get(entry.getKey());
                mapList.add(val);
            }

            countList.add(mapList);
        }

        Map<String,Object> returnMap=new HashMap();
        // count数据
        returnMap.put("allCount",allCount);
        // 报表详细数据
        returnMap.put("countList", countList);

        return new Result(true,"查询成功",returnMap);
    }

}
