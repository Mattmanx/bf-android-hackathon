package com.bluefletch.internal.feed;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluefletch.internal.feed.rest.Post;
import com.bluefletch.internal.feed.rest.User;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by grantstevens on 4/23/14.
 */
public class CustomListViewAdapter extends ArrayAdapter<Post> {

    Context context;

    public CustomListViewAdapter(Context context, int resourceId,
                                 List<Post> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /*private view holder class*/
    private class ViewHolder {
        ImageView profileImage;
        TextView userName;
        TextView timeStamp;
        TextView postText;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Post post = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.post_item, null);
            holder = new ViewHolder();
            holder.userName = (TextView) convertView.findViewById(R.id.userNamePost);
            holder.timeStamp = (TextView) convertView.findViewById(R.id.timeStampPost);
            holder.postText = (TextView) convertView.findViewById(R.id.postText);
            holder.profileImage = (ImageView) convertView.findViewById(R.id.profilePicturePost);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        User user = post.getPostUser();

        holder.userName.setText(user.getUsername());
        //holder.timeStamp.setText(TimeUtils.getTimeAgo(((Date)post.getCreatedDate()).getTime(), context));
        holder.timeStamp.setText(post.getCreatedDate());
        holder.postText.setText(post.getPostText());
        new DownloadImageTask(context, (ImageView)convertView.findViewById(R.id.profilePicturePost)).execute(context.getString(R.string.base_url) + user.getImageUrl());

        return convertView;
    }
}