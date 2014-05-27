package com.bluefletch.internal.feed.adapter;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bluefletch.internal.feed.R;
import com.bluefletch.internal.feed.rest.Comment;
import com.bluefletch.internal.feed.rest.Post;
import com.bluefletch.internal.feed.util.ISO8601;
import com.bluefletch.internal.feed.util.UrlFinder;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Bryan on 4/5/14.
 */
public class PostArrayAdapter extends ArrayAdapter<Post> {

    public static final String DATE_FORMAT_PRESENTABLE = "K:mm a MMM d";
    private static final SimpleDateFormat presentableDateFormat = new SimpleDateFormat(DATE_FORMAT_PRESENTABLE);
    private static int PRIMARY_TEXT_SIZE = 14;

    private static final String TAG = PostArrayAdapter.class.getSimpleName();

    private ArrayList<Post> posts;
    private ISO8601 iso8601;
    private CommentArrayAdapter commentArrayAdapter;
    private Context context;

    public PostArrayAdapter(Context context, int textViewResourceId, ArrayList<Post> posts) {
        super(context, textViewResourceId, posts);
        this.posts = posts;
        this.context = context;
        this.iso8601 = new ISO8601();
    }

    /*@Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        // tell adapters how each item will look when view is created for the first time
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View retView = inflater.inflate(R.layout.listitem_feed, viewGroup, false);
        return retView;
    }*/

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // take data from cursor and put it in the view

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listitem_feed, null);
        }

        Post post = posts.get(position);

        TextView textviewContent = (TextView) view.findViewById(R.id.listitem_posttext);
        //String content = cursor.getString(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_CONTENT));
        String content = post.getPostText();
        String formattedContent = UrlFinder.formatContent(content);
        textviewContent.setText(Html.fromHtml(formattedContent));
        textviewContent.setTextSize(PRIMARY_TEXT_SIZE);

        TextView textviewTime = (TextView) view.findViewById(R.id.listitem_time);
        Calendar cal = null;
        try {
            cal = iso8601.toCalendar(post.getCreatedDate());
        } catch (ParseException parseEx) {
            Log.e(TAG, "ParseException converting post createdDate to calendar: " + parseEx.getMessage());
        }

        //Date dateAddDtm = new SimpleDateFormat("MMMM d, yyyy", Locale.US).parse(post.getCreatedDate());
        //Date dateAddDtm = LaterListItem.getDateFromFormattedLong(cursor.getLong(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_ADD_DTM)));
        //textviewTime.setText(presentableDateFormat.format(dateAddDtm));
        if (cal != null) {
            textviewTime.setText(presentableDateFormat.format(cal.getTime()));
            //PrettyTime prettyTime = new PrettyTime();
            //textviewTime.setText(prettyTime.format(cal));
        }
        else {
            textviewTime.setText("");
        }
        textviewTime.setTextSize(PRIMARY_TEXT_SIZE - 2);

        ImageView imageviewUserPic = (ImageView) view.findViewById(R.id.listitem_userimage);
        Picasso.with(getContext())
                .load(getContext().getString(R.string.base_url) + post.getPostUser().getImageUrl())
                .resize(50, 50)
                .into(imageviewUserPic);

        TextView textviewUsername = (TextView) view.findViewById(R.id.listitem_username);
        //String id = cursor.getString(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_ID));
        String username = post.getPostUser().getUsername();
        textviewUsername.setText(username);

        TextView textviewPostId = (TextView) view.findViewById(R.id.listitem_postid);
        //int status = cursor.getInt(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_STATUS));
        textviewPostId.setText(post.get_id());

        TextView textviewCommentId = (TextView) view.findViewById(R.id.listitem_commentid);
        //int status = cursor.getInt(cursor.getColumnIndex(LaterListSQLiteHelper.COLUMN_STATUS));
        textviewCommentId.setText(Integer.toString(-1));

        TextView textviewCommentCountId = (TextView) view.findViewById(R.id.listitem_commentcount);
        textviewCommentCountId.setText(Integer.toString(post.getComments().size()));

        /*ListView commentListView = (ListView) view.findViewById(R.id.list_comments);
        ArrayList<Comment> c = new ArrayList<Comment>(post.getComments());
        commentArrayAdapter = new CommentArrayAdapter(getContext(), R.layout.listitem_feed, c);
        commentListView.setAdapter(commentArrayAdapter);*/

        return view;
    }

    public void setTextSize(int textSize) {
        PRIMARY_TEXT_SIZE = textSize;
    }

}
