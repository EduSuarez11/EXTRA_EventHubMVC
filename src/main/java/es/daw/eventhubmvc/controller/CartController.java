package es.daw.eventhubmvc.controller;

import es.daw.eventhubmvc.dto.cart.AddToCartForm;
import es.daw.eventhubmvc.entity.Purchase;
import es.daw.eventhubmvc.model.Cart;
import es.daw.eventhubmvc.model.CartItem;
import es.daw.eventhubmvc.service.CatalogClientService;
import es.daw.eventhubmvc.service.PurchaseService;
import es.daw.eventhubmvc.dto.ticket.TicketTypeDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final Cart cart;
    private final CatalogClientService catalogClientService;
    private final PurchaseService purchaseService;

    @GetMapping("/cart")
    public String view(Model model) {
        model.addAttribute("items", cart.getItems());
        model.addAttribute("total", cart.getTotal());
        return "cart/view";
    }

    @PostMapping("/cart/add")
    public String add(
            @Valid @ModelAttribute("addToCart") AddToCartForm form,
            RedirectAttributes ra
    ) {
        // 1. En form tenemos los valores de los parámetros del post
        // 2. Ejecuta Bean Validation (@NotBlank,@Min(1)....
        // MEJORA!!! no estamos recibiendo BindingResult, Spring lanzará error ...
        // no estamos controlando la validación...

        // VALIDACIÓN DEFENSIVA: evita que alguien envíe un ticketTypeCode inventado
        // No nos fiamos del ticketTypeCode que viene del cliente.
//        <input type="hidden" name="eventCode" th:value="${event.code}"/>
//        <input type="hidden" name="ticketTypeCode" th:value="${t.code}"/>
        // Vamos al catálogo (api data rest) y para recuperar los TicketTypeDTO del evento.
        List<TicketTypeDTO> ticketTypes =
                catalogClientService.findTicketTypesByEventCode(form.eventCode())
                        .content();

//        TicketTypeDTO ticketType = ticketTypes.stream()
//                .filter(t -> form.ticketTypeCode().equals(t.code()))
//                .findFirst()
//                .orElse(null);

        // De forma imperativa... neceisto un TicketTypeDTO
        TicketTypeDTO ticketType = null;
        for (TicketTypeDTO t : ticketTypes) {
            if (form.ticketTypeCode().equals(t.code())){
                ticketType = t;
                break;
            }
        }

        // Validaciones defensivas
        // MEJORA!!!! i18n
        // El mensaje del addFlashAttribute debe estar acorde el locale... repasar Locale y MessageSource
        if (ticketType == null) {
            ra.addFlashAttribute("errorMessage", "Ticket Type Not Found");
            return "redirect:/events/"+form.eventCode();
        }

        // Validación defensiva...
        BigDecimal unitPrice = ticketType.basePrice() != null
                ? ticketType.basePrice()
                : BigDecimal.ZERO;

        // PENDIENTE JUEVES 12 MARZO
        // Antes de añadir el item al carrito:
        // - Obtengo el número total de items del ticket code en cuestión (por ejemplo 2 tickets VIP)
        // - Compruebo si la suma de la cantidad que quiero comprar: 3 + 2 (ya tengo) > límite (categoría VIP)
        // - Si es mayor: mensajito de que te has pasado torpedo!!!

        CartItem item = new CartItem(
                ticketType.code(),
                ticketType.category().name(),
                unitPrice,
                form.qty()
        );

        // Carrito: tenemos métodos simples, sin lógica de negocio. Métodos de compartamiento...
        cart.addOrIncrement(item);

        // ---------------------
        ra.addFlashAttribute("successMessage", "Added " + form.qty() + " ticket(s) to your cart.");
        // ---------------------

        return "redirect:/events/" + form.eventCode();
    }

    @PostMapping("/cart/update")
    public String update(
            @RequestParam String ticketTypeCode,
            @RequestParam int qty
    ) {
        cart.updateQty(ticketTypeCode, qty);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String remove(@RequestParam String ticketTypeCode) {
        cart.remove(ticketTypeCode);
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(Authentication authentication) {

        Purchase purchase = purchaseService
                .createPurchaseFromCart(authentication.getName(), cart);

        cart.clear();

        return "redirect:/purchases/" + purchase.getId();
    }
}
