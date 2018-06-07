package com.LovellStudio.Flexogram;

class ToMeRequest {

    private String name;

    private String user;

    private String userVk;

    private String userNick;

    private String userDesc;

    private Boolean accept;

    public String getName() {
        return name;
    }

    public String getUserVk() {
        return userVk;
    }

    void setUserVk(String userVk) {
        this.userVk = userVk;
    }

    public void setName(String name) {
        this.name = name;
    }

    String getUser() {
        return user;
    }

    void setUser(String user) {
        this.user = user;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getUserDesc() {
        return userDesc;
    }

    void setUserDesc(String userDesc) {
        this.userDesc = userDesc;
    }

    Boolean getAccept() {
        return accept;
    }

    void setAccept(Boolean accept) {
        this.accept = accept;
    }
}
