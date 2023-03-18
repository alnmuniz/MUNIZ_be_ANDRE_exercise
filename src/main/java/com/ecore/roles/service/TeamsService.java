package com.ecore.roles.service;

import com.ecore.roles.client.model.Team;
import com.ecore.roles.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

public interface TeamsService {

    Team getTeam(UUID id);

    List<Team> getTeams();

    /**
     * Checks if given userId is either a team lead or member of the team having id equals teamId.
     * 
     * @param teamId Id of team to check.
     * @param userId Id of user to check.
     * @return true if user is member or team lead, false otherwise.
     * @throws ResourceNotFoundException if team with given Id does not exist.
     */
    boolean isMemberOfTeam(UUID teamId, UUID userId);
}
