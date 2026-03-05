package com.beyond.ordersystem.product.domain;

import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.product.dtos.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private Integer price;
    private Integer stockQuantity;
    private String imagePath;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Member member;

    public void updateProfileImageUrl(String url){
        this.imagePath = url;
    }

    public void updateStockQunatity(int orderQuantity){
        this.stockQuantity = this.stockQuantity-orderQuantity;
    }

    public void updateProduct(ProductUpdateDto dto){
        this.name = dto.getName();
        this.category = dto.getCategory();
        this.price = dto.getPrice();
        this.stockQuantity = dto.getStockQuantity();

    }

}
