package projector.application;

public class SongVersTime {

    private String songTitle;
    private double[] versTimes;

    public SongVersTime(String songTitle, int n) {
        this.songTitle = songTitle;
        versTimes = new double[n];
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public double[] getVersTimes() {
        return versTimes;//== null ? null : versTimes.clone();
    }

    public void setVersTimes(double[] versTimes) {
        this.versTimes = versTimes == null ? null : versTimes.clone();
    }

    public int countNotZero() {
        int count = 0;
        for (double x : versTimes) {
            if (x != 0) {
                ++count;
            }
        }
        return count;
    }
}
