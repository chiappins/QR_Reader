package com.chiappins.lettoreqr;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcel;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.chiappins.lettoreqr.Globali.codice;
import static com.chiappins.lettoreqr.Globali.prima;
import static com.chiappins.lettoreqr.Globali.qr;

//lettoreqr_chiave
//iPh2each

public class MainActivity extends AppCompatActivity {


    private static final int RC_BARCODE_CAPTURE = 9001;
    TextView stampa;
    Context context;
    boolean flash = false;
    boolean focus = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = this;

        stampa = (TextView) findViewById(R.id.stampa);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                prima = true;
                Intent intent = new Intent(context, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, focus);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, flash);
                Globali.codice = null;
                Globali.qr = null;
                startActivity(intent);
            }
        });

        findViewById(R.id.codice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prima = true;
                elabora();
            }
        });

    }


    private void elabora() {
        if (Globali.codice != null) {
            stampa.setText(R.string.barcode_success);
            stampa.setText("Codice scansionato: ");
            ((TextView) findViewById(R.id.codice)).setText("" + Globali.codice);
            if (prima) {
                prima = false;
                if (qr.contactInfo != null) {
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_INSERT);
                    i.putExtra(ContactsContract.Intents.Insert.NAME, qr.displayValue);
                    if (qr.contactInfo.addresses.length > 0) {
                        String s = "";
                        String p = "";
                        for (String temp : qr.contactInfo.addresses[0].addressLines){
                            s = s + p;
                            p = ", ";
                            s = s + temp;
                        }
                        i.putExtra(ContactsContract.Intents.Insert.POSTAL, s);
                        if (qr.contactInfo.addresses[0].type == 0)
                            i.putExtra(ContactsContract.Intents.Insert.POSTAL_TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME);
                        else
                            i.putExtra(ContactsContract.Intents.Insert.POSTAL_TYPE, qr.contactInfo.addresses[0].type);
                    }
                    if (qr.contactInfo.phones.length > 0) {
                        i.putExtra(ContactsContract.Intents.Insert.PHONE, qr.contactInfo.phones[0].number);
                        if (qr.contactInfo.phones[0].type == 0)
                            i.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, 1);
                        else
                            i.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, qr.contactInfo.phones[0].type);
                    }
                    if (qr.contactInfo.phones.length > 1) {
                        i.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, qr.contactInfo.phones[1].number);
                        i.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE_TYPE, qr.contactInfo.phones[1].type);
                    }
                    if (qr.contactInfo.phones.length > 2) {
                        i.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, qr.contactInfo.phones[2].number);
                        i.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE_TYPE, qr.contactInfo.phones[2].type);
                    }
                    if (qr.contactInfo.emails.length > 0) {
                        i.putExtra(ContactsContract.Intents.Insert.EMAIL, qr.contactInfo.emails[0].address);
                        if (qr.contactInfo.emails[0].type == 0)
                            i.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, 1);
                        else
                            i.putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, qr.contactInfo.emails[0].type);
                    }
                    if (qr.contactInfo.emails.length > 1) {
                        i.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, qr.contactInfo.emails[1].address);
                        i.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL_TYPE, qr.contactInfo.emails[1].type);
                    }
                    if (qr.contactInfo.emails.length > 2) {
                        i.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, qr.contactInfo.emails[2].address);
                        i.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL_TYPE, qr.contactInfo.emails[2].type);
                    }

                    i.putExtra(ContactsContract.Intents.Insert.COMPANY, qr.contactInfo.organization);
                    i.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, qr.contactInfo.title);
                    i.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    startActivity(i);
                }

                if (qr.calendarEvent != null) {
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_EDIT);
                    Date fulldate = null;
                    String inizio = qr.calendarEvent.start.year + "-"+qr.calendarEvent.start.month+"-"+qr.calendarEvent.start.day+"-"+qr.calendarEvent.start.hours+":"+qr.calendarEvent.start.minutes;
                    try {
                        fulldate = new SimpleDateFormat("yyyy-MM-dd-HH:mm").parse(inizio);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    i.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, fulldate.getTime());
                    inizio = qr.calendarEvent.end.year + "-"+qr.calendarEvent.end.month+"-"+qr.calendarEvent.end.day+"-"+qr.calendarEvent.end.hours+":"+qr.calendarEvent.end.minutes;
                    try {
                        fulldate = new SimpleDateFormat("yyyy-MM-dd-HH:mm").parse(inizio);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    i.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fulldate.getTime());
                    i.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);
                    i.putExtra(CalendarContract.Events.TITLE, qr.displayValue);
                    i.putExtra(CalendarContract.Events.DESCRIPTION, qr.calendarEvent.description);
                    i.putExtra(CalendarContract.Events.EVENT_LOCATION, qr.calendarEvent.location);
                    i.putExtra(CalendarContract.Events.ORGANIZER, qr.calendarEvent.organizer);
                    i.setType("vnd.android.cursor.item/event");
                    startActivity(i);
                }

                if (qr.email != null) {
                    Intent i = new Intent(Intent.ACTION_SENDTO);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, qr.email.address);
                    i.putExtra(Intent.EXTRA_SUBJECT, qr.email.subject);
                    i.putExtra(Intent.EXTRA_TEXT, qr.email.body);
                    startActivity(Intent.createChooser(i, "Send Email"));
                }

                if (qr.phone != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + qr.phone.number));
                    startActivity(intent);
                }

                if (qr.url != null) {
                    if (URLUtil.isValidUrl(Globali.codice)) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(Globali.codice));
                        startActivity(i);
                    }
                }

                if (qr.sms != null) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("sms:"+ qr.sms.phoneNumber));
                    i.putExtra("sms_body", qr.sms.message);
                    startActivity(i);
                }

                if (qr.wifi != null) {
                    WifiConfiguration conf = new WifiConfiguration();
                    conf.SSID = "\"" + qr.wifi.ssid + "\"";
                    if (qr.wifi.encryptionType == qr.wifi.WEP) {
                        conf.wepKeys[0] = "\"" + qr.wifi.password + "\"";
                        conf.wepTxKeyIndex = 0;
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    }
                    else if (qr.wifi.encryptionType == qr.wifi.WPA) {
                        conf.preSharedKey = "\""+ qr.wifi.password +"\"";
                    }
                    else if (qr.wifi.encryptionType == qr.wifi.OPEN) {
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    }
                    WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                    wifiManager.addNetwork(conf);
                    Toast.makeText(context, R.string.Wifi,
                            Toast.LENGTH_LONG).show();
                }

                if (qr.geoPoint != null) {
                    String[] parts = qr.displayValue.split("\\Q?q=\\E");
                    String nome = "";


                    if (parts.length > 1)
                        nome = parts[1];
                    Uri gmmIntentUri = Uri.parse("geo:"+qr.geoPoint.lat+"+,"+qr.geoPoint.lng+"?q="+qr.geoPoint.lat+","+qr.geoPoint.lng+"("+nome+")");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }


            }
        } else {

            stampa.setText(R.string.barcode_failure);
            ((TextView) findViewById(R.id.codice)).setText("");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        elabora();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        SharedPreferences prefs = this.getSharedPreferences("dati", Context.MODE_PRIVATE);
        flash = prefs.getBoolean("flash", false);
        menu.findItem(R.id.flash).setChecked(flash);
        focus = prefs.getBoolean("focus", true);
        menu.findItem(R.id.focus).setChecked(focus);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        SharedPreferences prefs = context.getSharedPreferences("dati", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        //noinspection SimplifiableIfStatement
        if (id == R.id.flash) {
            flash = !flash;
            item.setChecked(flash);
            editor.putBoolean("flash", flash);
            editor.commit();
            return true;
        }
        if (id == R.id.focus) {
            focus = !focus;
            item.setChecked(focus);
            editor.putBoolean("focus", focus);
            editor.commit();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
}
