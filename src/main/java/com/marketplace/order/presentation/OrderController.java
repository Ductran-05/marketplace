package com.marketplace.order.presentation;

import com.marketplace.order.application.GetOrdersUseCase;
import com.marketplace.order.application.PlaceOrderUseCase;
import com.marketplace.order.application.command.PlaceOrderCommand;
import com.marketplace.order.presentation.request.PlaceOrderRequest;
import com.marketplace.order.presentation.response.OrderResponse;
import com.marketplace.shared.presentation.PageResponse;
import com.marketplace.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final GetOrdersUseCase getOrdersUseCase;

    public OrderController(PlaceOrderUseCase placeOrderUseCase, GetOrdersUseCase getOrdersUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.getOrdersUseCase = getOrdersUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> place(@Valid @RequestBody PlaceOrderRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser user) {
        var orderId = placeOrderUseCase.execute(new PlaceOrderCommand(
                user.userId(),
                request.items().stream()
                        .map(i -> new PlaceOrderCommand.Item(i.productId(), i.quantity()))
                        .toList()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("orderId", orderId.toString()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable UUID id,
                                                 @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(OrderResponse.from(getOrdersUseCase.getById(id, user.userId())));
    }

    @GetMapping("/my")
    public ResponseEntity<PageResponse<OrderResponse>> myOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal AuthenticatedUser user) {
        var result = getOrdersUseCase.getMyOrders(user.userId(), page, Math.min(size, 100));
        return ResponseEntity.ok(new PageResponse<>(
                result.items().stream().map(OrderResponse::from).toList(),
                result.page(), result.size(), result.totalItems(), result.totalPages()
        ));
    }
}
