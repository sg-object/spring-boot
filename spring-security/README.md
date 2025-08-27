## @EnableWebSecurity
* Spring Security의 웹 보안 기능을 활성화하고, 사용자 정의 보안 설정을 적용할 수 있도록 SecurityFilterChain을 생성하는 역할
* Spring Boot의 autoconfigure package의 **SpringBootWebSecurityConfiguration.WebSecurityEnablerConfiguration** 의 @ConditionalOnClass를 이용해 Project의 dependency에 Spring Security가 존재하면 @EnableWebSecurity를 명시하지 않아도 자동으로 관련 기능들을 등록해줌
```java
  @Configuration(
    proxyBeanMethods = false
  )
  @ConditionalOnMissingBean(
    name = {"springSecurityFilterChain"}
  )
  @ConditionalOnClass({EnableWebSecurity.class})  // 해당 Class가 존재하는 경우 동작
  @EnableWebSecurity
  static class WebSecurityEnablerConfiguration {
    WebSecurityEnablerConfiguration() {
    }
  }
```


## SecurityFilterChain
* 사용자가 Filter Chain을 명시하지 않으면 기본 Filter Chain으로 SecurityFilterChainConfiguration의 defaultSecurityFilterChain이 등록됨 
```java
  @Configuration(
    proxyBeanMethods = false
  )
  @ConditionalOnDefaultWebSecurity
  static class SecurityFilterChainConfiguration {
    SecurityFilterChainConfiguration() {
    }

    @Bean
    @Order(2147483642)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
      http.authorizeHttpRequests((requests) -> {
        ((AuthorizeHttpRequestsConfigurer.AuthorizedUrl)requests.anyRequest()).authenticated();
      });
      http.formLogin(Customizer.withDefaults());
      http.httpBasic(Customizer.withDefaults());
      return (SecurityFilterChain)http.build();
    }
  }

  ////////////////////////////////////////////////////////////////
  
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @Conditional({DefaultWebSecurityCondition.class})
  public @interface ConditionalOnDefaultWebSecurity {
  }

  ////////////////////////////////////////////////////////////////

  class DefaultWebSecurityCondition extends AllNestedConditions {
    DefaultWebSecurityCondition() {
      super(ConfigurationPhase.REGISTER_BEAN);
    }
  
    @ConditionalOnMissingBean({SecurityFilterChain.class})
    static class Beans {
      Beans() {
      }
    }
  
    @ConditionalOnClass({SecurityFilterChain.class, HttpSecurity.class})
    static class Classes {
      Classes() {
      }
    }
  }
```
* DefaultWebSecurityCondition 내부에 정의한 Beans class의 **@ConditionalOnMissingBean({SecurityFilterChain.class})** 로 사용자가 Filter Chain을 명시했는지 판단
