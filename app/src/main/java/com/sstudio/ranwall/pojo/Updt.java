package com.sstudio.ranwall.pojo;

public class Updt {

    private String update;
    private String version;
    private String url;

    /**
     * No args constructor for use in serialization
     *
     */
    public Updt() {
    }

    /**
     *
     * @param update
     * @param url
     * @param version
     */
    public Updt(String update, String version, String url) {
        super();
        this.update = update;
        this.version = version;
        this.url = url;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
