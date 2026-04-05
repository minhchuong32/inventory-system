package com.system.inventorysystem.factory;

import com.system.inventorysystem.dto.SupplierDTO;
import com.system.inventorysystem.entity.Supplier;
import com.system.inventorysystem.enums.SupplierType;
import com.system.inventorysystem.strategy.SupplierClassificationFactory;
import com.system.inventorysystem.strategy.SupplierClassificationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupplierFactory implements EntityFactory<Supplier, SupplierDTO> {

    private final SupplierClassificationFactory classificationFactory;

    public Supplier updateFromEntity(Supplier existing, Supplier incoming) {
        existing.setName(incoming.getName());
        existing.setContactPerson(incoming.getContactPerson());
        existing.setPhone(incoming.getPhone());
        existing.setEmail(incoming.getEmail());
        existing.setAddress(incoming.getAddress());
        existing.setTaxCode(incoming.getTaxCode());
        existing.setBankAccount(incoming.getBankAccount());
        existing.setBankName(incoming.getBankName());

        boolean typeChanged = incoming.getSupplierType() != existing.getSupplierType();

        if (incoming.getSupplierType() != null) {
            existing.setSupplierType(incoming.getSupplierType());
        }

        if (typeChanged) {
            // Đổi loại => Ép buộc Strategy tính lại, bỏ qua con số người dùng gửi lên
            SupplierClassificationStrategy strategy = classificationFactory.getStrategy(existing.getSupplierType());
            existing.setCreditLimit(strategy.calculateCreditLimit(existing));
        } else if (incoming.getCreditLimit() != null) {
            existing.setCreditLimit(incoming.getCreditLimit());
        } else {
            SupplierClassificationStrategy strategy = classificationFactory.getStrategy(existing.getSupplierType());
            existing.setCreditLimit(strategy.calculateCreditLimit(existing));
        }

        existing.setStatus(incoming.getStatus());

        return existing;
    }

    @Override
    public SupplierDTO toDTO(Supplier supplier) {
        SupplierType type = supplier.getSupplierType() != null
                ? supplier.getSupplierType()
                : SupplierType.NEW;

        SupplierClassificationStrategy strategy = classificationFactory.getStrategy(type);

        return SupplierDTO.builder()
                .id(supplier.getId())
                .code(supplier.getCode())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .taxCode(supplier.getTaxCode())
                .bankAccount(supplier.getBankAccount())
                .bankName(supplier.getBankName())
                .creditLimit(supplier.getCreditLimit())
                .currentDebt(supplier.getCurrentDebt())
                .remainingCredit(supplier.getRemainingCredit())
                .status(supplier.getStatus())
                .supplierType(type.name())
                .supplierTypeDisplayName(type.getDisplayName())
                .discountPercentage(strategy.getDiscountPercentage())
                .classificationDescription(strategy.getDescription())
                .build();
    }
}
