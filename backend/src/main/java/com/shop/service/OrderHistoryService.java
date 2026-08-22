package com.shop.service;

import com.shop.dto.Invoice;
import com.shop.dto.OrderSummary;
import com.shop.dto.PageResult;
import com.shop.exception.BusinessException;
import com.shop.exception.ErrorCode;
import com.shop.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public class OrderHistoryService {

    static final int MAX_PAGE_SIZE = 100;

    private final OrderRepository orderRepository;

    public OrderHistoryService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public PageResult<OrderSummary> page(int page, int size) {
        validate(page, size);

        long totalItems = orderRepository.countAll();
        long offset = (long) page * size;
        List<OrderSummary> items = offset >= totalItems
                ? List.of()
                : orderRepository.findPage((int) offset, size);

        return PageResult.of(items, page, size, totalItems);
    }

    public Invoice findOrder(String orderId) {
        return orderRepository.getOrThrow(orderId);
    }

    private void validate(int page, int size) {
        if (page < 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_PAGE, "page phai >= 0.", Map.of("page", page));
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    ErrorCode.INVALID_PAGE,
                    "size phai trong khoang 1.." + MAX_PAGE_SIZE + ".",
                    Map.of("size", size));
        }
    }
}
