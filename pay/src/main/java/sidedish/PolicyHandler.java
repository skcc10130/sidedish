package sidedish;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import sidedish.config.kafka.KafkaProcessor;

@Service
public class PolicyHandler {

    @Autowired
    PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString) {
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCancelled_PayCancel(@Payload OrderCancelled orderCancelled) {

        if (orderCancelled.isMe()) {
            System.out.println("##### listener PayCancelled : " + orderCancelled.toJson());
            System.out.println("pay_policy_orderCancelled_payCancel");

            Payment payment = new Payment();
            payment.setOrderId(orderCancelled.getId());
            payment.setProcess("OrderCancelled");
            paymentRepository.save(payment);
        }
    }
}
