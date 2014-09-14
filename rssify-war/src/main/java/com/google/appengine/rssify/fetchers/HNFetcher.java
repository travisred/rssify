package com.google.appengine.rssify.fetchers;

import com.google.appengine.rssify.model.SourceFetcher;
import com.google.appengine.rssify.model.SourceItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by gianluca on 9/13/14.
 */
public class HNFetcher implements SourceFetcher {
    private static final Logger log = Logger.getLogger(HNFetcher.class.getName());

    private boolean askHN;
    private int minComments;

    public HNFetcher(int minComments, boolean askHN) {
        this.minComments = minComments;
        this.askHN = askHN;
    }

    @Override
    public List<SourceItem> fetchItems() throws IOException {
        String url;
        if (askHN) {
            url = "http://news.ycombinator.com/ask";
        } else {
            url = "http://news.ycombinator.com";
        }

        Document doc = Jsoup.connect(url).get();
        Elements articles = doc.select(".title a:not([rel])");
        Elements comments = doc.select(".subtext a:matches(comment|discuss)");
        if (articles.size() != comments.size()) {
            log.severe("Got " + articles.size() + " articles and " + comments.size() + " comments");
            return null;
        }

        log.info("Got " + articles.size() + " items ");

        List<SourceItem> sourceItems = new ArrayList<SourceItem>();

        for (int j = 0; j < comments.size(); ++j) {
            Element comment = comments.get(j);
            if (comment.text().contains("discuss")) {
                continue;
            }

            int numComments = Integer.parseInt(comment.text().split(" ")[0]);
            if (numComments < minComments) {
                continue;
            }

            Element article = articles.get(j);

            String link;
            if (askHN) {
                link = "http://news.ycombinator.com/" + article.attr("href");
            } else {
                link = article.attr("href");
            }

            String body = "<a href=\"http://news.ycombinator.com/" + comment.attr("href") + "\">" + comment.text() + "</a>";
            SourceItem sourceItem = new SourceItem(link,
                    article.text(), body);

            sourceItems.add(sourceItem);
        }

        log.info("Returning " + sourceItems.size() + " items");
        return sourceItems;
    }
}
