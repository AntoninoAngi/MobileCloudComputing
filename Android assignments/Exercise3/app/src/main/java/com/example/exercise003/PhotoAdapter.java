package com.example.exercise003;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class PhotoAdapter extends BaseAdapter {

    Context context;
    ArrayList<PhotoModel> PhotoArray;

    public PhotoAdapter(Context context, ArrayList<PhotoModel> PhotoArray) {

        this.context = context;
        this.PhotoArray = PhotoArray;
    }


    @Override
    public int getCount() {
        return PhotoArray.size();
    }

    @Override
    public Object getItem(int position) {
        return PhotoArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.lv_photos, null, true);

            holder.iv = (ImageView) convertView.findViewById(R.id.showImage);
            holder.tvname = (TextView) convertView.findViewById(R.id.showAuthor);

            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        Picasso.get().load(PhotoArray.get(position).getImgURL()).into(holder.iv);
        holder.tvname.setText(PhotoArray.get(position).getName());

        return convertView;
    }

    public class ViewHolder {

        TextView tvname;
        ImageView iv;
    }

}