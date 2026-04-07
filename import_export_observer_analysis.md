# 🔍 Phân tích Logic Import/Export & Observer Pattern

---

## 1. Luồng xử lý Phiếu Nhập Kho (Import)

```
Controller → ImportServiceImpl.save()  →  [Lưu phiếu trạng thái PENDING]
           → ImportServiceImpl.complete(id)
                   │
                   ├─ 1. Tìm phiếu (findByIdWithDetails)
                   ├─ 2. order.complete()  →  status: PENDING → COMPLETED
                   ├─ 3. importOrderRepository.save(order)
                   └─ 4. notifyListeners(OrderEvent{IMPORT_COMPLETED, "IMPORT"})
                               │
                               ├─ StockUpdateListener  → tăng tồn kho + ghi StockMovement (IN)
                               └─ SupplierDebtListener → cập nhật công nợ nhà cung cấp
```

### Chi tiết `save()` (Import)
- Tự sinh mã `PN%06d` (PN000001, PN000002...)
- Gán ngày tạo = `LocalDate.now()`
- Link từng `ImportDetail` → `ImportOrder` cha
- Gọi `detail.calculateTotal()` và `order.calculateTotal()` để tính tiền

### Chi tiết `complete()` (Import)
```java
order.complete();                   // check trạng thái, ném BusinessException nếu đã FINAL
ImportOrder completed = repo.save(order);
notifyListeners(OrderEvent.builder()
    .orderId(completed.getId())
    .eventType(EventType.IMPORT_COMPLETED)
    .orderType("IMPORT")
    .build());
```

### Chi tiết `cancel()` (Import)
```java
order.cancel();                     // status → CANCELLED
importOrderRepository.save(order);
notifyListeners(OrderEvent{ORDER_CANCELLED, "IMPORT"});
// StockCancelListener nhận → trừ lại kho + ghi StockMovement(OUT, lý do CANCEL_IMPORT)
```

---

## 2. Luồng xử lý Phiếu Xuất Kho (Export)

```
Controller → ExportServiceImpl.save()
                   │
                   ├─ Link ExportDetail → ExportOrder
                   ├─ calculateTotal()
                   └─ Strategy Pattern: DiscountStrategyFactory.getStrategy(customerType)
                        → tính discount theo loại KH (RETAIL/WHOLESALE/VIP)
                        → recalculateFinalAmount()

           → ExportServiceImpl.complete(id)
                   │
                   ├─ 1. Validate tồn kho từng dòng:
                   │       product.canExport(qty) → ném InsufficientStockException nếu không đủ
                   ├─ 2. order.complete()
                   ├─ 3. exportOrderRepository.save(order)
                   └─ 4. notifyListeners(OrderEvent{EXPORT_COMPLETED, "EXPORT"})
                               │
                               ├─ StockUpdateListener   → giảm tồn kho + ghi StockMovement (OUT)
                               └─ LowStockNotifyListener → quét kho, log cảnh báo hàng sắp hết
```

---

## 3. Observer Pattern — Cấu trúc triển khai

### Interface tầng trên
```
OrderEventPublisher  (interface)
  + subscribe(listener)
  + unsubscribe(listener)
  + notifyListeners(event)

OrderEventListener   (interface)
  + onOrderEvent(event)
  + supports(event)     ← filter — chỉ xử lý event mình quan tâm
```

### Abstract Publisher
```
AbstractOrderPublisher  implements OrderEventPublisher
  - List<OrderEventListener> listeners   ← danh sách subscriber
  + subscribe()   → thêm nếu chưa có
  + unsubscribe() → xóa
  + notifyListeners(event):
      for each listener:
          if listener.supports(event) → listener.onOrderEvent(event)
          (bắt exception riêng từng listener, không dừng cả chain)
```

### Service kế thừa Publisher
```
ImportServiceImpl  extends AbstractOrderPublisher
ExportServiceImpl  extends AbstractOrderPublisher
```

Spring tự động inject `List<OrderEventListener>` qua constructor,  
service gọi `allListeners.forEach(this::subscribe)` → đăng ký toàn bộ.

---

## 4. Bảng các Listener

| Listener | Lắng nghe event | Hành động |
|---|---|---|
| `StockUpdateListener` | `IMPORT_COMPLETED`, `EXPORT_COMPLETED` | Tăng/Giảm `product.quantity` + ghi `StockMovement` |
| `StockCancelListener` | `ORDER_CANCELLED` | Hoàn kho ngược lại + ghi `StockMovement` |
| `LowStockNotifyListener` | `EXPORT_COMPLETED` | Quét sản phẩm dưới `minQuantity`, log cảnh báo |
| `SupplierDebtListener` | `IMPORT_COMPLETED` | Cộng công nợ NCC theo `finalAmount` của phiếu nhập |

---

## 5. Sơ đồ luồng Observer khi xác nhận phiếu

```
ImportServiceImpl.complete(id)
        │
        └─► notifyListeners(IMPORT_COMPLETED)
                    │
                    ├─► StockUpdateListener.supports() ✅ → onOrderEvent()
                    │       └─ increaseStock() + recordMovement(IN)
                    │
                    ├─► StockCancelListener.supports() ❌ (bỏ qua)
                    │
                    ├─► LowStockNotifyListener.supports() ❌ (chỉ nghe EXPORT)
                    │
                    └─► SupplierDebtListener.supports() ✅ → onOrderEvent()
                            └─ supplier.updateDebt(finalAmount)
```

---

## 6. State Machine của phiếu (AbstractOrder)

```
              save()
[NEW] ──────────────────► PENDING
                              │
              complete()      │         cancel()
                    ┌─────────┘──────────────────┐
                    ▼                              ▼
               COMPLETED                      CANCELLED
             (trạng thái FINAL)            (trạng thái FINAL)
                    │                              │
            Không thể hủy                  Không thể complete
            BusinessException              BusinessException
```

**`isFinal()`** được check trong cả `complete()` lẫn `cancel()` — ngăn thao tác trên phiếu đã kết thúc.

---

## 7. Điểm thiết kế đáng chú ý

| Điểm | Giải thích |
|---|---|
| **Tách biệt nghiệp vụ kho** | Service không trực tiếp update tồn kho — giao cho Listener xử lý |
| **Dễ mở rộng** | Thêm tính năng mới (gửi email, push notification) chỉ cần thêm Listener mới, không sửa Service |
| **Error isolation** | `AbstractOrderPublisher` bắt exception từng listener riêng — listener lỗi không ảnh hưởng listener khác |
| **Strategy + Observer kết hợp** | Export dùng thêm `DiscountStrategy` để tính giảm giá trước khi save |
| **Spring DI tự động wiring** | `List<OrderEventListener>` được Spring inject đầy đủ → không cần đăng ký thủ công |
