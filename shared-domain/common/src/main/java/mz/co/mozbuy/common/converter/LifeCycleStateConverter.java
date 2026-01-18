package mz.co.mozbuy.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import mz.co.mozbuy.common.audit.LifeCycleState;

@Converter(autoApply = true)
public class LifeCycleStateConverter implements AttributeConverter<LifeCycleState, String> {

    @Override
    public String convertToDatabaseColumn(LifeCycleState attribute) {
        if (attribute == null) {
            return "ACTIVE";
        }
        // Salva o nome do Enum como string
        return attribute.name();
    }

    @Override
    public LifeCycleState convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return LifeCycleState.ACTIVE;
        }
        try {
            return LifeCycleState.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // Se houver valores numéricos no banco
            return LifeCycleState.ACTIVE;
        }
    }
}