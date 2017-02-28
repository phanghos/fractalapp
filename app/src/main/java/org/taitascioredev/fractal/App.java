package org.taitascioredev.fractal;

import android.app.Application;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.facebook.drawee.backends.pipeline.Fresco;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.http.LoggingMode;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Message;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.InboxPaginator;
import net.dean.jraw.paginators.SubredditPaginator;
import net.dean.jraw.paginators.SubredditSearchPaginator;
import net.dean.jraw.paginators.SubredditStream;
import net.dean.jraw.paginators.UserContributionPaginator;
import net.dean.jraw.paginators.UserSubredditsPaginator;

import org.taitascioredev.jraw.android.AndroidRedditClient;
import org.taitascioredev.jraw.android.AndroidTokenStore;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by roberto on 30/04/15.
 */
public class App extends Application {

    private int loginType;

    private AppCompatActivity context;
    private RedditClient client;
    private SubredditPaginator paginator;
    private List<Submission> submissions;
    private SubredditStream subredditStream;
    private UserSubredditsPaginator userSubredditsPaginator;
    private List<Subreddit> subreddits;
    private List<Subreddit> userSubreddits;
    private SubredditPaginator subredditPaginator;
    private List<Submission> submissionsSubreddit;
    private InboxPaginator inboxPaginator;
    private List<Message> messages;
    private Message message;
    private UserContributionPaginator hiddenPaginator;
    public static SubredditSearchPaginator searchPaginator;
    private Submission newSubmission;
    private int frontPageSorting = -1;
    private int subredditSorting = -1;
    private int subredditPageSorting = -1;
    private int inboxSorting = -1;
    public static Fragment currentFragment;

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
        client = new AndroidRedditClient(this);
        //client.setLoggingMode(LoggingMode.ALWAYS);
        AuthenticationManager.get().init(client, new RefreshTokenHandler(new AndroidTokenStore(this), client));
    }

    public void setLoginType(int loginType) { this.loginType = loginType; }
    public void setContext(AppCompatActivity context) { this.context = context; }
    public void setClient(RedditClient client) { this.client = client; }
    public void setPaginator(SubredditPaginator paginator) { this.paginator = paginator; }
    public void setSubmissions(List<Submission> submissions) { this.submissions = submissions; }
    public void setSubredditStream(SubredditStream subredditStream) { this.subredditStream = subredditStream; }
    public void setUserSubredditsPaginator(UserSubredditsPaginator userSubredditsPaginator) { this.userSubredditsPaginator = userSubredditsPaginator; }
    public void setSubreddits(List<Subreddit> subreddits) { this.subreddits = subreddits; }
    public void setUserSubreddits(List<Subreddit> userSubreddits) { this.userSubreddits = userSubreddits; }
    public void setSubredditPaginator(SubredditPaginator subredditPaginator) { this.subredditPaginator = subredditPaginator; }
    public void setSubmissionsSubreddit(List<Submission> submissionsSubreddit) { this.submissionsSubreddit = submissionsSubreddit; }
    public void setInboxPaginator(InboxPaginator inboxPaginator) { this.inboxPaginator = inboxPaginator; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
    public void setMessage(Message message) { this.message = message; }
    public void setHiddenPaginator(UserContributionPaginator hiddenPaginator) { this.hiddenPaginator = hiddenPaginator; }
    public void setNewSubmission(Submission newSubmission) { this.newSubmission = newSubmission; }
    public void setFrontPageSorting(int sorting) { this.frontPageSorting = sorting; }
    public void setSubredditSorting(int sorting) { this.subredditSorting = sorting; }
    public void setSubredditPageSorting(int sorting) { this.subredditPageSorting = sorting; }
    public void setInboxSorting(int sorting) { this.inboxSorting = sorting; }

    public int getLoginType() { return loginType; }
    public AppCompatActivity getContext() { return context; }
    public RedditClient getClient() { return client; }
    public SubredditPaginator getPaginator() { return paginator; }
    public List<Submission> getSubmissions() { return submissions; }
    public SubredditStream getSubredditStream() { return subredditStream; }
    public UserSubredditsPaginator getUserSubredditsPaginator() { return userSubredditsPaginator; }
    public List<Subreddit> getSubreddits() { return subreddits; }
    public List<Subreddit> getUserSubreddits() { return userSubreddits; }
    public SubredditPaginator getSubredditPaginator() { return subredditPaginator; }
    public List<Submission> getSubmissionsSubreddit() { return submissionsSubreddit; }
    public InboxPaginator getInboxPaginator() { return inboxPaginator; }
    public List<Message> getMessages() { return messages; }
    public Message getMessage() { return message; }
    public UserContributionPaginator getHiddenPaginator() { return hiddenPaginator; }
    public Submission getNewSubmission() { return newSubmission; }
    public int getFrontPageSorting() { return frontPageSorting; }
    public int getSubredditSorting() { return subredditSorting; }
    public int getSubredditPageSorting() { return subredditPageSorting; }
    public int getInboxSorting() { return inboxSorting; }
}
