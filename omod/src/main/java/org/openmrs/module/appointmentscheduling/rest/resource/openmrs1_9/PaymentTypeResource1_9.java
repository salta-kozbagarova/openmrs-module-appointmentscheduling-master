package org.openmrs.module.appointmentscheduling.rest.resource.openmrs1_9;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointmentscheduling.AppointmentType;
import org.openmrs.module.appointmentscheduling.PaymentType;
import org.openmrs.module.appointmentscheduling.api.AppointmentService;
import org.openmrs.module.appointmentscheduling.rest.controller.AppointmentRestController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + AppointmentRestController.APPOINTMENT_SCHEDULING_REST_NAMESPACE
+ "/paymenttype", supportedClass = PaymentType.class, supportedOpenmrsVersions = {"1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*"})
public class PaymentTypeResource1_9 extends MetadataDelegatingCrudResource<PaymentType> {
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display", findMethod("getDisplayString"));
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("confidential");
			description.addProperty("retired");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display", findMethod("getDisplayString"));
			description.addProperty("name");
			description.addProperty("description");
            description.addProperty("confidential");
			description.addProperty("retired");
			description.addProperty("auditInfo", findMethod("getAuditInfo"));
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("name");
		description.addRequiredProperty("description");
        description.addProperty("confidential");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getUpdatableProperties() {
		return getCreatableProperties();
	}
	
	@Override
	public PaymentType newDelegate() {
		return new PaymentType();
	}
	
	@Override
	public PaymentType save(PaymentType paymentType) {
		return Context.getService(AppointmentService.class).savePaymentType(paymentType);
	}
	
	@Override
	public PaymentType getByUniqueId(String uuid) {
		return Context.getService(AppointmentService.class).getPaymentTypeByUuid(uuid);
	}
	
	@Override
	public void purge(PaymentType paymentType, RequestContext requestContext) throws ResponseException {
		if (paymentType == null) {
			return;
		}
		Context.getService(AppointmentService.class).purgePaymentType(paymentType);
	}
	
	@Override
	protected NeedsPaging<PaymentType> doGetAll(RequestContext context) {
		return new NeedsPaging<PaymentType>(Context.getService(AppointmentService.class).getAllPaymentTypesSorted(
		    context.getIncludeAll()), context);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		return new NeedsPaging<PaymentType>(Context.getService(AppointmentService.class).getPaymentTypes(
		    context.getParameter("q"), context.getIncludeAll()), context);
	}
	
}
