package org.openmrs.module.appointmentscheduling.api.db;

import java.util.Date;
import java.util.List;

import org.openmrs.module.appointmentscheduling.PaymentType;

public interface PaymentTypeDAO extends SingleClassDAO {
	
	public List<PaymentType> getPaymentTypes(String fuzzySearchPhrase, boolean includeRetired);
	
	boolean verifyDuplicatedPaymentTypeName(PaymentType paymentType);

}
