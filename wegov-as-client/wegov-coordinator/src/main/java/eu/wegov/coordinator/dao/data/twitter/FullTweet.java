/////////////////////////////////////////////////////////////////////////
//
// ¬© University of Southampton IT Innovation Centre, 2011
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//	Created By :			Maxim Bashevoy
//	Created Date :			2011-07-29
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator.dao.data.twitter;

import java.util.ArrayList;

/**
 *
 * @author Maxim Bashevoy
 */
public class FullTweet {
    private Tweet tweet;
    private User user;
    private ArrayList<UserMention> userMentions;
    private ArrayList<Url> urls;
    private ArrayList<Hashtag> hashtags;

    public FullTweet(Tweet tweet, User user, ArrayList<UserMention> userMentions, ArrayList<Url> urls, ArrayList<Hashtag> hashtags) {
        this.tweet = tweet;
        this.user = user;
        this.userMentions = userMentions;
        this.urls = urls;
        this.hashtags = hashtags;
    }

    public ArrayList<Hashtag> getHashtags() {
        return hashtags;
    }

    public void setHashtags(ArrayList<Hashtag> hashtags) {
        this.hashtags = hashtags;
    }

    public Tweet getTweet() {
        return tweet;
    }

    public void setTweet(Tweet tweet) {
        this.tweet = tweet;
    }

    public ArrayList<Url> getUrls() {
        return urls;
    }

    public void setUrls(ArrayList<Url> urls) {
        this.urls = urls;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<UserMention> getUserMentions() {
        return userMentions;
    }

    public void setUserMentions(ArrayList<UserMention> userMentions) {
        this.userMentions = userMentions;
    }
    
    @Override
    public String toString() {
        String result = tweet.toString() + "\n" + user.toString() + "\n";
        for (UserMention mention : userMentions) {
            result = result + mention.toString() + "\n";
        }
        for (Url url : urls) {
            result = result + url.toString() + "\n";
        }
        for (Hashtag hashtag : hashtags) {
            result = result + hashtag.toString() + "\n";
        }
        return result;
    }
}
