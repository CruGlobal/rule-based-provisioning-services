package org.ccci.idm.rules.test;

import java.util.List;

import org.ccci.idm.dao.IdentityDAO;
import org.ccci.idm.obj.IdentityUser;

public class MockIdentityDAO implements IdentityDAO
{

    @Override public void updateEmailVerified(IdentityUser identityUser) throws Exception
    {
    }

    @Override
    public boolean save(IdentityUser identityUser) throws Exception
    {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public IdentityUser load(IdentityUser identityUser) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IdentityUser> find(IdentityUser identityUser) throws Exception
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() throws Exception
    {
        // TODO Auto-generated method stub

    }

    @Override
    public IdentityUser loadBySsoGuidOrUsername(String ssoGuidOrUsername) throws Exception
    {
        IdentityUser user = new IdentityUser();
        user.getEmployee().setEmployeeId("000123456");
        user.getAccount().setSsoguid(ssoGuidOrUsername);
        user.getAccount().setCn(ssoGuidOrUsername);
        user.getAccount().setUsername("test.user@ccci.org");
        user.getDesignation().setDesignationId("0123456");
        return user;
    }
}
