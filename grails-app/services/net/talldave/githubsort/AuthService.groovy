package net.talldave.githubsort

import org.apache.commons.codec.binary.Base64

class AuthService {

    static transactional = false

    // scoping this to session instead of as a singleton since I'm storing per-user data here
    static scope = "session"
    // note: I'm not in love with this approach but in the interests of MVP thinking it will get the job done

    static proxy = true

    // private is obviously ignored in groovy, in a non-MVP could increase security around this class
    private String username
    private String password

    boolean isLoggedIn() {
        username && password
    }

    String getUser() {
        username
    }

    void logout() {
        username = null
        password = null
    }

    void login(String user, String pass) {
        username = user
        password = pass
    }

    String encodeCredentials(String separator=':') {
        def bytes = Base64.encodeBase64("${username}${separator}${password}".bytes)
        new String(bytes)
    }

}
