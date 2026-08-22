package com.shop.web;

import com.shop.dto.Invoice;
import com.shop.dto.OrderSummary;
import com.shop.dto.PageResult;
import com.shop.service.OrderHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final OrderHistoryService orderHistoryService;

    public AdminController(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @Operation(
            summary = "Lich su don hang, don moi nhat len dau",
            description = """
                    Phan trang chay duoi database bang `OFFSET ? ROWS FETCH NEXT ? ROWS ONLY`, \
                    khong keo ca bang len roi cat o Java.

                    Trang vuot qua du lieu tra ve danh sach rong (khong phai loi).""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "{ items, page, size, totalItems, totalPages }"),
            @ApiResponse(responseCode = "400", description = "INVALID_PAGE / INVALID_PARAMETER")})
    @GetMapping("/orders")
    public PageResult<OrderSummary> orders(
            @Parameter(description = "Dem tu 0", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Trong khoang 1..100", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        return orderHistoryService.page(page, size);
    }

    @Operation(summary = "Chi tiet mot don da chot, kem cac dong hang")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hoa don day du"),
            @ApiResponse(responseCode = "404", description = "ORDER_NOT_FOUND")})
    @GetMapping("/orders/{orderId}")
    public Invoice order(
            @Parameter(description = "Ma don da chot", example = "DH-000001")
            @PathVariable String orderId) {
        return orderHistoryService.findOrder(orderId);
    }
}
