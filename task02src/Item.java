package task02src;

public class Item {
    private String id;
    private String product;
    private int rating;
    private int price;

    public Item() {
    }

    public Item(String id, String product, int rating, int price) {
        this.id = id;
        this.product = product;
        this.rating = rating;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

}
