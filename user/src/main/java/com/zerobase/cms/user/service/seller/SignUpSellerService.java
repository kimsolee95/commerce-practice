package com.zerobase.cms.user.service.seller;

import com.zerobase.cms.user.domain.SignUpForm;
import com.zerobase.cms.user.domain.model.Seller;
import com.zerobase.cms.user.domain.repository.SellerRepository;
import com.zerobase.cms.user.exception.CustomException;
import com.zerobase.cms.user.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignUpSellerService {

  private final SellerRepository sellerRepository;

  public Seller signUp(SignUpForm form) {
    return sellerRepository.save(Seller.from(form));
  }

  public boolean isEmailExist(String email) {
    return sellerRepository.findByEmail(email).isPresent();
  }

  @Transactional
  public void verifyEmail(String email, String code) {

    Seller seller = sellerRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_SELLER));

    if (seller.isVerify()) {
      throw new CustomException(ErrorCode.ALREADY_VERIFY);
    } else if (!seller.getVerificationCode().equals(code)) {
      throw new CustomException(ErrorCode.WRONG_VERIFICATION);
    } else if (seller.getVerifyExpiredAt().isBefore(LocalDateTime.now())) {
      throw new CustomException(ErrorCode.EXPIRE_CODE);
    }
    seller.setVerify(true);
  }

  @Transactional
  public LocalDateTime changeSellerValidateEamil(Long sellerId, String verificationCode) {

    Seller seller = sellerRepository.findById(sellerId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_EXIST_SELLER));

    seller.setVerificationCode(verificationCode);
    seller.setVerifyExpiredAt(LocalDateTime.now().plusDays(1));
    return seller.getVerifyExpiredAt();
  }

}
