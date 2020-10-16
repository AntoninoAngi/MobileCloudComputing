package gmail.albertosilveiramos.mcc_fall_2019_g01;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

public interface IResult {
    public void notifySuccess(String requestType, JSONObject response);
    public  void notifySuccess(String requestType, JSONArray response);
    public void notifyError(String requestType, VolleyError error);

}