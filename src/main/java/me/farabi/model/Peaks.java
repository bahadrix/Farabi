package me.farabi.model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

import java.util.ArrayList;

@Entity("Peaks")
public class Peaks {
    @Id
    private int ID = 0;
    private ArrayList<PeakHolder> list = new ArrayList<PeakHolder>();

    public int getID() {
        return ID;
    }

    public void setID(int id) {
        this.ID = id;
    }

    public ArrayList<PeakHolder> getList() {
        return list;
    }

    public void setList(ArrayList<PeakHolder> list) {
        this.list = list;
    }

}
