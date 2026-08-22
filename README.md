# Hệ thống Đơn hàng & Kho hàng

## Build & run

Yêu cầu: JDK 21+, Maven 3.9+, Node.js 18+ và Docker.

Khởi động Oracle (chỉ cần làm một lần). `APP_USER` tạo sẵn schema riêng cho ứng
dụng — bảng của app không nằm lẫn trong `SYSTEM`, và app không cần quyền DBA:

```bash
docker run -d --name oracle-db -p 1521:1521 \
  -e ORACLE_PASSWORD=oracle \
  -e APP_USER=shop -e APP_USER_PASSWORD=shop \
  -v oracle-data:/opt/oracle/oradata \
  gvenzl/oracle-free:latest
```

Nếu container đã được tạo từ trước mà chưa có schema này, tạo tay một lần:

```bash
docker exec -i oracle-db sqlplus -s system/oracle@//localhost:1521/FREEPDB1 <<'SQL'
CREATE USER shop IDENTIFIED BY shop;
GRANT CONNECT, RESOURCE TO shop;
ALTER USER shop QUOTA UNLIMITED ON USERS;
exit;
SQL
```

Backend tự tạo bảng và nạp dữ liệu mẫu trong lần chạy đầu. Thông tin kết nối mặc
định nằm ở `application.properties`, đổi được qua biến môi trường `DB_URL`,
`DB_USERNAME`, `DB_PASSWORD`.

Chờ Oracle khởi động xong, rồi mở hai terminal.

```bash
# Terminal 1 — backend, http://localhost:8080
cd backend
mvn spring-boot:run
```

```bash
# Terminal 2 — frontend, http://localhost:5173
cd frontend
npm install
npm run dev
```

Mở http://localhost:5173.

## Ba API theo đề bài

Mỗi phần nhận đúng dữ liệu nó cần — body của phần sau là body phần trước cộng
thêm một trường:

| API | Body |
| --- | --- |
| Phần 1 — `/subtotal` | `{ items }` |
| Phần 2 — `/promotion` | `{ items, couponCode }` |
| Phần 3 — `/confirm` | `{ items, couponCode, region }` |

với `items = [{ productId, quantity }]` đúng như đầu vào đề bài mô tả.

`couponCode` nhận 4 giá trị:

| Giá trị | Ý nghĩa |
| --- | --- |
| `null` / bỏ trống | Chưa chọn gì — server tự tìm **mã đầu tiên đủ điều kiện** theo thứ tự SALE50K → SALE10PT |
| `"NONE"` | Khách **chủ động từ chối** voucher — không tìm mã nào cả |
| `"SALE50K"` | Dùng đúng mã này (không đủ điều kiện thì trả `couponCode: null`, không phải lỗi) |
| `"SALE10PT"` | Dùng đúng mã này |

`null` và `"NONE"` **khác nhau**: bỏ trống nghĩa là "chưa chọn, cho tôi mã tốt
nhất", còn `"NONE"` là "tôi không muốn dùng voucher". Việc tự tìm mã chạy **sau**
khi đã trừ giảm giá theo số lượng, vì điều kiện ≥ 300.000đ của SALE50K xét trên
số tiền còn lại chứ không phải subtotal gốc.

Giao diện gọi Phần 2 không kèm mã khi khách vào bước thanh toán, rồi hiển thị
đúng mã server đã chọn; mỗi lần khách đổi voucher là một lần gọi lại chính API đó
với mã cụ thể.

### PHẦN 1 — `POST /api/orders/subtotal`

```bash
curl -s localhost:8080/api/orders/subtotal -H "Content-Type: application/json" -d '{
  "items": [{"productId":"P01","quantity":2},{"productId":"P03","quantity":1}]
}'
```

```json
{
  "lines": [
    { "productId": "P01", "name": "Ao thun",  "unitPrice": 150000, "quantity": 2, "lineTotal": 300000 },
    { "productId": "P03", "name": "Tai nghe", "unitPrice": 890000, "quantity": 1, "lineTotal": 890000 }
  ],
  "subtotal": 1190000
}
```

### PHẦN 2 — `POST /api/orders/promotion`

```bash
curl -s localhost:8080/api/orders/promotion -H "Content-Type: application/json" -d '{
  "items": [{"productId":"P01","quantity":2},{"productId":"P03","quantity":1}],
  "couponCode": "SALE50K"
}'
```

```json
{
  "lines": [ ... như Phần 1 ... ],
  "subtotal": 1190000,
  "categoryDiscount": 0,
  "couponCode": "SALE50K",
  "couponDiscount": 50000,
  "total": 1140000
}
```

`total` là tổng **trước thuế**. Thứ tự áp dụng: giảm theo số lượng trước, coupon
tính trên phần còn lại, tổng cuối không bao giờ âm.

### PHẦN 3 — `POST /api/orders/confirm`

```bash
curl -s localhost:8080/api/orders/confirm -H "Content-Type: application/json" -d '{
  "items": [{"productId":"P01","quantity":2},{"productId":"P03","quantity":1}],
  "couponCode": "SALE50K",
  "region": "noi_thanh"
}'
```

```json
{
  "orderId": "DH-000001",
  "createdAt": "2026-08-22T13:07:11",
  "... như Phần 2 ...": null,
  "region": "noi_thanh",
  "taxRate": 0.08,
  "taxAmount": 91200,
  "finalTotal": 1231200,
  "stockUpdated": true
}
```

Bước này tính lại toàn bộ khuyến mãi từ đầu (không tin số liệu Phần 2 mà client
đang hiển thị), rồi trừ kho trong một giao dịch. Lỗi ở bất kỳ bước nào cũng
không làm thay đổi tồn kho.

Lỗi trả về cùng một dạng `{ code, message, details }`:

| Mã | HTTP | Khi nào |
| --- | --- | --- |
| `EMPTY_ORDER` | 400 | đơn không có sản phẩm nào |
| `INVALID_PRODUCT_ID` | 400 | thiếu `productId` trong body |
| `INVALID_QUANTITY` | 400 | `quantity < 1` |
| `INVALID_COUPON` | 400 | mã giảm giá không tồn tại |
| `INVALID_REGION` | 400 | khu vực không hợp lệ |
| `INVALID_PAGE` | 400 | `page < 0` hoặc `size` ngoài khoảng 1..100 |
| `INVALID_PARAMETER` | 400 | query param sai kiểu, ví dụ `?page=abc` |
| `PRODUCT_NOT_FOUND` | 404 | `productId` không có trong kho hàng |
| `ORDER_NOT_FOUND` | 404 | không tìm thấy đơn theo `orderId` |
| `OUT_OF_STOCK` | 409 | đơn hợp lệ nhưng kho không đủ tại thời điểm xác nhận |
| `INTERNAL_ERROR` | 500 | lỗi ngoài dự kiến; chi tiết chỉ ghi vào log |

Mã giảm giá không đủ điều kiện **không phải lỗi**: `/promotion` vẫn trả 200 với
`couponCode: null` và `couponDiscount: 0`.

## Quản trị — lịch sử đơn hàng

Ngoài đề bài. Bấm **"Lịch sử đơn hàng"** ở góc trên bên phải giao diện.

`GET /api/admin/orders?page=0&size=10` — `page` đếm từ 0, `size` trong khoảng
1..100, đơn mới nhất lên đầu.

```json
{
  "items": [
    { "orderId": "DH-000006", "createdAt": "2026-08-22T14:23:06",
      "itemCount": 1, "region": "noi_thanh", "couponCode": "SALE10PT",
      "finalTotal": 145800 }
  ],
  "page": 0, "size": 10, "totalItems": 6, "totalPages": 1
}
```

Phân trang chạy dưới database bằng `OFFSET ? ROWS FETCH NEXT ? ROWS ONLY`, không
kéo cả bảng lên rồi cắt ở Java. Trang vượt quá dữ liệu trả về danh sách rỗng
(không phải lỗi); `page`/`size` sai kiểu hoặc ngoài khoảng thì trả 400.

`GET /api/admin/orders/{orderId}` trả hoá đơn đầy đủ kèm các dòng hàng, hoặc 404
`ORDER_NOT_FOUND`.

Nhóm `/api/admin` chỉ đọc, không có endpoint nào sửa hay xoá dữ liệu: một đơn đã
chốt là bản ghi lịch sử.

## Ghi chú kỹ thuật

**Tiền tệ tính bằng `long`, đơn vị 1 VND.** VND không có đơn vị nhỏ hơn 1 đồng
nên mọi số tiền đều là số nguyên. Tỉ lệ khai báo bằng phần trăm nguyên (`8` = 8%),
luôn nhân trước chia sau nên không mất chính xác ở bước trung gian, và **làm tròn
nửa lên** về đồng gần nhất: `(amount * 8 + 50) / 100`. Làm tròn thay vì cắt đuôi
để tổng tiền không bị lệch dần xuống qua nhiều dòng hàng. Không dùng `double` ở
bất kỳ phép tính tiền nào. Xem `backend/.../util/Money.java`.

**Khuyến mãi và thuế đều dùng Strategy pattern.**

Mỗi điểm mở rộng là một package: interface + Context ở ngoài, các cài đặt nằm
trong `strategy/`.

```
com.shop.promotion/          com.shop.tax/
    CouponStrategy.java          TaxStrategy.java
    CouponContext.java           TaxContext.java
    strategy/                    strategy/
        Sale50kCoupon.java           NoiThanhTax.java
        Sale10PercentCoupon.java     NgoaiThanhTax.java
                                     TinhKhacTax.java
```

Cả hai Context đều nhận `List<...Strategy>` do Spring tiêm sẵn, nên thêm mã giảm
giá hoặc khu vực mới chỉ cần thêm một `@Component` vào `strategy/` — không sửa
`PromotionService` hay `OrderService`, và không có `switch`/`if` nào phải mở ra.
Thứ tự ưu tiên khi server tự chọn mã giảm giá lấy từ `@Order` trên từng chiến lược.

`Region` vì vậy chỉ còn là định danh khu vực; thuế suất thuộc về `TaxStrategy`.
Nếu sau này một khu vực tính thuế khác kiểu (miễn thuế dưới một ngưỡng, thuế bậc
thang…), chỉ riêng chiến lược đó ghi đè `taxFor`.
