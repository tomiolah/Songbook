package projector.controller;

import com.bence.projector.common.dto.LoginDTO;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import projector.controller.listener.LoginListener;

import java.util.ArrayList;
import java.util.List;

public class LoginController {

    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField passwordTextField;
    private List<LoginListener> loginListeners;

    public void addListener(LoginListener loginListener) {
        if (loginListeners == null) {
            loginListeners = new ArrayList<>(1);
        }
        loginListeners.add(loginListener);
    }

    public void loginButtonOnAction() {
        final LoginDTO user = new LoginDTO();
        user.setUsername(emailTextField.getText().trim());
        user.setPassword(passwordTextField.getText().trim());
        for (LoginListener loginListener : loginListeners) {
            loginListener.onLogin(user);
        }
    }
}
