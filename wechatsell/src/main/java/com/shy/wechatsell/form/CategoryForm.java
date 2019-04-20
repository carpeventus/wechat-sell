package com.shy.wechatsell.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Haiyu
 * @date 2018/10/28 23:26
 */
@Data
public class CategoryForm {
    private Integer categoryId;
    /** 类目名字 */
    @NotBlank(message = "类目名称不能为空")
    private String categoryName;
    /** 类目编号 */
    @NotNull(message = "类目编号不能为空")
    private Integer categoryType;
}
