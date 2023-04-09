package com.example.cmput301w21t25.customAdapters;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Constraints;

import com.example.cmput301w21t25.R;
import com.example.cmput301w21t25.experiments.Experiment;
import com.example.cmput301w21t25.forum.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Eden
 * A custom array adapter used to display comments in list views
 */
public class CustomListComment extends ArrayAdapter<Comment> {
    private ArrayList<Comment> comments;
    private Context context;
    private Experiment forumExperiment;
    private Comment comment;
    private Date date;
    private String dateString;

    public CustomListComment(Context context, ArrayList<Comment> comments, Experiment forumExperiment) {
        super(context,0,comments);
        this.comments = comments;
        this.context = context;
        this.forumExperiment = forumExperiment;
    }

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        //The content view is not displayed if the adapter is empty
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.comment_content, parent, false);
        }

        comment = comments.get(position);

        ConstraintLayout background = view.findViewById(R.id.commentLinearLayout);
        Drawable contentBackground = background.getBackground();

        ImageView replyGraphic = view.findViewById(R.id.replyToGraphic);
        ImageView newThreadGraphic = view.findViewById(R.id.newThreadGraphic);

        ImageView line1 = view.findViewById(R.id.line1_1);
        ImageView line2 = view.findViewById(R.id.line2);
        ImageView line3 = view.findViewById(R.id.line3_1);

        TextView commentHeader = view.findViewById(R.id.replyToText);
        TextView commenterName = view.findViewById(R.id.commenterName);
        TextView commentDate = view.findViewById(R.id.commentDate);
        TextView commentContent = view.findViewById(R.id.comment);
        TextView indent = view.findViewById(R.id.blankSpace);

        //ImageButton replyButton = view.findViewById(R.id.replyButton);

        date = comment.getCommentDate();
        dateString = formatDate(date);

        //Set a header/graphic based on whether it's a new comment or a response
        if (comment.getCommentParent().equals("")) {
            newThreadGraphic.setVisibility(View.VISIBLE);
            replyGraphic.setVisibility(View.GONE);
            commentHeader.setText("New Thread");
        }
        else {
            replyGraphic.setVisibility(View.VISIBLE);
            newThreadGraphic.setVisibility(View.GONE);
            commentHeader.setText("Replying to: " + comment.getRespondingTo() + "...");
        }

        //If the commenter is the experiment owner, change the colour of their name and specify
        if (forumExperiment.getOwnerID().equals(comment.getCommenterID())) {
            commenterName.setText("Owner: " + comment.getCommenterName());
            commenterName.setTextColor(context.getResources().getColor(R.color.custom_Yellow_dark));
        }
        else if (!forumExperiment.getOwnerID().equals(comment.getCommentID())) {
            commenterName.setText(comment.getCommenterName());
            commenterName.setTextColor(context.getResources().getColor(R.color.custom_Blue_light));
        }

        if (comment.getCommentDepth() > 0) {
            replyGraphic.setPadding((50 * comment.getCommentDepth()) + 36, 0, 0, 0);
            //newThreadGraphic.setPadding((50 * comment.getCommentDepth()) + 36, 0, 0, 0);
            line1.setPadding((50 * comment.getCommentDepth()), 0, 0, 0);
            line2.setPadding((50 * comment.getCommentDepth()), 0, 0, 0);
            line3.setPadding((50 * comment.getCommentDepth()), 0, 0, 0);
            commentHeader.setPadding(0, 0, 0, 0);
            commenterName.setPadding((50 * comment.getCommentDepth()) +36, 0, 0, 0);
            commentDate.setPadding((50 * comment.getCommentDepth()) +36, 0, 0, 0);
            commentContent.setPadding((50 * comment.getCommentDepth()) +36, 0, 0, 0);

        }

        else {
            //replyGraphic.setPadding(0, 0, 0, 0);
            newThreadGraphic.setPadding(36, 0, 0, 0);
            commentHeader.setPadding(130, 0, 0, 0);
            commenterName.setPadding(36, 0, 0, 0);
            commentDate.setPadding(36, 0, 0, 0);
            commentContent.setPadding(36, 0, 0, 0);
        }

        /*indent.setWidth(10 * comment.getCommentDepth());
        Log.d("COMMENT_DEPTH", String.valueOf(comment.getCommentDepth()));
        //indent.setPadding(10 * comment.getCommentDepth(), 0, 0, 0);
        ViewGroup.LayoutParams params = indent.getLayoutParams();
        params.width = (10 * comment.getCommentDepth());
        indent.setLayoutParams(params);*/

        //Convert Date object to simple format
        commentDate.setText(dateString);
        commentContent.setText(comment.getComment());

        return view;
    }

    /**
     *
     * @param date
     * The date the comment was created
     * @return
     * A formatted version of the date (String)
     */
    private String formatDate(Date date) {

        SimpleDateFormat condensedDate = new SimpleDateFormat("MM-dd-yyyy");
        String formattedDate = condensedDate.format(date);
        return formattedDate;
    }

}