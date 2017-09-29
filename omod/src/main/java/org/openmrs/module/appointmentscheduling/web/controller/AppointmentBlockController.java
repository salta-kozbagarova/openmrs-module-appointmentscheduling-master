package org.openmrs.module.appointmentscheduling.web.controller;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointmentscheduling.Appointment;
import org.openmrs.module.appointmentscheduling.AppointmentBlock;
import org.openmrs.module.appointmentscheduling.AppointmentType;
import org.openmrs.module.appointmentscheduling.TimeSlot;
import org.openmrs.module.appointmentscheduling.api.AppointmentService;
import org.openmrs.module.appointmentscheduling.rest.controller.AppointmentRestController;
import org.openmrs.module.appointmentscheduling.validator.AppointmentBlockValidator;
import org.openmrs.module.appointmentscheduling.web.AppointmentBlockEditor;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + AppointmentRestController.APPOINTMENT_SCHEDULING_REST_NAMESPACE + "/appointmentblockcontroller")
public class AppointmentBlockController {

    /** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<AppointmentBlock> save(HttpServletRequest request) throws Exception {

        if (Context.isAuthenticated()) {

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            } finally {
                reader.close();
            }
            ObjectMapper mapper = new ObjectMapper();

            JsonNode rootNode = mapper.readTree(sb.toString());
            JsonNode providerNode = rootNode.path("provider");
            JsonNode locationNode = rootNode.path("location");
            JsonNode timeSlotLengthNode = rootNode.path("timeSlotLength");
            JsonNode startDateNode = rootNode.path("startDate");
            JsonNode endDateNode = rootNode.path("endDate");
            JsonNode typesNode = rootNode.path("types");
            JsonNode appointmentBlockTypeNode = rootNode.path("appointmentBlockType");
            JsonNode appointmentBlockId = rootNode.path("appointmentBlockId");
            JsonNode nursesQuantity = rootNode.path("nursesQuantity");
            JsonNode additionalProviders = rootNode.path("additionalProviders");
            JsonNode paymentTypes = rootNode.path("paymentTypes");

            SimpleDateFormat sdf = OpenmrsUtil.getDateTimeFormat(Locale.ENGLISH);
            Date fromDate = sdf.parse(startDateNode.getTextValue());
            Date toDate = sdf.parse(endDateNode.getTextValue());

            ProviderService providerService = Context.getProviderService();
            LocationService locationService = Context.getLocationService();
            AppointmentService appointmentService = Context.getService(AppointmentService.class);

            Provider provider = providerService.getProviderByUuid(providerNode.getTextValue());
            Location location = locationService.getLocationByUuid(locationNode.getTextValue());
            Set< AppointmentType > types = new HashSet<AppointmentType>();
            String appointmentBlockType = appointmentBlockTypeNode.getTextValue();

            ArrayList<String> typesList = new ArrayList<String>();
            Iterator<JsonNode> iterator = typesNode.getElements();
            while(iterator.hasNext()) {
                JsonNode type = iterator.next();
                typesList.add(type.getTextValue());
            }
            Object[] objects = typesList.toArray();
            String[] typeValuesUUID = Arrays.copyOf(objects,objects.length,String[].class);

            for (String aTypeValuesUUID : typeValuesUUID) {
                types.add(appointmentService.getAppointmentTypeByUuid(aTypeValuesUUID));
            }
            AppointmentBlock appointmentBlock = new AppointmentBlock(fromDate, toDate, provider, location, types, appointmentBlockType);

            if (!appointmentBlockId.isNull() && !appointmentBlockId.isMissingNode()) {
                appointmentBlock.setId(appointmentBlockId.getIntValue());
            }

//            //Check if overlapping appointment blocks exist in the system(We will consider Time And Provider only)
//            if (appointmentService.getOverlappingAppointmentBlocks(appointmentBlock).size() > 0) { //Overlapping exists
//                return new ResponseEntity<AppointmentBlock>(HttpStatus.BAD_REQUEST);
//            }
            //First we need to save the appointment block (before creating the time slot)
            appointmentBlock = appointmentService.saveAppointmentBlock(appointmentBlock);

            //Create the time slots.
            Integer slotLength = Integer.parseInt(timeSlotLengthNode.getTextValue());
            long appointmentBlocklengthInMinutes = (appointmentBlock.getEndDate().getTime() - appointmentBlock.getStartDate().getTime()) / 60000;
            int howManyTimeSlotsToCreate = (int) (appointmentBlocklengthInMinutes / slotLength);
            List<TimeSlot> currentTimeSlots = appointmentService.getTimeSlotsInAppointmentBlock(appointmentBlock);
            // TODO :: If the number of current TimeSLots and TimeSlots to create are equal, timeSlots won't be updated. Do I need to invent a new algorythm?
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
            return new ResponseEntity<AppointmentBlock>(HttpStatus.CREATED);
        }
        return new ResponseEntity<AppointmentBlock>(HttpStatus.UNAUTHORIZED);
    }
}