package amosalexa.services.transfertemplates;

import api.DynamoDbClient;
import api.DynamoDbStorable;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class TransferTemplate implements Comparable<TransferTemplate>, DynamoDbStorable {
    protected int id;
    protected String target;
    protected double amount;
    protected Date createdAt;

    public static Factory factory = (Factory<TransferTemplate>) TransferTemplate::new;
    public static final String TABLE_NAME = "transfer_template";

    private TransferTemplate() {
    }

    private TransferTemplate(String target, double amount) {
        this.target = target;
        this.amount = amount;
        this.createdAt = new Date();
    }

    public TransferTemplate(int id) {
        this.id = id;
    }

    public static TransferTemplate make(String target, double amount) {
        TransferTemplate transferTemplate = new TransferTemplate(target, amount);
        DynamoDbClient.instance.putItem(TABLE_NAME, transferTemplate);
        return transferTemplate;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public String getTarget() {
        return target;
    }

    public double getAmount() {
        return amount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TransferTemplate)) {
            return false;
        }

        TransferTemplate tt = (TransferTemplate) obj;

        return this == tt ||
                (id == tt.id && amount == tt.amount && target.equals(tt.target) && createdAt.equals(tt.createdAt));
    }

    @Override
    public int compareTo(TransferTemplate o) {
        if (id > o.id) {
            return 1;
        }
        if (id < o.id) {
            return -1;
        }
        return 0;
    }

    @Override
    public Map<String, AttributeValue> getDynamoDbItem() {
        Map<String, AttributeValue> map = new TreeMap<>();

        map.put("id", new AttributeValue().withN(Integer.toString(this.id)));
        map.put("target", new AttributeValue(this.target));
        map.put("amount", new AttributeValue().withN(Double.toString(this.amount)));
        map.put("createdAt", new AttributeValue().withN(Long.toString(this.createdAt.getTime())));

        return map;
    }

    @Override
    public Map<String, AttributeValue> getDynamoDbKey() {
        Map<String, AttributeValue> map = new TreeMap<>();
        map.put("id", new AttributeValue().withN(Integer.toString(this.id)));
        return map;
    }

    @Override
    public void setDynamoDbAttribute(String attributeName, AttributeValue attributeValue) {
        switch (attributeName) {
            case "id":
                this.id = Integer.parseInt(attributeValue.getN());
                break;
            case "target":
                this.target = attributeValue.getS();
                break;
            case "amount":
                this.amount = Double.parseDouble(attributeValue.getN());
                break;
            case "createdAt":
                this.createdAt = new Date(Long.parseLong(attributeValue.getN()));
                break;
            default:
                throw new RuntimeException("Unknown attribute");
        }
    }

}