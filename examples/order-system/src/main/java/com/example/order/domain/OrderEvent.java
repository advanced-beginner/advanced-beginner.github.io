package com.example.order.domain;

import java.time.LocalDateTime;

/**
 * 주문 시스템의 모든 상태 변경을 나타내는 이벤트 객체입니다.
 * Java 17의 record를 사용하여 불변(immutable) 데이터 객체를 간결하게 정의합니다.
 * 이 객체는 Kafka 메시지의 값(value)으로 직렬화되어 전송됩니다.
 *
 * @param orderId     이벤트의 대상이 되는 주문의 고유 ID. Kafka 메시지의 Key로 사용됩니다.
 * @param customerId  주문을 생성한 고객의 ID.
 * @param status      이벤트가 발생했을 때의 주문 상태.
 * @param description 이벤트에 대한 사람이 읽을 수 있는 설명.
 * @param timestamp   이벤트가 발생한 시간.
 */
public record OrderEvent(
    String orderId,
    String customerId,
    OrderStatus status,
    String description,
    LocalDateTime timestamp
) {
    /**
     * '주문 생성' 이벤트를 생성합니다.
     * @param orderId 새로 생성된 주문의 ID
     * @param customerId 주문한 고객의 ID
     * @return OrderStatus가 CREATED로 설정된 새로운 OrderEvent 객체
     */
    public static OrderEvent created(String orderId, String customerId) {
        return new OrderEvent(orderId, customerId, OrderStatus.CREATED,
                "주문이 생성되었습니다", LocalDateTime.now());
    }

    /**
     * '결제 완료' 이벤트를 생성합니다.
     * @param orderId 결제가 완료된 주문의 ID
     * @param customerId 주문한 고객의 ID
     * @return OrderStatus가 PAID로 설정된 새로운 OrderEvent 객체
     */
    public static OrderEvent paid(String orderId, String customerId) {
        return new OrderEvent(orderId, customerId, OrderStatus.PAID,
                "결제가 완료되었습니다", LocalDateTime.now());
    }

    /**
     * '배송 시작' 이벤트를 생성합니다.
     * @param orderId 배송이 시작된 주문의 ID
     * @param customerId 주문한 고객의 ID
     * @return OrderStatus가 SHIPPED로 설정된 새로운 OrderEvent 객체
     */
    public static OrderEvent shipped(String orderId, String customerId) {
        return new OrderEvent(orderId, customerId, OrderStatus.SHIPPED,
                "배송이 시작되었습니다", LocalDateTime.now());
    }

    /**
     * '배송 완료' 이벤트를 생성합니다.
     * @param orderId 배송이 완료된 주문의 ID
     * @param customerId 주문한 고객의 ID
     * @return OrderStatus가 DELIVERED로 설정된 새로운 OrderEvent 객체
     */
    public static OrderEvent delivered(String orderId, String customerId) {
        return new OrderEvent(orderId, customerId, OrderStatus.DELIVERED,
                "배송이 완료되었습니다", LocalDateTime.now());
    }

    /**
     * '주문 취소' 이벤트를 생성합니다.
     * @param orderId 취소된 주문의 ID
     * @param customerId 주문한 고객의 ID
     * @param reason 취소 사유
     * @return OrderStatus가 CANCELLED로 설정된 새로운 OrderEvent 객체
     */
    public static OrderEvent cancelled(String orderId, String customerId, String reason) {
        return new OrderEvent(orderId, customerId, OrderStatus.CANCELLED,
                "주문이 취소되었습니다: " + reason, LocalDateTime.now());
    }
}
