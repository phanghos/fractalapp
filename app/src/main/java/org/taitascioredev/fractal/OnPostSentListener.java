package org.taitascioredev.fractal;

import net.dean.jraw.models.Submission;

/**
 * Created by roberto on 30/05/15.
 */
public interface OnPostSentListener {

    void OnSuccess(Submission submission);
    void OnFailure();
}
