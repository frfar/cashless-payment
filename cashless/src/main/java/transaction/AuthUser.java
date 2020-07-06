package transaction;

public class AuthUser {

    private int id;
    private String name;
    private String contact;
    private String email;
    private String token;
    private boolean isAdmin;

    public void setToken(String token) {
        this.token = token;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContact() {
        return contact;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
