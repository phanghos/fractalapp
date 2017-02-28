package org.taitascioredev.fractal;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.auth.NoSuchTokenException;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.http.oauth.OAuthHelper;
import net.dean.jraw.models.LoggedInAccount;
import net.dean.jraw.models.Submission;

import org.taitascioredev.adapters.CommentAdapter;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

/**
 * Created by roberto on 12/05/15.
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        LoginFragment.OnAccountLoggedListener,
        CommentAdapter.OnSubmissionVoteListener {

    UUID uuid;
    Credentials credentials;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_view) NavigationView mNavView;

    ActionBarDrawerToggle drawerToggle;
    MaterialDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setToolbar();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                setNavigationIcon();
            }
        });

        MenuItem inbox = mNavView.getMenu().findItem(R.id.inbox);
        MenuItem hidden = mNavView.getMenu().findItem(R.id.hidden);
        MenuItem saved = mNavView.getMenu().findItem(R.id.saved);

        hidden.setVisible(false);
        saved.setVisible(false);

        if (AuthenticationManager.get().getRedditClient().getAuthenticationMethod().isUserless()) {
            inbox.setVisible(false);
            //hidden.setVisible(false);
            //saved.setVisible(false);
        }
        else {
            inbox.setVisible(true);
            //hidden.setVisible(true);
            //saved.setVisible(true);
        }

        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (!menuItem.isChecked()) menuItem.setChecked(true);

                mDrawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.log:
                        if (AuthenticationManager.get().getRedditClient().getAuthenticationMethod().isUserless()) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new LoginFragment()).commit();
                        } else
                            Utils.logout(MainActivity.this);

                        return true;
                    case R.id.home:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new MainFragment(), getString(R.string.fragment_main)).commit();

                        return true;
                    case R.id.subreeddits:
                        if (AuthenticationManager.get().getRedditClient().getAuthenticationMethod().isUserless()) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container,
                                            new SubredditsFragmentUserless(),
                                            getString(R.string.fragment_subreddits_userless))
                                    .addToBackStack(null).commit();
                        } else {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container,
                                            new SubredditsFragmentOAuth(),
                                            getString(R.string.fragment_subreddits_oauth))
                                    .addToBackStack(null).commit();
                        }

                        return true;
                    case R.id.inbox:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container,
                                        new InboxFragment(),
                                        getString(R.string.fragment_inbox))
                                .addToBackStack(null).commit();

                        return true;
                    /*
                    case R.id.hidden:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HiddenFragment()).addToBackStack(null).commit();
                        return true;
                    case R.id.saved:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SavedFragment()).addToBackStack(null).commit();
                        return true;
                        */
                    /*
                    case R.id.user:
                        new MaterialDialog.Builder(MainActivity.this)
                                .title("Search user")
                                .inputType(InputType.TYPE_CLASS_TEXT)
                                .input("Username", "", false, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog materialDialog, CharSequence query) {
                                        UserFragment fragment = new UserFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("username", query + "");
                                        fragment.setArguments(bundle);
                                        MainActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                                    }
                                })
                                .show();
                        return true;
                        */
                    case R.id.settings:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new PreferencesFragment(), getString(R.string.fragment_preferences))
                                .addToBackStack(null).commit();

                        return true;
                    default:
                        return false;
                }
            }
        });

        /*
        drawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        */

        //mDrawerLayout.setDrawerListener(drawerToggle);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState != null) setNavigationIcon();

        Fragment fragment = null;
        Bundle b = getIntent().getExtras();
        if (b != null) {
            String action = b.getString("action");

            switch (action) {
                case "main":
                    fragment = new MainFragment();
                    break;
                case "subreddits":
                    if (AuthenticationManager.get().getRedditClient().getAuthenticationMethod().isUserless())
                        fragment = new SubredditsFragmentUserless();
                    else
                        fragment = new SubredditsFragmentOAuth();

                    break;
                case "subreddit":
                    if (b.containsKey("id")) fragment = new CommentsFragment();
                    else                     fragment = new SubredditPageFragment();
                    fragment.setArguments(b);

                    break;
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment).commit();
        }
        else {
            fragment = new MainFragment();

            if (getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_main)) == null)
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment, getString(R.string.fragment_main)).commit();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setNavigationIcon() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        Log.d("FRAGMENT COUNT", backStackEntryCount+"");
        //drawerToggle.setDrawerIndicatorEnabled(backStackEntryCount == 0);
        ActionBar ab = getSupportActionBar();

        if (backStackEntryCount == 0) ab.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        else                          ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
    }

    /*
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
    */

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getString(R.string.hint_search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.err_empty_field), Toast.LENGTH_SHORT).show();
                    return true;
                }


                SearchSubredditFragment fragment = new SearchSubredditFragment();
                Bundle bundle = new Bundle();
                bundle.putString("query", s.trim());
                fragment.setArguments(bundle);
                menu.findItem(R.id.search).collapseActionView();
                Utils.hideKeyboard(MainActivity.this);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment, getString(R.string.fragment_search_subreddit))
                        .addToBackStack(null).commit();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //if (drawerToggle.isDrawerIndicatorEnabled() && drawerToggle.onOptionsItemSelected(item))
            //return true;

        switch (item.getItemId()) {
            case android.R.id.home:
                int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();

                if (backStackEntryCount == 0) mDrawerLayout.openDrawer(GravityCompat.START);
                else                          super.onBackPressed();

                return true;
            case R.id.item_about:
                showAboutDialog();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //drawerToggle.syncState();

        AuthenticationState state = AuthenticationManager.get().checkAuthState();
        Log.d("Authentication state", state.name());
        Log.d("Authentication status", AuthenticationManager.get().getRedditClient().getOAuthHelper().getAuthStatus().name());

        switch (state) {
            case READY:
                if (Utils.hasExpired(this))  {
                    if (AuthenticationManager.get().getRedditClient().isAuthenticated()) {
                        credentials = LoginFragment.CREDENTIALS;
                        new RefreshTokenAsync().execute();
                    }
                    else {
                        uuid = UUID.randomUUID();
                        credentials = Credentials.userlessApp(Constants.CLIENT_ID, uuid);
                        new AuthenticateAsync().execute();
                    }
                }

                break;
            case NONE:
                uuid = UUID.randomUUID();
                credentials = Credentials.userlessApp(Constants.CLIENT_ID, uuid);
                new AuthenticateAsync().execute();
                break;
            case NEED_REFRESH:
                credentials = LoginFragment.CREDENTIALS;
                new RefreshTokenAsync().execute();
                break;
        }

        if (AuthenticationManager.get().getRedditClient().getAuthenticationMethod().isUserless()) {
            Log.d("Authentication method", "USERLESS");
            MenuItem item = mNavView.getMenu().findItem(R.id.log);
            item.setTitle("Log In");
        }
        else {
            Log.d("Authentication method", "OAUTH");
            new GetMeAsync().execute();
            MenuItem item = mNavView.getMenu().findItem(R.id.log);
            item.setTitle("Log Out");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_displayStyle")) {
            String displayPref = Utils.getDisplayPreference(this);
        }
    }

    @Override
    public void OnAccountLoggedIn() {
        new GetMeAsync().execute();
    }

    @Override
    public void OnAccountLoggedOut() {
        MenuItem item = mNavView.getMenu().findItem(R.id.log);
        item.setTitle("Log In");
    }

    private void showAboutDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .theme(Theme.LIGHT)
                .title("About")
                .customView(R.layout.about, false)
                .positiveText("ok")
                .show();

        View v = dialog.getCustomView();
        TextView tvAbout   = (TextView) v.findViewById(R.id.tv_about);
        TextView tvContact = (TextView) v.findViewById(R.id.tv_contact);
        SpannableStringBuilder ssb;
        int start, end;

        ssb = new SpannableStringBuilder(tvAbout.getText().toString());
        start = ssb.toString().indexOf("Roberto");
        end   = start + getString(R.string.about_roberto).length();
        CalligraphyTypefaceSpan typefaceSpan = new CalligraphyTypefaceSpan(
                TypefaceUtils.load(getAssets(), "fonts/OpenSans-Semibold.ttf"));
        ssb.setSpan(typefaceSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        start = ssb.toString().indexOf("Manuel");
        end   = start + getString(R.string.about_manuel).length();
        typefaceSpan = new CalligraphyTypefaceSpan(
                TypefaceUtils.load(getAssets(), "fonts/OpenSans-Semibold.ttf"));
        ssb.setSpan(typefaceSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        tvAbout.setText(ssb);

        ssb = new SpannableStringBuilder(tvContact.getText().toString());
        start = ssb.toString().indexOf("tatasciore");
        end   = start + getString(R.string.contact_email).length();
        MyClickableSpan clickableSpan = new MyClickableSpan(ContextCompat.getColor(this, R.color.colorPrimary)) {
            @Override
            public void onClick(View widget) {
                try {
                    Intent i = new Intent(Intent.ACTION_SENDTO);
                    i.setData(Uri.parse("mailto:" + getString(R.string.contact_email)));
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData data = ClipData.newPlainText("e-mail", "tatasciorecont@gmail.com");
                    clipboard.setPrimaryClip(data);

                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.err_no_email),
                            Toast.LENGTH_SHORT).show();
                }
            }
        };
        ssb.setSpan(typefaceSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        ssb.setSpan(clickableSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        tvContact.setText(ssb);
        tvContact.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onVoted(int pos, Submission s) {

    }

    private class GetMeAsync extends AsyncTask<Void, Void, LoggedInAccount> {

        @Override
        protected LoggedInAccount doInBackground(Void... params) {
            return AuthenticationManager.get().getRedditClient().me();
        }

        @Override
        protected void onPostExecute(LoggedInAccount loggedInAccount) {
            super.onPostExecute(loggedInAccount);
            Utils.username = loggedInAccount.getFullName();
            TextView username = (TextView) findViewById(R.id.username);
            TextView email = (TextView) findViewById(R.id.email);
            username.setText(loggedInAccount.getFullName());
            email.setText(loggedInAccount.getLinkKarma() + " link karma");
            MenuItem item = mNavView.getMenu().findItem(R.id.log);
            item.setTitle("Log Out");
        }
    }

    class AuthenticateAsync extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = Utils.showProgressDialog(MainActivity.this, "Please wait...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isConnected = Utils.isConnected(getApplicationContext());
            Log.d("IS CONNECTED", isConnected+"");
            if (!isConnected) return false;

            try {
                OAuthData data = AuthenticationManager.get().getRedditClient().getOAuthHelper().easyAuth(credentials);
                AuthenticationManager.get().getRedditClient().authenticate(data);
                Utils.saveExpirationDate(getApplicationContext(), data.getExpirationDate());
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();

            if (!result) {

            }
        }
    }

    private class RefreshTokenAsync extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = Utils.showProgressDialog(MainActivity.this, "Please wait...");
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean isConnected = Utils.isConnected(getApplicationContext());
            Log.d("IS CONNECTED", isConnected+"");
            if (!isConnected) return false;

            try {
                AuthenticationManager.get().refreshAccessToken(credentials);
                Date expirationDate = new Date();
                expirationDate.setTime(expirationDate.getTime() + 60 * 1000 * 60);
                Utils.saveExpirationDate(getApplicationContext(), expirationDate);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();

            if (!result) {

            }
        }
    }
}
