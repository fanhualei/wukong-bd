package com.wukong.mapreduce.version;



public class VersionInfo {
    private String date;
    private String user;
    private String game;
    private int hour;
    private String from;
    private String version;
    private String location;

    @Override
    public String toString() {
        return date+"\t"+user+"\t"+game+"\t"+hour+"\t"+from+"\t"+version+"\t"+location;
    }

    public VersionInfo(String date, String user, String game, int hour, String from, String version, String location) {
        this.date = date;
        this.user = user;
        this.game = game;
        this.hour = hour;
        this.from = from;
        this.version = version;
        this.location = location;
    }

    public VersionInfo() {

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }




}
