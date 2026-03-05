package com.beyond.ordersystem.common.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SseMessageDto {
    private String receiver;
    private String sender;
    private String message;

}
