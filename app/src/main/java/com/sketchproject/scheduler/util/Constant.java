package com.sketchproject.scheduler.util;

/**
 * Scheduler Android App
 * Created by Angga on 10/7/2015.
 */
public class Constant {

    public static final String STATUS_SUCCESS 		    = "success";
    public static final String STATUS_FAILED 		    = "failed";
    public static final String STATUS_GRANTED 		    = "granted";
    public static final String STATUS_RESTRICT 		    = "restrict";
    public static final String STATUS_MISMATCH 		    = "mismatch";

    public static final String SERVER 				    = "http://scheduler.angga-ari.com/"; //"http://10.0.3.2/scheduler_api/";
    public static final String URL_LOGIN 			    = SERVER+"user/login";
    public static final String URL_LOGOUT 			    = SERVER+"user/logout";
    public static final String URL_REGISTER 		    = SERVER+"user/register";
    public static final String URL_ACCOUNT 			    = SERVER+"user/edit";
    public static final String URL_ACCOUNT_UPDATE	    = SERVER+"user/update";
    public static final String URL_DASHBOARD		    = SERVER+"schedule/summary";

    public static final String URL_NOTE_VIEW 		    = SERVER+"note";
    public static final String URL_NOTE_INSERT 		    = SERVER+"note/insert";
    public static final String URL_NOTE_EDIT 		    = SERVER+"note/edit";
    public static final String URL_NOTE_UPDATE 		    = SERVER+"note/update";
    public static final String URL_NOTE_DELETE 		    = SERVER+"note/delete";

    public static final String URL_SCHEDULE_VIEW 		= SERVER+"schedule";
    public static final String URL_SCHEDULE_INCOMING	= SERVER+"schedule/incoming";
    public static final String URL_SCHEDULE_TODAY       = SERVER+"schedule/today";
    public static final String URL_SCHEDULE_TOMORROW	= SERVER+"schedule/tomorrow";
    public static final String URL_SCHEDULE_INSERT 		= SERVER+"schedule/insert";
    public static final String URL_SCHEDULE_EDIT 		= SERVER+"schedule/edit";
    public static final String URL_SCHEDULE_UPDATE 		= SERVER+"schedule/update";
    public static final String URL_SCHEDULE_DELETE 		= SERVER+"schedule/delete";
}
