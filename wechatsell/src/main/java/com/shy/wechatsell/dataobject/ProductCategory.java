package com.shy.wechatsell.dataobject;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 * 类目
 * @author Haiyu
 * @date 2018/10/15 9:40
 */
@Entity
@Data
@DynamicUpdate
public class ProductCategory  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;
    /** 类目名字 */
    private String categoryName;
    /** 类目编号 */
    private Integer categoryType;
    /** 创建日期 */
    private Date createTime;
    /** 更新日期 */
    private Date updateTime;

}
