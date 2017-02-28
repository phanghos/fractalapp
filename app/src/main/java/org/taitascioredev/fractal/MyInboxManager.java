package org.taitascioredev.fractal;

import net.dean.jraw.ApiException;
import net.dean.jraw.EndpointImplementation;
import net.dean.jraw.Endpoints;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.http.RestResponse;
import net.dean.jraw.managers.InboxManager;
import net.dean.jraw.models.Captcha;
import net.dean.jraw.util.JrawUtils;

import java.util.Map;

/**
 * Created by roberto on 01/06/15.
 */
public class MyInboxManager extends InboxManager {

    public MyInboxManager(RedditClient client) {
        super(client);
    }

    @Override
    public void compose(String to, String subject, String body) throws NetworkException, ApiException {
        compose("", to, subject, body, null, null);
    }

    @EndpointImplementation(Endpoints.COMPOSE)
    public void compose(String from, String to, String subject, String body, Captcha captcha, String captchaAttempt) throws NetworkException, ApiException, ApiException {
        Map<String, String> args = JrawUtils.mapOf(
                "api_type", "json",
                "from_sr", from,
                "subject", subject,
                "text", body,
                "to", to
        );

        if (captcha != null) {
            if (captchaAttempt == null) {
                throw new IllegalArgumentException("Captcha present but the attempt is not");
            }

            args.put("iden", captcha.getId());
            args.put("captcha", captchaAttempt);
        }

        RestResponse response = reddit.execute(reddit.request()
                .endpoint(Endpoints.COMPOSE)
                .post(args)
                .build());
        if (response.hasErrors()) {
            throw response.getError();
        }
    }
}
