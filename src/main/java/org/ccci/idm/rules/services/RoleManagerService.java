package org.ccci.idm.rules.services;

import java.util.Collection;

import org.ccci.idm.obj.RoleAssignment;

/**
 * A Role Manager Service wraps a particular role management system, such as Grouper.
 * This allows querying for existing role assignments as well as granting and revoking
 * new role assignments.
 * 
 * @author Nathan.Kopp
 */
public interface RoleManagerService
{
    /**
     * Find all roles assigned to a particular user (identified by the userId) within the
     * context of the current role manager service.
     * 
     * @param userId
     * @return
     * @throws Exception
     */
    Collection<RoleAssignment> findExistingAssignedRoles(String userId) throws Exception;
    
    /**
     * Add a new role assignment to a user.  If the role is already assigned, do nothing.
     * The target user is identified in the RoleAssignment object.
     * 
     * @param r
     * @throws Exception
     */
    void assignRoleToPerson(RoleAssignment r) throws Exception;
    
    /**
     * Remove a role assignment from the user.
     * The target user is identified in the RoleAssignment object.
     * 
     * @param r
     * @throws Exception
     */
    void removeRoleFromPerson(RoleAssignment r) throws Exception;
    
    /**
     * Update the "expiration" date for a role assignment.
     * 
     * @param r
     * @throws Exception
     */
    void updateRoleExpiration(RoleAssignment r) throws Exception;
    
    /**
     * Convert the role's display name (in the ruleset) into a relative identifier for use
     * in the role management system.
     * 
     * @param string
     * @return
     */
    String convertRoleNameToId(String string);
    
    /**
     * Convert the role's display name (in the ruleset) into a absolute-path identifier
     * for use in the role management system. 
     * @param name
     * @return
     */
    String convertRoleNameToFullPath(String name);
    
    /**
     * Get the identifier for the user that this service logs in as when adding roles
     * to people.  Any roles added by this service will be attested by this identifier.
     * 
     * @return
     */
    String getAttestorId();

}
