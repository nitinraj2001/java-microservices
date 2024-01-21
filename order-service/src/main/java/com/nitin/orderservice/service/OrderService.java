package com.nitin.orderservice.service;

import com.nitin.orderservice.dto.InventoryResponse;
import com.nitin.orderservice.dto.OrderLineItemsDto;
import com.nitin.orderservice.dto.OrderRequest;
import com.nitin.orderservice.entity.Order;
import com.nitin.orderservice.entity.OrderLineItems;
import com.nitin.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;

    public String placedOrder(OrderRequest orderRequest){
        Order order=new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItemsList(orderRequest.getOrderLineItemsDtoList().stream().map(this::mapToDto).collect(Collectors.toList()));

        //fetch all skuCodes from order line Numbers
        List<String> skuCodes=order.getOrderLineItemsList().stream().map(orderLineItems -> orderLineItems.getSkuCode()).collect(Collectors.toList());
        //before placing order it call inventory service to check whether product is in stock or not
        InventoryResponse[] inventoryResponses = webClientBuilder.build().get().uri("http://inventory-service/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode",skuCodes).build())
                        .retrieve().bodyToMono(InventoryResponse[].class).block();
        //Determine whether all products are in stocks or not
        boolean allInStock=Arrays.stream(inventoryResponses).allMatch(inventoryResponse -> inventoryResponse.isInStock());
        if(allInStock){
            orderRepository.save(order);
            return "Order is placed successfully";
        }
        else{
            throw new IllegalArgumentException("Product is not in stock pls try again later!");
        }

        //log.info("order {} is saved successfully",order.getOrderNumber());
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems=new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());

        return orderLineItems;

    }
}
