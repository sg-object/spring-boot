## DelegatingFilterProxy
* Servlet Container의 Filter Chain에 추가되어 Spring Container의 FilterChainProxy에 요청을 전달함
* 요청이 Spring Container로 전달되었기 때문에 Bean을 통한 처리가 가능
* DelegatingFilterProxyRegistrationBean에 FilterChainProxy의 Bean 이름을 등록 **(springSecurityFilterChain)**
* Application이 구동되면서 DelegatingFilterProxyRegistrationBean의 getFilter()가 호출되어 DelegatingFilterProxy가 등록됨
```java
@AutoConfiguration(
  after = {SecurityAutoConfiguration.class}
)
@ConditionalOnWebApplication(
  type = Type.SERVLET
)
@EnableConfigurationProperties({SecurityProperties.class})
@ConditionalOnClass({AbstractSecurityWebApplicationInitializer.class, SessionCreationPolicy.class})
public class SecurityFilterAutoConfiguration {
  private static final String DEFAULT_FILTER_NAME = "springSecurityFilterChain";

  public SecurityFilterAutoConfiguration() {
  }

  @Bean
  @ConditionalOnBean(
    name = {"springSecurityFilterChain"}
  )
  public DelegatingFilterProxyRegistrationBean securityFilterChainRegistration(SecurityProperties securityProperties) {
    DelegatingFilterProxyRegistrationBean registration = new DelegatingFilterProxyRegistrationBean("springSecurityFilterChain", new ServletRegistrationBean[0]);
    registration.setOrder(securityProperties.getFilter().getOrder());
    registration.setDispatcherTypes(this.getDispatcherTypes(securityProperties));
    return registration;
  }

  private EnumSet<DispatcherType> getDispatcherTypes(SecurityProperties securityProperties) {
    return securityProperties.getFilter().getDispatcherTypes() == null ? null : (EnumSet)securityProperties.getFilter().getDispatcherTypes().stream().map((type) -> {
      return DispatcherType.valueOf(type.name());
    }).collect(Collectors.toCollection(() -> {
      return EnumSet.noneOf(DispatcherType.class);
    }));
  }
}
```
```java
public class DelegatingFilterProxy extends GenericFilterBean {
  @Nullable
  private String contextAttribute;
  @Nullable
  private WebApplicationContext webApplicationContext;
  @Nullable
  private String targetBeanName;
  private boolean targetFilterLifecycle;
  @Nullable
  private volatile Filter delegate;
  private final Lock delegateLock;
  
  //...

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    Filter delegateToUse = this.delegate;
    if (delegateToUse == null) {
      this.delegateLock.lock();

      try {
        delegateToUse = this.delegate;
        if (delegateToUse == null) {
          WebApplicationContext wac = this.findWebApplicationContext();
          if (wac == null) {
            throw new IllegalStateException("No WebApplicationContext found: no ContextLoaderListener or DispatcherServlet registered?");
          }

          delegateToUse = this.initDelegate(wac);
        }

        this.delegate = delegateToUse;
      } finally {
        this.delegateLock.unlock();
      }
    }

    this.invokeDelegate(delegateToUse, request, response, filterChain);
  }
}
```

## FilterChainProxy [springSecurityFilterChain]
* DelegatingFilterProxy로부터 받은 요청을 각 Filter Chain에 전달해서 처리
* FilterChainProxy의 멤버 변수로 Filter Chain List가 존재
* 사용자가 Filter Chain을 정의하지 않으면 defaultSecurityFilterChain이 자동 등록됨
* Filter Chain List에서 요청에 맞는 Filter Chain을 찾아서 처리
```java
public class FilterChainProxy extends GenericFilterBean {
  private static final Log logger = LogFactory.getLog(FilterChainProxy.class);
  private static final String FILTER_APPLIED = FilterChainProxy.class.getName().concat(".APPLIED");
  private SecurityContextHolderStrategy securityContextHolderStrategy;
  private List<SecurityFilterChain> filterChains;
  private FilterChainValidator filterChainValidator;
  private HttpFirewall firewall;
  private RequestRejectedHandler requestRejectedHandler;
  private ThrowableAnalyzer throwableAnalyzer;
  private FilterChainDecorator filterChainDecorator;

  //...
}
```

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
