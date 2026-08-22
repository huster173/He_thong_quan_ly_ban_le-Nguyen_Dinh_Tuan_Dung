package com.shop.web;

import com.shop.dto.Invoice;
import com.shop.dto.OrderRequest;
import com.shop.dto.PromotionRequest;
import com.shop.dto.PromotionResult;
import com.shop.dto.SubtotalRequest;
import com.shop.dto.SubtotalResult;
import com.shop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(
            summary = "PHAN 1 -- Tinh tong tien don hang",
            description = "Tra ve chi tiet tung dong hang va subtotal. Khong doc va khong sua ton kho.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "subtotal = tong cac lineTotal"),
            @ApiResponse(responseCode = "400", description = "EMPTY_ORDER / INVALID_PRODUCT_ID / INVALID_QUANTITY"),
            @ApiResponse(responseCode = "404", description = "PRODUCT_NOT_FOUND")})
    @PostMapping("/subtotal")
    public SubtotalResult subtotal(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(examples = {
                    @ExampleObject(name = "De bai -- ky vong subtotal 1.190.000", value = """
                            {"items": [{"productId": "P01", "quantity": 2},
                                       {"productId": "P03", "quantity": 1}]}"""),
                    @ExampleObject(name = "Gop dong trung productId -- P01 x2 + x1 ra MOT dong x3", value = """
                            {"items": [{"productId": "P01", "quantity": 2},
                                       {"productId": "P01", "quantity": 1}]}"""),
                    @ExampleObject(name = "Loi 400 -- quantity < 1", value = """
                            {"items": [{"productId": "P01", "quantity": 0}]}"""),
                    @ExampleObject(name = "Loi 404 -- san pham khong ton tai", value = """
                            {"items": [{"productId": "P99", "quantity": 1}]}""")}))
            @Valid @RequestBody SubtotalRequest request) {
        return orderService.subtotal(request.items());
    }

    @Operation(
            summary = "PHAN 2 -- Ap dung khuyen mai",
            description = """
                    Giam gia theo so luong TRUOC, coupon tinh tren phan con lai. `total` la tong \
                    truoc thue.

                    `couponCode` nhan 4 gia tri: bo trong (server tu tim ma dau tien du dieu kien), \
                    `NONE` (khach tu choi voucher), `SALE50K`, `SALE10PT`.

                    Ma hop le nhung don chua du dieu kien KHONG phai loi: van tra 200 voi \
                    `couponCode: null` va `couponDiscount: 0`.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "total = subtotal - categoryDiscount - couponDiscount"),
            @ApiResponse(responseCode = "400", description = "INVALID_COUPON neu ma khong ton tai"),
            @ApiResponse(responseCode = "404", description = "PRODUCT_NOT_FOUND")})
    @PostMapping("/promotion")
    public PromotionResult promotion(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(examples = {
                    @ExampleObject(name = "De bai -- SALE50K, ky vong total 1.140.000", value = """
                            {"items": [{"productId": "P01", "quantity": 2},
                                       {"productId": "P03", "quantity": 1}],
                             "couponCode": "SALE50K"}"""),
                    @ExampleObject(name = "Giam 10% theo category -- 3 SP clothing, ky vong total 355.000", value = """
                            {"items": [{"productId": "P01", "quantity": 3}],
                             "couponCode": "SALE50K"}"""),
                    @ExampleObject(name = "Bo trong couponCode -- server tu chon ma tot nhat", value = """
                            {"items": [{"productId": "P01", "quantity": 3}]}"""),
                    @ExampleObject(name = "NONE -- khach chu dong tu choi voucher", value = """
                            {"items": [{"productId": "P01", "quantity": 3}],
                             "couponCode": "NONE"}"""),
                    @ExampleObject(name = "SALE10PT tren don nho -- khong co nguong, ky vong 135.000", value = """
                            {"items": [{"productId": "P01", "quantity": 1}],
                             "couponCode": "SALE10PT"}"""),
                    @ExampleObject(name = "Loi 400 -- ma giam gia khong ton tai", value = """
                            {"items": [{"productId": "P01", "quantity": 1}],
                             "couponCode": "SALE99K"}""")}))
            @Valid @RequestBody PromotionRequest request) {
        return orderService.promotion(request.items(), request.couponCode());
    }

    @Operation(
            summary = "PHAN 3 -- Tru kho an toan & hoa don cuoi cung",
            description = """
                    Tinh LAI toan bo khuyen mai tu dau (khong tin so lieu Phan 2 ma client dang \
                    hien thi), cong thue theo khu vuc, roi tru kho va luu lich su don trong MOT \
                    giao dich.

                    Thue suat: `noi_thanh` 8%, `ngoai_thanh` 10%, `tinh_khac` 12%. Bo trong \
                    `region` thi mac dinh `noi_thanh`.

                    Loi o bat ky buoc nao cung khong lam thay doi ton kho.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "finalTotal = total + taxAmount, kem orderId da chot"),
            @ApiResponse(responseCode = "400", description = "INVALID_COUPON / INVALID_REGION / INVALID_QUANTITY"),
            @ApiResponse(responseCode = "404", description = "PRODUCT_NOT_FOUND"),
            @ApiResponse(responseCode = "409", description = "OUT_OF_STOCK -- kho khong du tai thoi diem xac nhan")})
    @PostMapping("/confirm")
    public Invoice confirm(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(examples = {
                    @ExampleObject(name = "De bai -- noi thanh 8%, ky vong finalTotal 1.231.200", value = """
                            {"items": [{"productId": "P01", "quantity": 2},
                                       {"productId": "P03", "quantity": 1}],
                             "couponCode": "SALE50K",
                             "region": "noi_thanh"}"""),
                    @ExampleObject(name = "Ngoai thanh 10% -- ky vong finalTotal 1.254.000", value = """
                            {"items": [{"productId": "P01", "quantity": 2},
                                       {"productId": "P03", "quantity": 1}],
                             "couponCode": "SALE50K",
                             "region": "ngoai_thanh"}"""),
                    @ExampleObject(name = "Tinh khac 12% -- ky vong finalTotal 1.276.800", value = """
                            {"items": [{"productId": "P01", "quantity": 2},
                                       {"productId": "P03", "quantity": 1}],
                             "couponCode": "SALE50K",
                             "region": "tinh_khac"}"""),
                    @ExampleObject(name = "Loi 409 -- P03 chi con 5 trong kho, don xin 6", value = """
                            {"items": [{"productId": "P03", "quantity": 6}],
                             "couponCode": "NONE",
                             "region": "noi_thanh"}"""),
                    @ExampleObject(name = "Loi 400 -- khu vuc khong hop le", value = """
                            {"items": [{"productId": "P01", "quantity": 1}],
                             "region": "sao_hoa"}""")}))
            @Valid @RequestBody OrderRequest request) {
        return orderService.confirm(request);
    }
}
