package org.taitascioredev.fractal;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Picasso;

import net.dean.jraw.ApiException;
import net.dean.jraw.models.Captcha;

import org.markdown4j.Markdown4jProcessor;

import java.io.IOException;

//import com.gc.materialdesign.views.ButtonFlat;

/**
 * Created by roberto on 31/05/15.
 */
public class ComposePmFragment extends Fragment implements View.OnClickListener {

    private MyApp app;

    private boolean needsCaptcha;
    private boolean determinedCaptcha;
    private Captcha captcha;

    private String userStr;
    private String subjectStr;
    private String contentStr;
    private String captchaUrl;
    private String captchaStr;

    private EditText user;
    private EditText subject;
    private EditText content;
    //private ButtonFlat preview;
    //private ButtonFlat submit;
    private ImageButton bold;
    private ImageButton italics;
    private ImageButton link;
    private Toolbar toolbar;
    private AppCompatActivity context;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose_pm, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = (AppCompatActivity) getActivity();
        app = (MyApp) context.getApplication();

        user = (EditText) context.findViewById(R.id.et_username);
        subject = (EditText) context.findViewById(R.id.edit_text_subject);
        content = (EditText) context.findViewById(R.id.edit_text_content);
        /*
        preview = (ButtonFlat) context.findViewById(R.id.button_preview);
        preview.setOnClickListener(this);
        submit = (ButtonFlat) context.findViewById(R.id.button_submit);
        submit.setOnClickListener(this);
        */
        bold = (ImageButton) context.findViewById(R.id.image_bold);
        bold.setOnClickListener(this);
        italics = (ImageButton) context.findViewById(R.id.image_italics);
        italics.setOnClickListener(this);
        link = (ImageButton) context.findViewById(R.id.image_link);
        link.setOnClickListener(this);

        checkIfCaptchaIsNeeded();

        toolbar = (Toolbar) context.findViewById(R.id.bottom_toolbar);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("debug", "onDestroy");
        needsCaptcha = false;
        determinedCaptcha = false;
        captcha = null;
        user.setText("");
        subject.setText("");
        content.setText("");
    }

    private void checkIfCaptchaIsNeeded() {
        if (app.getClient().getAuthenticationMethod().isUserless()) {
            needsCaptcha = true;
            Log.d("debug", "NEEDS CAPTCHA (USERLESS)");
            new GetCaptchaTask().execute();
        }
        else
            new NeedsCaptchaTask().execute();
    }

    @Override
    public void onClick(View v) {
        String text = content.getText().toString();
        switch (v.getId()) {
            case R.id.image_bold:
                showBoldDialog();
                break;
            case R.id.image_italics:
                showItalicsDialog();
                break;
            case R.id.image_link:
                showAddLinkDialog();
                break;
            case R.id.button_preview:
                showPreviewDialog();
                break;
            case R.id.button_submit:
                if (!checkFields())
                    break;
                if (needsCaptcha && determinedCaptcha)
                    showCaptchaDialog();
                else
                    new SendPmTask().execute();
                break;
        }
    }

    private void showBoldDialog() {
        new MaterialDialog.Builder(context)
                .title("Add bold")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Text", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (input.toString().length() == 0)
                            Toast.makeText(context, "Field cannot be blank", Toast.LENGTH_SHORT).show();
                        else
                            content.append(" **" + input + "** ");
                        content.requestFocus();
                    }
                })
                .positiveText("add text")
                .negativeText("cancel")
                .show();
    }

    private void showItalicsDialog() {
        new MaterialDialog.Builder(context)
                .title("Add italics")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Text", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (input.toString().length() == 0)
                            Toast.makeText(context, "Field cannot be blank", Toast.LENGTH_SHORT).show();
                        else
                            content.append(" *" + input + "* ");
                    }
                })
                .positiveText("add text")
                .negativeText("cancel")
                .show();
    }

    private void showAddLinkDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title("Add link")
                .customView(R.layout.dialog_add_link, false)
                .positiveText("add link")
                .negativeText("cancel")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        final EditText textEt = (EditText) dialog.getView().findViewById(R.id.edit_text_link_text);
                        final EditText urlEt = (EditText) dialog.getView().findViewById(R.id.edit_text_link_url);
                        String text = textEt.getText().toString();
                        String url = urlEt.getText().toString();
                        if (text.length() == 0 || url.length() == 0)
                            Toast.makeText(context, "Fields cannot be blank", Toast.LENGTH_SHORT).show();
                        else {
                            content.append(" [" + text + "](" + url + ") ");
                            content.requestFocus();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .show();
    }

    private void showPreviewDialog() {
        String text = content.getText().toString();
        if (text.length() == 0) {
            Toast.makeText(context, "Nothing to preview", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String html = new Markdown4jProcessor().process(text);
            new MaterialDialog.Builder(context)
                    .title("Preview")
                    .content(Html.fromHtml(html))
                    .positiveText("ok")
                    .show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //text = parseBold(text);
        //text = parseItalics(text);
        //text = parseLinks(text);
    }

    private String trimSpaces(String str) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isWhitespace(c))
                builder.append(c);
        }
        return builder.toString();
    }

    private void showLinkDialog() {
        new MaterialDialog.Builder(context)
                .title("Link")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Text", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (input.toString().length() == 0)
                            Toast.makeText(context, "Field cannot be blank", Toast.LENGTH_SHORT).show();
                        else
                            content.append(input);
                        content.requestFocus();
                    }
                })
                .positiveText("ok")
                .negativeText("cancel")
                .show();
    }

    private void showCaptchaDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title("CAPTCHA")
                .customView(R.layout.dialog_captcha, false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        EditText captchaText = (EditText) dialog.findViewById(R.id.edit_text_captcha);
                        captchaStr = captchaText.getText().toString();
                        Log.d("debug", "CAPTCHA ATTEMPT: " + captchaStr);
                        new SendPmTask().execute();
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .positiveText("submit")
                .negativeText("cancel")
                .autoDismiss(false)
                .show();
        final ImageView captchaImg = (ImageView) dialog.findViewById(R.id.image_captcha);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (needsCaptcha && captcha != null && captchaUrl != null)
                    Picasso.with(context).load(captchaUrl).placeholder(R.drawable.placeholder).into(captchaImg);
            }
        });
    }

    private void issueNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_action_upload)
                        .setContentTitle("Sending message...")
                        .setProgress(0, 0, true)
                        .setAutoCancel(false);

        int mNotificationId = 001;
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private void updateNotification(boolean success) {
        int icon = R.drawable.ic_check_grey600_24dp;
        String text = "Your message was sent successfully!";

        if (!success) {
            icon = R.drawable.ic_close_grey600_24dp;
            text = "Something went wrong. Try again";
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(icon)
                        .setContentTitle(text)
                        .setProgress(0, 0, false)
                        .setAutoCancel(true);

        int mNotificationId = 001;
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private boolean checkFields() {
        userStr = user.getText().toString();
        subjectStr = subject.getText().toString();
        contentStr = content.getText().toString();

        if (userStr.length() == 0 || subjectStr.length() == 0 || contentStr.length() == 0) {
            Toast.makeText(context, "Fields cannot be blank", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private class NeedsCaptchaTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return app.getClient().needsCaptcha();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                Log.d("debug", "NEEDS CAPTCHA");
            }
            else {
                Log.d("debug", "DOESNT NEED CAPTCHA");
                determinedCaptcha = true;
            }
            needsCaptcha = result;
            new GetCaptchaTask().execute();
        }
    }

    private class GetCaptchaTask extends AsyncTask<Void, Void, Captcha> {


        @Override
        protected Captcha doInBackground(Void... params) {
            try {
                return app.getClient().getNewCaptcha();
            } catch (ApiException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Captcha captcha) {
            super.onPostExecute(captcha);
            if (captcha != null) {
                Log.d("debug", "CAPTCHA URL: " + captcha.getImageUrl().toExternalForm());
                captchaUrl = captcha.getImageUrl().toExternalForm();
                determinedCaptcha = true;
                //Picasso.with(context).load(captcha.getImageUrl().toExternalForm()).placeholder(R.drawable.placeholder).into(captchaImg);
            }
            else {
                Toast.makeText(context, "Couldn't get CAPTCHA. Try again", Toast.LENGTH_SHORT).show();
            }
            ComposePmFragment.this.captcha = captcha;
        }
    }

    private class SendPmTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            issueNotification();
            Log.d("debug", userStr + "-" + subjectStr + "-" + contentStr);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            MyInboxManager inbox = new MyInboxManager(app.getClient());
            if (needsCaptcha) {
                try {
                    inbox.compose("", userStr, subjectStr, contentStr, captcha, captchaStr);
                    return true;
                } catch (ApiException e) {
                    e.printStackTrace();
                    Log.d("debug", e.getReason());
                    return false;
                }
            }
            else {
                try {
                    inbox.compose(userStr, subjectStr, contentStr);
                    return true;
                } catch (ApiException e) {
                    e.printStackTrace();
                    Log.d("debug", e.getReason());
                    return false;
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            updateNotification(result);
        }
    }
}
