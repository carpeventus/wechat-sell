package com.shy.wechatsell.handler;

import com.shy.wechatsell.config.ProjectUrlConfig;
import com.shy.wechatsell.exception.SellException;
import com.shy.wechatsell.exception.SellerAuthorizeException;
import com.shy.wechatsell.util.ResultVOUtil;
import com.shy.wechatsell.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Haiyu
 * @date 2018/10/30 15:16
 */
@ControllerAdvice
public class SellerExceptionHandler {
    @Autowired
    private ProjectUrlConfig projectUrlConfig;

    // 拦截登录异常，若发生异常，说明用户未登录，则引导用户重新登录
    @ExceptionHandler(SellerAuthorizeException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handlerAuthorizeException() {
        return "redirect:"+
                projectUrlConfig.getWechatOpenAuthorize()+
                "/sell/wechat/qrAuthorize"+
                "?returnUrl="+
                projectUrlConfig.getSell()+
                "/sell/user/login";
    }

    @ExceptionHandler(SellException.class)
    @ResponseBody
    public ResultVO handlerSellException(SellException e) {
        return ResultVOUtil.error(e.getCode(),e.getMessage());
    }
}
