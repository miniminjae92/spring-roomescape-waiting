package roomescape.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReservationViewController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/reservation")
    public String reservation() {
        return "reservation";
    }

    @GetMapping("/my-reservation")
    public String myReservation() {
        return "my-reservation";
    }

    @GetMapping("/login")
    public String login() {
        return "auth";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/admin/theme")
    public String adminTheme() {
        return "admin-theme";
    }

    @GetMapping("/admin/date")
    public String adminDate() {
        return "admin-date";
    }

    @GetMapping("/admin/time")
    public String adminTime() {
        return "admin-time";
    }

    @GetMapping("/popular-themes")
    public String popularThemes() {
        return "popular-themes";
    }

    @GetMapping("/admin/slot")
    public String adminSlot() {
        return "admin-slot";
    }

    @GetMapping("/admin/reservation")
    public String adminReservation() {
        return "admin-reservation";
    }
}
