package org.openmrs.module.appointmentscheduling.rest.resource.openmrs1_9;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointmentscheduling.AppointmentBlock;
import org.openmrs.module.appointmentscheduling.AppointmentType;
import org.openmrs.module.appointmentscheduling.TimeSlot;
import org.openmrs.module.appointmentscheduling.api.AppointmentService;
import org.openmrs.module.appointmentscheduling.rest.controller.AppointmentRestController;
import org.openmrs.module.appointmentscheduling.rest.resource.openmrs1_9.util.AppointmentRestUtils;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + AppointmentRestController.APPOINTMENT_SCHEDULING_REST_NAMESPACE
        + "/appointmentblock", supportedClass = AppointmentBlock.class, supportedOpenmrsVersions = {"1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*"})
public class AppointmentBlockResource1_9 extends DataDelegatingCrudResource<AppointmentBlock> {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("appointmentBlockId");
			description.addProperty("uuid");
			description.addProperty("display", findMethod("getDisplayString"));
			description.addProperty("startDate");
			description.addProperty("endDate");
			description.addProperty("provider", Representation.DEFAULT);
			description.addProperty("location", Representation.REF);
			description.addProperty("types", Representation.REF);
			description.addProperty("appointmentBlockType");
			description.addProperty("timeSlotLength");
			description.addProperty("nursesQuantity");
			description.addProperty("additionalProviders", Representation.REF);
			description.addProperty("paymentTypes", Representation.REF);
			description.addProperty("voided");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("appointmentBlockId");		// should I remove it?
			description.addProperty("uuid");
			description.addProperty("display", findMethod("getDisplayString"));
			description.addProperty("startDate");
			description.addProperty("endDate");
			description.addProperty("provider", Representation.FULL);
			description.addProperty("location", Representation.FULL);
			description.addProperty("types", Representation.FULL);
			description.addProperty("appointmentBlockType");
			description.addProperty("timeSlotLength");
			description.addProperty("nursesQuantity");
			description.addProperty("additionalProviders", Representation.FULL);
			description.addProperty("paymentTypes", Representation.FULL);
			description.addProperty("voided");
			description.addProperty("auditInfo", findMethod("getAuditInfo"));
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("startDate");
		description.addRequiredProperty("endDate");
		description.addRequiredProperty("location");
		description.addRequiredProperty("types");
		description.addRequiredProperty("appointmentBlockType");
		description.addProperty("timeSlotLength");
		description.addProperty("provider");
		description.addProperty("nursesQuantity");
		description.addProperty("additionalProviders");
		description.addProperty("paymentTypes");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		return getCreatableProperties();
	}
	
	@Override
	public AppointmentBlock newDelegate() {
		return new AppointmentBlock();
	}
	
	@Override
	public AppointmentBlock save(AppointmentBlock appointmentBlock) {
		AppointmentService appointmentService = Context.getService(AppointmentService.class);
		
		appointmentBlock =  appointmentService.saveAppointmentBlock(appointmentBlock);
		Integer slotLength = appointmentBlock.getTimeSlotLength();
		long appointmentBlocklengthInMinutes = (appointmentBlock.getEndDate().getTime() - appointmentBlock.getStartDate().getTime()) / 60000;
		
		int howManyTimeSlotsToCreate = (int) (appointmentBlocklengthInMinutes / slotLength);
        List<TimeSlot> currentTimeSlots = appointmentService.getTimeSlotsInAppointmentBlock(appointmentBlock);
        
        if (currentTimeSlots.size() != howManyTimeSlotsToCreate) { //the time slot length changed therefore we need to update.
            //First we will purge the current time slots.
            for (TimeSlot timeSlot : currentTimeSlots) {
                appointmentService.purgeTimeSlot(timeSlot);
            }
            //Then we will add the new time slots corresponding to the new time slot length
            Date startDate = appointmentBlock.getStartDate();
            Date endDate;
            Calendar cal;
            //Create the time slots except the last one because it might be larger from the rest.
            for (int i = 0; i < howManyTimeSlotsToCreate - 1; i++) {
                cal = Context.getDateTimeFormat().getCalendar();
                cal.setTime(startDate);
                cal.add(Calendar.MINUTE, slotLength); // add slotLength minutes
                endDate = cal.getTime();
                TimeSlot timeSlot = new TimeSlot(appointmentBlock, startDate, endDate);
                startDate = endDate;
                appointmentService.saveTimeSlot(timeSlot);
            }
            TimeSlot timeSlot = new TimeSlot(appointmentBlock, startDate, appointmentBlock.getEndDate());
            appointmentService.saveTimeSlot(timeSlot);
        }
        
		return appointmentBlock;
	}
	
	@Override
	public AppointmentBlock getByUniqueId(String uuid) {
		return Context.getService(AppointmentService.class).getAppointmentBlockByUuid(uuid);
	}
	
	@Override
	protected void delete(AppointmentBlock appointmentBlock, String reason, RequestContext context) throws ResponseException {
		if (appointmentBlock.isVoided()) {
			return;
		}
		Context.getService(AppointmentService.class).voidAppointmentBlock(appointmentBlock, reason);
	}
	
	@Override
	public void purge(AppointmentBlock appointmentBlock, RequestContext requestContext) throws ResponseException {
		if (appointmentBlock == null) {
			return;
		}
		Context.getService(AppointmentService.class).purgeAppointmentBlock(appointmentBlock);
	}
	
	@Override
	protected NeedsPaging<AppointmentBlock> doGetAll(RequestContext context) {
		return new NeedsPaging<AppointmentBlock>(Context.getService(AppointmentService.class).getAllAppointmentBlocks(
		    context.getIncludeAll()), context);
	}
	
	/**
	 * Returns a list of AppointmentBlocks that fall within the given constraints
	 * 
	 * @param appointmentType - Type of the appointment this block must support
	 * @param fromDate - (optional) earliest start date.
	 * @param toDate - (optional) latest start date.
	 * @param context - (optional) the appointment block's provider.
	 * @param location - (optional) the appointment block's location. (or predecessor location)t
	 * @return list of AppointmentBlocks that fall within the given constraints
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		
		Date startDate = context.getParameter("fromDate") != null ? (Date) ConversionUtil.convert(
		    context.getParameter("fromDate"), Date.class) : null;
		
		Date endDate = context.getParameter("toDate") != null ? (Date) ConversionUtil.convert(
		    context.getParameter("toDate"), Date.class) : null;
		
		List<AppointmentType> types = AppointmentRestUtils.getAppointmentTypes(context);
		
		Provider provider = context.getParameter("provider") != null ? Context.getProviderService().getProviderByUuid(
		    context.getParameter("provider")) : null;
		
		// for some reason the getAppointmentBlocks service method takes a comma-separated string of location ids instead of a list of location
		String location = context.getParameter("location") != null ? Context.getLocationService()
		        .getLocationByUuid(context.getParameter("location")).getId().toString() : null;

		return new NeedsPaging<AppointmentBlock>(Context.getService(AppointmentService.class).getAppointmentBlocksByTypes(
				startDate, endDate, location, provider, types), context);
	}
	
	public String getDisplayString(AppointmentBlock appointmentBlock) {
		return appointmentBlock.getProvider() + ", " + appointmentBlock.getLocation() + ": "
		        + appointmentBlock.getStartDate() + " - " + appointmentBlock.getEndDate();
	}

	//START :: Custom functions
	public Integer getTimeSlotLength (AppointmentBlock appointmentBlock) {
		List<TimeSlot> timeSlots = Context.getService(AppointmentService.class).getTimeSlotsInAppointmentBlock(appointmentBlock);
		if (timeSlots != null && !timeSlots.isEmpty()) {
			TimeSlot timeSlot = Context.getService(AppointmentService.class).getTimeSlotsInAppointmentBlock(appointmentBlock).get(0);
			return (int) ((timeSlot.getEndDate().getTime() - timeSlot.getStartDate().getTime())/60000);
		}
		return 0;
	}
	// END :: Custom functions
}
