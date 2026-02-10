package com.example.amit_mitzmacher_tal_mitzmacher.data.utils;

import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import java.util.Locale;

public class TranslationHelper {

    public interface TranslationCallback {
        void onTranslationComplete(String translatedText);
    }

    // בדיקה האם המכשיר בעברית
    public static boolean isDeviceInHebrew() {
        String lang = Locale.getDefault().getLanguage();
        return lang.equals("iw") || lang.equals("he");
    }

    /**
     * פונקציית תרגום חכמה:
     * מזהה את שפת המקור ומתרגמת לשפת המכשיר (עברית או אנגלית).
     */
    public static void translate(String text, TranslationCallback callback) {
        if (text == null || text.trim().isEmpty()) {
            callback.onTranslationComplete("");
            return;
        }

        // זיהוי שפת הטקסט שנשלח
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(languageCode -> {
                    String targetLang = isDeviceInHebrew() ? TranslateLanguage.HEBREW : TranslateLanguage.ENGLISH;

                    // אם השפה המזוהה כבר זהה לשפת היעד, אין צורך בתרגום
                    if (languageCode.equals(targetLang)) {
                        callback.onTranslationComplete(text);
                    } else {
                        // אם הטקסט בעברית והיעד אנגלית, או להפך - נבצע תרגום
                        String sourceLang = targetLang.equals(TranslateLanguage.ENGLISH) ?
                                TranslateLanguage.HEBREW : TranslateLanguage.ENGLISH;
                        performTranslation(text, sourceLang, targetLang, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    // במקרה של כישלון בזיהוי, ננסה תרגום ברירת מחדל לפי שפת המכשיר
                    String source = isDeviceInHebrew() ? TranslateLanguage.ENGLISH : TranslateLanguage.HEBREW;
                    String target = isDeviceInHebrew() ? TranslateLanguage.HEBREW : TranslateLanguage.ENGLISH;
                    performTranslation(text, source, target, callback);
                });
    }

    /**
     * תרגום ספציפי לאנגלית (משמש לחיפוש ב-API)
     */
    public static void translateToEnglish(String text, TranslationCallback callback) {
        if (text == null || text.trim().isEmpty()) {
            callback.onTranslationComplete("");
            return;
        }

        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(languageCode -> {
                    if (languageCode.equals("en")) {
                        callback.onTranslationComplete(text);
                    } else {
                        performTranslation(text, TranslateLanguage.HEBREW, TranslateLanguage.ENGLISH, callback);
                    }
                })
                .addOnFailureListener(e -> performTranslation(text, TranslateLanguage.HEBREW, TranslateLanguage.ENGLISH, callback));
    }

    private static void performTranslation(String text, String sourceLang, String targetLang, TranslationCallback callback) {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build();

        final Translator translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> {
                    translator.translate(text)
                            .addOnSuccessListener(callback::onTranslationComplete)
                            .addOnFailureListener(e -> callback.onTranslationComplete(text));
                })
                .addOnFailureListener(e -> callback.onTranslationComplete(text));
    }
}