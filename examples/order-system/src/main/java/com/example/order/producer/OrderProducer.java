package com.example.order.producer;

import com.example.order.domain.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 주문 관련 이벤트를 Kafka 토픽으로 발행하는 Producer 클래스입니다.
 */
@Component
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);
    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 주문 이벤트를 Kafka의 'order-events' 토픽으로 발행합니다.
     * <p>
     * 이 메서드의 핵심은 {@code event.orderId()}를 메시지 Key로 사용하는 것입니다.
     * Kafka는 동일한 Key를 가진 메시지를 항상 동일한 파티션으로 전송하는 것을 보장합니다.
     * 이를 통해 '주문 생성' -> '결제 완료' -> '배송 시작' 등 특정 주문에 대한
     * 이벤트들이 Consumer에서 순서대로 처리되는 것을 보장할 수 있습니다.
     *
     * @param event 발행할 주문 이벤트 객체
     */
    public void publish(OrderEvent event) {
        log.info("이벤트 발행 시도 - OrderId: {}, Status: {}", event.orderId(), event.status());

        // orderId를 Key로 사용하여 메시지를 비동기적으로 전송합니다.
        kafkaTemplate.send(TOPIC, event.orderId(), event)
                .whenComplete((result, ex) -> {
                    // 전송 결과를 비동기 콜백으로 처리합니다.
                    if (ex == null) {
                        // 성공 시, 어떤 파티션과 오프셋에 저장되었는지 로그를 남깁니다.
                        log.info("발행 성공 - Topic: {}, Partition: {}, Offset: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        // 실패 시, 에러 로그를 남겨 추적할 수 있도록 합니다.
                        log.error("발행 실패 - OrderId: {}, Status: {}", event.orderId(), event.status(), ex);
                    }
                });
    }
}
