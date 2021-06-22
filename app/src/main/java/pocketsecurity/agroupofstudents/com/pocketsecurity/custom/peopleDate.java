package pocketsecurity.agroupofstudents.com.pocketsecurity.custom;

/**
 * Created by charlieyu on 2019-07-10.
 */

public class peopleDate {
    // number of people detected in one day

    // the date
    int year;
    int month;
    int day;

    public peopleDate(int year, int month, int day){
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public String getDate(){
        String date = Integer.toString(month) + "/" + Integer.toString(day) + "/" + Integer.toString(year);
        return date;
    }

}
