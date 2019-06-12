package jarvizz.project.models;

public class SuperUser {
    private String username;
    private  String password;
    private static SuperUser superUser = new SuperUser("ADMIN","ADMIN");


    private SuperUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static SuperUser getInstance() {
        return superUser ;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
