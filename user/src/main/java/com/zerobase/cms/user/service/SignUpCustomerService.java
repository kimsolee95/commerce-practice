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
  public void verifyEmail(String email, String code) {

    Customer customer = customerRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_CUSTOMER));

    if (customer.isVerify()) {
      throw new CustomException(ErrorCode.ALREADY_VERIFY);
    } else if (!customer.getVerificationCode().equals(code)) {
      throw new CustomException(ErrorCode.WRONG_VERIFICATION);
    } else if (customer.getVerifyExpiredAt().isBefore(LocalDateTime.now())) {
      throw new CustomException(ErrorCode.EXPIRE_CODE);
    }

    customer.setVerify(true);
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
