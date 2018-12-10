import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.apache.commons.lang.StringEscapeUtils.unescapeXml;

public class FlashcardsCreator {
    JSONmodel jsonModel = new JSONmodel();

    String getJsonContent(APIparameters apiParameters, WebsiteProvider websiteProvider) {
        /*  To get data visit /gapi/{functionName}[?[{functionParameter1}={value}
             [&{functionParameter2}={value}[&{functionParameter3}={value}...]]]] page.
             Example: Translate Polish 'witaj' into English, output format is json:
             https://glosbe.com/gapi/translate?from=pol&dest=eng&format=json&phrase=witaj&pretty=true */

        String jsonURL = "https://www.glosbe.com/gapi/translate?from=" + apiParameters.getFrom() + "&dest=" +
                apiParameters.getDest() + "&format=json&phrase=" + apiParameters.getPhraseToTranslate() + "&pretty=true";
        System.out.println("\nURL: " + jsonURL + "\n"); // only for debugging
        return websiteProvider.getUrlContents(jsonURL);
    }

    void printFlashcards(APIparameters apiParameters, CommunicationWithUser communicationWithUser) {

        while (true) {
                getDataFromJSON(communicationWithUser, apiParameters);
                System.out.println("\n------------------------------------\n\n FRONT SIDE:\n");
                System.out.println("\t" + apiParameters.getPhraseToTranslate()); // print source phrase
                System.out.println("\n\n BACK SIDE:\n\n");
                System.out.println("\t" + jsonModel.getTranslation()); //print translation
                //for (enMeaningIndex = 0; !(tuc.get(0).get("meanings").get(enMeaningIndex).get("language").asText().equals("en")); enMeaningIndex++) {
                //   System.out.println("loop pass" + enMeaningIndex);
                //}

                int enMeaningIndex = 0;
                //If translating phrase is not in polish, print meaning of this phrase
                if (!(apiParameters.getFrom().equals("pol"))) {
                    //todo: It sometimes prints meaning in polish e.g. choosing eng->pol and typing 'digit'.
                    System.out.println("meaning: " + jsonModel.getMeaning());
                }


                System.out.println("\n----------------------------------");

        }

    }
    void getDataFromJSON(CommunicationWithUser communicationWithUser, APIparameters apiParameters) {
        communicationWithUser.userSetPhraseToTranslate();
        WebsiteProvider websiteProvider = new WebsiteProvider();
        ObjectMapper translationMapper = new ObjectMapper();
        translationMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {

            JsonNode root = translationMapper.readTree(getJsonContent(apiParameters, websiteProvider));

            JsonNode tuc = root.get("tuc"); // get() calls specific value in the array /tuc/0/meanings/0/text
            JsonNode phrase = tuc.get(0).get("phrase");

            jsonModel.setTranslation(phrase.get("text").asText());

            int enMeaningIndex = 0;
            //for (enMeaningIndex = 0; !(tuc.get(0).get("meanings").get(enMeaningIndex).get("language").asText().equals("en")); enMeaningIndex++) {
            //   System.out.println("loop pass" + enMeaningIndex);
            //}

            //If translating phrase is not in polish, print meaning of this phrase
            jsonModel.setMeaning(unescapeXml(tuc.get(0).get("meanings").get(enMeaningIndex).get("text").asText())); // skips entities like '&quot'


        }
        catch (IOException e) {
        e.printStackTrace();
    }


    }
}