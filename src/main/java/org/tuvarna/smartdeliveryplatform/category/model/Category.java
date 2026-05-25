package org.tuvarna.smartdeliveryplatform.category.model;

import jakarta.persistence.*;
import lombok.*;
import org.tuvarna.smartdeliveryplatform.merchant.model.Merchant;
import org.tuvarna.smartdeliveryplatform.shared.enums.MerchantType;

import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MerchantType type;

    @Column(nullable = false)
    private Boolean isGlobal;

    @Column(nullable = false)
    private Boolean isDeleted;
}