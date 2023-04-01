package com.chatsit.chat.user;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.chatsit.chat.NightMode;
import com.chatsit.chat.R;


public class TermsActivity extends AppCompatActivity {
    NightMode sharedPref;
    @SuppressLint({"SetJavaScriptEnabled", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new NightMode(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.NormalDarkTheme);
        } else setTheme(R.style.NormalDayTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        TextView textView2 = findViewById(R.id.textView2);
        textView2.setText("Terms & Conditions");

        WebView webView = findViewById(R.id.webView);
        findViewById(R.id.imageView).setOnClickListener(v -> onBackPressed());
        webView.requestFocus();
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.setSoundEffectsEnabled(true);
        webView.loadData("<!DOCTYPE html>\n" +
                        "    <html>\n" +
                        "    <head>\n" +
                        "      <meta charset='utf-8'>\n" +
                        "      <meta name='viewport' content='width=device-width'>\n" +
                        "      <title>Terms &amp; Conditions</title>\n" +
                        "      <style> body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding:1em; } </style>\n" +
                        "    </head>\n" +
                        "    <body>\n" +
                        "    <strong>Terms &amp; Conditions</strong> <p>\n" +
                        "                  By downloading or using the app, these terms will\n" +
                        "                  automatically apply to you – you should make sure therefore\n" +
                        "                  that you read them carefully before using the app. You’re not\n" +
                        "                  allowed to copy, or modify the app, any part of the app, or\n" +
                        "                  our trademarks in any way. You’re not allowed to attempt to\n" +
                        "                  extract the source code of the app, and you also shouldn’t try\n" +
                        "                  to translate the app into other languages, or make derivative\n" +
                        "                  versions. The app itself, and all the trade marks, copyright,\n" +
                        "                  database rights and other intellectual property rights related\n" +
                        "                  to it, still belong to ChatsiT.\n" +
                        "                </p> <p>\n" +
                        "                  ChatsiT is committed to ensuring that the app is\n" +
                        "                  as useful and efficient as possible. For that reason, we\n" +
                        "                  reserve the right to make changes to the app or to charge for\n" +
                        "                  its services, at any time and for any reason. We will never\n" +
                        "                  charge you for the app or its services without making it very\n" +
                        "                  clear to you exactly what you’re paying for.\n" +
                        "                </p> <p>\n" +
                        "                  The ChatsiT app stores and processes personal data that\n" +
                        "                  you have provided to us, in order to provide our\n" +
                        "                  Service. It’s your responsibility to keep your phone and\n" +
                        "                  access to the app secure. We therefore recommend that you do\n" +
                        "                  not jailbreak or root your phone, which is the process of\n" +
                        "                  removing software restrictions and limitations imposed by the\n" +
                        "                  official operating system of your device. It could make your\n" +
                        "                  phone vulnerable to malware/viruses/malicious programs,\n" +
                        "                  compromise your phone’s security features and it could mean\n" +
                        "                  that the ChatsiT app won’t work properly or at all.\n" +
                        "                </p> <div><p>\n" +
                        "                    The app does use third party services that declare their own\n" +
                        "                    Terms and Conditions.\n" +
                        "                  </p> <p>\n" +
                        "                    Link to Terms and Conditions of third party service\n" +
                        "                    providers used by the app\n" +
                        "                  </p> <ul><li><a href=\"https://policies.google.com/terms\" target=\"_blank\" rel=\"noopener noreferrer\">Google Play Services</a></li><li><a href=\"https://developers.google.com/admob/terms\" target=\"_blank\" rel=\"noopener noreferrer\">AdMob</a></li><li><a href=\"https://firebase.google.com/terms/analytics\" target=\"_blank\" rel=\"noopener noreferrer\">Google Analytics for Firebase</a></li><li><a href=\"https://firebase.google.com/terms/crashlytics\" target=\"_blank\" rel=\"noopener noreferrer\">Firebase Crashlytics</a></li><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><!----><li><a href=\"https://www.mapbox.com/legal/tos\" target=\"_blank\" rel=\"noopener noreferrer\">Mapbox</a></li><!----><!----><!----></ul></div> <p>\n" +
                        "                  You should be aware that there are certain things that\n" +
                        "                  ChatsiT will not take responsibility for. Certain\n" +
                        "                  functions of the app will require the app to have an active\n" +
                        "                  internet connection. The connection can be Wi-Fi, or provided\n" +
                        "                  by your mobile network provider, but ChatsiT\n" +
                        "                  cannot take responsibility for the app not working at full\n" +
                        "                  functionality if you don’t have access to Wi-Fi, and you don’t\n" +
                        "                  have any of your data allowance left.\n" +
                        "                </p> <p></p> <p>\n" +
                        "                  If you’re using the app outside of an area with Wi-Fi, you\n" +
                        "                  should remember that your terms of the agreement with your\n" +
                        "                  mobile network provider will still apply. As a result, you may\n" +
                        "                  be charged by your mobile provider for the cost of data for\n" +
                        "                  the duration of the connection while accessing the app, or\n" +
                        "                  other third party charges. In using the app, you’re accepting\n" +
                        "                  responsibility for any such charges, including roaming data\n" +
                        "                  charges if you use the app outside of your home territory\n" +
                        "                  (i.e. region or country) without turning off data roaming. If\n" +
                        "                  you are not the bill payer for the device on which you’re\n" +
                        "                  using the app, please be aware that we assume that you have\n" +
                        "                  received permission from the bill payer for using the app.\n" +
                        "                </p> <p>\n" +
                        "                  Along the same lines, ChatsiT cannot always take\n" +
                        "                  responsibility for the way you use the app i.e. You need to\n" +
                        "                  make sure that your device stays charged – if it runs out of\n" +
                        "                  battery and you can’t turn it on to avail the Service,\n" +
                        "                  ChatsiT cannot accept responsibility.\n" +
                        "                </p> <p>\n" +
                        "                  With respect to ChatsiT’s responsibility for your\n" +
                        "                  use of the app, when you’re using the app, it’s important to\n" +
                        "                  bear in mind that although we endeavour to ensure that it is\n" +
                        "                  updated and correct at all times, we do rely on third parties\n" +
                        "                  to provide information to us so that we can make it available\n" +
                        "                  to you. ChatsiT accepts no liability for any\n" +
                        "                  loss, direct or indirect, you experience as a result of\n" +
                        "                  relying wholly on this functionality of the app.\n" +
                        "                </p> <p>\n" +
                        "                  At some point, we may wish to update the app. The app is\n" +
                        "                  currently available on Android – the requirements for\n" +
                        "                  system(and for any additional systems we\n" +
                        "                  decide to extend the availability of the app to) may change,\n" +
                        "                  and you’ll need to download the updates if you want to keep\n" +
                        "                  using the app. ChatsiT does not promise that it\n" +
                        "                  will always update the app so that it is relevant to you\n" +
                        "                  and/or works with the Android version that you have\n" +
                        "                  installed on your device. However, you promise to always\n" +
                        "                  accept updates to the application when offered to you, We may\n" +
                        "                  also wish to stop providing the app, and may terminate use of\n" +
                        "                  it at any time without giving notice of termination to you.\n" +
                        "                  Unless we tell you otherwise, upon any termination, (a) the\n" +
                        "                  rights and licenses granted to you in these terms will end;\n" +
                        "                  (b) you must stop using the app, and (if needed) delete it\n" +
                        "                  from your device.\n" +
                "<p><strong>Terms &amp; Conditions for Objectionable Content</strong></p>\n" +
                        "<p>4. Objectionable Content Policy. Be Heard maintains a zero tolerance policy regarding objectionable content.</p>\n" +
                        "<p>Objectionable content may not be uploaded or displayed to the extent such content includes, is in conjunction with, or</p>\n" +
                        "<p>alongside any, Objectionable Content Objectionable Content includes, but is not limited to: (i) sexually explicit materials;</p>\n" +
                        "<p>(ii) obscene, defamatory, libelous, slanderous, violent and/or unlawful content or profanity; (iii) content that infringes upon</p>\n" +
                        "<p>the rights of any third party, including copyright, trademark, privacy, publicity or other personal or proprietary right, or that</p>\n" +
                        "<p>is deceptive or fraudulent; (iv) content that promotes the use or sale of illegal or regulated substances, tobacco products,</p>\n" +
                        "<p>ammunition and/or firearms; and (v) gambling, including without limitation, any online casino, sports books, bingo or</p>\n" +
                        "<p>poker. Any user can flag content they deem objectionable for review. Content will be moderated by Be Heard to ensure</p>\n" +
                        "<p>the timely removal of any and all objectionable content. User accounts which have been confirmed responsible for</p>\n" +
                        "<p>posting objectionable content will be restricted from access to the &nbsp;app.</p>\n" +
                        "<p><br></p>" +
                        "                </p> <p><strong>Changes to This Terms and Conditions</strong></p> <p>\n" +
                        "                  We may update our Terms and Conditions\n" +
                        "                  from time to time. Thus, you are advised to review this page\n" +
                        "                  periodically for any changes. We will\n" +
                        "                  notify you of any changes by posting the new Terms and\n" +
                        "                  Conditions on this page.\n" +
                        "                </p> <p>\n" +
                        "                  These terms and conditions are effective as of 2021-11-03\n" +
                        "                </p> <p><strong>Contact Us</strong></p> <p>\n" +
                        "                  If you have any questions or suggestions about our\n" +
                        "                  Terms and Conditions, do not hesitate to contact us\n" +
                        "                  at dyolandamasiel@gmail.com.\n" +
                        "                </p> </p>\n" +
                        "    </body>\n" +
                        "    </html>\n" +
                        "      ",
                "text/html", "UTF-8");

    }
}