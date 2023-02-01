package com.zerobase.cms.order.domain.model;

import com.zerobase.cms.order.domain.product.AddProductItemForm;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AuditOverride(forClass = BaseEntity.class)
public class ProductItem extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long sellerId;

  @Audited
  private String name;

  @Audited
  private Integer price;

  private Integer count;

  //양방향 매핑 (product 내에서도 productItem을 참조)
  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "product_id")
  private Product product;

  public static ProductItem of(Long sellerId, AddProductItemForm form) {
    return ProductItem.builder()
        .sellerId(sellerId)
        .name(form.getName())
        .price(form.getPrice())
        .count(form.getCount())
        .build();
  }
}
