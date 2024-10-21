package src;

public class GoogleData {
    private String app;
    private String category;
    private float rating;

    public GoogleData(){

    }
    
    public GoogleData(String app, String category, float rating) {
        this.app = app;
        this.category = category;
        this.rating = rating;
    }

    public String getApp() {
        return app;
    }
    public void setApp(String app) {
        this.app = app;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public float getRating() {
        return rating;
    }
    public void setRating(float rating) {
        this.rating = rating;
    }

    

}
