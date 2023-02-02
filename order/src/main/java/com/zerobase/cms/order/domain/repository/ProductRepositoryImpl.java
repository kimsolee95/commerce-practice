package com.zerobase.cms.order.domain.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zerobase.cms.order.domain.model.Product;
import com.zerobase.cms.order.domain.model.QProduct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom{

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public List<Product> searchByName(String name) {

    String search = "%" + name + "%";
    QProduct product = QProduct.product;
    return jpaQueryFactory.selectFrom(product)
        .where(product.name.like(search))
        .fetch();
  }
}
