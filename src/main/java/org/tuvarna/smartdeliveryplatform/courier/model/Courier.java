package org.tuvarna.smartdeliveryplatform.courier.model;

import jakarta.persistence.*;
import lombok.*;
import org.tuvarna.smartdeliveryplatform.user.model.User;

import java.util.UUID;

@Entity
@Table(name = "couriers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;

    @Column(name = "is_busy", nullable = false)
    private Boolean isBusy;

}
