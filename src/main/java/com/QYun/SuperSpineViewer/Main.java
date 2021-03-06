package com.QYun.SuperSpineViewer;

import com.QYun.SuperSpineViewer.Controller.Launcher;
import com.QYun.SuperSpineViewer.Controller.Spine;
import com.jfoenix.controls.JFXProgressBar;
import javafx.application.Application;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Main {
    public static RecordFX recordFX;
    public static Spine spineController;
    public static JFXProgressBar progressBar;
    public static ImageView spineRender;
    public static Label FPS;
    public static Label Skel;
    public static Label Atlas;
    public static int width;
    public static int height;
    public static byte perform = 6;
    public static byte sequence = 0;
    public static float quality = 0.5f;
    public static boolean isLoad = false;
    public static boolean preA = true;
    public static boolean recording = false;
    public static String openPath = null;
    public static Preferences Pref = Preferences.userRoot().node("/com/QYun/SuperSpineViewer");
    public static String outPath = Pref.get("lastSave", System.getProperty("user.home"));

    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("reset")) {
                try {
                    Pref.clear();
                } catch (BackingStoreException ignored) {
                }
            } else openPath = args[0];
        }
        Application.launch(Launcher.class, args);
    }
}
