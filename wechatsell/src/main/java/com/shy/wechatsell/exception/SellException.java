package com.shy.wechatsell.exception;

import com.shy.wechatsell.enums.ResultEnum;
import lombok.Getter;

/**
 * @author Haiyu
 * @date 2018/10/17 9:56
 */
@Getter
public class SellException extends RuntimeException {
    private Integer code;
    public SellException(ResultEnum resultEnum) {
        super(resultEnum.getMsg());
        this.code = resultEnum.getCode();
    }

    public SellException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
