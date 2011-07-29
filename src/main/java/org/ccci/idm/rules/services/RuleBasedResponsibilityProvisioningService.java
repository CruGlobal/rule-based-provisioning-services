package org.ccci.idm.rules.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ccci.idm.obj.RoleAssignment;
import org.ccci.idm.util.Util;
import org.ccci.soa.obj.USEmployment;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

public abstract class RuleBasedResponsibilityProvisioningService
{
    protected String roleBasePath;
    protected String attestorId;
    protected boolean convertRoleNames = true;
    
    protected KnowledgeBase kbase;
    

    public RuleBasedResponsibilityProvisioningService(String attestorId, String roleBasePath, boolean convertRoleNames)
    {
        super();
        this.attestorId = attestorId;
        this.roleBasePath = roleBasePath;
        this.convertRoleNames = convertRoleNames;
        kbase = KnowledgeBaseFactory.newKnowledgeBase();
        ResourceFactory.getResourceChangeNotifierService().start();
        ResourceFactory.getResourceChangeScannerService().start();
    }
    
    /**
     * Example of a changeset:
     * <change-set xmlns='http://drools.org/drools-5.0/change-set' xmlns:xs='http://www.w3.org/2001/XMLSchema-instance' xs:schemaLocation='http://drools.org/drools-5.0/change-set change-set-5.0.xsd' >
     *   <add>
     *       <resource source='classpath:org/domain/someRules.drl' type='DRL' />
     *       <resource source='classpath:org/domain/aFlow.drf' type='DRF' />
     *   </add>
     * </change-set>
     * @param changeSetFileName
     */
    public void addChangeSetRuleset(String changeSetFileName)
    {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        Resource res = loadResource(changeSetFileName);
        
        if (res == null) throw new RuntimeException("file not found");
        
        kbuilder.add(res, ResourceType.CHANGE_SET);
    
        if (kbuilder.hasErrors())
        {
            throw new RuntimeException("Ruleset SiebelResponsibilityProvisioningRules.xls has errors:\n" + kbuilder.getErrors());
        }
    
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }

    private Resource loadResource(String changeSetFileName)
    {
        Resource res;
        if(changeSetFileName.startsWith("http:"))
            res = ResourceFactory.newUrlResource(changeSetFileName);
        else if(changeSetFileName.startsWith("classpath:"))
            res = ResourceFactory.newClassPathResource(changeSetFileName.substring(10));
        else
            res = ResourceFactory.newFileResource(changeSetFileName);
        return res;
    }
    
    public void addDrlRuleset(String drlFileName)
    {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        Resource res = loadResource(drlFileName);
        if (res == null) throw new RuntimeException("file not found");
        
        kbuilder.add(res, ResourceType.DRL);
    
        if (kbuilder.hasErrors())
        {
            throw new RuntimeException("Ruleset SiebelResponsibilityProvisioningRules.xls has errors:\n" + kbuilder.getErrors());
        }
    
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }
    
    public void addPackageRuleset(String pkgFileName)
    {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        Resource res = loadResource(pkgFileName);
        if (res == null) throw new RuntimeException("file not found");
        
        kbuilder.add(res, ResourceType.PKG);
    
        if (kbuilder.hasErrors())
        {
            throw new RuntimeException("Ruleset SiebelResponsibilityProvisioningRules.xls has errors:\n" + kbuilder.getErrors());
        }
    
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }
    
    public void addExcelRuleset(String excelFileName, String... excelWorksheetNames)
    {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        Resource res = loadResource(excelFileName);
        if (res == null) throw new RuntimeException("file not found");
  
        for(String worksheetName : excelWorksheetNames)
        {
            DecisionTableConfiguration dtconf = KnowledgeBuilderFactory.newDecisionTableConfiguration();
            dtconf.setInputType(DecisionTableInputType.XLS);
            dtconf.setWorksheetName(worksheetName);
            kbuilder.add(res, ResourceType.DTABLE, dtconf);
        }
  
  
        if (kbuilder.hasErrors())
        {
            throw new RuntimeException("Ruleset SiebelResponsibilityProvisioningRules.xls has errors:\n" + kbuilder.getErrors());
        }
  
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
    }

    public String computeAndApplyRolesForEmployee(String ssoGuid, USEmployment employment) throws Exception
    {
        Collection<RoleAssignment> allExistingAssignments = findExistingAssignedRoles(ssoGuid);
        Collection<RoleAssignment> externalExistingAssignments = filterExistingRolesKeepExternal(allExistingAssignments);
        Collection<RoleAssignment> newAssignments = computeNewRoleAssignments(employment, ssoGuid, externalExistingAssignments);
        
        applyRoleAssignments(newAssignments, allExistingAssignments);

        return null;
    }

    private void applyRoleAssignments(Collection<RoleAssignment> newAssignments, Collection<RoleAssignment> allExistingAssignments) throws Exception
    {
        // add any new assignments
        for (RoleAssignment newOne : newAssignments)
        {
            RoleAssignment found = null;
            for (RoleAssignment r2 : allExistingAssignments)
            {
                if (r2.matches(newOne))
                {
                    found = r2;
                    break;
                }
            }
            if (found==null)
            {
                assignRoleToPerson(newOne);
            }
            else if(!thisIsAttestor(found) && thisIsAttestor(newOne))
            {
                // it now matches the rules, so let's replace to claim attestation
                removeRoleFromPerson(newOne);
                assignRoleToPerson(newOne);
            }
        }

        // remove any assignments that don't apply any more
        for (RoleAssignment r2 : allExistingAssignments)
        {
            boolean found = false;
            for (RoleAssignment r : newAssignments)
            {
                if (r2.matches(r))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
                removeRoleFromPerson(r2);
        }
    }


    private List<RoleAssignment> computeNewRoleAssignments(USEmployment employment, String ssoGuid,  Collection<RoleAssignment> externalExistingAssignments)
    {
        StatefulKnowledgeSession ksession = setupRulesSession(employment, kbase, externalExistingAssignments);

        ksession.fireAllRules();
        
        List<RoleAssignment> newAssignments = extractRoleAssignmentsFromSession(ksession);

        massageDataForNewRoles(newAssignments, ssoGuid);
        newAssignments = removeDuplicateNewRoles(newAssignments, ssoGuid);

        return newAssignments;
    }

    private List<RoleAssignment> removeDuplicateNewRoles(List<RoleAssignment> newAssignments, String ssoGuid)
    {
        List<RoleAssignment> output = new ArrayList<RoleAssignment>();
        
        for(int i=0; i<newAssignments.size(); i++)
        {
            RoleAssignment newOne = (RoleAssignment)newAssignments.get(i);
            
            boolean shouldAdd = true;
            for(RoleAssignment alreadySelected : output)
            {
                if(alreadySelected.matches(newOne))
                {
                    // keep the one attested by us
                    if(!thisIsAttestor(alreadySelected) && thisIsAttestor(newOne))
                    {
                        // swap - keep
                        output.remove(alreadySelected);
                        output.add(newOne);
                        break; // to avoid exception caused by remove()
                    }
                    else
                    {
                        shouldAdd = false;
                    }
                }
            }
            if(shouldAdd) output.add(newOne);
        }
        return output;
    }

    private List<RoleAssignment> extractRoleAssignmentsFromSession(StatefulKnowledgeSession ksession)
    {
        List<RoleAssignment> assignments = new ArrayList<RoleAssignment>();
        for(Object fact : ksession.getObjects())
        {
            if(fact instanceof RoleAssignment) assignments.add((RoleAssignment)fact);
        }
        return assignments;
    }

    private void massageDataForNewRoles(Collection<RoleAssignment> newAssignments, String ssoGuid)
    {
        for(RoleAssignment r : newAssignments)
        {
            if(!r.isExisting() && Util.isBlank(r.getAttestorId()))
            {
                r.setAttestorId(attestorId);
                r.setAssigneeId(ssoGuid);
                r.setRoleId(convertRoleNameToFullPath(r.getRoleId()));
            }
        }
    }

    private StatefulKnowledgeSession setupRulesSession(USEmployment employment, KnowledgeBase kbase, Collection<RoleAssignment> externalExistingAssignments)
    {
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        FactHandle employmentHandle = ksession.insert(employment);
        for (RoleAssignment role : externalExistingAssignments)
        {
            FactHandle roleHandle = ksession.insert(role);
        }
        return ksession;
    }


    private Collection<RoleAssignment> filterExistingRolesKeepExternal(Collection<RoleAssignment> allExistingAssignments)
    {
        Collection<RoleAssignment> externalAssignments = new ArrayList<RoleAssignment>();
        for (RoleAssignment assignment : allExistingAssignments)
        {
            if (!thisIsAttestor(assignment))
                externalAssignments.add(assignment);
        }
        return externalAssignments;
    }

    private boolean thisIsAttestor(RoleAssignment assignment)
    {
      return attestorId.equalsIgnoreCase(assignment.getAttestorId());
    }



    private String convertRoleNameToFullPath(String name)
    {
        if(roleBasePath==null) return convertRoleNames?convertRoleNameToId(name):name;
        else return roleBasePath+":"+(convertRoleNames?convertRoleNameToId(name):name);
    }
    
    protected abstract Collection<RoleAssignment> findExistingAssignedRoles(String globalId) throws Exception;
    protected abstract void assignRoleToPerson(RoleAssignment r) throws Exception;
    protected abstract void removeRoleFromPerson(RoleAssignment r) throws Exception;
    protected abstract String convertRoleNameToId(String string);

}
