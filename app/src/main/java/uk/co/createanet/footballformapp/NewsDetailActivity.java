package uk.co.createanet.footballformapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import uk.co.createanet.footballformapp.lib.GetRSSFeed;

public class NewsDetailActivity extends AdvertActivity {

    private static final String KEY_MODEL = "model";
    private GetRSSFeed.Entry newsItem;

    private TextView text_title;
    private ImageView main_image;
    private TextView text_date;
    private Button btn_share;
    private Button btn_website;
    private TextView text_description;

    public static Intent newInstance(Context c, GetRSSFeed.Entry item) {
        Intent i = new Intent(c, NewsDetailActivity.class);
        i.putExtra(KEY_MODEL, item);

        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        if (getIntent().getExtras() != null) {
            newsItem = (GetRSSFeed.Entry) getIntent().getExtras().getSerializable(KEY_MODEL);
        }

        text_title = (TextView) findViewById(R.id.text_title);
        main_image = (ImageView) findViewById(R.id.main_image);
        text_date = (TextView) findViewById(R.id.text_date);
        btn_share = (Button) findViewById(R.id.btn_share);
        btn_website = (Button) findViewById(R.id.btn_website);
        text_description = (TextView) findViewById(R.id.text_description);

        text_title.setText(newsItem.title);
        ImageLoader.getInstance().displayImage(newsItem.imageUrl, main_image);
        text_date.setText(newsItem.getPubDate());
        text_description.setText(newsItem.getSummary());

        if (newsItem.link == null) {
            btn_website.setVisibility(View.GONE);
            btn_share.setVisibility(View.GONE);
        }

        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, newsItem.getLink() + "\nSent via the Football Form Android app");
//                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
                startActivity(Intent.createChooser(intent, "Share with"));
            }
        });

        btn_website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String link = newsItem.getLink();

                if (link != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.getLink()));
                    startActivity(Intent.createChooser(intent, "Choose browser"));
                }
            }
        });

    }

}
