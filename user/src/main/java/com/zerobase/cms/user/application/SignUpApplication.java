package com.zerobase.cms.user.application;

import com.zerobase.cms.user.client.MailgunClient;
import com.zerobase.cms.user.client.mailgun.SendMailForm;
import com.zerobase.cms.user.domain.SignUpForm;
import com.zerobase.cms.user.domain.model.Customer;
import com.zerobase.cms.user.domain.model.Seller;
import com.zerobase.cms.user.exception.CustomException;
import com.zerobase.cms.user.exception.ErrorCode;
import com.zerobase.cms.user.service.customer.SignUpCustomerService;
import com.zerobase.cms.user.service.seller.SignUpSellerService;
import com.zerobase.domain.domain.common.UserType;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignUpApplication {

  private final MailgunClient mailgunClient;
  private final SignUpCustomerService signUpCustomerService;
  private final SignUpSellerService signUpSellerService;

  private final String TYPE_CUSTOMER = "customer";
  private final String TYPE_SELLER = "seller";

  public void customerVerify(String email, String code) {
    signUpCustomerService.verifyEmail(email, code);
  }

  public String customerSignUp(SignUpForm form) {

    if (signUpCustomerService.isEmailExist(form.getEmail())) {
      throw new CustomException(ErrorCode.ALREADY_REGISTERED_ACCOUNT);
    } else {

      Customer customer = signUpCustomerService.signUp(form);
      LocalDateTime now = LocalDateTime.now();
      String code = getRandomCode();

      SendMailForm sendMailForm = SendMailForm.builder()
          .from("ahhasolee@naver.com")
          .to(form.getEmail())
          .subject("Verification Email")
          .text(getVerificationEmailBody(form.getEmail(), form.getName(), TYPE_CUSTOMER , code))
          .build();
      mailgunClient.sendEmail(sendMailForm);
      signUpCustomerService.changeCustomerEmail(customer.getId(), code);
      return "회원 가입에 성공하였습니다.";
    }
  }

  public void  sellerVerify(String email, String code) {
    signUpSellerService.verifyEmail(email, code);
  }

  public String sellerSignUp(SignUpForm form) {

    if (signUpSellerService.isEmailExist(form.getEmail())) {
      throw new CustomException(ErrorCode.ALREADY_REGISTERED_ACCOUNT);
    } else {

      Seller seller = signUpSellerService.signUp(form);

      String code = getRandomCode();
      SendMailForm sendMailForm = SendMailForm.builder()
          .from("ahhasolee@naver.com")
          .to(form.getEmail())
          .subject("Verification Email (Seller)")
          .text(getVerificationEmailBody(seller.getEmail(), seller.getName(), TYPE_SELLER, code))
          .build();
      log.info("Send Email result: " + mailgunClient.sendEmail(sendMailForm).getStatusCode());

      signUpSellerService.changeSellerValidateEamil(seller.getId(), code);
      return "회원 가입에 성공하였습니다.";
    }
  }

  private String getRandomCode() {
    return RandomStringUtils.random(10, true, true);
  }

  private String getVerificationEmailBody(String email, String name, String type, String code) {

    StringBuilder builder = new StringBuilder();
    return builder.append("Hello ").append(name).append("! Please Click Link for verification. \n\n")
        .append("http://localhost:8081/signup/" + type + "/verify?email=")
        .append(email)
        .append("&code=")
        .append(code).toString();
  }

}
