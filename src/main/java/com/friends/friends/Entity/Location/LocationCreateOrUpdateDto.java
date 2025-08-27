package com.friends.friends.Entity.Location;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationCreateOrUpdateDto {
    @NotBlank(message = "Adresa je povinná")
    private String address;
    @NotNull(message = "Zeměpisná šířka je povinná")
    private Double latitude;
    @NotNull(message = "Zeměpisná délka je povinná")
    private Double longitude;
}
