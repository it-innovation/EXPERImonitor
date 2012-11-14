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
//	Created Date :			2011-07-28
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator.dao.data.twitter;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;
import java.sql.Timestamp;

/**
 *
 * @author Maxim Bashevoy
 */
public class User extends Dao {
    public static final String TABLE_NAME = "Users";
    
    public User() {
        this("", false, false, false, "",
                "", "", "", new Timestamp(System.currentTimeMillis()),
                "", "", false, 0, false, "", "", false, false,
                "", "", 0, 0, "", 0, 0, "", "", false, "", false, "", "", "", false, false,
                "", "", "", new Timestamp(System.currentTimeMillis()), 0);
    }

    public User(String id, 
            boolean show_all_inline_media,
            boolean verified,
            boolean geo_enabled,
            String profile_link_color,
            String follow_request_sent,
            String lang,
            String notifications,
            Timestamp created_at,
            String profile_sidebar_border_color,
            String time_zone,
            boolean contributors_enabled,
            int statuses_count,
            boolean profile_use_background_image,
            String profile_image_url,
            String description,
            boolean is_translator,
            boolean following,
            String profile_background_image_url_https,
            String profile_background_color,
            int followers_count,
            int listed_count,
            String profile_background_image_url,
            int favourites_count,
            int friends_count,
            String screen_name,
            String url,
            boolean default_profile,
            String profile_text_color,
            boolean isProtected,
            String profile_image_url_https,
            String profile_sidebar_fill_color,
            String name,
            boolean default_profile_image,
            boolean profile_background_tile,
            String location,
            String utc_offset,
            String comment,
            Timestamp collected_at,
            int outputOfTaskID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("id", "character varying(60) NOT NULL", id));
        properties.add(new Triplet("Show_all_inline_media", "boolean", show_all_inline_media));
        properties.add(new Triplet("Verified", "boolean", verified));
        properties.add(new Triplet("Geo_enabled", "boolean", geo_enabled));
        properties.add(new Triplet("Profile_link_color", "character varying(8)", profile_link_color));
        properties.add(new Triplet("Follow_request_sent", "character varying(256)", follow_request_sent));
        properties.add(new Triplet("Lang", "character varying(4)", lang));
        properties.add(new Triplet("Notifications", "character varying(8)", notifications));
        properties.add(new Triplet("Created_at", "timestamp with time zone", created_at));
        properties.add(new Triplet("Profile_sidebar_border_color", "character varying(8)", profile_sidebar_border_color));
        properties.add(new Triplet("Time_zone", "character varying(60)", time_zone));
        properties.add(new Triplet("Contributors_enabled", "boolean", contributors_enabled));
        properties.add(new Triplet("Statuses_count", "integer", statuses_count));
        properties.add(new Triplet("Profile_use_background_image", "boolean", profile_use_background_image));
        properties.add(new Triplet("Profile_image_url", "text", profile_image_url));
        properties.add(new Triplet("Description", "text", description));
        properties.add(new Triplet("Is_translator", "boolean", is_translator));
        properties.add(new Triplet("Following", "boolean", following));
        properties.add(new Triplet("Profile_background_image_url_https", "text", profile_background_image_url_https));
        properties.add(new Triplet("Profile_background_color", "character varying(8)", profile_background_color));
        properties.add(new Triplet("Followers_count", "integer", followers_count));
        properties.add(new Triplet("Listed_count", "integer", listed_count));
        properties.add(new Triplet("Profile_background_image_url", "text", profile_background_image_url));
        properties.add(new Triplet("Favourites_count", "integer", favourites_count));
        properties.add(new Triplet("Friends_count", "integer", friends_count));
        properties.add(new Triplet("Screen_name", "character varying(256)", screen_name));
        properties.add(new Triplet("Url", "text", url));
        properties.add(new Triplet("Default_profile", "boolean", default_profile));
        properties.add(new Triplet("Profile_text_color", "character varying(8)", profile_text_color));
        properties.add(new Triplet("Protected", "boolean", isProtected));
        properties.add(new Triplet("Profile_image_url_https", "text", profile_image_url_https));
        properties.add(new Triplet("Profile_sidebar_fill_color", "character varying(8)", profile_sidebar_fill_color));
        properties.add(new Triplet("Name", "character varying(256)", name));
        properties.add(new Triplet("Default_profile_image", "boolean", default_profile_image));
        properties.add(new Triplet("Profile_background_tile", "boolean", profile_background_tile));
        properties.add(new Triplet("Location", "character varying(256)", location));
        properties.add(new Triplet("Utc_offset", "character varying(10)", utc_offset));
        properties.add(new Triplet("Collected_at", "timestamp with time zone", collected_at));
//        properties.add(new Triplet("OutputOfTaskID", "integer", outputOfTaskID));
    }

    @Override
    public Dao createNew() {
        return new User();
    }
    
    public String getID() {
        return getValueForKeyAsString("id");
    }
    
    public String getName() {
        return getValueForKeyAsString("Name");
    }
    
    public String getScreenName() {
        return getValueForKeyAsString("Screen_name");
    }
    
    public String getUrl() {
        return getValueForKeyAsString("Url");
    }
    
    public String getTimeCollected() {
        return getValueForKeyAsString("Collected_at");
    }
    
    public String getTimeCreated() {
        return getValueForKeyAsString("Created_at");
    }    
    
    public Timestamp getTimeCreatedAsTimestamp() {
        return getValueForKeyAsTimestamp("Created_at");
    }    
    
    public String getDescription() {
        return getValueForKeyAsString("Description");
    }    
    
    public String getLanguage() {
        return getValueForKeyAsString("Lang");
    }    
    
    public String getLocation() {
        return getValueForKeyAsString("Location");
    }    
    
    public String getTimezone() {
        return getValueForKeyAsString("Time_zone");
    }    
    
    public String getNumTweets() {
        return getValueForKeyAsString("Statuses_count");
    }    
    
    public String getNumFollowers() {
        return getValueForKeyAsString("Followers_count");
    }    
    
    public String getNumFriends() {
        return getValueForKeyAsString("Friends_count");
    }    
    
    public String getNumListed() {
        return getValueForKeyAsString("Listed_count");
    }    
    
    public String getNumFavorites() {
        return getValueForKeyAsString("Favourites_count");
    }    
    
    public String getProfileImageUrl() {
        return getValueForKeyAsString("Profile_image_url");
    }    
}
