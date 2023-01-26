package com.zerobase.cms.user.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

  private final ErrorCode errorCode;

  public CustomException(ErrorCode errorCode) {
    super(errorCode.getDetail()); // 상위 생성자로 전달
    this.errorCode = errorCode;
  }

}
