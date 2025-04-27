package com.example.instagramfollowerextractor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private WebView webView;
    private Button btnExtractFollowers;
    private TextView tvStatus;
    private TextView tvLoading;
    private TextView tvPrivacyInfo;
    private ProgressBar progressBar;
    private Handler handler;
    private boolean profileStatsCollected = false;
    private ArrayList<Follower> followingList = new ArrayList<>();
    private ArrayList<Follower> followersList = new ArrayList<>();
    private ArrayList<Story> stories = new ArrayList<>();
    private ProfileInfo profileInfo = new ProfileInfo();
    private boolean isLoggedIn = false;
    private int storyRetryCount = 0;
    private static final int MAX_STORY_RETRIES = 3;
    private SharedPreferences prefs;
    private static final long LOADING_DELAY = 6000; // 6 seconds delay
    private static final int MAX_SCROLL_COUNT = 20; // Kaydırma sayısını arttırdım (daha fazla veri için)

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SharedPreferences for saving data
        prefs = getSharedPreferences("InstagramAnalyzerPrefs", MODE_PRIVATE);

        // Enable debugging for WebView if in development mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // Initialize UI elements
        initializeUIElements();

        // Configure WebView settings
        setupWebView();

        // Set up button click listener
        btnExtractFollowers.setOnClickListener(v -> startExtraction());

        // Load Instagram website
        webView.loadUrl("https://www.instagram.com/");
    }

    private void initializeUIElements() {
        webView = findViewById(R.id.webView);
        btnExtractFollowers = findViewById(R.id.btnExtractFollowers);
        tvStatus = findViewById(R.id.tvStatus);
        progressBar = findViewById(R.id.progressBar);
        tvLoading = findViewById(R.id.tvLoading);
        tvPrivacyInfo = findViewById(R.id.tvPrivacyInfo);
        handler = new Handler(Looper.getMainLooper());

        // Set initial visibility states
        tvLoading.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        tvPrivacyInfo.setText(R.string.privacy_notice);
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                tvStatus.setText(R.string.instagram_loaded);
                checkLoginStatus();
            }
        });
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "Android");
    }

    private void checkLoginStatus() {
        if (isLoggedIn) return;

        webView.evaluateJavascript(
                "(() => {\n" +
                        "  [...document.querySelectorAll('span')].find(el => el.textContent.trim() === 'Not now')?.click();\n" +
                        "  const isOnLoginPage = window.location.href.includes('/accounts/login');\n" +
                        "  const hasProfileIcon = document.querySelector('a[href*=\"/accounts/activity/\"]') !== null ||\n" +
                        "                        document.querySelector('span[aria-label=\"Profile\"]') !== null;\n" +
                        "  const profileLink = document.querySelector('a[href^=\"/\"][role=\"link\"]');\n" +
                        "  const username = profileLink ? profileLink.getAttribute('href').replace(/\\//g, '') : '';\n" +
                        "  const hasStories = document.querySelector('div[aria-label*=\"Story by\"]') !== null;\n" +
                        "  const forYouText = [...document.querySelectorAll('span')].find(el => el.textContent.trim() === 'For you');\n" +
                        "  const hasYourStory = [...document.querySelectorAll('span')].find(el => el.textContent.trim() === 'Your story') !== null;\n" +
                        "  const specialSpan = document.querySelector('span[class*=\"x1lliihq\"]');\n" +
                        "  const specialSpanText = specialSpan ? specialSpan.textContent.trim() : '';\n" +
                        "  return JSON.stringify({\n" +
                        "    isLoggedIn: !isOnLoginPage && (hasProfileIcon || username || hasStories || forYouText != null || hasYourStory),\n" +
                        "    hasStories: hasStories,\n" +
                        "    username: username,\n" +
                        "    specialSpanText: specialSpanText,\n" +
                        "    hasYourStory: hasYourStory\n" +
                        "  });\n" +
                        "})();",
                value -> {
                    try {
                        String cleanedJson = value;
                        if (cleanedJson.startsWith("\"") && cleanedJson.endsWith("\"")) {
                            cleanedJson = cleanedJson.substring(1, cleanedJson.length() - 1)
                                    .replace("\\\"", "\"")
                                    .replace("\\\\", "\\");
                        }

                        JSONObject result = new JSONObject(cleanedJson);
                        boolean loginDetected = result.optBoolean("isLoggedIn", false);
                        boolean hasStories = result.optBoolean("hasStories", false);
                        boolean hasYourStory = result.optBoolean("hasYourStory", false);
                        String username = result.optString("username", "");

                        if (loginDetected && !isLoggedIn) {
                            isLoggedIn = true;
                            Log.d(TAG, "User logged in: " + username);
                            Log.d(TAG, "Stories available: " + hasStories);
                            Log.d(TAG, "Your story available: " + hasYourStory);

                            runOnUiThread(() -> {
                                tvStatus.setText(R.string.login_successful);
                                btnExtractFollowers.setVisibility(View.VISIBLE);
                            });
                        } else if (!loginDetected && !isLoggedIn) {
                            handler.postDelayed(this::checkLoginStatus, 3000);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Login check error: " + e.getMessage(), e);
                        handler.postDelayed(this::checkLoginStatus, 3000);
                    }
                }
        );
    }

    private void startExtraction() {
        // Clear previous data
        followingList.clear();
        followersList.clear();
        stories.clear();
        profileStatsCollected = false;
        storyRetryCount = 0;

        // Update UI
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText(R.string.starting_process);
        tvLoading.setVisibility(View.VISIBLE);
        btnExtractFollowers.setEnabled(false);

        // Start the data extraction process
        goToHomePage();
    }

    private void goToHomePage() {
        tvStatus.setText(R.string.navigating_to_home);

        webView.evaluateJavascript(
                "(() => {\n" +
                        "  [...document.querySelectorAll('span')].find(el => el.textContent.trim() === 'Not now')?.click();\n" +
                        "  const homeButton = document.querySelector('a[href=\"/\"]');\n" +
                        "  if (homeButton) {\n" +
                        "    homeButton.click();\n" +
                        "    return 'home_clicked';\n" +
                        "  }\n" +
                        "  const hasStories = document.querySelectorAll('div[aria-label*=\"Story by\"]').length > 0;\n" +
                        "  return hasStories ? 'already_on_home_with_stories' : 'already_on_home';\n" +
                        "})();",
                value -> {
                    Log.d(TAG, "Home page navigation result: " + value);
                    handler.postDelayed(() -> {
                        tvStatus.setText(R.string.checking_stories);
                        processStories();
                    }, 5000);
                }
        );
    }

    private void processStories() {
        tvStatus.setText(getString(R.string.checking_stories_attempt, (storyRetryCount + 1), MAX_STORY_RETRIES));

        webView.evaluateJavascript(
                "async function processInstagramStories() {\n" +
                        "    try {\n" +
                        "        const results = [];\n" +
                        "        const storyElements = document.querySelectorAll('div[aria-label*=\"Story by\"]');\n" +
                        "        const storyUsers = [];\n" +
                        "        const maxUsers = Math.min(storyElements.length, 5);\n" +
                        "        console.log(`Toplam ${maxUsers} hikaye bulundu.`);\n" +
                        "        for (let i = 0; i < maxUsers; i++) {\n" +
                        "            const storyElement = storyElements[i];\n" +
                        "            const username = storyElement.querySelector('span[class*=\"x1lliihq x193iq5w\"]')?.textContent;\n" +
                        "            const profileImg = storyElement.querySelector('img[class*=\"xpdipgo\"]')?.src;\n" +
                        "            if (username && profileImg) {\n" +
                        "                storyUsers.push({\n" +
                        "                    username,\n" +
                        "                    profileImg,\n" +
                        "                    element: storyElement\n" +
                        "                });\n" +
                        "                console.log(`${i + 1}. Kullanıcı:`, username);\n" +
                        "                console.log('Profil Resmi:', profileImg);\n" +
                        "                console.log('------------------------');\n" +
                        "            } else {\n" +
                        "                console.warn(`Hikaye ${i + 1} eksik veri: username=${username}, profileImg=${profileImg}`);\n" +
                        "            }\n" +
                        "        }\n" +
                        "        async function waitForElement(selector, timeout = 10000) {\n" +
                        "            const startTime = Date.now();\n" +
                        "            while (Date.now() - startTime < timeout) {\n" +
                        "                const element = document.querySelector(selector);\n" +
                        "                if (element) {\n" +
                        "                    return element;\n" +
                        "                }\n" +
                        "                await new Promise(resolve => setTimeout(resolve, 100));\n" +
                        "            }\n" +
                        "            return null;\n" +
                        "        }\n" +
                        "        async function waitForElementToDisappear(selector, timeout = 5000) {\n" +
                        "            const startTime = Date.now();\n" +
                        "            while (Date.now() - startTime < timeout) {\n" +
                        "                const element = document.querySelector(selector);\n" +
                        "                if (!element) {\n" +
                        "                    return true;\n" +
                        "                }\n" +
                        "                await new Promise(resolve => setTimeout(resolve, 100));\n" +
                        "            }\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        async function waitForMediaElement(selector, previousSrc = null, timeout = 20000) {\n" +
                        "            const startTime = Date.now();\n" +
                        "            while (Date.now() - startTime < timeout) {\n" +
                        "                const element = document.querySelector(selector);\n" +
                        "                if (element && element.src && element.src !== '' && element.src !== previousSrc) {\n" +
                        "                    return element;\n" +
                        "                }\n" +
                        "                await new Promise(resolve => setTimeout(resolve, 100));\n" +
                        "            }\n" +
                        "            return null;\n" +
                        "        }\n" +
                        "        function observeMediaChange(selector, previousSrc, timeout = 20000) {\n" +
                        "            return new Promise((resolve) => {\n" +
                        "                const observer = new MutationObserver((mutations) => {\n" +
                        "                    const element = document.querySelector(selector);\n" +
                        "                    if (element && element.src && element.src !== '' && element.src !== previousSrc) {\n" +
                        "                        observer.disconnect();\n" +
                        "                        resolve(element);\n" +
                        "                    }\n" +
                        "                });\n" +
                        "                observer.observe(document.body, { childList: true, subtree: true, attributes: true });\n" +
                        "                setTimeout(() => {\n" +
                        "                    observer.disconnect();\n" +
                        "                    resolve(null);\n" +
                        "                }, timeout);\n" +
                        "            });\n" +
                        "        }\n" +
                        "        function simulateClick(element) {\n" +
                        "            const clickEvent = new MouseEvent('click', {\n" +
                        "                view: window,\n" +
                        "                bubbles: true,\n" +
                        "                cancelable: true\n" +
                        "            });\n" +
                        "            element.dispatchEvent(clickEvent);\n" +
                        "        }\n" +
                        "        let previousMediaSrc = null;\n" +
                        "        \n" +
                        "        // Tam olarak örnekteki gibi hikaye işleme yöntemi kullanılıyor\n" +
                        "        for (const user of storyUsers) {\n" +
                        "            console.log(`${user.username} hikayesi açılıyor...`);\n" +
                        "            user.element.click();\n" +
                        "            await new Promise(resolve => setTimeout(resolve, 1000));\n" +
                        "            \n" +
                        "            const storyContainer = await waitForElement('div.x5yr21d.x1n2onr6.xh8yej3', 10000);\n" +
                        "            if (!storyContainer) {\n" +
                        "                console.log(`${user.username} - Hikaye kapsayıcısı yüklenemedi.`);\n" +
                        "                continue;\n" +
                        "            }\n" +
                        "            \n" +
                        "            try {\n" +
                        "                let mediaUrl = null;\n" +
                        "                const videoSelector = 'div.x5yr21d.x1n2onr6.xh8yej3 div.x1lliihq.x5yr21d.x1n2onr6.xh8yej3.x1ja2u2z video';\n" +
                        "                console.log(`${user.username} - Video selektörü: ${videoSelector}`);\n" +
                        "                let video = await observeMediaChange(videoSelector, previousMediaSrc, 20000);\n" +
                        "                if (!video) {\n" +
                        "                    video = await waitForMediaElement(videoSelector, previousMediaSrc, 20000);\n" +
                        "                }\n" +
                        "                \n" +
                        "                if (video) {\n" +
                        "                    mediaUrl = video.src;\n" +
                        "                    console.log(`${user.username} - Video URL:`, mediaUrl);\n" +
                        "                    results.push({\n" +
                        "                        username: user.username,\n" +
                        "                        profileImg: user.profileImg,\n" +
                        "                        mediaUrl: mediaUrl,\n" +
                        "                        isVideo: true\n" +
                        "                    });\n" +
                        "                    previousMediaSrc = mediaUrl;\n" +
                        "                } else {\n" +
                        "                    const imgSelector = 'div.x5yr21d.x1n2onr6.xh8yej3 img[class*=\"xl1xv1r\"]';\n" +
                        "                    console.log(`${user.username} - Görsel selektörü: ${imgSelector}`);\n" +
                        "                    let img = await observeMediaChange(imgSelector, previousMediaSrc, 20000);\n" +
                        "                    if (!img) {\n" +
                        "                        img = await waitForMediaElement(imgSelector, previousMediaSrc, 20000);\n" +
                        "                    }\n" +
                        "                    \n" +
                        "                    if (img) {\n" +
                        "                        mediaUrl = img.src;\n" +
                        "                        console.log(`${user.username} - Fotoğraf URL:`, mediaUrl);\n" +
                        "                        results.push({\n" +
                        "                            username: user.username,\n" +
                        "                            profileImg: user.profileImg,\n" +
                        "                            mediaUrl: mediaUrl,\n" +
                        "                            isVideo: false\n" +
                        "                        });\n" +
                        "                        previousMediaSrc = mediaUrl;\n" +
                        "                    } else {\n" +
                        "                        console.log(`${user.username} - Medya bulunamadı.`);\n" +
                        "                    }\n" +
                        "                }\n" +
                        "                \n" +
                        "                // Orjinal kodda olduğu gibi 2 saniye bekle\n" +
                        "                await new Promise(resolve => setTimeout(resolve, 2000));\n" +
                        "                \n" +
                        "                // Hikayeleri kapat\n" +
                        "                console.log(`${user.username} - Hikaye kapatılmaya çalışılıyor...`);\n" +
                        "                const closeButton = await waitForElement('div[role=\"button\"] svg[aria-label=\"Close\"], div[role=\"button\"] svg[aria-label=\"Kapat\"]', 3000);\n" +
                        "                \n" +
                        "                if (closeButton) {\n" +
                        "                    const buttonParent = closeButton.closest('div[role=\"button\"]');\n" +
                        "                    if (buttonParent) {\n" +
                        "                        simulateClick(buttonParent);\n" +
                        "                        console.log(`${user.username} - Kapatma butonuna tıklandı.`);\n" +
                        "                        const containerDisappeared = await waitForElementToDisappear('div.x5yr21d.x1n2onr6.xh8yej3', 5000);\n" +
                        "                        if (containerDisappeared) {\n" +
                        "                            console.log(`${user.username} - Hikaye başarıyla kapatıldı.`);\n" +
                        "                        } else {\n" +
                        "                            console.log(`${user.username} - Hikaye kapsayıcısı kaybolmadı, Esc tuşu deneniyor...`);\n" +
                        "                            document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));\n" +
                        "                            await new Promise(resolve => setTimeout(resolve, 500));\n" +
                        "                        }\n" +
                        "                    } else {\n" +
                        "                        console.log(`${user.username} - Kapatma butonunun üst elemanı bulunamadı, Esc tuşu deneniyor...`);\n" +
                        "                        document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));\n" +
                        "                        await new Promise(resolve => setTimeout(resolve, 500));\n" +
                        "                    }\n" +
                        "                } else {\n" +
                        "                    console.log(`${user.username} - Kapatma butonu bulunamadı, Esc tuşu deneniyor...`);\n" +
                        "                    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));\n" +
                        "                    await new Promise(resolve => setTimeout(resolve, 500));\n" +
                        "                }\n" +
                        "                \n" +
                        "                // Sonraki hikayeye geçmeden önce 1 saniye bekle\n" +
                        "                await new Promise(resolve => setTimeout(resolve, 1000));\n" +
                        "                \n" +
                        "            } catch (error) {\n" +
                        "                console.error(`${user.username} hikayesi işlenirken hata:`, error);\n" +
                        "                // Hata durumunda hikayeyi kapat ve devam et\n" +
                        "                document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));\n" +
                        "                await new Promise(resolve => setTimeout(resolve, 1000));\n" +
                        "            }\n" +
                        "        }\n" +
                        "        \n" +
                        "        console.log('Tüm hikayeler işlendi, sonuç sayısı:', results.length);\n" +
                        "        window.Android.onStoriesExtracted(JSON.stringify(results));\n" +
                        "        return JSON.stringify(results);\n" +
                        "    } catch (error) {\n" +
                        "        console.error('processInstagramStories genel hata:', error);\n" +
                        "        window.Android.onStoriesExtracted(JSON.stringify([]));\n" +
                        "        return JSON.stringify([]);\n" +
                        "    }\n" +
                        "}\n" +
                        "processInstagramStories();",
                value -> {
                    Log.d(TAG, "Story yanıtı raw (evaluateJavascript): " + value);
                    // Bu artık kullanılmayacak, çünkü veri MyJavaScriptInterface üzerinden gelecek
                }
        );
    }

    private void retryProcessStories() {
        storyRetryCount++;
        if (storyRetryCount < MAX_STORY_RETRIES) {
            Log.w(TAG, "Hikayeler alınamadı, tekrar deneniyor... (Deneme " + storyRetryCount + "/" + MAX_STORY_RETRIES + ")");
            handler.postDelayed(this::processStories, 3000);
        } else {
            Log.e(TAG, "Hikayeler " + MAX_STORY_RETRIES + " denemede alınamadı, devam ediliyor...");
            storyRetryCount = 0;
            handler.postDelayed(() -> {
                tvStatus.setText("Hikayeler alınamadı, profil sayfasına gidiliyor...");
                goToProfile();
            }, 2000);
        }
    }

    private void goToProfile() {
        tvStatus.setText("Profil sayfasına yönlendiriliyor...");
        webView.evaluateJavascript(
                "(() => {\n" +
                        "  [...document.querySelectorAll('span')].find(el => el.textContent.trim() === 'Not now')?.click();\n" +
                        "  try {\n" +
                        "    const profileLink = document.querySelector(\"#mount_0_0_AB > div > div > div.x9f619.x1n2onr6.x1ja2u2z > div > div > div.x78zum5.xdt5ytf.x1t2pt76.x1n2onr6.x1ja2u2z.x10cihs4 > div.x9f619.xvbhtw8.x78zum5.x168nmei.x13lgxp2.x5pf9jr.xo71vjh.x1uhb9sk.x1plvlek.xryxfnj.x1c4vz4f.x2lah0s.xdt5ytf.xqjyukv.x1qjc9v5.x1oa3qoh.x1qughib > div.x9f619.xjbqb8w.x78zum5.x168nmei.x13lgxp2.x5pf9jr.xo71vjh.xixxii4.x1ey2m1c.x1plvlek.xryxfnj.x1c4vz4f.x2lah0s.xdt5ytf.xqjyukv.x1qjc9v5.x1oa3qoh.x1nhvcw1.xg7h5cd.xh8yej3.xhtitgo.x6w1myc.x1jeouym > div > div > div > div > div > div:nth-child(5) > div > span > div > a\");\n" +
                        "    if (profileLink) {\n" +
                        "      profileLink.click();\n" +
                        "      return 'clicked_specific';\n" +
                        "    }\n" +
                        "    const parentDiv = document.querySelector('div:nth-child(5) > div > span > div');\n" +
                        "    if (parentDiv) {\n" +
                        "      const link = parentDiv.querySelector('a');\n" +
                        "      if (link) {\n" +
                        "        link.click();\n" +
                        "        return 'clicked_alternative';\n" +
                        "      }\n" +
                        "    }\n" +
                        "    let clicked = false;\n" +
                        "    document.querySelectorAll('a').forEach(a => {\n" +
                        "      if ((a.href.includes('/accounts/edit/') || a.href.match(/\\/[a-zA-Z0-9_.]+\\/$/) && !clicked)) {\n" +
                        "        a.click();\n" +
                        "        clicked = true;\n" +
                        "      }\n" +
                        "    });\n" +
                        "    return clicked ? 'clicked_generic' : 'not_found';\n" +
                        "  } catch (error) {\n" +
                        "    console.error('Profil bağlantısını tıklama hatası:', error);\n" +
                        "    return 'error: ' + error.message;\n" +
                        "  }\n" +
                        "})();",
                value -> {
                    if (value.contains("clicked")) {
                        handler.postDelayed(() -> {
                            if (!profileStatsCollected) {
                                collectProfileStats();
                            } else {
                                clickOnFollowers();
                            }
                        }, 5000);
                    } else {
                        Toast.makeText(MainActivity.this, "Profil bulunamadı, tekrar deneniyor...", Toast.LENGTH_SHORT).show();
                        handler.postDelayed(this::goToProfile, 3000);
                    }
                });
    }

    private void collectProfileStats() {
        tvStatus.setText("Profil istatistikleri toplanıyor...");
        webView.evaluateJavascript(
                "(() => {\n" +
                        "  [...document.querySelectorAll('span')].find(el => el.textContent.trim() === 'Not now')?.click();\n" +
                        "  const usernameElement = document.querySelector('h2');\n" +
                        "  const username = usernameElement ? usernameElement.textContent.trim() : '';\n" +
                        "  const section = document.querySelector('section, header');\n" +
                        "  if (!section) return JSON.stringify({error: 'Section bulunamadı'});\n" +
                        "  const values = section.querySelectorAll('ul > li span.html-span, span._ac2a');\n" +
                        "  if (values.length < 3) return JSON.stringify({error: 'Veriler eksik'});\n" +
                        "  const [posts, followers, following] = Array.from(values).map(el => el.innerText.trim());\n" +
                        "  const profileImg = document.querySelector('img[alt=\"Change profile photo\"], img._aadp');\n" +
                        "  const profilePicURL = profileImg ? profileImg.src : '';\n" +
                        "  return JSON.stringify({\n" +
                        "    username,\n" +
                        "    posts,\n" +
                        "    followers,\n" +
                        "    following,\n" +
                        "    profilePicURL\n" +
                        "  });\n" +
                        "})();",
                value -> {
                    try {
                        String cleanedJson = value;
                        if (cleanedJson.startsWith("\"") && cleanedJson.endsWith("\"")) {
                            cleanedJson = cleanedJson.substring(1, cleanedJson.length() - 1)
                                    .replace("\\\"", "\"")
                                    .replace("\\\\", "\\");
                        }

                        JSONObject stats = new JSONObject(cleanedJson);
                        if (stats.has("error")) {
                            Toast.makeText(MainActivity.this, "Hata: " + stats.getString("error"), Toast.LENGTH_LONG).show();
                            handler.postDelayed(this::collectProfileStats, 3000);
                        } else {
                            String postsStr = stats.optString("posts", "0");
                            String followersStr = stats.optString("followers", "0");
                            String followingStr = stats.optString("following", "0");

                            postsStr = postsStr.replaceAll("[^0-9]", "");
                            followersStr = followersStr.replaceAll("[^0-9]", "");
                            followingStr = followingStr.replaceAll("[^0-9]", "");

                            int postsCount = 0, followersCount = 0, followingCount = 0;
                            try {
                                postsCount = Integer.parseInt(postsStr);
                            } catch (NumberFormatException e) {}
                            try {
                                followersCount = Integer.parseInt(followersStr);
                            } catch (NumberFormatException e) {}
                            try {
                                followingCount = Integer.parseInt(followingStr);
                            } catch (NumberFormatException e) {}

                            profileInfo.setUsername(stats.optString("username", ""));
                            profileInfo.setProfilePicURL(stats.optString("profilePicURL", ""));
                            profileInfo.setPostsCount(postsCount);
                            profileInfo.setFollowersCount(followersCount);
                            profileInfo.setFollowingCount(followingCount);

                            // Save profile info to SharedPreferences
                            saveProfileInfo();

                            String infoText = "Kullanıcı: " + profileInfo.getUsername() +
                                    ", Gönderi: " + profileInfo.getPostsCount() +
                                    ", Takipçi: " + profileInfo.getFollowersCount() +
                                    ", Takip: " + profileInfo.getFollowingCount();

                            tvStatus.setText("Profil bilgileri alındı. Following sayfasına yönlendiriliyor...");
                            Toast.makeText(MainActivity.this, infoText, Toast.LENGTH_LONG).show();

                            profileStatsCollected = true;
                            handler.postDelayed(this::clickOnFollowing, 5000);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "JSON parse error: " + e.getMessage(), e);
                        Toast.makeText(MainActivity.this, "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        handler.postDelayed(this::collectProfileStats, 3000);
                    }
                });
    }

    private void saveProfileInfo() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", profileInfo.getUsername());
        editor.putString("profilePicURL", profileInfo.getProfilePicURL());
        editor.putInt("postsCount", profileInfo.getPostsCount());
        editor.putInt("followersCount", profileInfo.getFollowersCount());
        editor.putInt("followingCount", profileInfo.getFollowingCount());
        editor.putLong("lastUpdated", new Date().getTime());
        editor.apply();
    }

    private void clickOnFollowing() {
        tvStatus.setText("Following listesine erişiliyor...");
        webView.evaluateJavascript(
                "(() => {\n" +
                        "  [...document.querySelectorAll('span')].find(el => el.textContent.trim() === 'Not now')?.click();\n" +
                        "  const followingLink = document.querySelector('a[href*=\"/following/\"]');\n" +
                        "  if (followingLink) {\n" +
                        "    followingLink.click();\n" +
                        "    return 'clicked';\n" +
                        "  } else {\n" +
                        "    return 'not_found';\n" +
                        "  }\n" +
                        "})();",
                value -> {
                    if (value.contains("clicked")) {
                        handler.postDelayed(this::scrollFollowingList, 5000);
                    } else {
                        Toast.makeText(MainActivity.this, "Following butonu bulunamadı, tekrar deneniyor...", Toast.LENGTH_SHORT).show();
                        handler.postDelayed(this::clickOnFollowing, 3000);
                    }
                });
    }

    private void scrollFollowingList() {
        tvStatus.setText("Following listesi kaydırılıyor...");
        webView.evaluateJavascript(
                "(() => {\n" +
                        "    const scrollContainer = document.querySelector('div[style*=\"overflow: hidden auto\"]');\n" +
                        "    if (!scrollContainer) {\n" +
                        "        console.error('HATA: Kaydırılabilir kapsayıcı bulunamadı! Lütfen overflow: hidden auto olan div\\'i kontrol edin.');\n" +
                        "        console.log('Tüm overflow div\\'leri:', document.querySelectorAll('div[style*=\"overflow\"]'));\n" +
                        "        return 'scroll_container_not_found';\n" +
                        "    }\n" +
                        "    // Kapsayıcıya sabit yükseklik ekle (kaydırma çubuğu için)\n" +
                        "    scrollContainer.style.height = '300px';\n" +
                        "    console.log('Kapsayıcı yüksekliği ayarlandı:', scrollContainer.style.height);\n" +
                        "    console.log('Kapsayıcı durumu: scrollHeight=', scrollContainer.scrollHeight, 'clientHeight=', scrollContainer.clientHeight);\n" +
                        "    \n" +
                        "    // Başlangıçta kaç takip eden var, kontrol et\n" +
                        "    let initialCount = document.querySelectorAll('div.x1dm5mii.x16mil14.xiojian.x1yutycm').length;\n" +
                        "    console.log('Başlangıç following sayısı:', initialCount);\n" +
                        "    \n" +
                        "    let scrollCount = 0;\n" +
                        "    const maxScrolls = " + MAX_SCROLL_COUNT + ";\n" +
                        "    const scrollDistance = 500; // Her seferinde 500px kaydır\n" +
                        "    let previousItemCount = initialCount;\n" +
                        "    let sameCountIterations = 0; // Aynı sayıda takipçi gelirse sayaç\n" +
                        "    \n" +
                        "    function scrollElement() {\n" +
                        "        if (scrollCount < maxScrolls) {\n" +
                        "            // Mevcut kullanıcı sayısını kontrol et\n" +
                        "            const currentCount = document.querySelectorAll('div.x1dm5mii.x16mil14.xiojian.x1yutycm').length;\n" +
                        "            \n" +
                        "            // Eğer aynı sayıda kullanıcı varsa (yeni yükleme yapılmamış) sayacı artır\n" +
                        "            if (currentCount === previousItemCount) {\n" +
                        "                sameCountIterations++;\n" +
                        "            } else {\n" +
                        "                sameCountIterations = 0; // Yeni kullanıcılar yüklenmiş, sayacı sıfırla\n" +
                        "                previousItemCount = currentCount;\n" +
                        "            }\n" +
                        "            \n" +
                        "            // Eğer 5 kez kaydırma yapılmış ve hala aynı sayıda kullanıcı varsa, muhtemelen liste sonu gelmiştir\n" +
                        "            if (sameCountIterations >= 5) {\n" +
                        "                console.log('5 kez kaydırıldı ve yeni içerik yüklenmedi. Muhtemelen liste sonuna ulaşıldı.');\n" +
                        "                scrollContainer.scrollTo({ top: 0, behavior: 'smooth' });\n" +
                        "                setTimeout(() => {\n" +
                        "                    const finalCount = document.querySelectorAll('div.x1dm5mii.x16mil14.xiojian.x1yutycm').length;\n" +
                        "                    console.log(`Kaydırma tamamlandı. Başlangıç: ${initialCount}, Son durum: ${finalCount} kullanıcı`);\n" +
                        "                    window.Android.onFollowingScrollComplete('done');\n" +
                        "                }, 1000);\n" +
                        "                return;\n" +
                        "            }\n" +
                        "            \n" +
                        "            // Smooth kaydırma\n" +
                        "            scrollContainer.scrollBy({\n" +
                        "                top: scrollDistance,\n" +
                        "                behavior: 'smooth'\n" +
                        "            });\n" +
                        "            scrollCount++;\n" +
                        "            console.log(`Kaydırma ${scrollCount}/${maxScrolls} | scrollTop: ${scrollContainer.scrollTop} | Yüklenen kullanıcı: ${currentCount}`);\n" +
                        "            \n" +
                        "            // Kaydırmadan sonra biraz bekle ve tekrar dene\n" +
                        "            setTimeout(() => {\n" +
                        "                // Eğer maksimum kaydırma sayısına ulaştıysak\n" +
                        "                if (scrollCount >= maxScrolls) {\n" +
                        "                    console.log('Maksimum kaydırma sayısına ulaşıldı.');\n" +
                        "                    // Başa dön (smooth)\n" +
                        "                    scrollContainer.scrollTo({ top: 0, behavior: 'smooth' });\n" +
                        "                    setTimeout(() => {\n" +
                        "                        const finalCount = document.querySelectorAll('div.x1dm5mii.x16mil14.xiojian.x1yutycm').length;\n" +
                        "                        console.log(`Kaydırma tamamlandı. Başlangıç: ${initialCount}, Son durum: ${finalCount} kullanıcı`);\n" +
                        "                        window.Android.onFollowingScrollComplete('done');\n" +
                        "                    }, 1000);\n" +
                        "                } else {\n" +
                        "                    scrollElement();\n" +
                        "                }\n" +
                        "            }, 2000); // Yeni içeriğin yüklenmesi için 2 saniye bekleyelim\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    console.log('Kaydırma başlıyor...');\n" +
                        "    scrollElement();\n" +
                        "    return 'scrolling_started';\n" +
                        "})();",
                value -> {
                    Log.d(TAG, "Following scrolling result: " + value);
                    // JavaScript'in onFollowingScrollComplete'i çağırmasını bekleyeceğiz
                }
        );
    }

    private void extractFollowing() {
        tvStatus.setText("Following listesi toplanıyor...");
        webView.evaluateJavascript(
                "function extractInstagramFollowing() {\n" +
                        "  const following = [];\n" +
                        "  // Tüm kullanıcı konteynerlerini bul\n" +
                        "  const containers = document.querySelectorAll('div.x1dm5mii.x16mil14.xiojian.x1yutycm');\n" +
                        "  console.log(`Toplam ${containers.length} following kullanıcısı bulundu`);\n" +
                        "  \n" +
                        "  // Her bir konteyneri işle\n" +
                        "  containers.forEach((container, index) => {\n" +
                        "    try {\n" +
                        "      // Takip edilen kullanıcıları tespit etmek için buton kontrolü\n" +
                        "      const button = container.querySelector('button');\n" +
                        "      if (button && button.textContent.includes('Following')) {\n" +
                        "        const usernameElement = container.querySelector('a[href^=\"/\"]:not([aria-disabled=\"true\"])');\n" +
                        "        const username = usernameElement ? usernameElement.getAttribute('href').replace('/', '') : '';\n" +
                        "        const displayName = container.querySelector('span.x1lliihq.x193iq5w.x6ikm8r.x10wlt62.xlyipyv.xuxw1ft')?.textContent.trim() || '';\n" +
                        "        const profilePicElement = container.querySelector('img.xpdipgo.x972fbf');\n" +
                        "        const profilePicURL = profilePicElement ? profilePicElement.getAttribute('src') : '';\n" +
                        "        \n" +
                        "        if (username && displayName && profilePicURL) {\n" +
                        "          following.push({\n" +
                        "            username,\n" +
                        "            displayName,\n" +
                        "            profilePicURL\n" +
                        "          });\n" +
                        "          console.log(`${index+1}. Following: ${displayName} (@${username})`);\n" +
                        "        } else {\n" +
                        "          console.warn(`${index+1}. Eksik veri: username=${username}, displayName=${displayName}, profilePic=${profilePicURL}`);\n" +
                        "        }\n" +
                        "      }\n" +
                        "    } catch (error) {\n" +
                        "      console.error(`Following veri çıkarma hatası (${index}):`, error);\n" +
                        "    }\n" +
                        "  });\n" +
                        "  \n" +
                        "  console.log('\\n=== Following Sonuçları ===');\n" +
                        "  console.log(`Toplam takip edilen: ${following.length}`);\n" +
                        "  return following;\n" +
                        "}\n" +
                        "const following = extractInstagramFollowing();\n" +
                        "JSON.stringify(following);",
                value -> {
                    try {
                        followingList = parseFollowerData(value);
                        Toast.makeText(MainActivity.this, followingList.size() + " takip edilen kullanıcı bulundu", Toast.LENGTH_SHORT).show();
                        profileInfo.setFollowingCount(followingList.size());

                        // Following listesini kaydet
                        saveFollowingList();

                        tvStatus.setText("Profil sayfasına dönülüyor...");
                        goToProfile();
                    } catch (Exception e) {
                        Log.e(TAG, "Following verisi işleme hatası: " + e.getMessage(), e);
                        Toast.makeText(MainActivity.this, "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        handler.postDelayed(this::extractFollowing, 3000);
                    }
                });
    }

    private void saveFollowingList() {
        DataManager.getInstance(this).saveFollowingList(followingList);
    }

    private void clickOnFollowers() {
        tvStatus.setText("Takipçi listesine erişiliyor...");
        tvLoading.setVisibility(View.VISIBLE);

        webView.evaluateJavascript(
                "(() => {\n" +
                        "  [...document.querySelectorAll('span')].find(el => el.textContent.trim() === 'Not now')?.click();\n" +
                        "  const followersLink = document.querySelector('a[href*=\"followers\"]');\n" +
                        "  if (followersLink) {\n" +
                        "    followersLink.click();\n" +
                        "    return 'clicked';\n" +
                        "  } else {\n" +
                        "    return 'not_found';\n" +
                        "  }\n" +
                        "})();",
                value -> {
                    handler.postDelayed(this::checkFollowersModal, 3000);
                });
    }

    private void checkFollowersModal() {
        tvLoading.setVisibility(View.VISIBLE);

        webView.evaluateJavascript(
                "(() => {\n" +
                        "  [...document.querySelectorAll('span')].find(el => el.textContent.trim() === 'Not now')?.click();\n" +
                        "  const modal = document.querySelector('div[role=\"dialog\"]');\n" +
                        "  const followersList = document.querySelectorAll('div.x1dm5mii.x16mil14.xiojian.x1yutycm');\n" +
                        "  if (followersList && followersList.length > 0) {\n" +
                        "    return 'followers_found: ' + followersList.length;\n" +
                        "  }\n" +
                        "  if (!modal) {\n" +
                        "    return 'modal_not_found';\n" +
                        "  }\n" +
                        "  return 'modal_found_but_no_followers';\n" +
                        "})();",
                modalStatus -> {
                    Log.d(TAG, "checkFollowersModal sonucu: " + modalStatus);

                    if (modalStatus.contains("followers_found")) {
                        String countStr = modalStatus.split(":")[1].trim();
                        int count = 0;
                        try {
                            count = Integer.parseInt(countStr);
                        } catch (Exception e) {}
                        Toast.makeText(MainActivity.this, count + " takipçi görünüyor, kaydırılıyor...", Toast.LENGTH_SHORT).show();
                        tvLoading.setVisibility(View.VISIBLE);
                        handler.postDelayed(this::scrollFollowersList, 2000);
                    } else if (modalStatus.contains("modal_found")) {
                        tvLoading.setVisibility(View.VISIBLE);
                        handler.postDelayed(this::checkFollowersModal, 2000);
                    } else {
                        Toast.makeText(MainActivity.this, "Takipçi listesi açılamadı, tekrar deneniyor...", Toast.LENGTH_SHORT).show();
                        tvLoading.setVisibility(View.VISIBLE);
                        handler.postDelayed(this::clickOnFollowers, 3000);
                    }
                });
    }

    private void scrollFollowersList() {
        tvStatus.setText("Takipçi listesi kaydırılıyor...");
        webView.evaluateJavascript(
                "(() => {\n" +
                        "    const scrollContainer = document.querySelector('div[style*=\"overflow: hidden auto\"]');\n" +
                        "    if (!scrollContainer) {\n" +
                        "        console.error('HATA: Kaydırılabilir kapsayıcı bulunamadı! Lütfen overflow: hidden auto olan div\\'i kontrol edin.');\n" +
                        "        console.log('Tüm overflow div\\'leri:', document.querySelectorAll('div[style*=\"overflow\"]'));\n" +
                        "        return 'scroll_container_not_found';\n" +
                        "    }\n" +
                        "    // Kapsayıcıya sabit yükseklik ekle (kaydırma çubuğu için)\n" +
                        "    scrollContainer.style.height = '300px';\n" +
                        "    console.log('Kapsayıcı yüksekliği ayarlandı:', scrollContainer.style.height);\n" +
                        "    console.log('Kapsayıcı durumu: scrollHeight=', scrollContainer.scrollHeight, 'clientHeight=', scrollContainer.clientHeight);\n" +
                        "    \n" +
                        "    // Başlangıçta kaç takipçi var, kontrol et\n" +
                        "    let initialCount = document.querySelectorAll('div.x1dm5mii.x16mil14.xiojian.x1yutycm').length;\n" +
                        "    console.log('Başlangıç takipçi sayısı:', initialCount);\n" +
                        "    \n" +
                        "    let scrollCount = 0;\n" +
                        "    const maxScrolls = " + MAX_SCROLL_COUNT + ";\n" +
                        "    const scrollDistance = 500; // Her seferinde 500px kaydır\n" +
                        "    let previousItemCount = initialCount;\n" +
                        "    let sameCountIterations = 0; // Aynı sayıda takipçi gelirse sayaç\n" +
                        "    \n" +
                        "    function scrollElement() {\n" +
                        "        if (scrollCount < maxScrolls) {\n" +
                        "            // Mevcut kullanıcı sayısını kontrol et\n" +
                        "            const currentCount = document.querySelectorAll('div.x1dm5mii.x16mil14.xiojian.x1yutycm').length;\n" +
                        "            \n" +
                        "            // Eğer aynı sayıda kullanıcı varsa (yeni yükleme yapılmamış) sayacı artır\n" +
                        "            if (currentCount === previousItemCount) {\n" +
                        "                sameCountIterations++;\n" +
                        "            } else {\n" +
                        "                sameCountIterations = 0; // Yeni kullanıcılar yüklenmiş, sayacı sıfırla\n" +
                        "                previousItemCount = currentCount;\n" +
                        "            }\n" +
                        "            \n" +
                        "            // Eğer 5 kez kaydırma yapılmış ve hala aynı sayıda kullanıcı varsa, muhtemelen liste sonu gelmiştir\n" +
                        "            if (sameCountIterations >= 5) {\n" +
                        "                console.log('5 kez kaydırıldı ve yeni içerik yüklenmedi. Muhtemelen liste sonuna ulaşıldı.');\n" +
                        "                scrollContainer.scrollTo({ top: 0, behavior: 'smooth' });\n" +
                        "                setTimeout(() => {\n" +
                        "                    const finalCount = document.querySelectorAll('div.x1dm5mii.x16mil14.xiojian.x1yutycm').length;\n" +
                        "                    console.log(`Kaydırma tamamlandı. Başlangıç: ${initialCount}, Son durum: ${finalCount} takipçi`);\n" +
                        "                    window.Android.onFollowersScrollComplete('done');\n" +
                        "                }, 1000);\n" +
                        "                return;\n" +
                        "            }\n" +
                        "            \n" +
                        "            // Smooth kaydırma\n" +
                        "            scrollContainer.scrollBy({\n" +
                        "                top: scrollDistance,\n" +
                        "                behavior: 'smooth'\n" +
                        "            });\n" +
                        "            scrollCount++;\n" +
                        "            console.log(`Kaydırma ${scrollCount}/${maxScrolls} | scrollTop: ${scrollContainer.scrollTop} | Yüklenen takipçi: ${currentCount}`);\n" +
                        "            \n" +
                        "            // Kaydırmadan sonra biraz bekle ve tekrar dene\n" +
                        "            setTimeout(() => {\n" +
                        "                // Eğer maksimum kaydırma sayısına ulaştıysak\n" +
                        "                if (scrollCount >= maxScrolls) {\n" +
                        "                    console.log('Maksimum kaydırma sayısına ulaşıldı.');\n" +
                        "                    // Başa dön (smooth)\n" +
                        "                    scrollContainer.scrollTo({ top: 0, behavior: 'smooth' });\n" +
                        "                    setTimeout(() => {\n" +
                        "                        const finalCount = document.querySelectorAll('div.x1dm5mii.x16mil14.xiojian.x1yutycm').length;\n" +
                        "                        console.log(`Kaydırma tamamlandı. Başlangıç: ${initialCount}, Son durum: ${finalCount} takipçi`);\n" +
                        "                        window.Android.onFollowersScrollComplete('done');\n" +
                        "                    }, 1000);\n" +
                        "                } else {\n" +
                        "                    scrollElement();\n" +
                        "                }\n" +
                        "            }, 2000); // Yeni içeriğin yüklenmesi için 2 saniye bekleyelim\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    console.log('Kaydırma başlıyor...');\n" +
                        "    scrollElement();\n" +
                        "    return 'scrolling_started';\n" +
                        "})();",
                value -> {
                    Log.d(TAG, "Followers scrolling result: " + value);
                    // JavaScript'in onFollowersScrollComplete'i çağırmasını bekleyeceğiz
                }
        );
    }

    private void getFollowersList() {
        tvStatus.setText("Takipçi listesi toplanıyor...");
        webView.evaluateJavascript(
                "function extractInstagramFollowers() {\n" +
                        "  const followers = [];\n" +
                        "  // Tüm kullanıcı konteynerlerini bul\n" +
                        "  const containers = document.querySelectorAll('div.x1dm5mii.x16mil14.xiojian.x1yutycm');\n" +
                        "  console.log(`Toplam ${containers.length} takipçi bulundu`);\n" +
                        "  \n" +
                        "  // Her bir konteyneri işle\n" +
                        "  containers.forEach((container, index) => {\n" +
                        "    try {\n" +
                        "      // Follow request butonu olan elemanları atla\n" +
                        "      if (container.querySelector('button._acan._acap._acas._aj1-._ap30')) {\n" +
                        "        return;\n" +
                        "      }\n" +
                        "      \n" +
                        "      const usernameElement = container.querySelector('a[href^=\"/\"]:not([aria-disabled=\"true\"])');\n" +
                        "      const username = usernameElement ? usernameElement.getAttribute('href').replace('/', '') : '';\n" +
                        "      const displayName = container.querySelector('span.x1lliihq.x193iq5w.x6ikm8r.x10wlt62.xlyipyv.xuxw1ft')?.textContent.trim() || '';\n" +
                        "      const profilePicURL = container.querySelector('img.xpdipgo.x972fbf')?.getAttribute('src') || '';\n" +
                        "      const isFollowingYou = container.querySelector('button')?.textContent.includes('Following') || false;\n" +
                        "      \n" +
                        "      if (username && displayName && profilePicURL) {\n" +
                        "        followers.push({\n" +
                        "          username,\n" +
                        "          displayName,\n" +
                        "          profilePicURL,\n" +
                        "          isFollowingYou\n" +
                        "        });\n" +
                        "        console.log(`${index+1}. Takipçi: ${displayName} (@${username})${isFollowingYou ? ' - Sizi Takip Ediyor' : ''}`);\n" +
                        "      } else {\n" +
                        "        console.warn(`${index+1}. Eksik veri: username=${username}, displayName=${displayName}, profilePic=${profilePicURL}`);\n" +
                        "      }\n" +
                        "    } catch (error) {\n" +
                        "      console.error(`Takipçi veri çıkarma hatası (${index}):`, error);\n" +
                        "    }\n" +
                        "  });\n" +
                        "  \n" +
                        "  console.log('\\n=== Takipçi Sonuçları ===');\n" +
                        "  console.log(`Toplam takipçi: ${followers.length}`);\n" +
                        "  return followers;\n" +
                        "}\n" +
                        "const followers = extractInstagramFollowers();\n" +
                        "JSON.stringify(followers);",
                value -> {
                    try {
                        Log.d(TAG, "getFollowersList sonucu başlangıcı: " +
                                (value != null && value.length() > 50 ? value.substring(0, 50) + "..." : value));

                        followersList = parseFollowerData(value);

                        if (followersList.size() < 1) {
                            webView.evaluateJavascript(
                                    "document.querySelectorAll('div.x1dm5mii.x16mil14.xiojian.x1yutycm').length;",
                                    checkResult -> {
                                        Log.d(TAG, "Takipçi kontrol sonucu: " + checkResult);
                                        int count = 0;
                                        try {
                                            if (checkResult != null) { count = Integer.parseInt(checkResult);
                                            }
                                        } catch (NumberFormatException e) {
                                            Log.e(TAG, "Sayı dönüşüm hatası: " + e.getMessage());
                                        }

                                        if (count == 0) {
                                            Toast.makeText(MainActivity.this, "Takipçiler yüklenemedi, yeniden deneniyor...", Toast.LENGTH_SHORT).show();
                                            tvLoading.setVisibility(View.VISIBLE);
                                            handler.postDelayed(this::clickOnFollowers, 3000);
                                        } else {
                                            Toast.makeText(MainActivity.this, "Takipçi verisi alınamadı, tekrar deneniyor...", Toast.LENGTH_SHORT).show();
                                            tvLoading.setVisibility(View.VISIBLE);
                                            handler.postDelayed(this::getFollowersList, 3000);
                                        }
                                    }
                            );
                            return;
                        }

                        Toast.makeText(MainActivity.this, followersList.size() + " takipçi bulundu", Toast.LENGTH_SHORT).show();
                        profileInfo.setFollowersCount(followersList.size());

                        // Takipçi listesini kaydet
                        saveFollowersList();

                        // Karşılaştırmalar yapıp kaydet
                        calculateAndSaveComparisons();

                        // Dashboard'a yönlendir
                        launchDashboard();

                        tvStatus.setText("İşlem tamamlandı. Sonuçlar yeni sayfada gösteriliyor.");
                        progressBar.setVisibility(View.GONE);
                        btnExtractFollowers.setEnabled(true);

                    } catch (Exception e) {
                        Log.e(TAG, "Takipçi verisi işleme hatası: " + e.getMessage(), e);
                        Toast.makeText(MainActivity.this, "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        tvStatus.setText("Hata oluştu.");
                        progressBar.setVisibility(View.GONE);
                        btnExtractFollowers.setEnabled(true);
                    }
                });
    }

    private void saveFollowersList() {
        DataManager.getInstance(this).saveFollowersList(followersList);
    }

    private void calculateAndSaveComparisons() {
        // Takip etmeyenler (siz takip ediyorsunuz ama onlar sizi takip etmiyor)
        ArrayList<Follower> notFollowingBack = new ArrayList<>();
        for (Follower following : followingList) {
            boolean isFollowingBack = false;
            for (Follower follower : followersList) {
                if (following.getUsername().equals(follower.getUsername())) {
                    isFollowingBack = true;
                    break;
                }
            }
            if (!isFollowingBack) {
                notFollowingBack.add(following);
            }
        }

        // Takip etmedikleriniz (onlar sizi takip ediyor ama siz onları takip etmiyorsunuz)
        ArrayList<Follower> notFollowing = new ArrayList<>();
        for (Follower follower : followersList) {
            boolean isFollowing = false;
            for (Follower following : followingList) {
                if (follower.getUsername().equals(following.getUsername())) {
                    isFollowing = true;
                    break;
                }
            }
            if (!isFollowing) {
                notFollowing.add(follower);
            }
        }

        // Sonuçları kaydet
        DataManager.getInstance(this).saveNotFollowingBackList(notFollowingBack);
        DataManager.getInstance(this).saveNotFollowingList(notFollowing);

        // Engelleyenler listesi (takip etmeyenlerden rastgele)
        ArrayList<Follower> blockers = new ArrayList<>();
        if (!notFollowingBack.isEmpty()) {
            int blockerCount = Math.min(notFollowingBack.size(), 5); // En fazla 5 kullanıcı seç
            for (int i = 0; i < blockerCount; i++) {
                blockers.add(notFollowingBack.get(i));
            }
        }
        DataManager.getInstance(this).saveBlockersList(blockers); // PRO olmayan kullanıcılar için boş

        // Gizli hayranlar (sizi takip ediyor ama siz takip etmiyorsunuz)
        ArrayList<Follower> secretAdmirers = new ArrayList<>();
        if (!notFollowing.isEmpty()) {
            int admirerCount = Math.min(notFollowing.size(), 8); // En fazla 8 kullanıcı seç
            for (int i = 0; i < admirerCount; i++) {
                secretAdmirers.add(notFollowing.get(i));
            }
        }
        DataManager.getInstance(this).saveSecretAdmirersList(secretAdmirers); // PRO olmayan kullanıcılar için boş

        // Kaybedilen/kazanılan takipçiler kontrolü
        checkForFollowerChanges();
    }

    private void checkForFollowerChanges() {
        // Önceki takipçi listesini al
        ArrayList<Follower> previousFollowers = DataManager.getInstance(this).getPreviousFollowersList();

        if (previousFollowers != null && !previousFollowers.isEmpty()) {
            // Kaybedilen takipçiler (önceden takip ediyordu şimdi etmiyor)
            ArrayList<Follower> lostFollowers = new ArrayList<>();
            for (Follower prevFollower : previousFollowers) {
                boolean stillFollowing = false;
                for (Follower currentFollower : followersList) {
                    if (prevFollower.getUsername().equals(currentFollower.getUsername())) {
                        stillFollowing = true;
                        break;
                    }
                }
                if (!stillFollowing) {
                    lostFollowers.add(prevFollower);
                }
            }

            // Yeni takipçiler (şimdi takip ediyor ama önceden etmiyordu)
            ArrayList<Follower> newFollowers = new ArrayList<>();
            for (Follower currentFollower : followersList) {
                boolean wasFollowingBefore = false;
                for (Follower prevFollower : previousFollowers) {
                    if (currentFollower.getUsername().equals(prevFollower.getUsername())) {
                        wasFollowingBefore = true;
                        break;
                    }
                }
                if (!wasFollowingBefore) {
                    newFollowers.add(currentFollower);
                }
            }

            // Kaybedilen ve yeni takipçileri kaydet
            DataManager.getInstance(this).saveLostFollowersList(lostFollowers);
            DataManager.getInstance(this).saveNewFollowersList(newFollowers);
        }

        // Şimdiki takipçileri sonraki karşılaştırma için kaydediyoruz
        DataManager.getInstance(this).updatePreviousFollowersList(followersList);
    }

    private void launchDashboard() {
        // Hikayeleri kaydet
        DataManager.getInstance(this).saveStoriesList(stories);

        // Profil ziyaretçileri oluştur (ilk 3'ü görünür, geri kalanı PRO için)
        DataManager.getInstance(this).generateProfileVisitors();

        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);

        // Kullanıcı daha sonra veriyi yenilemek isteyebileceği için MainActivity'yi kapatmıyoruz
    }

    private ArrayList<Follower> parseFollowerData(String jsonData) {
        ArrayList<Follower> followers = new ArrayList<>();
        try {
            if (jsonData == null || jsonData.isEmpty()) {
                Log.e(TAG, "JSON verisi boş");
                return followers;
            }

            String cleanJson = jsonData;
            if (cleanJson.startsWith("\"") && cleanJson.endsWith("\"")) {
                cleanJson = cleanJson.substring(1, cleanJson.length() - 1)
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                        .replace("\\n", "\n");
            }

            JSONArray array = new JSONArray(cleanJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String username = obj.optString("username");
                String displayName = obj.optString("displayName");
                String profilePicURL = obj.optString("profilePicURL");
                boolean isFollowingYou = obj.optBoolean("isFollowingYou", false);

                if (!username.isEmpty() && !displayName.isEmpty() && !profilePicURL.isEmpty()) {
                    Follower follower = new Follower(username, displayName, profilePicURL);
                    follower.setFollowingYou(isFollowingYou);
                    followers.add(follower);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "JSON Parse Error: " + e.getMessage(), e);
            Log.e(TAG, "JSON Data: " + (jsonData != null ? jsonData.substring(0, Math.min(100, jsonData.length())) + "..." : "null"));
            e.printStackTrace();
        }
        return followers;
    }

    private class MyJavaScriptInterface {
        @JavascriptInterface
        public void onStoriesExtracted(String json) {
            runOnUiThread(() -> {
                try {
                    Log.d(TAG, "onStoriesExtracted JSON: " + json);

                    if (json == null || json.trim().isEmpty() || json.equals("[]") || json.equals("{}")) {
                        Log.w(TAG, "Hikaye verisi boş veya null: " + json);
                        retryProcessStories();
                        return;
                    }

                    stories.clear();
                    JSONArray storyArray = new JSONArray(json);
                    Log.d(TAG, "JSON array boyutu: " + storyArray.length());

                    if (storyArray.length() == 0) {
                        Log.w(TAG, "Hiç hikaye bulunamadı!");
                        retryProcessStories();
                        return;
                    }

                    for (int i = 0; i < storyArray.length(); i++) {
                        JSONObject storyObj = storyArray.getJSONObject(i);
                        String username = storyObj.optString("username", "");
                        String profileImg = storyObj.optString("profileImg", "");
                        String mediaUrl = storyObj.optString("mediaUrl", "");
                        boolean isVideo = storyObj.optBoolean("isVideo", false);

                        Log.d(TAG, String.format("JSON Story %d: username=%s, isVideo=%b", i + 1, username, isVideo));
                        Log.d(TAG, "  Profile Image URL: " + profileImg);
                        Log.d(TAG, "  Media URL: " + mediaUrl);

                        if (!username.isEmpty() && !profileImg.isEmpty() && !mediaUrl.isEmpty()) {
                            Story story = new Story(username, profileImg, mediaUrl, isVideo);
                            stories.add(story);
                            Log.d(TAG, "  -> Story başarıyla eklendi: " + story.toString());
                        } else {
                            Log.e(TAG, "  -> Bazı alanlar boş, story eklenemedi: username=" + username +
                                    ", profileImg=" + profileImg + ", mediaUrl=" + mediaUrl);
                        }
                    }

                    Log.d(TAG, "Toplam hikaye sayısı (onStoriesExtracted sonunda): " + stories.size());
                    for (int i = 0; i < stories.size(); i++) {
                        Log.d(TAG, "Hikaye " + (i + 1) + ": " + stories.get(i).toString());
                    }

                    storyRetryCount = 0;
                    handler.postDelayed(() -> {
                        tvStatus.setText("Hikayeler işlendi (" + stories.size() + "). Profil sayfasına gidiliyor...");
                        goToProfile();
                    }, 2000);

                } catch (Exception e) {
                    Log.e(TAG, "onStoriesExtracted hatası: " + e.getMessage(), e);
                    e.printStackTrace();
                    retryProcessStories();
                }
            });
        }

        @JavascriptInterface
        public void onFollowingScrollComplete(String result) {
            runOnUiThread(() -> {
                Log.d(TAG, "Following kaydırma tamamlandı: " + result);
                tvStatus.setText("Following listesi kaydırma tamamlandı, veri alınıyor...");
                handler.postDelayed(MainActivity.this::extractFollowing, 1000);
            });
        }

        @JavascriptInterface
        public void onFollowersScrollComplete(String result) {
            runOnUiThread(() -> {
                Log.d(TAG, "Followers kaydırma tamamlandı: " + result);
                tvStatus.setText("Takipçi listesi kaydırma tamamlandı, veri alınıyor...");
                handler.postDelayed(MainActivity.this::getFollowersList, 1000);
            });
        }

        @JavascriptInterface
        public void onFollowersExtracted(String json) {
            runOnUiThread(() -> {
                try {
                    ArrayList<Follower> followers = new ArrayList<>();
                    JSONArray array = new JSONArray(json);

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String username = obj.optString("username");
                        String displayName = obj.optString("displayName");
                        String profilePicURL = obj.optString("profilePicURL");
                        boolean isFollowingYou = obj.optBoolean("isFollowingYou", false);

                        Follower follower = new Follower(username, displayName, profilePicURL);
                        follower.setFollowingYou(isFollowingYou);
                        followers.add(follower);
                    }

                    followersList = followers;
                    tvLoading.setVisibility(View.VISIBLE);
                    calculateAndSaveComparisons();
                    launchDashboard();

                    tvStatus.setText("Takipçiler başarıyla yüklendi.");
                    progressBar.setVisibility(View.GONE);
                    btnExtractFollowers.setEnabled(true);

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("Hata oluştu.");
                    btnExtractFollowers.setEnabled(true);
                }
            });
        }
    }
}