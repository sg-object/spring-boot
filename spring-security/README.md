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
* Spring Boot의 autoconfigure package의 **SpringBootWebSecurityConfiguration.WebSecurityEnablerConfiguration** 의 @ConditionalOnClass를 이용해 Project에 Spring Security 의존성이 활성화되어 있으면 @EnableWebSecurity를 명시하지 않아도 자동으로 관련 기능들을 등록해줌
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

## MultiSecurityFilterChain
* SecurityFilterChain은 여려개 등록 가능
* 복수의 SecurityFilterChain 등록 시 각 Chain의 RequestMatcher를 설정해야 됨 (인가 RequestMatcher와 별도)
* HttpSecurity의 securityMatchers Method를 이용해 Chain의 RequestMatcher를 설정
* SecurityFilterChain은 등록 순서로 실행되기 때문에 실행 순서를 설정할 때는 @Order를 사용 
```java
@Order(1)
@Bean
SecurityFilterChain firstChain(final HttpSecurity http) throws Exception {
  http.securityMatchers(auth -> auth.requestMatchers("/test/**"));
  http.authorizeHttpRequests(req -> req.requestMatchers("/test/pass").permitAll());
  return http.build();
}

@Order(2)
@Bean
SecurityFilterChain secondChain(final HttpSecurity http) throws Exception {
  http.securityMatchers(auth -> auth.requestMatchers("/admin/**"));
  http.authorizeHttpRequests(req -> req.requestMatchers("/admin/pass").permitAll());
  return http.build();
}
```

## SecurityContextHolder
* Spring Security에서 현재 사용자의 보안 정보를 저장하고 접근하는 데 사용되는 핵심 클래스
* Application 전반에서 인증된 사용자 정보를 참조할 수 있게 해주는 **Security Context**를 관리
* SecurityContextHolder에서 Security Context를 직접 관리하지 않고 SecurityContextHolderStrategy에 위임함
* Thread와 Security Context 연결 방식에 따라 3가지 전략이 존재
* MODE_THREADLOCAL : 현재 Thread에서만 사용가능 **(Default)**
* MODE_INHERITABLETHREADLOCAL : 현재 Thread의 자식 Thread까지 공유
* MODE_GLOBAL : Application의 모든 Thread에 공유
```java
public class SecurityContextHolder {
  public static final String MODE_THREADLOCAL = "MODE_THREADLOCAL";
  public static final String MODE_INHERITABLETHREADLOCAL = "MODE_INHERITABLETHREADLOCAL";
  public static final String MODE_GLOBAL = "MODE_GLOBAL";
  private static final String MODE_PRE_INITIALIZED = "MODE_PRE_INITIALIZED";
  public static final String SYSTEM_PROPERTY = "spring.security.strategy";
  private static String strategyName = System.getProperty("spring.security.strategy");
  private static SecurityContextHolderStrategy strategy;
  private static int initializeCount = 0;

  //...

  private static void initializeStrategy() {
    if ("MODE_PRE_INITIALIZED".equals(strategyName)) {
      Assert.state(strategy != null, "When using MODE_PRE_INITIALIZED, setContextHolderStrategy must be called with the fully constructed strategy");
    } else {
      if (!StringUtils.hasText(strategyName)) {
        strategyName = "MODE_THREADLOCAL";
      }

      if (strategyName.equals("MODE_THREADLOCAL")) {
        strategy = new ThreadLocalSecurityContextHolderStrategy();
      } else if (strategyName.equals("MODE_INHERITABLETHREADLOCAL")) {
        strategy = new InheritableThreadLocalSecurityContextHolderStrategy();
      } else if (strategyName.equals("MODE_GLOBAL")) {
        strategy = new GlobalSecurityContextHolderStrategy();
      } else {
        try {
          Class<?> clazz = Class.forName(strategyName);
          Constructor<?> customStrategy = clazz.getConstructor();
          strategy = (SecurityContextHolderStrategy) customStrategy.newInstance();
        } catch (Exception var2) {
          Exception ex = var2;
          ReflectionUtils.handleReflectionException(ex);
        }

      }
    }
  }
  
  //...
  
}
```
* Spring Security 5.7이후로 SecurityContextPersistenceFilter가 Deprecated 되고 SecurityContextHolderFilter를 사용
* SecurityContextHolderFilter는 Filter Chain에 자동 추가됨
* Request를 완료할 때 SecurityContextHolderFilter에서 Security Context를 Clear 처리
```java
public class SecurityContextHolderFilter extends GenericFilterBean {
  private static final String FILTER_APPLIED = SecurityContextHolderFilter.class.getName() + ".APPLIED";
  private final SecurityContextRepository securityContextRepository;
  private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

  //...

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    this.doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
  }

  private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    if (request.getAttribute(FILTER_APPLIED) != null) {
      chain.doFilter(request, response);
    } else {
      request.setAttribute(FILTER_APPLIED, Boolean.TRUE);
      Supplier<SecurityContext> deferredContext = this.securityContextRepository.loadDeferredContext(request);

      try {
        this.securityContextHolderStrategy.setDeferredContext(deferredContext);
        chain.doFilter(request, response);
      } finally {
        
        //########################################################
        this.securityContextHolderStrategy.clearContext();  // Clear 처리
        //########################################################
        
        request.removeAttribute(FILTER_APPLIED);
      }

    }
  }

  //...
  
}
```