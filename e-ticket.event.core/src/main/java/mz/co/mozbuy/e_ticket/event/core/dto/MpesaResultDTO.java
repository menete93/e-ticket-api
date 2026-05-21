package mz.co.mozbuy.e_ticket.event.core.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpesaResultDTO {

    @JsonProperty("output_ResponseCode")
    private String responseCode;

    @JsonProperty("output_ResponseDesc")
    private String responseDescription;

    @JsonProperty("output_TransactionID")
    private String transactionID;

    @JsonProperty("output_ConversationID")
    private String conversationID;

    @JsonProperty("output_ThirdPartyReference")
    private String thirdPartyReference;

    @JsonIgnore
    private Integer statusCode;

    @JsonIgnore
    private String checkoutRequestId;

    public boolean isSuccessful() {
        Integer code = this.getStatusCode();
        return code != null && (code == 200 || code == 201);
    }

    public boolean isSuccessResponse() {
        return "0".equals(responseCode) || "200".equals(responseCode);
    }
}