package projector.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

public class SongVersTimes {

    private static final Logger LOG = LoggerFactory.getLogger(SongVersTime.class);

    private static SongVersTimes instance;
    private List<SongVersTime> timesList;

    private SongVersTimes() {
        FileInputStream fstream;
        try {
            fstream = new FileInputStream("songVersTimes");
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF-8"));
            br.mark(4);
            if ('\ufeff' != br.read()) {
                br.reset(); // not the BOM marker
            }
            String strLine;
            DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
            timesList = new LinkedList<>();
            while (br.ready() && (strLine = br.readLine()) != null) {
                try {
                    formatter.parse(strLine);
                    readTitleAndTimes(br, strLine);
                    break;
                } catch (ParseException ignored) {
                }
            }
            while (br.ready() && (strLine = br.readLine()) != null) {
                if (strLine.trim().isEmpty()) {
                    continue;
                }
                try {
                    formatter.parse(strLine);
                } catch (ParseException e) {
                    if (readTitleAndTimes(br, strLine)) break;
                }
            }
            br.close();
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            timesList = new LinkedList<>();
            SongVersTime tmp = new SongVersTime("", 0);
            timesList.add(tmp);
        }
    }

    public synchronized static SongVersTimes getInstance() {
        if (instance == null) {
            instance = new SongVersTimes();
        }
        return instance;
    }

    public static void main(String[] args) {
        SongVersTimes songVersTimes = SongVersTimes.getInstance();
        double[] times = songVersTimes.getAverageTimes("h183. ");
        for (double time : times) {
            System.out.print(time + " ");
        }
        System.out.println();
    }

    private boolean readTitleAndTimes(BufferedReader br, String strLine) throws IOException {
        String strLine2 = br.readLine();
        if (strLine2 == null) {
            return true;
        }
        String[] split = strLine2.split(" ");
        try {
            SongVersTime tmp = new SongVersTime(strLine, split.length);
            double[] times = tmp.getVersTimes();
            for (int i = 0; i < split.length; ++i) {
                times[i] = Double.parseDouble(split[i]);
            }
            timesList.add(tmp);
        } catch (NumberFormatException ignored) {
        }
        return false;
    }

    public synchronized double[] getAverageTimes(String title) {
        if (timesList == null) {
            return new double[0];
        }
        int i = timesList.size() - 1;
        while (i >= 0 && !timesList.get(i).getSongTitle().equals(title)) {
            --i;
        }
        if (i >= 0 && timesList.get(i).getSongTitle().equals(title)) {
            double[] times = timesList.get(i).getVersTimes();
            int[] counts = new int[times.length];
            for (int j = 0; j < times.length; ++j) {
                counts[j] = 1;
            }
            while (i > 0) {
                --i;
                if (timesList.get(i).getSongTitle().equals(title)) {
                    double[] t2 = timesList.get(i).getVersTimes();
                    for (int j = 0; j < t2.length && j < times.length; ++j) {
                        times[j] += t2[j];
                        ++counts[j];
                    }
                }
            }
            for (int j = 0; j < times.length; ++j) {
                times[j] /= counts[j];
            }
            return times;
        }
        return null;
    }

}
