package amosalexa.services.bankaccount;


import amosalexa.SessionStorage;
import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BalanceLimitService extends AbstractSpeechService implements SpeechService {

	@Override
	public String getDialogName() {
		return this.getClass().getName();
	}

	@Override
	public List<String> getStartIntents() {
		return Arrays.asList(
				SET_BALANCE_LIMIT_INTENT
		);
	}

	@Override
	public List<String> getHandledIntents() {
		return Arrays.asList(
				SET_BALANCE_LIMIT_INTENT,
				YES_INTENT,
				NO_INTENT
		);
	}

	private static final String SET_BALANCE_LIMIT_INTENT = "SetBalanceLimitIntent";
	private static final String CARD_TITLE = "Kontolimit";
	private static final String NEW_BALANCE_LIMIT = "NewBalanceLimit";

	public BalanceLimitService(SpeechletSubject speechletSubject) {
		subscribe(speechletSubject);
	}

	@Override
	public void subscribe(SpeechletSubject speechletSubject) {
		for(String intent : getHandledIntents()) {
			speechletSubject.attachSpeechletObserver(this, intent);
		}
	}

	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
		Intent intent = requestEnvelope.getRequest().getIntent();
		Session session = requestEnvelope.getSession();

		SessionStorage.Storage sessionStorage = SessionStorage.getInstance().getStorage(session.getSessionId());

		if(intent.getName().equals(SET_BALANCE_LIMIT_INTENT)) {
			Map<String, Slot> slots = intent.getSlots();
			Slot balanceLimitAmountSlot = slots.get("BalanceLimitAmount");

			if(balanceLimitAmountSlot == null || balanceLimitAmountSlot.getValue() == null) {
				// TODO: This interferes with the SavingsPlanService
				return getAskResponse(CARD_TITLE, "Auf welchen Betrag möchtest du dein Kontolimit setzen?");
				//return getErrorResponse();
			}

			String balanceLimitAmount = balanceLimitAmountSlot.getValue();

			if(balanceLimitAmount.equals("?")) {
				return getErrorResponse("Der angegebene Betrag ist ungültig.");
			}

			sessionStorage.put(NEW_BALANCE_LIMIT, balanceLimitAmount);
			return getBalanceLimitAskResponse(balanceLimitAmount);

		} else if(intent.getName().equals(YES_INTENT)) {
			if(!sessionStorage.containsKey(NEW_BALANCE_LIMIT)) {
				return getErrorResponse();
			}
			return setBalanceLimit((String)sessionStorage.get(NEW_BALANCE_LIMIT));
		} else if(intent.getName().equals(NO_INTENT)) {
			return getResponse(CARD_TITLE, "");
		}

		return null;
	}

	private SpeechletResponse getBalanceLimitAskResponse(String balanceLimitAmount) {
		return getAskResponse(CARD_TITLE, "Möchtest du dein Kontolimit wirklich auf " + balanceLimitAmount + " Euro setzen?");
	}

	private SpeechletResponse setBalanceLimit(String balanceLimitAmount) {
		// TODO: Set new limit
		return getResponse(CARD_TITLE, "Okay, dein Kontolimit wurde auf " + balanceLimitAmount + " Euro gesetzt.");
	}

}
