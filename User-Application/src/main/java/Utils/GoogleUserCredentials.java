package Utils;

public class GoogleUserCredentials {
    public String name;
    public String token;
    public String id;
    public String email;

    public GoogleUserCredentials(String idToken, String userId, String email, String name) {
        this.token = idToken;
        this.id = userId;
        this.email = email;
        this.name = name;
    }
}
