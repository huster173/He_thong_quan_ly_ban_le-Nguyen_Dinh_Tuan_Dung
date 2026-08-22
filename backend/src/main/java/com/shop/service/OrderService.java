package com.shop.service;

import com.shop.dto.Invoice;
import com.shop.dto.OrderItemRequest;
import com.shop.dto.OrderRequest;
import com.shop.dto.PromotionResult;
import com.shop.dto.SubtotalResult;
import com.shop.model.Region;
import com.shop.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final PricingService pricingService;
    private final PromotionService promotionService;
    private final TaxService taxService;
    private final StockService stockService;
    private final OrderRepository orderRepository;
    private final PlatformTransactionManager transactionManager;

    public OrderService(PricingService pricingService,
                        PromotionService promotionService,
                        TaxService taxService,
                        StockService stockService,
                        OrderRepository orderRepository,
                        PlatformTransactionManager transactionManager) {
        this.pricingService = pricingService;
        this.promotionService = promotionService;
        this.taxService = taxService;
        this.stockService = stockService;
        this.orderRepository = orderRepository;
        this.transactionManager = transactionManager;
    }

    public SubtotalResult subtotal(List<OrderItemRequest> items) {
        return pricingService.calculate(items);
    }

    public PromotionResult promotion(List<OrderItemRequest> items, String couponCode) {
        return promotionService.apply(subtotal(items), couponCode);
    }

    /*
     * Chong race condition khi nhieu don cung mua mot san pham.
     *
     * Khong SELECT ton kho roi so sanh bang if trong Java: con so doc duoc co the da cu
     * vao luc UPDATE chay. Dieu kien "stock >= ?" nam ngay trong cau UPDATE cua
     * StockService, DB khoa dong va tu quyet dinh don nao lay duoc hang. StockService
     * cung duyet productId theo thu tu tang dan de hai don trung san pham khong khoa
     * cheo nhau gay deadlock.
     *
     * Tru kho nhieu san pham + ghi 2 bang lich su nam trong CUNG mot transaction, nen
     * khong the co canh tru duoc mot phan roi dung lai, hay kho bi tru ma don khong luu.
     * Tinh tien va khuyen mai lam truoc khi mo transaction de row lock giu cang ngan cang tot.
     *
     * Khuyen mai tinh lai tu dau o day, khong dung lai ket qua client dang hien thi:
     * gio hang co the da doi giua luc xem truoc va luc bam xac nhan.
     */
    public Invoice confirm(OrderRequest request) {
        Map<String, Integer> quantities = pricingService.mergeAndValidate(request.items());
        PromotionResult promotion = promotionService.apply(
                pricingService.calculateMerged(quantities), request.couponCode());

        TransactionStatus tx = transactionManager.getTransaction(transactionDefinition());
        Invoice invoice;
        try {
            invoice = buildInvoice(
                    orderRepository.nextOrderId(), LocalDateTime.now(),
                    promotion, request.regionOrDefault(), true);

            stockService.deduct(quantities);
            orderRepository.save(invoice);
        } catch (RuntimeException | Error e) {
            transactionManager.rollback(tx);
            throw e;
        }

        transactionManager.commit(tx);
        return invoice;
    }

    private TransactionDefinition transactionDefinition() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("confirm-order");
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return definition;
    }

    private Invoice buildInvoice(String orderId, LocalDateTime createdAt,
                                 PromotionResult promotion, Region region, boolean stockUpdated) {
        long taxAmount = taxService.taxFor(promotion.total(), region);
        long finalTotal = promotion.total() + taxAmount;

        return new Invoice(
                orderId,
                createdAt,
                promotion.lines(),
                promotion.subtotal(),
                promotion.categoryDiscount(),
                promotion.couponCode(),
                promotion.couponDiscount(),
                promotion.total(),
                region,
                region.getTaxRate(),
                taxAmount,
                finalTotal,
                stockUpdated);
    }
}
