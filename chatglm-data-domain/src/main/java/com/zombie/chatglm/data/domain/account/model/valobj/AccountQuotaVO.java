package com.zombie.chatglm.data.domain.account.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @description 账户额度值对象
 */
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountQuotaVO {

    /**
     * 总量额度
     */
    private Integer totalQuota;
    /**
     * 剩余额度
     */
    private Integer surplusQuota;
    /**
     * 每日免费额度
     */
    private Integer freeQuota;

}
