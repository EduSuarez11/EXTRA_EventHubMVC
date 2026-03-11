package es.daw.eventhubmvc.controller;

import es.daw.eventhubmvc.dto.cart.AddToCartForm;
import es.daw.eventhubmvc.dto.event.EventDTO;
import es.daw.eventhubmvc.dto.event.ResponseEvents;
import es.daw.eventhubmvc.dto.ticket.TicketTypeDTO;
import es.daw.eventhubmvc.service.CatalogClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventsController {

    private final CatalogClientService catalogClientService;

    @GetMapping("/events")
    public String listEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "startDateTime") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(required = false) String locationCode,
            Model model
    ) {

        ResponseEvents response;

        if (locationCode == null || locationCode.isBlank()) {
            response = catalogClientService.listEvents(page, size, sort, dir);
        } else {
             //Cuando filtro por ubicación, ignoro los parámetros de paginación y ordenación
            response = catalogClientService.findEventsByLocationCode(locationCode, page, size, sort, dir);
        }

        List<EventDTO> events = response.content();

        model.addAttribute("events", events);
        model.addAttribute("page", response.page());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("locationCode", locationCode);

        return "events/list";
    }

    @GetMapping("/events/{eventCode}")
    public String detail(@PathVariable String eventCode, Model model) {

        EventDTO event = catalogClientService.findEventByCode(eventCode);

        List<TicketTypeDTO> ticketTypes =
                catalogClientService.findTicketTypesByEventCode(eventCode)
                        .content();

        model.addAttribute("event", event);
        model.addAttribute("ticketTypes", ticketTypes);

        if (!model.containsAttribute("addToCart")) {
            model.addAttribute("addToCart", new AddToCartForm(eventCode, "", 1));
        }

        return "events/detail";
    }

}
