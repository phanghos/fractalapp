package org.taitascioredev.fractal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import net.dean.jraw.models.LoggedInAccount;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by roberto on 12/05/15.
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, LoginFragment.OnAccountLoggedListener {

    private MyApp app;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView navView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (MyApp) getApplication();
        app.setContext(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                setNavigationIcon();
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navView = (NavigationView) findViewById(R.id.navigation_view);

        MenuItem inbox = navView.getMenu().findItem(R.id.inbox);
        MenuItem hidden = navView.getMenu().findItem(R.id.hidden);
        MenuItem saved = navView.getMenu().findItem(R.id.saved);

        hidden.setVisible(false);
        saved.setVisible(false);

        if (app.getClient().getAuthenticationMethod().isUserless()) {
            inbox.setVisible(false);
            //hidden.setVisible(false);
            //saved.setVisible(false);
        }
        else {
            inbox.setVisible(true);
            //hidden.setVisible(true);
            //saved.setVisible(true);
        }

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.inbox && app.getClient().getAuthenticationMethod().isUserless()) {
                    Toast.makeText(MainActivity.this, "You need to be logged in to perform this action", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (menuItem.isChecked())
                    menuItem.setChecked(false);
                else
                    menuItem.setChecked(true);
                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.log:
                        if (app.getClient().getAuthenticationMethod().isUserless()) {
                            LoginFragment fragment = new LoginFragment();
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                        } else
                            Utils.logout(MainActivity.this);
                        return true;
                    case R.id.home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MainFragment()).commit();
                        return true;
                    case R.id.subreeddits:
                        if (app.getClient().getAuthenticationMethod().isUserless())
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SubredditsFragmentUserless()).addToBackStack(null).commit();
                        else
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SubredditsFragmentOAuth()).addToBackStack(null).commit();
                        return true;
                    case R.id.inbox:
                        /*
                        if (app.getClient().getAuthenticationMethod().isUserless())
                            Toast.makeText(MainActivity.this, "You need to be logged in to perform this action", Toast.LENGTH_SHORT).show();
                        else
                        */
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new InboxFragment()).addToBackStack(null).commit();
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
                        MainActivity.this.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PreferencesFragment()).addToBackStack(null).commit();
                        return true;
                    default:
                        return false;
                }
            }
        });
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
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

        drawerLayout.setDrawerListener(drawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //drawerToggle.syncState();

        Fragment fragment = null;

        Bundle b = getIntent().getExtras();
        if (b != null) {
            String action = b.getString("action");

            switch (action) {
                case "subreddits":
                    if (app.getClient().getAuthenticationMethod().isUserless())
                        fragment = new SubredditsFragmentUserless();
                    else
                        fragment = new SubredditsFragmentOAuth();
                    break;
                case "subreddit":
                    fragment = new SubredditPageFragment();
                    fragment.setArguments(b);
                    break;
            }


        }
        else
            fragment = new MainFragment();

        /*
        String url = getIntent().getStringExtra("url");
        Bundle b = new Bundle();
        if (url != null) {
            b.putString("url", url);
            Log.d("debug", url);
        }
        fragment.setArguments(b);
        */

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragment).commit();
    }

    //@Override
    //public void init(Bundle bundle) {
        /*
        if (app.getClient().getAuthenticationMethod().isUserless()) {
            account.setTitle("User-less");
            account.setSubTitle("Login");
        }
        else {
            new GetMeTask().execute();
            addAccountSection(newSection("Logout", R.drawable.ic_key, new MaterialSectionListener() {
                @Override
                public void onClick(MaterialSection materialSection) {
                    Utils.logout(MainActivity.this);
                }
            }));
        }
        addAccountSection(newSection("Add Account", R.drawable.ic_add_circle_black_24dp, new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection materialSection) {
                getSupportActionBar().hide();
                setFragment(new LoginFragment(), "");
            }
        }));
        setAccountListener(new MaterialAccountListener() {
            @Override
            public void onAccountOpening(MaterialAccount materialAccount) {

            }

            @Override
            public void onChangeAccount(MaterialAccount materialAccount) {
                new GetMeTask().execute();
            }
        });
        setDrawerHeaderImage(R.drawable.header);
        MaterialSection compose = newSection("New post", new ComposePostFragment());
        addSection(compose);
        */
    //}

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setNavigationIcon() {
        int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        drawerToggle.setDrawerIndicatorEnabled(backStackEntryCount == 0);
    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.isDrawerIndicatorEnabled() && drawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawerToggle.syncState();
        //Utils.hasActiveUser = app.getClient().hasActiveUserContext();
        if (app.getClient().getAuthenticationMethod().isUserless()) {
            Log.d("debug", "USERLESS");
            MenuItem item = navView.getMenu().findItem(R.id.log);
            item.setTitle("Log In");
        }
        else {
            Log.d("debug", "OAUTH");
            new GetMeTask().execute();
            MenuItem item = navView.getMenu().findItem(R.id.log);
            item.setTitle("Log Out");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("debug", "onStop MainActivity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("debug", "onDestroy MainActivity");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_displayStyle")) {
            String displayPref = Utils.getDisplayPreference(this);
            if (displayPref.equals("1"))
                ;
            else
                ;
        }
    }

    @Override
    public void OnAccountLoggedIn() {
        new GetMeTask().execute();
    }

    @Override
    public void OnAccountLoggedOut() {
        MenuItem item = navView.getMenu().findItem(R.id.log);
        item.setTitle("Log In");
    }

    private class GetMeTask extends AsyncTask<Void, Void, LoggedInAccount> {

        @Override
        protected LoggedInAccount doInBackground(Void... params) {
            return app.getClient().me();
        }

        @Override
        protected void onPostExecute(LoggedInAccount loggedInAccount) {
            super.onPostExecute(loggedInAccount);
            Utils.username = loggedInAccount.getFullName();
            TextView username = (TextView) findViewById(R.id.username);
            TextView email = (TextView) findViewById(R.id.email);
            username.setText(loggedInAccount.getFullName());
            email.setText(loggedInAccount.getLinkKarma() + " link karma");
            MenuItem item = navView.getMenu().findItem(R.id.log);
            item.setTitle("Log Out");
        }
    }
}
