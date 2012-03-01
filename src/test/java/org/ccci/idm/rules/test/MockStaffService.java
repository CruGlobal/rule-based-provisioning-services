package org.ccci.idm.rules.test;

import org.ccci.soa.pshr.client.Exception_Exception;
import org.ccci.soa.pshr.client.StaffService;
import org.ccci.soa.pshr.client.UsEmployeeInfo;
import org.ccci.soa.pshr.client.UsStaffMember;

public class MockStaffService implements StaffService
{

    @Override
    public UsStaffMember getStaff(String serverId, String serverSecret, String emplid) throws Exception_Exception
    {
        UsStaffMember staff = new UsStaffMember();
        UsEmployeeInfo employment = new UsEmployeeInfo();
        employment.setActiveStatus("A");
        employment.setEmplStatus("A");
        employment.setPaygroup("USS");
        staff.setEmploymentInfo(employment);
        return staff;
    }

}
