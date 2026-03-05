package com.beyond.ordersystem.ordering.dtos;

import com.beyond.ordersystem.ordering.domain.OrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderDetailDto {
    private Long detailId;
    private String productName;
    private Integer productCount;
    public static OrderDetailDto fromEntity(OrderDetail orderDetail){
        return  OrderDetailDto.builder()
                .detailId(orderDetail.getId())
                .productName(orderDetail.getProduct().getName())
                .productCount(orderDetail.getQuantity())
                .build();
    }
}
