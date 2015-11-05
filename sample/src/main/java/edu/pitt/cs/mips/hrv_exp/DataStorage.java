package edu.pitt.cs.mips.hrv_exp;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

class DataSample {
    public long timestamp;
    public int value;

    public DataSample(long l, int i) {
        timestamp = l;
        value = i;
    }

    public static String toCSV(ArrayList<DataSample> arraylist) {
        StringBuilder stringbuilder = new StringBuilder();

        for (Iterator<DataSample> iterator = arraylist.iterator(); iterator.hasNext(); ) {
            DataSample sample = iterator.next();

            stringbuilder.append("" + sample.timestamp + "," + sample.value + "\r\n");
        }

        return stringbuilder.toString();
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public int getValue() {
        return value;
    }

    public boolean equals(Object o) {
        DataSample newData = (DataSample) o;

        if (this.timestamp == newData.getTimeStamp() && this.value == newData.getValue()) {
            return true;
        } else {
            return false;
        }
    }
}

class DataSample2 {

    public long timestamp;
    public int value;
    public long video_timestamp;

    public DataSample2(long l, int i, long l2 ) {
        timestamp = l;
        value = i;
        video_timestamp = l2;
    }

    public static String toCSV(ArrayList<DataSample2> arraylist) {
        StringBuilder stringbuilder = new StringBuilder();

        for (Iterator<DataSample2> iterator = arraylist.iterator(); iterator.hasNext(); ) {
            DataSample2 sample = iterator.next();

            stringbuilder.append("" + sample.timestamp + "," + sample.value + "," + sample.video_timestamp + "\r\n");
        }

        return stringbuilder.toString();
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public int getValue() {
        return value;
    }

    public long getVideoTimestamp() {
        return video_timestamp;
    }

    public boolean equals(Object o) {
        DataSample2 newData = (DataSample2) o;

        if (this.timestamp == newData.getTimeStamp() && this.value == newData.getValue() && this.video_timestamp == newData.getVideoTimestamp() ) {
            return true;
        } else {
            return false;
        }
    }
}

public class DataStorage {

    private DataStorage(Context c) {
        context = c;
        samples = new ArrayList<DataSample>(10000);
        System.out.println("Samples is ~~~~~~~~~~~~`"+samples);
        samples1 = new ArrayList<DataSample2>(10000);
        samples2 = new ArrayList<DataSample>(10000);
        samples3 = new ArrayList<DataSample>(10000);
        samples4 = new ArrayList<DataSample>(10000);
        samples5 = new ArrayList<DataSample>(10000);
        samples6 = new ArrayList<DataSample>(10000);
        samples7 = new ArrayList<DataSample>(10000);
        samples8 = new ArrayList<DataSample>(10000);
        samples9 = new ArrayList<DataSample>(10000);
        samples10 = new ArrayList<DataSample>(10000);
        samples11 = new ArrayList<DataSample>(10000);
        samples12 = new ArrayList<DataSample>(10000);
        samples13 = new ArrayList<DataSample>(10000);
        samples14 = new ArrayList<DataSample>(10000);
        samples15 = new ArrayList<DataSample>(10000);
        samples16 = new ArrayList<DataSample>(10000);
    }

    public static boolean AddSample(long l, int i) {
        if (instance != null) {
            instance.add(l, i);
            System.out.println("AddSample: "+Long.toString(l)+";"+Integer.toString(i));
            return true;
        }

        return false;
    }

//    public static boolean AddSample(long l, int i, long l2) {
//        if (instance != null) {
//            instance.add(l, i, l2);
//
//            return true;
//        }
//
//        return false;
//    }

    public static boolean AddPeak(long l, int i) {
        if (instance != null) {
            instance.add2(l, i);

            return true;
        }

        return false;
    }

    public static boolean AddWordIndex(long l, int i) {
        if (instance != null) {
            instance.add8(l, i);

            return true;
        }

        return false;
    }

    public static boolean AddYSample(long l, int i) {
        if (instance != null) {
            instance.add11(l, i);

            return true;
        }

        return false;
    }

    public static boolean AddUSample(long l, int i) {
        if (instance != null) {
            instance.add12(l, i);

            return true;
        }

        return false;
    }

    public static boolean AddVSample(long l, int i) {
        if (instance != null) {
            instance.add13(l, i);

            return true;
        }

        return false;
    }

    public static boolean AddRSample(long l, int i) {
        if (instance != null) {
            instance.add14(l, i);

            return true;
        }

        return false;
    }

    public static boolean AddGSample(long l, int i) {
        if (instance != null) {
            instance.add15(l, i);

            return true;
        }

        return false;
    }

    public static boolean AddBSample(long l, int i) {
        if (instance != null) {
            instance.add16(l, i);

            return true;
        }

        return false;
    }

    public static boolean AddZeroCross(long l, int i) {
        if (instance != null) {
            instance.add3(l, i);

            return true;
        }

        return false;
    }

    public static boolean AddBpm(long l, int i) {
        if (instance != null) {
            instance.add4(l, i);

            return true;
        }

        return false;
    }

    public static boolean AddRawBpm(long l, int i) {
        if (instance != null) {
            instance.add5(l, i);

            return true;
        }

        return false;
    }

    public static boolean DeletePeak(long l, int i) {
        if (instance != null) {
            return instance.delete(l, i);
        }

        return false;
    }

    public static boolean DeleteZeroCross(long l, int i) {
        if (instance != null) {
            return instance.delete2(l, i);
        }

        return false;
    }

    public static boolean AddCoverMoment(long l, int i) {
        if (instance != null) {
            instance.add6(l, i);

            return true;
        }

        return false;
    }

    public static boolean AddUncoverMoment(long l, int i) {
        if (instance != null) {
            instance.add7(l, i);

            return true;
        }

        return false;
    }

    public static DataStorage getInstance(Context c) {
        if (instance == null) {
            instance = new DataStorage(c);
        }

        return instance;
    }

    public boolean setUser (String s) {
        if ( instance != null) {
            userId = s;

            return true;
        }

        return false;
    }

    public void add(long l, int i) {
        if (samples != null) {
            DataSample sample = new DataSample(l, i);
            samples.add(sample);
        }
    }

    public void add(long l, int i, long l2 ) {
        if (samples1 != null) {
            DataSample2 sample = new DataSample2(l, i, l2);
            samples1.add(sample);
        }
    }

    public void add2(long l, int i) {
        DataSample sample = new DataSample(l, i);

        if (samples2 != null) {
            samples2.add(sample);
        }

        if (samples3 != null) {
            samples3.add(sample);
        }
    }

    public void add3(long l, int i) {
        DataSample sample = new DataSample(l, i);

        if (samples4 != null) {
            samples4.add(sample);
        }

        if (samples5 != null) {
            samples5.add(sample);
        }
    }

    public void add4(long l, int i) {
        if (samples6 != null) {
            DataSample sample = new DataSample(l, i);
            samples6.add(sample);
        }
    }

    public void add5(long l, int i) {
        if (samples7 != null) {
            DataSample sample = new DataSample(l, i);
            samples7.add(sample);
        }
    }

    public void add6(long l, int i) {
        if (samples8 != null) {
            DataSample sample = new DataSample(l, i);
            samples8.add(sample);
        }
    }

    public void add7(long l, int i) {
        if (samples9 != null) {
            DataSample sample = new DataSample(l, i);
            samples9.add(sample);
        }
    }

    public void add8(long l, int i) {
        if (samples10 != null) {
            DataSample sample = new DataSample(l, i);
            samples10.add(sample);
        }
    }

    public void add11(long l, int i) {
        if (samples11 != null) {
            DataSample sample11 = new DataSample(l, i);
            samples11.add(sample11);
        }
    }

    public void add12(long l, int i) {
        if (samples12 != null) {
            DataSample sample12 = new DataSample(l, i);
            samples12.add(sample12);
        }
    }

    public void add13(long l, int i) {
        if (samples13 != null) {
            DataSample sample13 = new DataSample(l, i);
            samples13.add(sample13);
        }
    }

    public void add14(long l, int i) {
        if (samples14 != null) {
            DataSample sample14 = new DataSample(l, i);
            samples14.add(sample14);
        }
    }

    public void add15(long l, int i) {
        if (samples15 != null) {
            DataSample sample15 = new DataSample(l, i);
            samples15.add(sample15);
        }
    }

    public void add16(long l, int i) {
        if (samples16 != null) {
            DataSample sample16 = new DataSample(l, i);
            samples16.add(sample16);
        }
    }


    public boolean delete(long l, int i) {
        boolean result = false;

        if (samples3 != null) {
            DataSample sample = new DataSample(l, i);
            result = samples3.remove(sample);
        }

        return result;
    }

    public boolean delete2(long l, int i) {
        boolean result = false;

        if (samples5 != null) {
            DataSample sample = new DataSample(l, i);
            result = samples5.remove(sample);
        }

        return result;
    }

    public void clearData() {
        if (samples != null) {
            samples.clear();
        }

        if (samples1 != null) {
            samples1.clear();
        }

        if (samples2 != null) {
            samples2.clear();
        }

        if (samples3 != null) {
            samples3.clear();
        }

        if (samples4 != null) {
            samples4.clear();
        }

        if (samples5 != null) {
            samples5.clear();
        }

        if (samples6 != null) {
            samples6.clear();
        }

        if (samples7 != null) {
            samples7.clear();
        }

        if (samples8 != null) {
            samples8.clear();
        }

        if (samples9 != null) {
            samples9.clear();
        }

        if (samples10 != null) {
            samples10.clear();
        }
    }
//public String save( String course, String lecture, String topic, String time ) {
    public String save( String title, long time ) {

        if ((samples == null && samples10 == null) || (samples.size() == 0 && samples10.size() == 0)) {
            return "samples null";
        }

        Log.i("DataStorage", "end");

        String directory = context.getExternalFilesDir(null) + "/pitthrv/AttentiveLearnerData/" + userId + "/" + title + "/" + time + "/";
        System.out.println("directory is: ~~~~~~~~~~~~"+directory);

        File dir = null;

        String filename = "samples.csv";
        String filename2 = "raw_peaks.csv";
        String filename3 = "valid_peaks.csv";
        String filename4 = "raw_zerocross.csv";
        String filename5 = "valid_zerocross.csv";
        String filename6 = "Bpm.csv";
        String filename7 = "RawBpm.csv";
        String filename8 = "LensCover.csv";
        String filename9 = "LensUncover.csv";
        String filename10 = "WordIndex.csv";
        String filename11 = "yData.csv";
        String filename12 = "uData.csv";
        String filename13 = "vData.csv";
        String filename14 = "rData.csv";
        String filename15 = "gData.csv";
        String filename16 = "bData.csv";

        try {
            OutputStreamWriter outputstreamwriter;

            if ( samples != null && samples.size() != 0) {
                dir = new File(directory + "quiz/");

                if (!dir.exists())
                    dir.mkdirs();

                File file = new File(dir, filename);
                outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file, true));

                outputstreamwriter.write(DataSample.toCSV(samples));
            } else {
                dir = new File(directory);

                if (!dir.exists())
                    dir.mkdirs();

                File file = new File(dir, filename);
                outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file, true));

                outputstreamwriter.write(DataSample2.toCSV(samples1));
            }
            outputstreamwriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.i("DataStorage", e.toString());
        }

        if (samples2 != null && samples2.size() != 0) {
            File file2 = new File(dir, filename2);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file2, true));

                outputstreamwriter.write(DataSample.toCSV(samples2));
                outputstreamwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples3 != null && samples3.size() != 0) {
            File file3 = new File(dir, filename3);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file3, true));

                outputstreamwriter.write(DataSample.toCSV(samples3));
                outputstreamwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples4 != null && samples4.size() != 0) {
            File file4 = new File(dir, filename4);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file4, true));

                outputstreamwriter.write(DataSample.toCSV(samples4));
                outputstreamwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples5 != null && samples5.size() != 0) {
            File file5 = new File(dir, filename5);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file5, true));

                outputstreamwriter.write(DataSample.toCSV(samples5));
                outputstreamwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples6 != null && samples6.size() != 0) {
            File file6 = new File(dir, filename6);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file6, true));

                outputstreamwriter.write(DataSample.toCSV(samples6));

                int count = 0;
                int sum = 0;
                int bmp_average = 0;

                for (int i = 0; i < samples6.size(); i++) {
                    int bmp_value = samples6.get(i).value;

                    if (bmp_value > 0 && bmp_value < 150) {
                        count++;
                        sum = sum + bmp_value;
                    }
                }

                if (count > 0) {
                    bmp_average = sum / count;
                }

                outputstreamwriter.append("" + bmp_average);
                outputstreamwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples7 != null && samples7.size() != 0) {
            File file7 = new File(dir, filename7);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file7, true));

                outputstreamwriter.write(DataSample.toCSV(samples7));
                outputstreamwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples8 != null && samples8.size() != 0) {
            File file8 = new File(dir, filename8);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file8, true));

                outputstreamwriter.write(DataSample.toCSV(samples8));
                outputstreamwriter.close();
                Log.i("DataStorage", "write cover time complete.");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples9 != null && samples9.size() != 0) {
            File file9 = new File(dir, filename9);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file9, true));

                outputstreamwriter.write(DataSample.toCSV(samples9));
                outputstreamwriter.close();
                Log.i("DataStorage", "write uncover time complete.");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples10 != null && samples10.size() != 0) {
            File file10 = new File(dir, filename10);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file10, true));

                outputstreamwriter.write(DataSample.toCSV(samples10));
                outputstreamwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples11 != null && samples11.size() != 0) {
            File file11 = new File(dir, filename11);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file11, true));

                outputstreamwriter.write(DataSample.toCSV(samples11));
                outputstreamwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples12 != null && samples12.size() != 0) {
            File file12 = new File(dir, filename12);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file12, true));

                outputstreamwriter.write(DataSample.toCSV(samples12));
                outputstreamwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples13 != null && samples13.size() != 0) {
            File file13 = new File(dir, filename13);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file13, true));

                outputstreamwriter.write(DataSample.toCSV(samples13));
                outputstreamwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples14 != null && samples14.size() != 0) {
            File file14 = new File(dir, filename14);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file14, true));

                outputstreamwriter.write(DataSample.toCSV(samples14));
                outputstreamwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples15 != null && samples15.size() != 0) {
            File file15 = new File(dir, filename15);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file15, true));

                outputstreamwriter.write(DataSample.toCSV(samples15));
                outputstreamwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        if (samples16 != null && samples16.size() != 0) {
            File file16 = new File(dir, filename16);

            try {
                OutputStreamWriter outputstreamwriter = new OutputStreamWriter(new FileOutputStream(file16, true));

                outputstreamwriter.write(DataSample.toCSV(samples16));
                outputstreamwriter.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("DataStorage", e.toString());
            }
        }

        return "saved";
    }

    private static Context context;
    private static DataStorage instance;

    private String userId;
    private ArrayList<DataSample> samples;
    private ArrayList<DataSample2> samples1;
    private ArrayList<DataSample> samples2;
    private ArrayList<DataSample> samples3;
    private ArrayList<DataSample> samples4;
    private ArrayList<DataSample> samples5;
    private ArrayList<DataSample> samples6;
    private ArrayList<DataSample> samples7;
    private ArrayList<DataSample> samples8;
    private ArrayList<DataSample> samples9;
    private ArrayList<DataSample> samples10;
    private ArrayList<DataSample> samples11;
    private ArrayList<DataSample> samples12;
    private ArrayList<DataSample> samples13;
    private ArrayList<DataSample> samples14;
    private ArrayList<DataSample> samples15;
    private ArrayList<DataSample> samples16;
}
