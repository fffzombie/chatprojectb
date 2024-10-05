package com.zombie.chatglm.data.trigger.http;

import com.alibaba.fastjson.JSON;
import com.alipay.api.internal.util.AlipaySignature;
import com.google.common.eventbus.EventBus;
import com.zombie.chatglm.data.domain.auth.service.IAuthService;
import com.zombie.chatglm.data.domain.order.model.entity.PayOrderEntity;
import com.zombie.chatglm.data.domain.order.model.entity.ProductEntity;
import com.zombie.chatglm.data.domain.order.model.entity.ShopCartEntity;
import com.zombie.chatglm.data.domain.order.service.IOrderService;
import com.zombie.chatglm.data.trigger.http.dto.SaleProductDTO;
import com.zombie.chatglm.data.types.common.Constants;
import com.zombie.chatglm.data.types.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin("${app.config.cross-origin}")
@RequestMapping("/api/${app.config.api-version}/sale/")
public class SalesController {

    @Value("${alipay.alipay_public_key}")
    private String alipayPublicKey;

    @Resource
    private IOrderService orderService;

    @Resource
    private IAuthService authService;
    @Resource
    private EventBus eventBus;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    /**
     * 商品列表查询
     * 开始地址：http://localhost:8091/api/v1/sale/query_product_list
     * 测试地址：http://apix.natapp1.cc/api/v1/sale/query_product_list
     * <p>
     * curl -X GET \
     * -H "Authorization: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJveGZBOXc4LTI..." \
     * -H "Content-Type: application/x-www-form-urlencoded" \
     * http://localhost:8091/api/v1/sale/query_product_list
     */
    @GetMapping("query_product_list")
    public Response<List<SaleProductDTO>> queryProductList(@RequestHeader("Authorization") String token){

        try{
            //1.token校验
            boolean success = authService.checkToken(token);
            if(!success){
                return Response.<List<SaleProductDTO>>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }
            //2.查询商品
            List<ProductEntity> productEntityList = orderService.queryProductList();
            log.info("商品查询 {}", JSON.toJSONString(productEntityList));

            List<SaleProductDTO> mallProductDTOS = new ArrayList<>();
            for (ProductEntity productEntity : productEntityList) {
                SaleProductDTO saleProductDTO = SaleProductDTO.builder()
                        .productDesc(productEntity.getProductDesc())
                        .productName(productEntity.getProductName())
                        .productId(productEntity.getProductId())
                        .price(productEntity.getPrice())
                        .quota(productEntity.getQuota())
                        .build();
                mallProductDTOS.add(saleProductDTO);
            }

            //3.返回结果
            return Response.<List<SaleProductDTO>>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(mallProductDTOS)
                    .build();
        }catch (Exception e){
            log.error("商品查询失败", e);
            return Response.<List<SaleProductDTO>>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }


    /**
     * 用户商品下单
     * 开始地址：http://localhost:8091/api/v1/sale/create_pay_order?productId=
     * 测试地址：http://apix.natapp1.cc/api/v1/sale/create_pay_order
     * <p>
     * curl -X POST \
     * -H "Authorization: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJveGZBOXc4LTI..." \
     * -H "Content-Type: application/x-www-form-urlencoded" \
     * -d "productId=1001" \
     * http://localhost:8091/api/v1/sale/create_pay_order
     */
    @RequestMapping(value = "create_pay_order", method = RequestMethod.POST)
    public Response<String> createParOrder(@RequestHeader("Authorization") String token, @RequestParam Integer productId) {
        try {
            //1.token校验
            boolean success = authService.checkToken(token);
            if(!success){
                return Response.<String>builder()
                        .code(Constants.ResponseCode.TOKEN_ERROR.getCode())
                        .info(Constants.ResponseCode.TOKEN_ERROR.getInfo())
                        .build();
            }


            //2.token解析
            String openid = authService.openid(token);
            assert null != openid;
            log.info("用户商品下单，根据商品ID创建支付单开始 openid:{} productId:{}", openid, productId);

            ShopCartEntity shopCartEntity = ShopCartEntity.builder()
                    .openid(openid)
                    .productId(productId)
                    .build();

            PayOrderEntity payOrder = orderService.createOrder(shopCartEntity);
            log.info("用户商品下单，根据商品ID创建支付单完成 openid: {} productId: {} orderPay: {}", openid, productId, payOrder.toString());


            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(payOrder.getPayUrl())
                    .build();

        }catch (Exception e){
            log.error("用户商品下单，根据商品ID创建支付单失败", e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }

    }


    /**
     * 支付回调
     * 开发地址：http:/localhost:8091/api/v1/sale/pay_notify
     * 测试地址：http://apix.natapp1.cc/api/v1/sale/pay_notify
     * 线上地址：https://你的域名/api/v1/sale/pay_notify
     */
    @PostMapping("pay_notify")
    public void payNotify(HttpServletRequest request){
        try{
            log.info("支付回调，消息接收 {}", request.getParameter("trade_status"));
            if(request.getParameter("trade_status").equals("TRADE_SUCCESS")){
                Map<String, String> params = new HashMap<>();
                Map<String, String[]> requestParams = request.getParameterMap();
                for (String name : requestParams.keySet()) {
                    params.put(name, request.getParameter(name));
                }
                String sign = params.get("sign");
                String content = AlipaySignature.getSignCheckContentV1(params);
                //验签
                boolean checkSignature = AlipaySignature.rsa256CheckContent(content, sign, alipayPublicKey, "UTF-8");
                if(!checkSignature){
                    log.error("支付宝沙箱验签失败");
                    return;
                }

                //验签通过
                String orderId = params.get("out_trade_no");
                String transactionId = params.get("trade_no");
                String payAmount = params.get("buyer_pay_amount");
                String payTime = params.get("gmt_payment");
                log.info("支付成功 orderId:{} payAmount:{} payTime: {}", orderId, payAmount, payTime);

                // 更新订单为已支付
                boolean isSuccess = orderService.changeOrderPaySuccess(orderId,transactionId,new BigDecimal(payAmount),dateFormat.parse(payTime));
                if(isSuccess){
                    //发布消息 触发发货任务
                    eventBus.post(orderId);
                }
            }
        }catch (Exception e){
            log.error("支付回调，处理失败", e);
        }
    }
}
