package com.shy.wechatsell.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * HTTP请求返回的最外层对象
 *
 * @author Haiyu
 * @date 2018/10/16 9:13
 */
@Data
public class ResultVO<T> implements Serializable {
    private static final long serialVersionUID = -8852899642721598313L;
    /** 状态码 */
    private Integer code;
    /** 状态信息 */
    private String msg;
    /** 数据 */
    private T data;
}
