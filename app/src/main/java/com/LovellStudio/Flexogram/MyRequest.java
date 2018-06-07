package com.LovellStudio.Flexogram;

class MyRequest {

    private String name;

    private String author;

    private String vk;

    private Boolean accept;

    Boolean getAccept() {
        return accept;
    }

    public String getVk() {return vk;}

    void setVk(String vk) {
        this.vk = vk;
    }

    void setAccept(Boolean accept) {
        this.accept = accept;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String getAuthor() {
        return author;
    }

    void setAuthor(String author) {
        this.author = author;
    }
}
