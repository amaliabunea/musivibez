package backend.ml;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.*;

import java.util.List;

public class NLU {
    private NaturalLanguageUnderstanding service;

    public NLU() {
        service = new NaturalLanguageUnderstanding(
                "2018-03-16",
                "apikey",
                "4WQiF0OVC6HhWdjecz9HIBGABuzAQt4VR8B7beedCn3U"
        );
        service.setEndPoint("https://gateway-syd.watsonplatform.net/natural-language-understanding/api");
    }

    public List<KeywordsResult> getKeywords(String text) {

        KeywordsOptions keywordsOptions = new KeywordsOptions.Builder()
                .emotion(true)
                .sentiment(true)
                .build();

        Features features = new Features.Builder()
                .keywords(keywordsOptions)
                .build();

        AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                .text(text)
                .features(features)
                .build();

        AnalysisResults response = service
                .analyze(parameters)
                .execute();

        return response.getKeywords();
    }
    public String getEmotions(String text) {
        EmotionOptions emotionOptions = new EmotionOptions.Builder()
                .build();
        EntitiesOptions entitiesOptions = new EntitiesOptions.Builder()
                .emotion(true)
                .sentiment(true)
                .build();

        Features features = new Features.Builder()
                .entities(entitiesOptions)
                .emotion(emotionOptions)
                .build();

        AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                .text(text)
                .features(features)
                .build();

        AnalysisResults response = service
                .analyze(parameters)
                .execute();
        return response.getEmotion().getDocument().getEmotion().toString();
    }
}
