package com.gesslar.threshvote;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Switch;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * Created by gesslar on 2016-03-05.
 */
public class SettingsActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                .beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();

        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();
    }

    public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private Preference mPrefEthos = null;
        private Preference mPrefName = null;
        private Preference mPrefSound = null;
        private SwitchPreference mPrefSoundEnabled = null;
        SoundPool soundPool;
        Integer sndLight, sndMedium, sndDark;
        boolean oldSounds = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
        AudioAttributes audioAttributes;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.threshvoteprefs);

            String mEthos, mName, mSound = "";
            Boolean mSoundEnabled;

            mPrefName = findPreference(getString(R.string.pref_key_character_name));
            mName = getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_character_name), "");
            mPrefName.setSummary(mName);

            mPrefEthos = findPreference(getString(R.string.pref_key_ethos));
            mEthos = getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_ethos), getString(R.string.pref_ethos_default));
            mEthos = TranslateListItem(mEthos, R.array.pref_values_ethos, R.array.title_pref_ethos);
            mPrefEthos.setSummary(mEthos);

            mPrefSoundEnabled = (SwitchPreference) findPreference(getString(R.string.pref_key_enable_notification_sound));
            mSoundEnabled = getPreferenceManager().getSharedPreferences().getBoolean(getString(R.string.pref_key_enable_notification_sound), getResources().getBoolean(R.bool.default_enable_sound));
            mPrefSoundEnabled.setChecked(mSoundEnabled);

            mPrefSound = findPreference(getString(R.string.pref_key_notification_sound));
            mSound = getPreferenceManager().getSharedPreferences().getString(getString(R.string.pref_key_notification_sound), getString(R.string.pref_sound_option_default));
            mSound = TranslateListItem(mSound, R.array.pref_values_sound, R.array.title_pref_sound);
            mPrefSound.setSummary(mSound);

            if(oldSounds)
            {
                soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
                sndLight = soundPool.load(getActivity(), R.raw.notification_light, 1);
                sndMedium = soundPool.load(getActivity(), R.raw.notification_light, 1);
                sndDark = soundPool.load(getActivity(), R.raw.notification_light, 1);
            }
            else
            {

                audioAttributes = new AudioAttributes.Builder()
                        .setContentType(audioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build();

                soundPool = new SoundPool.Builder()
                        .setMaxStreams(1)
                        .setAudioAttributes(audioAttributes)
                        .build();


                sndLight = soundPool.load(getActivity(), R.raw.notification_light, 1);
                sndMedium = soundPool.load(getActivity(), R.raw.notification_medium, 1);
                sndDark = soundPool.load(getActivity(), R.raw.notification_dark, 1);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            ThreshVoteIntentService.mActive++;
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            long yourmilliseconds = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.CANADA);
            Date resultdate = new Date(yourmilliseconds);
            Log.d("Settings", "SHOWN: " + ThreshVoteIntentService.mIPAddress + "' - " + resultdate.toString());
        }

        @Override
        public void onPause() {
            ThreshVoteIntentService.mActive--;
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

            long yourmilliseconds = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm", Locale.CANADA);
            Date resultdate = new Date(yourmilliseconds);
            Log.d("Settings", "HIDDEN: " + ThreshVoteIntentService.mIPAddress + "' - " + resultdate.toString());


            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d("PrefsChanged", "KEY: " + key);
            if(key.equals(getString(R.string.pref_key_character_name))) {
                String name = sharedPreferences.getString(key, "");
                mPrefName.setSummary(name);
                ThreshVoteIntentService.startActionLoadCharacterName(getActivity());
            } else if(key.equals(getString(R.string.pref_key_ethos))) {
                String defaultEthos = getString(R.string.pref_ethos_default);
                String ethos = sharedPreferences.getString(key, defaultEthos);
                ethos = TranslateListItem(ethos, R.array.pref_values_ethos, R.array.title_pref_ethos);
                mPrefEthos.setSummary(ethos);
            } else if(key.equals(getString(R.string.pref_key_notification_sound))) {
                String defaultSound = getString(R.string.pref_sound_option_default);
                String sound = sharedPreferences.getString(key, defaultSound);

                if(sound.equals(getString(R.string.pref_sound_option_light))) {
                    soundPool.play(sndLight, 0.9f, 0.9f, 1, 0, 1);
                } else if(sound.equals(getString(R.string.pref_sound_option_medium))) {
                    soundPool.play(sndMedium, 0.9f, 0.9f, 1, 0, 1);
                } else if(sound.equals(getString(R.string.pref_sound_option_dark))) {
                    soundPool.play(sndDark, 0.9f, 0.9f, 1, 0, 1);
                }

                sound = TranslateListItem(sound, R.array.pref_values_sound, R.array.title_pref_sound);
                mPrefSound.setSummary(sound);
            } else if(key.equals(getString(R.string.pref_key_enable_notification_sound))) {
                ThreshVoteIntentService.startActionDetermineSoundEnabled(getActivity());
            } else if(key.equals(getString(R.string.pref_key_opportune_mode))) {
                ThreshVoteIntentService.startActionDetermineOpportune(getActivity());
            }
        }

        private String TranslateListItem(String item, Integer FromListID, Integer ToListID) {
            String[] From = getResources().getStringArray(FromListID);
            String[] To = getResources().getStringArray(ToListID);

            Integer pos = Arrays.asList(From).indexOf(item);
            return To[pos];
        }
    }
}
