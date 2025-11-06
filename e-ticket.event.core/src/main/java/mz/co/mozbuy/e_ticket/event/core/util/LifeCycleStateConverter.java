//package mz.co.mozbuy.e_ticket.event.core.util;
//
//import jakarta.persistence.AttributeConverter;
//import jakarta.persistence.Converter;
//import mz.co.mozbuy.e_ticket.event.core.model.LifeCycleState;
//
//@Converter(autoApply = true)
//public class LifeCycleStateConverter implements AttributeConverter<LifeCycleState, Integer> {
//
//    @Override
//    public Integer convertToDatabaseColumn(LifeCycleState attribute) {
//        return attribute != null ? attribute.getCode() : null;
//    }
//
//    @Override
//    public LifeCycleState convertToEntityAttribute(Integer dbData) {
//        return dbData != null ? LifeCycleState.fromCode(dbData) : LifeCycleState.ACTIVE;
//    }
//}