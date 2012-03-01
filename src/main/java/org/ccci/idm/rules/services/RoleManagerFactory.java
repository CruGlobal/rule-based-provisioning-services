package org.ccci.idm.rules.services;

public interface RoleManagerFactory
{
    public RoleManagerService construct(String attestorId, String roleBasePath, boolean convertRoleNames);
}
