# 서비스 시나리오
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

## AS-IS 조직 (Horizontally-Aligned)
  ![image](https://user-images.githubusercontent.com/82795797/122673113-f14fa780-d209-11eb-9bf8-e08ef5d8e958.png)

## TO-BE 조직 (Vertically-Aligned)
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
> 괄호는 수행주체, 폴리시 부착을 둘째단계에서 해놔도 상관 없음. 전체 연계가 초기에 드러남
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
* 대리점의 경우 Polyglot 검증을 위해 Hsql로 셜계

![image](https://user-images.githubusercontent.com/82795797/122673409-49d37480-d20b-11eb-82ae-50aa63ab6094.png)

# 구현

## DDD 적용
> 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 app 마이크로서비스). 
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

> Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다.

```
package sidedish;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface OrderRepository extends PagingAndSortingRepository<Order, Long> {
}
```

> 적용 후 REST API 테스트


```
C:\>http POST http://localhost:8081/orders item=sidedish1 qty=1 price=11111 store=1

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/1"
        },
        "self": {
            "href": "http://localhost:8081/orders/1"
        }
    },
    "item": "sidedish1",
    "price": 11111.0,
    "qty": 1,
    "status": "Ordered",
    "store": "1"
}
```
```
C:\>http GET http://localhost:8081/orders/1

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/1"
        },
        "self": {
            "href": "http://localhost:8081/orders/1"
        }
    },
    "item": "sidedish1",
    "price": 11111.0,
    "qty": 1,
    "status": "Shipped",
    "store": "1"
}
```

## 폴리글랏 퍼시스턴스
> 대리점의 경우 타서비스와 다르게 hsqldb로 구현하여 MSA간 서로 다른 종류의 DB간에도 문제없이 동작하여 다형성을 만족하는지 확인하였다.

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
> (설정) gateway > applitcation.yml
```
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
> (확인) gateway(8088)
```
C:\>http POST http://localhost:8088/orders item=sidedish1 qty=2 price=22222 store=2

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/2"
        },
        "self": {
            "href": "http://localhost:8081/orders/2"
        }
    },
    "item": "sidedish1",
    "price": 22222.0,
    "qty": 2,
    "status": "Ordered",
    "store": "2"
}
```
```
C:\>http GET http://localhost:8088/orders/2

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/2"
        },
        "self": {
            "href": "http://localhost:8081/orders/2"
        }
    },
    "item": "sidedish1",
    "price": 22222.0,
    "qty": 2,
    "status": "Shipped",
    "store": "2"
}
```

## 동기식 호출 과 Fallback 처리
> 분석 단계에서의 조건 중 주문(app)->결제(pay)간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 
* 결제서비스를 호출하기 위하여 FeignClient 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현
```
* 구현 : (app) PaymentService.java
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
* 구현 : (app) Order.java
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
---------- ---------- ---------- ---------- ---------- ---------- ----------
2. 주문 생성 및 확인
---------- ---------- ---------- ---------- ---------- ---------- ----------
C:\>http POST http://localhost:8088/orders item=sidedish1 qty=3 price=33333 store=3

{
    "error": "Internal Server Error",
    "message": "Could not commit JPA transaction; nested exception is javax.persistence.RollbackException: Error while committing the transaction",
    "path": "/orders",
    "status": 500,
    "timestamp": "2021-05-29T03:04:00.284+0000"
}
---------- ---------- ---------- ---------- ---------- ---------- ----------
3. 결제 서비스 재기동
---------- ---------- ---------- ---------- ---------- ---------- ----------
4. 주문 생성 및 확인
---------- ---------- ---------- ---------- ---------- ---------- ----------
C:\>http POST http://localhost:8088/orders item=sidedish1 qty=3 price=33333 store=3

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/4"
        },
        "self": {
            "href": "http://localhost:8081/orders/4"
        }
    },
    "item": "sidedish1",
    "price": 33333.0,
    "qty": 3,
    "status": "Ordered",
    "store": "3"
}

C:\>http GET http://localhost:8081/orders/4

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/4"
        },
        "self": {
            "href": "http://localhost:8081/orders/4"
        }
    },
    "item": "sidedish1",
    "price": 33333.0,
    "qty": 3,
    "status": "Shipped",
    "store": "3"
}
```
* 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다.(서킷브레이커, 폴백처리는 운영단계에서 설명한다.)

## 비동기식 호출 / 시간적 디커플링 / 장애격리
> 결제(pay)가 이루어진 후에 대리점(store)으로 이를 알려주는 행위는 비동기식으로 처리하여 대리점(store)의 업무처리를 위하여 결제주문이 블로킹 되지 않도록 처리한다.
* 결제완료(payCompleted) 이벤트를 카프카로 송신한다(Publish)
```
* 구현 : (pay) Payment.java
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
* 대리점(store)에서는 결제완료(payCompleted)를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다.
```
* 구현 : (store) PolicyHandler.java
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
* 대리점(store)은 주문(app)/결제(pay)와 완전히 분리되어 있으며, 이벤트 수신에 따라 처리되기 때문에, 유지보수를 위해 서비스가 잠시 중단되어도 주문 수신에 문제가 없다.
```
1. 대리점 서비스 정지
---------- ---------- ---------- ---------- ---------- ---------- ----------
2. 주문 생성 및 확인
---------- ---------- ---------- ---------- ---------- ---------- ----------
C:\>http POST http://localhost:8088/orders item=sidedish1 qty=4 price=44444 store=4

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/5"
        },
        "self": {
            "href": "http://localhost:8081/orders/5"
        }
    },
    "item": "sidedish1",
    "price": 44444.0,
    "qty": 4,
    "status": "Ordered",
    "store": "4"
}

C:\>http GET http://localhost:8081/orders/5

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/5"
        },
        "self": {
            "href": "http://localhost:8081/orders/5"
        }
    },
    "item": "sidedish1",
    "price": 44444.0,
    "qty": 4,
    "status": "Payed",
    "store": "4"
}
---------- ---------- ---------- ---------- ---------- ---------- ----------
3. 대리점 서비스 재시작
---------- ---------- ---------- ---------- ---------- ---------- ----------
4. 주문 확인(status : 'Payed' -> 'Shipped')
---------- ---------- ---------- ---------- ---------- ---------- ----------
C:\>http GET http://localhost:8081/orders/5

{
    "_links": {
        "order": {
            "href": "http://localhost:8081/orders/5"
        },
        "self": {
            "href": "http://localhost:8081/orders/5"
        }
    },
    "item": "sidedish1",
    "price": 44444.0,
    "qty": 4,
    "status": "Shipped",
    "store": "4"
}
```


# 운영

## Deploy / Pipeline

> 소스 가져오기
```
$ git clone https://github.com/skcc10130/sidedish.git
```
<<< 캡쳐 >>>

> 빌드하기
```
$ cd sidedish
$ cd app
$ mvn package -Dmaven.test.skip=true
-> app / customer / gateway / pay / store 모두 위의 과정 수행
```
<<< 캡쳐 >>>

> 네임스페이스 만들기
```
$ kubectl create ns cnatest
```

- 컨테이너 생성 및 확인
```
$ kubectl create deploy app --image=skcc10130acr.azurecr.io/app:v1 -n cnatest
$ kubectl create deploy customer --image=skcc10130acr.azurecr.io/customer:v1 -n cnatest
$ kubectl create deploy gateway --image=skcc10130acr.azurecr.io/gateway:v1 -n cnatest
$ kubectl create deploy pay --image=skcc10130acr.azurecr.io/pay:v1 -n cnatest
$ kubectl create deploy store --image=skcc10130acr.azurecr.io/store:v1 -n cnatest
 
$ kubectl expose deploy app --type="ClusterIP" --port=8080 -n cnatest
$ kubectl expose deploy customer --type="ClusterIP" --port=8080 -n cnatest
$ kubectl expose deploy gateway --type="ClusterIP" --type=LoadBalancer --port=8080 -n cnatest
$ kubectl expose deploy pay --type="ClusterIP" --port=8080 -n cnatest
$ kubectl expose deploy store --type="ClusterIP" --port=8080 -n cnatest

$ kubectl get pod, service -n cnatest
```
<<< 캡쳐 >>>
```
$ http POST http://20.194.22.52:8080/orders item=sidedish1 price=11111 qty=1 store=1
$ http GET http://20.194.22.52:8080/orders/2
```
<<< 캡쳐 >>>

## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 앱(app)-->결제(pay) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 777 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```
# application.yml
feign:
  hystrix:
    enabled: true
    
hystrix:
  커맨드:
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 777
```

* 참조. siege 설치 및 실행(명령어)
```
$ kubectl run siege --image=apexacme/siege-nginx -n cnatest
$ kubectl exec -it pod/siege -c siege -n cnatest -- /bin/bash
root@siege:/# http POST http://20.194.22.52:8080/orders item=sidedish1 price=11111 qty=1 store=1
```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

```
root@siege:/# siege -c100 -t60S -r10 -v --content-type "application/json" 'http://20.194.22.52:8080/orders POST {"item": "sidedish7", "price":"777", "qty":"7", "store":"7"}'
```
- 부하 발생하여 CB가 발동하여 요청 실패처리하였고, 밀린 부하가 pay에서 처리되면서 다시 order를 받기 시작 

<<< 캡쳐 >>>
![image](https://user-images.githubusercontent.com/73699193/98098702-07eefb80-1ed2-11eb-94bf-316df4bf682b.png)

- report
<<< 캡쳐 >>>
![image](https://user-images.githubusercontent.com/73699193/98099047-6e741980-1ed2-11eb-9c55-6fe603e52f8b.png)

- CB 잘 적용됨을 확인


### 오토스케일 아웃

- 반찬가게 시스템에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:

```
# autocale out 설정
store > deployment.yml 설정
```
![image](https://user-images.githubusercontent.com/73699193/98187434-44fbd200-1f54-11eb-9859-daf26f812788.png)

```
$ kubectl autoscale deploy app --min=1 --max=10 --cpu-percent=15 -n cnatest
```
<<< 캡쳐 >>>
![image](https://user-images.githubusercontent.com/73699193/98100149-ce1ef480-1ed3-11eb-908e-a75b669d611d.png)


-
- CB 에서 했던 방식대로 워크로드를 2분 동안 걸어준다.
```
$ kubectl exec -it pod/siege -c siege -n cnatest -- /bin/bash
root@siege:/# siege -c100 -t60S -r10 -v --content-type "application/json" 'http://20.194.22.52:8080/orders POST {"item": "sidedish7", "price":"777", "qty":"7", "store":"7"}'
```
![image](https://user-images.githubusercontent.com/73699193/98102543-0d9b1000-1ed7-11eb-9cb6-91d7996fc1fd.png)

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy app -w -n cnatest
```
- 어느정도 시간이 흐른 후 스케일 아웃이 벌어지는 것을 확인할 수 있다. max=10 
- 부하를 줄이니 늘어난 스케일이 점점 줄어들었다.
<<< 캡쳐 >>>

![image](https://user-images.githubusercontent.com/73699193/98102926-92862980-1ed7-11eb-8f19-a673d72da580.png)

- 다시 부하를 주고 확인하니 Availability가 높아진 것을 확인 할 수 있었다.

![image](https://user-images.githubusercontent.com/73699193/98103249-14765280-1ed8-11eb-8c7c-9ea1c67e03cf.png)


## 무정지 재배포
* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함
- seige 로 배포작업 직전에 워크로드를 모니터링 함.

```
$ kubectl apply -f catch/kubernetes/deployment_readiness.yml
```
```
$ kubectl exec -it pod/siege -c siege -n cnatest -- /bin/bash
root@siege:/# siege -c50 -t120S -r10 -v --content-type "application/json" 'http://52.231.14.253:8080/orders POST {"item": "sidedish7", "price":"777", "qty":"7", "store":"7"}'
```

* readiness 옵션이 없는 경우 배포 중 서비스 요청처리 실패

![image](https://user-images.githubusercontent.com/11955597/120112926-014c0c80-c1b3-11eb-93c3-f418209b69c8.png)


* readiness 옵션이 추가된 deployment.yml을 적용
```
$ kubectl apply -f catch/kubernetes/deployment.yml
```
![image](https://user-images.githubusercontent.com/11955597/120112818-7c60f300-c1b2-11eb-951b-1514648b01ac.png)


* 새버전으로의 배포 시작
```
kubectl set image deploy catch catch=cnateam4.azurecr.io/catch:v2 -n default
```

* 기존 버전과 새 버전의 catch pod 공존 중

![image](https://user-images.githubusercontent.com/11955597/120113839-525dff80-c1b7-11eb-97ec-6ff76ae07b4c.png)

* Availability : 100% 확인

![image](https://user-images.githubusercontent.com/11955597/120113867-6f92ce00-c1b7-11eb-848c-e773d98f9ea4.png)


## Config Map
* application.yml 설정 (catch 서비스)

- default 부분

![image](https://user-images.githubusercontent.com/11955597/120114255-45daa680-c1b9-11eb-9d09-d3417b5df42f.png)

- docker 부분

![image](https://user-images.githubusercontent.com/11955597/120114260-525eff00-c1b9-11eb-9bea-c95c0ae59d05.png)

* deployment.yml 설정 (catch 서비스)

![image](https://user-images.githubusercontent.com/11955597/120114310-94884080-c1b9-11eb-8d10-8f5205b1f500.png)

* config map 생성 후 조회
```
$ kubectl create configmap apiurl --from-literal=url=http://payment:8080 --from-literal=fluentd-server-ip=10.xxx.xxx.xxx -n default
```
![image](https://user-images.githubusercontent.com/11955597/120114378-edf06f80-c1b9-11eb-8585-dac97058bb1b.png)

* 설정한 URL로 택시요청 호출
```
http POST http://catch:8080/catches price=250000 startingPoint=Busan destination=Seoul customer=Peter  status=approve
```
![image](https://user-images.githubusercontent.com/11955597/120114562-a3bbbe00-c1ba-11eb-8c04-4c52713c4abd.png)

* config map 삭제 후 catch 서비스 재시작
```
$ kubectl delete configmap apiurl -n default
$ kubectl get pod/catch-574665c7bc-n6dn2 -n default -o yaml | kubectl replace --force -f- 
```
![image](https://user-images.githubusercontent.com/11955597/120114992-b931e780-c1bc-11eb-888b-887d8f16027c.png)

![image](https://user-images.githubusercontent.com/11955597/120115006-c6e76d00-c1bc-11eb-9200-2dd35d2f9ed5.png)

* config map 삭제된 상태에서 주문 호출
```
http POST http://catch:8080/catches price=250000 startingPoint=Busan destination=Seoul customer=Peter  status=approve
```
![image](https://user-images.githubusercontent.com/11955597/120115086-175eca80-c1bd-11eb-9653-31213696b36e.png)

![image](https://user-images.githubusercontent.com/11955597/120115125-44ab7880-c1bd-11eb-82ff-496c62732d08.png)

```
$ kubectl get pod/catch-574665c7bc-z2tzj -o yaml
$ kubectl describe pod/catch-574665c7bc-z2tzj
```
![image](https://user-images.githubusercontent.com/11955597/120115219-a23fc500-c1bd-11eb-84ec-f8acf0fd2bf3.png)
![image](https://user-images.githubusercontent.com/11955597/120115282-ecc14180-c1bd-11eb-947e-179722c287b9.png)


## Self-healing (Liveness Probe)

* catch 서비스 정상 확인

![image](https://user-images.githubusercontent.com/11955597/120116102-71fa2580-c1c1-11eb-8ca0-08adf9f6a34d.png)

* deployment.yml (catch 서비스)에 Liveness Probe 옵션 추가
```
          livenessProbe:
            tcpSocket:
              port: 8081
            initialDelaySeconds: 5
            periodSeconds: 5
```
![image](https://user-images.githubusercontent.com/11955597/120116153-b1287680-c1c1-11eb-992d-db264a3c1f86.png)

* catch deploy 재배포 후 liveness 가 적용된 부분 확인

![image](https://user-images.githubusercontent.com/11955597/120116296-7c68ef00-c1c2-11eb-8d32-eeadd9eb555d.png)


* catch 서비스의 liveness 가 발동되어 5번 retry 시도한 부분 확인

![image](https://user-images.githubusercontent.com/11955597/120116360-c2be4e00-c1c2-11eb-9e28-04d84b06f6bd.png)



# --------------------------------이하 수정 필요
# 신규 개발 조직의 추가
  ![image](https://user-images.githubusercontent.com/487999/79684133-1d6c4300-826a-11ea-94a2-602e61814ebf.png)


## 마케팅팀의 추가
    - KPI: 신규 고객의 유입률 증대와 기존 고객의 충성도 향상
    - 구현계획 마이크로 서비스: 기존 customer 마이크로 서비스를 인수하며, 고객에 음식 및 맛집 추천 서비스 등을 제공할 예정

## 이벤트 스토밍 
  ![image](https://user-images.githubusercontent.com/487999/79685356-2b729180-8273-11ea-9361-a434065f2249.png)


## 헥사고날 아키텍처 변화 

![image](https://user-images.githubusercontent.com/487999/79685243-1d704100-8272-11ea-8ef6-f4869c509996.png)

## 구현  

기존의 마이크로 서비스에 수정을 발생시키지 않도록 Inbund 요청을 REST 가 아닌 Event 를 Subscribe 하는 방식으로 구현. 기존 마이크로 서비스에 대하여 아키텍처나 기존 마이크로 서비스들의 데이터베이스 구조와 관계없이 추가됨. 

## 운영과 Retirement

Request/Response 방식으로 구현하지 않았기 때문에 서비스가 더이상 불필요해져도 Deployment 에서 제거되면 기존 마이크로 서비스에 어떤 영향도 주지 않음.

* [비교] 결제 (pay) 마이크로서비스의 경우 API 변화나 Retire 시에 app(주문) 마이크로 서비스의 변경을 초래함:

예) API 변화시
```
# Order.java (Entity)

    @PostPersist
    public void onPostPersist(){

        fooddelivery.external.결제이력 pay = new fooddelivery.external.결제이력();
        pay.setOrderId(getOrderId());
        
        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제(pay);

                --> 

        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제2(pay);

    }
```

예) Retire 시
```
# Order.java (Entity)

    @PostPersist
    public void onPostPersist(){

        /**
        fooddelivery.external.결제이력 pay = new fooddelivery.external.결제이력();
        pay.setOrderId(getOrderId());
        
        Application.applicationContext.getBean(fooddelivery.external.결제이력Service.class)
                .결제(pay);

        **/
    }
```


![image](https://user-images.githubusercontent.com/45971330/118398020-b948c800-b691-11eb-84f0-f69f29b29017.png)



