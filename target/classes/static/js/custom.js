/* your js go here */
$(function () {

    var webSocket = new WebSocket("ws://192.168.31.209:8080/xb_socket/"+JSON.parse(localStorage.getItem("loginUser")).id);
    webSocket.onmessage=function(event){
        layer.msg(event.data);
    }

    $("#avatar").click(function () {
        console.log("你点击了头像");
        window.location.href = '../html/user_look.html'
    });

    //file表单选中文件时,让file表单的val展示到showname这个展示框
    $('#myFile').change(function () {
        $('#showname').val($(this).val())
    });

    //用户列表，查看详情，通过class名字userDetail来找
    $(".userDetail").click(function () {
        console.log("你点击了用户详情");
        window.location.href = '../html/user_detail.html'
    });

    //发布会议跳转链接
    $("#addMeet").click(function () {
        window.location.href = '../html/meeting_add.html'
    });

    //添加文章跳转链接
    $("#addArt").click(function () {
        window.location.href = '../html/article_add.html'
    });


    $(".myList>ul>li").on("click",function () {
        if($(this).find("div").attr('class')=='list-group myGroup isHidden'){
            $(this).find("div").removeClass("isHidden")
        }else{
            $(this).find("div").addClass("isHidden")
        }


    })

    $(".table").find("input[type='checkbox']").on("click",function () {
        if($(this).prop("checked")){
            layer.msg("关注成功")
        }else{
            layer.msg("取关成功")

        }

    })


});

