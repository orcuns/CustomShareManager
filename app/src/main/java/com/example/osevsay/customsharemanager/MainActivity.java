package com.example.osevsay.customsharemanager;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    Uri uri = Uri.parse("android.resource://com.example.osevsay.customsharemanager/"+ R.mipmap.ic_launcher);

    Uri pictureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath() + "/Pictures/Screenshots/cats.png"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent shareIntent = new Intent();
//                shareIntent.setAction(Intent.ACTION_SEND);
//                shareIntent.putExtra(Intent.EXTRA_TEXT, "HOLY COW");
//                shareIntent.setType("*/*");
//                startActivity(Intent.createChooser(shareIntent, "HOLY COW"));

//                onShareClick();
                onShareClick2();
            }
        });

    }

    public void onShareClick() {
        Resources resources = getResources();

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);
        // Native email client doesn't currently support HTML, but it doesn't hurt to try in case they fix it
        emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(resources.getString(R.string.share_email_native)));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.share_email_subject));
        emailIntent.setType("message/rfc822");

        PackageManager pm = getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");


        Intent openInChooser = Intent.createChooser(emailIntent, resources.getString(R.string.share_chooser_text));

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
        for (int i = 0; i < resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            if(packageName.contains("android.email")) {
                emailIntent.setPackage(packageName);
            }
            else if(packageName.contains("twitter") || packageName.contains("facebook") || packageName.contains("mms") || packageName.contains("whatsapp")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                if(packageName.contains("twitter")) {
                    intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.share_twitter));
                }
                else if(packageName.contains("facebook")) {
                    // Warning: Facebook IGNORES our text. They say "These fields are intended for users to express themselves. Pre-filling these fields erodes the authenticity of the user voice."
                    // One workaround is to use the Facebook SDK to post, but that doesn't allow the user to choose how they want to share. We can also make a custom landing page, and the link
                    // will show the <meta content ="..."> text from that page with our link in Facebook.

//                    intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.share_facebook));
                    intent.putExtra(Intent.EXTRA_STREAM, pictureUri);
                    intent.setType("image/*");

                }
                else if(packageName.contains("mms")) {
                    intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.share_sms));
                }
                else if(packageName.contains("whatsapp")) {
                    intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.whatsupp));
                }
                else if(packageName.contains("android.gm")) {
                    intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(resources.getString(R.string.share_email_gmail)));
                    intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.share_email_subject));
                    intent.setType("message/rfc822");
                }

                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        // convert intentList to array
        LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);

        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        startActivity(openInChooser);
    }


    public void onShareClick2(){
        List<Intent> targetShareIntents = new ArrayList<Intent>();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        List<ResolveInfo> resInfos = getPackageManager().queryIntentActivities(shareIntent, 0);
        if(!resInfos.isEmpty()){
            System.out.println("Have package");
            for(ResolveInfo resInfo : resInfos){
                String packageName = resInfo.activityInfo.packageName;
                Log.i("Package Name", packageName);

                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, resInfo.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);

                if(packageName.contains("com.twitter.android") || packageName.contains("mms")|| packageName.contains("gm")){

                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "Text");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                    intent.setPackage(packageName);
                    targetShareIntents.add(intent);
                }
                else if (packageName.contains("facebook")) {

                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, pictureUri);
                    intent.setPackage(packageName);
                    targetShareIntents.add(intent);
                }

            }
            if(!targetShareIntents.isEmpty()){
                System.out.println("Have Intent");
                Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Choose app to share");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                startActivity(chooserIntent);
            }else{
                System.out.println("Do not Have Intent");
            }
        }
    }
}
