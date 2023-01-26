package com.zerobase.cms.user.config;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

  @Value(value = "${mailgun.key}")
  private String mailgunKey; //1de076a33ab42a7d21638b1220080887-c9746cf8-5ad99928

  @Qualifier(value = "mailgun")
  @Bean
  public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
    return new BasicAuthRequestInterceptor("api", mailgunKey);
  }

}
