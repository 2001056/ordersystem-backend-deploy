package com.beyond.ordersystem.ordering.dtos;

import com.beyond.ordersystem.ordering.domain.OrderDetail;
import com.beyond.ordersystem.ordering.domain.OrderStatus;
import com.beyond.ordersystem.ordering.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderListDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailDto> orderDetails;
    public static OrderListDto fromEntity(Ordering ordering){
        List<OrderDetailDto> orderDetailDtos = new ArrayList<>();
        for (OrderDetail orderDetail : ordering.getOrderDetailList()){
            orderDetailDtos.add(OrderDetailDto.fromEntity(orderDetail));
        }
        OrderListDto orderListDto = OrderListDto.builder()
                .id(ordering.getId())
                .orderStatus(ordering.getOrderStatus())
                .memberEmail(ordering.getMember().getEmail())
                .orderDetails(orderDetailDtos)
                .build();
        return orderListDto;
    }
}
