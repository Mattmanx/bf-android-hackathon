package com.bluefletch.internal.feed.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluefletch.internal.feed.FeedActivity;
import com.bluefletch.internal.feed.R;
import com.bluefletch.internal.feed.rest.Comment;
import com.bluefletch.internal.feed.rest.Post;
import com.bluefletch.internal.feed.rest.User;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

/**
 * Created by Bryan on 4/5/14.
 */
public class PostArrayAdapter extends ArrayAdapter<Post> implements AnimateAdditionAdapter.Insertable<Post> {

    private final FeedActivity feedActivity;
    public PostArrayAdapter(FeedActivity context, int textViewResourceId, List<Post> posts) {
        super(context,textViewResourceId, posts);
        feedActivity = context;
        List<Post> inflated = new ArrayList<Post>();

        for (Post post : posts) {
            inflated.add(post);
            for(Comment c : post.getComments()) {
                inflated.add(c);
            }
        }
        super.clear();
        super.addAll(inflated);
    }

    /**
     * Will be called to insert given {@code item} at given {@code index} in the list.
     *
     * @param index the index the new item should be inserted at
     * @param item  the item to insert
     */
    @Override
    public void add(int index, Post item){
        insert(item, index);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        //return 0 for comments, 1 for post
        return getItem(position) instanceof Comment ? 0 : 1;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // take data from cursor and put it in the view
        LayoutInflater li = LayoutInflater.from (getContext());

        if (convertView == null) {
            switch (getItemViewType(position)) {
                case 0:
                    convertView = li.inflate(R.layout.comment_item, parent, false);
                    break;
                default:
                    // default to sortable
                    convertView = li.inflate(R.layout.post_item, parent, false);
                    break;
            }

            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        Post post = getItem(position);
        holder.loadPost(post);

        boolean isNewPost = position == 0 || post.isNewPostForUser();
        int color = isNewPost ? Color.parseColor("#E5FFFF")
                    : convertView.getResources().getColor(android.R.color.white);
        convertView.setBackgroundColor(color);


        //try to hack around autoLink=web bug in textviews.
        //Basically, that will capture all clicks and prevent a list onItemClickListener from firing
        //this process will manually forward click events
        final View v = convertView;
        holder.postText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timber.i("Triggering click event for row %s", position);
                feedActivity.onRowClick(v, position);
            }
        });

        return convertView;
    }


    /*private view holder class*/
    protected class ViewHolder {
        @InjectView(R.id.profilePicturePost) ImageView profileImage;
        @InjectView(R.id.userNamePost) TextView userName;
        @InjectView(R.id.timeStampPost) TextView timeStamp;
        @InjectView(R.id.postText) TextView postText;
        public ViewHolder(View view){
            ButterKnife.inject(this, view);
        }
        public void loadPost(Post p) {

            boolean isComment = p instanceof Comment;
            //load this row
            User user = isComment ? ((Comment)p).getCommentUser() : p.getPostUser();

            this.userName.setText("@" + user.getUsername());

            DateTime dt = new DateTime(p.getCreatedDate());//ISODateTimeFormat.dateTimeParser().parseDateTime(p.getCreatedDate());

            this.timeStamp.setText(new PrettyTime().format(dt.toDate()));

            String content = isComment ? ((Comment)p).getCommentText() : p.getPostText();
            this.postText.setText(content);

            Picasso.with(getContext())
                    .load(getContext().getString(R.string.base_url) + user.getImageUrl())
                    .fit()
                    .centerCrop()
                    .placeholder(new IconDrawable(getContext(), Iconify.IconValue.fa_user)
                                    .colorRes(android.R.color.holo_blue_light))
                    .into(this.profileImage);

        }
    }

}
