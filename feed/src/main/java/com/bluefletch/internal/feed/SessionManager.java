package com.bluefletch.internal.feed;

import android.content.Context;
import android.content.SharedPreferences;

import com.bluefletch.internal.feed.rest.User;

/**
 * Container to manage the user session.  This will persist a session in Shared Preferences so that
 * users do not have to log in again after exiting the application.  Provides an API to initialize
 * a session on login and terminate a session on logout or session expiration.
 *
 * Created by mattmehalso on 4/3/14.
 */
public class SessionManager {
    private static final String SHARED_PREF_SESSION_NAME = "feed-session";

    private SharedPreferences prefs;

    private Session cachedSession;

    /**
     * Primary constructor - provide context for access to SharedPrefs.
     * @param context
     */
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(SHARED_PREF_SESSION_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Initializes a new session, overwriting the previous one if exists (should not).  The session
     * data will be cached for quick retrieval and persisted in Shared Preferences storage.
     *
     * @param sessionToken
     * @param user
     */
    public void initializeSession(String sessionToken, User user) {
        cachedSession = new Session(sessionToken, user);
        persistSession(cachedSession);
    }

    /**
     * Terminates the current session, if one exists.  The session data will also be removed from
     * Shared Preferences storage.
     */
    public void terminateSession() {
        cachedSession = null;
        persistSession(null);
    }

    /**
     * Gets the currently signed on user.
     * @return
     */
    public User getUser() {
        if(cachedSession == null) {
            cachedSession = retrieveSavedSession();
        }

        return cachedSession == null ? null : cachedSession.getUser();
    }

    /**
     * Gets the current session token (for future requests).
     * @return
     */
    public String getSessionToken() {
        if(cachedSession == null) {
            cachedSession = retrieveSavedSession();
        }

        return cachedSession == null ? null : cachedSession.getCookie();
    }

    /**
     * Convience method to dtermine whether session is currently active / available (so far as the
     * app knows locally).
     * @return
     */
    public boolean isSessionActive() {
        if(cachedSession == null) {
            cachedSession = retrieveSavedSession();
        }

        return cachedSession != null;
    }

    /**
     * Helper method to persist the user session in Shared Prefs
     * @param session
     */
    private void persistSession(Session session) {
        if(session != null) {
            User user = session.getUser();
            prefs.edit()
                    .putString("_id", user.get_id())
                    .putString("createdDate", user.getCreatedDate())
                    .putString("imageUrl", user.getImageUrl())
                    .putString("lastActionDate", user.getLastActionDate())
                    .putString("username", user.getUsername())
                    .putString("cookie", session.getCookie())
                    .commit();
        } else {
            prefs.edit().clear().commit();
        }
    }

    /**
     * Helper method to retrieve the user session from Shared Prefs.
     * @return
     */
    private Session retrieveSavedSession() {
        if("EMPTY".equals(prefs.getString("_id", "EMPTY"))) {
            return null;
        } else {
            User user = new User();
            user.set_id(prefs.getString("_id", "_id"));
            user.setCreatedDate(prefs.getString("createdDate", "createdDate"));
            user.setImageUrl(prefs.getString("imageUrl", "imageUrl"));
            user.setLastActionDate(prefs.getString("lastActionDate", "lastActionDate"));
            user.setUsername(prefs.getString("username", "username")); 
            String cookie = prefs.getString("cookie", "cookie");
            Session session = new Session(cookie, user);

            return session;
        }
    }

    /**
     * Session POJO contains user information and the current session cookie.
     */
    private static final class Session {
        private String cookie;
        private User user;

        public Session(String cookie, User user) {
            this.cookie = cookie;
            this.user = user;
        }

        public String getCookie() {
            return cookie;
        }

        public User getUser() {
            return user;
        }
    }
}
