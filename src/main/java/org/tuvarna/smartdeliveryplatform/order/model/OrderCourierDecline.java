package org.tuvarna.smartdeliveryplatform.order.model;

import jakarta.persistence.*;
import lombok.*;
import org.tuvarna.smartdeliveryplatform.courier.model.Courier;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "order_courier_declines",
        uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "courier_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCourierDecline {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", nullable = false)
    private Courier courier;

    @Column(name = "declined_at", nullable = false)
    private LocalDateTime declinedAt;
}
