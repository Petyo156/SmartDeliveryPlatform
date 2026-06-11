package org.tuvarna.smartdeliveryplatform.address.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tuvarna.smartdeliveryplatform.address.model.Address;
import org.tuvarna.smartdeliveryplatform.user.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findAllByUserOrderByIsDefaultDesc(User user);
    List<Address> findAllByUserAndIsDefaultTrue(User user);
    Optional<Address> findAddressByIdAndUser(UUID uuid, User user);
    Optional<Address> findFirstByUserOrderByIdAsc(User user);
    Optional<Address> findByUserAndIsDefaultTrue(User user);
    int countByUser(User user);
}
