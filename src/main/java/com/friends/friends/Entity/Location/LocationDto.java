package com.friends.friends.Entity.Location;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationDto {
    
    private Long id;
    private String address;
    private Double latitude;
    private Double longitude;
}
