package com.tvpal.kobi.tvpal.Model;

import android.content.Context;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;

/**
 * Created by Kobi on 03/06/2016.
 */
public class FireBaseModel {

    Firebase myFirebaseRef;

    FireBaseModel(Context context){
        Firebase.setAndroidContext(context);
        myFirebaseRef = new Firebase("https://fiery-torch-7421.firebaseio.com/");
    }

    public void createUser(final User u,final Model.UserCreator next) {
        myFirebaseRef.createUser(u.getEmail(), u.getPassword(), new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> result) {
                Firebase userRef = myFirebaseRef.child("users").child(result.get("uid").toString());
                userRef.setValue(u);
                next.onResult(u);
                System.out.println("Successfully created user account with uid: " + result.get("uid"));
            }
            @Override
            public void onError(FirebaseError firebaseError) {
                Log.d("TAG:", firebaseError.toString());
                next.onError(firebaseError.toString());
            }
        });
    }

    private String getObjectKey(String invalidKey)
    {
        String[] components = invalidKey.split(".");
        String str=components[0];
        for (int i=1;i<components.length;i++) {
            str+="_"+components[i];
        }
        return str;
    }

    public void authenticate(String email, String pwd,Firebase.AuthResultHandler auth) {
            myFirebaseRef.authWithPassword(email, pwd, auth);
    }

    public boolean isAuthenticated() {
        return myFirebaseRef.getAuth()!=null;
    }
}
