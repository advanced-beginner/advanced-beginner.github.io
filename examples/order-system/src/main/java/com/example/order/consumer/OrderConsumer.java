package com.example.order.consumer;

import com.example.order.domain.OrderEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 'order-events' 토픽의 메시지를 구독하여 처리하는 Consumer 클래스입니다.
 * 주문의 상태 변경 이벤트를 수신하여 각 상태에 맞는 비즈니스 로직을 수행합니다.
 */
@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    /**
     * 'order-events' 토픽을 구독하는 리스너 메서드입니다.
     * 'order-processor' 그룹에 속하며, ConsumerRecord 전체를 수신하여
     * 메시지 값(payload) 외에 key, partition, offset 등 메타데이터를 함께 활용합니다.
     *
     * @param record Kafka로부터 수신한 전체 메시지 객체
     */
    @KafkaListener(topics = "order-events", groupId = "order-processor")
    public void consume(ConsumerRecord<String, OrderEvent> record) {
        // ConsumerRecord를 사용하면 이벤트 페이로드 외의 유용한 정보들을 얻을 수 있습니다.
        // record.key()는 Producer에서 설정한 orderId와 동일하며,
        // record.partition()과 record.offset()은 메시지의 정확한 위치를 나타내어
        // 문제 발생 시 추적에 매우 유용합니다.
        OrderEvent event = record.value();

        log.info("========================================");
        log.info("이벤트 수신");
        log.info("  Partition: {}, Offset: {}", record.partition(), record.offset());
        log.info("  Key (OrderId): {}", record.key());
        log.info("  Status: {}", event.status());
        log.info("  Description: {}", event.description());
        log.info("========================================");

        // 이벤트의 상태(status)에 따라 적절한 처리 메서드를 호출합니다.
        // 이는 이벤트 기반 아키텍처에서 상태 머신(State Machine)을 구현하는 일반적인 패턴입니다.
        processEvent(event);
    }

    /**
     * 이벤트의 상태에 따라 적절한 핸들러 메서드를 호출하는 라우팅 역할을 합니다.
     *
     * @param event 처리할 주문 이벤트
     */
    private void processEvent(OrderEvent event) {
        switch (event.status()) {
            case CREATED -> handleOrderCreated(event);
            case PAID -> handleOrderPaid(event);
            case SHIPPED -> handleOrderShipped(event);
            case DELIVERED -> handleOrderDelivered(event);
            case CANCELLED -> handleOrderCancelled(event);
            default -> log.warn("처리할 수 없는 상태의 이벤트 수신: {}", event.status());
        }
    }

    private void handleOrderCreated(OrderEvent event) {
        // TODO: 실제 비즈니스 로직 구현
        // 1. 데이터베이스에 주문 정보 저장 (상태: CREATED)
        // 2. 재고 서비스에 재고 확인 및 예약 요청
        // 3. 결제 서비스에 결제 요청 정보 전달
        log.info("[처리] 주문 생성 - OrderId: {}. 재고 확인 및 결제 대기", event.orderId());
    }

    private void handleOrderPaid(OrderEvent event) {
        // TODO: 실제 비즈니스 로직 구현
        // 1. 데이터베이스 주문 상태 업데이트 (상태: PAID)
        // 2. 배송 서비스에 배송 요청
        log.info("[처리] 결제 완료 - OrderId: {}. 배송 준비 시작", event.orderId());
    }

    private void handleOrderShipped(OrderEvent event) {
        // TODO: 실제 비즈니스 로직 구현
        // 1. 데이터베이스 주문 상태 업데이트 (상태: SHIPPED)
        // 2. 알림 서비스에 '배송 시작' 알림 요청
        log.info("[처리] 배송 시작 - OrderId: {}. 고객에게 알림 발송", event.orderId());
    }

    private void handleOrderDelivered(OrderEvent event) {
        // TODO: 실제 비즈니스 로직 구현
        // 1. 데이터베이스 주문 상태 업데이트 (상태: DELIVERED)
        // 2. 주문 프로세스 완료 처리
        log.info("[처리] 배송 완료 - OrderId: {}. 주문 완료 처리", event.orderId());
    }

    private void handleOrderCancelled(OrderEvent event) {
        // TODO: 실제 비즈니스 로직 구현
        // 1. 데이터베이스 주문 상태 업데이트 (상태: CANCELLED)
        // 2. 결제 서비스에 환불 요청
        // 3. 재고 서비스에 예약된 재고 복구 요청
        log.info("[처리] 주문 취소 - OrderId: {}. 환불 처리 시작", event.orderId());
    }
}
