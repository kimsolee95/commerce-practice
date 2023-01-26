package com.zerobase.cms.user.service;

import com.zerobase.cms.user.domain.SignUpForm;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.domain.repository.CustomerRepository;
import com.zerobase.cms.user.exception.CustomException;
import com.zerobase.cms.user.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignUpCustomerService {

  private final CustomerRepository customerRepository;

  public Customer signUp(SignUpForm form) {
    return customerRepository.save(Customer.from(form));
  }

  public boolean isEmailExist(String email) {
    return customerRepository.findByEmail(email.toLowerCase(Locale.ROOT))
        .isPresent();
  }

  @Transactional
  public LocalDateTime changeCustomerEmail(Long customerId, String verificationCode) {
    Customer customer = customerRepository.findById(customerId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_CUSTOMER));

    customer.setVerificationCode(verificationCode);
    customer.setVerifyExpiredAt(LocalDateTime.now().plusDays(1));
    return customer.getVerifyExpiredAt();
  }

}
