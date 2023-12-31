package com.food.ordering.system.order.service.domain.mapper

import com.food.ordering.system.domain.valueobject.*
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress
import com.food.ordering.system.order.service.domain.dto.track.TrackOrderResponse
import com.food.ordering.system.order.service.domain.entity.Order
import com.food.ordering.system.order.service.domain.entity.OrderItem
import com.food.ordering.system.order.service.domain.entity.Product
import com.food.ordering.system.order.service.domain.entity.Restaurant
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventProduct
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OrderDataMapper {

    fun createOrderCommandToRestaurant(createOrderCommand: CreateOrderCommand): Restaurant {
        return Restaurant(
            restaurantId = RestaurantId(createOrderCommand.restaurantId),
            products = createOrderCommand.items.map { orderItem -> Product(ProductId(orderItem.productId)) }.toList()
        )
    }

    fun createOrderCommandToOrder(createOrderCommand: CreateOrderCommand): Order {
        return Order(
            customerId = CustomerId(createOrderCommand.customerId),
            restaurantId = RestaurantId(createOrderCommand.restaurantId),
            deliveryAddress = orderAddressToStreetAddress(createOrderCommand.address),
            price = Money(createOrderCommand.price),
            items = orderItemsToOrderItemEntities(createOrderCommand.items)
        )
    }

    private fun orderItemsToOrderItemEntities(
        items: List<com.food.ordering.system.order.service.domain.dto.create.OrderItem>,
    ): List<OrderItem> {
        return items.map { orderItem ->
            OrderItem(
                orderId = OrderId(UUID.randomUUID()),
                product = Product(ProductId(orderItem.productId)),
                price = Money(orderItem.price),
                subTotal = Money(orderItem.subTotal),
                orderItemId = OrderItemId(UUID.randomUUID()),
                quantity = orderItem.quantity,
            )
        }
    }


    private fun orderAddressToStreetAddress(address: OrderAddress): StreetAddress {
        return StreetAddress(
            id = UUID.randomUUID(),
            street = address.street,
            postalCode = address.postalCode,
            city = address.city,
        )
    }

    fun orderToCreateOrderResponse(
        order: Order,
        message: String,
    ): CreateOrderResponse {
        return CreateOrderResponse(
            orderTrackingId = order.trackingId.value,
            orderStatus = order.orderStatus,
            message = message,
        )
    }

    fun orderToTrackOrderResponse(order: Order): TrackOrderResponse {
        return TrackOrderResponse(
            orderTrackingId = order.trackingId.value,
            orderStatus = order.orderStatus,
            failureMessages = order.failureMessages
        )
    }

    fun orderCreatedEventToOrderPaymentEventPayload(orderCreatedEvent: OrderCreatedEvent): OrderPaymentEventPayload {
        return OrderPaymentEventPayload(
            customerId = orderCreatedEvent.order.customerId.value.toString(),
            orderId = orderCreatedEvent.order.id.value.toString(),
            price = orderCreatedEvent.order.price.amount,
            createdAt = orderCreatedEvent.createdAt,
            paymentOrderStatus = PaymentOrderStatus.PENDING.name
        )
    }

    fun orderPaidEventToOrderApprovalEventPayload(orderPaidEvent: OrderPaidEvent): OrderApprovalEventPayload {
        return OrderApprovalEventPayload(
            orderId = orderPaidEvent.order.id.value.toString(),
            restaurantId = orderPaidEvent.order.restaurantId.value.toString(),
            price = orderPaidEvent.order.price.amount,
            products = orderPaidEvent.order.items.map { orderItem ->
                OrderApprovalEventProduct(
                    id = orderItem.product.id.value.toString(),
                    quantity = orderItem.quantity,
                )
            },
            createdAt = orderPaidEvent.createdAt,
            restaurantOrderStatus = RestaurantOrderStatus.PAID.name
        )
    }

    fun orderCancelledEventToOrderPaymentEventPayload(orderCancelledEvent: OrderCancelledEvent): OrderPaymentEventPayload =
        OrderPaymentEventPayload(
            orderId = orderCancelledEvent.order.id.value.toString(),
            customerId = orderCancelledEvent.order.customerId.value.toString(),
            price = orderCancelledEvent.order.price.amount,
            createdAt = orderCancelledEvent.createdAt,
            paymentOrderStatus = PaymentOrderStatus.CANCELLED.name,
        )
}
