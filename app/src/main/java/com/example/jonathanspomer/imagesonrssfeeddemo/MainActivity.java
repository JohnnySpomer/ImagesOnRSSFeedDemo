package com.example.jonathanspomer.imagesonrssfeeddemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MainActivity extends AppCompatActivity {

    private URL url = null;
    private LigaFeedHandler ligaFeedHandler;
    private ArrayList<Feed> feedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String intentURL = "https://www.bundesliga.com/rss/en/rss_news.xml";

        try {
            url = new URL(intentURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        RSSTask rssTask = new RSSTask();
        rssTask.execute();

        Toast.makeText(MainActivity.this, "Loading Threads", Toast.LENGTH_SHORT).show();
    }

    class RSSTask extends AsyncTask<Void, Void, Void> {

        private SAXParser saxParser;

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                saxParser = SAXParserFactory.newInstance().newSAXParser();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

            HttpURLConnection httpURLConnection = null;
            InputStream inputStream = null;
            try {
                httpURLConnection = (HttpURLConnection)url.openConnection();
                inputStream = httpURLConnection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ligaFeedHandler = new LigaFeedHandler();

            try {
                saxParser.parse(inputStream, ligaFeedHandler);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
                System.exit(1);
            }

            return null;
        }

        //has UI thread access
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            makeFeedItems();
            FeedAdapter feedAdapter;
            ListView listView;

            feedAdapter = new FeedAdapter(MainActivity.this, R.layout.list_item, feedItems);

            listView = findViewById(R.id.nice_listview);
            listView.setAdapter(feedAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(MainActivity.this, "onClick", Toast.LENGTH_LONG).show();
                }
            });

            Log.d("Jody", "post execute!");
        }
    }

    class LigaFeedHandler extends DefaultHandler {
        private ArrayList<String> title;
        private ArrayList<String> description;
        private ArrayList<String> pubDate;
        private ArrayList<String> link;
        private ArrayList<String> guid;
        private ArrayList<String> mediaContent;
        private ArrayList<String> mediaThumbnail;
        private boolean inItem, inTitle, inDescription, inPubDate, inLink, inGuid;
        private String stringTitle, stringDescription, stringPubDate, stringLink, stringGuid;

        public ArrayList<String> getTitles() { return title; }
        public ArrayList<String> getDescriptions() { return description; }
        public ArrayList<String> getPubDates() { return pubDate; }
        public ArrayList<String> getLinks() { return link; }
        public ArrayList<String> getGuids() { return guid; }
        public ArrayList<String> getMediaContents() { return mediaContent; }
        public ArrayList<String> getMediaThumbnails() { return mediaThumbnail; }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            Log.d("Jody", "startDocument");

            title = new ArrayList<String>(10);
            description = new ArrayList<String>(10);
            pubDate = new ArrayList<String>(10);
            link = new ArrayList<String>(10);
            guid = new ArrayList<String>(10);
            mediaContent = new ArrayList<String>(10);
            mediaThumbnail = new ArrayList<String>(10);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            Log.d("Jody", "startElement: " + qName);
            if (qName.equals("item")) {
                inItem = true;
            } else if(inItem && qName.equals("title")) {
                inTitle = true;
                stringTitle = new String();
            }else if(inItem && qName.equals("description")) {
                inDescription = true;
                stringDescription = new String();
            }else if(inItem && qName.equals("pubDate")) {
                inPubDate = true;
                stringPubDate = new String();
            }else if(inItem && qName.equals("link")) {
                inLink = true;
                stringLink = new String();
            }else if(inItem && qName.equals("guid")) {
                inGuid = true;
                stringGuid = new String();
            }else if(inItem && qName.equals("media:content")) {
                mediaContent.add(attributes.getValue("url"));
            }else if(inItem && qName.equals("media:thumbnail")) {
                mediaThumbnail.add(attributes.getValue("url"));
            }

        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            Log.d("Jody", "endElement: " + qName);
            if (qName.equals("item")) {
                inItem = false;
            } else if(inItem && qName.equals("title")) {
                inTitle = false;
                title.add(stringTitle);
            } else if(inItem && qName.equals("description")) {
                inDescription = false;
                description.add(stringDescription);
            } else if(inItem && qName.equals("pubDate")) {
                inPubDate = false;
                pubDate.add(stringPubDate);
            } else if(inItem && qName.equals("link")) {
                inLink = false;
                link.add(stringLink);
            } else if(inItem && qName.equals("guid")) {
                inGuid = false;
                guid.add(stringGuid);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            if(inTitle) {
                String s = new String(ch, start, length);
                stringTitle += s;
                Log.d("Jody", "chars: " + stringTitle);
            } else if (inDescription) {
                String s = new String(ch, start, length);
                stringDescription += s;
                Log.d("Jody", "chars: " + stringDescription);
            }  else if (inPubDate) {
                String s = new String(ch, start, length);
                stringPubDate += s;
                Log.d("Jody", "chars: " + stringPubDate);
            }  else if (inLink) {
                String s = new String(ch, start, length);
                stringLink += s;
                Log.d("Jody", "chars: " + stringLink);
            }  else if (inGuid) {
                String s = new String(ch, start, length);
                stringGuid += s;
                Log.d("Jody", "chars: " + stringGuid);
            }
        }
    }

    //custom ArrayAdapter for our ListView
    private class FeedAdapter extends ArrayAdapter<Feed> {

        private ArrayList<Feed> items;

        public FeedAdapter(Context context, int textViewResourceId, ArrayList<Feed> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        //This method is called once for every item in the ArrayList as the list is loaded.
        //It returns a View -- a list item in the ListView -- for each item in the ArrayList
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item, null);
            }

            Feed currentItem = items.get(position);
            if (currentItem != null) {
                TextView tt = (TextView) v.findViewById(R.id.toptext);
                TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                ImageView imageView = v.findViewById(R.id.imgListItem);
                String image = currentItem.getMediaContent();
                imageView.setTag(image);
                if (tt != null) {
                    tt.setText("Title: " + currentItem.getTitle());
                }
                if (bt != null) {
                    bt.setText("Publication Date: " + currentItem.getPubDate());
                }
                if (imageView != null && !image.equals("")) {
                    new LoadImage(image, imageView).execute();
                }
            }
            return v;
        }
    }

    //class to define properties of each item in the list
    class Feed {

        private String title;
        private String description;
        private String pubDate;
        private String link;
        private String guid;
        private String mediaContent;
        private String mediaThumbnail;

        public Feed(String title, String description, String pubDate, String link, String guid, String mediaContent, String mediaThumbnail) {
            this.title = title;
            this.description = description;
            this.pubDate = pubDate;
            this.link = link;
            this.guid = guid;
            this.mediaContent = mediaContent;
            this.mediaThumbnail =mediaThumbnail;
        }

        public String getTitle() {
            return title;
        }
        public String getDescription() {
            return description;
        }
        public String getPubDate() {
            return pubDate;
        }
        public String getLink() { return link; }
        public String getGuid() {
            return guid;
        }
        public String getMediaContent() {
            return mediaContent;
        }
        public String getMediaThumbnail() {
            return mediaThumbnail;
        }
    }

    private  void makeFeedItems() {

        ArrayList<String> titles = ligaFeedHandler.getTitles();
        feedItems = new ArrayList<>(10);

        for (int x = 0; x < titles.size(); x++) {
            feedItems.add(new Feed(
                    ligaFeedHandler.getTitles().get(x),
                    ligaFeedHandler.getDescriptions().get(x),
                    ligaFeedHandler.getPubDates().get(x),
                    ligaFeedHandler.getLinks().get(x),
                    ligaFeedHandler.getGuids().get(x),
                    ligaFeedHandler.getMediaContents().get(x),
                    ligaFeedHandler.getMediaThumbnails().get(x)));
        }

    }


    /*
        This is the async task that loads the image for every item in the list view.
        It is called in the FeedAdapter in the getView method.
     */
    private class LoadImage extends AsyncTask<Void, Void, Void> {

        String imageToLoad;
        String tag;
        ImageView viewToPopulate;
        private Bitmap bitmap;

        // This is the LoadImage constructor, it takes the images string,
        // and the imageView to populate as arguments.
        public LoadImage(String image, ImageView imageView){
            this.imageToLoad = image;
            this.viewToPopulate = imageView;
            this.tag = imageView.getTag().toString();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(imageToLoad).getContent());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!viewToPopulate.getTag().toString().equals(imageToLoad)) {
               /* The path is not same. This means that this
                  image view is handled by some other async task.
                  We don't do anything and return. */
                return;
            }

            if(bitmap != null && viewToPopulate != null){

                if(viewToPopulate.getTag().toString().equals(imageToLoad)){
                    viewToPopulate.setVisibility(View.VISIBLE);
                    viewToPopulate.setImageBitmap(bitmap);
                }else{
                    viewToPopulate.setVisibility(View.GONE);
                }

            }else{
                viewToPopulate.setVisibility(View.GONE);
            }

        }
    }
}
