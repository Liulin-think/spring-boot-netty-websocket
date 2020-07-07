package com.liulin.request;

import lombok.Data;

/**
 * @author liulin_think
 * @Package com.liulin.request.BaseRequest
 * @date 2020/7/6
 */
@Data
public class BaseRequest<T> {
    private T data;
    private int code;
}
