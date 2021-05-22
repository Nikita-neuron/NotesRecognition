package com.example.tabnote;

import com.example.tabnote.ServerCommunication.Tab;

import java.util.Comparator;

public class TabComparator {

    public TabNameComparator sortByName() {
        return new TabNameComparator();
    }

    public TabUserNameComparator sortByUserName() {
        return new TabUserNameComparator();
    }

    static class TabNameComparator implements Comparator<Tab> {
        @Override
        public int compare(Tab o1, Tab o2) {
            return o1.getTitle().compareTo(o2.getTitle());
        }
    }

    static class TabUserNameComparator implements Comparator<Tab> {
        @Override
        public int compare(Tab o1, Tab o2) {
            return o1.getUsername().compareTo(o2.getUsername());
        }
    }
}
