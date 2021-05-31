package sidedish;

import org.springframework.beans.BeanUtils;

import javax.persistence.*;

@Entity
@Table(name = "Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String store;
    private String item;
    private Double price;
    private Integer qty;
    private String status;

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

    @PostUpdate
    public void onPostUpdate() {
    }

    @PostRemove
    public void onPostRemove() {
        this.setStatus("OrderCancelled");
        OrderCancelled orderCancelled = new OrderCancelled();
        BeanUtils.copyProperties(this, orderCancelled);
        orderCancelled.publish();
    }

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

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", item='" + item + '\'' +
                ", qty=" + qty +
                ", status='" + status + '\'' +
                ", store='" + store + '\'' +
                ", price=" + price +
                '}';
    }
}