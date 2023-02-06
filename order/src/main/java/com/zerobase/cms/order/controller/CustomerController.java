package com.zerobase.cms.order.controller;

import com.zerobase.cms.order.application.CartApplication;
import com.zerobase.cms.order.application.OrderApplication;
import com.zerobase.cms.order.domain.product.AddProductCartForm;
import com.zerobase.cms.order.domain.redis.Cart;
import com.zerobase.cms.order.service.CartService;
import com.zerobase.domain.config.JwtAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer/cart")
@RequiredArgsConstructor
public class CustomerController {

  private final CartApplication cartApplication;
  private final JwtAuthenticationProvider provider;
  private final OrderApplication orderApplication;

  @PostMapping
  public ResponseEntity<Cart> addCart(@RequestHeader(name = "X-AUTH-TOKEN") String token,
      @RequestBody AddProductCartForm form) {
    return ResponseEntity.ok(cartApplication.addCart(provider.getUserVo(token).getId(), form));
  }

  @GetMapping
  public ResponseEntity<Cart> showCart(@RequestHeader(name = "X-AUTH-TOKEN") String token,
      @RequestBody AddProductCartForm form) {
    return ResponseEntity.ok(cartApplication.getCart(provider.getUserVo(token).getId()));
  }

  @PutMapping
  public ResponseEntity<Cart> updateCart(@RequestHeader(name = "X-AUTH-TOKEN") String token,
      @RequestBody Cart cart) {
    return ResponseEntity.ok(cartApplication.updateCart(provider.getUserVo(token).getId(), cart));
  }

  @PostMapping("/order")
  public ResponseEntity<Cart> order(@RequestHeader(name = "X-AUTH-TOKEN") String token,
      @RequestBody Cart cart) {
    orderApplication.order(token, cart);
    return ResponseEntity.ok().build();
  }

}
