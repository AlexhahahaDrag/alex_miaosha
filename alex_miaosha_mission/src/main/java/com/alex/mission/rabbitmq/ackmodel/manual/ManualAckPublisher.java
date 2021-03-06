package com.alex.mission.rabbitmq.ackmodel.manual;

import cn.hutool.json.JSONUtil;
import com.alex.common.obj.SeckillMessage;
import com.alex.mission.rabbitmq.constants.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ManualAckPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void sendMsg(SeckillMessage seckillMessage) {
        if (seckillMessage != null) {
            try {
                rabbitTemplate.setExchange(RabbitMQConstants.MANUAL_ACKNOWLEDGE_EXCHANGE);
                rabbitTemplate.setRoutingKey(RabbitMQConstants.MANUAL_ACKNOWLEDGE_ROUTING_KEY);
                rabbitTemplate.convertAndSend(seckillMessage);
                log.info("确认消费模式为手动确认机制-消息模型-生产者-发送消息：{}", JSONUtil.toJsonStr(seckillMessage));
            } catch (Exception e) {
                log.error("确认消费模式为手动确认机制-消息模型-生产者-发送消息：{}，错误信息：{}", JSONUtil.toJsonStr(seckillMessage), e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
