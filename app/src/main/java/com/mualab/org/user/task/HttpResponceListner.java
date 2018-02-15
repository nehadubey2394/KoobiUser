package com.mualab.org.user.task;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by dharmraj on 14/7/17.
 */

public class HttpResponceListner {

    public interface Listener {
        /**
         * Called when a response is received.
         */
        void onResponse(String response, String apiName);

        void ErrorListener(VolleyError error);
    }

    public interface LoginRegistrationListener {
        /**
         * Called when a response is received.
         */
        void onResponse(String response);

        void ErrorListener(VolleyError error);
    }


    public interface FirebaseListener {
        /**
         * Called when a response is received.
         **/
        void isSuccessfulLogin(FirebaseUser user);

        void isSuccessfulRegistration(FirebaseUser user);

        void ErrorListener(Task<AuthResult> task);
    }
}
