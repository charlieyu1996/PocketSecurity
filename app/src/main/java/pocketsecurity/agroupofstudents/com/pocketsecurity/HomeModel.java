package pocketsecurity.agroupofstudents.com.pocketsecurity;

import android.os.Environment;
import com.github.mikephil.charting.data.Entry;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Observable;
import pocketsecurity.agroupofstudents.com.pocketsecurity.custom.peopleDate;

/**
 * Created by charlieyu on 2019-07-06.
 */

public class HomeModel extends Observable {
    // Create static instance of this model
    private static final HomeModel instances = new HomeModel();
    public static HomeModel getInstance(){return instances;}

    // Private variables for home summary
    private int currCount; // counts how many entries are there
    private ArrayList<Entry> valuesFlag;

    private boolean finishedRecording;
    File flagFile;

    // Constructor
    HomeModel(){
        valuesFlag = new ArrayList<>();
        currCount = 0;
        finishedRecording = false;

        File dir = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOCUMENTS), "Pocket_Security_MetaData");
        if (!dir.exists()){
            dir.mkdirs();
        }
        flagFile = new File(dir, "Flags.txt");

        try {
            FileInputStream fis = new FileInputStream(flagFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;
            while ((strLine = br.readLine()) != null) {
                String[] stringToken = strLine.split(":");
                int year = Integer.parseInt(stringToken[0]);
                int month = Integer.parseInt(stringToken[1]);
                int day = Integer.parseInt(stringToken[2]);
                int flag = Integer.parseInt(stringToken[3]);
                addEntry(year, month, day, flag, false);
            }
            br.close();
            in.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // notify observers
    private void notifyObs(){
        setChanged();
        notifyObservers();
    }

    // Get functions
    public ArrayList<Entry> getFlag(){ return valuesFlag; }
    public boolean getFinishedRecording() { return finishedRecording; }


    // Set functions
    public void setFinishedRecording(boolean set){
        this.finishedRecording = set;
    }

    public void addEntry(int year, int month, int day, int flag, boolean writeFile){
        if(writeFile) {
            try {
                FileWriter writer = new FileWriter(flagFile, true);
                writer.write(year + ":" + month + ":" + day + ":" + flag + "\n");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        peopleDate pd = new peopleDate(year, month, day);
        valuesFlag.add(new Entry(currCount, flag, pd));
        currCount++;
        notifyObs();
    }

}
