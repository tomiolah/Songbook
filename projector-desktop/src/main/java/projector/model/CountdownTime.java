package projector.model;

import com.j256.ormlite.field.DatabaseField;

public class CountdownTime extends BaseEntity {

    @DatabaseField
    private String timeText;
    @DatabaseField
    private Long counter;

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }

    public long getCounter() {
        if (counter == null) {
            return 0;
        }
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }
}
