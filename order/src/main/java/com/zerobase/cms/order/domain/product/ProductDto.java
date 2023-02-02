package com.zerobase.cms.order.domain.product;

import com.zerobase.cms.order.domain.model.Product;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

  private Long id;
  private String name;
  private String description;
  private List<ProductItemDto> items;

  //product item 포함 from 메서드
  public static ProductDto from(Product product) {
    List<ProductItemDto> items = product.getProductItems()
        .stream().map(ProductItemDto::from).collect(Collectors.toList());

    return ProductDto.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .items(items)
        .build();
  }

  //product item 포함하지 않는 from 메서드
  public static ProductDto withoutItemsFrom(Product product) {

    return ProductDto.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .build();
  }

}
