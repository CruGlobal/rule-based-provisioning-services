package org.ccci.idm.rules.test;

import java.util.List;

import org.ccci.idm.dao.IdentityDAO;
import org.ccci.idm.obj.IdentityUser;

public class MockIdentityDAO implements IdentityDAO
{

    @Override
    public void save(IdentityUser identityUser) throws Exception
    {
        // TODO Auto-generated method stub

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
    public IdentityUser loadBySsoGuid(String ssoguid) throws Exception
    {
        IdentityUser user = new IdentityUser();
        user.getEmployee().setEmployeeId("000123456");
        user.getAccount().setSsoguid(ssoguid);
        user.getAccount().setCn(ssoguid);
        user.getAccount().setUsername("test.user@ccci.org");
        user.getDesignation().setDesignationId("0123456");
        return user;
    }

}
