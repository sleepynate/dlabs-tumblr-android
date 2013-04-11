
package com.detroitlabs.thinks.you.are.cool;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.junit.Before;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class FeedReaderActivityTest {

    private FeedReaderActivity act;

    @Before
    public void setup() {
        Robolectric.addPendingHttpResponse(200, sampleFeedWithOneItem, null);
        act = new FeedReaderActivity();
        act.onCreate(null);
    }

    @Test
    public void hasAListViewWithId_feed_list() throws Exception {
        ListView rssItemListView = getRssItemListView();
        assertThat(rssItemListView, notNullValue());
    }

    @Test
    public void populatesTheListViewFromRssItemObjects() {
        ArrayList<RssItem> items = new ArrayList<RssItem>() {{
            add(new RssItem("Detroit Labs is awesome", "http://win.winning.io"));
            add(new RssItem("Detroit Labs is awesomer", "http://detroitlabs.tumblr.com"));
        }};

        act.setRssItems(items);

        ListAdapter adapter = getRssItemListViewAdapter();

        assertThat(adapter.getCount(), is(2));
        assertThat(adapter.getItem(0), is(RssItem.class));
    }

    @Test
    public void parsesAnItemProperly() {
        List<RssItem> items = act.fetchRssItems(sampleFeedWithOneItem);
        assertThat(items.size(), is(1));

        RssItem item = items.get(0);
        assertThat(item.getHeadline(), equalTo("Nerd Nite Detroit"));
        assertThat(item.getLink(), equalTo("http://detroitlabs.tumblr.com/post/47025040436"));
    }

    @Test
    public void fetchesRssOnStartup() {
        ListAdapter adapter = getRssItemListViewAdapter();

        assertThat(adapter.getCount(), is(1));
        assertThat(adapter.getItem(0), is(RssItem.class));
    }

    @Test
    public void tappingListItemOpensUrl() {
        ListView l = getRssItemListView();
        final View row = l.getChildAt(0);
        row.performClick();

        ShadowActivity shadowActivity = Robolectric.shadowOf(act);
        Intent started = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = Robolectric.shadowOf(started);
        assertThat(shadowIntent.getData().toString(), equalTo("http://detroitlabs.tumblr.com/post/47025040436"));

    }

    private ListView getRssItemListView() {
        return (ListView) act.findViewById(R.id.feed_list);
    }

    private ListAdapter getRssItemListViewAdapter() {
        ListView rssItemListView = getRssItemListView();
        return rssItemListView.getAdapter();
    }

    public static String sampleFeedWithOneItem =
    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
    "<rss version=\"2.0\">" +
        "<channel>" +
            "<atom:link rel=\"hub\" href=\"http://tumblr.superfeedr.com/\" />" +
            "<description>We make world-class apps.</description>" +
            "<title>Detroit Labs</title>" +
            "<generator>Tumblr (3.0; @detroitlabs)</generator>" +
            "<link>http://detroitlabs.tumblr.com/</link>" +

            "<item>" +
                "<title>Nerd Nite Detroit</title>" +
                "<description><p><img alt=\"image\" src=\"http://media.tumblr.com/c010f180a15701f1051da50c142bd119/tumblr_inline_mkopmxiL4J1qz4rgp.png\"/></p><p><a href=\"https://detroitlabs1.squarespace.com/podcast/2013/4/3/season-1-episode-9-nerd-nite-detroit\" target=\"_blank\">Season 1, Episode 9</a></p><p><a href=\"https://twitter.com/ndh313\">Nathan Hughes</a>, <a href=\"http://blog.slaunchaman.com/\">Jeff Kelley</a>, and <a href=\"https://twitter.com/malusman\">Chris Trevarthen</a> talk with <a href=\"https://twitter.com/balanon\">Henry</a> about the inaugural edition of <a href=\"http://detroit.nerdnite.com/\" target=\"_blank\">Detroit&#8217;s Nerd Nite</a>. Described by Nathan as an opportunity to &#8220;drink and listen to people talk about things they love&#8221;, most major American cities have a Nerd Nite and now it&#8217;s Detroit&#8217;s turn. What tools do you use to organize an event? What special secret announcement is made during this podcast? Do you like spiders?</p></description>" +
                "<link>http://detroitlabs.tumblr.com/post/47025040436</link>" +
                "<guid>http://detroitlabs.tumblr.com/post/47025040436</guid>" +
                "<pubDate>Wed, 03 Apr 2013 11:01:00 -0400</pubDate><dc:creator>cdpetersen</dc:creator>" +
            "</item>" +
        "</channel>" +
    "</rss>";
}


