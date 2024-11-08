package com.mialeds.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mialeds.models.PermisosRoles;

@Repository
public interface PermisosRolesRepository extends JpaRepository<PermisosRoles, Integer> {


}
