package sidedish;

import org.springframework.beans.BeanUtils;

import javax.persistence.*;

@Entity
@Table(name = "Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long orderId;
    private Double price;
    private String process;

    @PrePersist
    public void onPrePersist() {
        System.out.println("***** 결재 요청 *****");

        if ("Ordered".equals(process)) {
            System.out.println("***** 결재 진행 *****");
            setProcess("Payed");
            PayCompleted payCompleted = new PayCompleted();
            BeanUtils.copyProperties(this, payCompleted);
            payCompleted.publish();
            System.out.println(toString());
            System.out.println("***** 결재 완료 *****");

            try {
                Thread.currentThread().sleep((long) (400 + Math.random() * 220));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else if ("OrderCancelled".equals(process)) {
            System.out.println("***** 결재 취소 중 *****");
            setProcess("Pay Cancelled");
            PayCancelled payCancelled = new PayCancelled();
            BeanUtils.copyProperties(this, payCancelled);
            payCancelled.publish();
            System.out.println("***** 결재 취소 완료 *****");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", price=" + price +
                ", process='" + process + '\'' +
                '}';
    }
}
