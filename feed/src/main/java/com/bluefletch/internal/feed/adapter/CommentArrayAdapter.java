package com.bluefletch.internal.feed.adapter;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bluefletch.internal.feed.R;
import com.bluefletch.internal.feed.rest.Comment;
import com.bluefletch.internal.feed.util.ISO8601;
import com.bluefletch.internal.feed.util.UrlFinder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Bryan on 4/5/14.
 */
public class CommentArrayAdapter extends ArrayAdapter<Comment> {

    public static final String DATE_FORMAT_PRESENTABLE = "K:mm a | MMM d";
    private static final SimpleDateFormat presentableDateFormat = new SimpleDateFormat(DATE_FORMAT_PRESENTABLE);
    private static int PRIMARY_TEXT_SIZE = 16;

    private static final String TAG = CommentArrayAdapter.class.getSimpleName();

    private ArrayList<Comment> comments;
    private ISO8601 iso8601;

    public CommentArrayAdapter(Context context, int textViewResourceId, ArrayList<Comment> comments) {
        super(context, textViewResourceId, comments);
        this.comments = comments;
        this.iso8601 = new ISO8601();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // take data from cursor and put it in the view

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listitem_feed, null);
        }

        Comment comment = comments.get(position);

        Log.d(TAG, comment.getCommentText());

        TextView textviewContent = (TextView) view.findViewById(R.id.listitem_posttext);
        String content = comment.getCommentText();
        String formattedContent = UrlFinder.formatContent(content);
        textviewContent.setText(Html.fromHtml(formattedContent));
        textviewContent.setTextSize(PRIMARY_TEXT_SIZE);

        TextView textviewTime = (TextView) view.findViewById(R.id.listitem_time);
        Log.d(TAG, "comment.getCreatedDate(): " + comment.getCreatedDate());
        Calendar cal = null;
        try {
            cal = iso8601.toCalendar(comment.getCreatedDate());
        } catch (ParseException parseEx) {
            Log.e(TAG, "ParseException converting comment createdDate to calendar: " + parseEx.getMessage());
        }
        if (cal != null) {
            textviewTime.setText(presentableDateFormat.format(cal.getTime()));
        }
        else {
            textviewTime.setText("");
        }
        textviewTime.setTextSize(PRIMARY_TEXT_SIZE - 2);

        TextView textviewUsername = (TextView) view.findViewById(R.id.listitem_username);
        String username = comment.getCommentUser().getUsername();
        textviewUsername.setText(username);

        TextView textviewCommentId = (TextView) view.findViewById(R.id.listitem_commentid);
        textviewCommentId.setText(comment.get_id());

        return view;
    }

    public void setTextSize(int textSize) {
        PRIMARY_TEXT_SIZE = textSize;
    }

}
