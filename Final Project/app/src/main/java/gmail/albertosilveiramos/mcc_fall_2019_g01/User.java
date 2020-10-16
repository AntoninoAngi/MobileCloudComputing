package gmail.albertosilveiramos.mcc_fall_2019_g01;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;

public class User implements Parcelable {


    private String ID;
    private String image;
    private String username;
    private String email;

    private ArrayList<Project>favoriteList;

    private  ArrayList<String>favouriteProjIDs;
    public User(String username, String email){
        this.username=username;
        this.email=email;
        favoriteList = new ArrayList<>();
    }
    public  User(String ID , String username, String email, String image, ArrayList<String>favouriteProjIDs){
        this(username, email);
        this.ID = ID;
        this.image = image;
        favoriteList = new ArrayList<>();
        this.favouriteProjIDs = favouriteProjIDs;

    }


    public void setFavoriteList (ArrayList<Project> favoriteList ){
        for (Project project: favoriteList){
            Project isAlreadyRegistered = null;
            for (Project fav : this.favoriteList){
                if(fav.getProjectId().equals(project.getProjectId())){
                    isAlreadyRegistered = fav;
                }
            }
            if(isAlreadyRegistered != null){
                this.favoriteList.remove(isAlreadyRegistered);
                project.setFavoriteButtonState(true);
                this.favoriteList.add(project);

            }

            if(favouriteProjIDs.contains(project.getProjectId() )&& isAlreadyRegistered == null){

                project.setFavoriteButtonState(true);
                this.favoriteList.add(project);


            }

        }

    }

    public void UpdateUserFavorites(){


        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        DatabaseReference usersdRef = rootRef.child("users").child(this.getUserID());

        ValueEventListener eventListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {

                Iterable<DataSnapshot>s = dataSnapshot.child("favourites").getChildren();
                ArrayList<String> favourites = new ArrayList<>();
                for(DataSnapshot sub : s){
                    String key = (sub.getKey().substring(1));
                    favourites.add(key);
                    Project reference = null;
                    for (Project fav : favoriteList){
                        if(fav.getProjectId().equals(key)){
                           reference = fav;
                        }
                    }
                    if(reference!= null){
                        favoriteList.remove(reference);
                    }
                }
                favouriteProjIDs = favourites;


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };



    }

    public void RemoveFavourites(Project project){


        favouriteProjIDs.remove(project.getProjectId());
        this.favoriteList.remove(project);
      DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
      rootRef.child("users").child(getUserID()).child("favourites").child("-"+project.getProjectId()).removeValue();
    }

    public void AddFavourites(Project project){


        Project reference = null;
        for (Project fav : favoriteList){
            if(fav.getProjectId().equals(project.getProjectId())){
                reference = fav;
            }
        }
        if(reference== null){
            favouriteProjIDs.add(project.getProjectId());
            favoriteList.add(project);

        }

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("users").child(getUserID()).child("favourites").child("-"+project.getProjectId()).setValue(true);
    }


    public ArrayList<Project> getFavouriteList(){
        return  this.favoriteList;
    }

    public  ArrayList<String> getFavouriteProjIDs(){
        return  this.favouriteProjIDs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(ID);
        out.writeString(username);
        out.writeString(email);
        out.writeString(image);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private User(Parcel in) {
        ID = in.readString();
        username = in.readString();
        email = in.readString();
        image = in.readString();
    }




    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getUserID(){return  ID;};

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JSONObject ToJson()
    {
        JSONObject request = null;
        try{
            request = new JSONObject();
            request.put("ID", this.ID);
            request.put("username", this.getUsername());
            request.put("email", this.getEmail());
        }
        catch (Exception ex)
        {
            Log.d(this.getClass().toString(), ex.getMessage().toString());
        }

        return request;
    }



    public static User getUserFromDataFrame(DataSnapshot ds){

        String ID = ds.getKey();
        // encode image later
        String image = ds.child("image").getValue(String.class);
        String name = ds.child("username").getValue(String.class);
        String email = ds.child("email").getValue(String.class);
        Iterable<DataSnapshot>s = ds.child("favourites").getChildren();
        ArrayList<String> favourites = new ArrayList<>();
        for(DataSnapshot sub : s){
            favourites.add(sub.getKey().substring(1));
        }

        User user = new User(ID, name, email, image, favourites);
        return user;

    }

    public static  boolean Compare(ArrayList<User> compare_list, User user){
        boolean result = false;
        for(User filter:compare_list){

            if(filter.getUserID().equals(user.getUserID()))result = true;
        }
        return result;

    }

    public static ArrayList<User> merge(ArrayList<User>oldusers, ArrayList<User>newusers){

        ArrayList<User>mege = (ArrayList<User >)oldusers.clone();
        for (User newuser :newusers)
        {
            boolean isInside = false;
            for (User olduser : oldusers){
                if(newuser.getUserID().equals(olduser.getUserID()))isInside= true;
            }
            if (!isInside){
                mege.add(newuser);
            }
        }
        return mege;


    }
}
