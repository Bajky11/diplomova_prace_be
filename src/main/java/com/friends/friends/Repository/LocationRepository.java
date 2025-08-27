package com.friends.friends.Repository;

import com.friends.friends.Entity.Location.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    

    List<Location> findByAddressContainingIgnoreCase(String address);
    

}
