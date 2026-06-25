package mz.co.mozbuy.e_ticket.event.core.service.mpesa;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fc.sdk.APIContext;
import com.fc.sdk.APIMethodType;
import com.fc.sdk.APIRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mz.co.mozbuy.e_ticket.event.core.exceptions.BusinessException;
import mz.co.mozbuy.e_ticket.event.core.exceptions.SystemException;
import mz.co.mozbuy.e_ticket.event.core.integ.dto.MpesaResultDTO;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentProviderConfigEntity;
import mz.co.mozbuy.e_ticket.event.core.model.PaymentTransactionEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpesaIntegrationService {

    private final ObjectMapper objectMapper;

    /**
     * Envia requisição para API do M-Pesa
     */
    public MpesaResultDTO sendToMPesa(String customerMSISDN,
                                      PaymentTransactionEntity paymentTransaction,
                                      PaymentProviderConfigEntity providerApiConfig) {
        try {
            log.info("📤 Enviando requisição para M-Pesa - Transação: {}, Telefone: {}",
                    paymentTransaction.getReservationCode(), customerMSISDN);

            final var context = new APIContext();
            context.setApiKey(providerApiConfig.getApiKey());
            context.setPublicKey(providerApiConfig.getApiSecret());
            context.setSsl(true);
            context.setMethodType(APIMethodType.POST);
            context.setAddress(providerApiConfig.getBaseUrl());
            context.setPort(Integer.parseInt(providerApiConfig.getPort()));
            context.setPath(providerApiConfig.getPath());
            context.addHeader("Origin", "*");

            // Parâmetros da requisição M-Pesa
            context.addParameter("input_TransactionReference", paymentTransaction.getReservationCode());
            String msisdn = normalizeMsisdn(customerMSISDN);
            context.addParameter("input_CustomerMSISDN", msisdn);
            context.addParameter("input_Amount", String.valueOf(paymentTransaction.getAmount()));
            context.addParameter("input_ThirdPartyReference", paymentTransaction.getReservationCode());
            context.addParameter("input_ServiceProviderCode", providerApiConfig.getPartnerCode());

            final var request = new APIRequest(context);
            final var response = request.execute();

            log.info("📥 Resposta recebida do M-Pesa - Status Code: {}", response.getStatusCode());

            final var result = objectMapper.readValue(response.getResult(), MpesaResultDTO.class);

            if (result == null) {
                throw new BusinessException("MPESA_EMPTY_RESULT", "Resposta vazia do M-Pesa");
            }

            result.setStatusCode(response.getStatusCode());

            log.info("✅ M-Pesa Response - Code: {}, Description: {}",
                    result.getResponseCode(), result.getResponseDescription());

            return result;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Erro na integração com M-Pesa: {}", e.getMessage(), e);
            throw new SystemException("UNSUCCESSFULL_OPERATION", e.getMessage(), e, "MPESA");
        }
    }



    private String normalizeMsisdn(String msisdn) {
        if (msisdn == null) {
            throw new BusinessException("INVALID_PHONE", "Número inválido");
        }

        msisdn = msisdn.trim();
        msisdn = msisdn.replaceAll("\\D", "");

        if (msisdn.startsWith("258")) {
            return msisdn;
        }

        return "258" + msisdn;
    }

    /**
     * Consulta status da transação no M-Pesa
     */
    public MpesaResultDTO queryTransactionStatus(String transactionId,
                                                 PaymentProviderConfigEntity providerApiConfig) {
        try {
            log.info("🔍 Consultando status da transação M-Pesa: {}", transactionId);

            final var context = new APIContext();
            context.setApiKey(providerApiConfig.getApiKey());
            context.setPublicKey(providerApiConfig.getApiSecret());
            context.setSsl(true);
            context.setMethodType(APIMethodType.POST);
            context.setAddress(providerApiConfig.getBaseUrl());
            context.setPort(Integer.parseInt(providerApiConfig.getPort()));
            context.setPath(providerApiConfig.getPath());
            context.addHeader("Origin", "*");

            context.addParameter("input_TransactionReference", transactionId);
            context.addParameter("input_ServiceProviderCode", providerApiConfig.getPartnerCode());

            final var request = new APIRequest(context);
            final var response = request.execute();

            final var result = objectMapper.readValue(response.getResult(), MpesaResultDTO.class);

            if (result != null) {
                result.setStatusCode(response.getStatusCode());
            }

            return result;

        } catch (Exception e) {
            log.error("❌ Erro ao consultar status M-Pesa: {}", e.getMessage(), e);
            throw new SystemException("UNSUCCESSFULL_OPERATION", e.getMessage(), e, "MPESA");
        }
    }
}