package com.zombie.chatglm.data.trigger.http;

import com.zombie.chatglm.data.domain.account.model.entity.OrderEntity;
import com.zombie.chatglm.data.domain.account.model.valobj.AccountQuotaVO;
import com.zombie.chatglm.data.domain.account.service.IAccountQueryService;
import com.zombie.chatglm.data.domain.auth.service.IAuthService;
import com.zombie.chatglm.data.trigger.http.dto.AccountOrderResponseDTO;
import com.zombie.chatglm.data.trigger.http.dto.AccountQuotaResponseDTO;
import com.zombie.chatglm.data.types.common.Constants;
import com.zombie.chatglm.data.types.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/account/")
public class AccountController {


    @Resource
    private IAuthService authService;

    @Resource
    private IAccountQueryService accountQueryService;

    @RequestMapping(value = "query_account_quota", method = RequestMethod.GET)
    public Response<AccountQuotaResponseDTO> queryAccountQuota(@RequestHeader("Authorization") String token) {
        try {
            //1.token校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<AccountQuotaResponseDTO>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            //2.token解析
            String openid = authService.openid(token);

            //3.查询额度
            AccountQuotaVO accountQuotaVO = accountQueryService.queryAccountQuota(openid);

            return Response.<AccountQuotaResponseDTO>builder()
                    .data(AccountQuotaResponseDTO.builder()
                            .surplusQuota(accountQuotaVO.getSurplusQuota())
                            .totalQuota(accountQuotaVO.getTotalQuota())
                            .freeQuota(accountQuotaVO.getFreeQuota())
                            .build())
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询账户额度失败", e);
            return Response.<AccountQuotaResponseDTO>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_account_orders", method = RequestMethod.GET)
    public Response<List<AccountOrderResponseDTO>> queryAccountOrderList(@RequestHeader("Authorization") String token, @RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        try {
            //1.token校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<List<AccountOrderResponseDTO>>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }
            //2.token解析
            String openid = authService.openid(token);

            //3.订单查询
            List<OrderEntity> orderList = accountQueryService.queryAccountOrderList(pageNum, pageSize, openid);
            List<AccountOrderResponseDTO> accountOrderResponseDTOList = new ArrayList<>();
            for (OrderEntity orderEntity : orderList) {
                AccountOrderResponseDTO accountOrderResponseDTO = AccountOrderResponseDTO.builder()
                        .orderId(orderEntity.getOrderId())
                        .orderTime(orderEntity.getOrderTime())
                        .payTime(orderEntity.getPayTime())
                        .orderStatus(orderEntity.getOrderStatus().getDesc())
                        .payStatus(orderEntity.getPayStatusVO().getDesc())
                        .totalAmount(orderEntity.getTotalAmount())
                        .payType(orderEntity.getPayTypeVO().getDesc())
                        .productVo(orderEntity.getProductVo())
                        .build();
                accountOrderResponseDTOList.add(accountOrderResponseDTO);
            }
            return Response.<List<AccountOrderResponseDTO>>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(accountOrderResponseDTOList)
                    .build();


        } catch (Exception e) {
            log.error("查询账户订单失败", e);
            return Response.<List<AccountOrderResponseDTO>>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_account_order_count", method = RequestMethod.GET)
    public Response<Integer> queryAccountOrderCount(@RequestHeader("Authorization") String token) {
        try {
            //1.token校验
            boolean success = authService.checkToken(token);
            if (!success) {
                return Response.<Integer>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }

            //2.token解析
            String openid = authService.openid(token);

            //3.查询订单总数
            Integer orderCount = accountQueryService.queryAccountOrderCount(openid);
            return Response.<Integer>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(orderCount)
                    .build();
        } catch (Exception e) {
            log.error("查询账户订单总数失败", e);
            return Response.<Integer>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }

    }


}
