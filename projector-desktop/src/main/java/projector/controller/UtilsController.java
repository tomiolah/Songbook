package projector.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static projector.utils.CountDownTimerUtil.getRemainedDate;
import static projector.utils.CountDownTimerUtil.getTimeTextFromDate;


public class UtilsController {

    @FXML
    private Label countDownLabel;
    @FXML
    private TextField timeTextField;
    private ProjectionScreenController projectionScreenController;

    public void initialize() {
        timeTextField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String character = event.getCharacter();
            if (!character.matches("[0-9]") && (timeTextField.getText().contains(":") && character.equals(":"))) {
                event.consume();
            }
        });
        timeTextField.setOnKeyReleased(event -> setCountDownValue());
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    setCountDownValue();
                    //noinspection BusyWait
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void setCountDownValue() {
        String timeTextFromDate = getTimeTextFromDate(getRemainedDate(getFinishDate()));
        if (!timeTextFromDate.isEmpty() && !countDownLabel.getText().equals(timeTextFromDate)) {
            Platform.runLater(() -> countDownLabel.setText(timeTextFromDate));
        }
    }

    private Date getFinishDate() {
        String timeTextFieldText = timeTextField.getText();
        String[] split = timeTextFieldText.split(":");
        if (split.length < 2) {
            return null;
        }
        long hour = Integer.parseInt(split[0]);
        long minute = Integer.parseInt(split[1]);
        Date now = new Date();
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(now);   // assigns calendar to given date
        long hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        long minuteOfHour = calendar.get(Calendar.MINUTE);
        long secondsOfMinute = calendar.get(Calendar.SECOND);
        long millisecondsTo = (((hour - hourOfDay) * 60 - minuteOfHour + minute) * 60 - secondsOfMinute) * 1000;
        return new Date(now.getTime() + millisecondsTo);
    }

    public void onShowCountDownButtonEvent() {
        projectionScreenController.setCountDownTimer(getFinishDate());
    }

    public void setProjectionScreenController(ProjectionScreenController projectionScreenController) {
        this.projectionScreenController = projectionScreenController;
    }
}
