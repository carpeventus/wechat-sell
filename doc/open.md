# 微信开放平台的开发使用
> 本文档仅针对问我借用了账号的情况（借用了支付账号并且在有效期内）, 如果自己有开放账号, 可直接按照慕课网视频或者微信文档开发

1. 准备好openid和网址, openid是在“师兄干货”里的那个。网址，比如支付调试网址是`http://abc.natapp.cc/sell/pay/create`, 这次接收code的网址是`http://abc.natapp.cc/sell/wechat/qrUserInfo`
2. 电脑浏览器下面的链接, 注意替换openid和接收code的网址(需要urlEncode，看后面的示例)

    ```
    https://open.weixin.qq.com/connect/qrconnect?appid=wx6ad144e54af67d87&redirect_uri=http%3A%2F%2Fsell.springboot.cn%2Fsell%2Fqr%2F{OPENID}&response_type=code&scope=snsapi_login&state={接收code的网址}
    ```
3. 最后会重定向到`接收code的网址?code=xxxxxxxxxxx`
4. 例子

    openid是oTgZpweNnfivA9ER9EIXoHAjlrWQ
    
    接收code的网址是http://abc.natapp.cc/sell/wechat/qrUserInfo
    
    那么访问的地址为
    
    ```
    https://open.weixin.qq.com/connect/qrconnect?appid=wx6ad144e54af67d87&redirect_uri=http%3A%2F%2Fsell.springboot.cn%2Fsell%2Fqr%2FoTgZpweNnfivA9ER9EIXoHAjlrWQ&response_type=code&scope=snsapi_login&state=http%3a%2f%2fabc.natapp.cc%2fsell%2fwechat%2fqrUserInfo
    ```
    
    最后会重定向到
    
    ```
    http://abc.natapp.cc/sell/wechat/qrUserInfo?code=001bitQu0uteYd13BsQu0TetQu0bitQ
    ```
    拿到code之后即可获取扫码者的openid了, 还不清楚? 请观看慕课网《Spring Boot企业微信点餐系统》第12章卖家扫码登录部分
5. 问：扫码之后, pc端不会跳转是什么原因?
   
    答：{接收code的网址}不能访问
    
6. 问：课程视频中/qrUserInfo有个state参数，这里为什么没有？
   
    课程视频是这样的
    
    ![图片](https://img.mukewang.com/szimg/5bd94a2900018dff19201039.jpg)
    
    答：借用我的账号和自己拥有账号，开发调试肯定会有些区别。借用我的账号，我已经做了一次跳转了。这个地方，最重要的是可以获取到openid，之后的跳转url你可以先写死，调试通了就行，以后有了自己的账号就能打通了。
