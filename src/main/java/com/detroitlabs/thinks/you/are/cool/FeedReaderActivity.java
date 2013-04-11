package com.detroitlabs.thinks.you.are.cool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import sun.misc.IOUtils;
import sun.net.www.http.HttpClient;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FeedReaderActivity extends Activity {

    private static String TAG = "dlabs-tumblr";
    private ListView feedList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
        setContentView(R.layout.main);
        feedList = (ListView) findViewById(R.id.feed_list);
        new FeedFetcher().execute("http://detroitlabs.tumblr.com/rss");
    }

    public void setRssItems(final ArrayList<RssItem> items) {
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run() {
                feedList.setAdapter(new RssItemAdapter(FeedReaderActivity.this, android.R.layout.simple_list_item_1, items));
            }
        });
    }

    public List<RssItem> fetchRssItems(String xml) {
        ArrayList<RssItem> items = new ArrayList<RssItem>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser xpp = factory.newPullParser();

            // We will get the XML from an input stream
            xpp.setInput(new StringReader(xml));

            boolean insideItem = false;

            // Returns the type of current event: START_TAG, END_TAG, etc..
            int eventType = xpp.getEventType();
            String headline = null;
            String link = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = true;
                    } else if (xpp.getName().equalsIgnoreCase("title")) {
                        if (insideItem)
                            headline = xpp.nextText(); //extract the headline
                    } else if (xpp.getName().equalsIgnoreCase("link")) {
                        if (insideItem)
                            link = xpp.nextText(); //extract the link of article
                    }
                }else if(eventType==XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){
                    insideItem=false;
                    items.add(new RssItem(headline, link));
                }

                eventType = xpp.next(); //move to next element
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    public InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    public class RssItemAdapter extends ArrayAdapter<RssItem> {

        private final Context context;
        private final int rid;
        private final List<RssItem> items;

        public RssItemAdapter(Context context, int textViewResourceId, List<RssItem> objects) {
            super(context, textViewResourceId, objects);
            this.context = context;
            rid = textViewResourceId;
            this.items = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(rid, parent, false);
            TextView textView = (TextView) rowView.findViewById(android.R.id.text1);
            final RssItem item = getItem(position);
            textView.setText(item.getHeadline());
            rowView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink()));
                    startActivity(browserIntent);
                }
            });
            return rowView;
        }
    }

    public class FeedFetcher extends AsyncTask<String, Void, Boolean>  {

        @Override
        protected Boolean doInBackground(String... strings) {
            for(String url: strings) {
                try {
                    String result = readStringFromUrl(url);
                    final List<RssItem> rssItems = fetchRssItems(result);
                    setRssItems((ArrayList<RssItem>) rssItems);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

       private String readStringFromUrl(String url) throws IOException, URISyntaxException {
           DefaultHttpClient client = new DefaultHttpClient();
           HttpGet request = new HttpGet();
           request.setURI(new URI(url));
           HttpResponse response = client.execute(request);
           InputStream ips  = response.getEntity().getContent();
           InputStreamReader ipsr = new InputStreamReader(ips);
           BufferedReader br = new BufferedReader(ipsr);

           StringBuilder output = new StringBuilder();
           String inputLine;
           while ((inputLine = br.readLine()) != null) {
               output.append(inputLine);
           }
           return output.toString();
       }
    }
}

