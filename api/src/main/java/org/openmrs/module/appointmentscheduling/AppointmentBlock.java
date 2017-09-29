/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.appointmentscheduling;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;
import org.openmrs.Provider;

import java.util.Date;
import java.util.Set;

public class AppointmentBlock extends BaseOpenmrsData {
	
	private static final long serialVersionUID = 1L;
	
	private Integer appointmentBlockId;
	
	private Date startDate;
	
	private Date endDate;
	
	private Provider provider;
	
	private Location location;

	// Custom column
	private String appointmentBlockType;
	
	private Integer timeSlotLength;
	
	private Set<AppointmentType> types;
	
	private Integer nursesQuantity;
	
	private Set<Provider> additionalProviders;
	
	private Set<PaymentType> paymentTypes;
	
	public AppointmentBlock() {}
	
	public AppointmentBlock(Integer appointmentBlockId) {
		setId(appointmentBlockId);
	}

	public AppointmentBlock(Date startDate, Date endDate, Provider provider, Location location, Set<AppointmentType> types, String appointmentBlockType) {
		setStartDate(startDate);
		setEndDate(endDate);
		setProvider(provider);
		setLocation(location);
		setTypes(types);
		setAppointmentBlockType(appointmentBlockType);
	}
	
	public Integer getAppointmentBlockId() {
		return appointmentBlockId;
	}
	
	public void setAppointmentBlockId(Integer appointmentBlockId) {
		this.appointmentBlockId = appointmentBlockId;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getAppointmentBlockId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setAppointmentBlockId(id);
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public Provider getProvider() {
		return provider;
	}
	
	public void setProvider(Provider provider) {
		this.provider = provider;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public Set<AppointmentType> getTypes() {
		return types;
	}
	
	public void setTypes(Set<AppointmentType> types) {
		this.types = types;
	}

	public String getAppointmentBlockType() {
		return appointmentBlockType;
	}

	public void setAppointmentBlockType(String appointmentBlockType) {
		this.appointmentBlockType = appointmentBlockType;
	}
	
	public Integer getTimeSlotLength() {
		return timeSlotLength;
	}
	
	public void setTimeSlotLength(Integer timeSlotLength) {
		this.timeSlotLength = timeSlotLength;
	}
	
	public Integer getNursesQuantity() {
		return nursesQuantity;
	}
	
	public void setNursesQuantity(Integer nursesQuantity) {
		this.nursesQuantity = nursesQuantity;
	}
	
	public Set<Provider> getAdditionalProviders() {
		return additionalProviders;
	}
	
	public void setAdditionalProviders(Set<Provider> additionalProviders) {
		this.additionalProviders = additionalProviders;
	}
	
	public Set<PaymentType> getPaymentTypes() {
		return paymentTypes;
	}
	
	public void setPaymentTypes(Set<PaymentType> paymentTypes) {
		this.paymentTypes = paymentTypes;
	}
	
}
