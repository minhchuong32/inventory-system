package com.system.inventorysystem.entity;

import com.system.inventorysystem.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "export_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExportDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "export_order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ExportOrder exportOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "total_price", precision = 18, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        if (quantity != null && unitPrice != null) {
            BigDecimal disc = discountPercent != null ? discountPercent : BigDecimal.ZERO;
            BigDecimal multiplier = BigDecimal.ONE.subtract(disc.divide(BigDecimal.valueOf(100)));
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity)).multiply(multiplier).setScale(2,
                    RoundingMode.HALF_UP);
        }
    }

    @Transient
    public BigDecimal getDiscountAmount() {
        if (quantity == null || unitPrice == null || discountPercent == null)
            return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .multiply(discountPercent.divide(BigDecimal.valueOf(100)));
    }

    @Transient
    public BigDecimal getResolvedTotalPrice() {
        if (totalPrice != null) {
            return totalPrice;
        }
        if (quantity != null && unitPrice != null) {
            BigDecimal disc = discountPercent != null ? discountPercent : BigDecimal.ZERO;
            BigDecimal multiplier = BigDecimal.ONE.subtract(disc.divide(BigDecimal.valueOf(100)));
            return unitPrice.multiply(BigDecimal.valueOf(quantity)).multiply(multiplier).setScale(2,
                    RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && product != null && quantity != null && quantity > 0
                && unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) >= 0;
    }
}