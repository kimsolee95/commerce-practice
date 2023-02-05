package com.zerobase.cms.order.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductItem is a Querydsl query type for ProductItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductItem extends EntityPathBase<ProductItem> {

    private static final long serialVersionUID = 617999904L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductItem productItem = new QProductItem("productItem");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final NumberPath<Integer> count = createNumber("count", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedAt = _super.modifiedAt;

    public final StringPath name = createString("name");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final QProduct product;

    public final NumberPath<Long> sellerId = createNumber("sellerId", Long.class);

    public QProductItem(String variable) {
        this(ProductItem.class, forVariable(variable), INITS);
    }

    public QProductItem(Path<? extends ProductItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductItem(PathMetadata metadata, PathInits inits) {
        this(ProductItem.class, metadata, inits);
    }

    public QProductItem(Class<? extends ProductItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product")) : null;
    }

}

