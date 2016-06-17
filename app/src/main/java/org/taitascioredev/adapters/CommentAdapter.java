package org.taitascioredev.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;

import org.lucasr.twowayview.widget.TwoWayView;
import org.taitascioredev.fractal.CommentInfo;
import org.taitascioredev.fractal.Constants;
import org.taitascioredev.fractal.R;
import org.taitascioredev.fractal.Utils;

import java.util.List;

/**
 * Created by roberto on 30/10/15.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> implements PopupMenu.OnMenuItemClickListener {

    private String baseUrl;

    private final AppCompatActivity context;
    private final List<CommentNode> objects;
    private final List<CommentInfo> infoList;
    private Comment comment;

    private TwoWayView recyclerView;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        boolean isMenuVisible;

        View line;
        LinearLayout layout;
        TextView author;
        TextView body;
        TextView replies;
        LinearLayout lyActions;

        public ViewHolder(View v) {
            super(v);

            isMenuVisible = false;
            line = v.findViewById(R.id.indentation_line);
            layout = (LinearLayout) v.findViewById(R.id.linear_layout);
            author = (TextView) v.findViewById(R.id.text_comment_author);
            body = (TextView) v.findViewById(R.id.text_comment_body);
            replies = (TextView) v.findViewById(R.id.text_number_replies);
            lyActions = (LinearLayout) v.findViewById(R.id.layout_actions);
        }
    }

    public CommentAdapter(AppCompatActivity context, List<CommentNode> objects, List<CommentInfo> infoList) {
        this.context = context;
        this.objects = objects;
        this.infoList = infoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, final int position) {
        View v = LayoutInflater.from(context).inflate(R.layout.comment_row_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        CommentNode node = objects.get(position);
        Comment comment = node.getComment();

        /*
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.isMenuVisible) {
                    holder.lyActions.setVisibility(View.GONE);
                    holder.isMenuVisible = false;
                }
                else {
                    holder.lyActions.setVisibility(View.VISIBLE);
                    holder.isMenuVisible = true;
                }
            }
        });
        */

        holder.author.setText(comment.getAuthor());
        final Spanned spanned = Html.fromHtml(comment.getBodyHtml());
        //body.setText(Utils.setUrlSpans(context, Html.fromHtml(spanned.toString())));
        holder.body.setText(Utils.setUrlSpans(context, spanned, true));
        holder.body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.body.setText(Utils.setUrlSpans(context, spanned, false));
            }
        });
        holder.body.setMovementMethod(LinkMovementMethod.getInstance());

        if (node.getChildren().size() == 0)
            holder.replies.setVisibility(View.GONE);
        else {
            holder.replies.setText(node.getTotalSize() + "");
            holder.replies.setVisibility(View.VISIBLE);
        }

        int indentation = getIndentation(comment.getId());
        //holder.view.setPadding(10 + indentation, 10, 10, 10);
        if (indentation > 0) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.line.getLayoutParams();
            int pixels = Utils.dpToPixels(context, indentation - 1) * 5;
            //params.leftMargin = pixels;
            //holder.line.setLayoutParams(params);
            int color = Color.parseColor(Constants.INDENTATION_COLORS[(indentation - 1) % Constants.INDENTATION_COLORS.length]);
            holder.line.setBackgroundColor(color);
            holder.line.setVisibility(View.VISIBLE);

            //int width = holder.layout.getLayoutParams().width - pixels;
            //holder.layout.getLayoutParams().width = width;
            params = (ViewGroup.MarginLayoutParams) holder.layout.getLayoutParams();
            params.leftMargin = pixels;
            holder.layout.setLayoutParams(params);

        }
        else {
            holder.line.setVisibility(View.GONE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.layout.getLayoutParams();
            params.leftMargin = 0;
            holder.layout.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reply:

                return true;
            case R.id.permalink_comment:
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("permalink", baseUrl + comment.getId());
                clipboard.setPrimaryClip(data);
                Toast.makeText(context, "Permalink copied to the clipboard", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.parent:

                return true;
            default:
                return false;
        }
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setRecyclerView(TwoWayView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public TwoWayView getRecyclerView() {
        return recyclerView;
    }

    private int getIndentation(String id) {
        for (CommentInfo comment : infoList)
            if (comment.id.equals(id))
                return comment.indentation;
        return -1;
    }
}
