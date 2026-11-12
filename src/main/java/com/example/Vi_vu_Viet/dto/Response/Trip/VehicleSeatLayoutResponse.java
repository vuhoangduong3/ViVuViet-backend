package com.example.Vi_vu_Viet.dto.Response.Trip;
import lombok.Data;
import java.util.List;
import java.util.UUID;
@Data
public class VehicleSeatLayoutResponse {
    private UUID vehicleId;
    private UUID seatLayoutId;
    private String name;
    private List<Seat> seats;
    @Data
    public static class Seat {
        private UUID id;
        private String seatCode;
        private String seatClass;
        private Integer deck;
        private Integer rowNo;
        private Integer columnNo;
    }
}
