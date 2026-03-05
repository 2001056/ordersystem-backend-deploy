package com.beyond.ordersystem.product.dtos;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProductUpdateDto {
    private String name;
    private String category;
    private int price;
    private int stockQuantity;
//    이미지 수정은 일반적으로 별도의 api로 처리
    private MultipartFile productImage;

}
