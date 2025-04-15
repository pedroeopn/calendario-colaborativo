// InterstitialAdManager.java
package com.pedroeopn.calendariocolaborativo;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;

public class InterstitialAdManager {
    private InterstitialAd interstitialAd;
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long AD_INTERVAL = 15000; // 1 minuto

    public InterstitialAdManager(Context context) {
        this.context = context;
        loadAd();
        scheduleAd((Activity) context);
    }

    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        // Substitua "SEU_AD_UNIT_ID" pelo ID de unidade de anúncio intersticial obtido no AdMob
        InterstitialAd.load(context, context.getString(R.string.interstitial_ad_unit_id),
                adRequest, new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        interstitialAd = null;
                    }
                });
    }

    private void showAd(Activity activity) {
        if(interstitialAd != null) {
            interstitialAd.show(activity);
        } else {
            // Se o anúncio não estiver pronto, tente recarregar
            loadAd();
        }
    }

    // Agenda a exibição do anúncio a cada 1 minuto
    private void scheduleAd(final Activity activity) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showAd(activity);
                // Após exibir, recarrega o anúncio para exibição futura
                loadAd();
                // Agenda novamente para daqui a 1 minuto
                scheduleAd(activity);
            }
        }, AD_INTERVAL);
    }
}