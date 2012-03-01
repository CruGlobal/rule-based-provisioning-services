package org.ccci.idm.rules.services;

import org.ccci.idm.obj.IdentityUser;

public interface FactProvider
{
    public String getFactName();
    public Class<?> getFactType();
    public Object getFact(IdentityUser identityUser);
}
