package amosalexa.services.blockcard;

import amosalexa.AmosAlexaSpeechlet;
import amosalexa.SpeechletSubject;
import amosalexa.services.SpeechService;
import api.banking.AccountAPI;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import model.banking.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockCardService implements SpeechService {

    private static final Logger log = LoggerFactory.getLogger(BlockCardService.class);

    /**
     *
     */
    private static final String number = "0000000001";

    private static final String CONTEXT = "DIALOG_CONTEXT";
    private static final String BLOCK_CARD_DIALOG = "BlockCardService";

    public BlockCardService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    /**
     * ties the Speechlet Subject (Amos Alexa Speechlet) with an Speechlet Observer
     *
     * @param speechletSubject service
     */
    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        speechletSubject.attachSpeechletObserver(this, "BlockCardIntent");
        speechletSubject.attachSpeechletObserver(this, "AMAZON.YesIntent");
        speechletSubject.attachSpeechletObserver(this, "AMAZON.NoIntent");
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();

        String dialogContext = (String) session.getAttribute(CONTEXT);

        if( (dialogContext == null && !request.getIntent().getName().equals("BlockCardIntent")) ||
                (dialogContext != null && !dialogContext.equals(BLOCK_CARD_DIALOG))) {
            return null;
        }

        // TODO: Use account later to actually block a card
        Account account = AccountAPI.getAccount(number);

        if (request.getIntent().getName().equals("AMAZON.YesIntent")) {
            String cardNumberObj = (String) session.getAttribute("BlockCardService.CardNumber");

            if (cardNumberObj != null) {
                long cardNumber = Long.parseLong(cardNumberObj);

                // TODO: Lock card with number cardNumber

                return AmosAlexaSpeechlet.getSpeechletResponse("Karte " + cardNumberObj + " wurde gesperrt.", "", false);
            }

            return null;
        } else if (request.getIntent().getName().equals("AMAZON.NoIntent")) {
            session.setAttribute("BlockCardService.CardNumber", null);
            return AmosAlexaSpeechlet.getSpeechletResponse("Okay, tschüss.", "", false);
        } else if (request.getIntent().getName().equals("BlockCardIntent")) {
            session.setAttribute(CONTEXT, BLOCK_CARD_DIALOG);
            String bankCardNumber = request.getIntent().getSlot("BankCardNumber").getValue();

            if (bankCardNumber == null) {
                String speechText = "Wie lautet die Nummber der Karte?";
                String repromptText = "Sagen Sie auch die Nummer der Karte. Zum Beispiel: Sperre Karte 12345.";

                return AmosAlexaSpeechlet.getSpeechletResponse(speechText, repromptText, false);
            } else {
                session.setAttribute("BlockCardService.CardNumber", bankCardNumber);

                String speechText = "Möchten Sie die Karte " + bankCardNumber + " wirklich sperren?";
                String repromptText = "Bitte bestätigen Sie, indem Sie 'ja' sagen.";

                return AmosAlexaSpeechlet.getSpeechletResponse(speechText, repromptText, true);
            }
        }

        return null;
    }
}
