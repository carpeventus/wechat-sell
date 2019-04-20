package com.shy.wechatsell.dataobject;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author Haiyu
 * @date 2018/10/29 16:23
 */
@Data
@Entity
public class SellerInfo {
    @Id
    private String id;
    private String username;
    private String password;
    private String openid;
}
