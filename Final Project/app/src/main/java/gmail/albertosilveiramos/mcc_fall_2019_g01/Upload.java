package gmail.albertosilveiramos.mcc_fall_2019_g01;


import android.app.Application;

public class Upload extends Application {
    private String mname;
    private String mimageUrl;


    public Upload(String name, String imageUrl){
        if (name.trim().equals("")){
            name = "No name";
        }

        mname = name;
        mimageUrl = imageUrl;

    }

    public String getName(){
        return mname;
    }

    public void setName(String name){
        this.mname = name;
    }

    public String getImageUrl (){
        return mimageUrl;
    }

    public void setImageUrl(String imageUrl){
        this.mimageUrl = imageUrl;
    }


}
