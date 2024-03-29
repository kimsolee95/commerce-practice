package com.zerobase.cms.user.application;

import com.zerobase.cms.user.domain.SignInForm;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.domain.model.Seller;
import com.zerobase.cms.user.exception.CustomException;
import com.zerobase.cms.user.exception.ErrorCode;
import com.zerobase.cms.user.service.customer.CustomerService;
import com.zerobase.cms.user.service.seller.SellerService;
import com.zerobase.domain.config.JwtAuthenticationProvider;
import com.zerobase.domain.domain.common.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignInApplication {

  private final CustomerService customerService;
  private final SellerService sellerService;
  private final JwtAuthenticationProvider provider;

  public String customerLoginToken(SignInForm form) {

    //1. 로그인 가능 여부 check
    Customer customer = customerService.findValidCustomer(form.getEmail(), form.getPassword())
        .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_CHECK_FAIL));
    //2. 토큰 발행

    //3. 토큰 response
    return provider.createToken(customer.getEmail(), customer.getId(), UserType.CUSTOMER);
  }

  public String sellerLoginToken(SignInForm form) {

    //1. 로그인 가능 여부
    Seller seller = sellerService.findValidSeller(form.getEmail(), form.getPassword())
        .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_CHECK_FAIL));

    //2. 토큰 발행 및 response 담기
    return provider.createToken(seller.getEmail(), seller.getId(), UserType.SELLER);
  }

}
