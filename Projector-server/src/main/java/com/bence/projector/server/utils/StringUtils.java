package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtils {

    private static final int N = 2000;
    private static final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static int[][] t = null;

    public static String stripAccents(String s) {
        String nfdNormalizedString = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = pattern.matcher(nfdNormalizedString).replaceAll("");
        s = s.replaceAll("[^a-zA-Z0-9]", "");
        return s;
    }

    public synchronized static int highestCommonStringInt(String a, String b) {
        int aLength = a.length();
        if (aLength <= 0) {
            return 0;
        }
        int bLength = b.length();
        if (bLength <= 0) {
            return 0;
        }
        int i;
        int j = 0;
        if (t == null) {
            t = new int[N][];
            for (i = 0; i < N; ++i) {
                t[i] = new int[N];
                t[i][0] = 0;
            }
            for (j = 1; j < N; ++j) {
                t[0][j] = 0;
            }
        }
        char c;
        if (aLength >= N - 1) {
            aLength = N - 2;
        }
        if (bLength >= N - 1) {
            bLength = N - 2;
        }
        for (i = 0; i < aLength; ++i) {
            c = a.charAt(i);
            for (j = 0; j < bLength; ++j) {
                if (c == b.charAt(j)) {
                    t[i + 1][j + 1] = t[i][j] + 1;
                } else //noinspection ManualMinMaxCalculation
                    if (t[i + 1][j] > t[i][j + 1]) {
                        t[i + 1][j + 1] = t[i + 1][j];
                    } else {
                        t[i + 1][j + 1] = t[i][j + 1];
                    }
            }
        }
        return t[i][j];
    }

    public static int longestCommonSubString(String a, String b) {
        char[] X = a.toCharArray();
        char[] Y = b.toCharArray();
        int m = a.length();
        int n = b.length();
        int[][] LCStuff = new int[m + 1][n + 1];
        int result = 0;
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0 || j == 0)
                    LCStuff[i][j] = 0;
                else if (X[i - 1] == Y[j - 1]) {
                    LCStuff[i][j] = LCStuff[i - 1][j - 1] + 1;
                    result = Integer.max(result, LCStuff[i][j]);
                } else
                    LCStuff[i][j] = 0;
            }
        }
        return result;
    }

    public static void formatSongs(List<Song> songs) {
        for (Song song : songs) {
            song.setTitle(format(song.getTitle()));
            formatSongVerses(song.getVerses());
        }
    }

    private static void formatSongVerses(List<SongVerse> songVerses) {
        SongVerse lastVerse = null;
        for (SongVerse songVerse : songVerses) {
            songVerse.setText(format(songVerse.getText()));
            lastVerse = songVerse;
        }
        if (lastVerse != null) {
            String text = lastVerse.getText();
            lastVerse.setText(text.replaceAll("\nEnd$", ""));
        }
    }

    public static String format(String s) {
        String newValue = s.trim();
        newValue = newValue.replaceAll("([ \\t])([.?!,':])", "$2");
        newValue = newValue.replaceAll("([.?!,:])([^ “\".?!,:)])", "$1 $2");
        newValue = newValue.replaceAll(": /", " :/");
        newValue = newValue.replaceAll("/ :", "/: ");
        newValue = newValue.replaceAll(" {2}", " ");
        newValue = newValue.replaceAll("\\. \\. \\.", "…");
        newValue = newValue.replaceAll("\\.\\.\\.", "…");
        newValue = newValue.replaceAll("\\.([^ \"])", ". $1");
        newValue = newValue.replaceAll(" \\)", ")");
        newValue = newValue.replaceAll("\\( ", "(");
        newValue = newValue.replaceAll("\\. \"", ".\"");
        newValue = newValue.replaceAll("! \"", "!\"");
        newValue = newValue.replaceAll("\r\n", "\n");
        newValue = newValue.replaceAll("\n\n", "\n");
        newValue = newValue.replaceAll(" \t", " ");
        newValue = newValue.replaceAll("\t ", " ");
        newValue = newValue.replaceAll(" \n", "\n");
        newValue = newValue.replaceAll("\n ", "\n");
        newValue = newValue.replaceAll("\t\n", "\n");
        newValue = newValue.replaceAll("Ş", "Ș");
        newValue = newValue.replaceAll("ş", "ș");
        newValue = newValue.replaceAll("Ţ", "Ț");
        newValue = newValue.replaceAll("ţ", "ț");
        newValue = newValue.replaceAll("ã", "ă");
        newValue = newValue.replaceAll("õ", "ő");
        newValue = newValue.replaceAll("Õ", "Ő");
        newValue = newValue.replaceAll("û", "ű");
        return newValue;
    }
}
