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
  ![image](https://user-images.githubusercontent.com/487999/79684144-2a893200-826a-11ea-9a01-79927d3a0107.png)
  

## TO-BE 조직 (Vertically-Aligned)
  ![image](https://user-images.githubusercontent.com/487999/79684159-3543c700-826a-11ea-8d5f-a3fc0c4cad87.png)

## Event Storming 결과

### 이벤트 도출

![image](https://user-images.githubusercontent.com/70673885/97949704-dc87e600-1dd7-11eb-9525-544b2411cc51.png)

### 부적격 이벤트 탈락
> 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함.
* 계획된 사업(프로젝트) 범위에서 제외함( “회원 가입”, “메시지 발송됨”, “배송 취소됨” )
* UI의 이벤트로 제외함( “반찬 선택됨”, “주문수량 선택됨”, “결제버튼 클릭됨” )
* 이벤트의 의미가 동일함( “대리점에 주문정보가 전달됨” = “주문 완료됨” )

![image](https://user-images.githubusercontent.com/70673885/97949767-0a6d2a80-1dd8-11eb-8c2f-fa445fa61418.png)

### 액터, 커맨드 부착하여 읽기 좋게
![image](https://user-images.githubusercontent.com/73699193/97982030-82f2dc00-1e16-11eb-821d-27351387f8ad.png)

### 어그리게잇으로 묶기
> 어그리게잇을 생성하고, 그와 연결된 커맨드와 이벤트들에 의해 트랜잭션이 유지되어야 하는 단위로 묶어줌.

![image](https://user-images.githubusercontent.com/73699193/97982108-a158d780-1e16-11eb-9270-6e9646268fd1.png)

### 바운디드 컨텍스트로 묶기
> 도메인 서열 분리
* Core ( app, store ), 핵심서비스 : SLA 수준은 Up-time 99.9% 목표, 배포주기는 app(1주, 1회 미만) / store(1개월, 1회 미만)
* Supporting ( customer ), 경쟁력서비스 : SLA 수준은 Up-time 60.0% 목표, 배포주기는 1주 1회 이상
* General ( pay ), 일반서비스 : 외부서비스를 사용

![image](https://user-images.githubusercontent.com/73699193/97982213-c77e7780-1e16-11eb-87ef-03dbe66a6cf2.png)

### 폴리시 부착
> 괄호는 수행주체, 폴리시 부착을 둘째단계에서 해놔도 상관 없음. 전체 연계가 초기에 드러남

![image](https://user-images.githubusercontent.com/73699193/97982278-e3821900-1e16-11eb-97f4-fa2f59fc7ae0.png)

### 폴리시의 이동

![image](https://user-images.githubusercontent.com/73699193/97982413-19bf9880-1e17-11eb-9720-cd82cf1060ff.png)

### 컨텍스트 매핑
> 점선은 Pub/Sub, 실선은 Req/Resp

![image](https://user-images.githubusercontent.com/73699193/97982527-45428300-1e17-11eb-8641-b658bab34fc6.png)

### 완성된 모형

![image](https://user-images.githubusercontent.com/73699193/97982584-60ad8e00-1e17-11eb-8fb6-af87b7c6ff91.png)

### 기능적 요구사항 검증
1. 고객이 APP에서 반찬을 주문한다. (OK)
2. 고객이 결제한다. (OK)
3. 주문이 완료되면 주문내역이 대리점(반찬가게)에 전달된다. (OK)
4. 대리점에서 주문정보가 확인되면 주문한 반찬을 배달한다. (OK)
5. 고객이 배달상태를 조회할 수 있다. (OK)

![image](https://user-images.githubusercontent.com/73699193/97982759-96527700-1e17-11eb-9144-f95de1e0d01e.png)

6. 고객이 주문을 취소할 수 있다. (OK)
7. 주문이 취소되면 결제가 취소된다. (OK)
8. 고객이 결제상태를 조회할 수 있다. (OK)

![image](https://user-images.githubusercontent.com/73699193/97982841-b2eeaf00-1e17-11eb-9f09-9b74f85a96ca.png)

9. 고객이 전체적인 진행내역을 확인할 수 있다. (OK)

![image](https://user-images.githubusercontent.com/73699193/97982928-d3b70480-1e17-11eb-957e-6a9093d2a0d7.png)

### 비기능 요구사항 검증
1. 미결제가 주문은 거래가 성립되지 않아야 한다. -> Req/Res
2. 대리점관리 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다. -> Pub/Sub
3. 결제시스템이 과중되면 주문을 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다. > Circuit Breaker
4. 주문이 취소되면 '결제취소' 및 '주문정보'가 갱신되어야 한다. -> SAGA, 보상트랜젝션
5. 고객이 모든 진행내역을 조회 할 수 있도록 성능을 고려하여 별도의 View로 구성한다. -> CQRS

![image](https://user-images.githubusercontent.com/73699193/97983019-f6e1b400-1e17-11eb-86ef-d43873ccbb7d.png)

## 헥사고날 아키텍처 다이어그램 도출 (Polyglot)
* Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
* 호출관계에서 PubSub 과 Req/Resp 를 구분함
* 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐
* 대리점의 경우 Polyglot 검증을 위해 Hsql로 셜계

![image](https://user-images.githubusercontent.com/73699193/98181638-162b2f00-1f47-11eb-81af-0b71ff811e1c.png)


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

> 네임스페이스 만들기
```
$ kubectl create ns cnatest
$ kubectl get ns
```
<<< 캡쳐 >>>

> 소스 가져오기
```
$ ls
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

> 도커라이징: Azure 레지스트리에 도커 이미지 푸시하기
```
> docker build --tag skcc10130acr.azurecr.io/app:v1 .
> docker push skcc10130acr.azurecr.io/app:v1
```
<<< 캡쳐 >>>

- 컨테이너라이징: 디플로이 생성 확인
```
$ kubectl create deploy app --image=skcc10130acr.azurecr.io/app:v1 -n cnatest
$ kubectl create deploy customer --image=skcc10130acr.azurecr.io/customer:v1 -n cnatest
$ kubectl create deploy gateway --image=skcc10130acr.azurecr.io/gateway:v1 -n cnatest
$ kubectl create deploy pay --image=skcc10130acr.azurecr.io/pay:v1 -n cnatest
$ kubectl create deploy store --image=skcc10130acr.azurecr.io/store:v1 -n cnatest

$ kubectl get all -n sidedish
```
<<< 캡쳐 >>>

- 컨테이너라이징: 서비스 생성 확인
```
$ kubectl expose deploy app --type="ClusterIP" --port=8080 -n cnatest
$ kubectl expose deploy customer --type="ClusterIP" --port=8080 -n cnatest
$ kubectl expose deploy gateway --type="ClusterIP" --type=LoadBalancer --port=8080 -n cnatest
$ kubectl expose deploy pay --type="ClusterIP" --port=8080 -n cnatest
$ kubectl expose deploy store --type="ClusterIP" --port=8080 -n cnatest

$ kubectl get all -n cnatest
```
<<< 캡쳐 >>>

> deployment.yml 편집
```
namespace, image 설정
env 설정 (config Map) 
readiness 설정 (무정지 배포)
liveness 설정 (self-healing)
resource 설정 (autoscaling)
```
<<< 캡쳐 : app yaml 확인 >>>
![image](https://user-images.githubusercontent.com/73699193/98092861-8182eb80-1eca-11eb-87c5-afa22140ebad.png)

> deployment.yml로 서비스 배포 
```
$ cd yaml
$ kubectl apply -f configmap.yaml
$ kubectl apply -f gateway.yaml
$ kubectl apply -f app.yaml
$ kubectl apply -f pay.yaml
$ kubectl apply -f store.yaml
$ kubectl apply -f customer.yaml
```

## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 단말앱(app)-->결제(pay) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
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
![image](https://user-images.githubusercontent.com/73699193/98093705-a166df00-1ecb-11eb-83b5-f42e554f7ffd.png)

* 피호출 서비스(결제:pay)의 임의 부하처리 -400밀리에서 증감 220밀리 정도 왔다갔다하게..
```
# (pay) 결제이력.java (Entity)

    @PrePersist
    public void onPrePersist(){  //결제이력을 저장한 후 적당한 시간 끌기

        ...
        
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

```

* siege 툴 사용법:
```
 siege가 생성되어 있지 않으면:
 kubectl run siege --image=apexacme/siege-nginx -n cnatest
 siege 들어가기:
 kubectl exec -it pod/siege-5c7c46b788-4rn4r -c siege -n cnatest -- /bin/bash
 siege 종료:
 Ctrl + C -> exit
```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

```
siege -c100 -t60S -r10 -v --content-type "application/json" 'http://app:8080/orders POST {"item": "abc123", "qty":3}'
```
- 부하 발생하여 CB가 발동하여 요청 실패처리하였고, 밀린 부하가 pay에서 처리되면서 다시 order를 받기 시작 

![image](https://user-images.githubusercontent.com/73699193/98098702-07eefb80-1ed2-11eb-94bf-316df4bf682b.png)

- report

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
kubectl autoscale deploy store --min=1 --max=10 --cpu-percent=15 -n sidedish
```
![image](https://user-images.githubusercontent.com/73699193/98100149-ce1ef480-1ed3-11eb-908e-a75b669d611d.png)


-
- CB 에서 했던 방식대로 워크로드를 2분 동안 걸어준다.
```
kubectl exec -it pod/siege-84559fc587-kt46s -c siege -n cnatest -- /bin/bash
siege -c100 -t120S -r10 -v --content-type "application/json" 'http://store:8080/storeManages POST {"orderId":"456", "process":"Payed"}'
```
![image](https://user-images.githubusercontent.com/73699193/98102543-0d9b1000-1ed7-11eb-9cb6-91d7996fc1fd.png)

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy store -w -n sidedish
```
- 어느정도 시간이 흐른 후 스케일 아웃이 벌어지는 것을 확인할 수 있다. max=10 
- 부하를 줄이니 늘어난 스케일이 점점 줄어들었다.

![image](https://user-images.githubusercontent.com/73699193/98102926-92862980-1ed7-11eb-8f19-a673d72da580.png)

- 다시 부하를 주고 확인하니 Availability가 높아진 것을 확인 할 수 있었다.

![image](https://user-images.githubusercontent.com/73699193/98103249-14765280-1ed8-11eb-8c7c-9ea1c67e03cf.png)


## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscale 이나 CB 설정을 제거함


- seige 로 배포작업 직전에 워크로드를 모니터링 함.
```
kubectl apply -f kubernetes/deployment_readiness.yml
```
- readiness 옵션이 없는 경우 배포 중 서비스 요청처리 실패

![image](https://user-images.githubusercontent.com/73699193/98105334-2a394700-1edb-11eb-9633-f5c33c5dee9f.png)


- deployment.yml에 readiness 옵션을 추가 

![image](https://user-images.githubusercontent.com/73699193/98107176-75ecf000-1edd-11eb-88df-617c870b49fb.png)

- readiness적용된 deployment.yml 적용

```
kubectl apply -f kubernetes/deployment.yml
```
- 새로운 버전의 이미지로 교체
```
cd acr
az acr build --registry admin02 --image admin02.azurecr.io/store:v4 .
kubectl set image deploy store store=admin02.azurecr.io/store:v4 -n sidedish
```
- 기존 버전과 새 버전의 store pod 공존 중

![image](https://user-images.githubusercontent.com/73699193/98106161-65884580-1edc-11eb-9540-17a3c9bdebf3.png)

- Availability: 100.00 % 확인

![image](https://user-images.githubusercontent.com/73699193/98106524-c152ce80-1edc-11eb-8e0f-3731ca2f709d.png)



## Config Map

- apllication.yml 설정

* default쪽

![image](https://user-images.githubusercontent.com/73699193/98108335-1c85c080-1edf-11eb-9d0f-1f69e592bb1d.png)

* docker 쪽

![image](https://user-images.githubusercontent.com/73699193/98108645-ad5c9c00-1edf-11eb-8d54-487d2262e8af.png)

- Deployment.yml 설정

![image](https://user-images.githubusercontent.com/73699193/98108902-12b08d00-1ee0-11eb-8f8a-3a3ea82a635c.png)

- config map 생성 후 조회
```
kubectl create configmap apiurl --from-literal=url=http://pay:8080 --from-literal=fluentd-server-ip=10.xxx.xxx.xxx -n sidedish
```
![image](https://user-images.githubusercontent.com/73699193/98107784-5bffdd00-1ede-11eb-8da6-82dbead0d64f.png)

- 설정한 url로 주문 호출
```
http POST http://app:8080/orders item=dfdf1 qty=21
```

![image](https://user-images.githubusercontent.com/73699193/98109319-b732cf00-1ee0-11eb-9e92-ad0e26e398ec.png)

- configmap 삭제 후 app 서비스 재시작
```
kubectl delete configmap apiurl -n sidedish
kubectl get pod/app-56f677d458-5gqf2 -n sidedish -o yaml | kubectl replace --force -f-
```
![image](https://user-images.githubusercontent.com/73699193/98110005-cf571e00-1ee1-11eb-973f-2f4922f8833c.png)

- configmap 삭제된 상태에서 주문 호출   
```
http POST http://app:8080/orders item=dfdf2 qty=22
```
![image](https://user-images.githubusercontent.com/73699193/98110323-42f92b00-1ee2-11eb-90f3-fe8044085e9d.png)

![image](https://user-images.githubusercontent.com/73699193/98110445-720f9c80-1ee2-11eb-851e-adcd1f2f7851.png)

![image](https://user-images.githubusercontent.com/73699193/98110782-f4985c00-1ee2-11eb-97a7-1fed3c6b042c.png)



## Self-healing (Liveness Probe)

- store 서비스 정상 확인

![image](https://user-images.githubusercontent.com/27958588/98096336-fb1cd880-1ece-11eb-9b99-3d704cd55fd2.jpg)


- deployment.yml 에 Liveness Probe 옵션 추가
```
cd ~/sidedish/store/kubernetes
vi deployment.yml

(아래 설정 변경)
livenessProbe:
	tcpSocket:
	  port: 8081
	initialDelaySeconds: 5
	periodSeconds: 5
```
![image](https://user-images.githubusercontent.com/27958588/98096375-0839c780-1ecf-11eb-85fb-00e8252aa84a.jpg)

- store pod에 liveness가 적용된 부분 확인

![image](https://user-images.githubusercontent.com/27958588/98096393-0a9c2180-1ecf-11eb-8ac5-f6048160961d.jpg)

- store 서비스의 liveness가 발동되어 13번 retry 시도 한 부분 확인

![image](https://user-images.githubusercontent.com/27958588/98096461-20a9e200-1ecf-11eb-8b02-364162baa355.jpg)

