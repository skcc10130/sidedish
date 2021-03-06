# 서비스 개요
> 동네 반찬 주문 및 배송 서비스

## 기능적 요구사항
1. 고객이 APP에서 반찬을 주문한다.
2. 고객이 결제한다.
3. 주문이 완료되면 주문내역이 반찬가게에 전달된다.
4. 반찬가게에서 주문정보가 확인되면 주문한 반찬을 배달한다.
5. 고객이 배달상태를 조회할 수 있다.
6. 고객이 주문을 취소할 수 있다.
7. 주문이 취소되면 결제가 취소된다.
8. 고객이 결제상태를 조회할 수 있다.
9. 고객이 전체적인 진행내역을 확인할 수 있다.

## 비기능적 요구사항
1. 트랜잭션
   * 미결제 주문은 거래가 성립되지 않아야 한다.
   * 주문이 취소되면 '결제취소' 및 '주문정보'가 갱신되어야 한다.
2. 장애격리
   * 대리점 서비스가 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다.
   * 결제시스템이 과중되면 주문을 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다
3. 성능
   * 고객이 모든 진행내역을 조회 할 수 있도록 성능을 고려하여 별도의 View로 구성한다.


# 체크포인트
1. Saga
2. CQRS
3. Correlation
4. Req/Resp
5. Gateway
6. Deploy/ Pipeline
7. Circuit Breaker
8. Autoscale (HPA)
9. Zero-downtime deploy (Readiness Probe)
10. Config Map/ Persistence Volume
11. Polyglot
12. Self-healing (Liveness Probe)


# 분석/설계
> 구성원 개인 역할 중심의 Horizontally-Aligned 조직에서 서비스 중심의 Vertically-Aligned 조직으로 전환되면서 각 서비스를 분리하여 Domain-Driven한 마이크로서비스 아키텍쳐를 클라우드 네이티브하게 구현한다.

## As-Is 조직 (Horizontally-Aligned)
![image](https://user-images.githubusercontent.com/82795797/122673113-f14fa780-d209-11eb-9bf8-e08ef5d8e958.png)

## To-Be 조직 (Vertically-Aligned)
![image](https://user-images.githubusercontent.com/82795797/122673137-180dde00-d20a-11eb-81e0-666e5990167f.png)

## Event Storming 결과
### 이벤트 도출

![image](https://user-images.githubusercontent.com/82795797/122673175-412e6e80-d20a-11eb-850b-f1d759438aef.png)

### 부적격 이벤트 탈락
> 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함.
* 계획된 사업(프로젝트) 범위에서 제외함( “회원 가입”, “메시지 발송됨”, “배송 취소됨” )
* UI의 이벤트로 제외함( “반찬 선택됨”, “주문수량 선택됨”, “결제버튼 클릭됨” )
* 이벤트의 의미가 동일함( “대리점에 주문정보가 전달됨” = “주문 완료됨” )

![image](https://user-images.githubusercontent.com/82795797/122673212-5905f280-d20a-11eb-839c-c44e8b10c443.png)

### 액터, 커맨드 부착하여 읽기 좋게

![image](https://user-images.githubusercontent.com/82795797/122673270-99fe0700-d20a-11eb-92fd-0137601eaadf.png)

### 어그리게잇으로 묶기
> 어그리게잇을 생성하고, 그와 연결된 커맨드와 이벤트들에 의해 트랜잭션이 유지되어야 하는 단위로 묶어줌.

![image](https://user-images.githubusercontent.com/82795797/122673284-aaae7d00-d20a-11eb-93e1-bb7e98f782ff.png)

### 바운디드 컨텍스트로 묶기
> 도메인 서열 분리
* Core ( app, store ), 핵심서비스 : SLA 수준은 Up-time 99.9% 목표, 배포주기는 app(1주, 1회 미만) / store(1개월, 1회 미만)
* Supporting ( customer ), 경쟁력서비스 : SLA 수준은 Up-time 60.0% 목표, 배포주기는 1주 1회 이상
* General ( pay ), 일반서비스 : 외부서비스를 사용

![image](https://user-images.githubusercontent.com/82795797/122673306-c4e85b00-d20a-11eb-93de-655ace3aac99.png)

### 폴리시 부착
> 전체 연계가 초기에 드러남

![image](https://user-images.githubusercontent.com/82795797/122673324-d5003a80-d20a-11eb-83e9-f3f3dcad4717.png)

### 폴리시의 이동

![image](https://user-images.githubusercontent.com/82795797/122673336-e0536600-d20a-11eb-8cd8-97b0d4e7cbd5.png)

### 컨텍스트 매핑
> 점선은 Pub/Sub, 실선은 Req/Resp

![image](https://user-images.githubusercontent.com/82795797/122673352-01b45200-d20b-11eb-9384-ed3eb57e180f.png)

### 완성된 모형

![image](https://user-images.githubusercontent.com/82795797/122673368-142e8b80-d20b-11eb-97d9-84981e08982f.png)

### 기능적 요구사항 검증
1. 고객이 APP에서 반찬을 주문한다. (OK)
2. 고객이 결제한다. (OK)
3. 주문이 완료되면 주문내역이 대리점(반찬가게)에 전달된다. (OK)
4. 대리점에서 주문정보가 확인되면 주문한 반찬을 배달한다. (OK)
5. 고객이 배달상태를 조회할 수 있다. (OK)

![image](https://user-images.githubusercontent.com/82795797/122673376-20b2e400-d20b-11eb-8cbe-6c887d75fc23.png)

6. 고객이 주문을 취소할 수 있다. (OK)
7. 주문이 취소되면 결제가 취소된다. (OK)
8. 고객이 결제상태를 조회할 수 있다. (OK)

![image](https://user-images.githubusercontent.com/82795797/122673380-29a3b580-d20b-11eb-8dc6-b21b5a48b013.png)

9. 고객이 전체적인 진행내역을 확인할 수 있다. (OK)

![image](https://user-images.githubusercontent.com/73699193/97982928-d3b70480-1e17-11eb-957e-6a9093d2a0d7.png)

### 비기능 요구사항 검증
1. 미결제가 주문은 거래가 성립되지 않아야 한다. -> Req/Res
2. 대리점관리 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다. -> Pub/Sub
3. 결제시스템이 과중되면 주문을 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다. > Circuit Breaker
4. 주문이 취소되면 '결제취소' 및 '주문정보'가 갱신되어야 한다. -> SAGA, 보상트랜젝션
5. 고객이 모든 진행내역을 조회 할 수 있도록 성능을 고려하여 별도의 View로 구성한다. -> CQRS

![image](https://user-images.githubusercontent.com/82795797/122673399-3cb68580-d20b-11eb-943c-a234da645549.png)

## 헥사고날 아키텍처 다이어그램 도출 (Polyglot)
* Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
* 호출관계에서 PubSub 과 Req/Resp 를 구분함
* 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐
* 대리점의 경우 Polyglot 검증을 위해 HSql로 셜계

![image](https://user-images.githubusercontent.com/82795797/122673409-49d37480-d20b-11eb-82ae-50aa63ab6094.png)


# 구현
> 총 5가지 서비스(App, Pay, Store, Customer, Gateway)로 구현함.

![image](https://user-images.githubusercontent.com/82795797/122876962-0dba2400-d371-11eb-8d3e-05b1f58c809f.png)

## DDD 적용
> 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. 
이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 하지만, 일부 구현에 있어서 영문이 아닌 경우는 실행이 불가능한 경우가 있기 때문에 계속 사용할 방법은 아닌것 같다.
(Maven pom.xml, Kafka의 topic id, FeignClient 의 서비스 id 등은 한글로 식별자를 사용하는 경우 오류가 발생하는 것을 확인하였다)

```
package sidedish;

import org.springframework.beans.BeanUtils;

import javax.persistence.*;

@Entity
@Table(name = "Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String item;
    private Integer qty;
    private Double price;
    private String store;    
    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    -- 생략 --
```

> Entity / Repository Pattern 적용 및 JPA 를 통하여 다양한 데이터소스 유형에 대한 별도의 처리가 필요 없도록 하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다.

```
package sidedish;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository extends PagingAndSortingRepository<Order, Long> {
}
```

> 적용 후 REST API 테스트

![image](https://user-images.githubusercontent.com/82795797/122676200-f3206780-d217-11eb-99ac-c0fa6a04590e.png)

## 폴리글랏 퍼시스턴스
> 대리점의 경우 타서비스와 다르게 HSql로 구현하여 MSA간 서로 다른 종류의 DB간에도 문제없이 동작하여 다형성을 만족하는지 확인하였다.

* pom.xml : app, pay, customer 
```
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
```
* pom.xml : store 
```
		<dependency>
			<groupId>org.hsqldb</groupId>
			<artifactId>hsqldb</artifactId>
			<scope>runtime</scope>
		</dependency>
```

## Gateway 적용
```
(Gateway) - applitcation.yml
---------- ---------- ---------- ---------- ---------- ---------- ---------- 
spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: app
          uri: http://localhost:8081
          predicates:
            - Path=/orders/** 
        - id: store
          uri: http://localhost:8082
          predicates:
            - Path=/storeManages/**
        - id: pay
          uri: http://localhost:8083
          predicates:
            - Path=/payments/** 
        - id: customer
          uri: http://localhost:8084
          predicates:
            - Path= /customers/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true
```
> (확인) Gateway(8088)

![image](https://user-images.githubusercontent.com/82795797/122676589-a9388100-d219-11eb-8de8-d43efaefad2b.png)

## 동기식 호출 과 Fallback 처리
> 분석 단계에서의 조건 중 주문(app)->결제(pay)간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 
* 결제서비스를 호출하기 위하여 FeignClient 를 이용하여 Service 대행 인터페이스(Proxy) 를 구현
```
(App) PaymentService.java
---------- ---------- ---------- ---------- ---------- ---------- ----------
package sidedish.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "pay", url = "${api.url.pay}")
public interface PaymentService {

    @RequestMapping(method = RequestMethod.POST, path = "/payments")
    public void pay(@RequestBody Payment payment);

}
```
* 주문을 받은 직후(@PostPersist) 결제를 요청하도록 처리
```
(App) - Order.java
---------- ---------- ---------- ---------- ---------- ---------- ----------
@PostPersist
    public void onPostPersist() {
        System.out.println("******************** Order ******************* ");
        setStatus("Ordered");

        Ordered ordered = new Ordered();
        ordered.setId(this.getId());
        ordered.setItem(this.getItem());
        ordered.setQty(this.getQty());
        ordered.setStatus(this.getStatus());
        BeanUtils.copyProperties(this, ordered);
        ordered.publishAfterCommit();

        sidedish.external.Payment payment = new sidedish.external.Payment();
        payment.setOrderId(this.getId());
        payment.setProcess("Ordered");

        AppApplication.applicationContext.getBean(sidedish.external.PaymentService.class)
                .pay(payment);
    }
```
* 동기식 호출이 적용되어 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인
```
1. 결제 서비스 정지
2. 주문 생성 및 확인
```
![image](https://user-images.githubusercontent.com/82795797/122676945-1e588600-d21b-11eb-9c6c-f6b4ed303164.png)

```
3. 결제 서비스 재기동
4. 주문 생성 및 확인
```
![image](https://user-images.githubusercontent.com/82795797/122676689-10563580-d21a-11eb-8695-6eec935cb341.png)

* 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다.(서킷브레이커, 폴백처리는 운영단계에서 설명한다.)

## 비동기식 호출 / 시간적 디커플링 / 장애격리
> 결제(pay)가 이루어진 후에 대리점(store)으로 이를 알려주는 행위는 비동기식으로 처리하여 대리점(store)의 업무처리를 위하여 결제주문이 블로킹 되지 않도록 처리한다.
* 결제완료(payCompleted) 이벤트를 카프카로 송신한다(Publish)
```
(Pay) - Payment.java
---------- ---------- ---------- ---------- ---------- ---------- ----------
    @PrePersist
    public void onPrePersist() {
        System.out.println("***** 결재 요청 *****");

        if ("Ordered".equals(process)) {
            System.out.println("***** 결재 진행 *****");
            setProcess("Payed");
            PayCompleted payCompleted = new PayCompleted();
            BeanUtils.copyProperties(this, payCompleted);
            payCompleted.publishAfterCommit();
            System.out.println(toString());
            System.out.println("***** 결재 완료 *****");
```

* 대리점(store)은 주문(app)/결제(pay)와 완전히 분리되어 있으며, 이벤트 수신에 따라 처리되기 때문에, 유지보수를 위해 서비스가 잠시 중단되어도 주문 수신에 문제가 없다.
```
1. 대리점 서비스 정지
2. 주문 생성 및 확인
3. 대리점 서비스 재시작
4. 주문 확인(status : 'Payed' -> 'Shipped')
```
![image](https://user-images.githubusercontent.com/82795797/122676810-93778b80-d21a-11eb-8be8-b03b2b591fd1.png)

## SAGA / Correlation
> 대리점(store) 시스템에서 결제완료를 확인하면 주문의 상태가 갱신된다.
* 결제완료(payCompleted)를 수신하여 자신의 정책을 처리하도록 PolicyHandler를 구현
```
(Store) - PolicyHandler.java
---------- ---------- ---------- ---------- ---------- ---------- ----------
@Service
public class PolicyHandler {

    @Autowired
    StoreManageRepository storeManageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString) {
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPayCompleted_OrderReceive(@Payload PayCompleted payCompleted) {

        if (payCompleted.isMe()) {
            System.out.println("##### listener OrderReceive : " + payCompleted.toJson());
            System.out.println("store_policy_paycompleted_orderreceive");

            StoreManage storeManage = new StoreManage();
            storeManage.setOrderId(payCompleted.getOrderId());
            storeManage.setProcess("Payed");
            storeManageRepository.save(storeManage);
        }
    }
}
```

## CQRS
> 상태정보가 변경될때마다 event를 수신하여 조회하도록 별도의 View(Customer 서비스)를 구현하여 명령과 조회를 분리하였다.
```
(Customer) - CustomerViewHandler.java
---------- ---------- ---------- ---------- ---------- ---------- ----------
@Service
public class CustomerViewHandler {

    @Autowired
    private CustomerRepository customerRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrdered_then_CREATE_1(@Payload Ordered ordered) {
        try {
            if (ordered.isMe()) {
                // view 객체 생성
                Customer customer = new Customer();
                // view 객체에 이벤트의 Value 를 set 함
                customer.setOrderId(ordered.getId());
                customer.setItem(ordered.getItem());
                customer.setQty(ordered.getQty());
                customer.setPrice(ordered.getPrice());
                customer.setStatus(ordered.getStatus());
                // view 레파지 토리에 save
                customerRepository.save(customer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayCompleted_then_UPDATE_1(@Payload PayCompleted payCompleted) {
        try {
            if (payCompleted.isMe()) {
                // view 객체 조회
                List<Customer> customerList = customerRepository.findByOrderId(payCompleted.getOrderId());
                for (Customer customer : customerList) {
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    customer.setStatus(payCompleted.getProcess());
                    // view 레파지 토리에 save
                    customerRepository.save(customer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenShipped_then_UPDATE_2(@Payload Shipped shipped) {
        try {
            if (shipped.isMe()) {
                // view 객체 조회
                List<Customer> customerList = customerRepository.findByOrderId(shipped.getOrderId());
                for (Customer customer : customerList) {
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    customer.setStatus(shipped.getProcess());
                    // view 레파지 토리에 save
                    customerRepository.save(customer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderCancelled_then_UPDATE_3(@Payload OrderCancelled orderCancelled) {
        try {
            if (orderCancelled.isMe()) {
                // view 객체 조회
                List<Customer> customerList = customerRepository.findByOrderId(orderCancelled.getId());
                for (Customer customer : customerList) {
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    customer.setStatus(orderCancelled.getStatus());
                    // view 레파지 토리에 save
                    customerRepository.save(customer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenPayCancelled_then_UPDATE_4(@Payload PayCancelled payCancelled) {
        try {
            if (payCancelled.isMe()) {
                // view 객체 조회
                List<Customer> customerList = customerRepository.findByOrderId(payCancelled.getOrderId());
                for (Customer customer : customerList) {
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    customer.setStatus(payCancelled.getProcess());
                    // view 레파지 토리에 save
                    customerRepository.save(customer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
![image](https://user-images.githubusercontent.com/82795797/122882917-a05dc180-d377-11eb-8b6e-b4bbf816d6ef.png)


# 배포 및 운영
## CI/CD 설정
> 각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 Azure를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하에 Dockerfile 과 deployment.yml/service.yaml 에 포함되었다.

![image](https://user-images.githubusercontent.com/82795797/122677895-4e098d00-d21f-11eb-9b03-d564eb36e610.png)

![image](https://user-images.githubusercontent.com/82795797/122677409-14378700-d21d-11eb-9776-cd55d4ee40d9.png)

## 동기식 호출 / 서킷 브레이킹 / 장애격리
* 서킷 브레이킹 프레임워크 : Spring FeignClient + Hystrix 옵션을 사용하여 구현함
```
(App) - PaymentService.java
---------- ---------- ---------- ---------- ---------- ---------- ----------
package sidedish.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "pay", url = "${api.url.pay}")
public interface PaymentService {

    @RequestMapping(method = RequestMethod.POST, path = "/payments")
    public void pay(@RequestBody Payment payment);

}
```

* 시나리오는 앱(app) -> 결제(pay) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB 를 통하여 장애격리.
* Hystrix 를 설정 : 요청처리 쓰레드에서 처리시간이 777(ms) 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 설정(요청을 빠르게 실패처리, 차단)
```
(App) - application.yml
---------- ---------- ---------- ---------- ---------- ---------- ----------
feign:
  hystrix:
    enabled: true
    
hystrix:
  커맨드:
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 777
```

> 검증 및 테스트
* 부하테스터(Siege툴)을 통한 서킷 브레이커 동작 확인(동시사용자 100명, 60초)
* 부하 발생하여 CB가 발동하여 요청 실패처리하였고, 밀린 부하가 Pay에서 처리되면서 다시 주문을 받기 시작 
* 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 하지만, 93.98% 가 성공하였고 약6%가 실패했다는 것은 고객 사용성에 있어 좋지 않기 때문에 Retry 설정과 동적 스케일아웃(replica의 자동적 추가, HPA)을 통하여 시스템을 확장 해주는 후속처리가 필요.

![image](https://user-images.githubusercontent.com/82795797/122677600-f880b080-d21d-11eb-8046-6015b32986e2.png)

### 오토 스케일 아웃
> 앞서 CB는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 이에 replica를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:

```
          resources:
            limits:
              cpu: 500m
            requests:
              cpu: 200m
```
```
$ kubectl autoscale deploy app --min=1 --max=10 --cpu-percent=15 -n cnatest
```

> CB 에서 했던 방식대로 워크로드를 2분 동안 걸어준다.
* 어느정도 시간이 흐른 후 스케일 아웃이 벌어지는 것을 확인
* 전체적으로 주문 처리율(99.22%)이 증가함.

![image](https://user-images.githubusercontent.com/82795797/122678805-40ee9d00-d223-11eb-90c2-8af23b70a14e.png)

## 무정지 재배포
> 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함.
 readiness 옵션이 없는 경우 배포 중 서비스 요청처리 실패
* readiness 옵션이 추가된 deployment.yml을 적용
```
(App) - deployment.yml
---------- ---------- ---------- ---------- ---------- ---------- ----------
    spec:
      containers:
        - name: app
          image: skcc10130acr.azurecr.io/app:v1
          imagePullPolicy: Always
          ports:
            - containerPort: 8080
          env:
            - name: api.url.pay
              valueFrom:
                configMapKeyRef:
                  name: sidedish-config
                  key: api.url.pay
          readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 10
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 10
```

* 기존 버전과 새 버전의 catch pod 공존 중

![image](https://user-images.githubusercontent.com/82795797/122678250-ef451300-d220-11eb-911f-ce7afe24cbc0.png)

* Availability : 100% 확인

![image](https://user-images.githubusercontent.com/82795797/122678212-c290fb80-d220-11eb-8a48-1ff2e449cae3.png)

## Config Map
```
(App) - application.yml
---------- ---------- ---------- ---------- ---------- ---------- ----------
api:
  url:
    pay: ${configmapurl}

(App) - deployment.yml
---------- ---------- ---------- ---------- ---------- ---------- ----------
          env:
            - name: configmapurl
              valueFrom:
                configMapKeyRef:
                  name: apiurl
                  key: url

(적용) configmap.yaml
---------- ---------- ---------- ---------- ---------- ---------- ----------
apiVersion: v1
kind: ConfigMap
metadata:
  name: sidedish-config
  namespace: cnatest
data:
  api.url.pay: http://pay:8080
```
![image](https://user-images.githubusercontent.com/82795797/122678062-118a6100-d220-11eb-9a3c-9db31ef91207.png)

## Self-healing
> Liveness Probe 적용
* deploy 재배포 후 liveness 가 적용된 부분 확인(App 서비스의 liveness가 발동되어 5번 retry 시도)
```
(App) - deployment.yml
---------- ---------- ---------- ---------- ---------- ---------- ---------- 
          livenessProbe:
            tcpSocket:
              port: 8081
            initialDelaySeconds: 5
            periodSeconds: 5
```

![image](https://user-images.githubusercontent.com/82795797/122678118-5c0bdd80-d220-11eb-8b97-d1d2f0bab427.png)

감사합니다.
