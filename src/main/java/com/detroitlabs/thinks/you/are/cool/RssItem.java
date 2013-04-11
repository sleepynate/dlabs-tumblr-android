package com.detroitlabs.thinks.you.are.cool;

public class RssItem {
    public RssItem(String headline, String link) {
        this.headline = headline;
        this.link = link;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    String headline;
    String link;


}
