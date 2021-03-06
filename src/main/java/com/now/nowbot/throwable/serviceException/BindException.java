package com.now.nowbot.throwable.serviceException;

import com.now.nowbot.throwable.TipsException;

public class BindException extends TipsException {
    public static enum Type {
        BIND_Client_RelieveBindSuccess("您已成功解绑。TuT"),//解绑_成功
        BIND_Client_RelieveBindFailed("解绑失败！请重试。OwOb"),//解绑_失败
        BIND_Client_BindingNoName("你叫啥名呀？告诉我吧"),//绑定_玩家未输入用户名
        BIND_Client_AlreadyBound("不要重复绑定哟，小沐已经记住你啦！\n(如果要改绑请悄悄私聊我哦"),//绑定_玩家早已绑定
        BIND_Client_BindingOvertime("绑定超时！请重试。OwOb"),//绑定_绑定超时

        BIND_Me_NoBind("您还没有绑定呢，请输入 !bind 点击链接登录，完成绑定吧"),//查询自己_玩家未绑定
        BIND_Me_NoAuthorization("您撤销了授权呢，请输入 !bind 点击链接登录，重新授权吧"),//查询自己_玩家撤销授权
        BIND_Me_Banned("哼哼，你号没了"),//查询自己_玩家被封禁
        BIND_Me_Blacklisted("本 Bot 根本不想理你"),//查询自己_玩家黑名单

        BIND_Player_NoBind("他还没有绑定呢，请提醒他输入 !bind 点击链接登录，完成绑定吧"),//查询他人_玩家未绑定
        BIND_Player_NoAuthorization("他撤销了授权呢，请提醒他输入 !bind 点击链接登录，重新授权吧"),//查询他人_玩家撤销授权
        BIND_Player_NoData("你查的不会是我的机器人同类吧！"),//查询他人_玩家无数据
        BIND_Player_NotFound("这是谁呀，小沐不认识哦"),//查询他人_未搜到玩家
        BIND_Player_Banned("哼哼，他号没了"),//查询他人_玩家被封禁
        BIND_Player_Blacklisted("我不想和他一起玩！"),//查询他人_玩家黑名单

        BIND_Default_NoToken("哼，你 Token 失效啦！看在我们关系的份上，就帮你这一次吧！"),//token不存在，使用本机AccessToken
        BIND_Default_PictureRenderFailed("我...我画笔坏了画不出图呃"),//图片渲染失败，或者绘图出错
        BIND_Default_PictureSendFailed("图片被麻花疼拿去祭天了"),//图片发送失败
        BIND_Default_DefaultException("我好像生病了，需要休息一会..."),//默认报错
        ;//逗号分隔
        String message;
        Type(String message) {
            this.message = message;
        }
        }
    public BindException(BindException.Type type){
        super(type.message);
    }
}