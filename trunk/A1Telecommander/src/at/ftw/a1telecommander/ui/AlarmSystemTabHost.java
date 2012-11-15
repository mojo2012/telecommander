package at.ftw.a1telecommander.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import at.ftw.a1telecommander.R;

public class AlarmSystemTabHost extends TabActivity {
	final static String TAG = "A1Telecommander/AlarmSystemTabHost";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_system_tab_host);

        /** TabHost will have Tabs */
        TabHost tabHost = (TabHost)findViewById(android.R.id.tabhost);

        /** TabSpec used to create a new tab.
         * By using TabSpec only we can able to setContent to the tab.
         * By using TabSpec setIndicator() we can set name to tab. */

        /** tid1 is firstTabSpec Id. Its used to access outside. */
        TabSpec firstTabSpec = tabHost.newTabSpec("alarm_system");
        TabSpec secondTabSpec = tabHost.newTabSpec("alarm_system_settings");

        Resources res = getResources();
        
        /** TabSpec setIndicator() is used to set name for the tab. */
        /** TabSpec setContent() is used to set content for a particular tab. */
        firstTabSpec.setIndicator("Alarmsystem", res.getDrawable(R.drawable.alarm)).setContent(new Intent(this,AlarmSystem.class));
        secondTabSpec.setIndicator("Einstellungen", res.getDrawable(R.drawable.misc_settings)).setContent(new Intent(this,AlarmSystemSettings.class));

        /** Add tabSpec to the TabHost to display. */
        tabHost.addTab(firstTabSpec);
        tabHost.addTab(secondTabSpec);

    }
}