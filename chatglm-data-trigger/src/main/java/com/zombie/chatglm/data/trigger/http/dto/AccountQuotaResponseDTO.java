package com.zombie.chatglm.data.trigger.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description 账户额度传输对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountQuotaResponseDTO {

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
