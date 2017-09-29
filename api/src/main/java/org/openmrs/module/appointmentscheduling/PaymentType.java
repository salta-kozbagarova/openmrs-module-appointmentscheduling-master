package org.openmrs.module.appointmentscheduling;

import org.openmrs.BaseOpenmrsMetadata;

public class PaymentType extends BaseOpenmrsMetadata {
	
	private static final long serialVersionUID = 1L;
	
	private Integer paymentTypeId;

    private boolean confidential = false;

    public PaymentType() {
		
	}
	
	public PaymentType(Integer paymentTypeId) {
		setId(paymentTypeId);
	}
	
	public PaymentType(String name, String description) {
		setName(name);
		setDescription(description);
	}
	
	public Integer getPaymentTypeId() {
		return paymentTypeId;
	}
	
	public void setPaymentTypeId(Integer paymentTypeId) {
		this.paymentTypeId = paymentTypeId;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getPaymentTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setPaymentTypeId(id);
	}
	
	public String getDisplayString() {
		return getName();
	}

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    public boolean isConfidential() {
        return confidential;
    }

}
