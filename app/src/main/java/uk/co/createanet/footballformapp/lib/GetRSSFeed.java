package uk.co.createanet.footballformapp.lib;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by matt on 09/07/2014.
 */
public abstract class GetRSSFeed extends Fragment {

    public class DownloadXmlTask extends AsyncTask<String, Void, ArrayList<Entry>> {
        @Override
        protected ArrayList<Entry> doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                Log.d("FF", "Connect error");
//                return "Unable to connect";
            } catch (XmlPullParserException e) {
                Log.d("FF", "Parse error");
//                return "Unable to parse";
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Entry> items){
            gotNewsItems(items);
        }

    }

    public abstract void gotNewsItems(ArrayList<Entry> items);

    private ArrayList<Entry> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {

        InputStream stream = null;
        // Instantiate the parser

        NewsXmlParser stackOverflowXmlParser = new NewsXmlParser();
        ArrayList<Entry> entries = null;

        try {
            stream = downloadUrl(urlString);
            entries = stackOverflowXmlParser.parse(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return entries;
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    public String ns = null;
    public class NewsXmlParser {

        public ArrayList parse(InputStream in) throws XmlPullParserException, IOException {
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                parser.nextTag();
                return readFeed(parser);
            } finally {
                in.close();
            }
        }
    }

    private ArrayList readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {

        parser.require(XmlPullParser.START_TAG, ns, "rss");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("channel")) {
                return readEntries(parser);
            } else {
                skip(parser);
            }
        }

        return new ArrayList<Entry>();
    }

    public static class Entry implements Serializable {
        public static final Format formatterIn = new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm:ss +0000");
        public static final Format formatterOut = new SimpleDateFormat("EEE, dd MMMM yyyy HH:mm:ss");

        public final String title;
        public final String link;
        public final String summary;
        public final String imageUrl;
        public final String pubDate;
        public String websiteUrl;

        private Entry(String title, String summary, String link, String imageUrl, String pubDate) {
            this.title = title;
            this.summary = summary;
            this.link = link;
            this.imageUrl = imageUrl;
            this.pubDate = pubDate;
        }

        public String getPubDate(){
            return getDate(pubDate);
        }

        public String getSummary(){
            return summary.replaceAll("\\<.*?\\>", "");
        }

        public static String getDate(String dateIn){
            try {
                Date date = (Date)((DateFormat) formatterIn).parse(dateIn);
                return formatterOut.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return null;
        }

        public String getLink(){
            String returnLink = null;

            if(link != null) {
                if (!link.startsWith("http://") && !link.startsWith("https://")) {
                    returnLink = "http://" + link;
                } else {
                    returnLink = link;
                }
            }

            return returnLink;
        }
    }

    private ArrayList<Entry> readEntries(XmlPullParser parser) throws IOException, XmlPullParserException {
        ArrayList<Entry> entries = new ArrayList<Entry>();

        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("item")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }

        return entries;
    }


        // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
//        parser.require(XmlPullParser.START_TAG, ns, "item");
        String title = null;
        String summary = null;
        String link = null;
        String imageUrl = null;
        String pubDate = null;
        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("title")) {
                title = readValue(parser, "title");
            } else if (name.equals("description")) {
                summary = readValue(parser, "description");
            } else if (name.equals("link")) {
                link = readValue(parser, "link");
            } else if (name.equals("enclosure")) {
                imageUrl = readAttribute(parser, "enclosure", "url");
            } else if(name.equals("pubDate")){
                pubDate = readValue(parser, "pubDate");
            } else {
                skip(parser);
            }
        }
        return new Entry(title, summary, link, imageUrl, pubDate);
    }

    // Processes title tags in the feed.
    private String readValue(XmlPullParser parser, String key) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, key);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, key);
        return title;
    }

    private String readAttribute(XmlPullParser parser, String key, String attribute) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, key);

        String url = null;
        for(int i = 0, j = parser.getAttributeCount(); i < j; i++){
            String attr = parser.getAttributeName(i);

            if(attr.equals(attribute)){
                url = parser.getAttributeValue(i);
                break;
            }
        }

        skip(parser);

//        parser.require(XmlPullParser.END_TAG, ns, key);
        return url;
    }


    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
