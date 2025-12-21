package com.saicon.games.callcard.ws.data;

import com.saicon.games.callcard.ws.dto.CallCardStatsDTO;
import com.saicon.games.callcard.ws.response.ResponseStatus;
import com.saicon.games.callcard.ws.response.WSResponse;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Response wrapper for CallCardStatsDTO
 * Used in SOAP service responses
 */
@XmlRootElement(name = "ResponseCallCardStats")
public class ResponseCallCardStats extends WSResponse {

    private CallCardStatsDTO stats;

    public ResponseCallCardStats() {
        super();
    }

    public ResponseCallCardStats(String errorNumber, ResponseStatus status, CallCardStatsDTO stats) {
        super(errorNumber, status);
        this.stats = stats;
    }

    public ResponseCallCardStats(String errorNumber, String result, ResponseStatus status, CallCardStatsDTO stats) {
        super(errorNumber, result, status);
        this.stats = stats;
    }

    public CallCardStatsDTO getStats() {
        return stats;
    }

    public void setStats(CallCardStatsDTO stats) {
        this.stats = stats;
    }
}
