package com.mualab.org.user.activity.chat.Util;

public class TimeAgo {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            if ((diff / SECOND_MILLIS)==0)
                return "a few second ago";
            else
                return diff / SECOND_MILLIS+" second ago";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    /* SimpleDateFormat sd = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss");
                                        try {
                                            String lastTime = sd.format(new Date((Long) otherUser.lastActivity));
                                            Date past = sd.parse(lastTime);
                                            Date now = new Date();
                                            long seconds=TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
                                            long minutes= TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
                                            long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
                                            long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());

                                            if(seconds<60)
                                            {
                                                onlineStatus = seconds+" seconds ago";
                                            }
                                            else if(minutes<60)
                                            {
                                                onlineStatus = minutes+" minutes ago";
                                            }
                                            else if(hours<24)
                                            {
                                                onlineStatus = hours+" hours ago";
                                            }
                                            else
                                            {
                                                onlineStatus = days+" days ago";
                                            }

                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }*/

}
