//this code merely does one thing

import net.miginfocom.swing.MigLayout;
import web.AuthenticationService;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import javax.swing.*;

//this does the fields in the screen and the buttons that come with
public class LoginForm extends JPanel {
    JLabel title, emailLabel, passwordLabel;
    JTextField emailField;
    JButton loginBtn;
    JPasswordField passwordField;

    //this is the form of the login  and the text and button fields
    LoginForm() {
        title = new JLabel("Login Form");
        title.setForeground(Color.blue);

        emailLabel = new JLabel("Email");
        passwordLabel = new JLabel("Password");
        emailField = new JTextField();
        passwordField = new JPasswordField();
        loginBtn = new JButton("Login");

//        title.setBounds(100, 30, 400, 30);
//        emailLabel.setBounds(80, 70, 200, 30);
//        passwordLabel.setBounds(80, 110, 200, 30);
//        emailField.setBounds(300, 70, 200, 30);
//        passwordField.setBounds(300, 110, 200, 30);
//        loginBtn.setBounds(150, 160, 100, 30);

        setLayout(new MigLayout(
                "hidemode 3",
                // columns
                "[fill]" +
                        "[fill]" +
                        "[fill]" +
                        "[fill]" +
                        "[fill]" +
                        "[fill]" +
                        "[fill]" +
                        "[fill]" +
                        "[fill]" +
                        "[fill]" +
                        "[fill]" +
                        "[fill]" +
                        "[fill]" +
                        "[fill]",
                // rows
                "[]" +
                        "[]" +
                        "[]" +
                        "[]" +
                        "[]"));

        add(title, "cell 0 0 7 1");

        add(emailLabel, "cell 0 1 2 1");
        add(emailField, "cell 2 1 12 1");

        add(passwordLabel, "cell 0 2 2 1");
        add(passwordField, "cell 2 2 12 1");

        loginBtn.addActionListener(this::loginBtnActionPerformed);
        add(loginBtn, "cell 2 3 2 1");
    }

    private void loginBtnActionPerformed(ActionEvent e) {
        String username = emailField.getText();
        String password = passwordField.getText();
        String userToken = AuthenticationService.userLogin(username, password);
        if (userToken == null){
            title.setText("Invalid Email/Password");
        } else {
            title.setText("Login success");
        }
    }

}
